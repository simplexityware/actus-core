/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */

package org.actus.contracts;

import org.actus.AttributeConversionException;
import org.actus.attributes.ContractModel;
import org.actus.attributes.ContractModelProvider;
import org.actus.events.ContractEvent;
import org.actus.events.EventFactory;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.stk.STF_PRD_STK;
import org.actus.functions.stk.STF_TD_STK;
import org.actus.functions.swaps.*;
import org.actus.states.StateSpace;
import org.actus.types.EventType;
import org.actus.types.ReferenceRole;
import org.actus.util.CommonUtils;
import org.actus.types.ContractReference;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the Swap payoff algorithm
 *
 * @see <a href="https://www.actusfrf.org"></a>
 */
public final class Swap {

    // forward projection of the entire lifecycle of the contract
    public static ArrayList<ContractEvent> schedule(LocalDateTime to,
                                                    ContractModelProvider model) throws AttributeConversionException {
        ArrayList<ContractEvent> events = new ArrayList<ContractEvent>();
        //create children event schedule
        ContractModel firstLegModel = (ContractModel)model.<List<ContractReference>>getAs("ContractStructure").stream().filter(c-> ReferenceRole.FIL.equals(c.referenceRole)).collect(Collectors.toList()).get(0).getObject();
        ContractModel secondLegModel = (ContractModel)model.<List<ContractReference>>getAs("ContractStructure").stream().filter(c-> ReferenceRole.SEL.equals(c.referenceRole)).collect(Collectors.toList()).get(0).getObject();
        ArrayList<ContractEvent> firstLegSchedule = new ArrayList<>();
        ArrayList<ContractEvent> secondLegSchedule = new ArrayList<>();
        firstLegSchedule = ContractType.schedule(firstLegModel.getAs("MaturityDate"),firstLegModel);
        secondLegSchedule = ContractType.schedule(secondLegModel.getAs("MaturityDate"), secondLegModel);
        events.addAll(firstLegSchedule);
        events.addAll(secondLegSchedule);
        // compute parent events
        // purchase
        if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            ContractEvent purchase = EventFactory.createEvent(
                    model.getAs("PurchaseDate"),
                    EventType.PRD,
                    model.getAs("Currency"),
                    new POF_PRD_SWAPS(),
                    new STF_PRD_STK()
            );
            events.removeIf(e -> e.compareTo(purchase) == -1); // remove all pre-purchase events
            events.add(purchase);
        }
        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            ContractEvent termination = EventFactory.createEvent(
                    model.getAs("TerminationDate"),
                    EventType.TD,
                    model.getAs("Currency"),
                    new POF_TD_SWAPS(),
                    new STF_TD_STK()
            );
            events.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
            events.add(termination);
        }

        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), EventType.AD, model.getAs("Currency"), null, null)) == -1);

        // remove all post to-date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(to, EventType.AD, model.getAs("Currency"), null, null)) == 1);

        // return events
        return events;
    }

    // apply a set of events to the current state of a contract and return the post events state
    public static ArrayList<ContractEvent> apply(ArrayList<ContractEvent> events,
                                                 ContractModelProvider model,
                                                 RiskFactorModelProvider observer) throws AttributeConversionException {

        //remove all possibly congruent events
        events.removeAll(events.stream().filter(event -> EventType.IED.equals(event.type()) || EventType.PR.equals(event.type()) || EventType.IP.equals(event.type())).collect(Collectors.toCollection(ArrayList::new)));

        //create children event schedule
        ContractModel firstLegModel = (ContractModel)model.<List<ContractReference>>getAs("ContractStructure").get(0).getObject();
        ContractModel secondLegModel = (ContractModel)model.<List<ContractReference>>getAs("ContractStructure").get(1).getObject();
        ArrayList<ContractEvent> firstLegSchedule = new ArrayList<>();
        ArrayList<ContractEvent> secondLegSchedule = new ArrayList<>();
        //TODO: what if to(parameter) != MaturityDate
        firstLegSchedule = ContractType.schedule(firstLegModel.getAs("MaturityDate"),firstLegModel);
        secondLegSchedule = ContractType.schedule(secondLegModel.getAs("MaturityDate"), secondLegModel);

        // apply shedule
        List<ContractEvent> firstLegEvents = ContractType.apply(firstLegSchedule,firstLegModel,observer);
        List<ContractEvent> secondLegEvents = ContractType.apply(secondLegSchedule,secondLegModel,observer);

        //remove all post termination date events
        if(!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            ContractEvent terminationEvent = events.stream().filter(event -> EventType.TD.equals(event.type())).collect(Collectors.toList()).get(0);
            firstLegEvents.removeIf(e -> e.compareTo(terminationEvent) == 1);
            secondLegEvents.removeIf(e -> e.compareTo(terminationEvent) == 1);
        }

        // remove all pre-status date events
        firstLegEvents.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), EventType.AD, model.getAs("Currency"), null, null)) == -1);
        secondLegEvents.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), EventType.AD, model.getAs("Currency"), null, null)) == -1);

        // remove all post to-date events
        firstLegEvents.removeIf(e -> e.compareTo(EventFactory.createEvent(firstLegModel.getAs("MaturityDate"), EventType.AD, model.getAs("Currency"), null, null)) == 1);
        secondLegEvents.removeIf(e -> e.compareTo(EventFactory.createEvent(secondLegModel.getAs("MaturityDate"), EventType.AD, model.getAs("Currency"), null, null)) == 1);

        events.addAll(filterAndNettCongruentEvents(firstLegEvents,secondLegEvents));

        Collections.sort(events);

        // initialize state space per status date
        StateSpace state = initStateSpace(model, events.get(0));
        //TODO: what if children contracts have different DayCount and/or BusinessDay-Convention
        events.forEach(event -> event.eval(state, model, observer, firstLegModel.getAs("DayCountConvention"), firstLegModel.getAs("BusinessDayConvention")));
        //
        return events;
    }
    private static StateSpace initStateSpace(ContractModelProvider model, ContractEvent eventAtT0) throws AttributeConversionException {
        ContractModel firstLegModel = (ContractModel)model.<List<ContractReference>>getAs("ContractStructure").get(0).getObject();
        ContractModel secondLegModel = (ContractModel)model.<List<ContractReference>>getAs("ContractStructure").get(1).getObject();

        StateSpace states = CommonUtils.isNull(eventAtT0.states().statusDate) ? new StateSpace() : eventAtT0.states();
        states.statusDate = model.getAs("StatusDate");
        states.contractPerformance = !CommonUtils.isNull(model.getAs("ContractPerformance")) ? model.getAs("ContractPerformance") : null;
        states.maturityDate = firstLegModel.<LocalDateTime>getAs("MaturityDate").isAfter(secondLegModel.<LocalDateTime>getAs("MaturityDate")) ? firstLegModel.getAs("MaturityDate") : secondLegModel.getAs("MaturityDate");
        states.accruedInterest = eventAtT0.states().accruedInterest;

        // return the initialized state space
        return states;
    }

    // private method that filters all types of Congruents events and creates "netting events,
    private static List<ContractEvent> filterAndNettCongruentEvents(List<ContractEvent> firstLegEvents, List<ContractEvent> secondLegEvents){
        // sort the events according to their time sequence
        Collections.sort(firstLegEvents);
        Collections.sort(secondLegEvents);

        List<ContractEvent> events = new ArrayList<>();
        ContractEvent firstLegIED = firstLegEvents.stream().filter(event -> event.type().equals(EventType.IED)).collect(Collectors.toList()).get(0);
        ContractEvent secondLegIED = secondLegEvents.stream().filter(event -> event.type().equals(EventType.IED)).collect(Collectors.toList()).get(0);
        if(firstLegIED.time().isEqual(secondLegIED.time())){
            events.add(nettingEvent(firstLegIED,secondLegIED));
        } else {
            events.add(firstLegIED);
            events.add(secondLegIED);
        }

        List<ContractEvent> firstLegPR = firstLegEvents.stream().filter(event -> event.type().equals(EventType.PR)).collect(Collectors.toList());
        List<ContractEvent> secondLegPR = secondLegEvents.stream().filter(event -> event.type().equals(EventType.PR)).collect(Collectors.toList());
        events = netCongruentEvents(firstLegPR,secondLegPR,events);

        List<ContractEvent> firstLegIP = firstLegEvents.stream().filter(event -> event.type().equals(EventType.IP)).collect(Collectors.toList());
        List<ContractEvent> secondLegIP = secondLegEvents.stream().filter(event -> event.type().equals(EventType.IP)).collect(Collectors.toList());
        events = netCongruentEvents(firstLegIP,secondLegIP,events);

        return events;
    }
    //private method that filters congruent events and creates "netting" events of single typez
    private static List<ContractEvent> netCongruentEvents(List<ContractEvent> firstLegEvents, List<ContractEvent> secondLegEvents, List<ContractEvent> events){
        Iterator<ContractEvent> firstLegIt = firstLegEvents.iterator();
        Iterator<ContractEvent> secondLegIt = secondLegEvents.iterator();
        while(firstLegIt.hasNext() && secondLegIt.hasNext()) {
            ContractEvent tempFirstLegEvent = firstLegIt.next();
            while(secondLegIt.hasNext()){
                ContractEvent tempSecondLegEvent = secondLegIt.next();
                if(tempFirstLegEvent.time().isEqual(tempSecondLegEvent.time())){
                    events.add(nettingEvent(tempFirstLegEvent,tempSecondLegEvent));
                    secondLegIt.remove();
                    firstLegIt.remove();
                    break;
                }
            }
            secondLegIt = secondLegEvents.iterator();
        }
        events.addAll(firstLegEvents);events.addAll(secondLegEvents);
        return events;
    }
    // private method that allows creating a "netting" event from two events to be netted
    private static ContractEvent nettingEvent(ContractEvent e1, ContractEvent e2) {
        ContractEvent netting = EventFactory.createEvent(e1.time(), e1.type(), e1.currency(), new POF_NET_SWAPS(e1,e2), new STF_NET_SWAPS(e1,e2));
        return(netting);
    }

}

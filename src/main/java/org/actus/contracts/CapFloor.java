/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */

package org.actus.contracts;

import org.actus.AttributeConversionException;
import org.actus.attributes.ContractModel;
import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.events.ContractEvent;
import org.actus.events.EventFactory;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.stk.POF_PRD_STK;
import org.actus.functions.stk.STF_PRD_STK;
import org.actus.functions.stk.POF_TD_STK;
import org.actus.functions.stk.STF_TD_STK;
import org.actus.functions.capfl.POF_NET_CAPFL;
import org.actus.functions.capfl.STF_NET_CAPFL;
import org.actus.states.StateSpace;
import org.actus.time.calendar.NoHolidaysCalendar;
import org.actus.types.EventType;
import org.actus.types.ReferenceRole;
import org.actus.types.ContractRole;
import org.actus.util.CommonUtils;
import org.actus.types.ContractReference;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the CapFloor payoff algorithm
 *
 * @see <a href="https://www.actusfrf.org"></a>
 */
public final class CapFloor {

    // forward projection of the entire lifecycle of the contract
    public static ArrayList<ContractEvent> schedule(LocalDateTime to,
                                                    ContractModelProvider model) throws AttributeConversionException {
        
        // compute underlying event schedule
        ContractModel underlyingModel = (ContractModel)model.<List<ContractReference>>getAs("ContractStructure").stream().filter(c-> ReferenceRole.UDL.equals(c.referenceRole)).collect(Collectors.toList()).get(0).getObject();
        underlyingModel.addAttribute("ContractRole", ContractRole.RPA);
        ArrayList<ContractEvent> events = ContractType.schedule(underlyingModel.getAs("MaturityDate"), underlyingModel);

        // purchase
        if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            events.add(EventFactory.createEvent(
                model.getAs("PurchaseDate"), 
                EventType.PRD, 
                model.getAs("Currency"), 
                new POF_PRD_STK(), 
                new STF_PRD_STK(), 
                model.getAs("ContractID")));
        }

        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            ContractEvent termination = EventFactory.createEvent(
                    model.getAs("TerminationDate"),
                    EventType.TD,
                    model.getAs("Currency"),
                    new POF_TD_STK(),
                    new STF_TD_STK(),
                    model.getAs("ContractID")
            );
            events.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
            events.add(termination);
        }

        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), EventType.AD, model.getAs("Currency"), null, null, model.getAs("ContractID"))) == -1);

        // remove all post to-date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(to, EventType.AD, model.getAs("Currency"), null, null, model.getAs("ContractID"))) == 1);

        // return events
        return events;
    }

    // apply a set of events to the current state of a contract and return the post events state
    public static ArrayList<ContractEvent> apply(ArrayList<ContractEvent> events,
                                                 ContractModelProvider model,
                                                 RiskFactorModelProvider observer) throws AttributeConversionException {

        // evaluate events of underlying without cap/floor applied
        ContractModel underlyingModel = (ContractModel) model.<List<ContractReference>>getAs("ContractStructure").stream().filter(c-> ReferenceRole.UDL.equals(c.referenceRole)).collect(Collectors.toList()).get(0).getObject();
        ArrayList<ContractEvent> underlyingEvents = ContractType.apply(events, underlyingModel, observer).stream().filter(e -> EventType.IP.equals(e.eventType())).collect(Collectors.toCollection(ArrayList::new));

        // evaluate events of underlying with cap/floor applied
        underlyingModel.addAttribute("LifeCap", model.<Double>getAs("LifeCap"));
        underlyingModel.addAttribute("LifeFloor", model.<Double>getAs("LifeFloor"));
        ArrayList<ContractEvent> underlyingWithCapFloorEvents = ContractType.apply(events, underlyingModel, observer).stream().filter(e -> EventType.IP.equals(e.eventType())).collect(Collectors.toCollection(ArrayList::new));

        // net schedules of underlying with and without cap/floor applied
        Map<String, ContractEvent> mergedEvents = Stream.concat(underlyingEvents.stream(), underlyingWithCapFloorEvents.stream())
                    .collect(Collectors.toMap(
                            e -> e.eventTime().toString() + e.eventType(), // event key for merging
                            e -> e, // event itself
                            (e1, e2) -> nettingEvent(e1,e2,model,observer) // "merger" function
                            )
                    );
        events = new ArrayList<>(mergedEvents.values());
        Collections.sort(events);
        return events;
    }

    // private method that allows creating a "netting" event from two events to be netted
    private static ContractEvent nettingEvent(ContractEvent e1, ContractEvent e2, ContractModelProvider model, RiskFactorModelProvider observer) {
        ContractEvent e = EventFactory.createEvent(e1.eventTime(), e1.eventType(), e1.currency(), new POF_NET_CAPFL(e1,e2), new STF_NET_CAPFL(e1,e2), model.getAs("ContractID"));
        e.eval(new StateSpace(),model,observer,new DayCountCalculator("AA",new NoHolidaysCalendar()),model.<BusinessDayAdjuster>getAs("BusinessDayConvention"));
        return(e);
    }
}

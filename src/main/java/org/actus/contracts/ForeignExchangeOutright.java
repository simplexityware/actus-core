/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import org.actus.AttributeConversionException;
import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.events.ContractEvent;
import org.actus.functions.stk.*;
import org.actus.states.StateSpace;
import org.actus.events.EventFactory;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.util.CommonUtils;
import org.actus.util.StringUtils;
import org.actus.functions.pam.POF_AD_PAM;
import org.actus.functions.pam.STF_AD_PAM;
import org.actus.functions.fxout.POF_PRD_FXOUT;
import org.actus.functions.fxout.POF_TD_FXOUT;
import org.actus.functions.fxout.POF_STD_FXOUT;
import org.actus.functions.fxout.STF_STD_FXOUT;
import org.actus.functions.fxout.POF_STD1_FXOUT;
import org.actus.functions.fxout.STF_STD1_FXOUT;
import org.actus.functions.fxout.POF_STD2_FXOUT;
import org.actus.functions.fxout.STF_STD2_FXOUT;
import org.actus.functions.pam.POF_CD_PAM;
import org.actus.functions.fxout.STF_CD_FXOUT;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the Foreign Exchange Outright payoff algorithm
 * 
 * @see <a href="http://www.projectactus.org/"></a>
 */
public final class ForeignExchangeOutright {

    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {

        // init day count calculator 
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", null);
        
        // compute events
        ArrayList<ContractEvent> lifecycle = initEvents(analysisTimes,model);

        // compute and add contingent events
        lifecycle.addAll(initContingentEvents(analysisTimes,model,riskFactorModel));

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(lifecycle);

        // evaluate events
        lifecycle.forEach(e -> e.eval(states, model, riskFactorModel, dayCount, model.getAs("BusinessDayConvention")));
        
        // return all evaluated post-StatusDate events as the payoff
        return lifecycle;
    }

    // compute contingent payoff of the contract
    public static ArrayList<ContractEvent> payoff(Set<LocalDateTime> analysisTimes,
                                                  ContractModelProvider model,
                                                  RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return ForeignExchangeOutright.lifecycle(analysisTimes,model,riskFactorModel).stream().filter(ev->StringUtils.TransactionalEvents.contains(ev.type())).collect(Collectors.toCollection(ArrayList::new));
    }

    // compute next n events
    public static ArrayList<ContractEvent> next(LocalDateTime from,
                                                int n,
                                                ContractModelProvider model,
                                                RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        // convert single time input to set of times
        Set<LocalDateTime> times = new HashSet<LocalDateTime>();
        times.add(from);

        // init day count calculator
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", model.getAs("Calendar"));

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model);

        // compute and add contingent events
        events.addAll(initContingentEvents(times,model,riskFactorModel));

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(events);

        // evaluate only contingent events within time window
        ArrayList<ContractEvent> nextEvents = new ArrayList<ContractEvent>();
        Iterator<ContractEvent> iterator = events.iterator();
        int k=0;
        while(iterator.hasNext()) {
            ContractEvent event = iterator.next();
            // stop if we reached number of events
            if(k>=n) {
                break;
            }
            // eval event if not end of window reached
            event.eval(states, model, riskFactorModel, dayCount, model.getAs("BusinessDayConvention"));
            // add event to output list if after window start
            // note: need to evaluate also pre-start events in order to update states correctly
            if(!event.time().isBefore(from)) {
                nextEvents.add(event);
                k+=1;
            }
        }

        return nextEvents;
    }

    // compute next n events
    public static ArrayList<ContractEvent> next(int n,
                                                ContractModelProvider model,
                                                RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        // convert single time input to set of times
        Set<LocalDateTime> times = new HashSet<LocalDateTime>();
        times.add(model.getAs("StatusDate"));

        // init day count calculator
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", model.getAs("Calendar"));

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model);

        // compute and add contingent events
        events.addAll(initContingentEvents(times,model,riskFactorModel));

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(events);

        // evaluate only contingent events within time window
        ArrayList<ContractEvent> nextEvents = new ArrayList<ContractEvent>();
        Iterator<ContractEvent> iterator = events.iterator();
        int k=0;
        while(iterator.hasNext()) {
            ContractEvent event = iterator.next();
            // stop if we reached number of events
            if(k>=n) {
                break;
            }
            // eval event and update counter
            event.eval(states, model, riskFactorModel, dayCount, model.getAs("BusinessDayConvention"));
            nextEvents.add(event);
            k+=1;
        }

        return nextEvents;
    }

    // compute next events within period
    public static ArrayList<ContractEvent> next(LocalDateTime from,
                                                Period within,
                                                ContractModelProvider model,
                                                RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        // convert single time input to set of times
        Set<LocalDateTime> times = new HashSet<LocalDateTime>();
        times.add(from);

        // init day count calculator
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", model.getAs("Calendar"));

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model);

        // compute and add contingent events
        events.addAll(initContingentEvents(times,model,riskFactorModel));

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(events);

        // evaluate only contingent events within time window
        ArrayList<ContractEvent> nextEvents = new ArrayList<ContractEvent>();
        Iterator<ContractEvent> iterator = events.iterator();
        LocalDateTime end = from.plus(within);
        while(iterator.hasNext()) {
            ContractEvent event = iterator.next();
            // stop if we reached end of period
            if(event.time().isAfter(end)) {
                break;
            }
            // eval event if not end of window reached
            event.eval(states, model, riskFactorModel, dayCount, model.getAs("BusinessDayConvention"));
            // add event to output list if after window start
            // note: need to evaluate also pre-start events in order to update states correctly
            if(!event.time().isBefore(from)) {
                nextEvents.add(event);
            }
        }

        return nextEvents;
    }

    // compute next n events
    public static ArrayList<ContractEvent> next(Period within,
                                                ContractModelProvider model,
                                                RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        // convert single time input to set of times
        LocalDateTime from = model.getAs("StatusDate");
        Set<LocalDateTime> times = new HashSet<LocalDateTime>();
        times.add(from);

        // init day count calculator
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", model.getAs("Calendar"));

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model);

        // compute and add contingent events
        events.addAll(initContingentEvents(times,model,riskFactorModel));

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(events);

        // evaluate only contingent events within time window
        ArrayList<ContractEvent> nextEvents = new ArrayList<ContractEvent>();
        Iterator<ContractEvent> iterator = events.iterator();
        LocalDateTime end = from.plus(within);
        while(iterator.hasNext()) {
            ContractEvent event = iterator.next();
            // stop if we reached end of period
            if(event.time().isAfter(end)) {
                break;
            }
            // eval event and update counter
            event.eval(states, model, riskFactorModel, dayCount, model.getAs("BusinessDayConvention"));
            nextEvents.add(event);
        }

        return nextEvents;
    }

    // compute non-contingent portion of lifecycle of the contract
    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model) throws AttributeConversionException {
        // init day count calculator
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", null);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(analysisTimes,model);

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(events);

        // evaluate only non-contingent events and add these to new list
        ArrayList<ContractEvent> eventsNonContingent = new ArrayList<ContractEvent>();
        Iterator<ContractEvent> iterator = events.iterator();
        while(iterator.hasNext()) {
            ContractEvent event = iterator.next();
            if(StringUtils.ContingentEvents.contains(event.type())) {
                break;
            }
            event.eval(states, model, null, dayCount, model.getAs("BusinessDayConvention"));
            eventsNonContingent.add(event);
        }

        // return all non-contingent events as the non-contingent part of the lifecycle
        return events;
    }

    // compute non-contingent portion of payoff of the contract
    public static ArrayList<ContractEvent> payoff(Set<LocalDateTime> analysisTimes,
                                                  ContractModelProvider model) throws AttributeConversionException {
        return ForeignExchangeOutright.lifecycle(analysisTimes,model).stream().filter(ev->StringUtils.TransactionalEvents.contains(ev.type())).collect(Collectors.toCollection(ArrayList::new));
    }

    // compute next n non-contingent events
    public static ArrayList<ContractEvent> next(LocalDateTime from,
                                                int n,
                                                ContractModelProvider model) throws AttributeConversionException {
        // convert single time input to set of times
        Set<LocalDateTime> times = new HashSet<LocalDateTime>();
        times.add(from);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model);

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(events);

        // evaluate only contingent events within time window
        ArrayList<ContractEvent> nextEvents = new ArrayList<ContractEvent>();
        Iterator<ContractEvent> iterator = events.iterator();
        int k=0;
        while(iterator.hasNext()) {
            ContractEvent event = iterator.next();

            // stop if we reached number of events or if first contingent event occured
            if(k>=n || StringUtils.ContingentEvents.contains(event.type())) {
                break;
            }
            // eval event if not end of window reached
            event.eval(states, model, null, model.getAs("DayCountConvention"), model.getAs("BusinessDayConvention"));
            // add event to output list if after window start
            // note: need to evaluate also pre-start events in order to update states correctly
            if(!event.time().isBefore(from)) {
                nextEvents.add(event);
                k+=1;
            }
        }

        return nextEvents;
    }

    // compute next n non-contingent events
    public static ArrayList<ContractEvent> next(int n,
                                                ContractModelProvider model) throws AttributeConversionException {
        // convert single time input to set of times
        Set<LocalDateTime> times = new HashSet<LocalDateTime>();
        times.add(model.getAs("StatusDate"));

        // init day count calculator
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", model.getAs("Calendar"));

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model);

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(events);

        // evaluate only contingent events within time window
        ArrayList<ContractEvent> nextEvents = new ArrayList<ContractEvent>();
        Iterator<ContractEvent> iterator = events.iterator();
        int k=0;
        while(iterator.hasNext()) {
            ContractEvent event = iterator.next();
            // stop if we reached number of events or if first contingent event occured
            if(k>=n || StringUtils.ContingentEvents.contains(event.type())) {
                break;
            }
            // eval event and update counter
            event.eval(states, model, null, dayCount, model.getAs("BusinessDayConvention"));
            nextEvents.add(event);
            k+=1;
        }

        return nextEvents;
    }

    // compute next non-contingent events within period
    public static ArrayList<ContractEvent> next(LocalDateTime from,
                                                Period within,
                                                ContractModelProvider model) throws AttributeConversionException {
        // convert single time input to set of times
        Set<LocalDateTime> times = new HashSet<LocalDateTime>();
        times.add(from);

        // init day count calculator
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", model.getAs("Calendar"));

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model);

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(events);

        // evaluate only contingent events within time window
        ArrayList<ContractEvent> nextEvents = new ArrayList<ContractEvent>();
        Iterator<ContractEvent> iterator = events.iterator();
        LocalDateTime end = from.plus(within);
        while(iterator.hasNext()) {
            ContractEvent event = iterator.next();
            // stop if we reached number of events or if first contingent event occured
            if(event.time().isAfter(end) || StringUtils.ContingentEvents.contains(event.type())) {
                break;
            }
            // eval event if not end of window reached
            event.eval(states, model, null, dayCount, model.getAs("BusinessDayConvention"));
            // add event to output list if after window start
            // note: need to evaluate also pre-start events in order to update states correctly
            if(!event.time().isBefore(from)) {
                nextEvents.add(event);
            }
        }

        return nextEvents;
    }

    // compute next n non-contingent events
    public static ArrayList<ContractEvent> next(Period within,
                                                ContractModelProvider model) throws AttributeConversionException {
        // convert single time input to set of times
        LocalDateTime from = model.getAs("StatusDate");
        Set<LocalDateTime> times = new HashSet<LocalDateTime>();
        times.add(from);

        // init day count calculator
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", model.getAs("Calendar"));

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model);

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(events);

        // evaluate only contingent events within time window
        ArrayList<ContractEvent> nextEvents = new ArrayList<ContractEvent>();
        Iterator<ContractEvent> iterator = events.iterator();
        LocalDateTime end = from.plus(within);
        while(iterator.hasNext()) {
            ContractEvent event = iterator.next();
            // stop if we reached number of events or if first contingent event occured
            if(event.time().isAfter(end) || StringUtils.ContingentEvents.contains(event.type())) {
                break;
            }
            // eval event and update counter
            event.eval(states, model, null, dayCount, model.getAs("BusinessDayConvention"));
            nextEvents.add(event);
        }

        return nextEvents;
    }

    // compute (but not evaluate) non-contingent events
    private static ArrayList<ContractEvent> initEvents(Set<LocalDateTime> analysisTimes, ContractModelProvider model) throws AttributeConversionException {
        HashSet<ContractEvent> events = new HashSet<ContractEvent>();

        // determine settlement date (maturity) of the contract
        LocalDateTime settlement = model.getAs("SettlementDate");
        if (CommonUtils.isNull(settlement)) {
            settlement = model.getAs("MaturityDate");
        }
        // analysis events
        events.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
        // purchase
        if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            events.add(EventFactory.createEvent(model.getAs("PurchaseDate"), StringUtils.EventType_PRD, model.getAs("Currency"), new POF_PRD_FXOUT(), new STF_PRD_STK()));
        }
        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            events.add(EventFactory.createEvent(model.getAs("TerminationDate"), StringUtils.EventType_TD, model.getAs("Currency"), new POF_TD_FXOUT(), new STF_TD_STK()));
        }
        // settlement
        if (CommonUtils.isNull(model.getAs("DeliverySettlement")) || model.getAs("DeliverySettlement").equals(StringUtils.Settlement_Physical)) {
            events.add(EventFactory.createEvent(settlement, StringUtils.EventType_STD, model.getAs("Currency"), new POF_STD1_FXOUT(), new STF_STD1_FXOUT(), model.getAs("BusinessDayConvention")));
            events.add(EventFactory.createEvent(settlement, StringUtils.EventType_STD, model.getAs("Currency2"), new POF_STD2_FXOUT(), new STF_STD2_FXOUT(), model.getAs("BusinessDayConvention")));
        } else {
            events.add(EventFactory.createEvent(settlement, StringUtils.EventType_STD, model.getAs("Currency"), new POF_STD_FXOUT(), new STF_STD_FXOUT(), model.getAs("BusinessDayConvention")));
        }
        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null,
                null)) == -1);

        // return events
        return new ArrayList<ContractEvent>(events);
    }

    // compute (but not evaluate) contingent events
    private static ArrayList<ContractEvent> initContingentEvents(Set<LocalDateTime> analysisTimes, ContractModelProvider model, RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        HashSet<ContractEvent> events = new HashSet<ContractEvent>();

        if(riskFactorModel.keys().contains(model.getAs("LegalEntityIDCounterparty"))) {
            events.addAll(EventFactory.createEvents(riskFactorModel.times(model.getAs("LegalEntityIDCounterparty")),
                    StringUtils.EventType_CD, model.getAs("Currency"), new POF_CD_PAM(), new STF_CD_FXOUT()));
        }
        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null,
                null)) == -1);

        // return events
        return new ArrayList<ContractEvent>(events);
    }

    // initialize state space per status date
    private static StateSpace initStateSpace(ContractModelProvider model) {
        StateSpace states = new StateSpace();
        states.contractRoleSign = ContractRoleConvention.roleSign(model.getAs("ContractRole"));
        states.lastEventTime = model.getAs("StatusDate");
        return states;
    }
}
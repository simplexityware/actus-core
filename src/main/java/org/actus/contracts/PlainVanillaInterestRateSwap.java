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
import org.actus.states.StateSpace;
import org.actus.events.EventFactory;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.util.CommonUtils;
import org.actus.util.StringUtils;
import org.actus.time.ScheduleFactory;
import org.actus.functions.pam.POF_AD_PAM;
import org.actus.functions.swppv.STF_AD_SWPPV;
import org.actus.functions.swppv.POF_IED_SWPPV;
import org.actus.functions.swppv.STF_IED_SWPPV;
import org.actus.functions.swppv.POF_PR_SWPPV;
import org.actus.functions.swppv.STF_PR_SWPPV;
import org.actus.functions.fxout.POF_PRD_FXOUT;
import org.actus.functions.swppv.STF_PRD_SWPPV;
import org.actus.functions.fxout.POF_TD_FXOUT;
import org.actus.functions.swppv.STF_TD_SWPPV;
import org.actus.functions.swppv.POF_IP_SWPPV;
import org.actus.functions.swppv.STF_IP_SWPPV;
import org.actus.functions.swppv.POF_IPFix_SWPPV;
import org.actus.functions.swppv.STF_IPFix_SWPPV;
import org.actus.functions.swppv.POF_IPFloat_SWPPV;
import org.actus.functions.swppv.STF_IPFloat_SWPPV;
import org.actus.functions.pam.POF_RR_PAM;
import org.actus.functions.swppv.STF_RR_SWPPV;
import org.actus.functions.pam.POF_CD_PAM;
import org.actus.functions.swppv.STF_CD_SWPPV;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the Plain Vanilla Interest Rate Swap payoff algorithm
 * 
 * @see <a href="http://www.projectactus.org/"></a>
 */
public final class PlainVanillaInterestRateSwap {

    // compute contingent lifecycle of the contract
    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(analysisTimes,model);

        // compute and add contingent events
        events.addAll(initContingentEvents(model,riskFactorModel));

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(events);

        // evaluate events
        events.forEach(e -> e.eval(states, model, riskFactorModel, model.getAs("DayCountConventionProvider"), model.getAs("BusinessDayConvention")));

        // remove pre-purchase events if purchase date set (we only consider post-purchase events for analysis)
        if(!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            events.removeIf(e -> !e.type().equals(StringUtils.EventType_AD) && e.compareTo(EventFactory.createEvent(model.getAs("PurchaseDate"), StringUtils.EventType_PRD, model.getAs("Currency"), null, null)) == -1);
        }

        // return all evaluated post-StatusDate events as the payoff
        return events;
    }

    // compute contingent payoff of the contract
    public static ArrayList<ContractEvent> payoff(Set<LocalDateTime> analysisTimes,
                                                  ContractModelProvider model,
                                                  RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return PlainVanillaInterestRateSwap.lifecycle(analysisTimes,model,riskFactorModel).stream().filter(ev->StringUtils.TransactionalEvents.contains(ev.type())).collect(Collectors.toCollection(ArrayList::new));
    }

    // compute next n events
    public static ArrayList<ContractEvent> next(LocalDateTime from,
                                                int n,
                                                ContractModelProvider model,
                                                RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        // convert single time input to set of times
        Set<LocalDateTime> times = new HashSet<LocalDateTime>();
        times.add(from);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model);

        // compute and add contingent events
        events.addAll(initContingentEvents(model,riskFactorModel));

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
            event.eval(states, model, riskFactorModel, model.getAs("DayCountConventionProvider"), model.getAs("BusinessDayConvention"));
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

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model);

        // compute and add contingent events
        events.addAll(initContingentEvents(model,riskFactorModel));

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
            event.eval(states, model, riskFactorModel, model.getAs("DayCountConventionProvider"), model.getAs("BusinessDayConvention"));
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

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model);

        // compute and add contingent events
        events.addAll(initContingentEvents(model,riskFactorModel));

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
            event.eval(states, model, riskFactorModel, model.getAs("DayCountConventionProvider"), model.getAs("BusinessDayConvention"));
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

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model);

        // compute and add contingent events
        events.addAll(initContingentEvents(model,riskFactorModel));

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
            event.eval(states, model, riskFactorModel, model.getAs("DayCountConventionProvider"), model.getAs("BusinessDayConvention"));
            nextEvents.add(event);
        }

        return nextEvents;
    }

    // compute non-contingent portion of lifecycle of the contract
    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model) throws AttributeConversionException {

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
            event.eval(states, model, null, model.getAs("DayCountConventionProvider"), model.getAs("BusinessDayConvention"));
            eventsNonContingent.add(event);
        }

        // return all non-contingent events as the non-contingent part of the lifecycle
        return events;
    }

    // compute non-contingent portion of payoff of the contract
    public static ArrayList<ContractEvent> payoff(Set<LocalDateTime> analysisTimes,
                                                  ContractModelProvider model) throws AttributeConversionException {
        return PlainVanillaInterestRateSwap.lifecycle(analysisTimes,model).stream().filter(ev->StringUtils.TransactionalEvents.contains(ev.type())).collect(Collectors.toCollection(ArrayList::new));
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
            event.eval(states, model, null, model.getAs("DayCountConventionProvider"), model.getAs("BusinessDayConvention"));
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
            event.eval(states, model, null, model.getAs("DayCountConventionProvider"), model.getAs("BusinessDayConvention"));
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
            event.eval(states, model, null, model.getAs("DayCountConventionProvider"), model.getAs("BusinessDayConvention"));
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
            event.eval(states, model, null, model.getAs("DayCountConventionProvider"), model.getAs("BusinessDayConvention"));
            nextEvents.add(event);
        }

        return nextEvents;
    }

    // compute (but not evaluate) non-contingent events
    private static ArrayList<ContractEvent> initEvents(Set<LocalDateTime> analysisTimes, ContractModelProvider model) {
        ArrayList<ContractEvent> payoff = new ArrayList<ContractEvent>();
        // analysis events
        payoff.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_SWPPV()));
        // initial exchange
        payoff.add(EventFactory.createEvent(model.getAs("InitialExchangeDate"), StringUtils.EventType_IED, model.getAs("Currency"), new POF_IED_SWPPV(), new STF_IED_SWPPV()));
        // principal redemption
        payoff.add(EventFactory.createEvent(model.getAs("MaturityDate"), StringUtils.EventType_PR, model.getAs("Currency"), new POF_PR_SWPPV(), new STF_PR_SWPPV()));
        // purchase
        if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            payoff.add(EventFactory.createEvent(model.getAs("PurchaseDate"), StringUtils.EventType_PRD, model.getAs("Currency"), new POF_PRD_FXOUT(), new STF_PRD_SWPPV()));
        }
        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            payoff.add(EventFactory.createEvent(model.getAs("TerminationDate"), StringUtils.EventType_TD, model.getAs("Currency"), new POF_TD_FXOUT(), new STF_TD_SWPPV()));
        }
        // interest payment events
        if (CommonUtils.isNull(model.getAs("DeliverySettlement")) || model.getAs("DeliverySettlement").equals(StringUtils.Settlement_Physical)) {
            // in case of physical delivery (delivery of individual cash flows)
            // interest payment schedule
            Set<LocalDateTime> interestSchedule = ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfInterestPayment"),
                    model.getAs("MaturityDate"),
                    model.getAs("CycleOfInterestPayment"),
                    model.getAs("EndOfMonthConvention"));
            // fixed rate events                                                                                                    model.getAs("MaturityDate"),                                                                                                  model.getAs("EndOfMonthConvention"))
            payoff.addAll(EventFactory.createEvents(interestSchedule, StringUtils.EventType_IP, model.getAs("Currency"), new POF_IPFix_SWPPV(), new STF_IPFix_SWPPV(), model.getAs("BusinessDayConvention")));
            // floating rate events                                                                                                    model.getAs("MaturityDate"),                                                                                                  model.getAs("EndOfMonthConvention"))
            payoff.addAll(EventFactory.createEvents(interestSchedule, StringUtils.EventType_IP, model.getAs("Currency"), new POF_IPFloat_SWPPV(), new STF_IPFloat_SWPPV(), model.getAs("BusinessDayConvention")));
        } else {
            // in case of cash delivery (cash settlement)                                                                                                model.getAs("MaturityDate"),                                                                                                  model.getAs("EndOfMonthConvention"))
            payoff.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfInterestPayment"),
                    model.getAs("MaturityDate"),
                    model.getAs("CycleOfInterestPayment"),
                    model.getAs("EndOfMonthConvention")),
                    StringUtils.EventType_IP, model.getAs("Currency"), new POF_IP_SWPPV(), new STF_IP_SWPPV(), model.getAs("BusinessDayConvention")));

        }

        // rate reset
        payoff.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfRateReset"), model.getAs("MaturityDate"),
                model.getAs("CycleOfRateReset"), model.getAs("EndOfMonthConvention")),
                StringUtils.EventType_RR, model.getAs("Currency"), new POF_RR_PAM(), new STF_RR_SWPPV(), model.getAs("BusinessDayConvention")));
        // remove all pre-status date events
        payoff.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null,
                null)) == -1);

        return payoff;
    }

    // compute (but not evaluate) contingent events
    private static ArrayList<ContractEvent> initContingentEvents(ContractModelProvider model, RiskFactorModelProvider riskFactorModel) {
        ArrayList<ContractEvent> payoff = new ArrayList<ContractEvent>();
        if(riskFactorModel.keys().contains(model.getAs("LegalEntityIDCounterparty"))) {
            payoff.addAll(EventFactory.createEvents(riskFactorModel.times(model.getAs("LegalEntityIDCounterparty")),
                    StringUtils.EventType_CD, model.getAs("Currency"), new POF_CD_PAM(), new STF_CD_SWPPV()));
        }

        return payoff;
    }

    // initialize state space per status date
    private static StateSpace initStateSpace(ContractModelProvider model) throws AttributeConversionException {
        StateSpace states = new StateSpace();
        states.contractRoleSign = ContractRoleConvention.roleSign(model.getAs("ContractRole"));
        states.lastEventTime = model.getAs("StatusDate");
        if (!model.<LocalDateTime>getAs("InitialExchangeDate").isAfter(model.getAs("StatusDate"))) {
            states.nominalValue = model.getAs("NotionalPrincipal");
            states.nominalRate = model.getAs("NominalInterestRate");
            states.nominalAccrued = model.getAs("AccruedInterest");
        }
        return states;
    }
}

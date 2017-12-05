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
import org.actus.time.ScheduleFactory;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.util.CommonUtils;
import org.actus.util.StringUtils;
import org.actus.util.CycleUtils;
import org.actus.functions.pam.POF_AD_PAM;
import org.actus.functions.pam.STF_AD_PAM;
import org.actus.functions.clm.POF_IED_CLM;
import org.actus.functions.pam.STF_IED_PAM;
import org.actus.functions.pam.POF_PR_PAM;
import org.actus.functions.pam.STF_PR_PAM;
import org.actus.functions.clm.POF_IP_CLM;
import org.actus.functions.clm.STF_IP_CLM;
import org.actus.functions.pam.POF_IPCI_PAM;
import org.actus.functions.pam.STF_IPCI_PAM;
import org.actus.functions.pam.POF_RR_PAM;
import org.actus.functions.pam.STF_RR_PAM;
import org.actus.functions.pam.POF_FP_PAM;
import org.actus.functions.pam.STF_FP_PAM;
import org.actus.functions.pam.POF_CD_PAM;
import org.actus.functions.pam.STF_CD_PAM;


import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the Call Money payoff algorithm
 * 
 * @see <a href="http://www.projectactus.org/"></a>
 */
public final class CallMoney {

    // compute contingent lifecycle of the contract
    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {

        // determine maturity of the contract
        LocalDateTime maturity = maturity(model,analysisTimes);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(analysisTimes,model,maturity);

        // compute and add contingent events
        events.addAll(initContingentEvents(analysisTimes,model,maturity,riskFactorModel));

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
        return CallMoney.lifecycle(analysisTimes,model,riskFactorModel).stream().filter(ev->StringUtils.TransactionalEvents.contains(ev.type())).collect(Collectors.toCollection(ArrayList::new));
    }

    // compute next n events
    public static ArrayList<ContractEvent> next(LocalDateTime from,
                                                int n,
                                                ContractModelProvider model,
                                                RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        // convert single time input to set of times
        Set<LocalDateTime> times = new HashSet<LocalDateTime>();
        times.add(from);

        // determine maturity of the contract
        LocalDateTime maturity = maturity(model,times);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model,maturity);

        // compute and add contingent events
        events.addAll(initContingentEvents(times,model,maturity,riskFactorModel));

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

        // determine maturity of the contract
        LocalDateTime maturity = maturity(model,times);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model,maturity);

        // compute and add contingent events
        events.addAll(initContingentEvents(times,model,maturity,riskFactorModel));

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

        // determine maturity of the contract
        LocalDateTime maturity = maturity(model,times);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model,maturity);

        // compute and add contingent events
        events.addAll(initContingentEvents(times,model,maturity,riskFactorModel));

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

        // determine maturity of the contract
        LocalDateTime maturity = maturity(model,times);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model,maturity);

        // compute and add contingent events
        events.addAll(initContingentEvents(times,model,maturity,riskFactorModel));

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
        // determine maturity of the contract
        LocalDateTime maturity = maturity(model,analysisTimes);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(analysisTimes,model,maturity);

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
        return CallMoney.lifecycle(analysisTimes,model).stream().filter(ev->StringUtils.TransactionalEvents.contains(ev.type())).collect(Collectors.toCollection(ArrayList::new));
    }

    // compute next n non-contingent events
    public static ArrayList<ContractEvent> next(LocalDateTime from,
                                                int n,
                                                ContractModelProvider model) throws AttributeConversionException {
        // convert single time input to set of times
        Set<LocalDateTime> times = new HashSet<LocalDateTime>();
        times.add(from);

        // determine maturity of the contract
        LocalDateTime maturity = maturity(model,times);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model,maturity);

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

        // determine maturity of the contract
        LocalDateTime maturity = maturity(model,times);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model,maturity);

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

        // determine maturity of the contract
        LocalDateTime maturity = maturity(model,times);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model,maturity);

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

        // determine maturity of the contract
        LocalDateTime maturity = maturity(model,times);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model,maturity);

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

    // compute (but not evaluate) non-contingent events of the contract
    private static ArrayList<ContractEvent> initEvents(Set<LocalDateTime> analysisTimes, ContractModelProvider model, LocalDateTime maturity) throws AttributeConversionException {
        HashSet<ContractEvent> events = new HashSet<ContractEvent>();

        // create contract event schedules
        // analysis events
        events.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
        // initial exchange
        events.add(EventFactory.createEvent(model.getAs("InitialExchangeDate"), StringUtils.EventType_IED, model.getAs("Currency"), new POF_IED_CLM(), new STF_IED_PAM()));
        // principal redemption
        events.add(EventFactory.createEvent(maturity, StringUtils.EventType_PR, model.getAs("Currency"), new POF_PR_PAM(), new STF_PR_PAM()));
        // interest payment event
        events.add(EventFactory.createEvent(maturity, StringUtils.EventType_IP, model.getAs("Currency"), new POF_IP_CLM(), new STF_IP_CLM()));
        // interest payment capitalization (if specified)
        if (!CommonUtils.isNull(model.getAs("CycleOfInterestPayment"))) {
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfInterestPayment"),
                    maturity,
                    model.getAs("CycleOfInterestPayment"),
                    model.getAs("EndOfMonthConvention")),
                    StringUtils.EventType_IPCI, model.getAs("Currency"), new POF_IPCI_PAM(), new STF_IPCI_PAM(), model.getAs("BusinessDayConvention")));
        }
        // rate reset (if specified)
        if (!CommonUtils.isNull(model.getAs("CycleOfRateReset"))) {
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfRateReset"), maturity,
                    model.getAs("CycleOfRateReset"), model.getAs("EndOfMonthConvention")),
                    StringUtils.EventType_RR, model.getAs("Currency"), new POF_RR_PAM(), new STF_RR_PAM(), model.getAs("BusinessDayConvention")));
        }
        // fees (if specified)
        if (!CommonUtils.isNull(model.getAs("CycleOfFee"))) {
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfFee"), maturity,
                    model.getAs("CycleOfFee"), model.getAs("EndOfMonthConvention")),
                    StringUtils.EventType_FP, model.getAs("Currency"), new POF_FP_PAM(), new STF_FP_PAM(), model.getAs("BusinessDayConvention")));
        }
        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null,
                null)) == -1);

        // return events
        return new ArrayList<ContractEvent>(events);
    }

    // compute (but not evaluate) contingent events of the contract
    private static ArrayList<ContractEvent> initContingentEvents(Set<LocalDateTime> analysisTimes, ContractModelProvider model, LocalDateTime maturity, RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        HashSet<ContractEvent> events = new HashSet<ContractEvent>();

        // add counterparty default risk-factor contingent events
        if(riskFactorModel.keys().contains(model.getAs("LegalEntityIDCounterparty"))) {
            events.addAll(EventFactory.createEvents(riskFactorModel.times(model.getAs("LegalEntityIDCounterparty")),
                                             StringUtils.EventType_CD, model.getAs("Currency"), new POF_CD_PAM(), new STF_CD_PAM()));
        }
        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null,
                                                                  null)) == -1);
        
        // return events
        return new ArrayList<ContractEvent>(events);
    }

    // determine maturity of the contract
    private static LocalDateTime maturity(ContractModelProvider model,Set<LocalDateTime> analysisTimes) {
        LocalDateTime maturity = model.getAs("MaturityDate");
        if (CommonUtils.isNull(maturity)) {
            ArrayList<LocalDateTime> sortedTimes = new ArrayList<LocalDateTime>(analysisTimes);
            Collections.sort(sortedTimes);
            maturity = sortedTimes.get(0).plus(CycleUtils.parsePeriod(model.getAs("XDayNotice"),false));
        }
        return maturity;
    }

    private static StateSpace initStateSpace(ContractModelProvider model) throws AttributeConversionException {
        StateSpace states = new StateSpace();

        // TODO: some attributes can be null
        states.contractRoleSign = ContractRoleConvention.roleSign(model.getAs("ContractRole"));
        states.lastEventTime = model.getAs("StatusDate");
        if (!model.<LocalDateTime>getAs("InitialExchangeDate").isAfter(model.getAs("StatusDate"))) {
            states.nominalValue = states.contractRoleSign * model.<Double>getAs("NotionalPrincipal");
            states.nominalRate = model.getAs("NominalInterestRate");
            states.nominalAccrued = model.getAs("AccruedInterest");
            states.feeAccrued = model.getAs("FeeAccrued");
            states.nominalScalingMultiplier = 1;
            states.interestScalingMultiplier = 1;
        }
        
        // return the initialized state space
        return states;
    }

}

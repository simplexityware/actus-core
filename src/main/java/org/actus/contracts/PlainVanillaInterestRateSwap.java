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
import org.actus.functions.pam.*;
import org.actus.functions.swppv.*;
import org.actus.functions.fxout.*;

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

    // forward projection of the entire lifecycle of the contract
    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(analysisTimes,model);

        // compute and add contingent events
        events.addAll(riskFactorModel.events(model));

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(events);

        // evaluate events
        events.forEach(e -> e.eval(states, model, riskFactorModel, model.getAs("DayCountConvention"), model.getAs("BusinessDayConvention")));

        // remove pre-purchase events if purchase date set (we only consider post-purchase events for analysis)
        if(!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            events.removeIf(e -> !e.type().equals(StringUtils.EventType_AD) && e.compareTo(EventFactory.createEvent(model.getAs("PurchaseDate"), StringUtils.EventType_PRD, model.getAs("Currency"), null, null)) == -1);
        }

        // return all evaluated post-StatusDate events as the payoff
        return events;
    }

    // forward projection of the payoff of the contract
    public static ArrayList<ContractEvent> payoff(Set<LocalDateTime> analysisTimes,
                                                  ContractModelProvider model,
                                                  RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return PlainVanillaInterestRateSwap.lifecycle(analysisTimes,model,riskFactorModel).stream().filter(ev->StringUtils.TransactionalEvents.contains(ev.type())).collect(Collectors.toCollection(ArrayList::new));
    }

    // compute the contract schedule
    public static ArrayList<ContractEvent> schedule(ContractModelProvider model) throws AttributeConversionException {

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(new HashSet<LocalDateTime>(),model);

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(events);

        // evaluate only non-contingent events
        Iterator<ContractEvent> iterator = events.iterator();
        while(iterator.hasNext()) {
            ContractEvent event = iterator.next();
            if(StringUtils.ContingentEvents.contains(event.type())) {
                break;
            }
            event.eval(states, model, null, model.getAs("DayCountConvention"), model.getAs("BusinessDayConvention"));
        }

        // return all non-contingent events as the non-contingent part of the lifecycle
        return events;
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
            event.eval(states, model, null, model.getAs("DayCountConvention"), model.getAs("BusinessDayConvention"));
            nextEvents.add(event);
            k+=1;
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
            event.eval(states, model, null, model.getAs("DayCountConvention"), model.getAs("BusinessDayConvention"));
            nextEvents.add(event);
        }

        return nextEvents;
    }

    // apply a set of events to the current state of a contract and return the post events state
    public static StateSpace apply(Set<ContractEvent> events,
                                   ContractModelProvider model) throws AttributeConversionException {

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events according to their time sequence
        ArrayList<ContractEvent> seqEvents = new ArrayList<>(events);
        Collections.sort(seqEvents);

        // apply events according to their time sequence to current state
        seqEvents.forEach(e -> e.eval(states, model, null, model.getAs("DayCountConvention"), model.getAs("BusinessDayConvention")));

        // return post events states
        return states;
    }

    // compute (but not evaluate) non-contingent events
    private static ArrayList<ContractEvent> initEvents(Set<LocalDateTime> analysisTimes, ContractModelProvider model) {
        ArrayList<ContractEvent> payoff = new ArrayList<ContractEvent>();
        // analysis events
        payoff.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_SWPPV()));
        // purchase
        if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            payoff.add(EventFactory.createEvent(model.getAs("PurchaseDate"), StringUtils.EventType_PRD, model.getAs("Currency"), new POF_PRD_FXOUT(), new STF_PRD_SWPPV()));
        }
        // interest payment events
        if (CommonUtils.isNull(model.getAs("DeliverySettlement")) || model.getAs("DeliverySettlement").equals(StringUtils.Settlement_Physical)) {
            // in case of physical delivery (delivery of individual cash flows)
        	// fixed initial exchange
            payoff.add(EventFactory.createEvent(model.getAs("InitialExchangeDate"), StringUtils.EventType_IED, model.getAs("Currency"), new POF_IED_PAM(), new STF_IED_PAM()));
            // float initial exchange
            payoff.add(EventFactory.createEvent(model.getAs("InitialExchangeDate"), StringUtils.EventType_IED, model.getAs("Currency"), new POF_IEDFloat_SWPPV(), new STF_IED_SWPPV()));
            // fixed principal redemption
            payoff.add(EventFactory.createEvent(model.getAs("MaturityDate"), StringUtils.EventType_PR, model.getAs("Currency"), new POF_PR_PAM(), new STF_PR_SWPPV()));
            // float principal redemption
            payoff.add(EventFactory.createEvent(model.getAs("MaturityDate"), StringUtils.EventType_PR, model.getAs("Currency"), new POF_PRFloat_SWPPV(), new STF_PR_SWPPV()));
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
        	// initial exchange
        	payoff.add(EventFactory.createEvent(model.getAs("InitialExchangeDate"), StringUtils.EventType_IED, model.getAs("Currency"), new POF_IED_SWPPV(), new STF_IED_SWPPV()));
        	// principal redemption
        	payoff.add(EventFactory.createEvent(model.getAs("MaturityDate"), StringUtils.EventType_PR, model.getAs("Currency"), new POF_PR_SWPPV(), new STF_PR_SWPPV()));
            // in case of cash delivery (cash settlement)                                                                                                model.getAs("MaturityDate"),                                                                                                  model.getAs("EndOfMonthConvention"))
            payoff.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfInterestPayment"),
                    model.getAs("MaturityDate"),
                    model.getAs("CycleOfInterestPayment"),
                    model.getAs("EndOfMonthConvention")),
                    StringUtils.EventType_IP, model.getAs("Currency"), new POF_IP_SWPPV(), new STF_IP_SWPPV(), model.getAs("BusinessDayConvention")));

        }

        // rate reset
        payoff.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfRateReset"), model.getAs("MaturityDate"),
                model.getAs("CycleOfRateReset"), model.getAs("EndOfMonthConvention"), false),
                StringUtils.EventType_RR, model.getAs("Currency"), new POF_RR_PAM(), new STF_RR_SWPPV(), model.getAs("BusinessDayConvention")));
        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            ContractEvent termination =
                                        EventFactory.createEvent(model.getAs("TerminationDate"), StringUtils.EventType_TD, model.getAs("Currency"), new POF_TD_FXOUT(), new STF_TD_SWPPV());
            payoff.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
            payoff.add(termination);
        }
        // remove all pre-status date events
        payoff.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null,
                null)) == -1);
        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            ContractEvent termination =
                                        EventFactory.createEvent(model.getAs("TerminationDate"), StringUtils.EventType_TD, model.getAs("Currency"), new POF_TD_FXOUT(), new STF_TD_SWPPV());
            payoff.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
            payoff.add(termination);
        }

        return payoff;
    }

    // initialize state space per status date
    private static StateSpace initStateSpace(ContractModelProvider model) throws AttributeConversionException {
        StateSpace states = new StateSpace();
        states.nominalScalingMultiplier = 1;
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

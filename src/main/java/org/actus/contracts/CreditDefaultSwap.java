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
import org.actus.functions.pam.*;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the Principal At Maturity payoff algorithm
 * 
 * @see <a href="https://www.actusfrf.org"></a>
 */
public final class CreditDefaultSwap {

    // forward projection of the entire lifecycle of the contract
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
        return PrincipalAtMaturity.lifecycle(analysisTimes,model,riskFactorModel).stream().filter(ev->StringUtils.TransactionalEvents.contains(ev.type())).collect(Collectors.toCollection(ArrayList::new));
    }

    // compute the contract schedule
    public static ArrayList<ContractEvent> schedule(ContractModelProvider model) throws AttributeConversionException {

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(new HashSet<LocalDateTime>(),model);

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events according to their time of occurence
        Collections.sort(events);

        // evaluate events but stop at first contingent event
        Iterator<ContractEvent> iterator = events.iterator();
        while(iterator.hasNext()) {
            ContractEvent event = iterator.next();
            if(StringUtils.ContingentEvents.contains(event.type())) {
                break;
            }
            event.eval(states, model, null, model.getAs("DayCountConvention"), model.getAs("BusinessDayConvention"));
        }

        // return events
        return events;
    }

    // compute next events within period
    public static ArrayList<ContractEvent> next(Period within,
                                                ContractModelProvider model) throws AttributeConversionException {
        return null;
    }

    // apply a set of events to the current state of a contract and return the post events state
    public static StateSpace apply(Set<ContractEvent> events,
                                   ContractModelProvider model) throws AttributeConversionException {
    	return null;
    }
    
    private static ArrayList<ContractEvent> initEvents(Set<LocalDateTime> analysisTimes, ContractModelProvider model) throws AttributeConversionException {
        HashSet<ContractEvent> events = new HashSet<ContractEvent>();

        // create contract event schedules
        // analysis events
        events.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
        // initial exchange
        events.add(EventFactory.createEvent(model.getAs("InitialExchangeDate"), StringUtils.EventType_IED, model.getAs("Currency"), new POF_IED_PAM(), new STF_IED_PAM()));
    	// maturity, TODO: check eventType_pr --> eventType_md
        events.add(EventFactory.createEvent(model.getAs("MaturityDate"), StringUtils.EventType_PR, model.getAs("Currency"), new POF_PR_PAM(), new STF_PR_PAM()));
        // purchase
        if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            events.add(EventFactory.createEvent(model.getAs("PurchaseDate"), StringUtils.EventType_PRD, model.getAs("Currency"), new POF_PRD_PAM(), new STF_PRD_PAM()));
        }
        // rate reset
        if (!CommonUtils.isNull(model.getAs("CycleOfRateReset"))) { 
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfRateReset"), model.getAs("MaturityDate"),
                                                                                model.getAs("CycleOfRateReset"), model.getAs("EndOfMonthConvention"),true),
                                                 StringUtils.EventType_RR, model.getAs("Currency"), new POF_RR_PAM(), new STF_RR_PAM(), model.getAs("BusinessDayConvention")));
        }
        // fees (if specified)
        if (!CommonUtils.isNull(model.getAs("CycleOfFee"))) { 
        events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfFee"), model.getAs("MaturityDate"),
                                                                            model.getAs("CycleOfFee"), model.getAs("EndOfMonthConvention"),true),
                                             StringUtils.EventType_FP, model.getAs("Currency"), new POF_FP_PAM(), new STF_FP_PAM(), model.getAs("BusinessDayConvention")));
        }
        
        // TODO: XD, execution date
        // returns date of first credit event such that the total number of observed CEs qualifies for execution of protection
        // RCE + 
        
        // TODO: STD, settlement date
        // 
        
        // TODO: CD
        
        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            ContractEvent termination =
                                        EventFactory.createEvent(model.getAs("TerminationDate"), StringUtils.EventType_TD, model.getAs("Currency"), new POF_TD_PAM(), new STF_TD_PAM());
            events.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
            events.add(termination);
        }
        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null,
                                                                  null)) == -1);
        // return events
        return new ArrayList<ContractEvent>(events);
    }

    // compute (without evaluation) all events of the contract
    private static ArrayList<ContractEvent> initContingentEvents(ContractModelProvider model, RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        HashSet<ContractEvent> events = new HashSet<ContractEvent>();

        // optionality i.e. prepayment right (if specified)
        if (!(CommonUtils.isNull(model.getAs("CycleOfOptionality")) && CommonUtils.isNull(model.getAs("CycleAnchorDateOfOptionality")))) {
            Set<LocalDateTime> times = ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfOptionality"), model.getAs("MaturityDate"),model.getAs("CycleOfOptionality"), model.getAs("EndOfMonthConvention"));
            events.addAll(EventFactory.createEvents(times,StringUtils.EventType_PP, model.getAs("Currency"), new POF_PP_PAM(), new STF_PP_PAM(), model.getAs("BusinessDayConvention")));
            if(model.<String>getAs("PenaltyType")!="O") {
                events.addAll(EventFactory.createEvents(times,StringUtils.EventType_PY, model.getAs("Currency"), new POF_PY_PAM(), new STF_PY_PAM(), model.getAs("BusinessDayConvention")));
            }
        }
        // compute un-scheduled events
        events.addAll(riskFactorModel.events(model));

        // TODO: CE CreditEvent
        // returns all credit events of relevant counter parties with event date < maturity date, ids are found in ContractStructure     
        //events.addAll();
        
        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null,
                null)) == -1);

        
        
        // return events
        return new ArrayList<ContractEvent>(events);
    }

    private static StateSpace initStateSpace(ContractModelProvider model) throws AttributeConversionException {
        StateSpace states = new StateSpace();
        states.nominalScalingMultiplier = 1;
        states.interestScalingMultiplier = 1;

        // TODO: some attributes can be null
        states.lastEventTime = model.getAs("StatusDate");
        if (!model.<LocalDateTime>getAs("InitialExchangeDate").isAfter(model.getAs("StatusDate"))) {
            states.nominalValue = ContractRoleConvention.roleSign(model.getAs("ContractRole"))*model.<Double>getAs("NotionalPrincipal");
            states.nominalRate = model.getAs("NominalInterestRate");
            states.nominalAccrued = ContractRoleConvention.roleSign(model.getAs("ContractRole"))*model.<Double>getAs("AccruedInterest");
            states.feeAccrued = model.getAs("FeeAccrued");
        }
        
        // return the initialized state space
        return states;
    }

}

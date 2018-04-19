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

import org.actus.functions.cdswp.POF_AD_CDSWP;
import org.actus.functions.cdswp.STF_AD_CDSWP;
import org.actus.functions.cdswp.POF_CD_CDSWP;
import org.actus.functions.cdswp.STF_CD_CDSWP;
import org.actus.functions.cdswp.POF_EXD_CDSWP;
import org.actus.functions.cdswp.STF_EXD_CDSWP;
import org.actus.functions.cdswp.POF_FP1_CDSWP;
import org.actus.functions.cdswp.STF_FP1_CDSWP;
import org.actus.functions.cdswp.POF_FP2_CDSWP;
import org.actus.functions.cdswp.STF_FP2_CDSWP;
import org.actus.functions.cdswp.POF_FP3_CDSWP;
import org.actus.functions.cdswp.STF_FP3_CDSWP;
import org.actus.functions.cdswp.POF_MD_CDSWP;
import org.actus.functions.cdswp.STF_MD_CDSWP;
import org.actus.functions.cdswp.POF_PRD_CDSWP;
import org.actus.functions.cdswp.STF_PRD_CDSWP;
import org.actus.functions.cdswp.POF_RR_CDSWP;
import org.actus.functions.cdswp.STF_RR_CDSWP;
import org.actus.functions.cdswp.POF_STD_CDSWP;
import org.actus.functions.cdswp.STF_STD_CDSWP;
import org.actus.functions.cdswp.POF_TD_CDSWP;
import org.actus.functions.cdswp.STF_TD_CDSWP;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the Credit Default Swap payoff algorithm
 *
 * @see <a href="http://www.projectactus.org/"></a>
 */
public final class CreditDefaultSwap {


    // forward projection of the entire lifecycle of the contract
    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(analysisTimes,model);

        // compute and add contingent events
        events.addAll(initContingentEvents(analysisTimes,model,riskFactorModel));

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
        return CreditDefaultSwap.lifecycle(analysisTimes,model,riskFactorModel).stream().filter(ev->StringUtils.TransactionalEvents.contains(ev.type())).collect(Collectors.toCollection(ArrayList::new));
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

    // compute next n events
    public static ArrayList<ContractEvent> next(int n,
                                                ContractModelProvider model) throws AttributeConversionException {
        // define StatusDate as projection start time
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

    // compute next events within period
    public static ArrayList<ContractEvent> next(Period within,
                                                ContractModelProvider model) throws AttributeConversionException {
        // define StatusDate as projection start time
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
            // eval event if not end of window reached
            event.eval(states, model, null, model.getAs("DayCountConvention"), model.getAs("BusinessDayConvention"));
            // add event to output list if after window start
            // note: need to evaluate also pre-start events in order to update states correctly
            if(!event.time().isBefore(from)) {
                nextEvents.add(event);
            }
        }

        return nextEvents;
    }

    private static ArrayList<ContractEvent> initEvents(Set<LocalDateTime> analysisTimes, ContractModelProvider model) throws AttributeConversionException {
        HashSet<ContractEvent> events = new HashSet<ContractEvent>();

        // create contract event schedules
        // analysis events
        events.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_CDSWP(), new STF_AD_CDSWP()));

        // purchase
        if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            events.add(EventFactory.createEvent(model.getAs("PurchaseDate"), StringUtils.EventType_PRD, model.getAs("Currency"), new POF_PRD_CDSWP(), new STF_PRD_CDSWP()));
        }

        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            ContractEvent termination =
                    EventFactory.createEvent(model.getAs("TerminationDate"), StringUtils.EventType_TD, model.getAs("Currency"), new POF_TD_CDSWP(), new STF_TD_CDSWP());
            events.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
            events.add(termination);
        }

        // maturity
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            events.add(EventFactory.createEvent(model.getAs("MaturityDate"), StringUtils.EventType_MD, model.getAs("Currency"), new POF_MD_CDSWP(), new STF_MD_CDSWP()));
        }

        // EXD
        // TODO

        // settlement date
        if (!CommonUtils.isNull(model.getAs("ExchangeDate"))) {
            if (CommonUtils.isNull(model.getAs("SettlementDate"))) {
                events.add(EventFactory.createEvent(model.getAs("ExchangeDate"), StringUtils.EventType_STD, model.getAs("Currency"), new POF_EXD_CDSWP(), new STF_EXD_CDSWP()));
            } else {
                events.add(EventFactory.createEvent(model.getAs("SettlementDate"), StringUtils.EventType_STD, model.getAs("Currency"), new POF_STD_CDSWP(), new STF_STD_CDSWP()));
            }
        }

        // fees (if specified)
        // fee payment 1
        if ( !(CommonUtils.isNull(model.getAs("InitialExchangeDate")))) {
            events.add(EventFactory.createEvent(model.getAs("InitialExchangeDate"), StringUtils.EventType_FP, model.getAs("Currency"), new POF_FP1_CDSWP(), new STF_FP1_CDSWP()));
        }

        // fee payment 2
        if ( !((CommonUtils.isNull(model.getAs("CycleOfFee"))) || (model.<String>getAs("FeeBasis").equals("A"))) ) {
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfFee"), model.getAs("MaturityDate"),
                          model.getAs("CycleOfFee"), model.getAs("EndOfMonthConvention"),true),
                          StringUtils.EventType_FP, model.getAs("Currency"), new POF_FP2_CDSWP(), new STF_FP2_CDSWP(), model.getAs("BusinessDayConvention")));
        }

        // fee payment 3
        if ( !((CommonUtils.isNull(model.getAs("CycleOfFee"))) || (model.<String>getAs("FeeBasis").equals("N"))) ) {
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfFee"), model.getAs("MaturityDate"),
                          model.getAs("CycleOfFee"), model.getAs("EndOfMonthConvention"),true),
                          StringUtils.EventType_FP, model.getAs("Currency"), new POF_FP3_CDSWP(), new STF_FP3_CDSWP(), model.getAs("BusinessDayConvention")));
        }

        // interest payment related
        if (!CommonUtils.isNull(model.getAs("NominalInterestRate"))) {

            // rate reset (if specified)
            if (!CommonUtils.isNull(model.getAs("CycleOfRateReset"))) {
                events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfRateReset"), model.getAs("MaturityDate"),
                        model.getAs("CycleOfRateReset"), model.getAs("EndOfMonthConvention"),false),
                        StringUtils.EventType_RR, model.getAs("Currency"), new POF_RR_CDSWP(), new STF_RR_CDSWP(), model.getAs("BusinessDayConvention")));
            }
        }



        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null,
                null)) == -1);
        // return events
        return new ArrayList<ContractEvent>(events);
    }

    private static ArrayList<ContractEvent> initContingentEvents(Set<LocalDateTime> analysisTimes, ContractModelProvider model, RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        HashSet<ContractEvent> events = new HashSet<ContractEvent>();


        // add counterparty default risk-factor contingent events
        if(riskFactorModel.keys().contains(model.getAs("LegalEntityIDCounterparty"))) {
            events.addAll(EventFactory.createEvents(riskFactorModel.times(model.getAs("LegalEntityIDCounterparty")),
                    StringUtils.EventType_CD, model.getAs("Currency"), new POF_CD_CDSWP(), new STF_CD_CDSWP()));
        }

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
        states.probabilityOfDefault = 0.0;
        states.payoffAtSettlement = 0.0;

        // Value
        if (!CommonUtils.isNull(model.getAs("NotionalPrincipal"))){
            states.nominalValue = model.<Integer>getAs("Quantity") * model.<Double>getAs("NotionalPrincipal");
        }
        /*
        else {
            states.nominalValue = model.<Integer>getAs("Quantity") * // TODO: SU(Nvl per SD ....)
        }
        */

        // Rate
        if (CommonUtils.isNull(model.getAs("CycleOfFee"))){
            states.nominalRate = 0.0;
        } else if (model.<String>getAs("FeeBasis").equals("N")) {
            states.nominalRate = model.<Double>getAs("FeeRate");
        } else {
            states.nominalRate = model.<Double>getAs("FeeRate") * model.<Integer>getAs("Quantity");
        }

        // Accrued
        if (CommonUtils.isNull(model.getAs("CycleOfFee"))){
            states.nominalAccrued = 0.0;
        } else if (!CommonUtils.isNull(model.getAs("FeeAccrued"))) {
            states.nominalAccrued = model.getAs("FeeAccrued");
        }
        /*
        else if (model.<String>getAs("FeeBasis").equals("N")) {
            states.nominalAccrued = ; // TODO:
        } else {
            states.nominalAccrued = ; // TODO:
        }
        */

        states.lastEventTime = model.getAs("StatusDate");

        // return the initialized state space
        return states;
    }
}

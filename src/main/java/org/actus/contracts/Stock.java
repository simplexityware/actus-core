/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import org.actus.AttributeConversionException;
import org.actus.externals.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.events.ContractEvent;
import org.actus.states.StateSpace;
import org.actus.events.EventFactory;
import org.actus.time.ScheduleFactory;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.util.CommonUtils;
import org.actus.util.StringUtils;
import org.actus.util.Constants;
import org.actus.functions.pam.POF_AD_PAM;
import org.actus.functions.pam.STF_AD_PAM;
import org.actus.functions.pam.POF_CD_PAM;
import org.actus.functions.stk.STF_CD_STK;
import org.actus.functions.stk.POF_PRD_STK;
import org.actus.functions.stk.STF_PRD_STK;
import org.actus.functions.stk.POF_TD_STK;
import org.actus.functions.stk.STF_TD_STK;
import org.actus.functions.stk.POF_DV_STK;
import org.actus.functions.stk.STF_DV_STK;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the Stock payoff algorithm
 * 
 * @see <a href="http://www.projectactus.org/"></a>
 */
public final class Stock {

    // compute contingent lifecycle of the contract
    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {

        // init day count calculator
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", model.getAs("Calendar"));

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(analysisTimes,model);

        // compute and add contingent events
        events.addAll(initContingentEvents(analysisTimes,model,riskFactorModel));

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(events);

        // evaluate events
        events.forEach(e -> e.eval(states, model, riskFactorModel, dayCount, model.getAs("BusinessDayConvention")));

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
        return Stock.lifecycle(analysisTimes,model,riskFactorModel).stream().filter(ev->StringUtils.TransactionalEvents.contains(ev.type())).collect(Collectors.toCollection(ArrayList::new));
    }

    // compute contingent events in time window
    public static ArrayList<ContractEvent> events(Set<LocalDateTime> analysisTimes,
                                                  ContractModelProvider model,
                                                  RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        // init day count calculator
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", model.getAs("Calendar"));

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(analysisTimes,model);

        // compute and add contingent events
        events.addAll(initContingentEvents(analysisTimes,model,riskFactorModel));

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(events);

        // evaluate only contingent events within time window
        ArrayList<ContractEvent> eventsInWindow = new ArrayList<ContractEvent>();
        LocalDateTime start = analysisTimes.stream().min(Comparator.naturalOrder()).get();
        LocalDateTime end = analysisTimes.stream().max(Comparator.naturalOrder()).get();
        Iterator<ContractEvent> iterator = events.iterator();
        while(iterator.hasNext()) {
            ContractEvent event = iterator.next();
            // stop if we reached end of window
            if(event.time().isAfter(end)) {
                break;
            }
            // eval event if not end of window reached
            event.eval(states, model, riskFactorModel, dayCount, model.getAs("BusinessDayConvention"));
            // add event to output list if after window start
            // note: need to evaluate also pre-start events in order to update states correctly
            if(!event.time().isBefore(start)) {
                eventsInWindow.add(event);
            }
        }

        return eventsInWindow;
    }

    // compute contingent events in time period
    public static ArrayList<ContractEvent> events(LocalDateTime analysisTime,
                                                  Period period,
                                                  ContractModelProvider model,
                                                  RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return Stock.events(Stream.of(analysisTime,analysisTime.plus(period)).collect(Collectors.toSet()),model,riskFactorModel);
    }

    // compute contingent transactions in time window
    public static ArrayList<ContractEvent> transactions(Set<LocalDateTime> analysisTimes,
                                                        ContractModelProvider model,
                                                        RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return Stock.events(analysisTimes,model,riskFactorModel).stream().filter(ev->StringUtils.TransactionalEvents.contains(ev.type())).collect(Collectors.toCollection(ArrayList::new));
    }

    // compute contingent transactions in time period
    public static ArrayList<ContractEvent> transactions(LocalDateTime analysisTime,
                                                        Period period,
                                                        ContractModelProvider model,
                                                        RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return Stock.transactions(Stream.of(analysisTime,analysisTime.plus(period)).collect(Collectors.toSet()),model,riskFactorModel);
    }

    // compute non-contingent portion of lifecycle of the contract
    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model) throws AttributeConversionException {
        // init day count calculator
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", model.getAs("Calendar"));

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
        return Stock.lifecycle(analysisTimes,model).stream().filter(ev->StringUtils.TransactionalEvents.contains(ev.type())).collect(Collectors.toCollection(ArrayList::new));
    }

    // compute non-contingent events in time window
    public static ArrayList<ContractEvent> events(Set<LocalDateTime> analysisTimes,
                                                  ContractModelProvider model) throws AttributeConversionException {

        // init day count calculator
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", model.getAs("Calendar"));

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(analysisTimes,model);

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(events);

        // evaluate only non-contingent events and add these to new list
        ArrayList<ContractEvent> eventsNonContingent = new ArrayList<ContractEvent>();
        LocalDateTime start = analysisTimes.stream().min(Comparator.naturalOrder()).get();
        LocalDateTime end = analysisTimes.stream().max(Comparator.naturalOrder()).get();
        Iterator<ContractEvent> iterator = events.iterator();
        while(iterator.hasNext()) {
            ContractEvent event = iterator.next();
            // stop if we reached end of window or if first contingent event occured
            if(event.time().isAfter(end) || StringUtils.ContingentEvents.contains(event.type())) {
                break;
            }
            // eval event if not end of window or first contingent event
            event.eval(states, model, null, dayCount, model.getAs("BusinessDayConvention"));
            // add event to output list if after window start
            // note: need to evaluate also pre-start events in order to update states correctly
            if(!event.time().isBefore(start)) {
                eventsNonContingent.add(event);
            }
        }

        return eventsNonContingent;
    }

    // compute non-contingent events in time period
    public static ArrayList<ContractEvent> events(LocalDateTime analysisTime,
                                                  Period period,
                                                  ContractModelProvider model) throws AttributeConversionException {
        return Stock.events(Stream.of(analysisTime,analysisTime.plus(period)).collect(Collectors.toSet()),model);
    }

    // compute non-contingent transactions in time window
    public static ArrayList<ContractEvent> transactions(Set<LocalDateTime> analysisTimes,
                                                        ContractModelProvider model) throws AttributeConversionException {
        return Stock.events(analysisTimes,model).stream().filter(ev->StringUtils.TransactionalEvents.contains(ev.type())).collect(Collectors.toCollection(ArrayList::new));
    }

    // compute non-contingent transactions in time period
    public static ArrayList<ContractEvent> transactions(LocalDateTime analysisTime,
                                                        Period period,
                                                        ContractModelProvider model) throws AttributeConversionException {
        return Stock.transactions(Stream.of(analysisTime,analysisTime.plus(period)).collect(Collectors.toSet()),model);
    }

    // compute (but not evaluate) non-contingent events
    private static ArrayList<ContractEvent> initEvents(Set<LocalDateTime> analysisTimes, ContractModelProvider model) throws AttributeConversionException {
        HashSet<ContractEvent> events = new HashSet<ContractEvent>();

        // create contract event schedules
        // analysis events
        events.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
        // purchase
        if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            events.add(EventFactory.createEvent(model.getAs("PurchaseDate"), StringUtils.EventType_PRD, model.getAs("Currency"), new POF_PRD_STK(), new STF_PRD_STK()));
        }
        // dividend payment related
        if (!CommonUtils.isNull(model.getAs("CycleOfDividendPayment"))) {
            if(CommonUtils.isNull(model.getAs("TerminationDate"))) {
                events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfDividendPayment"),
                        model.<LocalDateTime>getAs("CycleAnchorDateOfDividendPayment").plus(Constants.MAX_LIFETIME),
                        model.getAs("CycleOfDividendPayment"),
                        model.getAs("EndOfMonthConvention")),
                        StringUtils.EventType_DV, model.getAs("Currency"), new POF_DV_STK(), new STF_DV_STK(), model.getAs("BusinessDayConvention")));
            } else {
                events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfDividendPayment"),
                        model.getAs("TerminationDate"),
                        model.getAs("CycleOfDividendPayment"),
                        model.getAs("EndOfMonthConvention")),
                        StringUtils.EventType_DV, model.getAs("Currency"), new POF_DV_STK(), new STF_DV_STK(), model.getAs("BusinessDayConvention")));

            }
        }
        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            ContractEvent termination =
                    EventFactory.createEvent(model.getAs("TerminationDate"), StringUtils.EventType_TD, model.getAs("Currency"), new POF_TD_STK(), new STF_TD_STK());
            events.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
            events.add(termination);
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

        // add counterparty default risk-factor contingent events
        if(riskFactorModel.keys().contains(model.getAs("LegalEntityIDCounterparty"))) {
            events.addAll(EventFactory.createEvents(riskFactorModel.times(model.getAs("LegalEntityIDCounterparty")),
                                             StringUtils.EventType_CD, model.getAs("Currency"), new POF_CD_PAM(), new STF_CD_STK()));
        }
        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null,
                                                                  null)) == -1);
        
        // return events
        return new ArrayList<ContractEvent>(events);
    }

    private static StateSpace initStateSpace(ContractModelProvider model) throws AttributeConversionException {
        StateSpace states = new StateSpace();

        // TODO: some attributes can be null
        states.contractRoleSign = ContractRoleConvention.roleSign(model.getAs("ContractRole"));
        states.lastEventTime = model.getAs("StatusDate");
        
        // return the initialized state space
        return states;
    }

}

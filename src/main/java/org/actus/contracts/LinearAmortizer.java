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
import org.actus.functions.pam.POF_IED_PAM;
import org.actus.functions.lam.STF_IED_LAM;
import org.actus.functions.lam.POF_PR_LAM;
import org.actus.functions.lam.STF_PR_LAM;
import org.actus.functions.lam.POF_PRD_LAM;
import org.actus.functions.lam.STF_PRD_LAM;
import org.actus.functions.lam.POF_IP_LAM;
import org.actus.functions.lam.POF_MD_LAM;
import org.actus.functions.pam.STF_IP_PAM;
import org.actus.functions.pam.STF_PR_PAM;
import org.actus.functions.pam.POF_IPCI_PAM;
import org.actus.functions.lam.STF_IPCI_LAM;
import org.actus.functions.pam.POF_RR_PAM;
import org.actus.functions.lam.STF_RR_LAM;
import org.actus.functions.pam.POF_SC_PAM;
import org.actus.functions.lam.STF_SC_LAM;
import org.actus.functions.pam.POF_PP_PAM;
import org.actus.functions.lam.STF_PP_LAM;
import org.actus.functions.pam.POF_PY_PAM;
import org.actus.functions.pam.POF_RRY_PAM;
import org.actus.functions.lam.STF_PY_LAM;
import org.actus.functions.lam.STF_RRY_LAM;
import org.actus.functions.pam.POF_FP_PAM;
import org.actus.functions.lam.STF_FP_LAM;
import org.actus.functions.lam.POF_TD_LAM;
import org.actus.functions.pam.STF_TD_PAM;
import org.actus.functions.pam.POF_CD_PAM;
import org.actus.functions.lam.STF_CD_LAM;
import org.actus.functions.lam.POF_IPCB_LAM;
import org.actus.functions.lam.STF_IPCB_LAM;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the Linear-Amortizer payoff algorithm
 * 
 * @see <a href="http://www.projectactus.org/"></a>
 */
public final class LinearAmortizer {

    // compute contingent lifecycle of the contract
    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {

        // determine maturity of the contract
        LocalDateTime maturity = maturity(model);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(analysisTimes,model,maturity);

        // compute and add contingent events
        events.addAll(initContingentEvents(analysisTimes,model,maturity,riskFactorModel));

        // initialize state space per status date
        StateSpace states = initStateSpace(model, maturity);

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

    // compute contingent payoff of the contract
    public static ArrayList<ContractEvent> payoff(Set<LocalDateTime> analysisTimes,
                                                  ContractModelProvider model,
                                                  RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return LinearAmortizer.lifecycle(analysisTimes,model,riskFactorModel).stream().filter(ev->StringUtils.TransactionalEvents.contains(ev.type())).collect(Collectors.toCollection(ArrayList::new));
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
        LocalDateTime maturity = maturity(model);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model,maturity);

        // compute and add contingent events
        events.addAll(initContingentEvents(times,model,maturity,riskFactorModel));

        // initialize state space per status date
        StateSpace states = initStateSpace(model,maturity);

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
            event.eval(states, model, riskFactorModel, model.getAs("DayCountConvention"), model.getAs("BusinessDayConvention"));
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
        LocalDateTime maturity = maturity(model);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model,maturity);

        // compute and add contingent events
        events.addAll(initContingentEvents(times,model,maturity,riskFactorModel));

        // initialize state space per status date
        StateSpace states = initStateSpace(model,maturity);

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
            event.eval(states, model, riskFactorModel, model.getAs("DayCountConvention"), model.getAs("BusinessDayConvention"));
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
        LocalDateTime maturity = maturity(model);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model,maturity);

        // compute and add contingent events
        events.addAll(initContingentEvents(times,model,maturity,riskFactorModel));

        // initialize state space per status date
        StateSpace states = initStateSpace(model,maturity);

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
            event.eval(states, model, riskFactorModel, model.getAs("DayCountConvention"), model.getAs("BusinessDayConvention"));
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
        LocalDateTime maturity = maturity(model);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model,maturity);

        // compute and add contingent events
        events.addAll(initContingentEvents(times,model,maturity,riskFactorModel));

        // initialize state space per status date
        StateSpace states = initStateSpace(model,maturity);

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
            event.eval(states, model, riskFactorModel, model.getAs("DayCountConvention"), model.getAs("BusinessDayConvention"));
            nextEvents.add(event);
        }

        return nextEvents;
    }

    // compute non-contingent portion of lifecycle of the contract
    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model) throws AttributeConversionException {
        // determine maturity of the contract
        LocalDateTime maturity = maturity(model);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(analysisTimes,model,maturity);

        // initialize state space per status date
        StateSpace states = initStateSpace(model, maturity);

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
            event.eval(states, model, null, model.getAs("DayCountConvention"), model.getAs("BusinessDayConvention"));
            eventsNonContingent.add(event);
        }

        // return all non-contingent events as the non-contingent part of the lifecycle
        return events;
    }

    // compute non-contingent portion of payoff of the contract
    public static ArrayList<ContractEvent> payoff(Set<LocalDateTime> analysisTimes,
                                                  ContractModelProvider model) throws AttributeConversionException {
        return LinearAmortizer.lifecycle(analysisTimes,model).stream().filter(ev->StringUtils.TransactionalEvents.contains(ev.type())).collect(Collectors.toCollection(ArrayList::new));
    }

    // compute next n non-contingent events
    public static ArrayList<ContractEvent> next(LocalDateTime from,
                                                int n,
                                                ContractModelProvider model) throws AttributeConversionException {
        // convert single time input to set of times
        Set<LocalDateTime> times = new HashSet<LocalDateTime>();
        times.add(from);

        // determine maturity of the contract
        LocalDateTime maturity = maturity(model);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model,maturity);

        // initialize state space per status date
        StateSpace states = initStateSpace(model,maturity);

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

        // determine maturity of the contract
        LocalDateTime maturity = maturity(model);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model,maturity);

        // initialize state space per status date
        StateSpace states = initStateSpace(model,maturity);

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

    // compute next non-contingent events within period
    public static ArrayList<ContractEvent> next(LocalDateTime from,
                                                Period within,
                                                ContractModelProvider model) throws AttributeConversionException {
        // convert single time input to set of times
        Set<LocalDateTime> times = new HashSet<LocalDateTime>();
        times.add(from);

        // determine maturity of the contract
        LocalDateTime maturity = maturity(model);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model,maturity);

        // initialize state space per status date
        StateSpace states = initStateSpace(model,maturity);

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

    // compute next n non-contingent events
    public static ArrayList<ContractEvent> next(Period within,
                                                ContractModelProvider model) throws AttributeConversionException {
        // convert single time input to set of times
        LocalDateTime from = model.getAs("StatusDate");
        Set<LocalDateTime> times = new HashSet<LocalDateTime>();
        times.add(from);

        // determine maturity of the contract
        LocalDateTime maturity = maturity(model);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(times,model,maturity);

        // initialize state space per status date
        StateSpace states = initStateSpace(model,maturity);

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

    private static ArrayList<ContractEvent> initEvents(Set<LocalDateTime> analysisTimes, ContractModelProvider model, LocalDateTime maturity) throws AttributeConversionException {
        HashSet<ContractEvent> events = new HashSet<ContractEvent>();
        
        // create contract event schedules
        // analysis events
        events.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
        // initial exchange
        events.add(EventFactory.createEvent(model.getAs("InitialExchangeDate"), StringUtils.EventType_IED, model.getAs("Currency"), new POF_IED_PAM(), new STF_IED_LAM()));
        // principal redemption
        events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfPrincipalRedemption"), maturity,
                                                                            model.getAs("CycleOfPrincipalRedemption"), model.getAs("EndOfMonthConvention")),
                                            StringUtils.EventType_PR, model.getAs("Currency"), new POF_PR_LAM(), new STF_PR_LAM(), model.getAs("BusinessDayConvention")));     
        events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfPrincipalRedemption"), maturity,
																			model.getAs("CycleOfPrincipalRedemption"), model.getAs("EndOfMonthConvention")),
											StringUtils.EventType_IP, model.getAs("Currency"), new POF_IP_LAM(), new STF_IP_PAM(), model.getAs("BusinessDayConvention")));    
        
        if (!CommonUtils.isNull(model.getAs("MaturityDate"))) {
        	        	events.forEach(e->{
        	        		if(e.type().equals(StringUtils.EventType_PR) && e.time().equals(model.getAs("MaturityDate"))) {
        	        			e.fPayOff(new POF_MD_LAM());
        	        			e.fStateTrans(new STF_PR_PAM());
        	        		}
        	        	});
        }
        // purchase
        if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            events.add(EventFactory.createEvent(model.getAs("PurchaseDate"), StringUtils.EventType_PRD, model.getAs("Currency"), new POF_PRD_LAM(), new STF_PRD_LAM()));
        }
        // interest payment related
        if (!CommonUtils.isNull(model.getAs("CycleOfInterestPayment"))) {
            // raw interest payment events
            Set<ContractEvent> interestEvents =
                                                        EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfInterestPayment"),
                                                                                                                 model.getAs("CycleAnchorDateOfPrincipalRedemption"),
                                                                                                                 model.getAs("CycleOfInterestPayment"),
                                                                                                                 model.getAs("EndOfMonthConvention")),
                                                                                  StringUtils.EventType_IP, model.getAs("Currency"), new POF_IP_LAM(), new STF_IP_PAM(), model.getAs("BusinessDayConvention"));
            // remove last event that falls exactly on cycle anchor date of principal redemption
            interestEvents.remove(EventFactory.createEvent(model.getAs("CycleAnchorDateOfPrincipalRedemption"), StringUtils.EventType_IP,
                                                           model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM(), model.getAs("BusinessDayConvention")));
            // adapt if interest capitalization set
            if (!CommonUtils.isNull(model.getAs("CapitalizationEndDate"))) {
                // for all events with time <= IPCED && type == "IP" do
                // change type to IPCI and payoff/state-trans functions
                ContractEvent capitalizationEnd = EventFactory.createEvent(model.getAs("CapitalizationEndDate"), StringUtils.EventType_IPCI,
                                                                            model.getAs("Currency"), new POF_IPCI_PAM(), new STF_IPCI_LAM(), model.getAs("BusinessDayConvention"));
                interestEvents.forEach(e -> {
                    if (e.type().equals(StringUtils.EventType_IP) && e.compareTo(capitalizationEnd) == -1) {
                        e.type(StringUtils.EventType_IPCI);
                        e.fPayOff(new POF_IPCI_PAM());
                        e.fStateTrans(new STF_IPCI_LAM());
                    }
                });
                // also, remove any IP event exactly at IPCED and replace with an IPCI event
                interestEvents.remove(EventFactory.createEvent(model.getAs("CapitalizationEndDate"), StringUtils.EventType_IP,
                                                                            model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM(), model.getAs("BusinessDayConvention")));
                interestEvents.add(capitalizationEnd);
            }
            events.addAll(interestEvents);
        }
        // rate reset (if specified)
        if (!CommonUtils.isNull(model.getAs("CycleOfRateReset"))) {            
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfRateReset"), maturity,
                                                                            model.getAs("CycleOfRateReset"), model.getAs("EndOfMonthConvention"),false),
                                            StringUtils.EventType_RR, model.getAs("Currency"), new POF_RR_PAM(), new STF_RR_LAM(), model.getAs("BusinessDayConvention")));
            if (model.<Double>getAs("NextResetRate")!=0) {
            	events.forEach(e->{
            	  if(e.type().equals(StringUtils.EventType_RR) && e.time().equals(model.getAs("CycleAnchorDateOfRateReset"))) {
            	       e.fPayOff(new POF_RRY_PAM());
            	       e.fStateTrans(new STF_RRY_LAM());
            	  }
            	});
            }
        }
        // fees (if specified)
        if (!CommonUtils.isNull(model.getAs("CycleOfFee"))) { 
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfFee"), maturity,
                                                                            model.getAs("CycleOfFee"), model.getAs("EndOfMonthConvention")),
                                             StringUtils.EventType_FP, model.getAs("Currency"), new POF_FP_PAM(), new STF_FP_LAM(), model.getAs("BusinessDayConvention")));
        }
        // scaling (if specified)
        if (!CommonUtils.isNull(model.getAs("ScalingEffect")) && (model.<String>getAs("ScalingEffect").contains("I") || model.<String>getAs("ScalingEffect").contains("N"))) { 
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfScalingIndex"), maturity,
                                                                            model.getAs("CycleOfScalingIndex"), model.getAs("EndOfMonthConvention"),false),
                                             StringUtils.EventType_SC, model.getAs("Currency"), new POF_SC_PAM(), new STF_SC_LAM(), model.getAs("BusinessDayConvention")));
        }
        // interest calculation base (if specified)
        if (!CommonUtils.isNull(model.getAs("InterestCalculationBase")) && model.getAs("InterestCalculationBase").equals("NTL")) { 
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfInterestCalculationBase"), maturity,
                                                                            model.getAs("CycleOfScalingIndex"), model.getAs("EndOfMonthConvention")),
                                             StringUtils.EventType_IPCB, model.getAs("Currency"), new POF_IPCB_LAM(), new STF_IPCB_LAM(), model.getAs("BusinessDayConvention")));
        }
        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            ContractEvent termination =
                                        EventFactory.createEvent(model.getAs("TerminationDate"), StringUtils.EventType_TD, model.getAs("Currency"), new POF_TD_LAM(), new STF_TD_PAM());
            events.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
            events.add(termination);
        }
        
        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null,
                                                                  null)) == -1);
        
        // return events
        return new ArrayList<ContractEvent>(events);
    }

    private static ArrayList<ContractEvent> initContingentEvents(Set<LocalDateTime> analysisTimes, ContractModelProvider model, LocalDateTime maturity, RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        HashSet<ContractEvent> events = new HashSet<ContractEvent>();

        // optionality i.e. prepayment right (if specified)
        if (!(CommonUtils.isNull(model.getAs("CycleOfOptionality")) && CommonUtils.isNull(model.getAs("CycleAnchorDateOfOptionality")))) {
            Set<LocalDateTime> times;
            if(!CommonUtils.isNull(model.getAs("CycleOfOptionality"))) {
                times = ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfOptionality"), maturity,model.getAs("CycleOfOptionality"), model.getAs("EndOfMonthConvention"));
            } else {
                times = riskFactorModel.times(model.getAs("ObjectCodeOfPrepaymentModel"));
                times.removeIf(e -> e.compareTo(model.getAs("CycleAnchorDateOfOptionality"))==-1);
            }
            events.addAll(EventFactory.createEvents(times,StringUtils.EventType_PP, model.getAs("Currency"), new POF_PP_PAM(), new STF_PP_LAM(), model.getAs("BusinessDayConvention")));
            if(model.getAs("PenaltyType")!="O") {
                events.addAll(EventFactory.createEvents(times,StringUtils.EventType_PY, model.getAs("Currency"), new POF_PY_PAM(), new STF_PY_LAM(), model.getAs("BusinessDayConvention")));
            }
        }
        // add counterparty default risk-factor contingent events
        if(riskFactorModel.keys().contains(model.getAs("LegalEntityIDCounterparty"))) {
            events.addAll(EventFactory.createEvents(riskFactorModel.times(model.getAs("LegalEntityIDCounterparty")),
                    StringUtils.EventType_CD, model.getAs("Currency"), new POF_CD_PAM(), new STF_CD_LAM()));
        }

        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null,
                null)) == -1);

        // return events
        return new ArrayList<ContractEvent>(events);
    }

    private static LocalDateTime maturity(ContractModelProvider model) {
        // determine maturity of the contract
        LocalDateTime maturity = model.getAs("MaturityDate");
        if (CommonUtils.isNull(maturity)) {
            LocalDateTime lastEvent;
            if(model.<LocalDateTime>getAs("CycleAnchorDateOfPrincipalRedemption").isBefore(model.getAs("StatusDate"))) {
                Set<LocalDateTime> previousEvents = ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfPrincipalRedemption"),model.getAs("StatusDate"),
                        model.getAs("CycleOfPrincipalRedemption"), model.getAs("EndOfMonthConvention"));
                previousEvents.removeIf( d -> d.isBefore(model.<LocalDateTime>getAs("StatusDate").minus(CycleUtils.parsePeriod(model.getAs("CycleOfInterestPayment")))));
                previousEvents.remove(model.getAs("StatusDate"));
                lastEvent = previousEvents.toArray(new LocalDateTime[1])[0];
            } else {
                lastEvent = model.getAs("CycleAnchorDateOfPrincipalRedemption");
            }
            Period cyclePeriod = CycleUtils.parsePeriod(model.getAs("CycleOfPrincipalRedemption"));
            maturity = lastEvent.plus(cyclePeriod.multipliedBy((int) Math.ceil(model.<Double>getAs("NotionalPrincipal")/model.<Double>getAs("NextPrincipalRedemptionPayment"))));
        }
        return maturity;
    }

    private static StateSpace initStateSpace(ContractModelProvider model, LocalDateTime maturity) throws AttributeConversionException {
        StateSpace states = new StateSpace();

        // TODO: some attributes can be null
        states.contractRoleSign = ContractRoleConvention.roleSign(model.getAs("ContractRole"));
        states.lastEventTime = model.getAs("StatusDate");
        states.nominalScalingMultiplier = 1;
        states.interestScalingMultiplier = 1;
        
        // init next principal redemption payment amount (can be null!)
        if (CommonUtils.isNull(model.getAs("NextPrincipalRedemptionPayment"))) {
            // count number of remaining events
            Set<LocalDateTime> remainingEvents = ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfPrincipalRedemption"),maturity,
                                            model.getAs("CycleOfPrincipalRedemption"), model.getAs("EndOfMonthConvention"));
            remainingEvents.removeIf( d -> d.isBefore(model.getAs("StatusDate")) );
            remainingEvents.remove(model.getAs("StatusDate"));
            int n = remainingEvents.size();
            // compute periodic payment in order to redeem entire nominal value
            states.nextPrincipalRedemptionPayment = states.contractRoleSign * model.<Double>getAs("NotionalPrincipal")/n;
        } else {
            states.nextPrincipalRedemptionPayment = states.contractRoleSign * model.<Double>getAs("NextPrincipalRedemptionPayment");
        }
        
        if (!model.<LocalDateTime>getAs("InitialExchangeDate").isAfter(model.getAs("StatusDate"))) {
            states.nominalValue = model.getAs("NotionalPrincipal");
            states.nominalRate = model.getAs("NominalInterestRate");
            states.nominalAccrued = model.getAs("AccruedInterest");
            states.feeAccrued = model.getAs("FeeAccrued");
            states.interestCalculationBase = states.contractRoleSign * ( (model.getAs("InterestCalculationBase").equals("NT"))? model.<Double>getAs("NotionalPrincipal") : model.<Double>getAs("InterestCalculationBaseAmount") );
        }
        
        // return the initialized state space
        return states;
    }

}

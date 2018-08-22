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
import org.actus.functions.nam.POF_PR_NAM;
import org.actus.functions.nam.STF_PR_NAM;
import org.actus.states.StateSpace;
import org.actus.events.EventFactory;
import org.actus.time.ScheduleFactory;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.util.Constants;
import org.actus.util.CommonUtils;
import org.actus.util.StringUtils;
import org.actus.util.CycleUtils;
import org.actus.util.AnnuityUtils;
import org.actus.functions.pam.*;
import org.actus.functions.lam.*;
import org.actus.functions.nam.*;
import org.actus.functions.ann.*;
import org.actus.functions.PayOffFunction;
import org.actus.functions.StateTransitionFunction;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the Annuity contract algorithm
 * 
 * @see <a href="http://www.projectactus.org/"></a>
 */
public final class Annuity {

    // forward projection of the entire lifecycle of the contract
    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {

        // determine maturity of the contract
        LocalDateTime maturity = maturity(model);
        
        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(analysisTimes,model,maturity);

        // compute and add contingent events
        events.addAll(initContingentEvents(model,maturity,riskFactorModel));

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
        return Annuity.lifecycle(analysisTimes,model,riskFactorModel).stream().filter(ev->StringUtils.TransactionalEvents.contains(ev.type())).collect(Collectors.toCollection(ArrayList::new));
    }

    // compute the contract schedule
    public static ArrayList<ContractEvent> schedule(ContractModelProvider model) throws AttributeConversionException {
        // determine maturity of the contract
        LocalDateTime maturity = maturity(model);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(new HashSet<LocalDateTime>(),model,maturity);

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

        // return events
        return events;
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

        // determine maturity of the contract
        LocalDateTime maturity = maturity(model);

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

    // compute (without evaluation) all events of the contract
    private static ArrayList<ContractEvent> initEvents(Set<LocalDateTime> analysisTimes, ContractModelProvider model,LocalDateTime maturity) throws AttributeConversionException {
        HashSet<ContractEvent> events = new HashSet<ContractEvent>();

        // create contract event schedules
        // analysis events
        events.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
        // initial exchange
        events.add(EventFactory.createEvent(model.getAs("InitialExchangeDate"), StringUtils.EventType_IED, model.getAs("Currency"), new POF_IED_PAM(), new STF_IED_LAM()));
        // principal redemption schedule
        Set<LocalDateTime> prSchedule = ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfPrincipalRedemption"), maturity,
                model.getAs("CycleOfPrincipalRedemption"), model.getAs("EndOfMonthConvention"), false);
        // -> chose right state transition function depending on ipcb attributes
        StateTransitionFunction stf=(!CommonUtils.isNull(model.getAs("InterestCalculationBase")) && model.getAs("InterestCalculationBase").equals("NTL"))? new STF_PR_NAM() : new STF_PR2_NAM();
        // regular principal redemption events
        events.addAll(EventFactory.createEvents(prSchedule, StringUtils.EventType_PR,
            model.getAs("Currency"), new POF_PR_NAM(), stf, model.getAs("BusinessDayConvention")));
        // regular interest payments aligned with principal redemption schedule
        events.addAll(EventFactory.createEvents(prSchedule, StringUtils.EventType_IP, model.getAs("Currency"), new POF_IP_LAM(), new STF_IP_ANN(), model.getAs("BusinessDayConvention")));
        // generate an IP at PRANX-1PRCL if IPANX is not defined
        LocalDateTime ipanx = model.<LocalDateTime>getAs("CycleAnchorDateOfPrincipalRedemption").minus(CycleUtils.parsePeriod(model.getAs("CycleOfPrincipalRedemption")));
        if(CommonUtils.isNull(model.getAs("CycleAnchorDateOfInterestPayment")) && ipanx.isAfter(model.getAs("InitialExchangeDate")))
        	events.add(EventFactory.createEvent(ipanx,StringUtils.EventType_IP, model.getAs("Currency"), new POF_IP_LAM(), new STF_IP_PAM(), model.getAs("BusinessDayConvention")));
        // -> chose right Payoff function depending on maturity
        PayOffFunction pof = (!CommonUtils.isNull(model.getAs("MaturityDate"))? new POF_PR_PAM():new POF_PR_NAM());
            events.add(EventFactory.createEvent(maturity,StringUtils.EventType_PR,model.getAs("Currency"),pof,new STF_PR_PAM(), model.getAs("BusinessDayConvention")));
            events.add(EventFactory.createEvent(maturity,StringUtils.EventType_IP, model.getAs("Currency"), new POF_IP_LAM(), new STF_IP_ANN(), model.getAs("BusinessDayConvention")));
        // purchase
        if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            events.add(EventFactory.createEvent(model.getAs("PurchaseDate"), StringUtils.EventType_PRD, model.getAs("Currency"), new POF_PRD_LAM(), new STF_PRD_ANN()));
        }
        // -> chose right state transition function for IPCI depending on ipcb attributes
        StateTransitionFunction stf_ipci=(!CommonUtils.isNull(model.getAs("InterestCalculationBase")) && model.getAs("InterestCalculationBase").equals("NTL"))? new STF_IPCI_LAM() : new STF_IPCI2_LAM();
        // interest payment related
        if (!CommonUtils.isNull(model.getAs("CycleOfInterestPayment")) || !CommonUtils.isNull(model.getAs("CycleAnchorDateOfInterestPayment"))) {
            // raw interest payment events
            Set<ContractEvent> interestEvents =
                    EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfInterestPayment"),
                            model.getAs("CycleAnchorDateOfPrincipalRedemption"),
                            model.getAs("CycleOfInterestPayment"),
                            model.getAs("EndOfMonthConvention"),false),
                            StringUtils.EventType_IP, model.getAs("Currency"), new POF_IP_LAM(), new STF_IP_ANN(), model.getAs("BusinessDayConvention"));
            // adapt if interest capitalization set
            if (!CommonUtils.isNull(model.getAs("CapitalizationEndDate"))) {
                // for all events with time <= IPCED && type == "IP" do
                // change type to IPCI and payoff/state-trans functions
                ContractEvent capitalizationEnd = EventFactory.createEvent(model.getAs("CapitalizationEndDate"), StringUtils.EventType_IPCI,
                        model.getAs("Currency"), new POF_IPCI_PAM(), stf_ipci, model.getAs("BusinessDayConvention"));
                interestEvents.forEach(e -> {
                    if (e.type().equals(StringUtils.EventType_IP) && e.compareTo(capitalizationEnd) == -1) {
                        e.type(StringUtils.EventType_IPCI);
                        e.fPayOff(new POF_IPCI_PAM());
                        e.fStateTrans(stf_ipci);
                    }
                });
                // also, remove any IP event exactly at IPCED and replace with an IPCI event
                interestEvents.remove(EventFactory.createEvent(model.getAs("CapitalizationEndDate"), StringUtils.EventType_IP,
                        model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM(), model.getAs("BusinessDayConvention")));
            }
            events.addAll(interestEvents);
        } else if(!CommonUtils.isNull(model.getAs("CapitalizationEndDate"))) {
            // if no extra interest schedule set but capitalization end date, add single IPCI event
            events.add(EventFactory.createEvent(model.getAs("CapitalizationEndDate"), StringUtils.EventType_IPCI,
                    model.getAs("Currency"), new POF_IPCI_PAM(), stf_ipci, model.getAs("BusinessDayConvention")));
        }
        // rate reset
        	Set<ContractEvent> rateResetEvents = EventFactory.createEvents(ScheduleFactory.createSchedule(model.<LocalDateTime>getAs("CycleAnchorDateOfRateReset"), maturity,
                    model.getAs("CycleOfRateReset"), model.getAs("EndOfMonthConvention"),false),
                    StringUtils.EventType_RR, model.getAs("Currency"), new POF_RR_PAM(), new STF_RR_ANN(), model.getAs("BusinessDayConvention"));
        	
        	if(!CommonUtils.isNull(model.getAs("NextResetRate"))) 
        	rateResetEvents.stream().sorted().
        	filter(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null, null)) == 1).findFirst().get().fStateTrans(new STF_RRY_ANN());
        	events.addAll(rateResetEvents);
        // fees (if specified)
        if (!CommonUtils.isNull(model.getAs("CycleOfFee"))) {
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfFee"), maturity,
                    model.getAs("CycleOfFee"), model.getAs("EndOfMonthConvention"),false),
                    StringUtils.EventType_FP, model.getAs("Currency"), new POF_FP_PAM(), new STF_FP_LAM(), model.getAs("BusinessDayConvention")));
        }
        // scaling (if specified)
        if (!CommonUtils.isNull(model.getAs("ScalingEffect")) && (model.<String>getAs("ScalingEffect").contains("I") || model.<String>getAs("ScalingEffect").contains("N"))) {
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("SycleAnchorDateOfScalingIndex"), maturity,
                    model.getAs("SycleOfScalingIndex"), model.getAs("EndOfMonthConvention"),false),
                    StringUtils.EventType_SC, model.getAs("Currency"), new POF_SC_PAM(), new STF_SC_LAM(), model.getAs("BusinessDayConvention")));
        }
        // interest calculation base (if specified)
        if (!CommonUtils.isNull(model.getAs("InterestCalculationBase")) && model.getAs("InterestCalculationBase").equals("NTL")) {
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfInterestCalculationBase"), maturity,
                    model.getAs("CycleOfScalingIndex"), model.getAs("EndOfMonthConvention"),false),
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


    // compute (without evaluation) all events of the contract
    private static ArrayList<ContractEvent> initContingentEvents(ContractModelProvider model, LocalDateTime maturity, RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        HashSet<ContractEvent> events = new HashSet<ContractEvent>();

        // optionality i.e. prepayment right (if specified)
        if (!(CommonUtils.isNull(model.getAs("CycleOfOptionality")) && CommonUtils.isNull(model.getAs("CycleAnchorDateOfOptionality")))) {
            Set<LocalDateTime> times = ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfOptionality"), maturity,model.getAs("CycleOfOptionality"), model.getAs("EndOfMonthConvention"));
            events.addAll(EventFactory.createEvents(times,StringUtils.EventType_PP, model.getAs("Currency"), new POF_PP_PAM(), new STF_PP_LAM(), model.getAs("BusinessDayConvention")));
            if(model.<String>getAs("PenaltyType")!="O") {
                events.addAll(EventFactory.createEvents(times,StringUtils.EventType_PY, model.getAs("Currency"), new POF_PY_PAM(), new STF_PY_LAM(), model.getAs("BusinessDayConvention")));
            }
        }
        // compute un-scheduled events
        events.addAll(riskFactorModel.events(model));

        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null,
                null)) == -1);

        // return events
        return new ArrayList<ContractEvent>(events);
    }

    // determine maturity of the contract
    private static LocalDateTime maturity(ContractModelProvider model) {
        // determine maturity of the contract
        LocalDateTime maturity = null;
        if(!CommonUtils.isNull(model.getAs("MaturityDate"))) {
            maturity = model.getAs("MaturityDate");
        } else if(!CommonUtils.isNull(model.getAs("AmortizationDate"))) {
            maturity = model.getAs("AmortizationDate");
        } else if(CommonUtils.isNull(model.getAs("CycleOfRateReset")) || CommonUtils.isNull(model.getAs("InterestCalculationBase")) || model.getAs("InterestCalculationBase").equals("NT")) {
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
                double coupon = model.<Double>getAs("NotionalPrincipal")*model.<Double>getAs("NominalInterestRate")*model.<DayCountCalculator>getAs("DayCountConvention").dayCountFraction(model.getAs("CycleAnchorDateOfPrincipalRedemption"), model.<LocalDateTime>getAs("CycleAnchorDateOfPrincipalRedemption").plus(cyclePeriod));
                maturity = lastEvent.plus(cyclePeriod.multipliedBy((int) Math.ceil(model.<Double>getAs("NotionalPrincipal")/(model.<Double>getAs("NextPrincipalRedemptionPayment")-coupon))));
        } else {
                maturity = model.<LocalDateTime>getAs("InitialExchangeDate").plus(Constants.MAX_LIFETIME);
        }
        return maturity;
    }

    private static StateSpace initStateSpace(ContractModelProvider model) throws AttributeConversionException {
        StateSpace states = new StateSpace();
        states.nominalScalingMultiplier = 1;
        states.interestScalingMultiplier = 1;
        states.contractRoleSign = ContractRoleConvention.roleSign(model.getAs("ContractRole"));
        states.lastEventTime = model.getAs("StatusDate");
        if (!model.<LocalDateTime>getAs("InitialExchangeDate").isAfter(model.getAs("StatusDate"))) {
            states.nominalValue = model.getAs("NotionalPrincipal");
            states.nominalRate = model.getAs("NominalInterestRate");
            // TODO: IPAC can be NULL
            states.nominalAccrued = model.getAs("AccruedInterest");
            // TODO: FEAC can be NULL
            states.feeAccrued = model.getAs("FeeAccrued");
            states.interestCalculationBase = states.contractRoleSign * ( (model.getAs("InterestCalculationBase").equals("NT"))? model.<Double>getAs("NotionalPrincipal") : model.<Double>getAs("InterestCalculationBaseAmount") );
        }
        
        // init next principal redemption payment amount (can be null for ANN!)
        states.nextPrincipalRedemptionPayment = states.contractRoleSign * AnnuityUtils.annuityPayment(model, model.getAs("NotionalPrincipal"), states.nominalAccrued, model.getAs("NominalInterestRate"));
        
        // return the initialized state space
        return states;
    }

}

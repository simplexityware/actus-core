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
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.types.EventType;
import org.actus.types.InterestCalculationBase;
import org.actus.util.Constants;
import org.actus.util.CommonUtils;
import org.actus.util.CycleUtils;
import org.actus.functions.pam.*;
import org.actus.functions.lam.*;
import org.actus.functions.nam.*;
import org.actus.functions.PayOffFunction;
import org.actus.functions.StateTransitionFunction;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;

/**
 * Represents the Negative Amortizer payoff algorithm
 * 
 * @see <a href="https://www.actusfrf.org"></a>
 */
public final class NegativeAmortizer {

    // compute next n non-contingent events
    public static ArrayList<ContractEvent> schedule(LocalDateTime to,
                                                    ContractModelProvider model) throws AttributeConversionException {
        ArrayList<ContractEvent> events = new ArrayList<ContractEvent>();

        // determine maturity of the contract
        LocalDateTime maturity = maturity(model);

        // initial exchange
        events.add(EventFactory.createEvent(model.getAs("InitialExchangeDate"), EventType.IED, model.getAs("Currency"), new POF_IED_PAM(), new STF_IED_LAM(), model.getAs("ContractID")));
        // principal redemption schedule
        Set<LocalDateTime> prSchedule = ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfPrincipalRedemption"), maturity,
                model.getAs("CycleOfPrincipalRedemption"), model.getAs("EndOfMonthConvention"), false);
        // -> chose right state transition function depending on ipcb attributes
        StateTransitionFunction stf=(!CommonUtils.isNull(model.getAs("InterestCalculationBase")) && model.getAs("InterestCalculationBase").equals(InterestCalculationBase.NTL))? new STF_PR_NAM() : new STF_PR2_NAM();
        // regular principal redemption events
        events.addAll(EventFactory.createEvents(prSchedule, EventType.PR,
            model.getAs("Currency"), new POF_PR_NAM(), stf, model.getAs("BusinessDayConvention"), model.getAs("ContractID")));
        // -> chose right Payoff function depending on maturity
        PayOffFunction pof = (!CommonUtils.isNull(model.getAs("MaturityDate"))? new POF_MD_PAM():new POF_PR_NAM());
        events.add(EventFactory.createEvent(maturity,EventType.MD,model.getAs("Currency"),pof,new STF_MD_PAM(), model.getAs("BusinessDayConvention"),model.getAs("ContractID")));
        // purchase
        if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            events.add(EventFactory.createEvent(model.getAs("PurchaseDate"), EventType.PRD, model.getAs("Currency"), new POF_PRD_LAM(), new STF_PRD_LAM(), model.getAs("ContractID")));
        }
        // -> chose right state transition function for IPCI depending on ipcb attributes
        StateTransitionFunction stf_ipci=(!CommonUtils.isNull(model.getAs("InterestCalculationBase")) && model.getAs("InterestCalculationBase").equals(InterestCalculationBase.NTL))? new STF_IPCI_LAM() : new STF_IPCI2_LAM();
        // interest payment related
        if (!CommonUtils.isNull(model.getAs("CycleOfInterestPayment")) || !CommonUtils.isNull(model.getAs("CycleAnchorDateOfInterestPayment"))) {
            // raw interest payment events
            Set<ContractEvent> interestEvents = EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfInterestPayment"),maturity,
                            model.getAs("CycleOfInterestPayment"),model.getAs("EndOfMonthConvention"),true),
                            EventType.IP, model.getAs("Currency"), new POF_IP_NAM(), new STF_IP_NAM(), model.getAs("BusinessDayConvention"), model.getAs("ContractID"));
            // adapt if interest capitalization set
            if (!CommonUtils.isNull(model.getAs("CapitalizationEndDate"))) {
                // for all events with time <= IPCED && type == "IP" do
                // change type to IPCI and payoff/state-trans functions
                ContractEvent capitalizationEnd = EventFactory.createEvent(model.getAs("CapitalizationEndDate"), EventType.IPCI,
                        model.getAs("Currency"), new POF_IPCI_PAM(), stf_ipci, model.getAs("BusinessDayConvention"), model.getAs("ContractID"));
                interestEvents.forEach(e -> {
                    if (e.eventType().equals(EventType.IP) && e.compareTo(capitalizationEnd) == -1) {
                        e.eventType(EventType.IPCI);
                        e.fPayOff(new POF_IPCI_PAM());
                        e.fStateTrans(stf_ipci);
                    }
                });
                // also, remove any IP event exactly at IPCED and replace with an IPCI event
                interestEvents.remove(EventFactory.createEvent(model.getAs("CapitalizationEndDate"), EventType.IP,
                        model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM(), model.getAs("BusinessDayConvention"), model.getAs("ContractID")));
            }
            events.addAll(interestEvents);
        }else if(!CommonUtils.isNull(model.getAs("CapitalizationEndDate"))) {
            // if no extra interest schedule set but capitalization end date, add single IPCI event
            events.add(EventFactory.createEvent(model.getAs("CapitalizationEndDate"), EventType.IPCI,
                    model.getAs("Currency"), new POF_IPCI_PAM(), stf_ipci, model.getAs("BusinessDayConvention"), model.getAs("ContractID")));
        }
        // rate reset
        Set<ContractEvent> rateResetEvents = EventFactory.createEvents(ScheduleFactory.createSchedule(model.<LocalDateTime>getAs("CycleAnchorDateOfRateReset"), maturity,
                model.getAs("CycleOfRateReset"), model.getAs("EndOfMonthConvention"),false),
                EventType.RR, model.getAs("Currency"), new POF_RR_PAM(), new STF_RR_LAM(), model.getAs("BusinessDayConvention"), model.getAs("ContractID"));

        // adapt fixed rate reset event
        if(!CommonUtils.isNull(model.getAs("NextResetRate"))) {
            ContractEvent fixedEvent = rateResetEvents.stream().sorted().filter(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), EventType.AD, model.getAs("Currency"), null, null, model.getAs("ContractID"))) == 1).findFirst().get();
            fixedEvent.fStateTrans(new STF_RRF_LAM());
            fixedEvent.eventType(EventType.RRF);
            rateResetEvents.add(fixedEvent);
        }

        events.addAll(rateResetEvents);
        // fees (if specified)
        if (!CommonUtils.isNull(model.getAs("CycleOfFee"))) {
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfFee"), maturity,
                    model.getAs("CycleOfFee"), model.getAs("EndOfMonthConvention")),
                    EventType.FP, model.getAs("Currency"), new POF_FP_PAM(), new STF_FP_LAM(), model.getAs("BusinessDayConvention"), model.getAs("ContractID")));
        }
        // scaling (if specified)
        if (!CommonUtils.isNull(model.getAs("ScalingEffect")) && (model.<String>getAs("ScalingEffect").contains("I") || model.<String>getAs("ScalingEffect").contains("N"))) {
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfScalingIndex"), maturity,
                    model.getAs("CycleOfScalingIndex"), model.getAs("EndOfMonthConvention"),false),
                    EventType.SC, model.getAs("Currency"), new POF_SC_PAM(), new STF_SC_LAM(), model.getAs("BusinessDayConvention"), model.getAs("ContractID")));
        }
        // interest calculation base (if specified)
        if (!CommonUtils.isNull(model.getAs("InterestCalculationBase")) && model.getAs("InterestCalculationBase").equals(InterestCalculationBase.NTL)) {
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfInterestCalculationBase"), maturity,
                    model.getAs("CycleOfInterestCalculationBase"), model.getAs("EndOfMonthConvention"),false),
                    EventType.IPCB, model.getAs("Currency"), new POF_IPCB_LAM(), new STF_IPCB_LAM(), model.getAs("BusinessDayConvention"), model.getAs("ContractID")));
        }
        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            ContractEvent termination =
                    EventFactory.createEvent(model.getAs("TerminationDate"), EventType.TD, model.getAs("Currency"), new POF_TD_LAM(), new STF_TD_PAM(), model.getAs("ContractID"));
            events.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
            events.add(termination);
        }

        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), EventType.AD, model.getAs("Currency"), null,null, model.getAs("ContractID"))) == -1);

        // remove all post to-date events
        if(CommonUtils.isNull(to)){
            to = maturity;
        }
        ContractEvent postDate = EventFactory.createEvent(to, EventType.AD, model.getAs("Currency"), null, null, model.getAs("ContractID"));
        events.removeIf(e -> e.compareTo(postDate) == 1);
      
        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(events);

        return events;
    }

    // apply a set of events to the current state of a contract and return the post events state
    public static ArrayList<ContractEvent> apply(ArrayList<ContractEvent> events,
                                                 ContractModelProvider model,
                                                 RiskFactorModelProvider observer) throws AttributeConversionException {

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events according to their time sequence
        Collections.sort(events);

        // apply events according to their time sequence to current state
        LocalDateTime initialExchangeDate = model.getAs("InitialExchangeDate");
		ListIterator eventIterator = events.listIterator();
		while (( states.statusDate.isBefore(initialExchangeDate) || states.notionalPrincipal > 0.0) && eventIterator.hasNext()) {
			((ContractEvent) eventIterator.next()).eval(states, model, observer, model.getAs("DayCountConvention"),
					model.getAs("BusinessDayConvention"));
		}

        // return evaluated events
        return events;
    }

    // determine maturity of the contract
    private static LocalDateTime maturity(ContractModelProvider model) {
        LocalDateTime maturity = model.getAs("MaturityDate");
        if (CommonUtils.isNull(maturity)) {
            if(CommonUtils.isNull(model.getAs("CycleOfRateReset")) || CommonUtils.isNull(model.getAs("InterestCalculationBase")) || model.getAs("InterestCalculationBase").equals(InterestCalculationBase.NT)) {
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
        }
        return maturity;
    }

    private static StateSpace initStateSpace(ContractModelProvider model) throws AttributeConversionException {
        StateSpace states = new StateSpace();

        // TODO: some attributes can be null
        states.statusDate = model.getAs("StatusDate");
        states.notionalScalingMultiplier = 1;
        states.interestScalingMultiplier = 1;
        
        // init next principal redemption payment amount (cannot be null for NAM!)
        states.nextPrincipalRedemptionPayment = ContractRoleConvention.roleSign(model.getAs("ContractRole"))*model.<Double>getAs("NextPrincipalRedemptionPayment");
        
        if (!model.<LocalDateTime>getAs("InitialExchangeDate").isAfter(model.getAs("StatusDate"))) {
            states.notionalPrincipal = ContractRoleConvention.roleSign(model.getAs("ContractRole"))*model.<Double>getAs("NotionalPrincipal");
            states.nominalInterestRate = model.getAs("NominalInterestRate");
            states.accruedInterest = ContractRoleConvention.roleSign(model.getAs("ContractRole"))*model.<Double>getAs("AccruedInterest");
            states.feeAccrued = model.getAs("FeeAccrued");
            states.interestCalculationBaseAmount = ContractRoleConvention.roleSign(model.getAs("ContractRole"))*( (model.getAs("InterestCalculationBase").equals(InterestCalculationBase.NT))? model.<Double>getAs("NotionalPrincipal") : model.<Double>getAs("InterestCalculationBaseAmount") );
        }
        
        // return the initialized state space
        return states;
    }

}

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
import org.actus.util.Constants;
import org.actus.util.CommonUtils;
import org.actus.util.StringUtils;
import org.actus.util.CycleUtils;
import org.actus.functions.pam.POF_AD_PAM;
import org.actus.functions.pam.STF_AD_PAM;
import org.actus.functions.pam.POF_IED_PAM;
import org.actus.functions.lam.STF_IED_LAM;
import org.actus.functions.nam.POF_PR_NAM;
import org.actus.functions.nam.STF_PR_NAM;
import org.actus.functions.lam.POF_PRD_LAM;
import org.actus.functions.lam.STF_PRD_LAM;
import org.actus.functions.lam.POF_IP_LAM;
import org.actus.functions.pam.STF_IP_PAM;
import org.actus.functions.pam.POF_IPCI_PAM;
import org.actus.functions.lam.STF_IPCI_LAM;
import org.actus.functions.pam.POF_RR_PAM;
import org.actus.functions.lam.STF_RR_LAM;
import org.actus.functions.pam.POF_SC_PAM;
import org.actus.functions.lam.STF_SC_LAM;
import org.actus.functions.pam.POF_PP_PAM;
import org.actus.functions.lam.STF_PP_LAM;
import org.actus.functions.pam.POF_PY_PAM;
import org.actus.functions.lam.STF_PY_LAM;
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
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents the Negative-Amortizer payoff algorithm
 * 
 * @see <a href="http://www.projectactus.org/"></a>
 */
public final class NegativeAmortizer {

    public static ArrayList<ContractEvent> evalAll(Set<LocalDateTime> analysisTimes,
                                                   ContractModelProvider model,
                                                   RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        
        // determine maturity of the contract
        LocalDateTime maturity = model.getAs("MaturityDate");
        if (CommonUtils.isNull(maturity)) {
                if(CommonUtils.isNull(model.getAs("CycleOfRateReset")) || CommonUtils.isNull(model.getAs("InterestCalculationBase")) || model.getAs("InterestCalculationBase").equals("NT")) {
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
        
        // compute events
        ArrayList<ContractEvent> payoff = initEvents(analysisTimes,model,riskFactorModel,maturity);
        
        // initialize state space per status date
        StateSpace states = initStateSpace(model);
        
        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(payoff);

        // evaluate events
        payoff.forEach(e -> e.eval(states, model, riskFactorModel, model.getAs("DayCountConvention"), model.getAs("BusinessDayConvention")));
        
        // remove pre-purchase events if purchase date set (we only consider post-purchase events for analysis)
        if(!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            payoff.removeIf(e -> !e.type().equals(StringUtils.EventType_AD) && e.compareTo(EventFactory.createEvent(model.getAs("PurchaseDate"), StringUtils.EventType_PRD, model.getAs("Currency"), null, null)) == -1);    
        }
        
        // return all evaluated post-StatusDate events as the payoff
        return payoff;
    }
    
    private static ArrayList<ContractEvent> initEvents(Set<LocalDateTime> analysisTimes, ContractModelProvider model, RiskFactorModelProvider riskFactorModel, LocalDateTime maturity) throws AttributeConversionException {
        HashSet<ContractEvent> events = new HashSet<ContractEvent>();   
        
        // create contract event schedules
        // analysis events
        events.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
        // initial exchange
        events.add(EventFactory.createEvent(model.getAs("InitialExchangeDate"), StringUtils.EventType_IED, model.getAs("Currency"), new POF_IED_PAM(), new STF_IED_LAM()));
        // principal redemption
        events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfPrincipalRedemption"), maturity,
                                                                            model.getAs("CycleOfPrincipalRedemption"), model.getAs("EndOfMonthConvention")),
                                            StringUtils.EventType_PR, model.getAs("Currency"), new POF_PR_NAM(), new STF_PR_NAM(), model.getAs("BusinessDayConvention")));     
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
                                                                            model.getAs("CycleOfRateReset"), model.getAs("EndOfMonthConvention")),
                                            StringUtils.EventType_RR, model.getAs("Currency"), new POF_RR_PAM(), new STF_RR_LAM(), model.getAs("BusinessDayConvention")));
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
                                                                            model.getAs("CycleOfScalingIndex"), model.getAs("EndOfMonthConvention")),
                                             StringUtils.EventType_SC, model.getAs("Currency"), new POF_SC_PAM(), new STF_SC_LAM(), model.getAs("BusinessDayConvention")));
        }
        // interest calculation base (if specified)
        if (!CommonUtils.isNull(model.getAs("InterestCalculationBase")) && model.getAs("InterestCalculationBase").equals("NTL")) { 
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfInterestCalculationBase"), maturity,
                                                                            model.getAs("CycleOfScalingIndex"), model.getAs("EndOfMonthConvention")),
                                             StringUtils.EventType_IPCB, model.getAs("Currency"), new POF_IPCB_LAM(), new STF_IPCB_LAM(), model.getAs("BusinessDayConvention")));
        }
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
        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            ContractEvent termination =
                                        EventFactory.createEvent(model.getAs("TerminationDate"), StringUtils.EventType_TD, model.getAs("Currency"), new POF_TD_LAM(), new STF_TD_PAM());
            events.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
            events.add(termination);
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

    private static StateSpace initStateSpace(ContractModelProvider model) throws AttributeConversionException {
        StateSpace states = new StateSpace();

        // TODO: some attributes can be null
        states.contractRoleSign = ContractRoleConvention.roleSign(model.getAs("ContractRole"));
        states.lastEventTime = model.getAs("StatusDate");
        
        // init next principal redemption payment amount (cannot be null for NAM!)
        states.nextPrincipalRedemptionPayment = states.contractRoleSign * model.<Double>getAs("NextPrincipalRedemptionPayment");
        
        if (!model.<LocalDateTime>getAs("InitialExchangeDate").isAfter(model.getAs("StatusDate"))) {
            states.nominalValue = model.getAs("NotionalPrincipal");
            states.nominalRate = model.getAs("NominalInterestRate");
            states.nominalAccrued = model.getAs("AccruedInterest");
            states.feeAccrued = model.getAs("FeeAccrued");
            states.nominalScalingMultiplier = 1;
            states.interestScalingMultiplier = 1;
            states.interestCalculationBase = states.contractRoleSign * ( (model.getAs("InterestCalculationBase").equals("NT"))? model.<Double>getAs("NotionalPrincipal") : model.<Double>getAs("InterestCalculationBaseAmount") );
        }
        
        // return the initialized state space
        return states;
    }

}

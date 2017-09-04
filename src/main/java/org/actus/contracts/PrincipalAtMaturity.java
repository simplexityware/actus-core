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
import org.actus.util.CommonUtils;
import org.actus.util.StringUtils;
import org.actus.functions.pam.POF_AD_PAM;
import org.actus.functions.pam.STF_AD_PAM;
import org.actus.functions.pam.POF_IED_PAM;
import org.actus.functions.pam.STF_IED_PAM;
import org.actus.functions.pam.POF_PR_PAM;
import org.actus.functions.pam.STF_PR_PAM;
import org.actus.functions.pam.POF_PRD_PAM;
import org.actus.functions.pam.STF_PRD_PAM;
import org.actus.functions.pam.POF_IP_PAM;
import org.actus.functions.pam.STF_IP_PAM;
import org.actus.functions.pam.POF_IPCI_PAM;
import org.actus.functions.pam.STF_IPCI_PAM;
import org.actus.functions.pam.POF_RR_PAM;
import org.actus.functions.pam.STF_RR_PAM;
import org.actus.functions.pam.POF_SC_PAM;
import org.actus.functions.pam.STF_SC_PAM;
import org.actus.functions.pam.POF_PP_PAM;
import org.actus.functions.pam.STF_PP_PAM;
import org.actus.functions.pam.POF_PY_PAM;
import org.actus.functions.pam.STF_PY_PAM;
import org.actus.functions.pam.POF_FP_PAM;
import org.actus.functions.pam.STF_FP_PAM;
import org.actus.functions.pam.POF_TD_PAM;
import org.actus.functions.pam.STF_TD_PAM;
import org.actus.functions.pam.POF_CD_PAM;
import org.actus.functions.pam.STF_CD_PAM;


import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents the Principal-At-Maturity payoff algorithm
 * 
 * @see <a href="http://www.projectactus.org/"></a>
 */
public final class PrincipalAtMaturity {

    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        
        // compute events
        ArrayList<ContractEvent> payoff = initEvents(analysisTimes,model,riskFactorModel);
        
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
    
    private static ArrayList<ContractEvent> initEvents(Set<LocalDateTime> analysisTimes, ContractModelProvider model, RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        HashSet<ContractEvent> events = new HashSet<ContractEvent>();

        // create contract event schedules
        // analysis events
        events.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
        // initial exchange
        events.add(EventFactory.createEvent(model.getAs("InitialExchangeDate"), StringUtils.EventType_IED, model.getAs("Currency"), new POF_IED_PAM(), new STF_IED_PAM()));
        // principal redemption
        events.add(EventFactory.createEvent(model.getAs("MaturityDate"), StringUtils.EventType_PR, model.getAs("Currency"), new POF_PR_PAM(), new STF_PR_PAM()));
        // purchase
        if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            events.add(EventFactory.createEvent(model.getAs("PurchaseDate"), StringUtils.EventType_PRD, model.getAs("Currency"), new POF_PRD_PAM(), new STF_PRD_PAM()));
        }
        // interest payment related
        if (!CommonUtils.isNull(model.getAs("NominalInterestRate"))) {
            // raw interest payment events
            Set<ContractEvent> interestEvents =
                                                        EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfInterestPayment"),
                                                                                                                 model.getAs("MaturityDate"),
                                                                                                                 model.getAs("CycleOfInterestPayment"),
                                                                                                                 model.getAs("EndOfMonthConvention")),
                                                                                  StringUtils.EventType_IP, model.getAs("Currency"), new POF_IP_PAM(), new STF_IP_PAM(), model.getAs("BusinessDayConvention"));
            // adapt if interest capitalization set
            if (!CommonUtils.isNull(model.getAs("CapitalizationEndDate"))) {
                // for all events with time <= IPCED && type == "IP" do
                // change type to IPCI and payoff/state-trans functions
                ContractEvent capitalizationEnd =
                                                  EventFactory.createEvent(model.getAs("CapitalizationEndDate"), StringUtils.EventType_IPCI,
                                                                            model.getAs("Currency"), new POF_IPCI_PAM(), new STF_IPCI_PAM(), model.getAs("BusinessDayConvention"));
                interestEvents.forEach(e -> {
                    if (e.type().equals(StringUtils.EventType_IP) && e.compareTo(capitalizationEnd) == -1) {
                        e.type(StringUtils.EventType_IPCI);
                        e.fPayOff(new POF_IPCI_PAM());
                        e.fStateTrans(new STF_IPCI_PAM());
                    }
                });
                // also, remove any IP event exactly at IPCED and replace with an IPCI event
                interestEvents.remove(EventFactory.createEvent(model.getAs("CapitalizationEndDate"), StringUtils.EventType_IP,
                                                                            model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM(), model.getAs("BusinessDayConvention")));
                interestEvents.add(capitalizationEnd);
            }
            events.addAll(interestEvents);
            // rate reset (if specified)
            if (!CommonUtils.isNull(model.getAs("CycleOfRateReset"))) {            
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfRateReset"), model.getAs("MaturityDate"),
                                                                                model.getAs("CycleOfRateReset"), model.getAs("EndOfMonthConvention")),
                                                 StringUtils.EventType_RR, model.getAs("Currency"), new POF_RR_PAM(), new STF_RR_PAM(), model.getAs("BusinessDayConvention")));
            }
        }
        // fees (if specified)
        if (!CommonUtils.isNull(model.getAs("CycleOfFee"))) { 
        events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfFee"), model.getAs("MaturityDate"),
                                                                            model.getAs("CycleOfFee"), model.getAs("EndOfMonthConvention")),
                                             StringUtils.EventType_FP, model.getAs("Currency"), new POF_FP_PAM(), new STF_FP_PAM(), model.getAs("BusinessDayConvention")));
        }
        // scaling (if specified)
        String scalingEffect=model.getAs("ScalingEffect");
        if (!CommonUtils.isNull(scalingEffect) && (scalingEffect.contains("I") || scalingEffect.contains("N"))) { 
        events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfScalingIndex"), model.getAs("MaturityDate"),
                                                                            model.getAs("CycleOfScalingIndex"), model.getAs("EndOfMonthConvention")),
                                             StringUtils.EventType_SC, model.getAs("Currency"), new POF_SC_PAM(), new STF_SC_PAM(), model.getAs("BusinessDayConvention")));
        }
        // optionality i.e. prepayment right (if specified)
        if (!(CommonUtils.isNull(model.getAs("CycleOfOptionality")) && CommonUtils.isNull(model.getAs("CycleAnchorDateOfOptionality")))) {
            Set<LocalDateTime> times;
            if(!CommonUtils.isNull(model.getAs("CycleOfOptionality"))) {
                times = ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfOptionality"), model.getAs("MaturityDate"),model.getAs("CycleOfOptionality"), model.getAs("EndOfMonthConvention"));
            } else {
                times = riskFactorModel.times(model.getAs("ObjectCodeOfPrepaymentModel"));
                times.removeIf(e -> e.compareTo(model.getAs("CycleAnchorDateOfOptionality"))==-1);
            }
            events.addAll(EventFactory.createEvents(times,StringUtils.EventType_PP, model.getAs("Currency"), new POF_PP_PAM(), new STF_PP_PAM(), model.getAs("BusinessDayConvention")));
            if(((char) model.getAs("PenaltyType"))!='O') {
                events.addAll(EventFactory.createEvents(times,StringUtils.EventType_PY, model.getAs("Currency"), new POF_PY_PAM(), new STF_PY_PAM(), model.getAs("BusinessDayConvention")));         
            }
        }
        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            ContractEvent termination =
                                        EventFactory.createEvent(model.getAs("TerminationDate"), StringUtils.EventType_TD, model.getAs("Currency"), new POF_TD_PAM(), new STF_TD_PAM());
            events.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
            events.add(termination);
        }
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

    private static StateSpace initStateSpace(ContractModelProvider model) throws AttributeConversionException {
        StateSpace states = new StateSpace();

        // TODO: some attributes can be null
        states.contractRoleSign = ContractRoleConvention.roleSign(model.getAs("ContractRole"));
        states.lastEventTime = model.getAs("StatusDate");
        if (!((LocalDateTime) model.getAs("InitialExchangeDate")).isAfter(model.getAs("StatusDate"))) {
            states.nominalValue = model.getAs("NotionalPrincipal");
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

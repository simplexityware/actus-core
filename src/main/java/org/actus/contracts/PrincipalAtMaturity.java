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

    public static ArrayList<ContractEvent> eval(Set<LocalDateTime> analysisTimes, 
                        		         ContractModelProvider model, 
                        		         RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        
        // compute events
        ArrayList<ContractEvent> payoff = initEvents(analysisTimes,model,riskFactorModel);
        
        // initialize state space per status date
        StateSpace states = initStateSpace(model);
        
        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(payoff);

        // evaluate events
        payoff.forEach(e -> e.eval(states, model, riskFactorModel, model.dayCountConvention(), model.businessDayConvention()));
        
        // remove pre-purchase events if purchase date set (we only consider post-purchase events for analysis)
        if(!CommonUtils.isNull(model.purchaseDate())) {
            payoff.removeIf(e -> !e.type().equals(StringUtils.EventType_AD) && e.compareTo(EventFactory.createEvent(model.purchaseDate(), StringUtils.EventType_PRD, model.currency(), null, null)) == -1);    
        }
        
        // return all evaluated post-StatusDate events as the payoff
        return payoff;
    }
    
    private static ArrayList<ContractEvent> initEvents(Set<LocalDateTime> analysisTimes, ContractModelProvider model, RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        HashSet<ContractEvent> events = new HashSet<ContractEvent>();

        // create contract event schedules
        // analysis events
        events.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.currency(), new POF_AD_PAM(), new STF_AD_PAM()));
        // initial exchange
        events.add(EventFactory.createEvent(model.initialExchangeDate(), StringUtils.EventType_IED, model.currency(), new POF_IED_PAM(), new STF_IED_PAM()));
        // principal redemption
        events.add(EventFactory.createEvent(model.maturityDate(), StringUtils.EventType_PR, model.currency(), new POF_PR_PAM(), new STF_PR_PAM()));
        // purchase
        if (!CommonUtils.isNull(model.purchaseDate())) {
            events.add(EventFactory.createEvent(model.purchaseDate(), StringUtils.EventType_PRD, model.currency(), new POF_PRD_PAM(), new STF_PRD_PAM()));
        }
        // interest payment related
        if (!CommonUtils.isNull(model.nominalInterestRate())) {
            // raw interest payment events
            Set<ContractEvent> interestEvents =
                                                        EventFactory.createEvents(ScheduleFactory.createSchedule(model.cycleAnchorDateOfInterestPayment(),
                                                                                                                 model.maturityDate(),
                                                                                                                 model.cycleOfInterestPayment(),
                                                                                                                 model.endOfMonthConvention()),
                                                                                  StringUtils.EventType_IP, model.currency(), new POF_IP_PAM(), new STF_IP_PAM(), model.businessDayConvention());
            // adapt if interest capitalization set
            if (!CommonUtils.isNull(model.capitalizationEndDate())) {
                // for all events with time <= IPCED && type == "IP" do
                // change type to IPCI and payoff/state-trans functions
                ContractEvent capitalizationEnd =
                                                  EventFactory.createEvent(model.capitalizationEndDate(), StringUtils.EventType_IPCI,
                                                                            model.currency(), new POF_IPCI_PAM(), new STF_IPCI_PAM(), model.businessDayConvention());
                interestEvents.forEach(e -> {
                    if (e.type().equals(StringUtils.EventType_IP) && e.compareTo(capitalizationEnd) == -1) {
                        e.type(StringUtils.EventType_IPCI);
                        e.fPayOff(new POF_IPCI_PAM());
                        e.fStateTrans(new STF_IPCI_PAM());
                    }
                });
                // also, remove any IP event exactly at IPCED and replace with an IPCI event
                interestEvents.remove(EventFactory.createEvent(model.capitalizationEndDate(), StringUtils.EventType_IP,
                                                                            model.currency(), new POF_AD_PAM(), new STF_AD_PAM(), model.businessDayConvention()));
                interestEvents.add(capitalizationEnd);
            }
            events.addAll(interestEvents);
            // rate reset (if specified)
            if (!CommonUtils.isNull(model.cycleOfRateReset())) {            
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.cycleAnchorDateOfRateReset(), model.maturityDate(),
                                                                                model.cycleOfRateReset(), model.endOfMonthConvention()),
                                                 StringUtils.EventType_RR, model.currency(), new POF_RR_PAM(), new STF_RR_PAM(), model.businessDayConvention()));
            }
        }
        // fees (if specified)
        if (!CommonUtils.isNull(model.cycleOfFee())) { 
        events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.cycleAnchorDateOfFee(), model.maturityDate(),
                                                                            model.cycleOfFee(), model.endOfMonthConvention()),
                                             StringUtils.EventType_FP, model.currency(), new POF_FP_PAM(), new STF_FP_PAM(), model.businessDayConvention()));
        }
        // scaling (if specified)
        if (!CommonUtils.isNull(model.scalingEffect()) && (model.scalingEffect().contains("I") || model.scalingEffect().contains("N"))) { 
        events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.cycleAnchorDateOfScalingIndex(), model.maturityDate(),
                                                                            model.cycleOfScalingIndex(), model.endOfMonthConvention()),
                                             StringUtils.EventType_SC, model.currency(), new POF_SC_PAM(), new STF_SC_PAM(), model.businessDayConvention()));
        }
        // optionality i.e. prepayment right (if specified)
        if (!(CommonUtils.isNull(model.cycleOfOptionality()) && CommonUtils.isNull(model.cycleAnchorDateOfOptionality()))) {
            Set<LocalDateTime> times;
            if(!CommonUtils.isNull(model.cycleOfOptionality())) {
                times = ScheduleFactory.createSchedule(model.cycleAnchorDateOfOptionality(), model.maturityDate(),model.cycleOfOptionality(), model.endOfMonthConvention());
            } else {
                times = riskFactorModel.times(model.objectCodeOfPrepaymentModel());
                times.removeIf(e -> e.compareTo(model.cycleAnchorDateOfOptionality())==-1);
            }
            events.addAll(EventFactory.createEvents(times,StringUtils.EventType_PP, model.currency(), new POF_PP_PAM(), new STF_PP_PAM(), model.businessDayConvention()));
            if(model.penaltyType()!='O') {
                events.addAll(EventFactory.createEvents(times,StringUtils.EventType_PY, model.currency(), new POF_PY_PAM(), new STF_PY_PAM(), model.businessDayConvention()));         
            }
        }
        // termination
        if (!CommonUtils.isNull(model.terminationDate())) {
            ContractEvent termination =
                                        EventFactory.createEvent(model.terminationDate(), StringUtils.EventType_TD, model.currency(), new POF_TD_PAM(), new STF_TD_PAM());
            events.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
            events.add(termination);
        }
        // add counterparty default risk-factor contingent events
        if(riskFactorModel.keys().contains(model.legalEntityIDCounterparty())) {
            events.addAll(EventFactory.createEvents(riskFactorModel.times(model.legalEntityIDCounterparty()),
                                             StringUtils.EventType_CD, model.currency(), new POF_CD_PAM(), new STF_CD_PAM()));
        }
        
        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.statusDate(), StringUtils.EventType_SD, model.currency(), null,
                                                                  null)) == -1);
        
        // return events
        return new ArrayList<ContractEvent>(events);
    }

    private static StateSpace initStateSpace(ContractModelProvider model) throws AttributeConversionException {
        StateSpace states = new StateSpace();

        // TODO: some attributes can be null
        states.contractRoleSign = ContractRoleConvention.roleSign(model.contractRole());
        states.lastEventTime = model.statusDate();
        if (!model.initialExchangeDate().isAfter(model.statusDate())) {
            states.nominalValue = model.notionalPrincipal();
            states.nominalRate = model.nominalInterestRate();
            states.nominalAccrued = model.accruedInterest();
            states.feeAccrued = model.feeAccrued();
            states.nominalScalingMultiplier = 1;
            states.interestScalingMultiplier = 1;
        }
        
        // return the initialized state space
        return states;
    }

}

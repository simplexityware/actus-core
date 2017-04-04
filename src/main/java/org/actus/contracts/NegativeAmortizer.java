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
        
        // determine maturity of the contract
        LocalDateTime maturity = model.maturityDate();
        if (CommonUtils.isNull(maturity)) {
            LocalDateTime lastEvent;
            if(model.cycleAnchorDateOfPrincipalRedemption().isBefore(model.statusDate())) {
                Set<LocalDateTime> previousEvents = ScheduleFactory.createSchedule(model.cycleAnchorDateOfPrincipalRedemption(),model.statusDate(),
                                            model.cycleOfPrincipalRedemption(), model.endOfMonthConvention());
                previousEvents.removeIf( d -> d.isBefore(model.statusDate().minus(CycleUtils.parsePeriod(model.cycleOfInterestPayment()))));
                previousEvents.remove(model.statusDate());
                lastEvent = previousEvents.toArray(new LocalDateTime[1])[0];
            } else {
                lastEvent = model.cycleAnchorDateOfPrincipalRedemption();   
            }
            Period cyclePeriod = CycleUtils.parsePeriod(model.cycleOfPrincipalRedemption());
            maturity = lastEvent.plus(cyclePeriod.multipliedBy((int) Math.ceil(model.notionalPrincipal()/model.nextPrincipalRedemptionPayment())));
        }        
        
        // create contract event schedules
        // analysis events
        events.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.currency(), new POF_AD_PAM(), new STF_AD_PAM()));
        // initial exchange
        events.add(EventFactory.createEvent(model.initialExchangeDate(), StringUtils.EventType_IED, model.currency(), new POF_IED_PAM(), new STF_IED_LAM()));
        // principal redemption
        events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.cycleAnchorDateOfPrincipalRedemption(), maturity,
                                                                            model.cycleOfPrincipalRedemption(), model.endOfMonthConvention()),
                                            StringUtils.EventType_PR, model.currency(), new POF_PR_LAM(), new STF_PR_LAM(), model.businessDayConvention()));     
        // purchase
        if (!CommonUtils.isNull(model.purchaseDate())) {
            events.add(EventFactory.createEvent(model.purchaseDate(), StringUtils.EventType_PRD, model.currency(), new POF_PRD_LAM(), new STF_PRD_LAM()));
        }
        // interest payment related
        if (!CommonUtils.isNull(model.cycleOfInterestPayment())) {
            // raw interest payment events
            Set<ContractEvent> interestEvents =
                                                        EventFactory.createEvents(ScheduleFactory.createSchedule(model.cycleAnchorDateOfInterestPayment(),
                                                                                                                 model.cycleAnchorDateOfPrincipalRedemption(),
                                                                                                                 model.cycleOfInterestPayment(),
                                                                                                                 model.endOfMonthConvention()),
                                                                                  StringUtils.EventType_IP, model.currency(), new POF_IP_LAM(), new STF_IP_PAM(), model.businessDayConvention());
            // remove last event that falls exactly on cycle anchor date of principal redemption
            interestEvents.remove(EventFactory.createEvent(model.cycleAnchorDateOfPrincipalRedemption(), StringUtils.EventType_IP,
                                                           model.currency(), new POF_AD_PAM(), new STF_AD_PAM(), model.businessDayConvention()));
            // adapt if interest capitalization set
            if (!CommonUtils.isNull(model.capitalizationEndDate())) {
                // for all events with time <= IPCED && type == "IP" do
                // change type to IPCI and payoff/state-trans functions
                ContractEvent capitalizationEnd = EventFactory.createEvent(model.capitalizationEndDate(), StringUtils.EventType_IPCI,
                                                                            model.currency(), new POF_IPCI_PAM(), new STF_IPCI_LAM(), model.businessDayConvention());
                interestEvents.forEach(e -> {
                    if (e.type().equals(StringUtils.EventType_IP) && e.compareTo(capitalizationEnd) == -1) {
                        e.type(StringUtils.EventType_IPCI);
                        e.fPayOff(new POF_IPCI_PAM());
                        e.fStateTrans(new STF_IPCI_LAM());
                    }
                });
                // also, remove any IP event exactly at IPCED and replace with an IPCI event
                interestEvents.remove(EventFactory.createEvent(model.capitalizationEndDate(), StringUtils.EventType_IP,
                                                                            model.currency(), new POF_AD_PAM(), new STF_AD_PAM(), model.businessDayConvention()));
                interestEvents.add(capitalizationEnd);
            }
            events.addAll(interestEvents);
        }
        // rate reset (if specified)
        if (!CommonUtils.isNull(model.cycleOfRateReset())) {            
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.cycleAnchorDateOfRateReset(), maturity,
                                                                            model.cycleOfRateReset(), model.endOfMonthConvention()),
                                            StringUtils.EventType_RR, model.currency(), new POF_RR_PAM(), new STF_RR_LAM(), model.businessDayConvention()));
        }
        // fees (if specified)
        if (!CommonUtils.isNull(model.cycleOfFee())) { 
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.cycleAnchorDateOfFee(), maturity,
                                                                            model.cycleOfFee(), model.endOfMonthConvention()),
                                             StringUtils.EventType_FP, model.currency(), new POF_FP_PAM(), new STF_FP_LAM(), model.businessDayConvention()));
        }
        // scaling (if specified)
        if (!CommonUtils.isNull(model.scalingEffect()) && (model.scalingEffect().contains("I") || model.scalingEffect().contains("N"))) { 
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.cycleAnchorDateOfScalingIndex(), maturity,
                                                                            model.cycleOfScalingIndex(), model.endOfMonthConvention()),
                                             StringUtils.EventType_SC, model.currency(), new POF_SC_PAM(), new STF_SC_LAM(), model.businessDayConvention()));
        }
        // interest calculation base (if specified)
        if (!CommonUtils.isNull(model.interestCalculationBase()) && model.interestCalculationBase().equals("NTL")) { 
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.cycleAnchorDateOfInterestCalculationBase(), maturity,
                                                                            model.cycleOfScalingIndex(), model.endOfMonthConvention()),
                                             StringUtils.EventType_IPCB, model.currency(), new POF_IPCB_LAM(), new STF_IPCB_LAM(), model.businessDayConvention()));
        }
        // optionality i.e. prepayment right (if specified)
        if (!(CommonUtils.isNull(model.cycleOfOptionality()) && CommonUtils.isNull(model.cycleAnchorDateOfOptionality()))) {
            Set<LocalDateTime> times;
            if(!CommonUtils.isNull(model.cycleOfOptionality())) {
                times = ScheduleFactory.createSchedule(model.cycleAnchorDateOfOptionality(), maturity,model.cycleOfOptionality(), model.endOfMonthConvention());
            } else {
                times = riskFactorModel.times(model.objectCodeOfPrepaymentModel());
                times.removeIf(e -> e.compareTo(model.cycleAnchorDateOfOptionality())==-1);
            }
            events.addAll(EventFactory.createEvents(times,StringUtils.EventType_PP, model.currency(), new POF_PP_PAM(), new STF_PP_LAM(), model.businessDayConvention()));
            if(model.penaltyType()!='O') {
                events.addAll(EventFactory.createEvents(times,StringUtils.EventType_PY, model.currency(), new POF_PY_PAM(), new STF_PY_LAM(), model.businessDayConvention()));         
            }
        }
        // termination
        if (!CommonUtils.isNull(model.terminationDate())) {
            ContractEvent termination =
                                        EventFactory.createEvent(model.terminationDate(), StringUtils.EventType_TD, model.currency(), new POF_TD_LAM(), new STF_TD_PAM());
            events.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
            events.add(termination);
        }
        // add counterparty default risk-factor contingent events
        if(riskFactorModel.keys().contains(model.legalEntityIDCounterparty())) {
            events.addAll(EventFactory.createEvents(riskFactorModel.times(model.legalEntityIDCounterparty()),
                                             StringUtils.EventType_CD, model.currency(), new POF_CD_PAM(), new STF_CD_LAM()));
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
        
        // init next principal redemption payment amount (cannot be null for NAM!)
        states.nextPrincipalRedemptionPayment = states.contractRoleSign * model.nextPrincipalRedemptionPayment();
        
        if (!model.initialExchangeDate().isAfter(model.statusDate())) {
            states.nominalValue = model.notionalPrincipal();
            states.nominalRate = model.nominalInterestRate();
            states.nominalAccrued = model.accruedInterest();
            states.feeAccrued = model.feeAccrued();
            states.nominalScalingMultiplier = 1;
            states.interestScalingMultiplier = 1;
            states.interestCalculationBase = states.contractRoleSign * ( (model.interestCalculationBase().equals("NT"))? model.notionalPrincipal() : model.interestCalculationBaseAmount() );
        }
        
        // return the initialized state space
        return states;
    }

}

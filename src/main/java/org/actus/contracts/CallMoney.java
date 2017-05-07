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
import org.actus.functions.clm.POF_IED_CLM;
import org.actus.functions.pam.STF_IED_PAM;
import org.actus.functions.pam.POF_PR_PAM;
import org.actus.functions.pam.STF_PR_PAM;
import org.actus.functions.clm.POF_IP_CLM;
import org.actus.functions.clm.STF_IP_CLM;
import org.actus.functions.pam.POF_IPCI_PAM;
import org.actus.functions.pam.STF_IPCI_PAM;
import org.actus.functions.pam.POF_RR_PAM;
import org.actus.functions.pam.STF_RR_PAM;
import org.actus.functions.pam.POF_FP_PAM;
import org.actus.functions.pam.STF_FP_PAM;
import org.actus.functions.pam.POF_CD_PAM;
import org.actus.functions.pam.STF_CD_PAM;


import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents the Call Money payoff algorithm
 * 
 * @see <a href="http://www.projectactus.org/"></a>
 */
public final class CallMoney {

    public static ArrayList<ContractEvent> eval(Set<LocalDateTime> analysisTimes, 
                        		         ContractModelProvider model, 
                        		         RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        
        // determine maturity of the contract
        LocalDateTime maturity = model.maturityDate();
        if (CommonUtils.isNull(maturity)) {
            ArrayList<LocalDateTime> sortedTimes = new ArrayList<LocalDateTime>(analysisTimes);
            Collections.sort(sortedTimes);
            maturity = sortedTimes.get(0).plus(CycleUtils.parsePeriod(model.xDayNotice(),false));
        }
        
        // compute events
        ArrayList<ContractEvent> payoff = initEvents(analysisTimes,model,riskFactorModel,maturity);
        
        // initialize state space per status date
        StateSpace states = initStateSpace(model);
        
        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(payoff);

        // evaluate events
        payoff.forEach(e -> e.eval(states, model, riskFactorModel, model.dayCountConvention(), model.businessDayConvention()));
        
        // return all evaluated post-StatusDate events as the payoff
        return payoff;
    }
    
    private static ArrayList<ContractEvent> initEvents(Set<LocalDateTime> analysisTimes, ContractModelProvider model, RiskFactorModelProvider riskFactorModel, LocalDateTime maturity) throws AttributeConversionException {
        HashSet<ContractEvent> events = new HashSet<ContractEvent>();

        // create contract event schedules
        // analysis events
        events.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.currency(), new POF_AD_PAM(), new STF_AD_PAM()));
        // initial exchange
        events.add(EventFactory.createEvent(model.initialExchangeDate(), StringUtils.EventType_IED, model.currency(), new POF_IED_CLM(), new STF_IED_PAM()));
        // principal redemption
        events.add(EventFactory.createEvent(maturity, StringUtils.EventType_PR, model.currency(), new POF_PR_PAM(), new STF_PR_PAM()));
        // interest payment event
        events.add(EventFactory.createEvent(maturity, StringUtils.EventType_IP, model.currency(), new POF_IP_CLM(), new STF_IP_CLM()));
        // interest payment capitalization (if specified)
        if (!CommonUtils.isNull(model.cycleOfInterestPayment())) {
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.cycleAnchorDateOfInterestPayment(),
                                                                                                                maturity,
                                                                                                                 model.cycleOfInterestPayment(),
                                                                                                                 model.endOfMonthConvention()),
                                                                                  StringUtils.EventType_IPCI, model.currency(), new POF_IPCI_PAM(), new STF_IPCI_PAM(), model.businessDayConvention()));
        }
        // rate reset (if specified)
        if (!CommonUtils.isNull(model.cycleOfRateReset())) {            
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.cycleAnchorDateOfRateReset(), maturity,
                                                                                model.cycleOfRateReset(), model.endOfMonthConvention()),
                                                 StringUtils.EventType_RR, model.currency(), new POF_RR_PAM(), new STF_RR_PAM(), model.businessDayConvention()));
        }
        // fees (if specified)
        if (!CommonUtils.isNull(model.cycleOfFee())) { 
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.cycleAnchorDateOfFee(), maturity,
                                                model.cycleOfFee(), model.endOfMonthConvention()),
                                                StringUtils.EventType_FP, model.currency(), new POF_FP_PAM(), new STF_FP_PAM(), model.businessDayConvention()));
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
            states.nominalValue = states.contractRoleSign * model.notionalPrincipal();
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

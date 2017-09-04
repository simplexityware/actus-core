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

    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        
        // determine maturity of the contract
        LocalDateTime maturity = model.getAs("MaturityDate");
        if (CommonUtils.isNull(maturity)) {
            ArrayList<LocalDateTime> sortedTimes = new ArrayList<LocalDateTime>(analysisTimes);
            Collections.sort(sortedTimes);
            maturity = sortedTimes.get(0).plus(CycleUtils.parsePeriod(model.getAs("XDayNotice"),false));
        }
        
        // compute events
        ArrayList<ContractEvent> payoff = initEvents(analysisTimes,model,riskFactorModel,maturity);
        
        // initialize state space per status date
        StateSpace states = initStateSpace(model);
        
        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(payoff);

        // evaluate events
        payoff.forEach(e -> e.eval(states, model, riskFactorModel, model.getAs("DayCountConvention"), model.getAs("BusinessDayConvention")));
        
        // return all evaluated post-StatusDate events as the payoff
        return payoff;
    }
    
    private static ArrayList<ContractEvent> initEvents(Set<LocalDateTime> analysisTimes, ContractModelProvider model, RiskFactorModelProvider riskFactorModel, LocalDateTime maturity) throws AttributeConversionException {
        HashSet<ContractEvent> events = new HashSet<ContractEvent>();

        // create contract event schedules
        // analysis events
        events.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
        // initial exchange
        events.add(EventFactory.createEvent(model.getAs("InitialExchangeDate"), StringUtils.EventType_IED, model.getAs("Currency"), new POF_IED_CLM(), new STF_IED_PAM()));
        // principal redemption
        events.add(EventFactory.createEvent(maturity, StringUtils.EventType_PR, model.getAs("Currency"), new POF_PR_PAM(), new STF_PR_PAM()));
        // interest payment event
        events.add(EventFactory.createEvent(maturity, StringUtils.EventType_IP, model.getAs("Currency"), new POF_IP_CLM(), new STF_IP_CLM()));
        // interest payment capitalization (if specified)
        if (!CommonUtils.isNull(model.getAs("CycleOfInterestPayment"))) {
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfInterestPayment"),
                                                                                                                maturity,
                                                                                                                 model.getAs("CycleOfInterestPayment"),
                                                                                                                 model.getAs("EndOfMonthConvention")),
                                                                                  StringUtils.EventType_IPCI, model.getAs("Currency"), new POF_IPCI_PAM(), new STF_IPCI_PAM(), model.getAs("BusinessDayConvention")));
        }
        // rate reset (if specified)
        if (!CommonUtils.isNull(model.getAs("CycleOfRateReset"))) {            
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfRateReset"), maturity,
                                                                                model.getAs("CycleOfRateReset"), model.getAs("EndOfMonthConvention")),
                                                 StringUtils.EventType_RR, model.getAs("Currency"), new POF_RR_PAM(), new STF_RR_PAM(), model.getAs("BusinessDayConvention")));
        }
        // fees (if specified)
        if (!CommonUtils.isNull(model.getAs("CycleOfFee"))) { 
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfFee"), maturity,
                                                model.getAs("CycleOfFee"), model.getAs("EndOfMonthConvention")),
                                                StringUtils.EventType_FP, model.getAs("Currency"), new POF_FP_PAM(), new STF_FP_PAM(), model.getAs("BusinessDayConvention")));
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
        if (!model.<LocalDateTime>getAs("InitialExchangeDate").isAfter(model.getAs("StatusDate"))) {
            states.nominalValue = states.contractRoleSign * model.<Double>getAs("NotionalPrincipal");
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

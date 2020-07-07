/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.lam;

import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.functions.StateTransitionFunction;
import org.actus.types.ContractRole;
import org.actus.util.CommonUtils;
import org.actus.states.StateSpace;
import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;

import java.time.LocalDateTime;

public final class STF_IED_LAM implements StateTransitionFunction {
    
    @Override
    public StateSpace eval(LocalDateTime time, StateSpace states,
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        StateSpace postEventStates = new StateSpace();
        
        // update state space
        states.nominalInterestRate = model.<Double>getAs("NominalInterestRate");
        states.statusDate = time;
        states.interestCalculationBaseAmount = ContractRoleConvention.roleSign(ContractRole.valueOf(model.getAs("ContractRole")))*
            ( (CommonUtils.isNull(model.getAs("InterestCalculationBase")) || model.getAs("InterestCalculationBase").equals("NT"))? 
            model.<Double>getAs("NotionalPrincipal") : model.<Double>getAs("InterestCalculationBaseAmount") );
        

        // if cycle anchor date of interest payment prior to IED, then update nominal accrued accordingly
        if(!CommonUtils.isNull(model.getAs("CycleAnchorDateOfInterestPayment")) &&
            model.<LocalDateTime>getAs("CycleAnchorDateOfInterestPayment").isBefore(model.getAs("InitialExchangeDate"))) {
            states.accruedInterest += states.interestCalculationBaseAmount *states.nominalInterestRate *dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(model.<LocalDateTime>getAs("CycleAnchorDateOfInterestPayment")),timeAdjuster.shiftCalcTime(time));
        }

        // copy post-event-states
        postEventStates.notionalPrincipal = states.notionalPrincipal;
        postEventStates.nominalInterestRate = states.nominalInterestRate;
        
        // return post-event-states
        return postEventStates;
        }
    
}

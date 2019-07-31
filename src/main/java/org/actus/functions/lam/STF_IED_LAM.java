/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.lam;

import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.functions.StateTransitionFunction;
import org.actus.util.CommonUtils;
import org.actus.states.StateSpace;
import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;

import java.time.LocalDateTime;

public final class STF_IED_LAM implements StateTransitionFunction {
    
    @Override
    public double[] eval(LocalDateTime time, StateSpace states, 
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        double[] postEventStates = new double[8];
        
        // update state space
        states.timeFromLastEvent = dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.lastEventTime), timeAdjuster.shiftCalcTime(time));
        states.nominalValue = ContractRoleConvention.roleSign(model.getAs("ContractRole"))*model.<Double>getAs("NotionalPrincipal");
        states.nominalRate = model.<Double>getAs("NominalInterestRate");
        states.lastEventTime = time;
        states.interestCalculationBase = ContractRoleConvention.roleSign(model.getAs("ContractRole"))*
            ( (CommonUtils.isNull(model.getAs("InterestCalculationBase")) || model.getAs("InterestCalculationBase").equals("NT"))? 
            model.<Double>getAs("NotionalPrincipal") : model.<Double>getAs("InterestCalculationBaseAmount") );
        

        // if cycle anchor date of interest payment prior to IED, then update nominal accrued accordingly
        if(!CommonUtils.isNull(model.getAs("CycleAnchorDateOfInterestPayment")) &&
            model.<LocalDateTime>getAs("CycleAnchorDateOfInterestPayment").isBefore(model.getAs("InitialExchangeDate"))) {
            states.nominalAccrued += states.interestCalculationBase*states.nominalRate*dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(model.<LocalDateTime>getAs("CycleAnchorDateOfInterestPayment")),timeAdjuster.shiftCalcTime(time));
        }

        // copy post-event-states
        postEventStates[0] = states.timeFromLastEvent;
        postEventStates[1] = states.nominalValue;
        postEventStates[3] = states.nominalRate;
        
        // return post-event-states
        return postEventStates;
        }
    
}

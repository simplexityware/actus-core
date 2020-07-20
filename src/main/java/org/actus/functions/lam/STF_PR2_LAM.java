/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.lam;

import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;
import org.actus.types.ContractRole;

import java.time.LocalDateTime;

public final class STF_PR2_LAM implements StateTransitionFunction {
    
    @Override
    public StateSpace eval(LocalDateTime time, StateSpace states,
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        StateSpace postEventStates = new StateSpace();
        double redemption = states.nextPrincipalRedemptionPayment - ContractRoleConvention.roleSign(model.getAs("ContractRole")) * Math.max(0, Math.abs(states.nextPrincipalRedemptionPayment) - Math.abs(states.notionalPrincipal));

        // update state space
        double timeFromLastEvent = dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.statusDate), timeAdjuster.shiftCalcTime(time));
        states.accruedInterest += states.nominalInterestRate * states.interestCalculationBaseAmount * timeFromLastEvent;
        states.feeAccrued += model.<Double>getAs("FeeRate") * states.notionalPrincipal * timeFromLastEvent;
        states.notionalPrincipal -= redemption;
        states.interestCalculationBaseAmount = states.notionalPrincipal;
        states.statusDate = time;
        
        // copy post-event-states
        postEventStates.notionalPrincipal = states.notionalPrincipal;
        postEventStates.accruedInterest = states.accruedInterest;
        postEventStates.nominalInterestRate = states.nominalInterestRate;
        postEventStates.feeAccrued = states.feeAccrued;
        
        // return post-event-states
        return postEventStates;
        }
    
}

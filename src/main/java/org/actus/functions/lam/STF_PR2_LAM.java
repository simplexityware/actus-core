/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.lam;

import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;

import java.time.LocalDateTime;

public final class STF_PR2_LAM implements StateTransitionFunction {
    
    @Override
    public double[] eval(LocalDateTime time, StateSpace states, 
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        double[] postEventStates = new double[8];
        
        // update state space
        states.timeFromLastEvent = dayCounter.dayCountFraction(states.lastEventTime, time);
        states.nominalAccrued += states.nominalRate * states.interestCalculationBase * states.timeFromLastEvent;
        states.feeAccrued += model.<Double>getAs("FeeRate") * states.nominalValue * states.timeFromLastEvent;
        states.nominalValue -= states.nextPrincipalRedemptionPayment - Math.max(0, states.nextPrincipalRedemptionPayment - states.contractRoleSign * states.nominalValue);
        states.interestCalculationBase = states.nominalValue;
        states.lastEventTime = time;
        
        // copy post-event-states
        postEventStates[0] = states.timeFromLastEvent;
        postEventStates[1] = states.nominalValue;
        postEventStates[2] = states.nominalAccrued;
        postEventStates[3] = states.nominalRate;
        postEventStates[6] = states.probabilityOfDefault;
        postEventStates[7] = states.feeAccrued;
        
        // return post-event-states
        return postEventStates;
        }
    
}

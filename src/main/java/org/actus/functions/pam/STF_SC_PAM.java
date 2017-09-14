/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.pam;

import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;
import org.actus.externals.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;

import java.time.LocalDateTime;

public final class STF_SC_PAM implements StateTransitionFunction {
    
    @Override
    public double[] eval(LocalDateTime time, StateSpace states, 
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        double[] postEventStates = new double[8];
        
        // update state space
        states.timeFromLastEvent = dayCounter.dayCountFraction(states.lastEventTime, time);
        states.nominalAccrued += states.nominalRate * states.nominalValue * states.timeFromLastEvent;
        states.feeAccrued += model.<Double>getAs("FeeRate") * states.nominalValue * states.timeFromLastEvent;
        if(model.<String>getAs("ScalingEffect").contains("I")) {
            states.interestScalingMultiplier = riskFactorModel.stateAt(model.getAs("MarketObjectCodeOfScalingIndex"),time,states,model);
        }
        if(model.<String>getAs("ScalingEffect").contains("N")) {
            states.nominalScalingMultiplier = riskFactorModel.stateAt(model.getAs("MarketObjectCodeOfScalingIndex"),time,states,model);
        }
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

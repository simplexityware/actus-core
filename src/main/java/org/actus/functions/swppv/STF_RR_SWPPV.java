/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.swppv;

import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;
import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;

import java.time.LocalDateTime;

public final class STF_RR_SWPPV implements StateTransitionFunction {
    
    @Override
    public double[] eval(LocalDateTime time, StateSpace states, 
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        double[] postEventStates = new double[8];
        
        // update state space
        states.timeFromLastEvent = dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.lastEventTime), timeAdjuster.shiftCalcTime(time));
        states.nominalAccrued += (model.<Double>getAs("NominalInterestRate") - states.nominalRate) * states.nominalValue * states.timeFromLastEvent;
        states.nominalAccruedFix += model.<Double>getAs("NominalInterestRate") * states.nominalValue * states.timeFromLastEvent;
        states.nominalAccruedFloat += (-1) * states.nominalRate * states.nominalValue * states.timeFromLastEvent;
        states.nominalRate = riskFactorModel.stateAt(model.getAs("MarketObjectCodeOfRateReset"),time,states,model) * model.<Double>getAs("RateMultiplier") + model.<Double>getAs("RateSpread");
        states.lastEventTime = time;
        
        // copy post-event-states
        postEventStates[0] = states.timeFromLastEvent;
        postEventStates[1] = states.nominalValue;
        postEventStates[2] = states.nominalAccruedFloat;
        postEventStates[3] = states.nominalRate;
        
        // return post-event-states
        return postEventStates;
        }
    
}

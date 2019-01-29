/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.ump;

import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;

import java.time.LocalDateTime;

public final class STF_PR_UMP implements StateTransitionFunction {
    double payoff=0.0;

    public STF_PR_UMP(double eventPayoff) {
        payoff=eventPayoff;
    }

    @Override
    public double[] eval(LocalDateTime time, StateSpace states, 
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        double[] postEventStates = new double[8];
        
        // update state space
        states.timeFromLastEvent = dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.lastEventTime), timeAdjuster.shiftCalcTime(time));
        states.nominalAccrued += states.nominalRate * states.nominalValue * states.timeFromLastEvent;
        states.nominalValue -= ContractRoleConvention.roleSign(model.getAs("ContractRole"))*payoff;
        states.feeAccrued += model.<Double>getAs("FeeRate") * states.nominalValue * states.timeFromLastEvent;
        states.lastEventTime = time;
        
        // copy post-event-states
        postEventStates[0] = states.timeFromLastEvent;
        postEventStates[1] = states.nominalValue;
        postEventStates[2] = states.nominalAccrued;
        postEventStates[3] = states.nominalRate;
        postEventStates[7] = states.feeAccrued;

        // return post-event-states
        return postEventStates;
        }
    
}

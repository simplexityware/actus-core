/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.mrgng;

import org.actus.attributes.ContractModel;
import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;

import java.time.LocalDateTime;

public final class STF_MR_MRGNG implements StateTransitionFunction {
    
    @Override
    public double[] eval(LocalDateTime time, StateSpace states, 
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        double[] postEventStates = new double[8];

        // compute delta market value of child contract
        ContractModel child = model.<ContractModel>getAs("Child");
        double marketValueObserved = riskFactorModel.stateAt(child.getAs("MarketObjectCode"),time,states,child);
        double deltaValue = ContractRoleConvention.roleSign(child.getAs("ContractRole")) * (marketValueObserved - states.marketValueObserved);

        // update state space
        states.timeFromLastEvent = dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.lastEventTime), timeAdjuster.shiftCalcTime(time));
        states.marketValueObserved = marketValueObserved;
        states.variationMargin += deltaValue;
        states.lastEventTime = time;
        
        // copy post-event-states
        postEventStates[0] = states.timeFromLastEvent;
        // TODO: add post event variation margin, marketvalueobserved states

        // return post-event-states
        return postEventStates;
        }
    
}

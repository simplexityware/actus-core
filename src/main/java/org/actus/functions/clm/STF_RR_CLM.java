/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.clm;

import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;

import java.time.LocalDateTime;

public final class STF_RR_CLM implements StateTransitionFunction {
    
    @Override
    public StateSpace eval(LocalDateTime time, StateSpace states,
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        StateSpace postEventStates = new StateSpace();

        // update state space
        double timeFromLastEvent = dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.statusDate), timeAdjuster.shiftCalcTime(time));
        states.accruedInterest += states.nominalInterestRate * states.notionalPrincipal * timeFromLastEvent;
        states.nominalInterestRate = riskFactorModel.stateAt(model.getAs("MarketObjectCodeOfRateReset"), time, states, model)
                * model.<Double>getAs("RateMultiplier") + model.<Double>getAs("RateSpread");
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

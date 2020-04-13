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
    public StateSpace eval(LocalDateTime time, StateSpace states,
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        StateSpace postEventStates = new StateSpace();
        
        // update state space
        double timeFromLastEvent = dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.statusDate), timeAdjuster.shiftCalcTime(time));
        states.accruedInterest += (model.<Double>getAs("NominalInterestRate") - states.nominalInterestRate) * states.notionalPrincipal * timeFromLastEvent;
        states.accruedInterest += model.<Double>getAs("NominalInterestRate") * states.notionalPrincipal * timeFromLastEvent;
        states.accruedInterest2 += (-1) * states.nominalInterestRate * states.notionalPrincipal * timeFromLastEvent;
        states.nominalInterestRate = riskFactorModel.stateAt(model.getAs("MarketObjectCodeOfRateReset"),time,states,model) * model.<Double>getAs("RateMultiplier") + model.<Double>getAs("RateSpread");
        states.statusDate = time;
        
        // copy post-event-states
        postEventStates.notionalPrincipal = states.notionalPrincipal;
        postEventStates.accruedInterest = states.accruedInterest2;
        postEventStates.nominalInterestRate = states.nominalInterestRate;
        
        // return post-event-states
        return postEventStates;
        }
    
}

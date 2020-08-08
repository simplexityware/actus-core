/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.pam;

import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;
import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;

import java.time.LocalDateTime;

public final class STF_RR_PAM implements StateTransitionFunction {
    
    @Override
    public StateSpace eval(LocalDateTime time, StateSpace states,
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        // compute new rate
        double rate = riskFactorModel.stateAt(model.getAs("MarketObjectCodeOfRateReset"), time, states, model)
                * model.<Double>getAs("RateMultiplier") + model.<Double>getAs("RateSpread");
        double deltaRate = rate - states.nominalInterestRate;

        // apply period cap/floor
        deltaRate = Math.min(Math.max(deltaRate,(-1)*model.<Double>getAs("PeriodFloor")),model.<Double>getAs("LifeCap"));
        rate = states.nominalInterestRate + deltaRate;

        // apply life cap/floor
        rate = Math.min(Math.max(rate,model.getAs("LifeFloor")),model.getAs("LifeCap"));

        // update state space
        double timeFromLastEvent = dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.statusDate), timeAdjuster.shiftCalcTime(time));
        states.accruedInterest += states.nominalInterestRate * states.notionalPrincipal * timeFromLastEvent;
        states.nominalInterestRate = rate;
        states.statusDate = time;

        // return post-event-states
        return StateSpace.copyStateSpace(states);
    }
    
}

/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.ann;

import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;
import org.actus.util.AnnuityUtils;
import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;

import java.time.LocalDateTime;

public final class STF_RR_ANN implements StateTransitionFunction {
    
    @Override
    public double[] eval(LocalDateTime time, StateSpace states, 
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        double[] postEventStates = new double[8];

        // compute new rate
        double rate = riskFactorModel.stateAt(model.getAs("MarketObjectCodeOfRateReset"), time, states, model)
                * model.<Double>getAs("RateMultiplier") + model.<Double>getAs("RateSpread");
        double deltaRate = rate - states.nominalRate;

        // apply period cap/floor
        deltaRate = Math.min(Math.max(deltaRate,(-1)*model.<Double>getAs("PeriodFloor")),model.<Double>getAs("LifeCap"));
        rate = states.nominalRate+deltaRate;

        // apply life cap/floor
        rate = Math.min(Math.max(rate,model.getAs("LifeFloor")),model.getAs("LifeCap"));

        // update state space
        states.timeFromLastEvent = dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.lastEventTime), timeAdjuster.shiftCalcTime(time));
        states.nominalAccrued += states.nominalRate * states.interestCalculationBase * states.timeFromLastEvent;
        states.feeAccrued += model.<Double>getAs("FeeRate") * states.nominalValue * states.timeFromLastEvent;
        states.nominalRate = rate;
        states.lastEventTime = time;
        states.nextPrincipalRedemptionPayment = ContractRoleConvention.roleSign(model.getAs("ContractRole"))*AnnuityUtils.annuityPayment(model, states.nominalValue, states.nominalAccrued, states.nominalRate);
        
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

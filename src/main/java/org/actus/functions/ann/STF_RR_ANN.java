/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.ann;

import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;
import org.actus.types.ContractRole;
import org.actus.util.AnnuityUtils;
import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;

import java.time.LocalDateTime;

public final class STF_RR_ANN implements StateTransitionFunction {
    
    @Override
    public StateSpace eval(LocalDateTime time, StateSpace states,
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        double timeFromLastEvent = dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.statusDate), timeAdjuster.shiftCalcTime(time));
        states.accruedInterest += states.nominalInterestRate * states.interestCalculationBaseAmount * timeFromLastEvent;
        states.feeAccrued += model.<Double>getAs("FeeRate") * states.notionalPrincipal * timeFromLastEvent;

        // compute new rate
        double rate =
                (riskFactorModel.stateAt(model.getAs("MarketObjectCodeOfRateReset"), time, states, model) * model.<Double>getAs("RateMultiplier"))
                + model.<Double>getAs("RateSpread")
                - states.nominalInterestRate;
        double deltaRate = Math.min(Math.max(rate,model.getAs("PeriodFloor")),model.getAs("PeriodCap"));
        states.nominalInterestRate = Math.min(Math.max(states.nominalInterestRate + deltaRate, model.getAs("LifeFloor")),model.getAs("LifeCap"));

        states.nextPrincipalRedemptionPayment = AnnuityUtils.annuityPayment(model, states.notionalPrincipal, states.accruedInterest, states.nominalInterestRate);

        states.statusDate = time;

        // return post-event-states
        return StateSpace.copyStateSpace(states);
    }
    
}

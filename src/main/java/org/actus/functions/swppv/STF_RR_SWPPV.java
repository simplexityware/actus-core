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
import org.actus.types.DeliverySettlement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class STF_RR_SWPPV implements StateTransitionFunction {
    
    @Override
    public StateSpace eval(LocalDateTime time, StateSpace states,
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        // update state space
        double timeFromLastEvent = dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.statusDate), timeAdjuster.shiftCalcTime(time));
        states.accruedInterest += ((model.getAs("DeliverySettlement")==DeliverySettlement.D)? model.<Double>getAs("NominalInterestRate") : (model.<Double>getAs("NominalInterestRate") - states.nominalInterestRate)) * states.notionalPrincipal * timeFromLastEvent;
        states.accruedInterest2 += (-1) * states.nominalInterestRate * states.notionalPrincipal * timeFromLastEvent;
        states.nominalInterestRate = BigDecimal.valueOf(riskFactorModel.stateAt(model.getAs("MarketObjectCodeOfRateReset"),time,states,model) * model.<Double>getAs("RateMultiplier")).add( BigDecimal.valueOf(model.<Double>getAs("RateSpread"))).doubleValue();
        states.statusDate = time;
        // return post-event-states 
        return StateSpace.copyStateSpace(states);
    }
    
}

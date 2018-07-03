/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.nam;

import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.PayOffFunction;
import org.actus.states.StateSpace;

import java.time.LocalDateTime;

public final class POF_IP_NAM implements PayOffFunction {
    
    @Override
        public double eval(LocalDateTime time, StateSpace states, 
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {

        // compute interest payment (excluding capitalization)
        // Note: for NAM, interest accrued in excess to PRNXT is capitalized
        double timeFromLastEvent = dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.lastEventTime), timeAdjuster.shiftCalcTime(time));
        double accrued = states.nominalAccrued + states.interestCalculationBase * states.timeFromLastEvent * states.nominalRate;
        double capitalization = states.contractRoleSign * Math.max(0,Math.abs(accrued)-Math.abs(states.nextPrincipalRedemptionPayment));
        double interest = accrued - capitalization;

        // return interest payoff
        return (1 - states.probabilityOfDefault) * states.interestScalingMultiplier * interest;
    }
}

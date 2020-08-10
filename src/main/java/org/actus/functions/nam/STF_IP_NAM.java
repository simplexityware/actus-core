/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.nam;

import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;
import org.actus.types.ContractRole;

import java.time.LocalDateTime;

public final class STF_IP_NAM implements StateTransitionFunction {

    @Override
    public StateSpace eval(LocalDateTime time, StateSpace states,
                         ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        // compute interest payment and capitalization
        // Note: for NAM, interest accrued in excess to PRNXT is capitalized
        double timeFromLastEvent = dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.statusDate), timeAdjuster.shiftCalcTime(time));
        double accrued = states.accruedInterest + states.interestCalculationBaseAmount * timeFromLastEvent * states.nominalInterestRate;
        double capitalization = ContractRoleConvention.roleSign(model.getAs("ContractRole"))*Math.max(0,Math.abs(accrued)-Math.abs(states.nextPrincipalRedemptionPayment));
        double interest = accrued - capitalization;

        // update state space
        states.accruedInterest = 0.0;
        states.notionalPrincipal += capitalization;
        states.feeAccrued += model.<Double>getAs("FeeRate") * states.notionalPrincipal * timeFromLastEvent;
        states.statusDate = time;

        // return post-event-states
        return StateSpace.copyStateSpace(states);
    }

}
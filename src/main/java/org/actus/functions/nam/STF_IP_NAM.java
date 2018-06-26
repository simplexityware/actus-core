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
import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;

import java.time.LocalDateTime;

public final class STF_IP_NAM implements StateTransitionFunction {

    @Override
    public double[] eval(LocalDateTime time, StateSpace states,
                         ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        double[] postEventStates = new double[8];

        // compute interest payment and capitalization
        // Note: for NAM, interest accrued in excess to PRNXT is capitalized
        double timeFromLastEvent = dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.lastEventTime), timeAdjuster.shiftCalcTime(time));
        double accrued = states.nominalAccrued + states.interestCalculationBase * states.timeFromLastEvent * states.nominalRate;
        double capitalization = states.contractRoleSign * Math.max(0,Math.abs(accrued-states.nextPrincipalRedemptionPayment));
        double interest = accrued - capitalization;

        // update state space
        states.timeFromLastEvent = timeFromLastEvent;
        states.lastInterestPayment = interest;
        states.nominalAccrued = 0.0;
        states.nominalValue += capitalization;
        states.feeAccrued += model.<Double>getAs("FeeRate") * states.nominalValue * states.timeFromLastEvent;
        states.lastEventTime = time;

        // copy post-event-states
        postEventStates[0] = states.timeFromLastEvent;
        postEventStates[1] = states.nominalValue;
        postEventStates[3] = states.nominalRate;
        postEventStates[6] = states.probabilityOfDefault;
        postEventStates[7] = states.feeAccrued;

        // return post-event-states
        return postEventStates;
    }

}
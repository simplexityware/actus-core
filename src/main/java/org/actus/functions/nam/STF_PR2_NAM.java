/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.nam;

import java.time.LocalDateTime;

import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;

public final class STF_PR2_NAM implements StateTransitionFunction {

	@Override
	public double[] eval(LocalDateTime time, StateSpace states, ContractModelProvider model,
			RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
		double[] postEventStates = new double[8];
		double principalRedemption =  states.nextPrincipalRedemptionPayment - states.lastInterestPayment;
		principalRedemption = principalRedemption - states.contractRoleSign * Math.max(0, Math.abs(principalRedemption) - Math.abs(states.nominalValue));
		
		// update state space
		states.timeFromLastEvent = dayCounter.dayCountFraction(states.lastEventTime, time);
		states.nominalAccrued += states.nominalRate * states.interestCalculationBase * states.timeFromLastEvent;
		states.feeAccrued += model.<Double>getAs("FeeRate") * states.nominalValue * states.timeFromLastEvent;
		states.nominalValue -= principalRedemption;
		states.interestCalculationBase = states.nominalValue;
		states.lastEventTime = time;

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

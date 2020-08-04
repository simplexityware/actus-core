/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.nam;

import java.time.LocalDateTime;

import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;
import org.actus.types.ContractRole;

public final class STF_PR2_NAM implements StateTransitionFunction {

	@Override
	public StateSpace eval(LocalDateTime time, StateSpace states, ContractModelProvider model,
			RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
		StateSpace postEventStates = new StateSpace();
	
		// update state space
		double timeFromLastEvent = dayCounter.dayCountFraction(states.statusDate, time);
		states.accruedInterest += states.nominalInterestRate * states.interestCalculationBaseAmount * timeFromLastEvent;
		states.feeAccrued += model.<Double>getAs("FeeRate") * states.notionalPrincipal * timeFromLastEvent;
		
		double principalRedemption =  states.nextPrincipalRedemptionPayment - states.accruedInterest;
		principalRedemption = principalRedemption - ContractRoleConvention.roleSign(model.getAs("ContractRole"))*Math.max(0, Math.abs(principalRedemption) - Math.abs(states.notionalPrincipal));
		
		states.notionalPrincipal -= principalRedemption;
		states.interestCalculationBaseAmount = states.notionalPrincipal;
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

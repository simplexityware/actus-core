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

public class STF_PRD_ANN implements StateTransitionFunction {

	@Override
	public StateSpace eval(LocalDateTime time, StateSpace states, ContractModelProvider model,
			RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
		StateSpace postEventStates = new StateSpace();

		// update state space
		double timeFromLastEvent = dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.statusDate),
				timeAdjuster.shiftCalcTime(time));
		states.accruedInterest += states.nominalInterestRate * states.interestCalculationBaseAmount * timeFromLastEvent;
		states.feeAccrued += model.<Double>getAs("FeeRate") * states.notionalPrincipal * timeFromLastEvent;
		states.statusDate = time;
		states.nextPrincipalRedemptionPayment = ContractRoleConvention.roleSign(model.getAs("ContractRole"))*AnnuityUtils.annuityPayment(model, states.notionalPrincipal, states.accruedInterest, states.nominalInterestRate);

		// copy post-event-states
		postEventStates.notionalPrincipal = states.notionalPrincipal;
		postEventStates.accruedInterest = states.accruedInterest;
		postEventStates.nominalInterestRate = states.nominalInterestRate;
		postEventStates.feeAccrued = states.feeAccrued;

		// return post-event-states
		return postEventStates;
	}

}

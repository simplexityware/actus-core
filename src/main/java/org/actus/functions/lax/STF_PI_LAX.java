package org.actus.functions.lax;

import java.time.LocalDateTime;

import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;

public class STF_PI_LAX implements StateTransitionFunction {

	private double prPayment = 0;

	public STF_PI_LAX(Double prPayment) {
		this.prPayment = prPayment;
	}

	@Override
	public StateSpace eval(LocalDateTime time, StateSpace states, ContractModelProvider model,
			RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
		StateSpace postEventStates = new StateSpace();
		double role = ContractRoleConvention.roleSign(model.getAs("ContractRole"));
		double redemption = role*prPayment - role * Math.max(0, Math.abs(prPayment) - Math.abs(states.notionalPrincipal));
		// update state space
		double timeFromLastEvent = dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.statusDate),
				timeAdjuster.shiftCalcTime(time));
		states.accruedInterest += states.nominalInterestRate * states.interestCalculationBaseAmount * timeFromLastEvent;
		states.feeAccrued += model.<Double>getAs("FeeRate") * states.notionalPrincipal * timeFromLastEvent;
		states.notionalPrincipal += redemption;
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

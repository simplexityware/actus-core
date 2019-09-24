package org.actus.functions.lax;

import java.time.LocalDateTime;

import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;

public class STF_PI_LAX2 implements StateTransitionFunction {

	private double prPayment = 0;

	public STF_PI_LAX2(Double prPayment) {
		this.prPayment = prPayment;
	}

	@Override
	public double[] eval(LocalDateTime time, StateSpace states, ContractModelProvider model,
			RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
		double[] postEventStates = new double[8];
		double role = ContractRoleConvention.roleSign(model.getAs("ContractRole"));
		double redemption = role * prPayment - role * Math.max(0, Math.abs(prPayment) - Math.abs(states.nominalValue));
		// update state space
		states.timeFromLastEvent = dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.lastEventTime),
				timeAdjuster.shiftCalcTime(time));
		states.nominalAccrued += states.nominalRate * states.interestCalculationBase * states.timeFromLastEvent;
		states.feeAccrued += model.<Double>getAs("FeeRate") * states.nominalValue * states.timeFromLastEvent;
		states.nominalValue += redemption;
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

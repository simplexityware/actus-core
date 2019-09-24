package org.actus.functions.lax;

import java.time.LocalDateTime;

import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;

public final class STF_RR_LAX implements StateTransitionFunction {
	double scheduledRate=0;
	
	public STF_RR_LAX(double rate) {
		this.scheduledRate=rate;
	}
	
	@Override
	public double[] eval(LocalDateTime time, StateSpace states, ContractModelProvider model,
			RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
		double[] postEventStates = new double[8];

				// compute new rate
				double rate = riskFactorModel.stateAt(model.getAs("MarketObjectCodeOfRateReset"), time, states, model)
						* model.<Double>getAs("RateMultiplier") + model.<Double>getAs("RateSpread") + scheduledRate;
				double deltaRate = rate - states.nominalRate;
				
				// apply period cap/floor
				deltaRate = Math.min(Math.max(deltaRate, (-1) * model.<Double>getAs("PeriodFloor")),
						model.<Double>getAs("LifeCap"));
				rate = states.nominalRate + deltaRate;

				// apply life cap/floor
				rate = Math.min(Math.max(rate, model.getAs("LifeFloor")), model.getAs("LifeCap"));

				// update state space
				states.timeFromLastEvent = dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.lastEventTime),
						timeAdjuster.shiftCalcTime(time));
				states.nominalAccrued += states.nominalRate * states.interestCalculationBase * states.timeFromLastEvent;
				states.feeAccrued += model.<Double>getAs("FeeRate") * states.nominalValue * states.timeFromLastEvent;
				states.nominalRate = rate;
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


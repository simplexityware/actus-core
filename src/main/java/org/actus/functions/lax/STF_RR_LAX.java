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
	public StateSpace eval(LocalDateTime time, StateSpace states, ContractModelProvider model,
			RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
		StateSpace postEventStates = new StateSpace();

				// compute new rate
				double rate = riskFactorModel.stateAt(model.getAs("MarketObjectCodeOfRateReset"), time, states, model)
						* model.<Double>getAs("RateMultiplier") + model.<Double>getAs("RateSpread") + scheduledRate;
				double deltaRate = rate - states.nominalInterestRate;
				
				// apply period cap/floor
				deltaRate = Math.min(Math.max(deltaRate, (-1) * model.<Double>getAs("PeriodFloor")),
						model.<Double>getAs("LifeCap"));
				rate = states.nominalInterestRate + deltaRate;

				// apply life cap/floor
				rate = Math.min(Math.max(rate, model.getAs("LifeFloor")), model.getAs("LifeCap"));

				// update state space
				double timeFromLastEvent = dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.statusDate),
						timeAdjuster.shiftCalcTime(time));
				states.accruedInterest += states.nominalInterestRate * states.interestCalculationBaseAmount * timeFromLastEvent;
				states.feeAccrued += model.<Double>getAs("FeeRate") * states.notionalPrincipal * timeFromLastEvent;
				states.nominalInterestRate = rate;
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


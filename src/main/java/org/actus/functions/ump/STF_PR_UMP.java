/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.ump;

import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;

import java.time.LocalDateTime;

public final class STF_PR_UMP implements StateTransitionFunction {
    double payoff=0.0;

    public STF_PR_UMP(double eventPayoff) {
        payoff=eventPayoff;
    }

    @Override
    public StateSpace eval(LocalDateTime time, StateSpace states,
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        StateSpace postEventStates = new StateSpace();
        
        // update state space
        double timeFromLastEvent = dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.statusDate), timeAdjuster.shiftCalcTime(time));
        states.accruedInterest += states.nominalInterestRate * states.notionalPrincipal * timeFromLastEvent;
        states.notionalPrincipal -= ContractRoleConvention.roleSign(model.getAs("ContractRole"))*payoff;
        states.feeAccrued += model.<Double>getAs("FeeRate") * states.notionalPrincipal * timeFromLastEvent;
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

/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.swppv;

import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;
import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;

import java.time.LocalDateTime;

public final class STF_TD_SWPPV implements StateTransitionFunction {
    
    @Override
    public StateSpace eval(LocalDateTime time, StateSpace states,
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        StateSpace postEventStates = new StateSpace();
        
        // update state space
        states.notionalPrincipal = 0.0;
        states.nominalInterestRate = 0.0;
        states.accruedInterest = 0.0;
        states.accruedInterest = 0.0;
        states.accruedInterest2 = 0.0;
        states.feeAccrued = 0.0;
        states.statusDate = time;
        
        // copy post-event-states

        // return post-event-states
        return postEventStates;
        }
    
}

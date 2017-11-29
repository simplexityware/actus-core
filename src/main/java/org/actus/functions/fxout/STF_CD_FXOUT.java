/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.fxout;

import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;
import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;

import java.time.LocalDateTime;

public final class STF_CD_FXOUT implements StateTransitionFunction {
    
    @Override
    public double[] eval(LocalDateTime time, StateSpace states, 
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        double[] postEventStates = new double[8];
        
        // update state space
        states.timeFromLastEvent = dayCounter.dayCountFraction(states.lastEventTime, time);
        states.probabilityOfDefault = riskFactorModel.stateAt(model.getAs("LegalEntityIDCounterparty"),time,states,model);
        states.lastEventTime = time;
        
        // copy post-event-states
        postEventStates[0] = states.timeFromLastEvent;
        postEventStates[6] = states.probabilityOfDefault;
        
        // return post-event-states
        return postEventStates;
        }
    
}

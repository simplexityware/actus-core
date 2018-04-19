/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.cdswp;

import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;

import java.time.LocalDateTime;

public final class STF_FP1_CDSWP implements StateTransitionFunction {
    
    @Override
    public double[] eval(LocalDateTime time, StateSpace states, 
                         ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        double[] postEventStates = new double[8];

        // update state space
        states.timeFromLastEvent = dayCounter.dayCountFraction(states.lastEventTime, time);
        states.lastEventTime = time;

        if(model.<String>getAs("FeeBasis").equals("N")) {
            states.nominalAccrued += states.nominalRate * states.nominalValue * states.timeFromLastEvent;
        }/* else {
            states.nominalAccrued += states.nominalRate * states.timeFromLastEvent // TODO: Divide by  Yfr(TeV-FECL, TeV;
        }*/

        // copy post-event-states
        postEventStates[0] = states.timeFromLastEvent;
        postEventStates[2] = states.nominalAccrued;

        // return post-event-states
        return postEventStates;
        }
    
}

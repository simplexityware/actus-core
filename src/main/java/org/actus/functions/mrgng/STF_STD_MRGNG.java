/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */

package org.actus.functions.mrgng;

import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;

import java.time.LocalDateTime;

public class STF_STD_MRGNG implements StateTransitionFunction {

    @Override
    public double[] eval(LocalDateTime time, StateSpace states,
                         ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        double[] postEventStates = new double[8];

        // update state space
        states.timeFromLastEvent = dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.lastEventTime), timeAdjuster.shiftCalcTime(time));
        states.variationMargin = 0.0;
        states.lastEventTime = time;

        // copy post-event-states
        postEventStates[0] = states.timeFromLastEvent;

        // return post-event-states
        return postEventStates;
    }
}

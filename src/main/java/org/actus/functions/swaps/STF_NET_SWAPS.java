/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.swaps;

import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.events.ContractEvent;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;

import java.time.LocalDateTime;

public final class STF_NET_SWAPS implements StateTransitionFunction {
    ContractEvent e1;
    ContractEvent e2;

    public STF_NET_SWAPS(ContractEvent e1, ContractEvent e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    @Override
    public double[] eval(LocalDateTime time, StateSpace states, 
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        double[] postEventStates = new double[8];

        // net post-event-states
        postEventStates[0] = e1.states()[0];
        postEventStates[1] = e1.nominalValue() + e2.nominalValue();
        postEventStates[2] = e1.nominalAccrued() + e2.nominalAccrued();
        
        // return post-event-states
        return postEventStates;
    }
    
}

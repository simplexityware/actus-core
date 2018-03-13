/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.pam;

import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;
import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.util.CommonUtils;

import java.time.LocalDateTime;

public final class STF_IED_PAM implements StateTransitionFunction {
    
    @Override
    public double[] eval(LocalDateTime time, StateSpace states, 
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        double[] postEventStates = new double[8];
        
        // update state space
        states.timeFromLastEvent = dayCounter.dayCountFraction(states.lastEventTime, time);
        states.nominalValue = ContractRoleConvention.roleSign(model.getAs("ContractRole")) * model.<Double>getAs("NotionalPrincipal");
        states.nominalRate = model.getAs("NominalInterestRate");
        states.lastEventTime = time;

        // if cycle anchor date of interest payment prior to IED, then update nominal accrued accordingly
        if(!CommonUtils.isNull(model.getAs("CycleAnchorDateOfInterestPayment")) &&
                model.<LocalDateTime>getAs("CycleAnchorDateOfInterestPayment").isBefore(model.getAs("InitialExchangeDate"))) {
            states.nominalAccrued=states.nominalValue*states.nominalRate*dayCounter.dayCountFraction(model.<LocalDateTime>getAs("CycleAnchorDateOfInterestPayment"),time);
        }

        // copy post-event-states
        postEventStates[0] = states.timeFromLastEvent;
        postEventStates[1] = states.nominalValue;
        postEventStates[2] = states.nominalAccrued;
        postEventStates[3] = states.nominalRate;
        postEventStates[6] = states.probabilityOfDefault;
        
        // return post-event-states
        return postEventStates;
        }
    
}

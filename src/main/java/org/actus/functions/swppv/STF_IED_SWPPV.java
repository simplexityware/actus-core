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
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.types.ContractRole;

import java.time.LocalDateTime;

public final class STF_IED_SWPPV implements StateTransitionFunction {
    
    @Override
    public StateSpace eval(LocalDateTime time, StateSpace states,
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        StateSpace postEventStates = new StateSpace();
        
        // update state space
        states.notionalPrincipal = ContractRoleConvention.roleSign(ContractRole.valueOf(model.getAs("ContractRole"))) * model.<Double>getAs("NotionalPrincipal");
        states.notionalPrincipal2 = ContractRoleConvention.roleSign(ContractRole.valueOf(model.getAs("ContractRole"))) * (-1) * model.<Double>getAs("NotionalPrincipal");
        states.nominalInterestRate = model.<Double>getAs("NominalInterestRate2");
        states.statusDate = time;
        
        // copy post-event-states
        postEventStates.notionalPrincipal = states.notionalPrincipal2;
        postEventStates.nominalInterestRate = states.nominalInterestRate;
        
        // return post-event-states
        return postEventStates;
        }
    
}

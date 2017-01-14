/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.functions.pam;

import org.actus.functions.PayOffFunction;
import org.actus.states.StateSpace;
import org.actus.attributes.ContractModel;
import org.actus.externals.MarketModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.contractrole.ContractRoleConvention;

import java.time.LocalDateTime;

public final class POF_PY_PAM implements PayOffFunction {
    
    @Override
    public double eval(LocalDateTime time, StateSpace states, 
    ContractModel model, MarketModelProvider marketModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        if(model.penaltyType=='A') {
            return (1 - states.probabilityOfDefault) * ContractRoleConvention.roleSign(model.contractRole) * model.penaltyRate;
        } else if(model.penaltyType=='N') {
            return (1 - states.probabilityOfDefault) * ContractRoleConvention.roleSign(model.contractRole) *
                dayCounter.dayCountFraction(states.lastEventTime, time) * model.penaltyRate * states.nominalValue;
        } else {
            return (1 - states.probabilityOfDefault) * ContractRoleConvention.roleSign(model.contractRole) *
                dayCounter.dayCountFraction(states.lastEventTime, time) * states.nominalValue * 
                Math.max(0, states.nominalRate - marketModel.stateAt(model.marketObjectCodeOfRateReset, time));    
        }
    }
}

/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.functions.pam;

import org.actus.functions.PayOffFunction;
import org.actus.states.StateSpace;
import org.actus.externals.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.contractrole.ContractRoleConvention;

import java.time.LocalDateTime;

public final class POF_FP_PAM implements PayOffFunction {
    
    @Override
    public double eval(LocalDateTime time, StateSpace states, 
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        if(model.feeBasis()=='A') {
            return (1 - states.probabilityOfDefault) * ContractRoleConvention.roleSign(model.contractRole()) * model.feeRate();
        } else { 
            return (1 - states.probabilityOfDefault) * 
                (states.feeAccrued + 
                    dayCounter.dayCountFraction(states.lastEventTime, time) * model.feeRate() * states.nominalValue);
        }
    }
}

/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.fxout;

import org.actus.conventions.contractdefault.ContractDefaultConvention;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.functions.PayOffFunction;
import org.actus.states.StateSpace;
import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;

import java.time.LocalDateTime;

public final class POF_STD_FXOUT implements PayOffFunction {
    
    @Override
    public double eval(LocalDateTime time, StateSpace states, 
                        ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        int contractRoleSign = ContractRoleConvention.roleSign(model.getAs("ContractRole"));
        return ContractDefaultConvention.performanceIndicator(states.contractPerformance) * contractRoleSign *
                (model.<Double>getAs("NotionalPrincipal") - 1 / riskFactorModel.stateAt(model.getAs("Currency2")+"/"+model.getAs("Currency"), model.getAs("MaturityDate"), states, model) * model.<Double>getAs("NotionalPrincipal2"));
    }
}

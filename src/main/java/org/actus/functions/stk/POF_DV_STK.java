/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.stk;

import org.actus.conventions.contractdefault.ContractDefaultConvention;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.functions.PayOffFunction;
import org.actus.states.StateSpace;
import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.util.CycleUtils;

import java.time.LocalDateTime;

public final class POF_DV_STK implements PayOffFunction {
    
    @Override
    public double eval(LocalDateTime time, StateSpace states, 
                        ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        return ContractDefaultConvention.performanceIndicator(states.contractStatus) * ContractRoleConvention.roleSign(model.getAs("ContractRole"))*model.<Integer>getAs("Quantity") * model.<Double>getAs("MarketValueObserved") *
            riskFactorModel.stateAt(model.getAs("MarketObjectCodeOfDividendRate"), time, states, model) * 
            dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(time.minus(CycleUtils.parsePeriod(model.getAs("CycleOfDividendPayment")))), timeAdjuster.shiftCalcTime(time));
    }
}

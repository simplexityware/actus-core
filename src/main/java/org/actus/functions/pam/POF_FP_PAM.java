/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.pam;

import org.actus.functions.PayOffFunction;
import org.actus.states.StateSpace;
import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.types.ContractRole;
import org.actus.util.CommonUtils;

import java.time.LocalDateTime;

public final class POF_FP_PAM implements PayOffFunction {
    
    @Override
    public double eval(LocalDateTime time, StateSpace states, 
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        if(model.<String>getAs("FeeBasis").equals("A")) {
            return CommonUtils.settlementCurrencyFxRate(riskFactorModel, model, time, states)
                    * ContractRoleConvention.roleSign(ContractRole.valueOf(model.getAs("ContractRole")))
                    * model.<Double>getAs("FeeRate");
        } else { 
            return CommonUtils.settlementCurrencyFxRate(riskFactorModel, model, time, states)
                    * (states.feeAccrued + dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.statusDate), timeAdjuster.shiftCalcTime(time))
                    * model.<Double>getAs("FeeRate")
                    * states.notionalPrincipal);
        }
    }
}

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

import java.time.LocalDateTime;

public final class POF_PY_PAM implements PayOffFunction {
    
    @Override
    public double eval(LocalDateTime time, StateSpace states, 
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        if(model.getAs("PenaltyType").equals("A")) {
            return riskFactorModel.stateAt(model.getAs("Currency") + "/" + model.getAs("SettlementCurrency"),time,states,model)
                    * ContractRoleConvention.roleSign(model.getAs("ContractRole")) * model.<Double>getAs("PenaltyRate");
        } else if(model.getAs("PenaltyType").equals("N")) {
            return riskFactorModel.stateAt(model.getAs("Currency") + "/" + model.getAs("SettlementCurrency"),time,states,model)
                    * ContractRoleConvention.roleSign(model.getAs("ContractRole"))
                    * dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.statusDate), timeAdjuster.shiftCalcTime(time))
                    * model.<Double>getAs("PenaltyRate")
                    * states.notionalPrincipal;
        } else {
            return riskFactorModel.stateAt(model.getAs("Currency") + "/" + model.getAs("SettlementCurrency"),time,states,model)
                    * ContractRoleConvention.roleSign(model.getAs("ContractRole"))
                    * dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.statusDate), timeAdjuster.shiftCalcTime(time))
                    * states.notionalPrincipal
                    * Math.max(0, states.nominalInterestRate - riskFactorModel.stateAt(model.getAs("MarketObjectCodeOfRateReset"), time,states,model));
        }
    }
}

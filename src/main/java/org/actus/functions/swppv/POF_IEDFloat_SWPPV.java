package org.actus.functions.swppv;

import java.time.LocalDateTime;

import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.contractdefault.ContractDefaultConvention;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.PayOffFunction;
import org.actus.states.StateSpace;

public class POF_IEDFloat_SWPPV implements PayOffFunction {
    
    @Override
        public double eval(LocalDateTime time, StateSpace states, 
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        return ContractDefaultConvention.performanceIndicator(states.contractStatus) *
        ContractRoleConvention.roleSign(model.getAs("ContractRole")) * 
        (model.<Double>getAs("NotionalPrincipal") + model.<Double>getAs("PremiumDiscountAtIED"));
        }

}

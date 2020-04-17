/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.lam;

import org.actus.conventions.contractdefault.ContractDefaultConvention;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.functions.PayOffFunction;
import org.actus.states.StateSpace;
import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;

import java.time.LocalDateTime;

public final class POF_PR_LAM implements PayOffFunction {
    
    @Override
        public double eval(LocalDateTime time, StateSpace states, 
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
		double redemption = states.nextPrincipalRedemptionPayment - ContractRoleConvention.roleSign(model.getAs("ContractRole"))*Math.max(0, Math.abs(states.nextPrincipalRedemptionPayment) - Math.abs(states.notionalPrincipal));
		return ContractDefaultConvention.performanceIndicator(states.contractPerformance) * states.notionalScalingMultiplier * redemption;
	}
}

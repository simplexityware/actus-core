/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.nam;

import org.actus.conventions.contractdefault.ContractDefaultConvention;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.functions.PayOffFunction;
import org.actus.states.StateSpace;
import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;

import java.time.LocalDateTime;

public final class POF_PR_NAM implements PayOffFunction {
    
    @Override
    public double eval(LocalDateTime time, StateSpace states, 
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
    	double timeFromLastEvent = dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.lastEventTime), timeAdjuster.shiftCalcTime(time));
        double redemption =  states.nextPrincipalRedemptionPayment - (states.nominalAccrued+states.nominalRate * timeFromLastEvent * states.interestCalculationBase);
    	redemption =  redemption - ContractRoleConvention.roleSign(model.getAs("ContractRole"))*Math.max(0, Math.abs(redemption) - Math.abs(states.nominalValue));
        return ContractDefaultConvention.performanceIndicator(states.contractStatus) * states.nominalScalingMultiplier * redemption;
        }
}

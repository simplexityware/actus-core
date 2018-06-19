/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.nam;

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
        double redemption =  states.nextPrincipalRedemptionPayment - states.nominalAccrued - states.timeFromLastEvent * states.nominalRate * states.interestCalculationBase;
    	redemption =  states.contractRoleSign * redemption - Math.max(0, states.contractRoleSign * redemption - states.contractRoleSign * states.nominalValue); 
        return (1 - states.probabilityOfDefault) * states.nominalScalingMultiplier * states.contractRoleSign * redemption;
        }
}

/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.functions.pam;

import org.actus.functions.PayOffFunction;
import org.actus.states.StateSpace;
import org.actus.externals.ContractModelProvider;
import org.actus.externals.MarketModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.contractrole.ContractRoleConvention;

import java.time.LocalDateTime;

public final class POF_IED_PAM implements PayOffFunction {
    
    @Override
        public double eval(LocalDateTime time, StateSpace states, 
    ContractModelProvider model, MarketModelProvider marketModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        return (1 - states.probabilityOfDefault) * 
        ContractRoleConvention.roleSign(model.contractRole()) * (-1) * 
        (model.notionalPrincipal() + model.premiumDiscountAtIED());
        }
}

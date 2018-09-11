/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.mrgng;

import org.actus.attributes.ContractModel;
import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.PayOffFunction;
import org.actus.states.StateSpace;

import java.time.LocalDateTime;

public final class POF_MR_MRGNG implements PayOffFunction {
    
    @Override
    public double eval(LocalDateTime time, StateSpace states, 
                        ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {

        // compute delta market value of child contract
        ContractModel child = model.<ContractModel>getAs("Child");
        System.out.println(child.<String>getAs("ContractType"));
        double marketValueObserved = riskFactorModel.stateAt(child.getAs("MarketObjectCode"),time,states,child);
        double deltaValue = ContractRoleConvention.roleSign(child.getAs("ContractRole")) * (marketValueObserved - states.marketValueObserved);

        // compute total margin = initial margin + total variation margin
        ContractModel parent = model.getAs("Parent");
        double maintenanceMarginLowerBound = parent.getAs("MaintenanceMarginLowerBound");
        double maintenanceMarginUpperBound = parent.getAs("MaintenanceMarginUpperBound");
        double totalMargin = parent.<Double>getAs("InitialMargin") + states.variationMargin + deltaValue;

        // compute margin call
        double marginCall = Math.max(Math.max(0,maintenanceMarginLowerBound-totalMargin),totalMargin-maintenanceMarginUpperBound);
        return states.contractRoleSign * marginCall;
    }
}

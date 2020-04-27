/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.fxout;

import org.actus.functions.PayOffFunction;
import org.actus.states.StateSpace;
import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;

import java.time.LocalDateTime;

public final class POF_PRD_FXOUT implements PayOffFunction {
    
    @Override
    public double eval(LocalDateTime time, StateSpace states, 
                        ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        return riskFactorModel.stateAt(model.getAs("Currency") + "/" + model.getAs("SettlementCurrency"),time,states,model)
                * (-1)
                * model.<Double>getAs("PriceAtPurchaseDate");
    }
}

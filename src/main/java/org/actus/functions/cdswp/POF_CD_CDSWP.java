/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.cdswp;

import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.PayOffFunction;
import org.actus.states.StateSpace;

import java.time.LocalDateTime;

public final class POF_CD_CDSWP implements PayOffFunction {
    
    @Override
    public double eval(LocalDateTime time, StateSpace states,
                       ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        return 0.0;
    }
}

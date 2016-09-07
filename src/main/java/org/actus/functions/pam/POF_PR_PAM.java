/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.functions.pam;

import org.actus.functions.PayOffFunction;
import org.actus.states.StateSpace;
import org.actus.attributes.ContractModel;
import org.actus.riskfactors.RiskFactorProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.contractrole.ContractRoleConvention;

import java.time.LocalDateTime;

public class POF_PR_PAM implements PayOffFunction {
    
    @Override
        public double eval(LocalDateTime time, StateSpace states, 
    ContractModel model, RiskFactorProvider riskFactors, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        return (1 - riskFactors.stateAt(model.legalEntityIDCounterparty, time, null)) * 
        ContractRoleConvention.roleSign(model.contractRole) * states.scalingMultiplier * 
        (states.nominalValue + states.nominalAccrued + 
        dayCounter.dayCountFraction(states.lastEventTime, time) * states.nominalRate * states.nominalValue);
        }
}

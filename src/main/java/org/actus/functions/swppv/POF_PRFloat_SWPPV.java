package org.actus.functions.swppv;

import java.time.LocalDateTime;

import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.PayOffFunction;
import org.actus.states.StateSpace;

public class POF_PRFloat_SWPPV implements PayOffFunction {
    
    @Override
        public double eval(LocalDateTime time, StateSpace states, 
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
    	return riskFactorModel.stateAt(model.getAs("Currency") + "/" + model.getAs("SettlementCurrency"),time,states,model)
                * states.notionalScalingMultiplier
                * states.notionalPrincipal2;
        }

}

package org.actus.functions.optns;

import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;
import org.actus.types.ContractReference;
import org.actus.types.OptionType;
import org.actus.util.CommonUtils;

import java.time.LocalDateTime;

public class STF_MD_OPTNS implements StateTransitionFunction {
    @Override
    public StateSpace eval(LocalDateTime time, StateSpace states, ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        if(CommonUtils.isNull(states.exerciseDate)){
            double x = 0.0;
            double st = riskFactorModel.stateAt(model.<ContractReference>getAs("ContractStructure").getContractAttribute("MarketObjectCode"), time, states, model);
            OptionType option = model.getAs("OptionType");
            if(option.equals(OptionType.C)){
                x = Math.max(st - model.<Double>getAs("OptionStrike1"), 0.0);
            } else if(option.equals(OptionType.P)){
                x = Math.max(model.<Double>getAs("OptionStrike1") - st, 0.0);
            } else{
                x = Math.max(st - model.<Double>getAs("OptionStrike1"), 0.0) + Math.max(model.<Double>getAs("OptionStrike2") - st, 0.0);
            }
            if(x == 0.0){
                states.exerciseDate = null;
            } else {
                states.exerciseDate = time;
            }
        }
        states.statusDate = time;

        return StateSpace.copyStateSpace(states);
    }
}

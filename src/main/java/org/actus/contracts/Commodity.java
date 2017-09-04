/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import org.actus.AttributeConversionException;
import org.actus.externals.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.events.ContractEvent;
import org.actus.states.StateSpace;
import org.actus.events.EventFactory;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.util.StringUtils;
import org.actus.util.CommonUtils;
import org.actus.functions.pam.POF_AD_PAM;
import org.actus.functions.pam.STF_AD_PAM;
import org.actus.functions.stk.POF_PRD_STK;
import org.actus.functions.stk.STF_PRD_STK;
import org.actus.functions.stk.POF_TD_STK;
import org.actus.functions.stk.STF_TD_STK;


import java.time.LocalDateTime;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents the Commodity payoff algorithm
 * 
 * @see <a href="http://www.projectactus.org/"></a>
 */
public final class Commodity {

    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        // init day count calculator 
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", null);
        
        // compute events
        ArrayList<ContractEvent> payoff = new ArrayList<ContractEvent>();
        // analysis events
        payoff.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
        // purchase
        if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            payoff.add(EventFactory.createEvent(model.getAs("PurchaseDate"), StringUtils.EventType_PRD, model.getAs("Currency"), new POF_PRD_STK(), new STF_PRD_STK()));
        }
        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            payoff.add(EventFactory.createEvent(model.getAs("TerminationDate"), StringUtils.EventType_TD, model.getAs("Currency"), new POF_TD_STK(), new STF_TD_STK()));
        }
        // remove all pre-status date events
        payoff.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null,
                                                                  null)) == -1);
        // initialize state space per status date
        StateSpace states = new StateSpace();
        states.contractRoleSign = ContractRoleConvention.roleSign(model.getAs("ContractRole"));
        states.lastEventTime = model.getAs("StatusDate");
        
        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(payoff);

        // evaluate events
        payoff.forEach(e -> e.eval(states, model, riskFactorModel, dayCount, model.getAs("BusinessDayConvention")));
        
        // return all evaluated post-StatusDate events as the payoff
        return payoff;
    }

}

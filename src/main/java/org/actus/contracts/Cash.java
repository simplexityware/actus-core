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
import org.actus.functions.pam.POF_AD_PAM;
import org.actus.functions.pam.STF_AD_PAM;
import org.actus.functions.csh.POF_PR_CSH;
import org.actus.functions.csh.STF_PR_CSH;


import java.time.LocalDateTime;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents the Cash payoff algorithm
 * 
 * @see <a href="http://www.projectactus.org/"></a>
 */
public final class Cash {

    public static ArrayList<ContractEvent> eval(Set<LocalDateTime> analysisTimes, 
                        		         ContractModelProvider model, 
                        		         RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        // init day count calculator 
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", null);
        
        // compute events
        ArrayList<ContractEvent> payoff = new ArrayList<ContractEvent>();
        payoff.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.currency(), new POF_AD_PAM(), new STF_AD_PAM()));
        payoff.add(EventFactory.createEvent(Collections.min(analysisTimes).plusSeconds(1), StringUtils.EventType_PR, model.currency(), new POF_PR_CSH(), new STF_PR_CSH()));
        
        // initialize state space per status date
        StateSpace states = new StateSpace();
        states.contractRoleSign = ContractRoleConvention.roleSign(model.contractRole());
        states.lastEventTime = model.statusDate();
        states.nominalValue = model.notionalPrincipal();
        
        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(payoff);

        // evaluate events
        payoff.forEach(e -> e.eval(states, model, riskFactorModel, dayCount, model.businessDayConvention()));
        
        // return all evaluated post-StatusDate events as the payoff
        return payoff;
    }

}

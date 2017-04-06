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
import org.actus.time.ScheduleFactory;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.util.CommonUtils;
import org.actus.util.StringUtils;
import org.actus.util.Constants;
import org.actus.functions.pam.POF_AD_PAM;
import org.actus.functions.pam.STF_AD_PAM;
import org.actus.functions.pam.POF_CD_PAM;
import org.actus.functions.stk.STF_CD_STK;
import org.actus.functions.stk.POF_PRD_STK;
import org.actus.functions.stk.STF_PRD_STK;
import org.actus.functions.stk.POF_TD_STK;
import org.actus.functions.stk.STF_TD_STK;
import org.actus.functions.stk.POF_DV_STK;
import org.actus.functions.stk.STF_DV_STK;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents the Stock payoff algorithm
 * 
 * @see <a href="http://www.projectactus.org/"></a>
 */
public final class Stock {

    public static ArrayList<ContractEvent> eval(Set<LocalDateTime> analysisTimes, 
                        		         ContractModelProvider model, 
                        		         RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        
        // compute events
        ArrayList<ContractEvent> payoff = initEvents(analysisTimes,model,riskFactorModel);
        
        // initialize state space per status date
        StateSpace states = initStateSpace(model);
        
        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(payoff);

        // evaluate events
        payoff.forEach(e -> e.eval(states, model, riskFactorModel, model.dayCountConvention(), model.businessDayConvention()));
        
        // remove pre-purchase events if purchase date set (we only consider post-purchase events for analysis)
        if(!CommonUtils.isNull(model.purchaseDate())) {
            payoff.removeIf(e -> !e.type().equals(StringUtils.EventType_AD) && e.compareTo(EventFactory.createEvent(model.purchaseDate(), StringUtils.EventType_PRD, model.currency(), null, null)) == -1);    
        }
        
        // return all evaluated post-StatusDate events as the payoff
        return payoff;
    }
    
    private static ArrayList<ContractEvent> initEvents(Set<LocalDateTime> analysisTimes, ContractModelProvider model, RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        HashSet<ContractEvent> events = new HashSet<ContractEvent>();

        // create contract event schedules
        // analysis events
        events.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.currency(), new POF_AD_PAM(), new STF_AD_PAM()));
        // purchase
        if (!CommonUtils.isNull(model.purchaseDate())) {
            events.add(EventFactory.createEvent(model.purchaseDate(), StringUtils.EventType_PRD, model.currency(), new POF_PRD_STK(), new STF_PRD_STK()));
        }
        // dividend payment related
        if (!CommonUtils.isNull(model.cycleOfDividendPayment())) {
            if(CommonUtils.isNull(model.terminationDate())) {
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.cycleAnchorDateOfDividendPayment(),
                                                                                model.cycleAnchorDateOfDividendPayment().plus(Constants.MAX_LIFETIME),
                                                                                model.cycleOfDividendPayment(),
                                                                                model.endOfMonthConvention()),
                                                  StringUtils.EventType_DV, model.currency(), new POF_DV_STK(), new STF_DV_STK(), model.businessDayConvention()));
            } else {
                events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.cycleAnchorDateOfDividendPayment(),
                                                                                model.terminationDate(),
                                                                                model.cycleOfDividendPayment(),
                                                                                model.endOfMonthConvention()),
                                                  StringUtils.EventType_DV, model.currency(), new POF_DV_STK(), new STF_DV_STK(), model.businessDayConvention()));

            }
        }
        // termination
        if (!CommonUtils.isNull(model.terminationDate())) {
            ContractEvent termination =
                                        EventFactory.createEvent(model.terminationDate(), StringUtils.EventType_TD, model.currency(), new POF_TD_STK(), new STF_TD_STK());
            events.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
            events.add(termination);
        }
        // add counterparty default risk-factor contingent events
        if(riskFactorModel.keys().contains(model.legalEntityIDCounterparty())) {
            events.addAll(EventFactory.createEvents(riskFactorModel.times(model.legalEntityIDCounterparty()),
                                             StringUtils.EventType_CD, model.currency(), new POF_CD_PAM(), new STF_CD_STK()));
        }
        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.statusDate(), StringUtils.EventType_SD, model.currency(), null,
                                                                  null)) == -1);
        
        // return events
        return new ArrayList<ContractEvent>(events);
    }

    private static StateSpace initStateSpace(ContractModelProvider model) throws AttributeConversionException {
        StateSpace states = new StateSpace();

        // TODO: some attributes can be null
        states.contractRoleSign = ContractRoleConvention.roleSign(model.contractRole());
        states.lastEventTime = model.statusDate();
        
        // return the initialized state space
        return states;
    }

}

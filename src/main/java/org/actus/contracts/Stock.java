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
import org.actus.conventions.daycount.DayCountCalculator;
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

    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        
        // init day count calculator
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", model.getAs("Calendar"));
            
        // compute events
        ArrayList<ContractEvent> payoff = initEvents(analysisTimes,model,riskFactorModel);
        
        // initialize state space per status date
        StateSpace states = initStateSpace(model);
        
        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(payoff);

        // evaluate events
        payoff.forEach(e -> e.eval(states, model, riskFactorModel, dayCount, model.getAs("BusinessDayConvention")));
        
        // remove pre-purchase events if purchase date set (we only consider post-purchase events for analysis)
        if(!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            payoff.removeIf(e -> !e.type().equals(StringUtils.EventType_AD) && e.compareTo(EventFactory.createEvent(model.getAs("PurchaseDate"), StringUtils.EventType_PRD, model.getAs("Currency"), null, null)) == -1);    
        }
        
        // return all evaluated post-StatusDate events as the payoff
        return payoff;
    }
    
    private static ArrayList<ContractEvent> initEvents(Set<LocalDateTime> analysisTimes, ContractModelProvider model, RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        HashSet<ContractEvent> events = new HashSet<ContractEvent>();

        // create contract event schedules
        // analysis events
        events.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
        // purchase
        if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            events.add(EventFactory.createEvent(model.getAs("PurchaseDate"), StringUtils.EventType_PRD, model.getAs("Currency"), new POF_PRD_STK(), new STF_PRD_STK()));
        }
        // dividend payment related
        if (!CommonUtils.isNull(model.getAs("CycleOfDividendPayment"))) {
            if(CommonUtils.isNull(model.getAs("TerminationDate"))) {
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfDividendPayment"),
                                                                                model.<LocalDateTime>getAs("CycleAnchorDateOfDividendPayment").plus(Constants.MAX_LIFETIME),
                                                                                model.getAs("CycleOfDividendPayment"),
                                                                                model.getAs("EndOfMonthConvention")),
                                                  StringUtils.EventType_DV, model.getAs("Currency"), new POF_DV_STK(), new STF_DV_STK(), model.getAs("BusinessDayConvention")));
            } else {
                events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfDividendPayment"),
                                                                                model.getAs("TerminationDate"),
                                                                                model.getAs("CycleOfDividendPayment"),
                                                                                model.getAs("EndOfMonthConvention")),
                                                  StringUtils.EventType_DV, model.getAs("Currency"), new POF_DV_STK(), new STF_DV_STK(), model.getAs("BusinessDayConvention")));

            }
        }
        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            ContractEvent termination =
                                        EventFactory.createEvent(model.getAs("TerminationDate"), StringUtils.EventType_TD, model.getAs("Currency"), new POF_TD_STK(), new STF_TD_STK());
            events.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
            events.add(termination);
        }
        // add counterparty default risk-factor contingent events
        if(riskFactorModel.keys().contains(model.getAs("LegalEntityIDCounterparty"))) {
            events.addAll(EventFactory.createEvents(riskFactorModel.times(model.getAs("LegalEntityIDCounterparty")),
                                             StringUtils.EventType_CD, model.getAs("Currency"), new POF_CD_PAM(), new STF_CD_STK()));
        }
        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null,
                                                                  null)) == -1);
        
        // return events
        return new ArrayList<ContractEvent>(events);
    }

    private static StateSpace initStateSpace(ContractModelProvider model) throws AttributeConversionException {
        StateSpace states = new StateSpace();

        // TODO: some attributes can be null
        states.contractRoleSign = ContractRoleConvention.roleSign(model.getAs("ContractRole"));
        states.lastEventTime = model.getAs("StatusDate");
        
        // return the initialized state space
        return states;
    }

}

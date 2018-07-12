/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import org.actus.AttributeConversionException;
import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.events.ContractEvent;
import org.actus.states.StateSpace;
import org.actus.events.EventFactory;
import org.actus.conventions.businessday.BusinessDayAdjuster;
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
import java.time.Period;
import java.util.*;

/**
 * Represents the Commodity payoff algorithm
 * 
 * @see <a href="http://www.projectactus.org/"></a>
 */
public final class Commodity {

    // forward projection of the entire lifecycle of the contract
    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        // init day count calculator 
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", null);

        // compute events
        ArrayList<ContractEvent> lifecycle = new ArrayList<ContractEvent>();
        // analysis events
        lifecycle.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
        // purchase
        if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            lifecycle.add(EventFactory.createEvent(model.getAs("PurchaseDate"), StringUtils.EventType_PRD, model.getAs("Currency"), new POF_PRD_STK(), new STF_PRD_STK()));
        }
        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            lifecycle.add(EventFactory.createEvent(model.getAs("TerminationDate"), StringUtils.EventType_TD, model.getAs("Currency"), new POF_TD_STK(), new STF_TD_STK()));
        }
        // remove all pre-status date events
        lifecycle.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null,
                null)) == -1);
        // initialize state space per status date
        StateSpace states = new StateSpace();
        states.contractRoleSign = ContractRoleConvention.roleSign(model.getAs("ContractRole"));
        states.lastEventTime = model.getAs("StatusDate");

        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(lifecycle);

        // evaluate events
        lifecycle.forEach(e -> e.eval(states, model, riskFactorModel, dayCount, new BusinessDayAdjuster(null, null)));

        // return all evaluated post-StatusDate events as the payoff
        return lifecycle;
    }

    // forward projection of the payoff of the contract
    public static ArrayList<ContractEvent> payoff(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return Commodity.lifecycle(analysisTimes,model,riskFactorModel);
    }

    // compute the contract schedule
    public static ArrayList<ContractEvent> schedule(ContractModelProvider model) throws AttributeConversionException {
        Set<LocalDateTime> times = new HashSet<LocalDateTime>();
        times.add(model.getAs("StatusDate"));
        return Commodity.lifecycle(times, model, null);
    }

    // compute next n non-contingent events
    public static ArrayList<ContractEvent> next(int n,
                                                ContractModelProvider model) throws AttributeConversionException {
        return new ArrayList<ContractEvent>();
    }

    // compute next n non-contingent events
    public static ArrayList<ContractEvent> next(Period within,
                                                ContractModelProvider model) throws AttributeConversionException {
        return new ArrayList<ContractEvent>();
    }

    // apply a set of events to the current state of a contract and return the post events state
    public static StateSpace apply(Set<ContractEvent> events,
                                   ContractModelProvider model) throws AttributeConversionException {

        // initialize state space per status date
        StateSpace states = new StateSpace();
        states.contractRoleSign = ContractRoleConvention.roleSign(model.getAs("ContractRole"));
        states.lastEventTime = model.getAs("StatusDate");

        // sort the events according to their time sequence
        ArrayList<ContractEvent> seqEvents = new ArrayList<>(events);
        Collections.sort(seqEvents);

        // apply events according to their time sequence to current state
        seqEvents.forEach(e -> e.eval(states, model, null, new DayCountCalculator("A/AISDA", null), new BusinessDayAdjuster(null, null)));

        // return post events states
        return states;
    }
}
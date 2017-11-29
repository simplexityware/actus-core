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
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.util.StringUtils;
import org.actus.functions.pam.POF_AD_PAM;
import org.actus.functions.pam.STF_AD_PAM;
import org.actus.functions.csh.POF_PR_CSH;
import org.actus.functions.csh.STF_PR_CSH;


import java.time.LocalDateTime;
import java.time.Period;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents the Cash payoff algorithm
 * 
 * @see <a href="http://www.projectactus.org/"></a>
 */
public final class Cash {

    // compute contingent lifecycle of the contract
    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        // init day count calculator 
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", null);
        
        // compute events
        ArrayList<ContractEvent> lifecycle = new ArrayList<ContractEvent>();
        lifecycle.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
        lifecycle.add(EventFactory.createEvent(Collections.min(analysisTimes).plusSeconds(1), StringUtils.EventType_PR, model.getAs("Currency"), new POF_PR_CSH(), new STF_PR_CSH()));
        
        // initialize state space per status date
        StateSpace states = new StateSpace();
        states.contractRoleSign = ContractRoleConvention.roleSign(model.getAs("ContractRole"));
        states.lastEventTime = model.getAs("StatusDate");
        states.nominalValue = model.getAs("NotionalPrincipal");
        
        // sort the events in the lifecycle-list according to their time of occurence
        Collections.sort(lifecycle);

        // evaluate events
        lifecycle.forEach(e -> e.eval(states, model, riskFactorModel, dayCount, model.getAs("BusinessDayConvention")));
        
        // return all evaluated post-StatusDate events as the lifecycle
        return lifecycle;
    }

    // compute contingent payoff of the contract
    // note: this is trivial no transactional events exist
    public static ArrayList<ContractEvent> payoff(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        // init day count calculator
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", null);

        // compute events
        ArrayList<ContractEvent> payoff = new ArrayList<ContractEvent>();
        payoff.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));

        // initialize state space per status date
        StateSpace states = new StateSpace();
        states.contractRoleSign = ContractRoleConvention.roleSign(model.getAs("ContractRole"));
        states.lastEventTime = model.getAs("StatusDate");
        states.nominalValue = model.getAs("NotionalPrincipal");

        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(payoff);

        // evaluate events
        payoff.forEach(e -> e.eval(states, model, riskFactorModel, dayCount, model.getAs("BusinessDayConvention")));

        // return all evaluated post-StatusDate events as the payoff
        return payoff;
    }

    // compute next n non-contingent events
    // note: this is trivial as no events at all
    public static ArrayList<ContractEvent> next(LocalDateTime from,
                                                int n,
                                                ContractModelProvider model,
                                                RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return new ArrayList<ContractEvent>();
    }

    // compute next n non-contingent events
    // note: this is trivial as no events at all
    public static ArrayList<ContractEvent> next(int n,
                                                ContractModelProvider model,
                                                RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return new ArrayList<ContractEvent>();
    }

    // compute next non-contingent events within period
    public static ArrayList<ContractEvent> next(LocalDateTime from,
                                                Period within,
                                                ContractModelProvider model,
                                                RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return new ArrayList<ContractEvent>();
    }

    // compute next n non-contingent events
    public static ArrayList<ContractEvent> next(Period within,
                                                ContractModelProvider model,
                                                RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return new ArrayList<ContractEvent>();
    }

    // compute non-contingent lifecycle of the contract
    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model) throws AttributeConversionException {
        // init day count calculator
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", null);

        // compute events
        ArrayList<ContractEvent> lifecycle = new ArrayList<ContractEvent>();
        lifecycle.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
        lifecycle.add(EventFactory.createEvent(Collections.min(analysisTimes).plusSeconds(1), StringUtils.EventType_PR, model.getAs("Currency"), new POF_PR_CSH(), new STF_PR_CSH()));

        // initialize state space per status date
        StateSpace states = new StateSpace();
        states.contractRoleSign = ContractRoleConvention.roleSign(model.getAs("ContractRole"));
        states.lastEventTime = model.getAs("StatusDate");
        states.nominalValue = model.getAs("NotionalPrincipal");

        // sort the events in the lifecycle-list according to their time of occurence
        Collections.sort(lifecycle);

        // evaluate events
        lifecycle.forEach(e -> e.eval(states, model, null, dayCount, model.getAs("BusinessDayConvention")));

        // return all evaluated post-StatusDate events as the lifecycle
        return lifecycle;
    }

    // compute non-contingent payoff of the contract
    public static ArrayList<ContractEvent> payoff(Set<LocalDateTime> analysisTimes,
                                                  ContractModelProvider model) throws AttributeConversionException {
        // init day count calculator
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", null);

        // compute events
        ArrayList<ContractEvent> payoff = new ArrayList<ContractEvent>();
        payoff.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));

        // initialize state space per status date
        StateSpace states = new StateSpace();
        states.contractRoleSign = ContractRoleConvention.roleSign(model.getAs("ContractRole"));
        states.lastEventTime = model.getAs("StatusDate");
        states.nominalValue = model.getAs("NotionalPrincipal");

        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(payoff);

        // evaluate events
        payoff.forEach(e -> e.eval(states, model, null, dayCount, model.getAs("BusinessDayConvention")));

        // return all evaluated post-StatusDate events as the payoff
        return payoff;
    }

    // compute next n non-contingent events
    // note: this is trivial as no events at all
    public static ArrayList<ContractEvent> next(LocalDateTime from,
                                                int n,
                                                ContractModelProvider model) throws AttributeConversionException {
        return new ArrayList<ContractEvent>();
    }

    // compute next n non-contingent events
    // note: this is trivial as no events at all
    public static ArrayList<ContractEvent> next(int n,
                                                ContractModelProvider model) throws AttributeConversionException {
        return new ArrayList<ContractEvent>();
    }

    // compute next non-contingent events within period
    public static ArrayList<ContractEvent> next(LocalDateTime from,
                                                Period within,
                                                ContractModelProvider model) throws AttributeConversionException {
        return new ArrayList<ContractEvent>();
    }

    // compute next n non-contingent events
    public static ArrayList<ContractEvent> next(Period within,
                                                ContractModelProvider model) throws AttributeConversionException {
        return new ArrayList<ContractEvent>();
    }

}

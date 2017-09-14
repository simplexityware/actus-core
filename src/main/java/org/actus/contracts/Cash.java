/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
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
import java.time.Period;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    // compute contingent events within time window
    // note: this is trivial as only 1 single non-transactional event (at first analysis time) occurs
    public static ArrayList<ContractEvent> events(Set<LocalDateTime> analysisTimes,
                                                  ContractModelProvider model,
                                                  RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return Cash.lifecycle(analysisTimes,model,riskFactorModel);
    }

    // compute contingent events within time period
    // note: this is trivial as only 1 single non-transactional event (at first analysis time) occurs
    public static ArrayList<ContractEvent> events(LocalDateTime analysisTime,
                                                  Period period,
                                                  ContractModelProvider model,
                                                  RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return Cash.events(Stream.of(analysisTime,analysisTime.plus(period)).collect(Collectors.toSet()),model,riskFactorModel);
    }

    // compute contingent transactions within time window
    // note: this is trivial as only 1 single non-transactional event (at first analysis time) occurs
    public static ArrayList<ContractEvent> transactions(Set<LocalDateTime> analysisTimes,
                                                  ContractModelProvider model,
                                                  RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return Cash.payoff(analysisTimes,model,riskFactorModel);
    }

    // compute contingent transactions within time period
    // note: this is trivial as only 1 single non-transactional event (at first analysis time) occurs
    public static ArrayList<ContractEvent> transactions(LocalDateTime analysisTime,
                                                  Period period,
                                                  ContractModelProvider model,
                                                  RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return Cash.transactions(Stream.of(analysisTime,analysisTime.plus(period)).collect(Collectors.toSet()),model,riskFactorModel);
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

    // compute non-contingent events within time window
    public static ArrayList<ContractEvent> events(Set<LocalDateTime> analysisTimes,
                                                  ContractModelProvider model) throws AttributeConversionException {
        return Cash.lifecycle(analysisTimes,model);
    }

    // compute non-contingent events within time period
    public static ArrayList<ContractEvent> events(LocalDateTime analysisTime,
                                                  Period period,
                                                  ContractModelProvider model) throws AttributeConversionException {
        return Cash.lifecycle(Stream.of(analysisTime,analysisTime.plus(period)).collect(Collectors.toSet()),model);
    }

    // compute non-contingent transactions within time window
    public static ArrayList<ContractEvent> transactions(Set<LocalDateTime> analysisTimes,
                                                        ContractModelProvider model) throws AttributeConversionException {
        return Cash.payoff(analysisTimes,model);
    }

    // compute non-contingent transactions within time period
    public static ArrayList<ContractEvent> transactions(LocalDateTime analysisTime,
                                                        Period period,
                                                        ContractModelProvider model) throws AttributeConversionException {
        return Cash.transactions(Stream.of(analysisTime,analysisTime.plus(period)).collect(Collectors.toSet()),model);
    }

}

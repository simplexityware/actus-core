/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import org.actus.AttributeConversionException;
import org.actus.attributes.ContractModel;
import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.events.ContractEvent;
import org.actus.events.EventFactory;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.mrgng.*;
import org.actus.functions.pam.POF_AD_PAM;
import org.actus.functions.pam.STF_AD_PAM;
import org.actus.states.StateSpace;
import org.actus.time.ScheduleFactory;
import org.actus.util.StringUtils;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents the Margining payoff algorithm
 *
 * TODO: add CLH ContractRole which was previously indicated
 * through an additional attribute ClearingHouse but needs to be
 * CNTRL now as a separate contract where one party is always the CLH
 *
 * @see <a href="http://www.projectactus.org/"></a>
 */
public final class Margining {

    // forward projection of the entire lifecycle of the contract
    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        // init day count calculator
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", null);
        
        // compute events
        ArrayList<ContractEvent> events = new ArrayList<ContractEvent>();
        // margining events
        events.addAll(
                EventFactory.createEvents(
                        ScheduleFactory.createSchedule(
                                model.getAs("CycleAnchorDateOfMargining"),
                                model.getAs("SettlementDate"),
                                model.getAs("CycleOfMargining"),
                                model.getAs("EndOfMonthConvention"),false),
                        StringUtils.EventType_MR, model.getAs("Currency"), new POF_MR_MRGNG(), new STF_MR_MRGNG(), model.getAs("BusinessDayConvention"))
        );
        // settlement event
        events.add(EventFactory.createEvent(model.getAs("SettlementDate"),StringUtils.EventType_STD,model.getAs("Currency"),new POF_STD_MRGNG(), new STF_STD_MRGNG()));
        // analysis events
        events.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_MRGNG()));


        // initialize state space per status date
        StateSpace states = new StateSpace();
        states.contractRoleSign = ContractRoleConvention.roleSign(model.getAs("ContractRole"));
        states.lastEventTime = model.getAs("StatusDate");
        states.variationMargin = model.<Double>getAs("VariationMargin");
        states.marketValueObserved = model.<Double>getAs("MarketValueObserved");

        System.out.println("MVO="+states.marketValueObserved);

        // sort the events in the lifecycle-list according to their time of occurence
        Collections.sort(events);

        // evaluate events
        events.forEach(e -> e.eval(states, model, riskFactorModel, dayCount, new BusinessDayAdjuster(null, null)));
        
        // return all evaluated post-StatusDate events as the lifecycle
        return events;
    }

    // forward projection of the payoff of the contract
    public static ArrayList<ContractEvent> payoff(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return Margining.lifecycle(analysisTimes,model,riskFactorModel).stream().filter(ev->StringUtils.TransactionalEvents.contains(ev.type())).collect(Collectors.toCollection(ArrayList::new));
    }

    // compute the contract schedule
    // TODO: implement
    public static ArrayList<ContractEvent> schedule(ContractModelProvider model) throws AttributeConversionException {
        return new ArrayList<ContractEvent>();
    }

    // compute next n non-contingent events
    // TODO: implement
    public static ArrayList<ContractEvent> next(int n,
                                                ContractModelProvider model) throws AttributeConversionException {
        return new ArrayList<ContractEvent>();
    }

    // compute next n non-contingent events
    // TODO: implement
    public static ArrayList<ContractEvent> next(Period within,
                                                ContractModelProvider model) throws AttributeConversionException {
        return new ArrayList<ContractEvent>();
    }

    // apply a set of events to the current state of a contract and return the post events state
    public static StateSpace apply(Set<ContractEvent> events,
                                   ContractModelProvider model) throws AttributeConversionException {

        // extract parent attributes
        ContractModel parent = model.getAs("Parent");

        // initialize state space per status date
        StateSpace states = new StateSpace();
        states.contractRoleSign = ContractRoleConvention.roleSign(parent.getAs("ContractRole"));
        states.lastEventTime = parent.getAs("StatusDate");
        states.variationMargin = parent.getAs("VariationMargin");

        // sort the events according to their time sequence
        ArrayList<ContractEvent> seqEvents = new ArrayList<>(events);
        Collections.sort(seqEvents);

        // apply events according to their time sequence to current state
        seqEvents.forEach(e -> e.eval(states, model, null, new DayCountCalculator("A/AISDA", null), new BusinessDayAdjuster(null, null)));

        // return post events states
        return states;
    }

}

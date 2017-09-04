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
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the Commodity payoff algorithm
 * 
 * @see <a href="http://www.projectactus.org/"></a>
 */
public final class Commodity {

    // compute contingent lifecycle of the contract
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
        lifecycle.forEach(e -> e.eval(states, model, riskFactorModel, dayCount, model.getAs("BusinessDayConvention")));

        // return all evaluated post-StatusDate events as the payoff
        return lifecycle;
    }

    // compute contingent payoff of the contract
    // note: trivial as all events are transactional events
    public static ArrayList<ContractEvent> payoff(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return Commodity.lifecycle(analysisTimes,model,riskFactorModel);
    }

    // compute contingent events in time window
    public static ArrayList<ContractEvent> events(Set<LocalDateTime> analysisTimes,
                                                  ContractModelProvider model,
                                                  RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return Commodity.lifecycle(analysisTimes,model,riskFactorModel).stream().filter(ev->!ev.time().isBefore(analysisTimes.stream().min(Comparator.naturalOrder()).get()) && ! ev.time().isAfter(analysisTimes.stream().max(Comparator.naturalOrder()).get())).collect(Collectors.toCollection(ArrayList::new));
    }

    // compute contingent events in time period
    public static ArrayList<ContractEvent> events(LocalDateTime analysisTime,
                                                  Period period,
                                                  ContractModelProvider model,
                                                  RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return Commodity.events(Stream.of(analysisTime,analysisTime.plus(period)).collect(Collectors.toSet()),model,riskFactorModel);
    }

    // compute contingent transactions in time window
    // note: trivial as all events are transactional events
    public static ArrayList<ContractEvent> transactions(Set<LocalDateTime> analysisTimes,
                                                        ContractModelProvider model,
                                                        RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return Commodity.events(analysisTimes,model,riskFactorModel);
    }

    // compute contingent transactions in time period
    // note: trivial as all events are transactional events
    public static ArrayList<ContractEvent> transactions(LocalDateTime analysisTime,
                                                        Period period,
                                                        ContractModelProvider model,
                                                        RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return Commodity.events(analysisTime,period,model,riskFactorModel);
    }

    // compute non-contingent lifecycle of the contract
    // note: trivial as no contingent events in the lifecycle of the contract
    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model) throws AttributeConversionException {
        return Commodity.lifecycle(analysisTimes,model,null);
    }

    // compute non-contingent payoff of the contract
    // note: trivial as no contingent events in the lifecycle of the contract
    public static ArrayList<ContractEvent> payoff(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model) throws AttributeConversionException {
        return Commodity.payoff(analysisTimes,model,null);
    }

    // compute non-contingent events of the contract
    // note: trivial as no contingent events in the lifecycle of the contract
    public static ArrayList<ContractEvent> events(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model) throws AttributeConversionException {
        return Commodity.events(analysisTimes,model,null);
    }

    // compute non-contingent events of the contract
    // note: trivial as no contingent events in the lifecycle of the contract
    public static ArrayList<ContractEvent> events(LocalDateTime analysisTime,
                                                     Period period,
                                                     ContractModelProvider model) throws AttributeConversionException {
        return Commodity.events(analysisTime,period,model,null);
    }

    // compute non-contingent transactions of the contract
    // note: trivial as no contingent events in the lifecycle of the contract
    public static ArrayList<ContractEvent> transactions(Set<LocalDateTime> analysisTimes,
                                                  ContractModelProvider model) throws AttributeConversionException {
        return Commodity.transactions(analysisTimes,model,null);
    }

    // compute non-contingent transactions of the contract
    // note: trivial as no contingent events in the lifecycle of the contract
    public static ArrayList<ContractEvent> transactions(LocalDateTime analysisTime,
                                                  Period period,
                                                  ContractModelProvider model) throws AttributeConversionException {
        return Commodity.transactions(analysisTime,period,model,null);
    }
}
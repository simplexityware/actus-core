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
import org.actus.functions.stk.*;
import org.actus.states.StateSpace;
import org.actus.events.EventFactory;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.time.ScheduleFactory;
import org.actus.util.CommonUtils;
import org.actus.util.Constants;
import org.actus.util.StringUtils;
import org.actus.functions.pam.POF_AD_PAM;
import org.actus.functions.pam.STF_AD_PAM;
import org.actus.functions.fxout.POF_PRD_FXOUT;
import org.actus.functions.fxout.POF_TD_FXOUT;
import org.actus.functions.fxout.POF_STD_FXOUT;
import org.actus.functions.fxout.STF_STD_FXOUT;
import org.actus.functions.fxout.POF_STD1_FXOUT;
import org.actus.functions.fxout.STF_STD1_FXOUT;
import org.actus.functions.fxout.POF_STD2_FXOUT;
import org.actus.functions.fxout.STF_STD2_FXOUT;
import org.actus.functions.pam.POF_CD_PAM;
import org.actus.functions.fxout.STF_CD_FXOUT;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the Foreign Exchange Outright payoff algorithm
 * 
 * @see <a href="http://www.projectactus.org/"></a>
 */
public final class ForeignExchangeOutright {

    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {

        // init day count calculator 
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", null);
        
        // compute events
        ArrayList<ContractEvent> lifecycle = initEvents(analysisTimes,model);

        // compute and add contingent events
        lifecycle.addAll(initContingentEvents(analysisTimes,model,riskFactorModel));

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(lifecycle);

        // evaluate events
        lifecycle.forEach(e -> e.eval(states, model, riskFactorModel, dayCount, model.getAs("BusinessDayConvention")));
        
        // return all evaluated post-StatusDate events as the payoff
        return lifecycle;
    }

    // compute contingent payoff of the contract
    public static ArrayList<ContractEvent> payoff(Set<LocalDateTime> analysisTimes,
                                                  ContractModelProvider model,
                                                  RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return ForeignExchangeOutright.lifecycle(analysisTimes,model,riskFactorModel).stream().filter(ev->StringUtils.TransactionalEvents.contains(ev.type())).collect(Collectors.toCollection(ArrayList::new));
    }

    // compute contingent events in time window
    // note: filter by date as only very few events
    public static ArrayList<ContractEvent> events(Set<LocalDateTime> analysisTimes,
                                                  ContractModelProvider model,
                                                  RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return ForeignExchangeOutright.lifecycle(analysisTimes,model,riskFactorModel).stream().filter(ev->!ev.time().isBefore(analysisTimes.stream().min(Comparator.naturalOrder()).get()) && ! ev.time().isAfter(analysisTimes.stream().max(Comparator.naturalOrder()).get())).collect(Collectors.toCollection(ArrayList::new));
    }

    // compute contingent events in time period
    public static ArrayList<ContractEvent> events(LocalDateTime analysisTime,
                                                  Period period,
                                                  ContractModelProvider model,
                                                  RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return ForeignExchangeOutright.events(Stream.of(analysisTime,analysisTime.plus(period)).collect(Collectors.toSet()),model,riskFactorModel);
    }

    // compute contingent transactions in time window
    public static ArrayList<ContractEvent> transactions(Set<LocalDateTime> analysisTimes,
                                                        ContractModelProvider model,
                                                        RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return ForeignExchangeOutright.events(analysisTimes,model,riskFactorModel).stream().filter(ev->StringUtils.TransactionalEvents.contains(ev.type())).collect(Collectors.toCollection(ArrayList::new));
    }

    // compute contingent transactions in time period
    public static ArrayList<ContractEvent> transactions(LocalDateTime analysisTime,
                                                        Period period,
                                                        ContractModelProvider model,
                                                        RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return ForeignExchangeOutright.transactions(Stream.of(analysisTime,analysisTime.plus(period)).collect(Collectors.toSet()),model,riskFactorModel);
    }

    // compute non-contingent portion of lifecycle of the contract
    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model) throws AttributeConversionException {
        // init day count calculator
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", null);

        // compute non-contingent events
        ArrayList<ContractEvent> events = initEvents(analysisTimes,model);

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(events);

        // evaluate only non-contingent events and add these to new list
        ArrayList<ContractEvent> eventsNonContingent = new ArrayList<ContractEvent>();
        Iterator<ContractEvent> iterator = events.iterator();
        while(iterator.hasNext()) {
            ContractEvent event = iterator.next();
            if(StringUtils.ContingentEvents.contains(event.type())) {
                break;
            }
            event.eval(states, model, null, dayCount, model.getAs("BusinessDayConvention"));
            eventsNonContingent.add(event);
        }

        // return all non-contingent events as the non-contingent part of the lifecycle
        return events;
    }

    // compute non-contingent portion of payoff of the contract
    public static ArrayList<ContractEvent> payoff(Set<LocalDateTime> analysisTimes,
                                                  ContractModelProvider model) throws AttributeConversionException {
        return ForeignExchangeOutright.lifecycle(analysisTimes,model).stream().filter(ev->StringUtils.TransactionalEvents.contains(ev.type())).collect(Collectors.toCollection(ArrayList::new));
    }

    // compute non-contingent events in time window
    // note: filter by date as only very few events
    public static ArrayList<ContractEvent> events(Set<LocalDateTime> analysisTimes,
                                                  ContractModelProvider model) throws AttributeConversionException {
        return ForeignExchangeOutright.lifecycle(analysisTimes,model).stream().filter(ev->!ev.time().isBefore(analysisTimes.stream().min(Comparator.naturalOrder()).get()) && ! ev.time().isAfter(analysisTimes.stream().max(Comparator.naturalOrder()).get())).collect(Collectors.toCollection(ArrayList::new));
    }

    // compute non-contingent events in time period
    public static ArrayList<ContractEvent> events(LocalDateTime analysisTime,
                                                  Period period,
                                                  ContractModelProvider model) throws AttributeConversionException {
        return ForeignExchangeOutright.events(Stream.of(analysisTime,analysisTime.plus(period)).collect(Collectors.toSet()),model);
    }

    // compute non-contingent transactions in time window
    public static ArrayList<ContractEvent> transactions(Set<LocalDateTime> analysisTimes,
                                                        ContractModelProvider model) throws AttributeConversionException {
        return ForeignExchangeOutright.events(analysisTimes,model).stream().filter(ev->StringUtils.TransactionalEvents.contains(ev.type())).collect(Collectors.toCollection(ArrayList::new));
    }

    // compute non-contingent transactions in time period
    public static ArrayList<ContractEvent> transactions(LocalDateTime analysisTime,
                                                        Period period,
                                                        ContractModelProvider model) throws AttributeConversionException {
        return ForeignExchangeOutright.transactions(Stream.of(analysisTime,analysisTime.plus(period)).collect(Collectors.toSet()),model);
    }

    // compute (but not evaluate) non-contingent events
    private static ArrayList<ContractEvent> initEvents(Set<LocalDateTime> analysisTimes, ContractModelProvider model) throws AttributeConversionException {
        HashSet<ContractEvent> events = new HashSet<ContractEvent>();

        // determine settlement date (maturity) of the contract
        LocalDateTime settlement = model.getAs("SettlementDate");
        if (CommonUtils.isNull(settlement)) {
            settlement = model.getAs("MaturityDate");
        }
        // analysis events
        events.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
        // purchase
        if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            events.add(EventFactory.createEvent(model.getAs("PurchaseDate"), StringUtils.EventType_PRD, model.getAs("Currency"), new POF_PRD_FXOUT(), new STF_PRD_STK()));
        }
        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            events.add(EventFactory.createEvent(model.getAs("TerminationDate"), StringUtils.EventType_TD, model.getAs("Currency"), new POF_TD_FXOUT(), new STF_TD_STK()));
        }
        // settlement
        if (CommonUtils.isNull(model.getAs("DeliverySettlement")) || model.getAs("DeliverySettlement").equals(StringUtils.Settlement_Physical)) {
            events.add(EventFactory.createEvent(settlement, StringUtils.EventType_STD, model.getAs("Currency"), new POF_STD1_FXOUT(), new STF_STD1_FXOUT(), model.getAs("BusinessDayConvention")));
            events.add(EventFactory.createEvent(settlement, StringUtils.EventType_STD, model.getAs("Currency2"), new POF_STD2_FXOUT(), new STF_STD2_FXOUT(), model.getAs("BusinessDayConvention")));
        } else {
            events.add(EventFactory.createEvent(settlement, StringUtils.EventType_STD, model.getAs("Currency"), new POF_STD_FXOUT(), new STF_STD_FXOUT(), model.getAs("BusinessDayConvention")));
        }
        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null,
                null)) == -1);

        // return events
        return new ArrayList<ContractEvent>(events);
    }

    // compute (but not evaluate) contingent events
    private static ArrayList<ContractEvent> initContingentEvents(Set<LocalDateTime> analysisTimes, ContractModelProvider model, RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        HashSet<ContractEvent> events = new HashSet<ContractEvent>();

        if(riskFactorModel.keys().contains(model.getAs("LegalEntityIDCounterparty"))) {
            events.addAll(EventFactory.createEvents(riskFactorModel.times(model.getAs("LegalEntityIDCounterparty")),
                    StringUtils.EventType_CD, model.getAs("Currency"), new POF_CD_PAM(), new STF_CD_FXOUT()));
        }
        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null,
                null)) == -1);

        // return events
        return new ArrayList<ContractEvent>(events);
    }

    // initialize state space per status date
    private static StateSpace initStateSpace(ContractModelProvider model) {
        StateSpace states = new StateSpace();
        states.contractRoleSign = ContractRoleConvention.roleSign(model.getAs("ContractRole"));
        states.lastEventTime = model.getAs("StatusDate");
        return states;
    }
}

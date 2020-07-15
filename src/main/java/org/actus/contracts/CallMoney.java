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
import org.actus.time.ScheduleFactory;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.types.ContractRole;
import org.actus.types.EndOfMonthConventionEnum;
import org.actus.types.EventType;
import org.actus.util.CommonUtils;
import org.actus.functions.clm.POF_IED_CLM;
import org.actus.functions.pam.STF_IED_PAM;
import org.actus.functions.pam.POF_PR_PAM;
import org.actus.functions.pam.STF_PR_PAM;
import org.actus.functions.pam.STF_RRF_PAM;
import org.actus.functions.clm.POF_IP_CLM;
import org.actus.functions.clm.STF_IP_CLM;
import org.actus.functions.pam.POF_IPCI_PAM;
import org.actus.functions.pam.STF_IPCI_PAM;
import org.actus.functions.pam.POF_RR_PAM;
import org.actus.functions.clm.STF_RR_CLM;
import org.actus.functions.pam.POF_FP_PAM;
import org.actus.functions.pam.STF_FP_PAM;


import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents the Call Money payoff algorithm
 * 
 * @see <a https://www.actusfrf.org"></a>
 */
public final class CallMoney {

    // compute next n events
    public static ArrayList<ContractEvent> schedule(LocalDateTime to,
                                                    ContractModelProvider model) throws AttributeConversionException {
        ArrayList<ContractEvent> events = new ArrayList<ContractEvent>();

        // determine maturity of the contract
        LocalDateTime maturity = maturity(model,to);

        // initial exchange
        events.add(EventFactory.createEvent(model.getAs("InitialExchangeDate"), EventType.IED, model.getAs("Currency"), new POF_IED_CLM(), new STF_IED_PAM()));
        // principal redemption
        events.add(EventFactory.createEvent(maturity, EventType.PR, model.getAs("Currency"), new POF_PR_PAM(), new STF_PR_PAM()));
        // interest payment event
        events.add(EventFactory.createEvent(maturity, EventType.IP, model.getAs("Currency"), new POF_IP_CLM(), new STF_IP_CLM()));
        // interest payment capitalization (if specified)
        if (!CommonUtils.isNull(model.getAs("CycleOfInterestPayment"))) {
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfInterestPayment"),
                    maturity,
                    model.getAs("CycleOfInterestPayment"),
                    EndOfMonthConventionEnum.valueOf(model.getAs("EndOfMonthConvention")),false),
                    EventType.IPCI, model.getAs("Currency"), new POF_IPCI_PAM(), new STF_IPCI_PAM(), model.getAs("BusinessDayConvention")));
        }
        // rate reset
        Set<ContractEvent> rateResetEvents = EventFactory.createEvents(ScheduleFactory.createSchedule(model.<LocalDateTime>getAs("CycleAnchorDateOfRateReset"), maturity,
                model.getAs("CycleOfRateReset"), EndOfMonthConventionEnum.valueOf(model.getAs("EndOfMonthConvention")),false),
                EventType.RR, model.getAs("Currency"), new POF_RR_PAM(), new STF_RR_CLM(), model.getAs("BusinessDayConvention"));
        
        // adapt fixed rate reset event
        if(!CommonUtils.isNull(model.getAs("NextResetRate"))) {
            ContractEvent fixedEvent = rateResetEvents.stream().sorted().filter(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), EventType.AD, model.getAs("Currency"), null, null)) == 1).findFirst().get();
            fixedEvent.fStateTrans(new STF_RRF_PAM());
            fixedEvent.type(EventType.RRF);
            rateResetEvents.add(fixedEvent);
        }

        events.addAll(rateResetEvents);

        // fees (if specified)
        if (!CommonUtils.isNull(model.getAs("CycleOfFee"))) {
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfFee"), maturity,
                    model.getAs("CycleOfFee"), EndOfMonthConventionEnum.valueOf(model.getAs("EndOfMonthConvention")),false),
                    EventType.FP, model.getAs("Currency"), new POF_FP_PAM(), new STF_FP_PAM(), model.getAs("BusinessDayConvention")));
        }
        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), EventType.AD, model.getAs("Currency"), null,
                null)) == -1);

        // remove all post to-date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(to, EventType.AD, model.getAs("Currency"), null, null)) == 1);

        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(events);

        return events;
    }

    // apply a set of events to the current state of a contract and return the post events state
    public static ArrayList<ContractEvent> apply(ArrayList<ContractEvent> events,
                                                 ContractModelProvider model,
                                                 RiskFactorModelProvider observer) throws AttributeConversionException {

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events according to their time sequence
        Collections.sort(events);

        // apply events according to their time sequence to current state
        events.forEach(e -> e.eval(states, model, observer, model.getAs("DayCountConvention"), model.getAs("BusinessDayConvention")));

        // return evaluated events
        return events;
    }

    // determine maturity of the contract
    private static LocalDateTime maturity(ContractModelProvider model, LocalDateTime to) {
        LocalDateTime maturity = model.getAs("MaturityDate");
        if (CommonUtils.isNull(maturity)) {
            maturity = to;
        }
        return maturity;
    }

    private static StateSpace initStateSpace(ContractModelProvider model) throws AttributeConversionException {
        StateSpace states = new StateSpace();
        states.notionalScalingMultiplier = 1;
        states.interestScalingMultiplier = 1;

        // TODO: some attributes can be null
        states.statusDate = model.getAs("StatusDate");
        if (!model.<LocalDateTime>getAs("InitialExchangeDate").isAfter(model.getAs("StatusDate"))) {
            states.notionalPrincipal = ContractRoleConvention.roleSign(ContractRole.valueOf(model.getAs("ContractRole")))*model.<Double>getAs("NotionalPrincipal");
            states.nominalInterestRate = model.getAs("NominalInterestRate");
            states.accruedInterest = ContractRoleConvention.roleSign(ContractRole.valueOf(model.getAs("ContractRole")))*model.<Double>getAs("AccruedInterest");
            states.feeAccrued = model.getAs("FeeAccrued");
        }
        
        // return the initialized state space
        return states;
    }

}

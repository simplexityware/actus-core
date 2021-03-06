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
import org.actus.util.CommonUtils;
import org.actus.util.StringUtils;
import org.actus.time.ScheduleFactory;
import org.actus.functions.pam.*;
import org.actus.functions.swppv.*;
import org.actus.functions.fxout.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents the Plain Vanilla Interest Rate Swap payoff algorithm
 * 
 * @see <a href="https://www.actusfrf.org"></a>
 */
public final class PlainVanillaInterestRateSwap {

    // compute next n non-contingent events
    public static ArrayList<ContractEvent> schedule(LocalDateTime to,
                                                    ContractModelProvider model) throws AttributeConversionException {
        ArrayList<ContractEvent> events = new ArrayList<ContractEvent>();

        // purchase
        if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            events.add(EventFactory.createEvent(model.getAs("PurchaseDate"), StringUtils.EventType_PRD, model.getAs("Currency"), new POF_PRD_FXOUT(), new STF_PRD_SWPPV()));
        }
        // interest payment events
        if (CommonUtils.isNull(model.getAs("DeliverySettlement")) || model.getAs("DeliverySettlement").equals(StringUtils.Settlement_Physical)) {
            // in case of physical delivery (delivery of individual cash flows)
            // fixed initial exchange
            events.add(EventFactory.createEvent(model.getAs("InitialExchangeDate"), StringUtils.EventType_IED, model.getAs("Currency"), new POF_IED_PAM(), new STF_IED_PAM()));
            // float initial exchange
            events.add(EventFactory.createEvent(model.getAs("InitialExchangeDate"), StringUtils.EventType_IED, model.getAs("Currency"), new POF_IEDFloat_SWPPV(), new STF_IED_SWPPV()));
            // fixed principal redemption
            events.add(EventFactory.createEvent(model.getAs("MaturityDate"), StringUtils.EventType_PR, model.getAs("Currency"), new POF_PR_PAM(), new STF_PR_SWPPV()));
            // float principal redemption
            events.add(EventFactory.createEvent(model.getAs("MaturityDate"), StringUtils.EventType_PR, model.getAs("Currency"), new POF_PRFloat_SWPPV(), new STF_PR_SWPPV()));
            // interest payment schedule
            Set<LocalDateTime> interestSchedule = ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfInterestPayment"),
                    model.getAs("MaturityDate"),
                    model.getAs("CycleOfInterestPayment"),
                    model.getAs("EndOfMonthConvention"));
            // fixed rate events                                                                                                    model.getAs("MaturityDate"),                                                                                                  model.getAs("EndOfMonthConvention"))
            events.addAll(EventFactory.createEvents(interestSchedule, StringUtils.EventType_IP, model.getAs("Currency"), new POF_IPFix_SWPPV(), new STF_IPFix_SWPPV(), model.getAs("BusinessDayConvention")));
            // floating rate events                                                                                                    model.getAs("MaturityDate"),                                                                                                  model.getAs("EndOfMonthConvention"))
            events.addAll(EventFactory.createEvents(interestSchedule, StringUtils.EventType_IP, model.getAs("Currency"), new POF_IPFloat_SWPPV(), new STF_IPFloat_SWPPV(), model.getAs("BusinessDayConvention")));
        } else {
            // initial exchange
            events.add(EventFactory.createEvent(model.getAs("InitialExchangeDate"), StringUtils.EventType_IED, model.getAs("Currency"), new POF_IED_SWPPV(), new STF_IED_SWPPV()));
            // principal redemption
            events.add(EventFactory.createEvent(model.getAs("MaturityDate"), StringUtils.EventType_PR, model.getAs("Currency"), new POF_PR_SWPPV(), new STF_PR_SWPPV()));
            // in case of cash delivery (cash settlement)                                                                                                model.getAs("MaturityDate"),                                                                                                  model.getAs("EndOfMonthConvention"))
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfInterestPayment"),
                    model.getAs("MaturityDate"),
                    model.getAs("CycleOfInterestPayment"),
                    model.getAs("EndOfMonthConvention")),
                    StringUtils.EventType_IP, model.getAs("Currency"), new POF_IP_SWPPV(), new STF_IP_SWPPV(), model.getAs("BusinessDayConvention")));

        }

        // rate reset
        events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfRateReset"), model.getAs("MaturityDate"),
                model.getAs("CycleOfRateReset"), model.getAs("EndOfMonthConvention"), false),
                StringUtils.EventType_RR, model.getAs("Currency"), new POF_RR_PAM(), new STF_RR_SWPPV(), model.getAs("BusinessDayConvention")));
        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            ContractEvent termination =
                                        EventFactory.createEvent(model.getAs("TerminationDate"), StringUtils.EventType_TD, model.getAs("Currency"), new POF_TD_FXOUT(), new STF_TD_SWPPV());
                                        events.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
            events.add(termination);
        }

        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            ContractEvent termination = EventFactory.createEvent(model.getAs("TerminationDate"), StringUtils.EventType_TD, model.getAs("Currency"), new POF_TD_FXOUT(), new STF_TD_SWPPV());
            events.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
            events.add(termination);
        }

        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null,null)) == -1);

        // remove all post to-date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(to, StringUtils.EventType_AD, model.getAs("Currency"), null,null)) == 1);

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

    // initialize state space per status date
    private static StateSpace initStateSpace(ContractModelProvider model) throws AttributeConversionException {
        StateSpace states = new StateSpace();
        states.nominalScalingMultiplier = 1;
        states.lastEventTime = model.getAs("StatusDate");
        if (!model.<LocalDateTime>getAs("InitialExchangeDate").isAfter(model.getAs("StatusDate"))) {
            states.nominalValue = ContractRoleConvention.roleSign(model.getAs("ContractRole"))*model.<Double>getAs("NotionalPrincipal");
            states.nominalRate = model.getAs("NominalInterestRate");
            states.nominalAccrued = ContractRoleConvention.roleSign(model.getAs("ContractRole"))*model.<Double>getAs("AccruedInterest");
        }
        return states;
    }
}

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
import org.actus.functions.stk.*;
import org.actus.states.StateSpace;
import org.actus.events.EventFactory;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.types.DeliverySettlement;
import org.actus.types.EventType;
import org.actus.util.CommonUtils;
import org.actus.functions.fxout.POF_PRD_FXOUT;
import org.actus.functions.fxout.POF_TD_FXOUT;
import org.actus.functions.fxout.POF_STD_FXOUT;
import org.actus.functions.fxout.STF_STD_FXOUT;
import org.actus.functions.fxout.POF_STD1_FXOUT;
import org.actus.functions.fxout.STF_STD1_FXOUT;
import org.actus.functions.fxout.POF_STD2_FXOUT;
import org.actus.functions.fxout.STF_STD2_FXOUT;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents the Foreign Exchange Outright payoff algorithm
 * 
 * @see <a href="https://www.actusfrf.org"></a>
 */
public final class ForeignExchangeOutright {

    // compute next n non-contingent events
    public static ArrayList<ContractEvent> schedule(LocalDateTime to,
                                                    ContractModelProvider model) throws AttributeConversionException {
        ArrayList<ContractEvent> events = new ArrayList<ContractEvent>();

        // determine settlement date (maturity) of the contract
        LocalDateTime settlement = model.getAs("SettlementDate");
        if (CommonUtils.isNull(settlement)) {
            settlement = model.getAs("MaturityDate");
        }
        // purchase
        if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            events.add(EventFactory.createEvent(model.getAs("PurchaseDate"), EventType.PRD, model.getAs("Currency"), new POF_PRD_FXOUT(), new STF_PRD_STK()));
        }
        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            events.add(EventFactory.createEvent(model.getAs("TerminationDate"), EventType.TD, model.getAs("Currency"), new POF_TD_FXOUT(), new STF_TD_STK()));
        }
        // settlement
        if (CommonUtils.isNull(model.getAs("DeliverySettlement")) || model.getAs("DeliverySettlement").equals(DeliverySettlement.D)) {
            events.add(EventFactory.createEvent(settlement, EventType.STD, model.getAs("Currency"), new POF_STD1_FXOUT(), new STF_STD1_FXOUT(), model.getAs("BusinessDayConvention")));
            events.add(EventFactory.createEvent(settlement, EventType.STD, model.getAs("Currency2"), new POF_STD2_FXOUT(), new STF_STD2_FXOUT(), model.getAs("BusinessDayConvention")));
        } else {
            events.add(EventFactory.createEvent(settlement, EventType.STD, model.getAs("Currency"), new POF_STD_FXOUT(), new STF_STD_FXOUT(), model.getAs("BusinessDayConvention")));
        }

        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), EventType.AD, model.getAs("Currency"), null,null)) == -1);

        // remove all post to-date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(to, EventType.AD, model.getAs("Currency"), null,null)) == 1);

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
        events.forEach(e -> e.eval(states, model, observer, new DayCountCalculator("A/AISDA", model.getAs("Calendar")), model.getAs("BusinessDayConvention")));

        // return evaluated events
        return events;
    }

    // initialize state space per status date
    private static StateSpace initStateSpace(ContractModelProvider model) {
        StateSpace states = new StateSpace();
        states.statusDate = model.getAs("StatusDate");
        return states;
    }
}

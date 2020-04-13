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
import org.actus.functions.stk.POF_PRD_STK;
import org.actus.functions.stk.STF_PRD_STK;
import org.actus.functions.stk.POF_TD_STK;
import org.actus.functions.stk.STF_TD_STK;


import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents the Commodity payoff algorithm
 * 
 * @see <a https://www.actusfrf.org"></a>
 */
public final class Commodity {

    // compute next n non-contingent events
    public static ArrayList<ContractEvent> schedule(LocalDateTime to,
                                                ContractModelProvider model) throws AttributeConversionException {
        ArrayList<ContractEvent> events = new ArrayList<ContractEvent>();

        // purchase
        if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            events.add(EventFactory.createEvent(model.getAs("PurchaseDate"), StringUtils.EventType_PRD, model.getAs("Currency"), new POF_PRD_STK(), new STF_PRD_STK()));
        }
        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            events.add(EventFactory.createEvent(model.getAs("TerminationDate"), StringUtils.EventType_TD, model.getAs("Currency"), new POF_TD_STK(), new STF_TD_STK()));
        }

        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null, null)) == -1);
        
        // remove all post to-date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null, null)) == 1);

        return events;
    }

    // apply a set of events to the current state of a contract and return the post events state
    public static ArrayList<ContractEvent> apply(ArrayList<ContractEvent> events,
                                                 ContractModelProvider model,
                                                 RiskFactorModelProvider observer) throws AttributeConversionException {

        // initialize state space per status date
        StateSpace states = new StateSpace();
        states.statusDate = model.getAs("StatusDate");

        // sort the events according to their time sequence
        Collections.sort(events);

        // apply events according to their time sequence to current state
        events.forEach(e -> e.eval(states, model, observer, new DayCountCalculator("A/AISDA", null), new BusinessDayAdjuster(null, null)));

        // return evaluated events
        return events;
    }
}
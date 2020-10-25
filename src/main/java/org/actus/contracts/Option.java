/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */

package org.actus.contracts;

import org.actus.AttributeConversionException;
import org.actus.attributes.ContractModelProvider;
import org.actus.events.ContractEvent;
import org.actus.events.EventFactory;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.optns.*;
import org.actus.functions.stk.STF_PRD_STK;
import org.actus.functions.stk.STF_TD_STK;
import org.actus.states.StateSpace;
import org.actus.types.EventType;
import org.actus.util.CommonUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents the Option contract algorithm
 *
 * @see <a https://www.actusfrf.org"></a>
 */

public final class Option {
    // forward projection of the entire lifecycle of the contract
    public static ArrayList<ContractEvent> schedule(LocalDateTime to,
                                                    ContractModelProvider model) throws AttributeConversionException {
        ArrayList<ContractEvent> events = new ArrayList<>();
        // purchase
        if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            events.add(EventFactory.createEvent(model.getAs("PurchaseDate"), EventType.PRD, model.getAs("Currency"), new POF_PRD_OPTNS(), new STF_PRD_STK(), model.getAs("ContractID")));
        }

        //exercise & settlement
        if(!CommonUtils.isNull(model.getAs("ExerciseDate"))){
            events.add(EventFactory.createEvent(model.getAs("ExerciseDate"), EventType.XD,model.getAs("Currency"), new POF_XD_OPTNS(), new STF_XD_OPTNS(), model.getAs("ContractID")));
            events.add(EventFactory.createEvent(model.<LocalDateTime>getAs("ExerciseDate").plus(model.getAs("SettlementPeriod")), EventType.STD, model.getAs("Currency"), new POF_STD_OPTNS(), new STF_STD_OPTNS(),model.getAs("ContractID")));
        }

        events.add(EventFactory.createEvent(model.getAs("MaturityDate"), EventType.MD, model.getAs("Currency"), new POF_MD_OPTNS(), new STF_MD_OPTNS(), model.getAs("ContractID")));

        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            ContractEvent termination = EventFactory.createEvent(
                    model.getAs("TerminationDate"),
                    EventType.TD,
                    model.getAs("Currency"),
                    new POF_TD_OPTNS(),
                    new STF_TD_STK(),
                    model.getAs("ContractID")
            );
            events.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
            events.add(termination);
        }
        return events;
    }

    // apply a set of events to the current state of a contract and return the post events state
    public static ArrayList<ContractEvent> apply(ArrayList<ContractEvent> events,
                                                 ContractModelProvider model,
                                                 RiskFactorModelProvider observer) throws AttributeConversionException {
        //Add external XD-event
        events.addAll(observer.events(model));

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events according to their time sequence
        Collections.sort(events);

        // apply events according to their time sequence to current state
        events.forEach(e -> e.eval(states, model, observer, model.getAs("DayCountConvention"), model.getAs("BusinessDayConvention")));

        // return post events states
        return events;
    }

    private static StateSpace initStateSpace(ContractModelProvider model) throws AttributeConversionException {
        StateSpace states = new StateSpace();

        // initialize state variables
        states.statusDate = model.getAs("StatusDate");
        states.exerciseAmount = model.getAs("ExerciseAmount");
        states.exerciseDate = model.getAs("ExerciseDate");
        states.contractPerformance = model.getAs("ContractPerformance");

        // return the initialized state space
        return states;
    }
}


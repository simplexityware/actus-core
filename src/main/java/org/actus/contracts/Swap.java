/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */

package org.actus.contracts;

import org.actus.AttributeConversionException;
import org.actus.attributes.ContractModel;
import org.actus.attributes.ContractModelProvider;
import org.actus.events.ContractEvent;
import org.actus.events.EventFactory;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.stk.STF_PRD_STK;
import org.actus.functions.stk.STF_TD_STK;
import org.actus.functions.swaps.POF_NET_SWAPS;
import org.actus.functions.swaps.POF_PRD_SWAPS;
import org.actus.functions.swaps.POF_TD_SWAPS;
import org.actus.functions.swaps.STF_NET_SWAPS;
import org.actus.states.StateSpace;
import org.actus.util.CommonUtils;
import org.actus.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents the Swap payoff algorithm
 *
 * @see <a href="https://www.actusfrf.org"></a>
 */
public final class Swap {

    // forward projection of the entire lifecycle of the contract
    public static ArrayList<ContractEvent> schedule(LocalDateTime to,
                                                    ContractModelProvider model) throws AttributeConversionException {
        ArrayList<ContractEvent> events = new ArrayList<ContractEvent>();

        // extract parent attributes
        ContractModel parent = model.getAs("Parent");

        // compute child 1 and child 2 events
        events.addAll(ContractType.schedule(to,model.getAs("Child1")));
        events.addAll(ContractType.schedule(to,model.getAs("Child2")));

        // compute parent events
        // purchase
        if (!CommonUtils.isNull(parent.getAs("PurchaseDate"))) {
            ContractEvent purchase = EventFactory.createEvent(parent.getAs("PurchaseDate"), StringUtils.EventType_PRD, parent.getAs("Currency"), new POF_PRD_SWAPS(), new STF_PRD_STK());
            events.removeIf(e -> e.compareTo(purchase) == -1); // remove all pre-purchase events
            events.add(purchase);
        }
        // termination
        if (!CommonUtils.isNull(parent.getAs("TerminationDate"))) {
            ContractEvent termination =
                    EventFactory.createEvent(parent.getAs("TerminationDate"), StringUtils.EventType_TD, parent.getAs("Currency"), new POF_TD_SWAPS(), new STF_TD_STK());
            events.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
            events.add(termination);
        }

        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(parent.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null, null)) == -1);

        // remove all post to-date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(to, StringUtils.EventType_SD, model.getAs("Currency"), null, null)) == 1);

        // return events
        return events;
    }

    // apply a set of events to the current state of a contract and return the post events state
    public static ArrayList<ContractEvent> apply(ArrayList<ContractEvent> events,
                                                 ContractModelProvider model,
                                                 RiskFactorModelProvider observer) throws AttributeConversionException {

        // extract parent attributes
        ContractModel parent = model.getAs("Parent");

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events according to their time sequence
        Collections.sort(events);

        // apply events according to their time sequence to current state
        events.forEach(e -> e.eval(states, model, observer, model.getAs("DayCountConvention"), model.getAs("BusinessDayConvention")));
        
        /*
        // apply settlement option, i.e. "delivery" of all events or net "settlement" of events at same time
        ArrayList<ContractEvent> events = null;
        if(!CommonUtils.isNull(parent.getAs("DeliverySettlement")) &&
                parent.getAs("DeliverySettlement").equals(StringUtils.Settlement_Cash)) { // net all events
            Map<String, ContractEvent> mergedEvents = Stream.concat(child1.stream(), child2.stream())
                    .collect(Collectors.toMap(
                            e -> e.time() + e.type(), // event key for merging
                            e -> e, // event itself
                            (e1, e2) -> nettingEvent(e1,e2) // "merger" function
                            )
                    );
            events = new ArrayList<>(mergedEvents.values());
        } else { // "net" only analysis events
            // step 1: merge all analysis events of child contracts
            Map<String, ContractEvent> mergedEvents = Stream
                    .concat(child1.stream(), child2.stream())
                    .filter(e->e.type().equals(StringUtils.EventType_AD))
                    .collect(Collectors.toMap(
                            e -> e.time() + e.type(), // event key for merging
                            e -> e, // event itself
                            (e1, e2) -> nettingEvent(e1,e2) // "merger" function
                            )
                    );
            // step 2: concatenate non-analysis events and merged analysis events
            events = Stream
                    .concat(Stream
                            .concat(child1.stream(),child2.stream())
                            .filter(e->!e.type().equals(StringUtils.EventType_AD)),
                            mergedEvents.values().stream())
                    .collect(Collectors.toCollection(ArrayList::new));
        }*/

        // return evaluated events
        return events;
    }

    private static StateSpace initStateSpace(ContractModelProvider model) throws AttributeConversionException {
        StateSpace states = new StateSpace();
        states.statusDate = model.getAs("StatusDate");

        // return the initialized state space
        return states;
    }

    // private method that allows creating a "netting" event from two events to be netted
    private static ContractEvent nettingEvent(ContractEvent e1, ContractEvent e2) {
        ContractEvent netting = EventFactory.createEvent(e1.time(), e1.type(), e1.currency(), new POF_NET_SWAPS(e1,e2), new STF_NET_SWAPS(e1,e2));
        netting.eval(null,null,null,null,null);
        return(netting);
    }

}

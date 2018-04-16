/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */

package org.actus.contracts;

import org.actus.AttributeConversionException;
import org.actus.attributes.ContractModel;
import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.events.ContractEvent;
import org.actus.events.EventFactory;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.pam.POF_PRD_PAM;
import org.actus.functions.pam.POF_TD_PAM;
import org.actus.functions.pam.STF_PRD_PAM;
import org.actus.functions.pam.STF_TD_PAM;
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
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the Swap payoff algorithm
 *
 * @see <a href="http://www.projectactus.org/"></a>
 */
public final class Swap {

    // forward projection of the entire lifecycle of the contract
    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        // extract parent attributes
        ContractModel parent = model.getAs("Parent");

        // compute child 1 and child 2 events
        ArrayList<ContractEvent> child1 = ContractType.lifecycle(analysisTimes,model.getAs("Child1"),riskFactorModel);
        ArrayList<ContractEvent> child2 = ContractType.lifecycle(analysisTimes,model.getAs("Child2"),riskFactorModel);

        // merge child events
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
        }

        // compute parent events

        // init state space and day counter
        StateSpace states = new StateSpace();
        states.lastEventTime = parent.getAs("StatusDate");
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", null);

        // purchase
        if (!CommonUtils.isNull(parent.getAs("PurchaseDate"))) {
            ContractEvent purchase = EventFactory.createEvent(parent.getAs("PurchaseDate"), StringUtils.EventType_PRD, parent.getAs("Currency"), new POF_PRD_SWAPS(), new STF_PRD_STK());
            events.removeIf(e -> e.compareTo(purchase) == -1); // remove all pre-purchase events
            purchase.eval(states,parent,riskFactorModel,dayCount,null);
            events.add(purchase);
        }
        // termination
        if (!CommonUtils.isNull(parent.getAs("TerminationDate"))) {
            ContractEvent termination =
                    EventFactory.createEvent(parent.getAs("TerminationDate"), StringUtils.EventType_TD, parent.getAs("Currency"), new POF_TD_SWAPS(), new STF_TD_STK());
            events.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
            states.lastEventTime = events.get(events.size()-1).time();
            termination.eval(states,parent,riskFactorModel,dayCount,null);
            events.add(termination);
        }

        // remove all pre-status date events
        ContractEvent sdEvent = EventFactory.createEvent(parent.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null, null);
        events.removeIf(e -> e.compareTo(sdEvent) == -1);

        // return events
        return events;
    }

    // forward projection of the payoff of the contract
    public static ArrayList<ContractEvent> payoff(Set<LocalDateTime> analysisTimes,
                                                  ContractModelProvider model,
                                                  RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        return Swap.lifecycle(analysisTimes,model,riskFactorModel).stream().filter(ev->StringUtils.TransactionalEvents.contains(ev.type())).collect(Collectors.toCollection(ArrayList::new));
    }

    // compute the contract schedule
    public static ArrayList<ContractEvent> schedule(ContractModelProvider model) throws AttributeConversionException {

        // extract parent attributes
        ContractModel parent = model.getAs("Parent");

        // compute child 1 and child 2 events
        ArrayList<ContractEvent> child1 = ContractType.schedule(model.getAs("Child1"));
        ArrayList<ContractEvent> child2 = ContractType.schedule(model.getAs("Child2"));

        // merge child events
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
        }

        // remove all pre-status date events
        ContractEvent sdEvent = EventFactory.createEvent(parent.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null, null);
        events.removeIf(e -> e.compareTo(sdEvent) == -1);

        // return events
        return events;
    }

    // compute next n events
    public static ArrayList<ContractEvent> next(int n,
                                                ContractModelProvider model) throws AttributeConversionException {
        // extract parent attributes
        ContractModel parent = model.getAs("Parent");

        // compute child 1 and child 2 events
        ArrayList<ContractEvent> child1 = ContractType.next(n,model.getAs("Child1"));
        ArrayList<ContractEvent> child2 = ContractType.next(n,model.getAs("Child2"));

        // merge child events
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
        }

        // remove all pre-status date events
        ContractEvent sdEvent = EventFactory.createEvent(parent.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null, null);
        events.removeIf(e -> e.compareTo(sdEvent) == -1);

        // return events
        return events;
    }

    // compute next n non-contingent events
    public static ArrayList<ContractEvent> next(Period within,
                                                ContractModelProvider model) throws AttributeConversionException {
        // extract parent attributes
        ContractModel parent = model.getAs("Parent");

        // compute child 1 and child 2 events
        ArrayList<ContractEvent> child1 = ContractType.next(within,model.getAs("Child1"));
        ArrayList<ContractEvent> child2 = ContractType.next(within,model.getAs("Child2"));

        // merge child events
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
        }

        // remove all pre-status date events
        ContractEvent sdEvent = EventFactory.createEvent(parent.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null, null);
        events.removeIf(e -> e.compareTo(sdEvent) == -1);

        // return events
        return events;
    }

    // private method that allows creating a "netting" event from two events to be netted
    private static ContractEvent nettingEvent(ContractEvent e1, ContractEvent e2) {
        ContractEvent netting = EventFactory.createEvent(e1.time(), e1.type(), e1.currency(), new POF_NET_SWAPS(e1,e2), new STF_NET_SWAPS(e1,e2));
        netting.eval(null,null,null,null,null);
        return(netting);
    }

}

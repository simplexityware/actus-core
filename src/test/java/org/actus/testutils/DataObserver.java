package org.actus.testutils;

import org.actus.events.ContractEvent;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.attributes.ContractModelProvider;
import org.actus.states.StateSpace;
import org.actus.types.ContractReference;
import org.actus.util.CommonUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.stream.Collectors;


public class DataObserver implements RiskFactorModelProvider {
    HashMap<String,HashMap<LocalDateTime,Double>> multiSeries = new HashMap<String,HashMap<LocalDateTime,Double>>();
    HashMap<String, List<ContractEvent>> eventsObserved = new HashMap<>();
    
    public Set<String> keys() {
        return multiSeries.keySet();
    }

    public void add(String symbol, HashMap<LocalDateTime,Double> series) {
        multiSeries.put(symbol,series);
    }

    public double stateAt(String id, LocalDateTime time, StateSpace contractStates,
            ContractModelProvider contractAttributes) {
        return multiSeries.get(id).get(time);
    }

    public void setEventsObserved(List<ContractEvent> observedEvents){
        this.eventsObserved.put(observedEvents.get(0).getContractID(),observedEvents);
    }

    public Set<ContractEvent> events(ContractModelProvider model){
        List<String> contractIdentifiers = model.<List<ContractReference>>getAs("ContractStructure").stream().map(c -> c.getContractAttribute("")).collect(Collectors.toList());
        HashSet<ContractEvent> events = new HashSet<>();
        contractIdentifiers.forEach(s -> {
            if(!CommonUtils.isNull(this.eventsObserved.get(s))){
                events.addAll(this.eventsObserved.get(s));
            }
        });
        return events;
    }
}

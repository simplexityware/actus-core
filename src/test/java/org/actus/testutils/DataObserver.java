package org.actus.testutils;

import org.actus.externals.RiskFactorModelProvider;
import org.actus.attributes.ContractModelProvider;
import org.actus.states.StateSpace;

import java.util.Set;
import java.util.HashMap;
import java.time.LocalDateTime;


public class DataObserver implements RiskFactorModelProvider {
    HashMap<String,HashMap<LocalDateTime,Double>> multiSeries = new HashMap<String,HashMap<LocalDateTime,Double>>();
    
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
}

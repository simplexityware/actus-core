package org.actus.testutils;

import java.util.Map;
import java.util.List;

public class TestData {
    String identifier;
    Map<String, Object> terms;
    String tMax;
    Map<String, ObservedDataSet> dataObserved;
    List<String> eventsObserved;
    List<ResultSet> results;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Map<String, Object> getTerms() {
        return terms;
    }

    public void setTerms(Map<String, Object> terms) {
        this.terms = terms;
    }

    public String gettMax() {
        return tMax;
    }

    public void settMax(String tMax) {
        this.tMax = tMax;
    }

    public Map<String, ObservedDataSet> getDataObserved() {
        return dataObserved;
    }

    public void setDataObserved(Map<String, ObservedDataSet> dataObserved) {
        this.dataObserved = dataObserved;
    }

    public List<String> getEventsObserved() {
        return eventsObserved;
    }

    public void setEventsObserved(List<String> eventsObserved) {
        this.eventsObserved = eventsObserved;
    }

    public List<ResultSet> getResults() {
        return results;
    }

    public void setResults(List<ResultSet> results) {
        this.results = results;
    }
}
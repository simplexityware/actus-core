/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import org.actus.attributes.ContractModel;
import org.actus.events.ContractEvent;
import org.actus.states.StateSpace;
import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;

import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.DynamicTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContractTest {
    
    static class ObservedDataPoint {
        String timestamp;
        Double value;

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }
    }

    static class ObservedDataSet {
        String identifier;
        List<ObservedDataPoint> data;

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public List<ObservedDataPoint> getData() {
            return data;
        }

        public void setData(List<ObservedDataPoint> data) {
            this.data = data;
        }

    }

    static class ResultSet {
        LocalDateTime eventDate;
        String eventType;
        Double payoff;
        Double notionalPrincipal;
        Double nominalInterestRate;
        Double accruedInterest;

        public String getEventDate() {
            return eventDate.toString();
        }

        public void setEventDate(String eventDate) {
            this.eventDate = LocalDateTime.parse(eventDate);
        }

        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        public Double getPayoff() {
            return payoff;
        }

        public void setPayoff(Double payoff) {
            this.payoff = payoff;
        }

        public Double getNotionalPrincipal() {
            return notionalPrincipal;
        }

        public void setNotionalPrincipal(Double notionalPrincipal) {
            this.notionalPrincipal = notionalPrincipal;
        }

        public Double getNominalInterestRate() {
            return nominalInterestRate;
        }

        public void setNominalInterestRate(Double nominalInterestRate) {
            this.nominalInterestRate = nominalInterestRate;
        }

        public Double getAccruedInterest() {
            return accruedInterest;
        }

        public void setAccruedInterest(Double accruedInterest) {
            this.accruedInterest = accruedInterest;
        }

        public String toString() {
            return "Date: " + eventDate + ", " +
                "Type: " + eventType + ", " +
                "Payoff: " + payoff + ", " +
                "Notinoal: " + notionalPrincipal + ", " +
                "Rate: " + nominalInterestRate + ", " +
                "Accrued: " + accruedInterest;
        }
    }

    static class TestData {
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

    class MarketModel implements RiskFactorModelProvider {
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

    @TestFactory
    public Stream<DynamicTest> test() {
        String testFile = "./src/test/resources/actus/actus-tests-PAM.json";

        // read tests from file
        Map<String, TestData> tests = readTests(testFile);

        // get ids of tests
        Set<String> testIds = tests.keySet();

        // go through test-id and perform test

        return testIds.stream().map(testId -> {

            // extract test for test ID
            TestData test = tests.get(testId);

            // create market model from data
            List<ObservedDataSet> dataObserved = new ArrayList(test.getDataObserved().values());
            MarketModel observer = createObserver(dataObserved);

            // create contract model from data
            ContractModel terms = createModel(tests.get(testId).getTerms());

            // compute and evaluate schedule
            ArrayList<ContractEvent> schedule = ContractType.schedule(terms.getAs("MaturityDate"), terms);
            schedule = ContractType.apply(schedule, terms, observer);
        
            // transform schedule to event list and return
            List<ResultSet> computedResults = schedule.stream().map(e -> { 
                ResultSet results = new ResultSet();
                results.setEventDate(e.time().toString());
                results.setEventType(e.type());
                results.setPayoff(e.payoff());
                results.setNotionalPrincipal(e.states().notionalPrincipal);
                results.setNominalInterestRate(e.states().nominalInterestRate);
                results.setAccruedInterest(e.states().accruedInterest);
                return results;
            }).collect(Collectors.toList());

            // extract test results
            List<ResultSet> expectedResults = test.getResults();
            for(int i=0; i<expectedResults.size(); i++) {
                System.out.println("expectedResults: " + expectedResults.get(i).toString());
                System.out.println("computedResults: " + computedResults.get(i).toString());
            }
            
            // create dynamic test
            return DynamicTest.dynamicTest("Test: " + testId,
                () -> assertEquals(expectedResults, computedResults));
        });
    }

    private ContractModel createModel(Map<String,Object> data) {
        // convert json terms object to a java map (required input for actus model parsing)
        Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {

            System.out.println(entry.getKey() + ":" + entry.getValue());

            // capitalize input json keys as required in contract model parser
            map.put(entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1), entry.getValue().toString());
        }

        // parse attributes
        return ContractModel.parse(map);   
    }

    private MarketModel createObserver(List<ObservedDataSet> data) {
        MarketModel observer = new MarketModel();

        data.forEach(entry -> {
            String symbol = entry.getIdentifier();
            HashMap<LocalDateTime,Double> series = new HashMap<LocalDateTime,Double>();
            entry.getData().forEach(obs -> {
                series.put(LocalDateTime.parse(obs.getTimestamp()), obs.getValue());
            });
            observer.add(symbol,series);
        });

        return observer;
    }

    private Map<String, TestData> readTests(String file) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, TestData> tests = new HashMap();

        try {
            // convert JSON string to Map
            tests = mapper.readValue(Paths.get(file).toFile(), new TypeReference<Map<String, TestData>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tests;
    }
    
}
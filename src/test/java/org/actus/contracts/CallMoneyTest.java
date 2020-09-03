/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import org.actus.testutils.ContractTestUtils;
import org.actus.testutils.TestData;
import org.actus.testutils.ObservedDataSet;
import org.actus.testutils.ResultSet;
import org.actus.testutils.DataObserver;
import org.actus.attributes.ContractModel;
import org. actus.events.ContractEvent;

import java.time.LocalDateTime;
import java.util.*;

import java.util.stream.Stream;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.DynamicTest;


public class CallMoneyTest {
    @TestFactory
    public Stream<DynamicTest> test() {
        String testFile = "./src/test/resources/actus/actus-tests-clm.json";

        // read tests from file
        Map<String, TestData> tests = ContractTestUtils.readTests(testFile);

        // get ids of tests
        Set<String> testIds = tests.keySet();

        // go through test-id and perform test
        // Note: skipping tests with currently unsupported features
        return testIds.stream().filter(testId -> !Arrays.asList("clm07","clm08","clm09","clm13","clm14").contains(testId)).map(testId -> {
            // extract test for test ID
            TestData test = tests.get(testId);

            // create market model from data
            List<ObservedDataSet> dataObserved = new ArrayList<ObservedDataSet>(test.getDataObserved().values());
            DataObserver observer = ContractTestUtils.createObserver(dataObserved);
          
            // create contract model from data
            ContractModel terms = ContractTestUtils.createModel(tests.get(testId).getTerms());

            // compute and evaluate schedule
            ArrayList<ContractEvent> schedule = CallMoney.schedule(LocalDateTime.parse(test.getto()), terms);
            schedule = CallMoney.apply(schedule, terms, observer);
        
            // transform schedule to event list and return
            List<ResultSet> computedResults = schedule.stream().map(e -> { 
                ResultSet results = new ResultSet();
                results.setEventDate(e.eventTime().toString());
                results.setEventType(e.eventType());
                results.setPayoff(e.payoff());
                results.setCurrency(e.currency());
                results.setNotionalPrincipal(e.states().notionalPrincipal);
                results.setNominalInterestRate(e.states().nominalInterestRate);
                results.setAccruedInterest(e.states().accruedInterest);
                if(testId.equals("clm13")) System.out.println(results);
                return results;
            }).collect(Collectors.toList());

            // extract test results
            List<ResultSet> expectedResults = test.getResults();
            
            // round results to available precision
            computedResults.forEach(result -> result.roundTo(10));
            expectedResults.forEach(result -> result.roundTo(10));

            // create dynamic test
            return DynamicTest.dynamicTest("Test: " + testId,
                () -> Assertions.assertArrayEquals(expectedResults.toArray(), computedResults.toArray()));
        });
    }
}

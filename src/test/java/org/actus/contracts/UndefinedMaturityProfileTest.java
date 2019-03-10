/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import org.actus.attributes.ContractModel;
import org.actus.attributes.ContractModelProvider;
import org.actus.events.ContractEvent;
import org.actus.events.EventFactory;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.pam.POF_AD_PAM;
import org.actus.functions.pam.POF_IPCI_PAM;
import org.actus.functions.pam.STF_AD_PAM;
import org.actus.functions.pam.STF_IPCI_PAM;
import org.actus.functions.ump.POF_PR_UMP;
import org.actus.functions.ump.STF_PR_UMP;
import org.actus.states.StateSpace;
import org.actus.time.ScheduleFactory;
import org.actus.util.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.IntStream;

public class UndefinedMaturityProfileTest {
    
    class RiskFactorModel_NoReplication implements RiskFactorModelProvider {
        public Set<String> keys() {
            Set<String> keys = new HashSet<String>();
            return keys;
        }
        
        public double stateAt(String id,LocalDateTime time,StateSpace contractStates,ContractModelProvider contractAttributes) {
            return Math.random();
        }
    }

    // risk factor model with simple replication payments
    class RiskFactorModel implements RiskFactorModelProvider {
        public Set<String> keys() {
            Set<String> keys = new HashSet<String>();
            return keys;
        }

        // here, we create a cashflow replication pattern in form of PR events
        public Set<ContractEvent> events(ContractModelProvider model) {
            HashSet<ContractEvent> events = new HashSet<>();

            // periods at which cashflows are modelled
            Period periods[] = new Period[] {
              Period.ofWeeks(1),
              Period.ofMonths(1),
              Period.ofMonths(6),
              Period.ofYears(1),
              Period.ofYears(5)
            };

            // replication payments
            double payments[] = new double[] {
                    0.1 * model.<Double>getAs("NotionalPrincipal"), // inflow
                    -0.2 * model.<Double>getAs("NotionalPrincipal"), // outflow
                    -0.5 * model.<Double>getAs("NotionalPrincipal"), // outtflow
                    0.3 * model.<Double>getAs("NotionalPrincipal"), // inflow
                    1.3 * model.<Double>getAs("NotionalPrincipal") // inflow
            };

            // convert to events
            events = IntStream.of(0,1,2,3,4).collect(
                    HashSet::new, (s,i) -> {
                        ContractEvent ev = EventFactory.createEvent(
                                model.<LocalDateTime>getAs("StatusDate").plus(periods[i]),
                                StringUtils.EventType_PR,model.getAs("Currency"),
                                new POF_PR_UMP(payments[i]),
                                new STF_PR_UMP(payments[i]));
                        s.add(ev);
                    }, Set::addAll);

            return events;
        }

        public double stateAt(String id,LocalDateTime time,StateSpace contractStates,ContractModelProvider contractAttributes) {
            return Math.random();
        }
    }
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void test_UMP_lifecycle_MandatoryAttributes() {
        thrown = ExpectedException.none();
        // define attributes
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "UMP");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPL");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = UndefinedMaturityProfile.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_UMP_lifecycle_withoutReplication() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "UMP");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        RiskFactorModel_NoReplication riskFactors = new RiskFactorModel_NoReplication();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = UndefinedMaturityProfile.lifecycle(analysisTimes,model,riskFactors);
    }
    
    
    @Test
    public void test_UMP_lifecycle_withIPCL() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "UMP");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfInterestPayment","1Q-");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = UndefinedMaturityProfile.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_UMP_lifecycle_withIPCLandIPANX() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "UMP");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleAnchorDateOfInterestPayment","2016-06-01T00:00:00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = UndefinedMaturityProfile.lifecycle(analysisTimes,model,riskFactors);
    }

    @Test
    public void test_UMP_lifecycle_withIP_withRRCL() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "UMP");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("MarketObjectCodeOfRateReset","DummyRate");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = UndefinedMaturityProfile.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_UMP_lifecycle_withIP_withRRCLandRRANX() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "UMP");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("MarketObjectCodeOfRateReset","DummyRate");
        map.put("CycleAnchorDateOfRateReset","2016-06-01T00:00:00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = UndefinedMaturityProfile.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_UMP_lifecycle_withIP_withRR_withFPwhereA() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "UMP");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("MarketObjectCodeOfRateReset","DummyRate");
        map.put("CycleOfFee","1Q-");
        map.put("FeeBasis","A");
        map.put("FeeRate","100");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = UndefinedMaturityProfile.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_UMP_lifecycle_withIP_withRR_withFPwhereN() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "UMP");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("MarketObjectCodeOfRateReset","DummyRate");
        map.put("CycleOfFee","1Q-");
        map.put("FeeBasis","N");
        map.put("FeeRate","0.01");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = UndefinedMaturityProfile.lifecycle(analysisTimes,model,riskFactors);
        System.out.println(events);
    }
    
    @Test
    public void test_UMP_lifecycle_withIP_withRR_withFP_withCalendar() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "UMP");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("MarketObjectCodeOfRateReset","DummyRate");
        map.put("CycleOfFee","1Q-");
        map.put("FeeBasis","N");
        map.put("FeeRate","0.01");
        map.put("Calendar","MondayToFriday");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = UndefinedMaturityProfile.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_UMP_lifecycle_withIP_withRR_withFP_withCalendar_withBDC_whereSCF() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "UMP");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("MarketObjectCodeOfRateReset","DummyRate");
        map.put("CycleOfFee","1Q-");
        map.put("FeeBasis","N");
        map.put("FeeRate","0.01");
        map.put("Calendar","MondayToFriday");
        map.put("BusinessDayCalendar","SCF");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = UndefinedMaturityProfile.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_UMP_lifecycle_withIP_withRR_withFP_withCalendar_withBDC_whereCSP() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "UMP");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("MarketObjectCodeOfRateReset","DummyRate");
        map.put("CycleOfFee","1Q-");
        map.put("FeeBasis","N");
        map.put("FeeRate","0.01");
        map.put("Calendar","MondayToFriday");
        map.put("BusinessDayCalendar","CSP");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = UndefinedMaturityProfile.lifecycle(analysisTimes,model,riskFactors);
    }
    
    
    @Test
    public void test_UMP_lifecycle_withRR_withFP_withCalendar_withBDC_whereCSP() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "UMP");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("XDayNotice", "6M");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("MarketObjectCodeOfRateReset","DummyRate");
        map.put("CycleOfFee","1Q-");
        map.put("FeeBasis","N");
        map.put("FeeRate","0.01");
        map.put("Calendar","MondayToFriday");
        map.put("BusinessDayCalendar","CSP");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = UndefinedMaturityProfile.lifecycle(analysisTimes,model,riskFactors);
    }

    @Test
    public void test_UMP_lifecycle_withIP_withRR_withFP_withCalendar_withTD() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "UMP");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("MarketObjectCodeOfRateReset","DummyRate");
        map.put("CycleOfFee","1Q-");
        map.put("FeeBasis","N");
        map.put("FeeRate","0.01");
        map.put("Calendar","MondayToFriday");
        map.put("TerminationDate", "2017-01-22T00:00:00");
        map.put("PriceAtTerminationDate", "1000.0");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = UndefinedMaturityProfile.lifecycle(analysisTimes,model,riskFactors);
    }

    @Test
    public void test_UMP_lifecycle_withIP_withRR_withFP_withCalendar_withBDC_withMultipleAnalysisTimes() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "UMP");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("XDayNotice", "6M");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("MarketObjectCodeOfRateReset","DummyRate");
        map.put("CycleOfFee","1Q-");
        map.put("FeeBasis","N");
        map.put("FeeRate","0.01");
        map.put("Calendar","MondayToFriday");
        map.put("BusinessDayCalendar","CSP");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        analysisTimes.add(LocalDateTime.parse("2016-04-01T00:00:00"));
        analysisTimes.add(LocalDateTime.parse("2016-07-01T00:00:00"));
        analysisTimes.add(LocalDateTime.parse("2016-09-01T00:00:00"));
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = UndefinedMaturityProfile.lifecycle(analysisTimes,model,riskFactors);
        //System.out.println(events);
    }

    @Test
    public void test_UMP_payoff_withIP_withRR_withFP_withCalendar_withBDC_withMultipleAnalysisTimes() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "UMP");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("XDayNotice", "6M");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("MarketObjectCodeOfRateReset","DummyRate");
        map.put("CycleOfFee","1Q-");
        map.put("FeeBasis","N");
        map.put("FeeRate","0.01");
        map.put("Calendar","MondayToFriday");
        map.put("BusinessDayCalendar","CSP");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        analysisTimes.add(LocalDateTime.parse("2016-04-01T00:00:00"));
        analysisTimes.add(LocalDateTime.parse("2016-07-01T00:00:00"));
        analysisTimes.add(LocalDateTime.parse("2016-09-01T00:00:00"));
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = UndefinedMaturityProfile.payoff(analysisTimes,model,riskFactors);
        //System.out.println(events);
    }

    @Test
    public void test_UMP_next_within_withIP_withRR_withFP_withCalendar_withBDC() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "UMP");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("XDayNotice", "6M");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("MarketObjectCodeOfRateReset","DummyRate");
        map.put("CycleOfFee","1Q-");
        map.put("FeeBasis","N");
        map.put("FeeRate","0.01");
        map.put("Calendar","MondayToFriday");
        map.put("BusinessDayCalendar","CSP");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = UndefinedMaturityProfile.next(Period.ofDays(10),model);
        //System.out.println(events);
    }

    @Test
    public void test_UMP_schedule_withIP_withRR_withFP_withCalendar_withBDC() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "UMP");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("XDayNotice", "6M");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("MarketObjectCodeOfRateReset","DummyRate");
        map.put("CycleOfFee","1Q-");
        map.put("FeeBasis","N");
        map.put("FeeRate","0.01");
        map.put("Calendar","MondayToFriday");
        map.put("BusinessDayCalendar","CSP");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = UndefinedMaturityProfile.schedule(model);
        //System.out.println(events);
    }

    @Test
    public void test_UMP_apply_AE() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "UMP");
        map.put("StatusDate", "2016-02-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("XDayNotice", "1M");
        map.put("MaturityDate", "2017-01-02T00:00:00");
        map.put("CycleOfInterestPayment","1Q-");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // create six analysis (monitoring) events
        Set<ContractEvent> events = EventFactory.createEvents(
                ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
                StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM());
        // apply events
        StateSpace postStates = UndefinedMaturityProfile.apply(events,model);
        System.out.print(
                "Last applied event: " + postStates.lastEventTime + "\n" +
                        "Post events nominal value: " + postStates.nominalValue + "\n" +
                        "Post events nominal rate: " + postStates.nominalRate + "\n" +
                        "Post events nominal accrued: " + postStates.nominalAccrued);

    }

    @Test
    public void test_UMP_apply_IP_PR() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "UMP");
        map.put("StatusDate", "2016-06-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("XDayNotice", "1M");
        map.put("MaturityDate", "2017-01-02T00:00:00");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("AccruedInterest", "2.4");

        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // create six interest capitalization events and add together with principal replication payments
        RiskFactorModel riskFactors = new RiskFactorModel();
        Set<ContractEvent> events = EventFactory.createEvents(
                ScheduleFactory.createSchedule(model.<LocalDateTime>getAs("StatusDate").plusMonths(1),model.<LocalDateTime>getAs("StatusDate").plusYears(4),model.getAs("CycleOfInterestPayment"),"SD"),
                StringUtils.EventType_IPCI, model.getAs("Currency"), new POF_IPCI_PAM(), new STF_IPCI_PAM());
        events.addAll(riskFactors.events(model));
        // apply events
        StateSpace postStates = UndefinedMaturityProfile.apply(events,model);
        System.out.print(
                "Last applied event: " + postStates.lastEventTime + "\n" +
                        "Post events nominal value: " + postStates.nominalValue + "\n" +
                        "Post events nominal rate: " + postStates.nominalRate + "\n" +
                        "Post events nominal accrued: " + postStates.nominalAccrued);

    }
}

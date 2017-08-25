/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import org.actus.attributes.ContractModel;
import org.actus.events.ContractEvent;
import org.actus.states.StateSpace;
import org.actus.externals.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class CallMoneyTest {
    
    class MarketModel implements RiskFactorModelProvider {
        public Set<String> keys() {
            Set<String> keys = new HashSet<String>();
            return keys;
        }
        
        public Set<LocalDateTime> times(String id) {
            Set<LocalDateTime> times = new HashSet<LocalDateTime>();
            return times;
        }
        
        public double stateAt(String id,LocalDateTime time,StateSpace contractStates,ContractModelProvider contractAttributes) {
            return 0.0;    
        }
    }
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void test_CLM_MandatoryAttributes() {
        thrown = ExpectedException.none();
        // define attributes
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "CLM");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("XDayNotice", "1M");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // evalAll PAM contract
        ArrayList<ContractEvent> events = CallMoney.evalAll(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_CLM_withMD() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "CLM");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("XDayNotice", "1M");
        map.put("MaturityDate", "2016-01-15T00:00:00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // evalAll PAM contract
        ArrayList<ContractEvent> events = CallMoney.evalAll(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_CLM_withMD_whereMDgreaterXDN() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "CLM");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("XDayNotice", "1M");
        map.put("MaturityDate", "2017-01-02T00:00:00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // evalAll PAM contract
        ArrayList<ContractEvent> events = CallMoney.evalAll(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_CLM_withMD_withIPCL() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "CLM");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
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
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // evalAll PAM contract
        ArrayList<ContractEvent> events = CallMoney.evalAll(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_CLM_withMD_withIPCLandIPANX() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "CLM");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("XDayNotice", "1M");
        map.put("MaturityDate", "2017-01-02T00:00:00");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleAnchorDateOfInterestPayment","2016-06-01T00:00:00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // evalAll PAM contract
        ArrayList<ContractEvent> events = CallMoney.evalAll(analysisTimes,model,riskFactors);
    }

    @Test
    public void test_CLM_withMD_withIP_withRRCL() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "CLM");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("XDayNotice", "1M");
        map.put("MaturityDate", "2017-01-02T00:00:00");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // evalAll PAM contract
        ArrayList<ContractEvent> events = CallMoney.evalAll(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_CLM_withMD_withIP_withRRCLandRRANX() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "CLM");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("XDayNotice", "1M");
        map.put("MaturityDate", "2017-01-02T00:00:00");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("CycleAnchorDateOfRateReset","2016-06-01T00:00:00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // evalAll PAM contract
        ArrayList<ContractEvent> events = CallMoney.evalAll(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_CLM_withMD_withIP_withRR_withFPwhereA() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "CLM");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("XDayNotice", "1M");
        map.put("MaturityDate", "2017-01-02T00:00:00");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("CycleOfFee","1Q-");
        map.put("FeeBasis","A");
        map.put("FeeRate","100");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // evalAll PAM contract
        ArrayList<ContractEvent> events = CallMoney.evalAll(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_CLM_withMD_withIP_withRR_withFPwhereN() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "CLM");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("XDayNotice", "1M");
        map.put("MaturityDate", "2017-01-02T00:00:00");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("CycleOfFee","1Q-");
        map.put("FeeBasis","N");
        map.put("FeeRate","0.01");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // evalAll PAM contract
        ArrayList<ContractEvent> events = CallMoney.evalAll(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_CLM_withMD_withIP_withRR_withFP_withCalendar() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "CLM");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("XDayNotice", "1M");
        map.put("MaturityDate", "2017-01-02T00:00:00");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
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
        MarketModel riskFactors = new MarketModel();
        // evalAll PAM contract
        ArrayList<ContractEvent> events = CallMoney.evalAll(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_CLM_withMD_withIP_withRR_withFP_withCalendar_withBDC_whereSCF() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "CLM");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("XDayNotice", "1M");
        map.put("MaturityDate", "2017-01-02T00:00:00");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
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
        MarketModel riskFactors = new MarketModel();
        // evalAll PAM contract
        ArrayList<ContractEvent> events = CallMoney.evalAll(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_CLM_withMD_withIP_withRR_withFP_withCalendar_withBDC_whereCSP() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "CLM");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("XDayNotice", "1M");
        map.put("MaturityDate", "2017-01-02T00:00:00");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
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
        MarketModel riskFactors = new MarketModel();
        // evalAll PAM contract
        ArrayList<ContractEvent> events = CallMoney.evalAll(analysisTimes,model,riskFactors);
    }
    
    
    @Test
    public void test_CLM_withIP_withRR_withFP_withCalendar_withBDC_whereCSP() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "CLM");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("XDayNotice", "6M");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
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
        MarketModel riskFactors = new MarketModel();
        // evalAll PAM contract
        ArrayList<ContractEvent> events = CallMoney.evalAll(analysisTimes,model,riskFactors);
    }
    
    
    @Test
    public void test_CLM_withMD_withIP_withRR_withFP_withCalendar_withBDC_withMultipleAnalysisTimes() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "CLM");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("XDayNotice", "6M");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
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
        MarketModel riskFactors = new MarketModel();
        // evalAll PAM contract
        ArrayList<ContractEvent> events = CallMoney.evalAll(analysisTimes,model,riskFactors);
        //System.out.println(events);
    }
}

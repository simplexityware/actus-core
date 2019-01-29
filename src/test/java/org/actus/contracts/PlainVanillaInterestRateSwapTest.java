/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import org.actus.attributes.ContractModel;
import org.actus.events.ContractEvent;
import org.actus.events.EventFactory;
import org.actus.functions.pam.POF_AD_PAM;
import org.actus.functions.pam.STF_AD_PAM;
import org.actus.functions.swppv.POF_IPFix_SWPPV;
import org.actus.functions.swppv.POF_IPFloat_SWPPV;
import org.actus.functions.swppv.STF_IPFix_SWPPV;
import org.actus.functions.swppv.STF_IPFloat_SWPPV;
import org.actus.states.StateSpace;
import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;

import java.time.Period;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.LocalDateTime;

import org.actus.time.ScheduleFactory;
import org.actus.util.StringUtils;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class PlainVanillaInterestRateSwapTest {
    
    class MarketModel implements RiskFactorModelProvider {
        public Set<String> keys() {
            Set<String> keys = new HashSet<String>();
            return keys;
        }
        
        public double stateAt(String id,LocalDateTime time,StateSpace contractStates,ContractModelProvider contractAttributes) {
            return 0.0;    
        }
    }
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void test_SWPPV_lifecycle_MandatoryAttributes() {
        thrown = ExpectedException.none();
        // define attributes
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("NominalInterestRate2", "0.005");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfRateReset", "1Q-");
        map.put("MarketObjectCodeOfRateReset", "RefRateXY");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.lifecycle(analysisTimes,model,riskFactors);
        System.out.println(events);
    }
    
    
    @Test
    public void test_SWPPV_lifecycle_withIPCL() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("NominalInterestRate2", "0.005");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfRateReset", "1Q-");
        map.put("MarketObjectCodeOfRateReset", "RefRateXY");
        map.put("CycleOfInterestPayment","1Q-");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_SWPPV_lifecycle_withIPCLandIPANX() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("NominalInterestRate2", "0.005");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfRateReset", "1Q-");
        map.put("MarketObjectCodeOfRateReset", "RefRateXY");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleAnchorDateOfInterestPayment","2016-06-01T00:00:00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_SWPPV_lifecycle_withIP_withRRANX() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("NominalInterestRate2", "0.005");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfRateReset", "1Q-");
        map.put("MarketObjectCodeOfRateReset", "RefRateXY");
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleAnchorDateOfRateReset","2016-06-01T00:00:00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_SWPPV_lifecycle_withIP_withSTDwhereD() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("NominalInterestRate2", "0.005");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfRateReset", "1Q-");
        map.put("MarketObjectCodeOfRateReset", "RefRateXY");
        map.put("DeliverySettlement","D");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_SWPPV_lifecycle_withIP_withSTDwhereS() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("NominalInterestRate2", "0.005");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfRateReset", "1Q-");
        map.put("MarketObjectCodeOfRateReset", "RefRateXY");
        map.put("DeliverySettlement","S");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_SWPPV_lifecycle_withIP_withSTDwhereD_withPRD() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("NominalInterestRate2", "0.005");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2015-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfRateReset", "1Q-");
        map.put("MarketObjectCodeOfRateReset", "RefRateXY");
        map.put("DeliverySettlement","S");
        map.put("PurchaseDate", "2016-01-02T00:00:00");
        map.put("PriceAtPurchaseDate", "50.0");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_SWPPV_lifecycle_withIP_withSTDwhereD_withPRD_withTD() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("NominalInterestRate2", "0.005");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2015-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfRateReset", "1Q-");
        map.put("MarketObjectCodeOfRateReset", "RefRateXY");
        map.put("DeliverySettlement","S");
        map.put("PurchaseDate", "2016-01-02T00:00:00");
        map.put("PriceAtPurchaseDate", "50.0");
        map.put("TerminationDate", "2016-01-03T00:00:00");
        map.put("PriceAtTerminationDate", "60.0");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_SWPPV_lifecycle_withIP_withSTDwhereS_withPRD_withTD() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("NominalInterestRate2", "0.005");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2015-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfRateReset", "1Q-");
        map.put("MarketObjectCodeOfRateReset", "RefRateXY");
        map.put("DeliverySettlement","D");
        map.put("PurchaseDate", "2016-01-02T00:00:00");
        map.put("PriceAtPurchaseDate", "50.0");
        map.put("TerminationDate", "2016-01-03T00:00:00");
        map.put("PriceAtTerminationDate", "60.0");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_SWPPV_lifecycle_withIP_withSTDwhereD_withPRD_withMultipleAnalysisTimes() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("NominalInterestRate2", "0.005");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2015-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfRateReset", "1Q-");
        map.put("MarketObjectCodeOfRateReset", "RefRateXY");
        map.put("DeliverySettlement","D");
        map.put("PurchaseDate", "2016-01-02T00:00:00");
        map.put("PriceAtPurchaseDate", "50.0");
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
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.lifecycle(analysisTimes,model,riskFactors);
        //System.out.println(events);
    }

    @Test
    public void test_SWPPV_lifecycle_withIP_withSTDwhereS_withPRD_withMultipleAnalysisTimes() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("NominalInterestRate2", "0.005");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2015-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfRateReset", "1Q-");
        map.put("MarketObjectCodeOfRateReset", "RefRateXY");
        map.put("DeliverySettlement","S");
        map.put("PurchaseDate", "2016-01-02T00:00:00");
        map.put("PriceAtPurchaseDate", "50.0");
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
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.lifecycle(analysisTimes,model,riskFactors);
        //System.out.println(events);
    }

    @Test
    public void test_SWPPV_payoff_withIP_withSTDwhereS_withPRD_withMultipleAnalysisTimes() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("NominalInterestRate2", "0.005");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2015-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfRateReset", "1Q-");
        map.put("MarketObjectCodeOfRateReset", "RefRateXY");
        map.put("DeliverySettlement","S");
        map.put("PurchaseDate", "2016-01-02T00:00:00");
        map.put("PriceAtPurchaseDate", "50.0");
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
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.payoff(analysisTimes,model,riskFactors);
        //System.out.println(events);
    }

    @Test
    public void test_SWPPV_next_within_withIP_withSTDwhereS_withPRD() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("NominalInterestRate2", "0.005");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2015-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfRateReset", "1Q-");
        map.put("MarketObjectCodeOfRateReset", "RefRateXY");
        map.put("DeliverySettlement","S");
        map.put("PurchaseDate", "2016-01-02T00:00:00");
        map.put("PriceAtPurchaseDate", "50.0");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.next(Period.ofDays(10),model);
        //System.out.println(events);
    }

    @Test
    public void test_SWPPV_next_within_fromSD_withIP_withSTDwhereS_withPRD() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("NominalInterestRate2", "0.005");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2015-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfRateReset", "1Q-");
        map.put("MarketObjectCodeOfRateReset", "RefRateXY");
        map.put("DeliverySettlement","S");
        map.put("PurchaseDate", "2016-01-02T00:00:00");
        map.put("PriceAtPurchaseDate", "50.0");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.next(Period.ofWeeks(1),model);
        //System.out.println(events);
    }

    @Test
    public void test_SWPPV_schedule_withIP_withSTDwhereS_withPRD() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("NominalInterestRate2", "0.005");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2015-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfRateReset", "1Q-");
        map.put("MarketObjectCodeOfRateReset", "RefRateXY");
        map.put("DeliverySettlement","S");
        map.put("PurchaseDate", "2016-01-02T00:00:00");
        map.put("PriceAtPurchaseDate", "50.0");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.schedule(model);
        //System.out.println(events);
    }

    @Test
    public void test_SWPPV_apply_AE() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("NominalInterestRate2", "0.005");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2015-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfRateReset", "1Q-");
        map.put("MarketObjectCodeOfRateReset", "RefRateXY");
        map.put("DeliverySettlement","D");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // create six analysis (monitoring) events
        Set<ContractEvent> events = EventFactory.createEvents(
                ScheduleFactory.createSchedule(model.getAs("StatusDate"),model.<LocalDateTime>getAs("StatusDate").plusMonths(6),"1M-","SD"),
                StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM());
        // apply events
        StateSpace postStates = PlainVanillaInterestRateSwap.apply(events,model);
        System.out.print(
                "Last applied event: " + postStates.lastEventTime + "\n" +
                        "Post events nominal value: " + postStates.nominalValue + "\n" +
                        "Post events nominal rate: " + postStates.nominalRate + "\n" +
                        "Post events nominal accrued: " + postStates.nominalAccrued);

    }

    @Test
    public void test_SWPPV_apply_IP() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("NominalInterestRate", "0.01");
        map.put("NominalInterestRate2", "0.005");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2015-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("CycleOfRateReset", "1Q-");
        map.put("MarketObjectCodeOfRateReset", "RefRateXY");
        map.put("DeliverySettlement","D");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // create six interest payment events according to the contract schedule
        Set<ContractEvent> events = EventFactory.createEvents(
                ScheduleFactory.createSchedule(model.<LocalDateTime>getAs("StatusDate").plusMonths(1),model.<LocalDateTime>getAs("StatusDate").plusMonths(6),model.getAs("CycleOfInterestPayment"),"SD"),
                StringUtils.EventType_IP, model.getAs("Currency"), new POF_IPFix_SWPPV(), new STF_IPFix_SWPPV());
        events.addAll(EventFactory.createEvents(
                ScheduleFactory.createSchedule(model.<LocalDateTime>getAs("StatusDate").plusMonths(1),model.<LocalDateTime>getAs("StatusDate").plusMonths(6),model.getAs("CycleOfInterestPayment"),"SD"),
                StringUtils.EventType_IP, model.getAs("Currency"), new POF_IPFloat_SWPPV(), new STF_IPFloat_SWPPV()));
        // apply events
        StateSpace postStates = PlainVanillaInterestRateSwap.apply(events,model);
        System.out.print(
                "Last applied event: " + postStates.lastEventTime + "\n" +
                        "Post events nominal value: " + postStates.nominalValue + "\n" +
                        "Post events nominal rate: " + postStates.nominalRate + "\n" +
                        "Post events nominal accrued: " + postStates.nominalAccrued);

    }

}

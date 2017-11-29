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

import java.time.Period;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class ForeignExchangeOutrightTest {
    
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
    public void test_FXOUT_lifecycle_MandatoryAttributes() {
        thrown = ExpectedException.none();
        // define attributes
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "FXOUT");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("Currency", "USD");
        map.put("Currency2", "EUR");
        map.put("MaturityDate", "2016-06-01T00:00:00");
        map.put("NotionalPrincipal", "1000");
        map.put("NotionalPrincipal2", "900");
        map.put("DeliverySettlement", "D");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = ForeignExchangeOutright.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_FXOUT_lifecycle_whithDeliverySettlement_whereS() {
        thrown = ExpectedException.none();
        // define attributes
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "FXOUT");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("Currency", "USD");
        map.put("Currency2", "EUR");
        map.put("MaturityDate", "2016-06-01T00:00:00");
        map.put("NotionalPrincipal", "1000");
        map.put("NotionalPrincipal2", "900");
        map.put("DeliverySettlement", "S");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = ForeignExchangeOutright.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_FXOUT_lifecycle_whereS_withSTD() {
        thrown = ExpectedException.none();
        // define attributes
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "FXOUT");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("Currency", "USD");
        map.put("Currency2", "EUR");
        map.put("MaturityDate", "2016-06-01T00:00:00");
        map.put("NotionalPrincipal", "1000");
        map.put("NotionalPrincipal2", "900");
        map.put("DeliverySettlement", "S");
        map.put("SettlementDate", "2016-06-03T00:00:00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = ForeignExchangeOutright.lifecycle(analysisTimes,model,riskFactors);
    }
    
    
    @Test
    public void test_FXOUT_lifecycle_withPRD() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "FXOUT");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("Currency", "USD");
        map.put("Currency2", "EUR");
        map.put("MaturityDate", "2016-06-01T00:00:00");
        map.put("NotionalPrincipal", "1000");
        map.put("NotionalPrincipal2", "900");
        map.put("DeliverySettlement", "D");
        map.put("PurchaseDate","2016-01-02T00:00:00");
        map.put("PriceAtPurchaseDate","100.0");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = ForeignExchangeOutright.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_FXOUT_lifecycle_withPRD_withTD() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "FXOUT");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("Currency", "USD");
        map.put("Currency2", "EUR");
        map.put("MaturityDate", "2016-06-01T00:00:00");
        map.put("NotionalPrincipal", "1000");
        map.put("NotionalPrincipal2", "900");
        map.put("DeliverySettlement", "D");
        map.put("PurchaseDate","2016-01-02T00:00:00");
        map.put("PriceAtPurchaseDate","100.0");
        map.put("TerminationDate","2016-01-05T00:00:00");
        map.put("PriceAtTerminationDate","150.0");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = ForeignExchangeOutright.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_FXOUT_lifecycle_withPRD_withTD_withSTD() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "FXOUT");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("Currency", "USD");
        map.put("Currency2", "EUR");
        map.put("MaturityDate", "2016-06-01T00:00:00");
        map.put("NotionalPrincipal", "1000");
        map.put("NotionalPrincipal2", "900");
        map.put("DeliverySettlement", "S");
        map.put("SettlementDate", "2016-06-03T00:00:00");
        map.put("PurchaseDate","2016-01-02T00:00:00");
        map.put("PriceAtPurchaseDate","100.0");
        map.put("TerminationDate","2016-01-05T00:00:00");
        map.put("PriceAtTerminationDate","150.0");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = ForeignExchangeOutright.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_FXOUT_lifecycle_withSTD_withMultipleAnalysisTimes() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "FXOUT");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("Currency", "USD");
        map.put("Currency2", "EUR");
        map.put("MaturityDate", "2016-06-01T00:00:00");
        map.put("NotionalPrincipal", "1000");
        map.put("NotionalPrincipal2", "900");
        map.put("DeliverySettlement", "S");
        map.put("SettlementDate", "2016-06-03T00:00:00");
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
        ArrayList<ContractEvent> events = ForeignExchangeOutright.lifecycle(analysisTimes,model,riskFactors);
        //System.out.println(events);
    }

    @Test
    public void test_FXOUT_payoff_withSTD_withMultipleAnalysisTimes() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "FXOUT");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("Currency", "USD");
        map.put("Currency2", "EUR");
        map.put("MaturityDate", "2016-06-01T00:00:00");
        map.put("NotionalPrincipal", "1000");
        map.put("NotionalPrincipal2", "900");
        map.put("DeliverySettlement", "S");
        map.put("SettlementDate", "2016-06-03T00:00:00");
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
        ArrayList<ContractEvent> events = ForeignExchangeOutright.payoff(analysisTimes,model,riskFactors);
        //System.out.println(events);
    }

    @Test
    public void test_FXOUT_next_5_withSTD() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "FXOUT");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("Currency", "USD");
        map.put("Currency2", "EUR");
        map.put("MaturityDate", "2016-06-01T00:00:00");
        map.put("NotionalPrincipal", "1000");
        map.put("NotionalPrincipal2", "900");
        map.put("DeliverySettlement", "S");
        map.put("SettlementDate", "2016-06-03T00:00:00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = ForeignExchangeOutright.next(LocalDateTime.parse("2016-01-01T00:00:00"),5,model,riskFactors);
        //System.out.println(events);
    }

    @Test
    public void test_FXOUT_next_5_fromSD_withSTD() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "FXOUT");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("Currency", "USD");
        map.put("Currency2", "EUR");
        map.put("MaturityDate", "2016-06-01T00:00:00");
        map.put("NotionalPrincipal", "1000");
        map.put("NotionalPrincipal2", "900");
        map.put("DeliverySettlement", "S");
        map.put("SettlementDate", "2016-06-03T00:00:00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = ForeignExchangeOutright.next(5,model,riskFactors);
        //System.out.println(events);
    }

    @Test
    public void test_FXOUT_next_within_withSTD() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "FXOUT");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("Currency", "USD");
        map.put("Currency2", "EUR");
        map.put("MaturityDate", "2016-06-01T00:00:00");
        map.put("NotionalPrincipal", "1000");
        map.put("NotionalPrincipal2", "900");
        map.put("DeliverySettlement", "S");
        map.put("SettlementDate", "2016-06-03T00:00:00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = ForeignExchangeOutright.next(LocalDateTime.parse("2016-01-01T00:00:00"),Period.ofDays(10),model,riskFactors);
        //System.out.println(events);
    }

    @Test
    public void test_FXOUT_next_within_fromSD_withSTD() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "FXOUT");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("Currency", "USD");
        map.put("Currency2", "EUR");
        map.put("MaturityDate", "2016-06-01T00:00:00");
        map.put("NotionalPrincipal", "1000");
        map.put("NotionalPrincipal2", "900");
        map.put("DeliverySettlement", "S");
        map.put("SettlementDate", "2016-06-03T00:00:00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = ForeignExchangeOutright.next(Period.ofWeeks(1),model,riskFactors);
        //System.out.println(events);
    }
    @Test
    public void test_FXOUT_noncontingent_lifecycle_withSTD_withMultipleAnalysisTimes() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "FXOUT");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("Currency", "USD");
        map.put("Currency2", "EUR");
        map.put("MaturityDate", "2016-06-01T00:00:00");
        map.put("NotionalPrincipal", "1000");
        map.put("NotionalPrincipal2", "900");
        map.put("DeliverySettlement", "S");
        map.put("SettlementDate", "2016-06-03T00:00:00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        analysisTimes.add(LocalDateTime.parse("2016-04-01T00:00:00"));
        analysisTimes.add(LocalDateTime.parse("2016-07-01T00:00:00"));
        analysisTimes.add(LocalDateTime.parse("2016-09-01T00:00:00"));
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = ForeignExchangeOutright.lifecycle(analysisTimes,model);
        //System.out.println(events);
    }

    @Test
    public void test_FXOUT_noncontingent_payoff_withSTD_withMultipleAnalysisTimes() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "FXOUT");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("Currency", "USD");
        map.put("Currency2", "EUR");
        map.put("MaturityDate", "2016-06-01T00:00:00");
        map.put("NotionalPrincipal", "1000");
        map.put("NotionalPrincipal2", "900");
        map.put("DeliverySettlement", "S");
        map.put("SettlementDate", "2016-06-03T00:00:00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        analysisTimes.add(LocalDateTime.parse("2016-04-01T00:00:00"));
        analysisTimes.add(LocalDateTime.parse("2016-07-01T00:00:00"));
        analysisTimes.add(LocalDateTime.parse("2016-09-01T00:00:00"));
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = ForeignExchangeOutright.payoff(analysisTimes,model);
        //System.out.println(events);
    }

    @Test
    public void test_FXOUT_noncontingent_next_5_withSTD() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "FXOUT");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("Currency", "USD");
        map.put("Currency2", "EUR");
        map.put("MaturityDate", "2016-06-01T00:00:00");
        map.put("NotionalPrincipal", "1000");
        map.put("NotionalPrincipal2", "900");
        map.put("DeliverySettlement", "S");
        map.put("SettlementDate", "2016-06-03T00:00:00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = ForeignExchangeOutright.next(5,model);
        //System.out.println(events);
    }

    @Test
    public void test_FXOUT_noncontingent_next_within_withSTD() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "FXOUT");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("Currency", "USD");
        map.put("Currency2", "EUR");
        map.put("MaturityDate", "2016-06-01T00:00:00");
        map.put("NotionalPrincipal", "1000");
        map.put("NotionalPrincipal2", "900");
        map.put("DeliverySettlement", "S");
        map.put("SettlementDate", "2016-06-03T00:00:00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = ForeignExchangeOutright.next(Period.ofWeeks(1),model);
        //System.out.println(events);
    }

}

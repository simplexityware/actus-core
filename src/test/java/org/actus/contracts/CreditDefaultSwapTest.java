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
import org.actus.functions.pam.POF_IP_PAM;
import org.actus.functions.pam.STF_AD_PAM;
import org.actus.functions.pam.STF_IP_PAM;
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

public class CreditDefaultSwapTest {
    
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
    public void test_PAM_lifecycle_MandatoryAttributes() {
        thrown = ExpectedException.none();
        // define attributes
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "PAM");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = PrincipalAtMaturity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_PAM_lifecycle_withIPatMD() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "PAM");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleAnchorDateOfFee", "2016-01-02T00:00:00");
        map.put("CycleOfFee", "1M-"); // cdfs 
        map.put("FeeBasis", "A"); // cdfs
        map.put("FeeRate", "10"); // cdfs
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = CreditDefaultSwap.lifecycle(analysisTimes,model,riskFactors);
        //System.out.println(events);
    }
    
    @Test
    public void test_CDSWP_model_generation() {
    	thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "CDSWP");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleAnchorDateOfFee", "2016-01-02T00:00:00");
        map.put("CycleOfFee", "1M-"); // CDSWP 
        map.put("FeeBasis", "A"); // CDSWP
        map.put("FeeRate", "10"); // CDSWP
        // additional attributes 
        map.put("ContractID", "3");
        map.put("ContractStructure","NOT DEFINED");
        map.put("ContractStatus", "DL");
        map.put("Seniority", "S");
        map.put("NonPerformingDate", "2016-06-02T00:00:00");
        map.put("PrepaymentPeriod", "0D");
        map.put("GracePeriod", "0D");
        map.put("DelinquencyPeriod", "0D");
        map.put("DelinquencyRate", "3");
        map.put("ContractDealDate", "2015-01-02T00:00:00");
        map.put("RecoveryRate", "1.1");
        map.put("ExecutionRuleMethod", "FTE");
        map.put("ExecutionRuleQualifier", "1"); // because FTE in XRM -> with rule in contract model?
        map.put("RecordedCreditEvents", "0");
        
        
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle CDSWP contract
        ArrayList<ContractEvent> events = CreditDefaultSwap.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_CDSWP_ContractStructure() {
    	thrown = ExpectedException.none();
    	
//    	ArrayList<HashMap<String, String>> ContractStructure = new ArrayList<HashMap<String, String>>();
//    	HashMap<String, String> ContractReference = new HashMap<String, String>();
//    	ContractReference.put("Object", "LEI0034332111");
//    	ContractReference.put("Type", "LegalEntityIdentifier");
//    	ContractReference.put("Role", "Underlier");
//    	ContractStructure.add(ContractReference);
//    	
//    	HashMap<String, String> ContractReference2 = new HashMap<String, String>();
//    	ContractReference2.put("Object", "LEI034r969583");
//    	ContractReference2.put("Type", "LegalEntityIdentifier");
//    	ContractReference2.put("Role", "Underlier");
//    	ContractStructure.add(ContractReference2);
    	
    	Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "CDSWP");
        map.put("Calendar", "NoHolidayCalendar");
//        map.put("BusinessDayConvention", "SCF");
        // End of month convention
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("CycleAnchorDateOfFee", "2016-01-02T00:00:00");
        map.put("CycleOfFee", "1M-"); // CDSWP 
        map.put("FeeBasis", "A"); // CDSWP
        map.put("FeeRate", "10"); // CDSWP
        // feeaccrued
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfRateReset","1Q-");
        map.put("FixingDays", "0D");
        map.put("DeliverySettlement", "S");
        
        // additional attributes 
        map.put("ContractID", "3");
        map.put("ContractStructure","{\"ContractReference\": [{"
        		+ "\"Object\": \"LEI0034332111\",\"Type\": \"LegalEntityIdentifier\",\"Role\": \"Underlier\"},{"
        		+ "\"Object\": \"LEI034r969583\",\"Type\": \"LegalEntityIdentifier\",\"Role\": \"Underlier\"}]}\r\n");
        map.put("ContractStatus", "DL");
        map.put("Seniority", "S");
        map.put("NonPerformingDate", "2016-06-02T00:00:00");
        map.put("PrepaymentPeriod", "0D");
        map.put("GracePeriod", "0D");
        map.put("DelinquencyPeriod", "0D");
        map.put("DelinquencyRate", "3");
        map.put("ContractDealDate", "2015-01-02T00:00:00");
        map.put("RecoveryRate", "1.1");
        map.put("ExecutionRuleMethod", "FTE");
        map.put("ExecutionRuleQualifier", "1"); // because FTE in XRM -> with rule in contract model?
        map.put("RecordedCreditEvents", "0");
        
        
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle CDSWP contract
        ArrayList<ContractEvent> events = CreditDefaultSwap.lifecycle(analysisTimes,model,riskFactors);
        //System.out.println(events);
    	
    }
    
    @Test
    public void test_CDSWP_CounterParty_Default() {
    	thrown = ExpectedException.none();
    	
    	ArrayList<HashMap<String, String>> ContractStructure = new ArrayList<HashMap<String, String>>();
    	HashMap<String, String> ContractReference = new HashMap<String, String>();
    	ContractReference.put("Object", "LEI0034332111");
    	ContractReference.put("Type", "LegalEntityIdentifier");
    	ContractReference.put("Role", "Underlier");
    	ContractStructure.add(ContractReference);
    	
    	HashMap<String, String> ContractReference2 = new HashMap<String, String>();
    	ContractReference2.put("Object", "LEI034r969583");
    	ContractReference2.put("Type", "LegalEntityIdentifier");
    	ContractReference2.put("Role", "Underlier");
    	ContractStructure.add(ContractReference2);
    	
    	Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "CDSWP");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleAnchorDateOfFee", "2016-01-02T00:00:00");
        map.put("CycleOfFee", "1M-"); // CDSWP 
        map.put("FeeBasis", "A"); // CDSWP
        map.put("FeeRate", "10"); // CDSWP
        
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfRateReset","1Q-");
        map.put("FixingDays", "0D");
        map.put("DeliverySettlement", "S");
       
        // additional attributes 
        map.put("ContractID", "3");
        map.put("ContractStructure","{\"ContractReference\": [{"
        		+ "\"Object\": \"LEI0034332111\",\"Type\": \"LegalEntityIdentifier\",\"Role\": \"Underlier\"},{"
        		+ "\"Object\": \"LEI034r969583\",\"Type\": \"LegalEntityIdentifier\",\"Role\": \"Underlier\"}]}\r\n");
        map.put("ContractStatus", "DL");
        map.put("Seniority", "S");
        map.put("NonPerformingDate", "2016-06-02T00:00:00");
        map.put("PrepaymentPeriod", "0D");
        map.put("GracePeriod", "0D");
        map.put("DelinquencyPeriod", "0D");
        map.put("DelinquencyRate", "3");
        map.put("ContractDealDate", "2015-01-02T00:00:00");
        map.put("RecoveryRate", "1.1");
        map.put("ExecutionRuleMethod", "FTE");
        map.put("ExecutionRuleQualifier", "1"); // because FTE in XRM -> with rule in contract model?
        map.put("RecordedCreditEvents", "0");
        
        
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        System.out.println(riskFactors.keys());
        
        // create six analysis (monitoring) events
//        Set<ContractEvent> events = EventFactory.createEvents(
//                ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
//                StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM());
//        
        // lifecycle CDSWP contract
        ArrayList<ContractEvent> events = CreditDefaultSwap.lifecycle(analysisTimes,model,riskFactors);
        System.out.println(events);
    	
    }

}

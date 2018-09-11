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

public class PrincipalAtMaturityTest {
    
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
    public void test_PAM_lifecycle_withIPCL() {
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
        map.put("CycleOfInterestPayment","1Q-");
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
    public void test_PAM_lifecycle_withIPCLandIPANX() {
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
        ArrayList<ContractEvent> events = PrincipalAtMaturity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_PAM_lifecycle_withIP_withRRCLandRRANX() {
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
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = PrincipalAtMaturity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_PAM_lifecycle_withIP_withRR_withSCwhere000() {
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
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","000");
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
    public void test_PAM_lifecycle_withIP_withRR_withSCwhereI00() {
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
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","I00");
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
    public void test_PAM_lifecycle_withIP_withRR_withSCwhereIN0() {
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
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
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
    public void test_PAM_lifecycle_withIP_withRR_withSCwhereIN0_withSCCL() {
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
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("CycleOfScalingIndex","1Q-");
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
    public void test_PAM_lifecycle_withIP_withRR_withSCwhereIN0_withSCCLandSCANX() {
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
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfScalingIndex","2016-06-01T00:00:00");
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
    public void test_PAM_lifecycle_withIP_withRR_withSC_withFPwhereA() {
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
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("CycleOfScalingIndex","1Q-");
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
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = PrincipalAtMaturity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_PAM_lifecycle_withIP_withRR_withSC_withFPwhereN() {
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
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("CycleOfScalingIndex","1Q-");
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
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = PrincipalAtMaturity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_PAM_lifecycle_withIP_withRR_withSC_withFP_withOPCL() {
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
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleOfFee","1Q-");
        map.put("FeeBasis","N");
        map.put("FeeRate","0.01");
        map.put("CycleOfOptionality","1Q-");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
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
    public void test_PAM_lifecycle_withIP_withRR_withSC_withFP_withOPANX() {
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
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("FeeBasis","N");
        map.put("FeeRate","0.01");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
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
    public void test_PAM_lifecycle_withIP_withRR_withSC_withFP_withOPCLandOPANX() {
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
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("FeeBasis","N");
        map.put("FeeRate","0.01");
        map.put("CycleOfOptionality","1Q-");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
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
    public void test_PAM_lifecycle_withIP_withRR_withSC_withFP_withOP_withPYwhereO() {
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
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("FeeBasis","N");
        map.put("FeeRate","0.01");
        map.put("CycleOfOptionality","1Q-");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
        map.put("PenaltyType","O");
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
    public void test_PAM_lifecycle_withIP_withRR_withSC_withFP_withOP_withPYwhereA() {
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
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("FeeBasis","N");
        map.put("FeeRate","0.01");
        map.put("CycleOfOptionality","1Q-");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
        map.put("PenaltyType","A");
        map.put("PenaltyRate","100");
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
    public void test_PAM_lifecycle_withIP_withRR_withSC_withFP_withOP_withPYwhereN() {
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
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("FeeBasis","N");
        map.put("FeeRate","0.01");
        map.put("CycleOfOptionality","1Q-");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
        map.put("PenaltyType","N");
        map.put("PenaltyRate","0.1");
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
    public void test_PAM_lifecycle_withIP_withRR_withSC_withFP_withOP_withPYwhereI() {
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
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("FeeBasis","N");
        map.put("FeeRate","0.01");
        map.put("CycleOfOptionality","1Q-");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
        map.put("PenaltyType","I");
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
    public void test_PAM_lifecycle_withIP_withRR_withSC_withOP_withMultipleAnalysisTimes() {
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
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
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
        ArrayList<ContractEvent> events = PrincipalAtMaturity.lifecycle(analysisTimes,model,riskFactors);
        //System.out.println(events);
    }

    @Test
    public void test_PAM_payoff_withIP_withRR_withSC_withOP_withMultipleAnalysisTimes() {
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
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
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
        ArrayList<ContractEvent> events = PrincipalAtMaturity.payoff(analysisTimes,model,riskFactors);
        //System.out.println(events);
    }

    @Test
    public void test_PAM_next_within_withIP_withRR_withSC_withOP() {
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
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = PrincipalAtMaturity.next(Period.ofDays(10),model);
        //System.out.println(events);
    }

    @Test
    public void test_PAM_next_within_fromSD_withIP_withRR_withSC_withOP() {
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
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = PrincipalAtMaturity.next(Period.ofWeeks(1),model);
        //System.out.println(events);
    }

    @Test
    public void test_PAM_schedule_withIP_withRR_withSC_withOP() {
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
        map.put("CycleOfInterestPayment","1Q-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle PAM contract
        ArrayList<ContractEvent> events = PrincipalAtMaturity.schedule(model);
        //System.out.println(events);
    }


    @Test
    public void test_PAM_apply_AE_preIED() {
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
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // create six analysis (monitoring) events
        Set<ContractEvent> events = EventFactory.createEvents(
                ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
                StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM());
        // apply events
        StateSpace postStates = PrincipalAtMaturity.apply(events,model);
        System.out.print(
                "Last applied event: " + postStates.lastEventTime + "\n" +
                        "Post events nominal value: " + postStates.nominalValue + "\n" +
                        "Post events nominal rate: " + postStates.nominalRate + "\n" +
                        "Post events nominal accrued: " + postStates.nominalAccrued);

    }

    @Test
    public void test_PAM_apply_AE_postIED() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "PAM");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-02-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // create six analysis (monitoring) events
        Set<ContractEvent> events = EventFactory.createEvents(
                ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
                StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM());
        // apply events
        StateSpace postStates = PrincipalAtMaturity.apply(events,model);
        System.out.print(
                "Last applied event: " + postStates.lastEventTime + "\n" +
                        "Post events nominal value: " + postStates.nominalValue + "\n" +
                        "Post events nominal rate: " + postStates.nominalRate + "\n" +
                        "Post events nominal accrued: " + postStates.nominalAccrued);

    }

    @Test
    public void test_PAM_apply_IP() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "PAM");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-02-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // create six interest payment events according to the contract schedule
        Set<ContractEvent> events = EventFactory.createEvents(
                ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),model.getAs("CycleOfInterestPayment"),"SD"),
                StringUtils.EventType_IP, model.getAs("Currency"), new POF_IP_PAM(), new STF_IP_PAM());
        // apply events
        StateSpace postStates = PrincipalAtMaturity.apply(events,model);
        System.out.print(
                "Last applied event: " + postStates.lastEventTime + "\n" +
                        "Post events nominal value: " + postStates.nominalValue + "\n" +
                        "Post events nominal rate: " + postStates.nominalRate + "\n" +
                        "Post events nominal accrued: " + postStates.nominalAccrued);

    }

}

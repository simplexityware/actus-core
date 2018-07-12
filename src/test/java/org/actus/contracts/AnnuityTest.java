/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import org.actus.attributes.ContractModel;
import org.actus.events.ContractEvent;
import org.actus.events.EventFactory;
import org.actus.functions.nam.POF_IP_NAM;
import org.actus.functions.nam.POF_PR_NAM;
import org.actus.functions.nam.STF_IP_NAM;
import org.actus.functions.nam.STF_PR_NAM;
import org.actus.functions.pam.POF_AD_PAM;
import org.actus.functions.pam.STF_AD_PAM;
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

public class AnnuityTest {
    
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
    public void test_ANN_lifecycle_MandatoryAttributes_withMaturity() {
        thrown = ExpectedException.none();
        // define attributes
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
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
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_ANN_lifecycle_MandatoryAttributes_withoutMaturity() {
        thrown = ExpectedException.none();
        // define attributes
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("NextPrincipalRedemptionPayment", "100.0");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_ANN_lifecycle_MandatoryAttributes_withMaturityAndPRNXT() {
        thrown = ExpectedException.none();
        // define attributes
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("NextPrincipalRedemptionPayment", "100.0");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_ANN_lifecycle_withPRANX() {
        thrown = ExpectedException.none();
        // define attributes
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleAnchorDateOfPrincipalRedemption", "2016-04-02T00:00:00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_ANN_lifecycle_withIPCL() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_ANN_lifecycle_withIPCLandIPANX() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
        map.put("CycleAnchorDateOfInterestPayment","2016-02-01T00:00:00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_ANN_lifecycle_withIP_withRRCLandRRANX() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
        map.put("CycleAnchorDateOfInterestPayment","2016-02-01T00:00:00");
        map.put("CycleOfRateReset","1Q-");
        map.put("CycleAnchorDateOfRateReset","2016-04-01T00:00:00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_ANN_lifecycle_withIP_withRR_withSCwhere000() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","000");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_ANN_lifecycle_withIP_withRR_withSCwhereI00() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","I00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_ANN_lifecycle_withIP_withRR_withSCwhereIN0() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_ANN_lifecycle_withIP_withRR_withSCwhereIN0_withSCCL() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
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
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_ANN_lifecycle_withIP_withRR_withSCwhereIN0_withSCCLandSCANX() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
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
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_ANN_lifecycle_withIP_withRR_withSC_withFPwhereA() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
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
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_ANN_lifecycle_withIP_withRR_withSC_withFPwhereN() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
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
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_ANN_lifecycle_withIP_withRR_withSC_withFP_withOPCL() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
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
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
        
    @Test
    public void test_ANN_lifecycle_withIP_withRR_withSC_withFP_withOPANX() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
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
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_ANN_lifecycle_withIP_withRR_withSC_withFP_withOPCLandOPANX() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
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
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }

    @Test
    public void test_ANN_lifecycle_withIP_withRR_withSC_withFP_withOP_withPYwhereO() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
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
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }

    @Test
    public void test_ANN_lifecycle_withIP_withRR_withSC_withFP_withOP_withPYwhereA() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
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
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_ANN_lifecycle_withIP_withRR_withSC_withFP_withOP_withPYwhereN() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
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
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_ANN_lifecycle_withIP_withRR_withSC_withFP_withOP_withPYwhereI() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
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
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_ANN_lifecycle_withIP_withRR_withSC_withFP_withOP_withPY_withIPCBwhereNT() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("FeeBasis","N");
        map.put("FeeRate","0.01");
        map.put("CycleOfOptionality","1Q-");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
        map.put("PenaltyType","I");
        map.put("InterestPaymentCalculationBase","NT");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_ANN_lifecycle_withIP_withRR_withSC_withFP_withOP_withPY_withIPCBwhereNTIED() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("FeeBasis","N");
        map.put("FeeRate","0.01");
        map.put("CycleOfOptionality","1Q-");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
        map.put("PenaltyType","I");
        map.put("InterestPaymentCalculationBase","NTIED");
        map.put("InterestPaymentCalculationBaseAmount","500.0");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_ANN_lifecycle_withIP_withRR_withSC_withFP_withOP_withPY_withIPCBwhereNTL() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("FeeBasis","N");
        map.put("FeeRate","0.01");
        map.put("CycleOfOptionality","1Q-");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
        map.put("PenaltyType","I");
        map.put("InterestPaymentCalculationBase","NTL");
        map.put("InterestPaymentCalculationBaseAmount","1000.0");
        map.put("CycleOfInterestCalculationBase","1Q-");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_ANN_lifecycle_withIP_withRR_withSC_withFP_withOP_withPY_withIPCBwhereNTLwithIPCBANX() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("FeeBasis","N");
        map.put("FeeRate","0.01");
        map.put("CycleOfOptionality","1Q-");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
        map.put("PenaltyType","I");
        map.put("InterestPaymentCalculationBase","NTL");
        map.put("InterestPaymentCalculationBaseAmount","1000.0");
        map.put("CycleOfInterestCalculationBase","1Q-");
        map.put("CycleAnchorDateOfInterestCalculationBase","2016-04-01T00:00:00");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
    }
    
    @Test
    public void test_ANN_lifecycle_withIP_withRR_withSC_withOP_withIPCB_withMultipleAnalysisTimes() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
        map.put("InterestPaymentCalculationBase","NTL");
        map.put("InterestPaymentCalculationBaseAmount","1000.0");
        map.put("CycleOfInterestCalculationBase","1Q-");
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
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.lifecycle(analysisTimes,model,riskFactors);
        //System.out.println(events);
    }

    @Test
    public void test_ANN_payoff_withIP_withRR_withSC_withOP_withIPCB_withMultipleAnalysisTimes() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
        map.put("InterestPaymentCalculationBase","NTL");
        map.put("InterestPaymentCalculationBaseAmount","1000.0");
        map.put("CycleOfInterestCalculationBase","1Q-");
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
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.payoff(analysisTimes,model,riskFactors);
        //System.out.println(events);
    }

    @Test
    public void test_ANN_next_5_withIP_withRR_withSC_withOP_withIPCB() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
        map.put("InterestPaymentCalculationBase","NTL");
        map.put("InterestPaymentCalculationBaseAmount","1000.0");
        map.put("CycleOfInterestCalculationBase","1Q-");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.next(5,model);
        //System.out.println(events);
    }

    @Test
    public void test_ANN_next_5_fromSD_withIP_withRR_withSC_withOP_withIPCB() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
        map.put("InterestPaymentCalculationBase","NTL");
        map.put("InterestPaymentCalculationBaseAmount","1000.0");
        map.put("CycleOfInterestCalculationBase","1Q-");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.next(5, model);
        //System.out.println(events);
    }

    @Test
    public void test_ANN_next_within_withIP_withRR_withSC_withOP_withIPCB() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
        map.put("InterestPaymentCalculationBase","NTL");
        map.put("InterestPaymentCalculationBaseAmount","1000.0");
        map.put("CycleOfInterestCalculationBase","1Q-");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.next(Period.ofDays(10),model);
        //System.out.println(events);
    }

    @Test
    public void test_ANN_next_within_fromSD_withIP_withRR_withSC_withOP_withIPCB() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
        map.put("InterestPaymentCalculationBase","NTL");
        map.put("InterestPaymentCalculationBaseAmount","1000.0");
        map.put("CycleOfInterestCalculationBase","1Q-");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.next(Period.ofDays(10),model);
        //System.out.println(events);
    }

    @Test
    public void test_ANN_schedule_withIP_withRR_withSC_withOP_withIPCB() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleAnchorDateOfPrincipalRedemption","2016-07-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2026-01-02T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("CycleOfInterestPayment","1M-");
        map.put("CycleOfRateReset","1Q-");
        map.put("ScalingEffect","IN0");
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        map.put("ObjectCodeOfPrepaymentModel","IDXY");
        map.put("InterestPaymentCalculationBase","NTL");
        map.put("InterestPaymentCalculationBaseAmount","1000.0");
        map.put("CycleOfInterestCalculationBase","1Q-");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // lifecycle LAM contract
        ArrayList<ContractEvent> events = Annuity.schedule(model);
        //System.out.println(events);
    }

    @Test
    public void test_ANN_apply_AE() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-02-02T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("NextPrincipalRedemptionPayment","100");
        map.put("InterestCalculationBase","NT");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // create six analysis (monitoring) events
        Set<ContractEvent> events = EventFactory.createEvents(
                ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
                StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM());
        // apply events
        StateSpace postStates = Annuity.apply(events,model);
        System.out.print(
                "Last applied event: " + postStates.lastEventTime + "\n" +
                        "Post events nominal value: " + postStates.nominalValue + "\n" +
                        "Post events nominal rate: " + postStates.nominalRate + "\n" +
                        "Post events nominal accrued: " + postStates.nominalAccrued);

    }

    @Test
    public void test_ANN_apply_IP_PR() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "ANN");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-02-02T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("NominalInterestRate","0.01");
        map.put("NextPrincipalRedemptionPayment","100");
        map.put("InterestCalculationBase","NT");
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // create six interest payment events according to the contract schedule
        Set<ContractEvent> events = EventFactory.createEvents(
                ScheduleFactory.createSchedule(model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(1),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),model.getAs("CycleOfPrincipalRedemption"),"SD"),
                StringUtils.EventType_IP, model.getAs("Currency"), new POF_IP_NAM(), new STF_IP_NAM());
        events.addAll(EventFactory.createEvents(
                ScheduleFactory.createSchedule(model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(1),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),model.getAs("CycleOfPrincipalRedemption"),"SD"),
                StringUtils.EventType_PR, model.getAs("Currency"), new POF_PR_NAM(), new STF_PR_NAM()));
        // apply events
        StateSpace postStates = Annuity.apply(events,model);
        System.out.print(
                "Last applied event: " + postStates.lastEventTime + "\n" +
                        "Post events nominal value: " + postStates.nominalValue + "\n" +
                        "Post events nominal rate: " + postStates.nominalRate + "\n" +
                        "Post events nominal accrued: " + postStates.nominalAccrued);

    }
}

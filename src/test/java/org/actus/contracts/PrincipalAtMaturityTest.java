/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import org.actus.attributes.ContractModel;
import org.actus.events.ContractEvent;
import org.actus.attributes.AttributeParser;
import org.actus.riskfactors.RiskFactorProvider;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class PrincipalAtMaturityTest {
    
    class RiskFactorModel implements RiskFactorProvider {
        public Set<String> keys() {
            Set<String> keys = new HashSet<String>();
            return keys;
        }
        
        public Set<LocalDateTime> times(String id) {
            Set<LocalDateTime> times = new HashSet<LocalDateTime>();
            return times;
        }
        
        public double stateAt(String id, LocalDateTime time, String term) {
            return 0.0;    
        }
    }
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void test_PAM_init_MandatoryAttributes() {
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
        ContractModel model = AttributeParser.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // init PAM contracts
        PrincipalAtMaturity contract = new PrincipalAtMaturity();
        contract.init(analysisTimes, model);
    }

    @Test
    public void test_PAM_init_AllAttributes() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "PAM");
        map.put("Calendar", "org.actus.time.calendar.NoHolidaysCalendar");
        map.put("BusinessDayConvention", "SCF");
        map.put("EndOfMonthConvention", "SD");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("CycleAnchorDateOfFee", "2016-01-02T00:00:00");
        map.put("CycleOfFee", "1Q-");
        map.put("FeeBasis", "1000.0");
        map.put("FeeRate", "0.05");
        map.put("FeeAccrued", "0.0");
        map.put("CycleAnchorDateOfInterestPayment", "2016-01-02T00:00:00");
        map.put("CycleOfInterestPayment", "1M+");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("AccruedInterest", "0.0");
        map.put("CapitalizationEndDate", "2016-04-02T00:00:00");
        map.put("CyclePointOfInterestPayment", "END");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("PremiumDiscountAtIED", "-100.0");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("PurchaseDate", "2016-05-01T00:00:00");
        map.put("PriceAtPurchaseDate", "800.0");
        map.put("TerminationDate", "2016-07-01T00:00:00");
        map.put("PriceAtTerminationDate", "900.0");
        map.put("MarketObjectCodeOfScalingIndex", "Index-XY");
        map.put("ScalingIndexAtStatusDate", "1000.0");
        map.put("CycleAnchorDateOfScalingIndex", "2016-01-02T00:00:00");
        map.put("CycleOfScalingIndex", "6M-");
        map.put("ScalingEffect", "INM");
        map.put("CycleAnchorDateOfRateReset", "2016-04-02T00:00:00");
        map.put("CycleOfRateReset", "2M-");
        map.put("RateSpread", "0.05");
        map.put("MarketObjectCodeRateReset", "ReferenceRate-XY");
        map.put("CyclePointOfRateReset", "BEG");
        map.put("FixingDays", "2D");
        map.put("NextResetRate", "0.08");
        map.put("RateMultiplier", "1.1");
        map.put("RateTerm", "4M");
        // parse attributes
        ContractModel model = AttributeParser.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // init PAM contracts
        PrincipalAtMaturity contract = new PrincipalAtMaturity();
        contract.init(analysisTimes, model);
    }
    
    @Test
    public void test_PAM_eval_MandatoryAttributes() {
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
        ContractModel model = AttributeParser.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // init PAM contract
        PrincipalAtMaturity contract = new PrincipalAtMaturity();
        contract.init(analysisTimes, model);
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // eval PAM contract
        ArrayList<ContractEvent> events = contract.eval(riskFactors);
    }
    
    @Test
    public void test_PAM_eval_withIPatMD() {
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
        ContractModel model = AttributeParser.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // init PAM contracts
        PrincipalAtMaturity contract = new PrincipalAtMaturity();
        contract.init(analysisTimes, model);
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // eval PAM contract
        ArrayList<ContractEvent> events = contract.eval(riskFactors);
    }
    
    @Test
    public void test_PAM_eval_withIPCL() {
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
        ContractModel model = AttributeParser.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // init PAM contracts
        PrincipalAtMaturity contract = new PrincipalAtMaturity();
        contract.init(analysisTimes, model);
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // eval PAM contract
        ArrayList<ContractEvent> events = contract.eval(riskFactors);
    }
    
    @Test
    public void test_PAM_eval_withIPCLandIPANX() {
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
        ContractModel model = AttributeParser.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // init PAM contracts
        PrincipalAtMaturity contract = new PrincipalAtMaturity();
        contract.init(analysisTimes, model);
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // eval PAM contract
        ArrayList<ContractEvent> events = contract.eval(riskFactors);
    }
    
    @Test
    public void test_PAM_eval_withIP_withRRCLandRRANX() {
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
        ContractModel model = AttributeParser.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // init PAM contracts
        PrincipalAtMaturity contract = new PrincipalAtMaturity();
        contract.init(analysisTimes, model);
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // eval PAM contract
        ArrayList<ContractEvent> events = contract.eval(riskFactors);
    }
    
    @Test
    public void test_PAM_eval_withIP_withRR_withSCCL() {
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
        map.put("CycleOfScalingIndex","1Q-");
        // parse attributes
        ContractModel model = AttributeParser.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // init PAM contracts
        PrincipalAtMaturity contract = new PrincipalAtMaturity();
        contract.init(analysisTimes, model);
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // eval PAM contract
        ArrayList<ContractEvent> events = contract.eval(riskFactors);
    }
    
    @Test
    public void test_PAM_eval_withIP_withRR_withSCCLandSCANX() {
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
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfScalingIndex","2016-06-01T00:00:00");
        // parse attributes
        ContractModel model = AttributeParser.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // init PAM contracts
        PrincipalAtMaturity contract = new PrincipalAtMaturity();
        contract.init(analysisTimes, model);
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // eval PAM contract
        ArrayList<ContractEvent> events = contract.eval(riskFactors);
    }
    
    @Test
    public void test_PAM_eval_withIP_withRR_withSC_withOPCL() {
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
        map.put("CycleOfOptionality","1Q-");
        // parse attributes
        ContractModel model = AttributeParser.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // init PAM contracts
        PrincipalAtMaturity contract = new PrincipalAtMaturity();
        contract.init(analysisTimes, model);
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // eval PAM contract
        ArrayList<ContractEvent> events = contract.eval(riskFactors);
    }
    
    @Test
    public void test_PAM_eval_withIP_withRR_withSC_withOPCLandOPANX() {
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
        map.put("CycleOfScalingIndex","1Q-");
        map.put("CycleAnchorDateOfOptionality","2016-06-01T00:00:00");
        // parse attributes
        ContractModel model = AttributeParser.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // init PAM contracts
        PrincipalAtMaturity contract = new PrincipalAtMaturity();
        contract.init(analysisTimes, model);
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // eval PAM contract
        ArrayList<ContractEvent> events = contract.eval(riskFactors);
    }

    @Test
    public void test_PAM_eval_AllAttributes() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "PAM");
        map.put("Calendar", "org.actus.time.calendar.NoHolidaysCalendar");
        map.put("BusinessDayConvention", "SCF");
        map.put("EndOfMonthConvention", "SD");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("CycleAnchorDateOfFee", "2016-01-02T00:00:00");
        map.put("CycleOfFee", "1Q-");
        map.put("FeeBasis", "1000.0");
        map.put("FeeRate", "0.05");
        map.put("FeeAccrued", "0.0");
        map.put("CycleAnchorDateOfInterestPayment", "2016-01-02T00:00:00");
        map.put("CycleOfInterestPayment", "1M+");
        map.put("NominalInterestRate", "0.01");
        map.put("DayCountConvention", "A/AISDA");
        map.put("AccruedInterest", "0.0");
        map.put("CapitalizationEndDate", "2016-04-02T00:00:00");
        map.put("CyclePointOfInterestPayment", "END");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("PremiumDiscountAtIED", "-100.0");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        map.put("PurchaseDate", "2016-05-01T00:00:00");
        map.put("PriceAtPurchaseDate", "800.0");
        map.put("TerminationDate", "2016-07-01T00:00:00");
        map.put("PriceAtTerminationDate", "900.0");
        map.put("MarketObjectCodeOfScalingIndex", "Index-XY");
        map.put("ScalingIndexAtStatusDate", "1000.0");
        map.put("CycleAnchorDateOfScalingIndex", "2016-01-02T00:00:00");
        map.put("CycleOfScalingIndex", "6M-");
        map.put("ScalingEffect", "INM");
        map.put("CycleAnchorDateOfRateReset", "2016-04-02T00:00:00");
        map.put("CycleOfRateReset", "2M-");
        map.put("RateSpread", "0.05");
        map.put("MarketObjectCodeRateReset", "ReferenceRate-XY");
        map.put("CyclePointOfRateReset", "BEG");
        map.put("FixingDays", "2D");
        map.put("NextResetRate", "0.08");
        map.put("RateMultiplier", "1.1");
        map.put("RateTerm", "4M");
        // parse attributes
        ContractModel model = AttributeParser.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // init PAM contracts
        PrincipalAtMaturity contract = new PrincipalAtMaturity();
        contract.init(analysisTimes, model);
        // define risk factor model
        RiskFactorModel riskFactors = new RiskFactorModel();
        // eval PAM contract
        ArrayList<ContractEvent> events = contract.eval(riskFactors);
    }
}

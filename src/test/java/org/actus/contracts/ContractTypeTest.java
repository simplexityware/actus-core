/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import org.actus.AttributeConversionException;
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

public class ContractTypeTest {
    
    class MarketModel implements RiskFactorModelProvider {
        public Set<String> keys() {
            Set<String> keys = new HashSet<String>();
            return keys;
        }
        
        public Set<LocalDateTime> times(String id) {
            Set<LocalDateTime> times = new HashSet<LocalDateTime>();
            return times;
        }
        
        public double stateAt(String id, LocalDateTime time,StateSpace states,ContractModelProvider contractAttributes) {
            return 0.0;    
        }
    }
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void test_UnknownCT_exception() {
        thrown.expect(AttributeConversionException.class);
        // define attributes
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "IDoNotExist");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2016-01-02T00:00:00");
        map.put("MaturityDate", "2017-01-01T00:00:00");
        map.put("NotionalPrincipal", "1000.0");
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        // define market model
        MarketModel marketModel = new MarketModel();
        // eval PAM contract
        ArrayList<ContractEvent> events = ContractType.eval(analysisTimes,model,marketModel);
    }
    
    @Test
    public void test_PAM_MandatoryAttributes() {
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
        // define market model
        MarketModel marketModel = new MarketModel();
        // eval PAM contract
        ArrayList<ContractEvent> events = ContractType.eval(analysisTimes,model,marketModel);
    }
    
    @Test
    public void test_PAM_withIP_withRR_withSC_withOP_withMultipleAnalysisTimes() {
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
        // parse attributes
        ContractModel model = ContractModel.parse(map);
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-01-01T00:00:00"));
        analysisTimes.add(LocalDateTime.parse("2016-04-01T00:00:00"));
        analysisTimes.add(LocalDateTime.parse("2016-07-01T00:00:00"));
        analysisTimes.add(LocalDateTime.parse("2016-09-01T00:00:00"));
        // define market model
        MarketModel marketModel = new MarketModel();
        // eval PAM contract
        ArrayList<ContractEvent> events = ContractType.eval(analysisTimes,model,marketModel);
        //System.out.println(events);
    }


}
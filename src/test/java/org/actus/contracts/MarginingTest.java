/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import org.actus.AttributeConversionException;
import org.actus.attributes.ContractModel;
import org.actus.attributes.ContractModel2;
import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.events.ContractEvent;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.states.StateSpace;
import org.actus.time.calendar.BusinessDayCalendarProvider;
import org.actus.time.calendar.MondayToFridayCalendar;
import org.actus.time.calendar.NoHolidaysCalendar;
import org.actus.util.CommonUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;

public class MarginingTest {
    
    class MarketModel implements RiskFactorModelProvider {
        Map<String,NavigableMap<LocalDateTime,Double>> marketPriceSeries;

        MarketModel() {
            marketPriceSeries=new HashMap();

            NavigableMap<LocalDateTime,Double> marketPriceSeries1 = new TreeMap();
            marketPriceSeries1.put(LocalDateTime.parse("2015-12-31T00:00:00"),0.0);
            marketPriceSeries1.put(LocalDateTime.parse("2016-07-02T00:00:00"),100.0);
            marketPriceSeries1.put(LocalDateTime.parse("2017-01-01T00:00:00"),0.0);

            NavigableMap<LocalDateTime,Double> marketPriceSeries2 = new TreeMap();
            marketPriceSeries2.put(LocalDateTime.parse("2015-12-31T00:00:00"),0.0);
            marketPriceSeries2.put(LocalDateTime.parse("2016-02-02T00:00:00"),10.0);
            marketPriceSeries2.put(LocalDateTime.parse("2016-03-02T00:00:00"),20.0);
            marketPriceSeries2.put(LocalDateTime.parse("2016-04-02T00:00:00"),-10.0);
            marketPriceSeries2.put(LocalDateTime.parse("2016-05-02T00:00:00"),-20.0);
            marketPriceSeries2.put(LocalDateTime.parse("2016-06-02T00:00:00"),-40.0);
            marketPriceSeries2.put(LocalDateTime.parse("2016-07-02T00:00:00"),-20.0);
            marketPriceSeries2.put(LocalDateTime.parse("2016-08-02T00:00:00"),0.0);
            marketPriceSeries2.put(LocalDateTime.parse("2016-09-02T00:00:00"),10.0);
            marketPriceSeries2.put(LocalDateTime.parse("2016-10-02T00:00:00"),20.0);
            marketPriceSeries2.put(LocalDateTime.parse("2016-11-02T00:00:00"),20.0);
            marketPriceSeries2.put(LocalDateTime.parse("2016-12-02T00:00:00"),10.0);
            marketPriceSeries2.put(LocalDateTime.parse("2017-01-01T00:00:00"),0.0);

            marketPriceSeries.put("CUSIP_1",marketPriceSeries1);
            marketPriceSeries.put("CUSIP_2",marketPriceSeries2);
        }

        public Set<String> keys() {
            Set<String> keys = new HashSet<String>();
            return keys;
        }
        
        public double stateAt(String id,LocalDateTime time,StateSpace contractStates,ContractModelProvider contractAttributes) {
            System.out.println(id + " --- " + time);
            return marketPriceSeries.get(id).floorEntry(time).getValue();
        }
    }

    private ContractModel2 swapChild(String priceSeries) {

        // define child 1 attributes
        Map<String, Object> child1 = new HashMap<String, Object>();
        child1.put("ContractType", "PAM");
        child1.put("ContractID", "XYZ_C1");
        child1.put("Calendar", "NoHolidayCalendar");
        child1.put("StatusDate", "2016-01-01T00:00:00");
        child1.put("LegalEntityIDCounterparty", "CORP-XY");
        child1.put("DayCountConvention", "A/AISDA");
        child1.put("Currency", "USD");
        child1.put("InitialExchangeDate", "2016-01-02T00:00:00");
        child1.put("MaturityDate", "2017-01-01T00:00:00");
        child1.put("NotionalPrincipal", "1000.0");
        child1.put("NominalInterestRate","0.01");
        child1.put("CycleOfInterestPayment","1Q-");

        // define child 2 attributes
        Map<String, Object> child2 = new HashMap<String, Object>();
        child2.put("ContractType", "PAM");
        child2.put("ContractID", "XYZ_C2");
        child2.put("Calendar", "NoHolidayCalendar");
        child2.put("StatusDate", "2016-01-01T00:00:00");
        child2.put("LegalEntityIDCounterparty", "CORP-XY");
        child2.put("DayCountConvention", "A/AISDA");
        child2.put("Currency", "USD");
        child2.put("InitialExchangeDate", "2016-01-02T00:00:00");
        child2.put("MaturityDate", "2017-01-01T00:00:00");
        child2.put("NotionalPrincipal", "1000.0");
        child2.put("NominalInterestRate","0.01");
        child2.put("CycleOfInterestPayment","1Q-");
        child2.put("CycleOfRateReset","1Q-");
        child2.put("MarketObjectCodeOfRateReset","LIBOR_3M");

        // define parent attributes
        Map<String, Object> parent = new HashMap<String, Object>();
        parent.put("ContractType", "SWAPS");
        parent.put("ContractID", "XYZ");
        parent.put("StatusDate", "2016-01-01T00:00:00");
        parent.put("ContractRole", "RFL");
        parent.put("LegalEntityIDCounterparty", "CORP-XY");
        parent.put("Currency", "USD");
        parent.put("MarketObjectCode", priceSeries);
        parent.put(((String) parent.get("ContractID"))+"_C1",child1);
        parent.put(((String) parent.get("ContractID"))+"_C2",child2);

        // parse attributes
        ContractModel2 model = ContractModel2.parse(parent);

        return model;

    }
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test_MRGNG_lifecycle_MandatoryAttributes() {
        thrown = ExpectedException.none();
        // parent attributes
        Map<String,Object> attributes = new HashMap<String,Object>();
        attributes.put("BusinessDayConvention","SCF");
        attributes.put("ContractType","MRGNG");
        attributes.put("StatusDate","2016-01-01T00:00:00");
        attributes.put("ContractRole","ST"); // non-clearing hous
        attributes.put("LegalEntityIDCounterparty","LEI_XYC");
        attributes.put("InitialMargin","0.0");
        attributes.put("CycleOfMargining","1M-");
        attributes.put("VariationMargin","0.0");
        attributes.put("Currency","USD");
        attributes.put("ContractDealDate","2016-01-02T00:00:00");
        attributes.put("SettlementDate","2017-01-01T00:00");
        attributes.put("MarketValueObserved","0.0");
        attributes.put(((String) attributes.get("ContractID")) + "_C1", swapChild("CUSIP_1"));
        // parse model
        //ContractModel2 model = ContractModel2.parse(attributes);
        ContractModel2 model2 = swapChild("H");
        ContractModelProvider model = model2.getAs("XYZ_C2");
        // define analysis times
        Set<LocalDateTime> analysisTimes = new HashSet<LocalDateTime>();
        analysisTimes.add(LocalDateTime.parse("2016-04-01T00:00:00"));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();
        // lifecycle Margining contract
        ArrayList<ContractEvent> events = Margining.lifecycle(analysisTimes,model,riskFactors);
        System.out.println(events);
    }

    @Test
    public void test_MRGNG_lifecycle_MissingAttributes() {
        thrown = ExpectedException.none();



    }

}

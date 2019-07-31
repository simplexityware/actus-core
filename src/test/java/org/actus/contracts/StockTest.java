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
import org.actus.states.StateSpace;
import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;

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

public class StockTest {
    
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
    public void test_STK_schedule_MandatoryAttributes() {
        thrown = ExpectedException.none();
        // define attributes
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "STK");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("Currency", "USD");
        // parse attributes
        ContractModel model = ContractModel.parse(map);

        // compute schedule
        ArrayList<ContractEvent> schedule = Stock.schedule(LocalDateTime.parse(map.get("StatusDate")).plusYears(5),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("StatusDate"),model.<LocalDateTime>getAs("StatusDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Stock.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_STK_schedule_withDV() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "STK");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("Currency", "USD");
        map.put("CycleAnchorDateOfDividendPayment","2016-01-01T00:00:00");
        map.put("CycleOfDividendPayment","1Q-");
        map.put("MarketObjectCodeOfDividendRate","DIVYIELD");
        map.put("MarketValueObserved","1000.0");
        // parse attributes
        ContractModel model = ContractModel.parse(map);

        // compute schedule
        ArrayList<ContractEvent> schedule = Stock.schedule(LocalDateTime.parse(map.get("StatusDate")).plusYears(5),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("StatusDate"),model.<LocalDateTime>getAs("StatusDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Stock.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_STK_schedule_withDV_withPRD() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "STK");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("Currency", "USD");
        map.put("CycleAnchorDateOfDividendPayment","2016-01-01T00:00:00");
        map.put("CycleOfDividendPayment","1Q-");
        map.put("MarketObjectCodeOfDividendRate","DIVYIELD");
        map.put("MarketValueObserved","1000.0");
        map.put("PurchaseDate","2016-01-02T00:00:00");
        map.put("PriceAtPurchaseDate","1000.0");
        // parse attributes
        ContractModel model = ContractModel.parse(map);

        // compute schedule
        ArrayList<ContractEvent> schedule = Stock.schedule(LocalDateTime.parse(map.get("StatusDate")).plusYears(5),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("StatusDate"),model.<LocalDateTime>getAs("StatusDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Stock.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_STK_schedule_withDV_withPRD_withTD() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "STK");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("Currency", "USD");
        map.put("CycleAnchorDateOfDividendPayment","2016-01-01T00:00:00");
        map.put("CycleOfDividendPayment","1Q-");
        map.put("MarketObjectCodeOfDividendRate","DIVYIELD");
        map.put("MarketValueObserved","1000.0");
        map.put("PurchaseDate","2016-01-02T00:00:00");
        map.put("PriceAtPurchaseDate","1000.0");
        map.put("TerminationDate","2016-01-05T00:00:00");
        map.put("PriceAtTerminationDate","1000.0");
        // parse attributes
        ContractModel model = ContractModel.parse(map);

        // compute schedule
        ArrayList<ContractEvent> schedule = Stock.schedule(LocalDateTime.parse(map.get("StatusDate")).plusYears(5),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("StatusDate"),model.<LocalDateTime>getAs("StatusDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Stock.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_STK_schedule_withDV_withPRD_withMultipleAnalysisTimes() {
        thrown = ExpectedException.none();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "STK");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RPA");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("Currency", "USD");
        map.put("CycleAnchorDateOfDividendPayment","2016-01-01T00:00:00");
        map.put("CycleOfDividendPayment","1Q-");
        map.put("MarketObjectCodeOfDividendRate","DIVYIELD");
        map.put("MarketValueObserved","1000.0");
        map.put("PurchaseDate","2016-01-02T00:00:00");
        map.put("PriceAtPurchaseDate","1000.0");
        // parse attributes
        ContractModel model = ContractModel.parse(map);

        // compute schedule
        ArrayList<ContractEvent> schedule = Stock.schedule(LocalDateTime.parse(map.get("StatusDate")).plusYears(5),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("StatusDate"),model.<LocalDateTime>getAs("StatusDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Stock.apply(schedule,model,riskFactors);
    }
}
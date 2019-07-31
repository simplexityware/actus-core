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

public class AnnuityTest {
    
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
    public void test_ANN_MandatoryAttributes_withMaturity() {
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
        
        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
        System.out.println(events);
    }
    
    @Test
    public void test_ANN_schedule_MandatoryAttributes_withoutMaturity() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("InitialExchangeDate")).plusYears(5),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_ANN_schedule_MandatoryAttributes_withMaturityAndPRNXT() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_ANN_schedule_withPRANX() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_ANN_schedule_withIPCL() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_ANN_schedule_withIPCLandIPANX() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_ANN_schedule_withIP_withRRCLandRRANX() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_ANN_schedule_withIP_withRR_withSCwhere000() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_ANN_schedule_withIP_withRR_withSCwhereI00() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_ANN_schedule_withIP_withRR_withSCwhereIN0() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_ANN_schedule_withIP_withRR_withSCwhereIN0_withSCCL() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_ANN_schedule_withIP_withRR_withSCwhereIN0_withSCCLandSCANX() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_ANN_schedule_withIP_withRR_withSC_withFPwhereA() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_ANN_schedule_withIP_withRR_withSC_withFPwhereN() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_ANN_schedule_withIP_withRR_withSC_withFP_withOPCL() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
        
    @Test
    public void test_ANN_schedule_withIP_withRR_withSC_withFP_withOPANX() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_ANN_schedule_withIP_withRR_withSC_withFP_withOPCLandOPANX() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }

    @Test
    public void test_ANN_schedule_withIP_withRR_withSC_withFP_withOP_withPYwhereO() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }

    @Test
    public void test_ANN_schedule_withIP_withRR_withSC_withFP_withOP_withPYwhereA() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_ANN_schedule_withIP_withRR_withSC_withFP_withOP_withPYwhereN() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_ANN_schedule_withIP_withRR_withSC_withFP_withOP_withPYwhereI() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_ANN_schedule_withIP_withRR_withSC_withFP_withOP_withPY_withIPCBwhereNT() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_ANN_schedule_withIP_withRR_withSC_withFP_withOP_withPY_withIPCBwhereNTIED() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_ANN_schedule_withIP_withRR_withSC_withFP_withOP_withPY_withIPCBwhereNTL() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_ANN_schedule_withIP_withRR_withSC_withFP_withOP_withPY_withIPCBwhereNTLwithIPCBANX() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_ANN_schedule_withIP_withRR_withSC_withOP_withIPCB_withMultipleAnalysisTimes() {
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

        // compute schedule
        ArrayList<ContractEvent> schedule = Annuity.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Annuity.apply(schedule,model,riskFactors);
    }
}

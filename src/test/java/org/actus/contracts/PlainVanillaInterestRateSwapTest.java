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
import org.actus.types.EndOfMonthConventionEnum;
import org.actus.types.EventType;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class PlainVanillaInterestRateSwapTest {
    
    class MarketModel implements RiskFactorModelProvider {
        public Set<String> keys() {
            Set<String> keys = new HashSet<String>();
            return keys;
        }

        @Override
        public double stateAt(String id,LocalDateTime time,StateSpace contractStates,ContractModelProvider contractAttributes) {
            return 0.0;    
        }
    }
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void test_SWPPV_schedule_MandatoryAttributes() {
        thrown = ExpectedException.none();
        // define attributes
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RF");
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
        model.getAs("hallo");
        // compute schedule
        ArrayList<ContractEvent> schedule = PlainVanillaInterestRateSwap.schedule(model.getAs("MaturityDate"),model);

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-", EndOfMonthConventionEnum.SD),
            EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.apply(schedule,model,riskFactors);
    }
    
    
    @Test
    public void test_SWPPV_schedule_withIPCL() {
        thrown = ExpectedException.none();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RF");
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

        // compute schedule
        ArrayList<ContractEvent> schedule = PlainVanillaInterestRateSwap.schedule(LocalDateTime.parse(model.getAs("MaturityDate")),model);

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-",EndOfMonthConventionEnum.SD),
            EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_SWPPV_schedule_withIPCLandIPANX() {
        thrown = ExpectedException.none();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RF");
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

        // compute schedule
        ArrayList<ContractEvent> schedule = PlainVanillaInterestRateSwap.schedule(LocalDateTime.parse(model.getAs("MaturityDate")),model);

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-",EndOfMonthConventionEnum.SD),
            EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_SWPPV_schedule_withIP_withRRANX() {
        thrown = ExpectedException.none();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RF");
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

        // compute schedule
        ArrayList<ContractEvent> schedule = PlainVanillaInterestRateSwap.schedule(LocalDateTime.parse(model.getAs("MaturityDate")),model);

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-",EndOfMonthConventionEnum.SD),
            EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.apply(schedule,model,riskFactors);
    }

    @Test
    public void test_SWPPV_schedule_withIP_withCNTRLisPF() {
        thrown = ExpectedException.none();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "PF");
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

        // compute schedule
        ArrayList<ContractEvent> schedule = PlainVanillaInterestRateSwap.schedule(LocalDateTime.parse(model.getAs("MaturityDate")),model);

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-",EndOfMonthConventionEnum.SD),
            EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_SWPPV_schedule_withIP_withSTDwhereD() {
        thrown = ExpectedException.none();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RF");
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

        // compute schedule
        ArrayList<ContractEvent> schedule = PlainVanillaInterestRateSwap.schedule(LocalDateTime.parse(model.getAs("MaturityDate")),model);

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-",EndOfMonthConventionEnum.SD),
            EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_SWPPV_schedule_withIP_withSTDwhereS() {
        thrown = ExpectedException.none();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RF");
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

        // compute schedule
        ArrayList<ContractEvent> schedule = PlainVanillaInterestRateSwap.schedule(LocalDateTime.parse(model.getAs("MaturityDate")),model);

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-",EndOfMonthConventionEnum.SD),
            EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_SWPPV_schedule_withIP_withSTDwhereD_withPRD() {
        thrown = ExpectedException.none();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RF");
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

        // compute schedule
        ArrayList<ContractEvent> schedule = PlainVanillaInterestRateSwap.schedule(LocalDateTime.parse(model.getAs("MaturityDate")),model);

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-",EndOfMonthConventionEnum.SD),
            EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_SWPPV_schedule_withIP_withSTDwhereD_withPRD_withTD() {
        thrown = ExpectedException.none();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RF");
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

        // compute schedule
        ArrayList<ContractEvent> schedule = PlainVanillaInterestRateSwap.schedule(LocalDateTime.parse(model.getAs("MaturityDate")),model);

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-",EndOfMonthConventionEnum.SD),
            EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_SWPPV_schedule_withIP_withSTDwhereS_withPRD_withTD() {
        thrown = ExpectedException.none();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RF");
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

        // compute schedule
        ArrayList<ContractEvent> schedule = PlainVanillaInterestRateSwap.schedule(LocalDateTime.parse(model.getAs("MaturityDate")),model);

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-",EndOfMonthConventionEnum.SD),
            EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.apply(schedule,model,riskFactors);
    }
    
    @Test
    public void test_SWPPV_schedule_withIP_withSTDwhereD_withPRD_withMultipleAnalysisTimes() {
        thrown = ExpectedException.none();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RF");
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

        // compute schedule
        ArrayList<ContractEvent> schedule = PlainVanillaInterestRateSwap.schedule(LocalDateTime.parse(model.getAs("MaturityDate")),model);

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-",EndOfMonthConventionEnum.SD),
            EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.apply(schedule,model,riskFactors);
    }

    @Test
    public void test_SWPPV_schedule_withIP_withSTDwhereS_withPRD_withMultipleAnalysisTimes() {
        thrown = ExpectedException.none();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ContractType", "SWPPV");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RF");
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

        // compute schedule
        ArrayList<ContractEvent> schedule = PlainVanillaInterestRateSwap.schedule(LocalDateTime.parse(model.getAs("MaturityDate")),model);

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-",EndOfMonthConventionEnum.SD),
            EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = PlainVanillaInterestRateSwap.apply(schedule,model,riskFactors);
    }
}

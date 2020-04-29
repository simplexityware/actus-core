/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import org.actus.attributes.ContractModel;
import org.actus.attributes.ContractModelProvider;
import org.actus.events.ContractEvent;
import org.actus.events.EventFactory;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.pam.POF_AD_PAM;
import org.actus.functions.pam.STF_AD_PAM;
import org.actus.states.StateSpace;
import org.actus.time.ScheduleFactory;
import org.actus.util.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.LocalDateTime;
import java.util.*;

public class SwapTest {
    
    class MarketModel implements RiskFactorModelProvider {
        public Set<String> keys() {
            Set<String> keys = new HashSet<String>();
            return keys;
        }

        @Override
        public double stateAt(String id,LocalDateTime time,StateSpace contractStates,ContractModelProvider contractAttributes) {
            return 0.0;    
        }

        @Override
        public double stateAt(String id,LocalDateTime time){
            return Math.random();
        }
    }
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test_SWAPS_schedule_MandatoryAttributes() {
        thrown = ExpectedException.none();

        // define parent attributes
        Map<String, String> parent = new HashMap<String, String>();
        parent.put("ContractType", "SWAPS");
        parent.put("ContractID", "XYZ");
        parent.put("StatusDate", "2016-01-01T00:00:00");
        parent.put("ContractRole", "RFL");
        parent.put("LegalEntityIDCounterparty", "CORP-XY");
        parent.put("Currency", "USD");

        // define child 1 attributes
        Map<String, String> child1 = new HashMap<String, String>();
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
        Map<String, String> child2 = new HashMap<String, String>();
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

        // combine child attributes in list
        ArrayList child = new ArrayList();
        child.add(child1);
        child.add(child2);

        // parse attributes
        ContractModel model = ContractModel.parse(parent, child);

        // compute schedule
        ArrayList<ContractEvent> schedule = Swap.schedule(LocalDateTime.parse(child1.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Swap.apply(schedule,model,riskFactors);
    }

    @Test
    public void test_SWAPS_schedule_withDelivery() {
        thrown = ExpectedException.none();

        // define parent attributes
        Map<String, String> parent = new HashMap<String, String>();
        parent.put("ContractType", "SWAPS");
        parent.put("ContractID", "XYZ");
        parent.put("StatusDate", "2016-01-01T00:00:00");
        parent.put("ContractRole", "RFL");
        parent.put("LegalEntityIDCounterparty", "CORP-XY");
        parent.put("Currency", "USD");
        parent.put("DeliverySettlement", "D");

        // define child 1 attributes
        Map<String, String> child1 = new HashMap<String, String>();
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
        Map<String, String> child2 = new HashMap<String, String>();
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

        // combine child attributes in list
        ArrayList child = new ArrayList();
        child.add(child1);
        child.add(child2);

        // parse attributes
        ContractModel model = ContractModel.parse(parent, child);

        // compute schedule
        ArrayList<ContractEvent> schedule = Swap.schedule(LocalDateTime.parse(child1.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Swap.apply(schedule,model,riskFactors);
    }

    @Test
    public void test_SWAPS_schedule_withSettlement() {
        thrown = ExpectedException.none();

        // define parent attributes
        Map<String, String> parent = new HashMap<String, String>();
        parent.put("ContractType", "SWAPS");
        parent.put("ContractID", "XYZ");
        parent.put("StatusDate", "2016-01-01T00:00:00");
        parent.put("ContractRole", "RFL");
        parent.put("LegalEntityIDCounterparty", "CORP-XY");
        parent.put("Currency", "USD");
        parent.put("DeliverySettlement", "S");

        // define child 1 attributes
        Map<String, String> child1 = new HashMap<String, String>();
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
        Map<String, String> child2 = new HashMap<String, String>();
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

        // combine child attributes in list
        ArrayList child = new ArrayList();
        child.add(child1);
        child.add(child2);

        // parse attributes
        ContractModel model = ContractModel.parse(parent, child);

        // compute schedule
        ArrayList<ContractEvent> schedule = Swap.schedule(LocalDateTime.parse(child1.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Swap.apply(schedule,model,riskFactors);
    }

    @Test
    public void test_SWAPS_schedule_withSettlement_withPurchase() {
        thrown = ExpectedException.none();

        // define parent attributes
        Map<String, String> parent = new HashMap<String, String>();
        parent.put("ContractType", "SWAPS");
        parent.put("ContractID", "XYZ");
        parent.put("StatusDate", "2016-01-01T00:00:00");
        parent.put("ContractRole", "RFL");
        parent.put("LegalEntityIDCounterparty", "CORP-XY");
        parent.put("Currency", "USD");
        parent.put("DeliverySettlement", "S");
        parent.put("PurchaseDate", "2016-05-01T00:00:00");
        parent.put("PriceAtPurchaseDate", "-95");

        // define child 1 attributes
        Map<String, String> child1 = new HashMap<String, String>();
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
        Map<String, String> child2 = new HashMap<String, String>();
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

        // combine child attributes in list
        ArrayList child = new ArrayList();
        child.add(child1);
        child.add(child2);

        // parse attributes
        ContractModel model = ContractModel.parse(parent, child);

        // compute schedule
        ArrayList<ContractEvent> schedule = Swap.schedule(LocalDateTime.parse(child1.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Swap.apply(schedule,model,riskFactors);
    }

    @Test
    public void test_SWAPS_schedule_withSettlement_withTermination() {
        thrown = ExpectedException.none();

        // define parent attributes
        Map<String, String> parent = new HashMap<String, String>();
        parent.put("ContractType", "SWAPS");
        parent.put("ContractID", "XYZ");
        parent.put("StatusDate", "2016-01-01T00:00:00");
        parent.put("ContractRole", "RFL");
        parent.put("LegalEntityIDCounterparty", "CORP-XY");
        parent.put("Currency", "USD");
        parent.put("DeliverySettlement", "S");
        parent.put("TerminationDate", "2016-05-01T00:00:00");
        parent.put("PriceAtTerminationDate", "105");

        // define child 1 attributes
        Map<String, String> child1 = new HashMap<String, String>();
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
        Map<String, String> child2 = new HashMap<String, String>();
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

        // combine child attributes in list
        ArrayList child = new ArrayList();
        child.add(child1);
        child.add(child2);

        // parse attributes
        ContractModel model = ContractModel.parse(parent, child);

        // compute schedule
        ArrayList<ContractEvent> schedule = Swap.schedule(LocalDateTime.parse(child1.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Swap.apply(schedule,model,riskFactors);
    }

    @Test
    public void test_SWAPS_schedule_withSettlement_withPurchaseAndTermination() {
        thrown = ExpectedException.none();

        // define parent attributes
        Map<String, String> parent = new HashMap<String, String>();
        parent.put("ContractType", "SWAPS");
        parent.put("ContractID", "XYZ");
        parent.put("StatusDate", "2016-01-01T00:00:00");
        parent.put("ContractRole", "RFL");
        parent.put("LegalEntityIDCounterparty", "CORP-XY");
        parent.put("Currency", "USD");
        parent.put("DeliverySettlement", "S");
        parent.put("PurchaseDate", "2016-05-01T00:00:00");
        parent.put("PriceAtPurchaseDate", "-95");
        parent.put("TerminationDate", "2016-11-01T00:00:00");
        parent.put("PriceAtTerminationDate", "105");

        // define child 1 attributes
        Map<String, String> child1 = new HashMap<String, String>();
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
        Map<String, String> child2 = new HashMap<String, String>();
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

        // combine child attributes in list
        ArrayList child = new ArrayList();
        child.add(child1);
        child.add(child2);

        // parse attributes
        ContractModel model = ContractModel.parse(parent, child);

        // compute schedule
        ArrayList<ContractEvent> schedule = Swap.schedule(LocalDateTime.parse(child1.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Swap.apply(schedule,model,riskFactors);
    }

    @Test
    public void test_SWAPS_schedule_withDelivery_withPurchaseAndTermination() {
        thrown = ExpectedException.none();

        // define parent attributes
        Map<String, String> parent = new HashMap<String, String>();
        parent.put("ContractType", "SWAPS");
        parent.put("ContractID", "XYZ");
        parent.put("StatusDate", "2016-01-01T00:00:00");
        parent.put("ContractRole", "RFL");
        parent.put("LegalEntityIDCounterparty", "CORP-XY");
        parent.put("Currency", "USD");
        parent.put("DeliverySettlement", "D");
        parent.put("PurchaseDate", "2016-05-01T00:00:00");
        parent.put("PriceAtPurchaseDate", "-95");
        parent.put("TerminationDate", "2016-11-01T00:00:00");
        parent.put("PriceAtTerminationDate", "105");

        // define child 1 attributes
        Map<String, String> child1 = new HashMap<String, String>();
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
        Map<String, String> child2 = new HashMap<String, String>();
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

        // combine child attributes in list
        ArrayList child = new ArrayList();
        child.add(child1);
        child.add(child2);

        // parse attributes
        ContractModel model = ContractModel.parse(parent, child);

        // compute schedule
        ArrayList<ContractEvent> schedule = Swap.schedule(LocalDateTime.parse(child1.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Swap.apply(schedule,model,riskFactors);
    }
}

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
import org.actus.types.EndOfMonthConventionEnum;
import org.actus.types.EventType;
import org.actus.types.ContractReference;
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

    }
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test_SWAPS_schedule_MandatoryAttributes() {
        thrown = ExpectedException.none();

        // define map attributes
        Map<String, Object> map = new HashMap<>();
        map.put("ContractType", "SWAPS");
        map.put("ContractID", "XYZ");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RFL");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("Currency", "USD");

        // define child 1 attributes
        Map<String, String> childObject1 = new HashMap<String, String>();
        childObject1.put("ContractType", "PAM");
        childObject1.put("ContractID", "XYZ_C1");
        childObject1.put("Calendar", "NoHolidayCalendar");
        childObject1.put("StatusDate", "2016-01-01T00:00:00");
        childObject1.put("LegalEntityIDCounterparty", "CORP-XY");
        childObject1.put("DayCountConvention", "A/AISDA");
        childObject1.put("Currency", "USD");
        childObject1.put("InitialExchangeDate", "2016-01-02T00:00:00");
        childObject1.put("MaturityDate", "2017-01-01T00:00:00");
        childObject1.put("NotionalPrincipal", "1000.0");
        childObject1.put("NominalInterestRate","0.01");
        childObject1.put("CycleOfInterestPayment","P1ML0");

        // define child 2 attributes
        Map<String, String> childObject2 = new HashMap<String, String>();
        childObject2.put("ContractType", "PAM");
        childObject2.put("ContractID", "XYZ_C2");
        childObject2.put("Calendar", "NoHolidayCalendar");
        childObject2.put("StatusDate", "2016-01-01T00:00:00");
        childObject2.put("LegalEntityIDCounterparty", "CORP-XY");
        childObject2.put("DayCountConvention", "A/AISDA");
        childObject2.put("Currency", "USD");
        childObject2.put("InitialExchangeDate", "2016-01-02T00:00:00");
        childObject2.put("MaturityDate", "2017-01-01T00:00:00");
        childObject2.put("NotionalPrincipal", "1000.0");
        childObject2.put("NominalInterestRate","0.01");
        childObject2.put("CycleOfInterestPayment","P1ML0");
        childObject2.put("CycleOfRateReset","P3ML1");
        childObject2.put("MarketObjectCodeOfRateReset","LIBOR_3M");

        // create attribute ContractRefernce
        Map<String,Object> contractReference1 = new HashMap<>();
        Map<String,Object> contractReference2 = new HashMap<>();
        contractReference1.put("ReferenceRole", "FIL");
        contractReference1.put("ReferenceType", "CNT");
        contractReference1.put("Object",childObject1);
        contractReference2.put("ReferenceRole", "SEL");
        contractReference2.put("ReferenceType", "CNT");
        contractReference2.put("Object",childObject2);

        // create and add attribute ContractStructure to map
        List<Map<String,Object>> contractStructure = new ArrayList<>();
        contractStructure.add(contractReference1);
        contractStructure.add(contractReference2);
        map.put("ContractStructure", contractStructure);

        // parse attributes
        ContractModel model = ContractModel.parse(map);

        // compute schedule
        ArrayList<ContractEvent> schedule = Swap.schedule(LocalDateTime.parse(childObject1.get("MaturityDate")),model);

        // add analysis events
        ContractModel childModel  = (ContractModel)((List<ContractReference>)model.getAs("ContractStructure")).get(0).<ContractModel>getObject();
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(childModel.getAs("InitialExchangeDate"),childModel.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"P3ML1",EndOfMonthConventionEnum.SD),
            EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM(), ));

        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Swap.apply(schedule,model,riskFactors);
    }

    @Test
    public void test_SWAPS_schedule_withDelivery() {
        thrown = ExpectedException.none();

        // define map attributes
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ContractType", "SWAPS");
        map.put("ContractID", "XYZ");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RFL");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("Currency", "USD");
        map.put("DeliverySettlement", "D");

        // define child 1 attributes
        Map<String, String> childObject1 = new HashMap<String, String>();
        childObject1.put("ContractType", "PAM");
        childObject1.put("ContractID", "XYZ_C1");
        childObject1.put("Calendar", "NoHolidayCalendar");
        childObject1.put("StatusDate", "2016-01-01T00:00:00");
        childObject1.put("LegalEntityIDCounterparty", "CORP-XY");
        childObject1.put("DayCountConvention", "A/AISDA");
        childObject1.put("Currency", "USD");
        childObject1.put("InitialExchangeDate", "2016-01-02T00:00:00");
        childObject1.put("MaturityDate", "2017-01-01T00:00:00");
        childObject1.put("NotionalPrincipal", "1000.0");
        childObject1.put("NominalInterestRate","0.01");
        childObject1.put("CycleOfInterestPayment","P1ML0");

        // define child 2 attributes
        Map<String, String> childObject2 = new HashMap<String, String>();
        childObject2.put("ContractType", "PAM");
        childObject2.put("ContractID", "XYZ_C2");
        childObject2.put("Calendar", "NoHolidayCalendar");
        childObject2.put("StatusDate", "2016-01-01T00:00:00");
        childObject2.put("LegalEntityIDCounterparty", "CORP-XY");
        childObject2.put("DayCountConvention", "A/AISDA");
        childObject2.put("Currency", "USD");
        childObject2.put("InitialExchangeDate", "2016-01-02T00:00:00");
        childObject2.put("MaturityDate", "2017-01-01T00:00:00");
        childObject2.put("NotionalPrincipal", "1000.0");
        childObject2.put("NominalInterestRate","0.01");
        childObject2.put("CycleOfInterestPayment","P1ML0");
        childObject2.put("CycleOfRateReset","P3ML1");
        childObject2.put("MarketObjectCodeOfRateReset","LIBOR_3M");

        // create attribute ContractRefernce
        Map<String,Object> contractReference1 = new HashMap<>();
        Map<String,Object> contractReference2 = new HashMap<>();
        contractReference1.put("ReferenceRole", "FIL");
        contractReference1.put("ReferenceType", "CNT");
        contractReference1.put("Object",childObject1);
        contractReference2.put("ReferenceRole", "SEL");
        contractReference2.put("ReferenceType", "CNT");
        contractReference2.put("Object",childObject2);

        // create and add attribute ContractStructure to map
        List<Map<String,Object>> contractStructure = new ArrayList<>();
        contractStructure.add(contractReference1);
        contractStructure.add(contractReference2);
        map.put("ContractStructure", contractStructure);

        // parse attributes
        ContractModel model = ContractModel.parse(map);

        // compute schedule
        ArrayList<ContractEvent> schedule = Swap.schedule(LocalDateTime.parse(childObject1.get("MaturityDate")),model);

        // add analysis events
        ContractModel childModel  = (ContractModel)((List<ContractReference>)model.getAs("ContractStructure")).get(0).<ContractModel>getObject();
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(childModel.getAs("InitialExchangeDate"),childModel.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"P3ML1",EndOfMonthConventionEnum.SD),
            EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM(), ));

        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Swap.apply(schedule,model,riskFactors);
    }

    @Test
    public void test_SWAPS_schedule_withSettlement() {
        thrown = ExpectedException.none();

        // define map attributes
        Map<String, Object> map = new HashMap<>();
        map.put("ContractType", "SWAPS");
        map.put("ContractID", "XYZ");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RFL");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("Currency", "USD");
        map.put("DeliverySettlement", "S");

        // define child 1 attributes
        Map<String, String> childObject1 = new HashMap<String, String>();
        childObject1.put("ContractType", "PAM");
        childObject1.put("ContractID", "XYZ_C1");
        childObject1.put("Calendar", "NoHolidayCalendar");
        childObject1.put("StatusDate", "2016-01-01T00:00:00");
        childObject1.put("LegalEntityIDCounterparty", "CORP-XY");
        childObject1.put("DayCountConvention", "A/AISDA");
        childObject1.put("Currency", "USD");
        childObject1.put("InitialExchangeDate", "2016-01-02T00:00:00");
        childObject1.put("MaturityDate", "2017-01-01T00:00:00");
        childObject1.put("NotionalPrincipal", "1000.0");
        childObject1.put("NominalInterestRate","0.01");
        childObject1.put("CycleOfInterestPayment","P1ML0");

        // define child 2 attributes
        Map<String, String> childObject2 = new HashMap<String, String>();
        childObject2.put("ContractType", "PAM");
        childObject2.put("ContractID", "XYZ_C2");
        childObject2.put("Calendar", "NoHolidayCalendar");
        childObject2.put("StatusDate", "2016-01-01T00:00:00");
        childObject2.put("LegalEntityIDCounterparty", "CORP-XY");
        childObject2.put("DayCountConvention", "A/AISDA");
        childObject2.put("Currency", "USD");
        childObject2.put("InitialExchangeDate", "2016-01-02T00:00:00");
        childObject2.put("MaturityDate", "2017-01-01T00:00:00");
        childObject2.put("NotionalPrincipal", "1000.0");
        childObject2.put("NominalInterestRate","0.01");
        childObject2.put("CycleOfInterestPayment","P1ML0");
        childObject2.put("CycleOfRateReset","P3ML1");
        childObject2.put("MarketObjectCodeOfRateReset","LIBOR_3M");

        // create attribute ContractRefernce
        Map<String,Object> contractReference1 = new HashMap<>();
        Map<String,Object> contractReference2 = new HashMap<>();
        contractReference1.put("ReferenceRole", "FIL");
        contractReference1.put("ReferenceType", "CNT");
        contractReference1.put("Object",childObject1);
        contractReference2.put("ReferenceRole", "SEL");
        contractReference2.put("ReferenceType", "CNT");
        contractReference2.put("Object",childObject2);

        // create and add attribute ContractStructure to map
        List<Map<String,Object>> contractStructure = new ArrayList<>();
        contractStructure.add(contractReference1);
        contractStructure.add(contractReference2);
        map.put("ContractStructure", contractStructure);

        // parse attributes
        ContractModel model = ContractModel.parse(map);

        // compute schedule
        ArrayList<ContractEvent> schedule = Swap.schedule(LocalDateTime.parse(childObject1.get("MaturityDate")),model);

        // add analysis events
        ContractModel childModel  = (ContractModel)((List<ContractReference>)model.getAs("ContractStructure")).get(0).<ContractModel>getObject();
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(childModel.getAs("InitialExchangeDate"),childModel.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"P3ML1",EndOfMonthConventionEnum.SD),
            EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM(), ));

        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Swap.apply(schedule,model,riskFactors);
    }

    @Test
    public void test_SWAPS_schedule_withSettlement_withPurchase() {
        thrown = ExpectedException.none();

        // define map attributes
        Map<String, Object> map = new HashMap<>();
        map.put("ContractType", "SWAPS");
        map.put("ContractID", "XYZ");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RFL");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("Currency", "USD");
        map.put("DeliverySettlement", "S");
        map.put("PurchaseDate", "2016-05-01T00:00:00");
        map.put("PriceAtPurchaseDate", "-95");

        // define child 1 attributes
        Map<String, String> childObject1 = new HashMap<String, String>();
        childObject1.put("ContractType", "PAM");
        childObject1.put("ContractID", "XYZ_C1");
        childObject1.put("Calendar", "NoHolidayCalendar");
        childObject1.put("StatusDate", "2016-01-01T00:00:00");
        childObject1.put("LegalEntityIDCounterparty", "CORP-XY");
        childObject1.put("DayCountConvention", "A/AISDA");
        childObject1.put("Currency", "USD");
        childObject1.put("InitialExchangeDate", "2016-01-02T00:00:00");
        childObject1.put("MaturityDate", "2017-01-01T00:00:00");
        childObject1.put("NotionalPrincipal", "1000.0");
        childObject1.put("NominalInterestRate","0.01");
        childObject1.put("CycleOfInterestPayment","P1ML0");

        // define child 2 attributes
        Map<String, String> childObject2 = new HashMap<String, String>();
        childObject2.put("ContractType", "PAM");
        childObject2.put("ContractID", "XYZ_C2");
        childObject2.put("Calendar", "NoHolidayCalendar");
        childObject2.put("StatusDate", "2016-01-01T00:00:00");
        childObject2.put("LegalEntityIDCounterparty", "CORP-XY");
        childObject2.put("DayCountConvention", "A/AISDA");
        childObject2.put("Currency", "USD");
        childObject2.put("InitialExchangeDate", "2016-01-02T00:00:00");
        childObject2.put("MaturityDate", "2017-01-01T00:00:00");
        childObject2.put("NotionalPrincipal", "1000.0");
        childObject2.put("NominalInterestRate","0.01");
        childObject2.put("CycleOfInterestPayment","P1ML0");
        childObject2.put("CycleOfRateReset","P3ML1");
        childObject2.put("MarketObjectCodeOfRateReset","LIBOR_3M");

        // create attribute ContractRefernce
        Map<String,Object> contractReference1 = new HashMap<>();
        Map<String,Object> contractReference2 = new HashMap<>();
        contractReference1.put("ReferenceRole", "FIL");
        contractReference1.put("ReferenceType", "CNT");
        contractReference1.put("Object",childObject1);
        contractReference2.put("ReferenceRole", "SEL");
        contractReference2.put("ReferenceType", "CNT");
        contractReference2.put("Object",childObject2);

        // create and add attribute ContractStructure to map
        List<Map<String,Object>> contractStructure = new ArrayList<>();
        contractStructure.add(contractReference1);
        contractStructure.add(contractReference2);
        map.put("ContractStructure", contractStructure);

        // parse attributes
        ContractModel model = ContractModel.parse(map);

        // compute schedule
        ArrayList<ContractEvent> schedule = Swap.schedule(LocalDateTime.parse(childObject1.get("MaturityDate")),model);

        // add analysis events
        ContractModel childModel  = (ContractModel)((List<ContractReference>)model.getAs("ContractStructure")).get(0).<ContractModel>getObject();
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(childModel.getAs("InitialExchangeDate"),childModel.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"P3ML1",EndOfMonthConventionEnum.SD),
            EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM(), ));

        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Swap.apply(schedule,model,riskFactors);
    }

    @Test
    public void test_SWAPS_schedule_withSettlement_withTermination() {
        thrown = ExpectedException.none();

        // define map attributes
        Map<String, Object> map = new HashMap<>();
        map.put("ContractType", "SWAPS");
        map.put("ContractID", "XYZ");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RFL");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("Currency", "USD");
        map.put("DeliverySettlement", "S");
        map.put("TerminationDate", "2016-05-01T00:00:00");
        map.put("PriceAtTerminationDate", "105");

        // define child 1 attributes
        Map<String, String> childObject1 = new HashMap<String, String>();
        childObject1.put("ContractType", "PAM");
        childObject1.put("ContractID", "XYZ_C1");
        childObject1.put("Calendar", "NoHolidayCalendar");
        childObject1.put("StatusDate", "2016-01-01T00:00:00");
        childObject1.put("LegalEntityIDCounterparty", "CORP-XY");
        childObject1.put("DayCountConvention", "A/AISDA");
        childObject1.put("Currency", "USD");
        childObject1.put("InitialExchangeDate", "2016-01-02T00:00:00");
        childObject1.put("MaturityDate", "2017-01-01T00:00:00");
        childObject1.put("NotionalPrincipal", "1000.0");
        childObject1.put("NominalInterestRate","0.01");
        childObject1.put("CycleOfInterestPayment","P1ML0");

        // define child 2 attributes
        Map<String, String> childObject2 = new HashMap<String, String>();
        childObject2.put("ContractType", "PAM");
        childObject2.put("ContractID", "XYZ_C2");
        childObject2.put("Calendar", "NoHolidayCalendar");
        childObject2.put("StatusDate", "2016-01-01T00:00:00");
        childObject2.put("LegalEntityIDCounterparty", "CORP-XY");
        childObject2.put("DayCountConvention", "A/AISDA");
        childObject2.put("Currency", "USD");
        childObject2.put("InitialExchangeDate", "2016-01-02T00:00:00");
        childObject2.put("MaturityDate", "2017-01-01T00:00:00");
        childObject2.put("NotionalPrincipal", "1000.0");
        childObject2.put("NominalInterestRate","0.01");
        childObject2.put("CycleOfInterestPayment","P1ML0");
        childObject2.put("CycleOfRateReset","P3ML1");
        childObject2.put("MarketObjectCodeOfRateReset","LIBOR_3M");

        // create attribute ContractRefernce
        Map<String,Object> contractReference1 = new HashMap<>();
        Map<String,Object> contractReference2 = new HashMap<>();
        contractReference1.put("ReferenceRole", "FIL");
        contractReference1.put("ReferenceType", "CNT");
        contractReference1.put("Object",childObject1);
        contractReference2.put("ReferenceRole", "SEL");
        contractReference2.put("ReferenceType", "CNT");
        contractReference2.put("Object",childObject2);

        // create and add attribute ContractStructure to map
        List<Map<String,Object>> contractStructure = new ArrayList<>();
        contractStructure.add(contractReference1);
        contractStructure.add(contractReference2);
        map.put("ContractStructure", contractStructure);

        // parse attributes
        ContractModel model = ContractModel.parse(map);

        // compute schedule
        ArrayList<ContractEvent> schedule = Swap.schedule(LocalDateTime.parse(childObject1.get("MaturityDate")),model);

        // add analysis events
        ContractModel childModel  = (ContractModel)((List<ContractReference>)model.getAs("ContractStructure")).get(0).<ContractModel>getObject();
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(childModel.getAs("InitialExchangeDate"),childModel.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"P3ML1",EndOfMonthConventionEnum.SD),
            EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM(), ));

        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Swap.apply(schedule,model,riskFactors);
    }

    @Test
    public void test_SWAPS_schedule_withSettlement_withPurchaseAndTermination() {
        thrown = ExpectedException.none();

        // define map attributes
        Map<String, Object> map = new HashMap<>();
        map.put("ContractType", "SWAPS");
        map.put("ContractID", "XYZ");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RFL");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("Currency", "USD");
        map.put("DeliverySettlement", "S");
        map.put("PurchaseDate", "2016-05-01T00:00:00");
        map.put("PriceAtPurchaseDate", "-95");
        map.put("TerminationDate", "2016-11-01T00:00:00");
        map.put("PriceAtTerminationDate", "105");

        // define child 1 attributes
        Map<String, String> childObject1 = new HashMap<String, String>();
        childObject1.put("ContractType", "PAM");
        childObject1.put("ContractID", "XYZ_C1");
        childObject1.put("Calendar", "NoHolidayCalendar");
        childObject1.put("StatusDate", "2016-01-01T00:00:00");
        childObject1.put("LegalEntityIDCounterparty", "CORP-XY");
        childObject1.put("DayCountConvention", "A/AISDA");
        childObject1.put("Currency", "USD");
        childObject1.put("InitialExchangeDate", "2016-01-02T00:00:00");
        childObject1.put("MaturityDate", "2017-01-01T00:00:00");
        childObject1.put("NotionalPrincipal", "1000.0");
        childObject1.put("NominalInterestRate","0.01");
        childObject1.put("CycleOfInterestPayment","P1ML0");

        // define child 2 attributes
        Map<String, String> childObject2 = new HashMap<String, String>();
        childObject2.put("ContractType", "PAM");
        childObject2.put("ContractID", "XYZ_C2");
        childObject2.put("Calendar", "NoHolidayCalendar");
        childObject2.put("StatusDate", "2016-01-01T00:00:00");
        childObject2.put("LegalEntityIDCounterparty", "CORP-XY");
        childObject2.put("DayCountConvention", "A/AISDA");
        childObject2.put("Currency", "USD");
        childObject2.put("InitialExchangeDate", "2016-01-02T00:00:00");
        childObject2.put("MaturityDate", "2017-01-01T00:00:00");
        childObject2.put("NotionalPrincipal", "1000.0");
        childObject2.put("NominalInterestRate","0.01");
        childObject2.put("CycleOfInterestPayment","P1ML0");
        childObject2.put("CycleOfRateReset","P3ML1");
        childObject2.put("MarketObjectCodeOfRateReset","LIBOR_3M");

        // create attribute ContractRefernce
        Map<String,Object> contractReference1 = new HashMap<>();
        Map<String,Object> contractReference2 = new HashMap<>();
        contractReference1.put("ReferenceRole", "FIL");
        contractReference1.put("ReferenceType", "CNT");
        contractReference1.put("Object",childObject1);
        contractReference2.put("ReferenceRole", "SEL");
        contractReference2.put("ReferenceType", "CNT");
        contractReference2.put("Object",childObject2);

        // create and add attribute ContractStructure to map
        List<Map<String,Object>> contractStructure = new ArrayList<>();
        contractStructure.add(contractReference1);
        contractStructure.add(contractReference2);
        map.put("ContractStructure", contractStructure);

        // parse attributes
        ContractModel model = ContractModel.parse(map);

        // compute schedule
        ArrayList<ContractEvent> schedule = Swap.schedule(LocalDateTime.parse(childObject1.get("MaturityDate")),model);

        // add analysis events
        ContractModel childModel  = (ContractModel)((List<ContractReference>)model.getAs("ContractStructure")).get(0).<ContractModel>getObject();
        schedule.addAll(EventFactory.createEvents(
                ScheduleFactory.createSchedule(childModel.getAs("InitialExchangeDate"),childModel.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"P3ML1",EndOfMonthConventionEnum.SD),
                EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM(), ));
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Swap.apply(schedule,model,riskFactors);
    }

    @Test
    public void test_SWAPS_schedule_withDelivery_withPurchaseAndTermination() {
        thrown = ExpectedException.none();

        // define map attributes
        Map<String, Object> map = new HashMap<>();
        map.put("ContractType", "SWAPS");
        map.put("ContractID", "XYZ");
        map.put("StatusDate", "2016-01-01T00:00:00");
        map.put("ContractRole", "RFL");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("Currency", "USD");
        map.put("DeliverySettlement", "D");
        map.put("PurchaseDate", "2016-05-01T00:00:00");
        map.put("PriceAtPurchaseDate", "-95");
        map.put("TerminationDate", "2016-11-01T00:00:00");
        map.put("PriceAtTerminationDate", "105");

        // define child 1 attributes
        Map<String, String> childObject1 = new HashMap<String, String>();
        childObject1.put("ContractType", "PAM");
        childObject1.put("ContractID", "XYZ_C1");
        childObject1.put("Calendar", "NoHolidayCalendar");
        childObject1.put("StatusDate", "2016-01-01T00:00:00");
        childObject1.put("LegalEntityIDCounterparty", "CORP-XY");
        childObject1.put("DayCountConvention", "A/AISDA");
        childObject1.put("Currency", "USD");
        childObject1.put("InitialExchangeDate", "2016-01-02T00:00:00");
        childObject1.put("MaturityDate", "2017-01-01T00:00:00");
        childObject1.put("NotionalPrincipal", "1000.0");
        childObject1.put("NominalInterestRate","0.01");
        childObject1.put("CycleOfInterestPayment","P1ML0");

        // define child 2 attributes
        Map<String, String> childObject2 = new HashMap<String, String>();
        childObject2.put("ContractType", "PAM");
        childObject2.put("ContractID", "XYZ_C2");
        childObject2.put("Calendar", "NoHolidayCalendar");
        childObject2.put("StatusDate", "2016-01-01T00:00:00");
        childObject2.put("LegalEntityIDCounterparty", "CORP-XY");
        childObject2.put("DayCountConvention", "A/AISDA");
        childObject2.put("Currency", "USD");
        childObject2.put("InitialExchangeDate", "2016-01-02T00:00:00");
        childObject2.put("MaturityDate", "2017-01-01T00:00:00");
        childObject2.put("NotionalPrincipal", "1000.0");
        childObject2.put("NominalInterestRate","0.01");
        childObject2.put("CycleOfInterestPayment","P1ML0");
        childObject2.put("CycleOfRateReset","P3ML1");
        childObject2.put("MarketObjectCodeOfRateReset","LIBOR_3M");

        // create attribute ContractRefernce
        Map<String,Object> contractReference1 = new HashMap<>();
        Map<String,Object> contractReference2 = new HashMap<>();
        contractReference1.put("ReferenceRole", "FIL");
        contractReference1.put("ReferenceType", "CNT");
        contractReference1.put("Object",childObject1);
        contractReference2.put("ReferenceRole", "SEL");
        contractReference2.put("ReferenceType", "CNT");
        contractReference2.put("Object",childObject2);

        // create and add attribute ContractStructure to map
        List<Map<String,Object>> contractStructure = new ArrayList<>();
        contractStructure.add(contractReference1);
        contractStructure.add(contractReference2);
        map.put("ContractStructure", contractStructure);

        // parse attributes
        ContractModel model = ContractModel.parse(map);

        // compute schedule
        ArrayList<ContractEvent> schedule = Swap.schedule(LocalDateTime.parse(childObject1.get("MaturityDate")),model);

        // add analysis events
        ContractModel childModel  = (ContractModel)((List<ContractReference>)model.getAs("ContractStructure")).get(0).<ContractModel>getObject();
        schedule.addAll(EventFactory.createEvents(
                ScheduleFactory.createSchedule(childModel.getAs("InitialExchangeDate"),childModel.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"P3ML1",EndOfMonthConventionEnum.SD),
                EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM(), ));
    
        // define risk factor model
        MarketModel riskFactors = new MarketModel();

        // apply events
        ArrayList<ContractEvent> events = Swap.apply(schedule,model,riskFactors);
    }
}

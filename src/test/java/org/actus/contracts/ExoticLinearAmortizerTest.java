/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ExoticLinearAmortizerTest {

	class MarketModel implements RiskFactorModelProvider {
		public Set<String> keys() {
			Set<String> keys = new HashSet<String>();
			return keys;
		}

		@Override
		public double stateAt(String id, LocalDateTime time, StateSpace contractStates,
							  ContractModelProvider contractAttributes) {
			return 0.0;
		}

	}

	class ValuedMarketModel implements RiskFactorModelProvider {
		private SortedMap<LocalDateTime, Double> series = null;

		ValuedMarketModel(LocalDateTime[] dateTimes, Double[] values) {
			SortedMap<LocalDateTime, Double> newMap = new TreeMap<LocalDateTime, Double>();
			for (int i = 0; i < values.length; i++) {
				newMap.put(dateTimes[i], values[i]);
			}
			series = newMap;
		}

		public Set<String> keys() {
			Set<String> keys = new HashSet<String>();
			return keys;
		}

		@Override
		public double stateAt(String id, LocalDateTime time, StateSpace contractStates,
							  ContractModelProvider contractAttributes) {
			SortedMap<LocalDateTime, Double> head = series.subMap(series.firstKey(), time);
			return head.get(head.lastKey());
		}

	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void test_LAX_schedule_MandatoryAttributes() {
		thrown = ExpectedException.none();
		// define attributes
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("contractType", "LAX");
		map.put("calendar", "NoHolidayCalendar");
		map.put("statusDate", "2012-12-30T00:00:00");
		map.put("contractRole", "RPL");
		map.put("dayCountConvention", "AA");
		map.put("currency", "USD");
		map.put("initialExchangeDate", "2013-01-01T00:00:00");
		map.put("maturityDate", "2015-06-01T00:00:00");
		map.put("notionalPrincipal", "2000.0");
		map.put("nominalInterestRate", "0.08");
		map.put("arrayCycleAnchorDateOfPrincipalRedemption",
				"2013-02-01T00:00:00,2013-09-01T00:00:00,2014-04-01T00:00:00");
		map.put("arrayCycleOfPrincipalRedemption", "P2ML1,P1ML1,P1ML0");
		map.put("arrayNextPrincipalRedemptionPayment", "250.0,100.0,200.0");
		map.put("arrayIncreaseDecrease", "DEC,INC,DEC");

		// parse attributes
		ContractModel model = ContractModel.parse(map);

		// compute schedule
		ArrayList<ContractEvent> schedule = ExoticLinearAmortizer.schedule(model.getAs("MaturityDate"),
				model);

		// add analysis events
		schedule.addAll(EventFactory.createEvents(
				ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),
						model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(0), "P1ML1", EndOfMonthConventionEnum.SD),
				EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM(), model.getAs("ContractID")));

		// define risk factor model
		LocalDateTime[] times = {LocalDateTime.parse("2012-06-01T00:00:00"),
				LocalDateTime.parse("2013-06-01T00:00:00"), LocalDateTime.parse("2013-12-01T00:00:00"),
				LocalDateTime.parse("2014-06-01T00:00:00"), LocalDateTime.parse("2014-12-01T00:00:00"),
				LocalDateTime.parse("2015-06-01T00:00:00")};
		Double[] values = {2.0, 1.2, 1.5, 0.8, 1.3, 0.5};
		ValuedMarketModel riskFactors = new ValuedMarketModel(times, values);
		// apply events
		ArrayList<ContractEvent> events = ExoticLinearAmortizer.apply(schedule, model, riskFactors);
	}

	@Test
	public void test_LAX_schedule_MandatoryAttributes_withMaturity() {
		thrown = ExpectedException.none();
		// define attributes
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("contractType", "LAX");
		map.put("calendar", "NoHolidayCalendar");
		map.put("statusDate", "2012-12-30T00:00:00");
		map.put("contractRole", "RPL");
		map.put("legalEntityIDCounterparty", "CORP-XY");
		map.put("dayCountConvention", "AA");
		map.put("currency", "USD");
		map.put("initialExchangeDate", "2013-01-01T00:00:00");
		 map.put("cycleOfPrincipalRedemption", "P3ML1");
		map.put("maturityDate", "2015-06-01T00:00:00");
		map.put("notionalPrincipal", "2000.0");
		map.put("nominalInterestRate", "0.08");
		map.put("endOfMonthConvention", EndOfMonthConventionEnum.SD.toString());
		map.put("contractID", "100021");
		map.put("arrayCycleAnchorDateOfInterestPayment", "2013-01-01T00:00:00");
		map.put("arrayCycleOfInterestPayment", "P1ML1");
		map.put("accruedInterest", "0");
		map.put("capitalizationEndDate", "NULL");
		map.put("cycleAnchorDateOfInterestCalculationBase", "NULL");
		map.put("cycleOfInterestCalculationBase", "P1ML0");
		map.put("contractDealDate", "2012-12-15T00:00:00");
		map.put("arrayIncreaseDecrease", "DEC,INC,DEC");
		map.put("scalingIndexAtStatusDate", "1");
		map.put("rateMultiplier", "1");
		map.put("arrayCycleAnchorDateOfRateReset", "2013-03-01T00:00:00,2013-10-01T00:00:00,2014-06-01T00:00:00");
		map.put("arrayCycleOfRateReset", "P2ML1,NULL,P2ML1");
		map.put("arrayRate", "0.01,0.2,-0.05");
		map.put("arrayFixedVariable", "VAR,FIX,VAR");
		map.put("marketObjectCodeOfRateReset", "USD.SWP");

		// parse attributes
		ContractModel model = ContractModel.parse(map);

		// compute schedule
		ArrayList<ContractEvent> schedule = ExoticLinearAmortizer.schedule(model.getAs("MaturityDate"),
				model);

		// add analysis events
		schedule.addAll(EventFactory.createEvents(
				ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),
						model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(0), "P1ML1", EndOfMonthConventionEnum.SD),
				EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM(), model.getAs("ContractID")));

		// define risk factor model
		LocalDateTime[] times = {LocalDateTime.parse("2012-06-01T00:00:00"),
				LocalDateTime.parse("2013-06-01T00:00:00"), LocalDateTime.parse("2013-12-01T00:00:00"),
				LocalDateTime.parse("2014-06-01T00:00:00"), LocalDateTime.parse("2014-12-01T00:00:00"),
				LocalDateTime.parse("2015-06-01T00:00:00")};
		Double[] values = {2.0, 1.2, 1.5, 0.8, 1.3, 0.5};
		ValuedMarketModel riskFactors = new ValuedMarketModel(times, values);
		// apply events
		ArrayList<ContractEvent> events = ExoticLinearAmortizer.apply(schedule, model, riskFactors);
	}

	@Test
	public void test_LAX_schedule_MandatoryAttributes_withMaturity_and_PurchaseDate() {
		thrown = ExpectedException.none();
		// define attributes
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("contractType", "LAX");
		map.put("calendar", "NoHolidayCalendar");
		map.put("statusDate", "2012-12-30T00:00:00");
		map.put("contractRole", "RPL");
		map.put("legalEntityIDCounterparty", "CORP-XY");
		map.put("dayCountConvention", "AA");
		map.put("currency", "USD");
		map.put("initialExchangeDate", "2013-01-01T00:00:00");
		 map.put("cycleOfPrincipalRedemption", "P3ML1");
		map.put("maturityDate", "2015-06-01T00:00:00");
		map.put("notionalPrincipal", "2000.0");
		map.put("nominalInterestRate", "0.08");
		map.put("endOfMonthConvention", EndOfMonthConventionEnum.SD.toString());
		map.put("purchaseDate", "2013-08-17T00:00:00");
		map.put("priceAtPurchaseDate", "2100.0");
		map.put("contractID", "100021");
		map.put("arrayCycleAnchorDateOfInterestPayment", "2013-01-01T00:00:00");
		map.put("arrayCycleOfInterestPayment", "P1ML1");
		map.put("accruedInterest", "0");
		map.put("capitalizationEndDate", "NULL");
		map.put("cycleAnchorDateOfInterestCalculationBase", "NULL");
		map.put("cycleOfInterestCalculationBase", "P1ML0");
		map.put("contractDealDate", "2012-12-15T00:00:00");
		map.put("arrayCycleAnchorDateOfPrincipalRedemption",
				"2013-02-01T00:00:00,2013-09-01T00:00:00,2014-04-01T00:00:00");
		map.put("arrayCycleOfPrincipalRedemption", "P2ML1,P1ML1,P1ML0");
		map.put("arrayNextPrincipalRedemptionPayment", "250.0,100.0,200.0");
		map.put("arrayIncreaseDecrease", "DEC,INC,DEC");
		map.put("scalingIndexAtStatusDate", "1");
		map.put("rateMultiplier", "1");
		map.put("arrayCycleAnchorDateOfRateReset", "2013-03-01T00:00:00,2013-10-01T00:00:00,2014-06-01T00:00:00");
		map.put("arrayCycleOfRateReset", "P2ML1,NULL,P2ML1");
		map.put("arrayRate", "0.01,0.2,-0.05");
		map.put("arrayFixedVariable", "VAR,FIX,VAR");
		map.put("marketObjectCodeOfRateReset", "USD.SWP");

		// parse attributes
		ContractModel model = ContractModel.parse(map);

		// compute schedule
		ArrayList<ContractEvent> schedule = ExoticLinearAmortizer.schedule(model.getAs("MaturityDate"),
				model);

		// add analysis events
		schedule.addAll(EventFactory.createEvents(
				ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),
						model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(0), "P1ML1", EndOfMonthConventionEnum.SD),
				EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM(), model.getAs("ContractID")));

		// define risk factor model
		LocalDateTime[] times = {LocalDateTime.parse("2012-06-01T00:00:00"),
				LocalDateTime.parse("2013-06-01T00:00:00"), LocalDateTime.parse("2013-12-01T00:00:00"),
				LocalDateTime.parse("2014-06-01T00:00:00"), LocalDateTime.parse("2014-12-01T00:00:00"),
				LocalDateTime.parse("2015-06-01T00:00:00")};
		Double[] values = {2.0, 1.2, 1.5, 0.8, 1.3, 0.5};
		ValuedMarketModel riskFactors = new ValuedMarketModel(times, values);
		// apply events
		ArrayList<ContractEvent> events = ExoticLinearAmortizer.apply(schedule, model, riskFactors);
	}

	@Test
	public void test_LAX_schedule_MandatoryAttributes_withMaturity_IPCB() {
		thrown = ExpectedException.none();
		// define attributes
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("contractType", "LAX");
		map.put("calendar", "NoHolidayCalendar");
		map.put("statusDate", "2019-07-18T00:00:00");
		map.put("contractRole", "RPA");
		map.put("legalEntityIDCounterparty", "CORP-XY");
		map.put("dayCountConvention", "A365");
		map.put("currency", "INR");
		map.put("initialExchangeDate", "2019-08-11T00:00:00");
		map.put("maturityDate", "2019-12-31T00:00:00");
		map.put("notionalPrincipal", "10000.0");
		map.put("nominalInterestRate", "0.18");
		map.put("endOfMonthConvention", EndOfMonthConventionEnum.SD.toString());
		map.put("arrayCycleAnchorDateOfInterestPayment", "2019-08-18T00:00:00,2019-11-10T00:00:00");
		map.put("arrayCycleOfInterestPayment", "P1WL0,P1WL0");
		map.put("arrayCycleAnchorDateOfPrincipalRedemption", "2019-08-18T00:00:00,2019-09-15T00:00:00");
		map.put("arrayCycleOfPrincipalRedemption", "P1WL0,P1WL0");
		map.put("arrayNextPrincipalRedemptionPayment", "700,800");
		map.put("arrayIncreaseDecrease", "DEC,DEC");
		map.put("rateMultiplier", "1");
		map.put("marketObjectCodeOfRateReset", "USD.SWP");
		map.put("accruedInterest", "0");
		map.put("interestCalculationBase", "NTL");
		map.put("interestCalculationBaseAmount", "10000.0");

		// parse attributes
		ContractModel model = ContractModel.parse(map);

		// compute schedule
		ArrayList<ContractEvent> schedule = ExoticLinearAmortizer.schedule(model.getAs("MaturityDate"),
				model);

		// add analysis events
		schedule.addAll(EventFactory.createEvents(
				ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),
						model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(0), "P1ML1", EndOfMonthConventionEnum.SD),
				EventType.AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM(), model.getAs("ContractID")));

		// define risk factor model
		LocalDateTime[] times = {
				LocalDateTime.parse("2019-07-18T00:00:00"),
				LocalDateTime.parse("2019-08-18T00:00:00"),
				LocalDateTime.parse("2019-08-25T00:00:00"), LocalDateTime.parse("2019-09-01T00:00:00"),
				LocalDateTime.parse("2019-09-08T00:00:00"), LocalDateTime.parse("2019-09-15T00:00:00"),
				LocalDateTime.parse("2019-09-22T00:00:00")};
		Double[] values = {2.0, 1.2, 1.5, 0.8, 1.3, 0.5};
		ValuedMarketModel riskFactors = new ValuedMarketModel(times, values);
		// apply events
		ArrayList<ContractEvent> events = ExoticLinearAmortizer.apply(schedule, model, riskFactors);
	}
}

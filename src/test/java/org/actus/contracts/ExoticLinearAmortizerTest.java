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
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.ObjectOutputStream.PutField;
import java.time.LocalDateTime;

import org.actus.time.ScheduleFactory;
import org.actus.util.StringUtils;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class ExoticLinearAmortizerTest {
    
	class MarketModel implements RiskFactorModelProvider {
    	public Set<String> keys() {
            Set<String> keys = new HashSet<String>();
            return keys;
        }
        
        public double stateAt(String id,LocalDateTime time,StateSpace contractStates,ContractModelProvider contractAttributes) {
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
        
        public double stateAt(String id,LocalDateTime time,StateSpace contractStates,ContractModelProvider contractAttributes) {
            SortedMap<LocalDateTime,Double> head = series.subMap(series.firstKey(), time);
        	return head.get(head.lastKey());    
        }
    }
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void test_LAX_schedule_MandatoryAttributes_withMaturity() {
        thrown = ExpectedException.none();
        // define attributes
        Map<String, String> map = new HashMap<String, String>();
        map.put("ContractType", "LAX");
        map.put("Calendar", "NoHolidayCalendar");
        map.put("StatusDate", "2012-12-30T00:00:00");
        map.put("ContractRole", "RPL");
        map.put("LegalEntityIDCounterparty", "CORP-XY");
        map.put("DayCountConvention", "A/AISDA");
        map.put("Currency", "USD");
        map.put("InitialExchangeDate", "2013-01-01T00:00:00");
        map.put("CycleOfPrincipalRedemption", "1Q-");
        map.put("MaturityDate", "2015-06-01T00:00:00");
        map.put("NotionalPrincipal", "2000.0");
        map.put("NominalInterestRate","0.08");
        map.put("EndOfMonthConvention", "SD");
        map.put("ContractID", "100021");
        map.put("ArrayCycleAnchorDateOfInterestPayment", "2013-01-01T00:00:00");
        map.put("ArrayCycleOfInterestPayment","1M-");
        map.put("AccruedInterest", "0");
        map.put("CapitalizationEndDate", "NULL");
        map.put("CycleAnchorDateOfInterestCalculationBase", "NULL");
        map.put("CycleOfInterestCalculationBase", "1M+");
        map.put("ContractDealDate", "2012-12-15T00:00:00");
        map.put("ArrayCycleAnchorDateOfPrincipalRedemption", "2013-02-01T00:00:00,2013-09-01T00:00:00,2014-04-01T00:00:00");
        map.put("ArrayCycleOfPrincipalRedemption", "2M-,1M-,1M+");
        map.put("ArrayNextPrincipalRedemptionPayment", "250.0,100.0,200.0");
        map.put("ArrayIncreaseDecrease","DEC,INC,DEC");
        map.put("ScalingIndexAtStatusDate", "1");
        map.put("RateMultiplier", "1");
        map.put("ArrayCycleAnchorDateOfRateReset", "2013-03-01T00:00:00,2013-10-01T00:00:00,2014-06-01T00:00:00");
        map.put("ArrayCycleOfRateReset", "2M-,NULL,2M-");
        map.put("ArrayRate", "0.01,0.2,-0.05");
        map.put("ArrayFixedVariable", "VAR,FIX,VAR");
        map.put("MarketObjectCodeRateReset", "USD.SWP");
        
        // parse attributes
        ContractModel model = ContractModel.parse(map);

        // compute schedule
        ArrayList<ContractEvent> schedule = ExoticLinearAmortizer.schedule(LocalDateTime.parse(map.get("MaturityDate")),model); 

        // add analysis events
        schedule.addAll(EventFactory.createEvents(
            ScheduleFactory.createSchedule(model.getAs("InitialExchangeDate"),model.<LocalDateTime>getAs("InitialExchangeDate").plusMonths(6),"1M-","SD"),
            StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM()));
    
        // define risk factor model
        LocalDateTime[] times = {LocalDateTime.parse("2012-06-01T00:00:00"),LocalDateTime.parse("2013-06-01T00:00:00"),LocalDateTime.parse("2013-12-01T00:00:00"),LocalDateTime.parse("2014-06-01T00:00:00"),LocalDateTime.parse("2014-12-01T00:00:00"),LocalDateTime.parse("2015-06-01T00:00:00")};
        Double[] values = {2.0,1.2,1.5,0.8,1.3, 0.5};
        ValuedMarketModel riskFactors = new ValuedMarketModel(times,values);

        // apply events
        ArrayList<ContractEvent> events = ExoticLinearAmortizer.apply(schedule,model,riskFactors);
        
    }
}

/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.attributes;

import org.actus.AttributeConversionException;

import java.util.Map;
import java.util.HashMap;

import org.actus.types.EndOfMonthConventionEnum;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class ContractModelTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test_AttributeConversionException() {
        thrown.expect(AttributeConversionException.class);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ContractType", "PAM");
        ContractModel model = ContractModel.parse(map);
    }

    @Test
    public void test_AttributeParser_PAM_MandatoryAttributes() {
        thrown = ExpectedException.none();
        Map<String, Object> map = new HashMap<String, Object>();
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
        ContractModel model = ContractModel.parse(map);
    }

    @Test
    public void test_AttributeParser_PAM_AllAttributes() {
        thrown = ExpectedException.none();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ContractType", "PAM");
        map.put("Calendar", "org.actus.time.calendar.NoHolidaysCalendar");
        map.put("BusinessDayConvention", "SCF");
        map.put("EndOfMonthConvention", EndOfMonthConventionEnum.SD.toString());
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
        ContractModel model = ContractModel.parse(map);
    }

}

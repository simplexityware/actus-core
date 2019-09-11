package org.actus.conventions.daycount;

import static org.junit.Assert.assertEquals;

/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.actus.AttributeConversionException;
import org.actus.attributes.ContractModel;
import org.actus.attributes.ContractModelProvider;
import org.actus.contracts.LinearAmortizer;
import org.actus.conventions.contractrole.ContractRoleConvention;
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
/*
 * ThirtyEThreeSixty (30E/336)
 * 
 */
public class ThirtyEThreeSixtyTest {
	
	ThirtyEThreeSixty convention = new ThirtyEThreeSixty();
	
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    LocalDateTime localDate1 = LocalDateTime.parse("2019-02-01T00:00:00");
    LocalDateTime localDate2 = LocalDateTime.parse("2019-04-30T00:00:00");
    LocalDateTime localDate3 = LocalDateTime.parse("2019-05-28T00:00:00");
    
    @Test
    public void test_daycount_ThirtyEThreeSixty_1() {
        thrown = ExpectedException.none();
        double result = 89.0;
        assertEquals(result, convention.dayCount(localDate1,localDate2),0);
    }

    @Test
    public void test_daycount_ThirtyEThreeSixty_2() {
        thrown = ExpectedException.none();
        double result = 117;
        assertEquals(result, convention.dayCount(localDate1,localDate3),0);
    }
    
    @Test
    public void test_dayCountFraction_ThirtyEThreeSixty_1() {
        thrown = ExpectedException.none();
        double result = 0.24722222222222223; // 89 divided by 360
        assertEquals(result, convention.dayCountFraction(localDate1,localDate2),0);
    }

    @Test
    public void test_dayCountFraction_ThirtyEThreeSixty_2() {
        thrown = ExpectedException.none();
        double result = 0.325; // 117 divided by 360
        assertEquals(result, convention.dayCountFraction(localDate1,localDate3),0);
    }

}


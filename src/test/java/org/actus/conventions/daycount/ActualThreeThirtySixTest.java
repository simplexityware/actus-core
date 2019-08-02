/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.conventions.daycount;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
/*
 * ActualThreeThirtySix(30E/360)
 * 
 */
public class ActualThreeThirtySixTest {

	ActualThreeThirtySix convention = new ActualThreeThirtySix();
	
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    LocalDateTime localDate1 = LocalDateTime.parse("2019-02-01T00:00:00");
    LocalDateTime localDate2 = LocalDateTime.parse("2019-04-30T00:00:00");
    
    @Test
    public void test_daycount_ActualThreeThirtySix_1() {
        thrown = ExpectedException.none();
        double result = 88.0;
        assertEquals(result, convention.dayCount(localDate1,localDate2),0);
    }

    @Test
    public void test_daycount_ActualThreeThirtySix_2() {
        thrown = ExpectedException.none();
        double result = 88.0;
        assertEquals(result, convention.dayCount(localDate1,localDate2),0);
    }
    
    @Test
    public void test_dayCountFraction_ActualThreeThirtySix_1() {
        thrown = ExpectedException.none();
        double result =0.2619047619047619; // 88 divided by 336
        assertEquals(result, convention.dayCountFraction(localDate1,localDate2),0.0);
    }

    @Test
    public void test_dayCountFraction_ActualThreeThirtySix_2() {
        thrown = ExpectedException.none();
        double result =0.2619047619047619;
        assertEquals(result, convention.dayCountFraction(localDate1,localDate2),0.0);
    }
}

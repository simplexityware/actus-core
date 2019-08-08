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
 * ActualActualISDATest(A/AISDA)
 * 
 */
public class ActualActualISDATest {

	ActualActualISDA convention = new ActualActualISDA();
	
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    LocalDateTime localDate1 = LocalDateTime.parse("2019-02-01T00:00:00");
    LocalDateTime localDate2 = LocalDateTime.parse("2019-03-30T00:00:00");
    LocalDateTime localDate3 = LocalDateTime.parse("2019-07-30T00:00:00");
    
    @Test
    public void test_daycount_ActualActualISDA_1() {
        thrown = ExpectedException.none();
        double result = 57.0;
        assertEquals(result, convention.dayCount(localDate1,localDate2),0);
    }

    @Test
    public void test_dayCountFraction_ActualActualISDA_1() {
        thrown = ExpectedException.none();
        double result = 0.15616438356164383; // 57 divided by 365 (not leap year basis)
        assertEquals(result, convention.dayCountFraction(localDate1,localDate2),0);
    }
    
    @Test
    public void test_daycount_ActualActualISDA_2() {
        thrown = ExpectedException.none();
        double result = 179.0;
        assertEquals(result, convention.dayCount(localDate1,localDate3),0);
    }  

    @Test
    public void test_dayCountFraction_ActualActualISDA_2() {
        thrown = ExpectedException.none();
        double result = 0.4904109589041096; // 179 divided by 365 (not leap year basis)
        assertEquals(result, convention.dayCountFraction(localDate1,localDate3),0);
    }

}

package org.actus.conventions.daycount;

import static org.junit.Assert.assertEquals;

/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */

import java.time.LocalDateTime;
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
    
    // define test inputs
    LocalDateTime start1 = LocalDateTime.parse("2006-01-31T00:00:00");
    LocalDateTime start2 = LocalDateTime.parse("2006-01-30T00:00:00");
    LocalDateTime start3 = LocalDateTime.parse("2006-02-28T00:00:00");
    LocalDateTime start4 = LocalDateTime.parse("2006-02-14T00:00:00");
    LocalDateTime start5 = LocalDateTime.parse("2006-09-30T00:00:00");
    LocalDateTime start6 = LocalDateTime.parse("2006-10-31T00:00:00");
    LocalDateTime start7 = LocalDateTime.parse("2007-08-31T00:00:00");
    LocalDateTime start8 = LocalDateTime.parse("2008-02-28T00:00:00");
    LocalDateTime start9 = LocalDateTime.parse("2008-02-28T00:00:00");
    LocalDateTime start10 = LocalDateTime.parse("2008-02-28T00:00:00");
    LocalDateTime start11 = LocalDateTime.parse("2007-02-26T00:00:00");
    LocalDateTime start12 = LocalDateTime.parse("2007-02-26T00:00:00");
    LocalDateTime start13 = LocalDateTime.parse("2008-02-29T00:00:00");
    LocalDateTime start14 = LocalDateTime.parse("2008-02-28T00:00:00");
    LocalDateTime start15 = LocalDateTime.parse("2008-02-28T00:00:00");
    
    LocalDateTime end1 = LocalDateTime.parse("2006-02-28T00:00:00");
    LocalDateTime end2 = LocalDateTime.parse("2006-02-28T00:00:00");
    LocalDateTime end3 = LocalDateTime.parse("2006-03-03T00:00:00");
    LocalDateTime end4 = LocalDateTime.parse("2006-02-28T00:00:00");
    LocalDateTime end5 = LocalDateTime.parse("2006-10-31T00:00:00");
    LocalDateTime end6 = LocalDateTime.parse("2006-11-28T00:00:00");
    LocalDateTime end7 = LocalDateTime.parse("2008-02-28T00:00:00");
    LocalDateTime end8 = LocalDateTime.parse("2008-08-28T00:00:00");
    LocalDateTime end9 = LocalDateTime.parse("2008-08-30T00:00:00");
    LocalDateTime end10 = LocalDateTime.parse("2008-08-31T00:00:00");
    LocalDateTime end11 = LocalDateTime.parse("2008-02-28T00:00:00");
    LocalDateTime end12 = LocalDateTime.parse("2008-02-29T00:00:00");
    LocalDateTime end13 = LocalDateTime.parse("2009-02-28T00:00:00");
    LocalDateTime end14 = LocalDateTime.parse("2008-03-30T00:00:00");
    LocalDateTime end15 = LocalDateTime.parse("2008-03-31T00:00:00");

    // define expected test results
    double days1 = 28.0;
    double days2 = 28.0;
    double days3 = 5.0;
    double days4 = 14.0;
    double days5 = 30.0;
    double days6 = 28.0;
    double days7 = 178.0;
    double days8 = 180.0;
    double days9 = 182.0;
    double days10 = 182.0;
    double days11 = 362.0;
    double days12 = 363.0;
    double days13 = 359.0;
    double days14 = 32.0;
    double days15 = 32.0;

    @Test
    public void test_daycount_ThirtyEThreeSixty_1() {
        thrown = ExpectedException.none();
        assertEquals(days1, convention.dayCount(start1,end1),0);
    }

    @Test
    public void test_daycount_ThirtyEThreeSixty_2() {
        thrown = ExpectedException.none();
        assertEquals(days2, convention.dayCount(start2,end2),0);
    }

    @Test
    public void test_daycount_ThirtyEThreeSixty_3() {
        thrown = ExpectedException.none();
        assertEquals(days3, convention.dayCount(start3,end3),0);
    }

    @Test
    public void test_daycount_ThirtyEThreeSixty_4() {
        thrown = ExpectedException.none();
        assertEquals(days4, convention.dayCount(start4,end4),0);
    }

    @Test
    public void test_daycount_ThirtyEThreeSixty_5() {
        thrown = ExpectedException.none();
        assertEquals(days5, convention.dayCount(start5,end5),0);
    }

    @Test
    public void test_daycount_ThirtyEThreeSixty_6() {
        thrown = ExpectedException.none();
        assertEquals(days6, convention.dayCount(start6,end6),0);
    }

    @Test
    public void test_daycount_ThirtyEThreeSixty_7() {
        thrown = ExpectedException.none();
        assertEquals(days7, convention.dayCount(start7,end7),0);
    }

    @Test
    public void test_daycount_ThirtyEThreeSixty_8() {
        thrown = ExpectedException.none();
        assertEquals(days8, convention.dayCount(start8,end8),0);
    }

    @Test
    public void test_daycount_ThirtyEThreeSixty_9() {
        thrown = ExpectedException.none();
        assertEquals(days9, convention.dayCount(start9,end9),0);
    }

    @Test
    public void test_daycount_ThirtyEThreeSixty_10() {
        thrown = ExpectedException.none();
        assertEquals(days10, convention.dayCount(start10,end10),0);
    }
    
    @Test
    public void test_daycount_ThirtyEThreeSixty_11() {
        thrown = ExpectedException.none();
        assertEquals(days11, convention.dayCount(start11,end11),0);
    }

    @Test
    public void test_daycount_ThirtyEThreeSixty_12() {
        thrown = ExpectedException.none();
        assertEquals(days12, convention.dayCount(start12,end12),0);
    }

    @Test
    public void test_daycount_ThirtyEThreeSixty_13() {
        thrown = ExpectedException.none();
        assertEquals(days13, convention.dayCount(start13,end13),0);
    }

    @Test
    public void test_daycount_ThirtyEThreeSixty_14() {
        thrown = ExpectedException.none();
        assertEquals(days14, convention.dayCount(start14,end14),0);
    }

    @Test
    public void test_daycount_ThirtyEThreeSixty_15() {
        thrown = ExpectedException.none();
        assertEquals(days15, convention.dayCount(start15,end15),0);
    }

    @Test
    public void test_fraction_ThirtyEThreeSixty_1() {
        thrown = ExpectedException.none();
        assertEquals(days1/360, convention.dayCountFraction(start1,end1),0);
    }

    @Test
    public void test_fraction_ThirtyEThreeSixty_2() {
        thrown = ExpectedException.none();
        assertEquals(days2/360, convention.dayCountFraction(start2,end2),0);
    }

    @Test
    public void test_fraction_ThirtyEThreeSixty_3() {
        thrown = ExpectedException.none();
        assertEquals(days3/360, convention.dayCountFraction(start3,end3),0);
    }

    @Test
    public void test_fraction_ThirtyEThreeSixty_4() {
        thrown = ExpectedException.none();
        assertEquals(days4/360, convention.dayCountFraction(start4,end4),0);
    }

    @Test
    public void test_fraction_ThirtyEThreeSixty_5() {
        thrown = ExpectedException.none();
        assertEquals(days5/360, convention.dayCountFraction(start5,end5),0);
    }

    @Test
    public void test_fraction_ThirtyEThreeSixty_6() {
        thrown = ExpectedException.none();
        assertEquals(days6/360, convention.dayCountFraction(start6,end6),0);
    }

    @Test
    public void test_fraction_ThirtyEThreeSixty_7() {
        thrown = ExpectedException.none();
        assertEquals(days7/360, convention.dayCountFraction(start7,end7),0);
    }

    @Test
    public void test_fraction_ThirtyEThreeSixty_8() {
        thrown = ExpectedException.none();
        assertEquals(days8/360, convention.dayCountFraction(start8,end8),0);
    }

    @Test
    public void test_fraction_ThirtyEThreeSixty_9() {
        thrown = ExpectedException.none();
        assertEquals(days9/360, convention.dayCountFraction(start9,end9),0);
    }

    @Test
    public void test_fraction_ThirtyEThreeSixty_10() {
        thrown = ExpectedException.none();
        assertEquals(days10/360, convention.dayCountFraction(start10,end10),0);
    }
    
    @Test
    public void test_fraction_ThirtyEThreeSixty_11() {
        thrown = ExpectedException.none();
        assertEquals(days11/360, convention.dayCountFraction(start11,end11),0);
    }

    @Test
    public void test_fraction_ThirtyEThreeSixty_12() {
        thrown = ExpectedException.none();
        assertEquals(days12/360, convention.dayCountFraction(start12,end12),0);
    }

    @Test
    public void test_fraction_ThirtyEThreeSixty_13() {
        thrown = ExpectedException.none();
        assertEquals(days13/360, convention.dayCountFraction(start13,end13),0);
    }

    @Test
    public void test_fraction_ThirtyEThreeSixty_14() {
        thrown = ExpectedException.none();
        assertEquals(days14/360, convention.dayCountFraction(start14,end14),0);
    }

    @Test
    public void test_fraction_ThirtyEThreeSixty_15() {
        thrown = ExpectedException.none();
        assertEquals(days15/360, convention.dayCountFraction(start15,end15),0);
    }

}


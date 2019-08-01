/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.util;

import org.actus.AttributeConversionException;

import java.time.DayOfWeek;
import java.time.Period;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.assertEquals;

public class CycleUtilsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test_AttributeConversionException_thrown() {
        thrown.expect(AttributeConversionException.class);
        Period period = CycleUtils.parsePeriod("XYZ");
    }

    @Test
    public void test_AttributeConversionException_notthrown() {
        thrown = ExpectedException.none();
        Period period = CycleUtils.parsePeriod("1M-");
    }

    @Test
    public void test_isPeriod_true() {
        assertEquals(true, CycleUtils.isPeriod("1M-"));
    }

    @Test
    public void test_isPeriod_false() {
        assertEquals(false, CycleUtils.isPeriod("1Fri-"));
    }

    @Test
    public void test_parsePeriod_1M() {
        assertEquals(Period.ofMonths(1), CycleUtils.parsePeriod("1M-"));
    }

    @Test
    public void test_parsePeriod_2W() {
        assertEquals(Period.ofWeeks(2), CycleUtils.parsePeriod("2W-"));
    }

    @Test
    public void test_parsePeriod_10Y() {
        assertEquals(Period.ofMonths(120), CycleUtils.parsePeriod("10Y-"));
    }

    @Test
    public void test_parsePeriod_1X_exception() {
        thrown.expect(AttributeConversionException.class);
        Period period = CycleUtils.parsePeriod("1X-");
    }

    @Test
    public void test_parsePeriod_0p1M_exception() {
        thrown.expect(AttributeConversionException.class);
        Period period = CycleUtils.parsePeriod("0.5M-");
    }

    @Test
    public void test_parsePosition_1() {
        assertEquals(1, CycleUtils.parsePosition("1Fri-"));
    }

    @Test
    public void test_parsePosition_4() {
        assertEquals(4, CycleUtils.parsePosition("4Mon-"));
    }

    @Test
    public void test_parsePosition_0p1_exception() {
        thrown.expect(AttributeConversionException.class);
        int position = CycleUtils.parsePosition("Fri-");
    }

    @Test
    public void test_parseWeekday_Mon() {
        assertEquals(DayOfWeek.MONDAY, CycleUtils.parseWeekday("1Mon-"));
    }

    @Test
    public void test_parseWeekday_Tue() {
        assertEquals(DayOfWeek.TUESDAY, CycleUtils.parseWeekday("1Tue-"));
    }

    @Test
    public void test_parseWeekday_Wed() {
        assertEquals(DayOfWeek.WEDNESDAY, CycleUtils.parseWeekday("1Wed-"));
    }

    @Test
    public void test_parseWeekday_Thu() {
        assertEquals(DayOfWeek.THURSDAY, CycleUtils.parseWeekday("1Thu-"));
    }

    @Test
    public void test_parseWeekday_Fri() {
        assertEquals(DayOfWeek.FRIDAY, CycleUtils.parseWeekday("1Fri-"));
    }

    @Test
    public void test_parseWeekday_Sat() {
        assertEquals(DayOfWeek.SATURDAY, CycleUtils.parseWeekday("1Sat-"));
    }

    @Test
    public void test_parseWeekday_Sun() {
        assertEquals(DayOfWeek.SUNDAY, CycleUtils.parseWeekday("1Sun-"));
    }

    @Test
    public void test_parseStub_short_period() {
        assertEquals('-', CycleUtils.parseStub("1M-"));
    }

    @Test
    public void test_parseStub_short_weekday() {
        assertEquals('-', CycleUtils.parseStub("1Sun-"));
    }

    @Test
    public void test_parseStub_long_period() {
        assertEquals('+', CycleUtils.parseStub("10M+"));
    }

    @Test
    public void test_parseStub_long_weekday() {
        assertEquals('+', CycleUtils.parseStub("1Sun+"));
    }

    @Test
    public void test_parseStub_exception() {
        thrown.expect(AttributeConversionException.class);
        char stub = CycleUtils.parseStub("1Mx");
    }

}

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
        Period period = CycleUtils.parsePeriod("P1ML1");
    }

    @Test
    public void test_isPeriod_true() {
        assertEquals(true, CycleUtils.isPeriod("P1ML1"));
    }

    @Test
    public void test_isPeriod_false() {
        assertEquals(false, CycleUtils.isPeriod("1FriL1"));
    }

    @Test
    public void test_parsePeriod_1M() {
        assertEquals(Period.ofMonths(1), CycleUtils.parsePeriod("P1ML1"));
    }

    @Test
    public void test_parsePeriod_2W() {
        assertEquals(Period.ofWeeks(2), CycleUtils.parsePeriod("P2WL1"));
    }

    @Test
    public void test_parsePeriod_10Y() {
        assertEquals(Period.ofYears(10), CycleUtils.parsePeriod("P10YL1"));
    }

    @Test
    public void test_parsePeriod_1X_exception() {
        thrown.expect(AttributeConversionException.class);
        Period period = CycleUtils.parsePeriod("1XL1");
    }

    @Test
    public void test_parsePeriod_0p1M_exception() {
        thrown.expect(AttributeConversionException.class);
        Period period = CycleUtils.parsePeriod("0.5ML1");
    }

    @Test
    public void test_parsePosition_1() {
        assertEquals(1, CycleUtils.parsePosition("1FriL1"));
    }

    @Test
    public void test_parsePosition_4() {
        assertEquals(4, CycleUtils.parsePosition("4MonL1"));
    }

    @Test
    public void test_parsePosition_0p1_exception() {
        thrown.expect(AttributeConversionException.class);
        int position = CycleUtils.parsePosition("FriL1");
    }

    @Test
    public void test_parseWeekday_Mon() {
        assertEquals(DayOfWeek.MONDAY, CycleUtils.parseWeekday("1MonL1"));
    }

    @Test
    public void test_parseWeekday_Tue() {
        assertEquals(DayOfWeek.TUESDAY, CycleUtils.parseWeekday("1TueL1"));
    }

    @Test
    public void test_parseWeekday_Wed() {
        assertEquals(DayOfWeek.WEDNESDAY, CycleUtils.parseWeekday("1WedL1"));
    }

    @Test
    public void test_parseWeekday_Thu() {
        assertEquals(DayOfWeek.THURSDAY, CycleUtils.parseWeekday("1ThuL1"));
    }

    @Test
    public void test_parseWeekday_Fri() {
        assertEquals(DayOfWeek.FRIDAY, CycleUtils.parseWeekday("1FriL1"));
    }

    @Test
    public void test_parseWeekday_Sat() {
        assertEquals(DayOfWeek.SATURDAY, CycleUtils.parseWeekday("1SatL1"));
    }

    @Test
    public void test_parseWeekday_Sun() {
        assertEquals(DayOfWeek.SUNDAY, CycleUtils.parseWeekday("1SunL1"));
    }

    @Test
    public void test_parseStub_short_period() {
        assertEquals(StringUtils.ShortStub, CycleUtils.parseStub("P1ML1"));
    }

    @Test
    public void test_parseStub_short_weekday() {
        assertEquals(StringUtils.ShortStub, CycleUtils.parseStub("1SunL1"));
    }

    @Test
    public void test_parseStub_long_period() {
        assertEquals(StringUtils.LongStub, CycleUtils.parseStub("P10ML0"));
    }

    @Test
    public void test_parseStub_long_weekday() {
        assertEquals(StringUtils.LongStub, CycleUtils.parseStub("1SunL0"));
    }

    @Test
    public void test_parseStub_exception() {
        thrown.expect(AttributeConversionException.class);
        char stub = CycleUtils.parseStub("P1MLx");
    }

}

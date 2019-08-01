/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.time;

import org.actus.AttributeConversionException;

import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.assertEquals;

public class WeekdayCycleAdjusterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test_AttributeConversionException_cycle_1Fri() {
        thrown.expect(AttributeConversionException.class);
        WeekdayCycleAdjuster adjuster = new WeekdayCycleAdjuster("1Fri");
    }
    
    @Test
    public void test_AttributeConversionException_cycle_1FrxShort() {
        thrown.expect(AttributeConversionException.class);
        WeekdayCycleAdjuster adjuster = new WeekdayCycleAdjuster("1Frx-");
    }

    @Test
    public void test_plus_1MonShort() {
        thrown = ExpectedException.none();
        
        // instantiate adjuster
        WeekdayCycleAdjuster adjuster = new WeekdayCycleAdjuster("1Mon-");

        // original and expected shifted time
        LocalDateTime t0 = LocalDateTime.parse("2019-01-01T00:00:00");
        LocalDateTime t1 = LocalDateTime.parse("2019-02-04T00:00:00");
        
        // finally compare expected and generated times
        assertEquals(t1, adjuster.plusCycle(t0));
    }

    @Test
    public void test_minus_1MonShort() {
        thrown = ExpectedException.none();
        
        // instantiate adjuster
        WeekdayCycleAdjuster adjuster = new WeekdayCycleAdjuster("1Mon-");

        // original and expected shifted time
        LocalDateTime t0 = LocalDateTime.parse("2019-01-01T00:00:00");
        LocalDateTime t1 = LocalDateTime.parse("2018-12-03T00:00:00");
        
        // finally compare expected and generated times
        assertEquals(t1, adjuster.minusCycle(t0));
    }

    @Test
    public void test_plus_1FriShort() {
        thrown = ExpectedException.none();
        
        // instantiate adjuster
        WeekdayCycleAdjuster adjuster = new WeekdayCycleAdjuster("1Fri-");

        // original and expected shifted time
        LocalDateTime t0 = LocalDateTime.parse("2019-07-01T00:00:00");
        LocalDateTime t1 = LocalDateTime.parse("2019-08-02T00:00:00");
        
        // finally compare expected and generated times
        assertEquals(t1, adjuster.plusCycle(t0));
    }

    @Test
    public void test_minus_1FriShort() {
        thrown = ExpectedException.none();
        
        // instantiate adjuster
        WeekdayCycleAdjuster adjuster = new WeekdayCycleAdjuster("1Fri-");

        // original and expected shifted time
        LocalDateTime t0 = LocalDateTime.parse("2019-07-01T00:00:00");
        LocalDateTime t1 = LocalDateTime.parse("2019-06-07T00:00:00");
        
        // finally compare expected and generated times
        assertEquals(t1, adjuster.minusCycle(t0));
    }

    @Test
    public void test_plus_3SatShort() {
        thrown = ExpectedException.none();
        
        // instantiate adjuster
        WeekdayCycleAdjuster adjuster = new WeekdayCycleAdjuster("3Sat-");

        // original and expected shifted time
        LocalDateTime t0 = LocalDateTime.parse("2019-10-01T00:00:00");
        LocalDateTime t1 = LocalDateTime.parse("2019-11-16T00:00:00");
        
        // finally compare expected and generated times
        assertEquals(t1, adjuster.plusCycle(t0));
    }

    @Test
    public void test_minus_3SatShort() {
        thrown = ExpectedException.none();
        
        // instantiate adjuster
        WeekdayCycleAdjuster adjuster = new WeekdayCycleAdjuster("3Sat-");

        // original and expected shifted time
        LocalDateTime t0 = LocalDateTime.parse("2019-10-01T00:00:00");
        LocalDateTime t1 = LocalDateTime.parse("2019-09-21T00:00:00");
        
        // finally compare expected and generated times
        assertEquals(t1, adjuster.minusCycle(t0));
    }

}

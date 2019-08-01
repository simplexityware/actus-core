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

public class PeriodCycleAdjusterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test_AttributeConversionException_cycle_1M() {
        thrown.expect(AttributeConversionException.class);
        PeriodCycleAdjuster adjuster = new PeriodCycleAdjuster("1M");
    }
    
    @Test
    public void test_AttributeConversionException_cycle_1Xshort() {
        thrown.expect(AttributeConversionException.class);
        PeriodCycleAdjuster adjuster = new PeriodCycleAdjuster("1X-");
    }

    @Test
    public void test_plus_1Ms() {
        thrown = ExpectedException.none();
        
        // instantiate adjuster
        PeriodCycleAdjuster adjuster = new PeriodCycleAdjuster("1M-");

        // original and expected shifted time
        LocalDateTime t0 = LocalDateTime.parse("2016-01-01T00:00:00");
        LocalDateTime t1 = LocalDateTime.parse("2016-02-01T00:00:00");
        
        // finally compare expected and generated times
        assertEquals(t1, adjuster.plusCycle(t0));
    }

    @Test
    public void test_minus_1Ms() {
        thrown = ExpectedException.none();
        
        // instantiate adjuster
        PeriodCycleAdjuster adjuster = new PeriodCycleAdjuster("1M-");

        // original and expected shifted time
        LocalDateTime t0 = LocalDateTime.parse("2016-01-01T00:00:00");
        LocalDateTime t1 = LocalDateTime.parse("2015-12-01T00:00:00");
        
        // finally compare expected and generated times
        assertEquals(t1, adjuster.minusCycle(t0));
    }

    @Test
    public void test_plus_1Ws() {
        thrown = ExpectedException.none();
        
        // instantiate adjuster
        PeriodCycleAdjuster adjuster = new PeriodCycleAdjuster("1W-");

        // original and expected shifted time
        LocalDateTime t0 = LocalDateTime.parse("2016-01-01T00:00:00");
        LocalDateTime t1 = LocalDateTime.parse("2016-01-08T00:00:00");
        
        // finally compare expected and generated times
        assertEquals(t1, adjuster.plusCycle(t0));
    }

    @Test
    public void test_minus_1Ws() {
        thrown = ExpectedException.none();
        
        // instantiate adjuster
        PeriodCycleAdjuster adjuster = new PeriodCycleAdjuster("1W-");

        // original and expected shifted time
        LocalDateTime t0 = LocalDateTime.parse("2016-01-01T00:00:00");
        LocalDateTime t1 = LocalDateTime.parse("2015-12-25T00:00:00");
        
        // finally compare expected and generated times
        assertEquals(t1, adjuster.minusCycle(t0));
    }

    @Test
    public void test_plus_1Ys() {
        thrown = ExpectedException.none();
        
        // instantiate adjuster
        PeriodCycleAdjuster adjuster = new PeriodCycleAdjuster("1Y-");

        // original and expected shifted time
        LocalDateTime t0 = LocalDateTime.parse("2016-01-01T00:00:00");
        LocalDateTime t1 = LocalDateTime.parse("2017-01-01T00:00:00");
        
        // finally compare expected and generated times
        assertEquals(t1, adjuster.plusCycle(t0));
    }

    @Test
    public void test_minus_1Ys() {
        thrown = ExpectedException.none();
        
        // instantiate adjuster
        PeriodCycleAdjuster adjuster = new PeriodCycleAdjuster("1Y-");

        // original and expected shifted time
        LocalDateTime t0 = LocalDateTime.parse("2016-01-01T00:00:00");
        LocalDateTime t1 = LocalDateTime.parse("2015-01-01T00:00:00");
        
        // finally compare expected and generated times
        assertEquals(t1, adjuster.minusCycle(t0));
    }

}

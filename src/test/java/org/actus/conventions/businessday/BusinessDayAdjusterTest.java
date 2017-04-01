/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.conventions.businessday;

import org.actus.AttributeConversionException;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.time.calendar.NoHolidaysCalendar;
import org.actus.time.calendar.MondayToFridayCalendar;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.assertEquals;

public class BusinessDayAdjusterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test_AttributeConversionException() {
        thrown.expect(AttributeConversionException.class);
        BusinessDayAdjuster adjuster = new BusinessDayAdjuster("INEXISTENT", null);
    }

    @Test
    public void test_SAME_NoHolidaysCalendar() {
        thrown = ExpectedException.none();
        BusinessDayAdjuster adjuster = new BusinessDayAdjuster("SAME", new NoHolidaysCalendar());
        
        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected event times shifted according to the business day convention
        // here in fact unshifted
        List<LocalDateTime> expectedEventTimes = new ArrayList<LocalDateTime>();
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected calc times shifted according to the business day convention
        // here in fact unshifted        
        List<LocalDateTime> expectedCalcTimes = new ArrayList<LocalDateTime>();
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedEventTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedEventTimes.add(adjuster.shiftEventTime(t)));
        
        // ... and shift times to calc times according to the business day convention
        List<LocalDateTime> shiftedCalcTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedCalcTimes.add(adjuster.shiftCalcTime(t)));
        
        // finally compare unshifted and shifted times
        assertEquals(expectedEventTimes, shiftedEventTimes);
        assertEquals(expectedCalcTimes, shiftedCalcTimes);
    }

    @Test
    public void test_SAME_MondayToFridayCalendar() {
        thrown = ExpectedException.none();
        BusinessDayAdjuster adjuster = new BusinessDayAdjuster("SAME", new MondayToFridayCalendar());
        
               // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected event times shifted according to the business day convention
        // here in fact unshifted
        List<LocalDateTime> expectedEventTimes = new ArrayList<LocalDateTime>();
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected calc times shifted according to the business day convention
        // here in fact unshifted        
        List<LocalDateTime> expectedCalcTimes = new ArrayList<LocalDateTime>();
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedEventTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedEventTimes.add(adjuster.shiftEventTime(t)));
        
        // ... and shift times to calc times according to the business day convention
        List<LocalDateTime> shiftedCalcTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedCalcTimes.add(adjuster.shiftCalcTime(t)));
        
        // finally compare unshifted and shifted times
        assertEquals(expectedEventTimes, shiftedEventTimes);
        assertEquals(expectedCalcTimes, shiftedCalcTimes);
    }

    @Test
    public void test_SCF_NoHolidaysCalendar() {
        thrown = ExpectedException.none();
        BusinessDayAdjuster adjuster = new BusinessDayAdjuster("SCF", new NoHolidaysCalendar());
        
        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected event times shifted according to the business day convention
        // here in fact unshifted
        List<LocalDateTime> expectedEventTimes = new ArrayList<LocalDateTime>();
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected calc times shifted according to the business day convention
        // here in fact unshifted        
        List<LocalDateTime> expectedCalcTimes = new ArrayList<LocalDateTime>();
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedEventTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedEventTimes.add(adjuster.shiftEventTime(t)));
        
        // ... and shift times to calc times according to the business day convention
        List<LocalDateTime> shiftedCalcTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedCalcTimes.add(adjuster.shiftCalcTime(t)));
        
        // finally compare unshifted and shifted times
        assertEquals(expectedEventTimes, shiftedEventTimes);
        assertEquals(expectedCalcTimes, shiftedCalcTimes);
    }

    @Test
    public void test_SCF_MondayToFridayCalendar() {
        thrown = ExpectedException.none();
        BusinessDayAdjuster adjuster = new BusinessDayAdjuster("SCF", new MondayToFridayCalendar());
        
        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected event times shifted according to the business day convention
        // here in fact unshifted
        List<LocalDateTime> expectedEventTimes = new ArrayList<LocalDateTime>();
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0)); 
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected calc times shifted according to the business day convention
        // here in fact unshifted        
        List<LocalDateTime> expectedCalcTimes = new ArrayList<LocalDateTime>();
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0)); 
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedEventTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedEventTimes.add(adjuster.shiftEventTime(t)));
        
        // ... and shift times to calc times according to the business day convention
        List<LocalDateTime> shiftedCalcTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedCalcTimes.add(adjuster.shiftCalcTime(t)));
        
        // finally compare unshifted and shifted times
        assertEquals(expectedEventTimes, shiftedEventTimes);
        assertEquals(expectedCalcTimes, shiftedCalcTimes);
    }
    
        @Test
    public void test_CSF_NoHolidaysCalendar() {
        thrown = ExpectedException.none();
        BusinessDayAdjuster adjuster = new BusinessDayAdjuster("CSF", new NoHolidaysCalendar());
        
        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected event times shifted according to the business day convention
        // here in fact unshifted
        List<LocalDateTime> expectedEventTimes = new ArrayList<LocalDateTime>();
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected calc times shifted according to the business day convention
        // here in fact unshifted        
        List<LocalDateTime> expectedCalcTimes = new ArrayList<LocalDateTime>();
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedEventTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedEventTimes.add(adjuster.shiftEventTime(t)));
        
        // ... and shift times to calc times according to the business day convention
        List<LocalDateTime> shiftedCalcTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedCalcTimes.add(adjuster.shiftCalcTime(t)));
        
        // finally compare unshifted and shifted times
        assertEquals(expectedEventTimes, shiftedEventTimes);
        assertEquals(expectedCalcTimes, shiftedCalcTimes);
    }

    @Test
    public void test_CSF_MondayToFridayCalendar() {
        thrown = ExpectedException.none();
        BusinessDayAdjuster adjuster = new BusinessDayAdjuster("CSF", new MondayToFridayCalendar());
        
        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected event times shifted according to the business day convention
        // here in fact unshifted
        List<LocalDateTime> expectedEventTimes = new ArrayList<LocalDateTime>();
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0)); 
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected calc times shifted according to the business day convention
        // here in fact unshifted        
        List<LocalDateTime> expectedCalcTimes = new ArrayList<LocalDateTime>();
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedEventTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedEventTimes.add(adjuster.shiftEventTime(t)));
        
        // ... and shift times to calc times according to the business day convention
        List<LocalDateTime> shiftedCalcTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedCalcTimes.add(adjuster.shiftCalcTime(t)));
        
        // finally compare unshifted and shifted times
        assertEquals(expectedEventTimes, shiftedEventTimes);
        assertEquals(expectedCalcTimes, shiftedCalcTimes);
    }
    
        @Test
    public void test_SCMF_NoHolidaysCalendar() {
        thrown = ExpectedException.none();
        BusinessDayAdjuster adjuster = new BusinessDayAdjuster("SCMF", new NoHolidaysCalendar());
        
        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected event times shifted according to the business day convention
        // here in fact unshifted
        List<LocalDateTime> expectedEventTimes = new ArrayList<LocalDateTime>();
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected calc times shifted according to the business day convention
        // here in fact unshifted        
        List<LocalDateTime> expectedCalcTimes = new ArrayList<LocalDateTime>();
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedEventTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedEventTimes.add(adjuster.shiftEventTime(t)));
        
        // ... and shift times to calc times according to the business day convention
        List<LocalDateTime> shiftedCalcTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedCalcTimes.add(adjuster.shiftCalcTime(t)));
        
        // finally compare unshifted and shifted times
        assertEquals(expectedEventTimes, shiftedEventTimes);
        assertEquals(expectedCalcTimes, shiftedCalcTimes);
    }

    @Test
    public void test_SCMF_MondayToFridayCalendar() {
        thrown = ExpectedException.none();
        BusinessDayAdjuster adjuster = new BusinessDayAdjuster("SCMF", new MondayToFridayCalendar());
        
        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected event times shifted according to the business day convention
        // here in fact unshifted
        List<LocalDateTime> expectedEventTimes = new ArrayList<LocalDateTime>();
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0)); 
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected calc times shifted according to the business day convention
        // here in fact unshifted        
        List<LocalDateTime> expectedCalcTimes = new ArrayList<LocalDateTime>();
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0)); 
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedEventTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedEventTimes.add(adjuster.shiftEventTime(t)));
        
        // ... and shift times to calc times according to the business day convention
        List<LocalDateTime> shiftedCalcTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedCalcTimes.add(adjuster.shiftCalcTime(t)));
        
        // finally compare unshifted and shifted times
        assertEquals(expectedEventTimes, shiftedEventTimes);
        assertEquals(expectedCalcTimes, shiftedCalcTimes);
    }
    
            @Test
    public void test_CSMF_NoHolidaysCalendar() {
        thrown = ExpectedException.none();
        BusinessDayAdjuster adjuster = new BusinessDayAdjuster("CSMF", new NoHolidaysCalendar());
        
        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected event times shifted according to the business day convention
        // here in fact unshifted
        List<LocalDateTime> expectedEventTimes = new ArrayList<LocalDateTime>();
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected calc times shifted according to the business day convention
        // here in fact unshifted        
        List<LocalDateTime> expectedCalcTimes = new ArrayList<LocalDateTime>();
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedEventTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedEventTimes.add(adjuster.shiftEventTime(t)));
        
        // ... and shift times to calc times according to the business day convention
        List<LocalDateTime> shiftedCalcTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedCalcTimes.add(adjuster.shiftCalcTime(t)));
        
        // finally compare unshifted and shifted times
        assertEquals(expectedEventTimes, shiftedEventTimes);
        assertEquals(expectedCalcTimes, shiftedCalcTimes);
    }

    @Test
    public void test_CSMF_MondayToFridayCalendar() {
        thrown = ExpectedException.none();
        BusinessDayAdjuster adjuster = new BusinessDayAdjuster("CSMF", new MondayToFridayCalendar());
        
        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected event times shifted according to the business day convention
        // here in fact unshifted
        List<LocalDateTime> expectedEventTimes = new ArrayList<LocalDateTime>();
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0)); 
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected calc times shifted according to the business day convention
        // here in fact unshifted        
        List<LocalDateTime> expectedCalcTimes = new ArrayList<LocalDateTime>();
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedEventTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedEventTimes.add(adjuster.shiftEventTime(t)));
        
        // ... and shift times to calc times according to the business day convention
        List<LocalDateTime> shiftedCalcTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedCalcTimes.add(adjuster.shiftCalcTime(t)));
        
        // finally compare unshifted and shifted times
        assertEquals(expectedEventTimes, shiftedEventTimes);
        assertEquals(expectedCalcTimes, shiftedCalcTimes);
    }
    
                @Test
    public void test_SCP_NoHolidaysCalendar() {
        thrown = ExpectedException.none();
        BusinessDayAdjuster adjuster = new BusinessDayAdjuster("SCP", new NoHolidaysCalendar());
        
        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected event times shifted according to the business day convention
        // here in fact unshifted
        List<LocalDateTime> expectedEventTimes = new ArrayList<LocalDateTime>();
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected calc times shifted according to the business day convention
        // here in fact unshifted        
        List<LocalDateTime> expectedCalcTimes = new ArrayList<LocalDateTime>();
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedEventTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedEventTimes.add(adjuster.shiftEventTime(t)));
        
        // ... and shift times to calc times according to the business day convention
        List<LocalDateTime> shiftedCalcTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedCalcTimes.add(adjuster.shiftCalcTime(t)));
        
        // finally compare unshifted and shifted times
        assertEquals(expectedEventTimes, shiftedEventTimes);
        assertEquals(expectedCalcTimes, shiftedCalcTimes);
    }

    @Test
    public void test_SCP_MondayToFridayCalendar() {
        thrown = ExpectedException.none();
        BusinessDayAdjuster adjuster = new BusinessDayAdjuster("SCP", new MondayToFridayCalendar());
        
        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected event times shifted according to the business day convention
        // here in fact unshifted
        List<LocalDateTime> expectedEventTimes = new ArrayList<LocalDateTime>();
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0)); 
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected calc times shifted according to the business day convention
        // here in fact unshifted        
        List<LocalDateTime> expectedCalcTimes = new ArrayList<LocalDateTime>();
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0)); 
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedEventTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedEventTimes.add(adjuster.shiftEventTime(t)));
        
        // ... and shift times to calc times according to the business day convention
        List<LocalDateTime> shiftedCalcTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedCalcTimes.add(adjuster.shiftCalcTime(t)));
        
        // finally compare unshifted and shifted times
        assertEquals(expectedEventTimes, shiftedEventTimes);
        assertEquals(expectedCalcTimes, shiftedCalcTimes);
    }
    
                @Test
    public void test_CSP_NoHolidaysCalendar() {
        thrown = ExpectedException.none();
        BusinessDayAdjuster adjuster = new BusinessDayAdjuster("CSP", new NoHolidaysCalendar());
        
        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected event times shifted according to the business day convention
        // here in fact unshifted
        List<LocalDateTime> expectedEventTimes = new ArrayList<LocalDateTime>();
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected calc times shifted according to the business day convention
        // here in fact unshifted        
        List<LocalDateTime> expectedCalcTimes = new ArrayList<LocalDateTime>();
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedEventTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedEventTimes.add(adjuster.shiftEventTime(t)));
        
        // ... and shift times to calc times according to the business day convention
        List<LocalDateTime> shiftedCalcTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedCalcTimes.add(adjuster.shiftCalcTime(t)));
        
        // finally compare unshifted and shifted times
        assertEquals(expectedEventTimes, shiftedEventTimes);
        assertEquals(expectedCalcTimes, shiftedCalcTimes);
    }

    @Test
    public void test_CSP_MondayToFridayCalendar() {
        thrown = ExpectedException.none();
        BusinessDayAdjuster adjuster = new BusinessDayAdjuster("CSP", new MondayToFridayCalendar());
        
        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected event times shifted according to the business day convention
        // here in fact unshifted
        List<LocalDateTime> expectedEventTimes = new ArrayList<LocalDateTime>();
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0)); 
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected calc times shifted according to the business day convention
        // here in fact unshifted        
        List<LocalDateTime> expectedCalcTimes = new ArrayList<LocalDateTime>();
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedEventTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedEventTimes.add(adjuster.shiftEventTime(t)));
        
        // ... and shift times to calc times according to the business day convention
        List<LocalDateTime> shiftedCalcTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedCalcTimes.add(adjuster.shiftCalcTime(t)));
        
        // finally compare unshifted and shifted times
        assertEquals(expectedEventTimes, shiftedEventTimes);
        assertEquals(expectedCalcTimes, shiftedCalcTimes);
    }
    
                @Test
    public void test_SCMP_NoHolidaysCalendar() {
        thrown = ExpectedException.none();
        BusinessDayAdjuster adjuster = new BusinessDayAdjuster("SCMP", new NoHolidaysCalendar());
        
        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected event times shifted according to the business day convention
        // here in fact unshifted
        List<LocalDateTime> expectedEventTimes = new ArrayList<LocalDateTime>();
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected calc times shifted according to the business day convention
        // here in fact unshifted        
        List<LocalDateTime> expectedCalcTimes = new ArrayList<LocalDateTime>();
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedEventTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedEventTimes.add(adjuster.shiftEventTime(t)));
        
        // ... and shift times to calc times according to the business day convention
        List<LocalDateTime> shiftedCalcTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedCalcTimes.add(adjuster.shiftCalcTime(t)));
        
        // finally compare unshifted and shifted times
        assertEquals(expectedEventTimes, shiftedEventTimes);
        assertEquals(expectedCalcTimes, shiftedCalcTimes);
    }

    @Test
    public void test_SCMP_MondayToFridayCalendar() {
        thrown = ExpectedException.none();
        BusinessDayAdjuster adjuster = new BusinessDayAdjuster("SCMP", new MondayToFridayCalendar());
        
        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected event times shifted according to the business day convention
        // here in fact unshifted
        List<LocalDateTime> expectedEventTimes = new ArrayList<LocalDateTime>();
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0)); 
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected calc times shifted according to the business day convention
        // here in fact unshifted        
        List<LocalDateTime> expectedCalcTimes = new ArrayList<LocalDateTime>();
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0)); 
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedEventTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedEventTimes.add(adjuster.shiftEventTime(t)));
        
        // ... and shift times to calc times according to the business day convention
        List<LocalDateTime> shiftedCalcTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedCalcTimes.add(adjuster.shiftCalcTime(t)));
        
        // finally compare unshifted and shifted times
        assertEquals(expectedEventTimes, shiftedEventTimes);
        assertEquals(expectedCalcTimes, shiftedCalcTimes);
    }
    
                @Test
    public void test_CSMP_NoHolidaysCalendar() {
        thrown = ExpectedException.none();
        BusinessDayAdjuster adjuster = new BusinessDayAdjuster("CSMP", new NoHolidaysCalendar());
        
        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected event times shifted according to the business day convention
        // here in fact unshifted
        List<LocalDateTime> expectedEventTimes = new ArrayList<LocalDateTime>();
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected calc times shifted according to the business day convention
        // here in fact unshifted        
        List<LocalDateTime> expectedCalcTimes = new ArrayList<LocalDateTime>();
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedEventTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedEventTimes.add(adjuster.shiftEventTime(t)));
        
        // ... and shift times to calc times according to the business day convention
        List<LocalDateTime> shiftedCalcTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedCalcTimes.add(adjuster.shiftCalcTime(t)));
        
        // finally compare unshifted and shifted times
        assertEquals(expectedEventTimes, shiftedEventTimes);
        assertEquals(expectedCalcTimes, shiftedCalcTimes);
    }

    @Test
    public void test_CSMP_MondayToFridayCalendar() {
        thrown = ExpectedException.none();
        BusinessDayAdjuster adjuster = new BusinessDayAdjuster("CSMP", new MondayToFridayCalendar());
        
        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected event times shifted according to the business day convention
        // here in fact unshifted
        List<LocalDateTime> expectedEventTimes = new ArrayList<LocalDateTime>();
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0)); 
        expectedEventTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // list of expected calc times shifted according to the business day convention
        // here in fact unshifted        
        List<LocalDateTime> expectedCalcTimes = new ArrayList<LocalDateTime>();
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 29, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 1, 0, 0)); 
        expectedCalcTimes.add(LocalDateTime.of(2016, 5, 2, 0, 0));
        
        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedEventTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedEventTimes.add(adjuster.shiftEventTime(t)));
        
        // ... and shift times to calc times according to the business day convention
        List<LocalDateTime> shiftedCalcTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedCalcTimes.add(adjuster.shiftCalcTime(t)));
        
        // finally compare unshifted and shifted times
        assertEquals(expectedEventTimes, shiftedEventTimes);
        assertEquals(expectedCalcTimes, shiftedCalcTimes);
    }

}

/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.time;

import org.actus.AttributeConversionException;
import org.actus.time.ScheduleFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.ArrayList;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.assertEquals;

public class ScheduleFactoryTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test_AttributeConversionException_cycle_1() {
        thrown.expect(AttributeConversionException.class);
        ScheduleFactory.createSchedule(LocalDateTime.parse("2016-01-01T00:00:00"), 
                                       LocalDateTime.parse("2017-01-01T00:00:00"), 
                                       "1M", "SD");
    }
    
    @Test
    public void test_AttributeConversionException_cycle_2() {
        thrown.expect(AttributeConversionException.class);
        ScheduleFactory.createSchedule(LocalDateTime.parse("2016-01-01T00:00:00"), 
                                       LocalDateTime.parse("2017-01-01T00:00:00"), 
                                       "1X-", "SD");
    }
    
    @Test
    public void test_AttributeConversionException_eomconv() {
        thrown.expect(AttributeConversionException.class);
        ScheduleFactory.createSchedule(LocalDateTime.parse("2016-01-01T00:00:00"), 
                                       LocalDateTime.parse("2017-01-01T00:00:00"), 
                                       "1M-", "XX");
    }

    @Test
    public void test_Schedule_Daily() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-01T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "1D-", "SD"));
        Collections.sort(generatedTimes);
        
        // list of expected event times shifted according to the business day convention
        // here in fact unshifted
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        LocalDateTime startTime = LocalDateTime.parse("2016-01-01T00:00:00");
        for(int i=0;i<367;i++) expectedTimes.add(startTime.plusDays(i));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }

}

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
    public void test_Schedule_Daily_SD_shortstub() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-01T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "1D-", "SD"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        LocalDateTime startTime = LocalDateTime.parse("2016-01-01T00:00:00");
        for(int i=0;i<367;i++) expectedTimes.add(startTime.plusDays(i));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_Daily_EOM_shortstub() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-01T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "1D-", "EOM"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        LocalDateTime startTime = LocalDateTime.parse("2016-01-01T00:00:00");
        for(int i=0;i<367;i++) expectedTimes.add(startTime.plusDays(i));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
        @Test
    public void test_Schedule_Daily_SD_longstub() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-01T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "1D+", "SD"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        LocalDateTime startTime = LocalDateTime.parse("2016-01-01T00:00:00");
        for(int i=0;i<367;i++) expectedTimes.add(startTime.plusDays(i));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
            @Test
    public void test_Schedule_Daily_SD_shortstub_endT24() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-01T00:00:00"), 
                LocalDateTime.parse("2017-01-01T23:59:59"), 
                "1D-", "SD"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        LocalDateTime startTime = LocalDateTime.parse("2016-01-01T00:00:00");
        for(int i=0;i<367;i++) expectedTimes.add(startTime.plusDays(i));
        expectedTimes.add(LocalDateTime.parse("2017-01-01T23:59:59"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
            @Test
    public void test_Schedule_Daily_SD_longstub_endT24() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-01T00:00:00"), 
                LocalDateTime.parse("2017-01-01T23:59:59"), 
                "1D+", "SD"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        LocalDateTime startTime = LocalDateTime.parse("2016-01-01T00:00:00");
        for(int i=0;i<366;i++) expectedTimes.add(startTime.plusDays(i));
        expectedTimes.add(LocalDateTime.parse("2017-01-01T23:59:59"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_BiDaily_SD_shortstub() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-01T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "2D-", "SD"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        LocalDateTime startTime = LocalDateTime.parse("2016-01-01T00:00:00");
        for(int i=0;i<367;i+=2) expectedTimes.add(startTime.plusDays(i));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }  
    
    @Test
    public void test_Schedule_31Daily_EOM_shortstub_startEndMonth() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "31D-", "EOM"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "31D-", "SD"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_Weekly_SD_shortstub() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-01T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "1W-", "SD"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-01T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "7D-", "SD"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_Weekly_SD_longstub() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-01T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "1W+", "SD"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-01T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "7D+", "SD"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
        
    @Test
    public void test_Schedule_Weekly_EOM_shortstub_startMidMonth() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-15T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "1W-", "EOM"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-15T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "7D-", "EOM"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_Weekly_EOM_shortstub_startEndMonth() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "1W-", "EOM"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "7D-", "EOM"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_4Weekly_SD_longstub_startEndMonth() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "4W-", "SD"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "28D-", "SD"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_4Weekly_EOM_shortstub_startEndMonth() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "4W-", "EOM"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "28D-", "SD"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_Monthly_SD_shortstub() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-01T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "1M-", "SD"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        LocalDateTime startTime = LocalDateTime.parse("2016-01-01T00:00:00");
        for(int i=0;i<13;i++) expectedTimes.add(startTime.plusMonths(i));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
     @Test
    public void test_Schedule_Monthly_SD_longstub_startBeginningMonth() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-01T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "1M+", "SD"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        LocalDateTime startTime = LocalDateTime.parse("2016-01-01T00:00:00");
        for(int i=0;i<13;i++) expectedTimes.add(startTime.plusMonths(i));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_Monthly_SD_shortstub_startMidMonth() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-15T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "1M-", "SD"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        LocalDateTime startTime = LocalDateTime.parse("2016-01-15T00:00:00");
        for(int i=0;i<12;i++) expectedTimes.add(startTime.plusMonths(i));
        expectedTimes.add(LocalDateTime.parse("2017-01-01T00:00:00"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
        @Test
    public void test_Schedule_Monthly_SD_longstub_startMidMonth() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-15T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "1M+", "SD"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        LocalDateTime startTime = LocalDateTime.parse("2016-01-15T00:00:00");
        for(int i=0;i<11;i++) expectedTimes.add(startTime.plusMonths(i));
        expectedTimes.add(LocalDateTime.parse("2017-01-01T00:00:00"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_BiMonthly_SD_longstub_startBeginningMonth() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-01T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "2M+", "SD"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        LocalDateTime startTime = LocalDateTime.parse("2016-01-01T00:00:00");
        for(int i=0;i<13;i+=2) expectedTimes.add(startTime.plusMonths(i));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_BiMonthly_SD_longstub_startMidMonth() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-15T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "2M+", "SD"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        LocalDateTime startTime = LocalDateTime.parse("2016-01-15T00:00:00");
        for(int i=0;i<9;i+=2) expectedTimes.add(startTime.plusMonths(i));
        expectedTimes.add(LocalDateTime.parse("2017-01-01T00:00:00"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_Monthly_EOM_shortstub_startMidMonth() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-15T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "1M-", "EOM"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        LocalDateTime startTime = LocalDateTime.parse("2016-02-15T00:00:00");
        for(int i=0;i<11;i++) expectedTimes.add(startTime.plusMonths(i));
        expectedTimes.add(LocalDateTime.parse("2017-01-01T00:00:00"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
        @Test
    public void test_Schedule_Monthly_EOM_shortstub_startEndMonthFeb() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "1M-", "EOM"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        expectedTimes.add(LocalDateTime.parse("2016-02-29T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-03-31T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-04-30T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-05-31T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-06-30T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-07-31T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-08-31T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-09-30T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-10-31T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-11-30T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-12-31T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2017-01-01T00:00:00"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_Monthly_EOM_longstub_startEndMonthFeb() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "1M+", "EOM"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        expectedTimes.add(LocalDateTime.parse("2016-02-29T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-03-31T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-04-30T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-05-31T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-06-30T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-07-31T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-08-31T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-09-30T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-10-31T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-11-30T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2017-01-01T00:00:00"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_Monthly_EOM_longstub_startEndMonthMar() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-03-31T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "1M+", "EOM"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        expectedTimes.add(LocalDateTime.parse("2016-03-31T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-04-30T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-05-31T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-06-30T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-07-31T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-08-31T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-09-30T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-10-31T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-11-30T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2017-01-01T00:00:00"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_Monthly_EOM_longstub_startEndMonthApr() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-04-30T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "1M+", "EOM"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        expectedTimes.add(LocalDateTime.parse("2016-04-30T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-05-31T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-06-30T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-07-31T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-08-31T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-09-30T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-10-31T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-11-30T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2017-01-01T00:00:00"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_BiMonthly_EOM_longstub_startEndMonthFeb() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "2M+", "EOM"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        expectedTimes.add(LocalDateTime.parse("2016-02-29T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-04-30T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-06-30T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-08-31T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2016-10-31T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2017-01-01T00:00:00"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
        
    @Test
    public void test_Schedule_BiMonthly_SD_shortstub_onlyStartAndEndTimes() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-11-01T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "2M-", "SD"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        expectedTimes.add(LocalDateTime.parse("2016-11-01T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2017-01-01T00:00:00"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }

    @Test
    public void test_Schedule_BiMonthly_EOM_shortstub_onlyStartAndEndTimes() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-11-30T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "2M-", "EOM"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        expectedTimes.add(LocalDateTime.parse("2016-11-30T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2017-01-01T00:00:00"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_BiMonthly_EOM_longstub_onlyStartAndEndTimes() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-11-30T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "2M+", "EOM"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        expectedTimes.add(LocalDateTime.parse("2016-11-30T00:00:00"));
        expectedTimes.add(LocalDateTime.parse("2017-01-01T00:00:00"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_Quarterly_SD_shortstub() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2026-01-01T00:00:00"), 
                "1Q-", "SD"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2026-01-01T00:00:00"), 
                "3M-", "SD"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_Quarterly_SD_longstub() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2026-01-01T00:00:00"), 
                "1Q+", "SD"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2026-01-01T00:00:00"), 
                "3M+", "SD"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_Quarterly_EOM_longstub() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2026-01-01T00:00:00"), 
                "1Q+", "EOM"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2026-01-01T00:00:00"), 
                "3M+", "EOM"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_BiQuarterly_EOM_longstub() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2026-01-01T00:00:00"), 
                "2Q+", "EOM"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2026-01-01T00:00:00"), 
                "6M+", "EOM"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_Halfyear_EOM_longstub() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2026-01-01T00:00:00"), 
                "1H+", "EOM"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2026-01-01T00:00:00"), 
                "2Q+", "EOM"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_Yearly_EOM_longstub() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2026-01-01T00:00:00"), 
                "1Y+", "EOM"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2026-01-01T00:00:00"), 
                "2H+", "EOM"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }
    
    @Test
    public void test_Schedule_BiYearly_EOM_longstub() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2026-01-01T00:00:00"), 
                "2Y+", "EOM"));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2026-01-01T00:00:00"), 
                "4H+", "EOM"));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }

}

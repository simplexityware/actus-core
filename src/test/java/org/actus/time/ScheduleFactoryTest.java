/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.time;

import org.actus.AttributeConversionException;
import org.actus.time.ScheduleFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.ArrayList;

import org.actus.types.EndOfMonthConventionEnum;
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
                                       "1M", EndOfMonthConventionEnum.SD);
    }
    
    @Test
    public void test_AttributeConversionException_cycle_2() {
        thrown.expect(AttributeConversionException.class);
        ScheduleFactory.createSchedule(LocalDateTime.parse("2016-01-01T00:00:00"), 
                                       LocalDateTime.parse("2017-01-01T00:00:00"), 
                                       "1X-", EndOfMonthConventionEnum.SD);
    }

    @Test
    public void test_Schedule_Daily_SD_shortstub() {
        thrown = ExpectedException.none();
        
        // list of generated times
        ArrayList<LocalDateTime> generatedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-01T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "P1DL1", EndOfMonthConventionEnum.SD));
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
                "P1DL1", EndOfMonthConventionEnum.EOM));
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
                "P1DL0", EndOfMonthConventionEnum.SD));
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
                "P1DL1", EndOfMonthConventionEnum.SD));
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
                "P1DL0", EndOfMonthConventionEnum.SD));
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
                "P2DL1", EndOfMonthConventionEnum.SD));
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
                "P31DL1", EndOfMonthConventionEnum.EOM));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "P31DL1", EndOfMonthConventionEnum.SD));
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
                "P1WL1", EndOfMonthConventionEnum.SD));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-01T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "P7DL1", EndOfMonthConventionEnum.SD));
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
                "P1WL0", EndOfMonthConventionEnum.SD));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-01T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "P7DL0", EndOfMonthConventionEnum.SD));
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
                "P1WL1", EndOfMonthConventionEnum.EOM));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-01-15T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "P7DL1", EndOfMonthConventionEnum.EOM));
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
                "P1WL1", EndOfMonthConventionEnum.EOM));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "P7DL1", EndOfMonthConventionEnum.EOM));
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
                "P4WL0", EndOfMonthConventionEnum.SD));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "P28DL0", EndOfMonthConventionEnum.SD));
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
                "P4WL1", EndOfMonthConventionEnum.EOM));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2017-01-01T00:00:00"), 
                "P28DL1", EndOfMonthConventionEnum.SD));
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
                "P1ML1", EndOfMonthConventionEnum.SD));
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
                "P1ML0", EndOfMonthConventionEnum.SD));
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
                "P1ML1", EndOfMonthConventionEnum.SD));
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
                "P1ML0", EndOfMonthConventionEnum.SD));
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
                "P2ML0", EndOfMonthConventionEnum.SD));
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
                "P2ML0", EndOfMonthConventionEnum.SD));
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
                "P1ML1", EndOfMonthConventionEnum.EOM));
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
                "P1ML1", EndOfMonthConventionEnum.EOM));
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
                "P1ML0", EndOfMonthConventionEnum.EOM));
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
                "P1ML0", EndOfMonthConventionEnum.EOM));
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
                "P1ML0", EndOfMonthConventionEnum.EOM));
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
                "P2ML0", EndOfMonthConventionEnum.EOM));
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
                "P2ML1", EndOfMonthConventionEnum.SD));
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
                "P2ML1", EndOfMonthConventionEnum.EOM));
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
                "P2ML0", EndOfMonthConventionEnum.EOM));
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
                "P3ML1", EndOfMonthConventionEnum.SD));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2026-01-01T00:00:00"), 
                "P3ML1", EndOfMonthConventionEnum.SD));
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
                "P3ML0", EndOfMonthConventionEnum.SD));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2026-01-01T00:00:00"), 
                "P3ML0", EndOfMonthConventionEnum.SD));
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
                "P3ML0", EndOfMonthConventionEnum.EOM));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2026-01-01T00:00:00"), 
                "P3ML0", EndOfMonthConventionEnum.EOM));
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
                "P6ML0", EndOfMonthConventionEnum.EOM));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2026-01-01T00:00:00"), 
                "P6ML0", EndOfMonthConventionEnum.EOM));
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
                "P6ML0", EndOfMonthConventionEnum.EOM));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2026-01-01T00:00:00"), 
                "P6ML0", EndOfMonthConventionEnum.EOM));
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
                "P1YL0", EndOfMonthConventionEnum.EOM));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2026-01-01T00:00:00"), 
                "P12ML0", EndOfMonthConventionEnum.EOM));
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
                "P2YL0", EndOfMonthConventionEnum.EOM));
        Collections.sort(generatedTimes);
        
        // list of expected times
        ArrayList<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>(
        ScheduleFactory.createSchedule(
                LocalDateTime.parse("2016-02-29T00:00:00"), 
                LocalDateTime.parse("2026-01-01T00:00:00"), 
                "P24ML0", EndOfMonthConventionEnum.EOM));
        Collections.sort(expectedTimes);
        
        // finally compare expected and generated times
        assertEquals(expectedTimes, generatedTimes);
    }

}

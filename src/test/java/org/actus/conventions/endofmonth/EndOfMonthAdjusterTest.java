/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.conventions.endofmonth;

import org.actus.AttributeConversionException;
import org.actus.conventions.endofmonth.EndOfMonthAdjuster;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.time.Period;

import org.actus.types.EndOfMonthConventionEnum;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.assertEquals;

public class EndOfMonthAdjusterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test_AttributeConversionException() {
        thrown.expect(AttributeConversionException.class);
        EndOfMonthAdjuster adjuster = new EndOfMonthAdjuster(null, LocalDateTime.of(2016, 02, 29, 0, 0), "P1ML1");
    }

    @Test
    public void test_SD_StartDateIsNotEOM_CycleM() {
        thrown = ExpectedException.none();
        EndOfMonthAdjuster adjuster = new EndOfMonthAdjuster(EndOfMonthConventionEnum.SD, LocalDateTime.of(2016, 02, 1, 0, 0), "P1ML1");

        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 30, 0, 0));

        // list of expected adjusted times
        List<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        expectedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedTimes.add(LocalDateTime.of(2016, 5, 30, 0, 0));

        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedTimes.add(adjuster.shift(t)));

        // finally compare unshifted and shifted times
        assertEquals(expectedTimes, shiftedTimes);
    }

    @Test
    public void test_SD_StartDateIsEOM_CycleD() {
        thrown = ExpectedException.none();
        EndOfMonthAdjuster adjuster = new EndOfMonthAdjuster(EndOfMonthConventionEnum.SD, LocalDateTime.of(2016, 02, 29, 0, 0), "P1DL1");

        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 30, 0, 0));

        // list of expected adjusted times
        List<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        expectedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedTimes.add(LocalDateTime.of(2016, 5, 30, 0, 0));

        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedTimes.add(adjuster.shift(t)));

        // finally compare unshifted and shifted times
        assertEquals(expectedTimes, shiftedTimes);
    }
    
        @Test
    public void test_SD_StartDateIsEOM_CycleW() {
        thrown = ExpectedException.none();
        EndOfMonthAdjuster adjuster = new EndOfMonthAdjuster(EndOfMonthConventionEnum.SD, LocalDateTime.of(2016, 02, 29, 0, 0), "P1WL1");

        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 30, 0, 0));

        // list of expected adjusted times
        List<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        expectedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedTimes.add(LocalDateTime.of(2016, 5, 30, 0, 0));

        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedTimes.add(adjuster.shift(t)));

        // finally compare unshifted and shifted times
        assertEquals(expectedTimes, shiftedTimes);
    }

    @Test
    public void test_SD_StartDateIsEOM_CycleM() {
        thrown = ExpectedException.none();
        EndOfMonthAdjuster adjuster = new EndOfMonthAdjuster(EndOfMonthConventionEnum.SD, LocalDateTime.of(2016, 02, 29, 0, 0), "P1ML1");

        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 30, 0, 0));

        // list of expected adjusted times
        List<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        expectedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedTimes.add(LocalDateTime.of(2016, 5, 30, 0, 0));

        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedTimes.add(adjuster.shift(t)));

        // finally compare unshifted and shifted times
        assertEquals(expectedTimes, shiftedTimes);
    }
    
    @Test
    public void test_EOM_StartDateIsNotEOM_CycleM() {
        thrown = ExpectedException.none();
        EndOfMonthAdjuster adjuster = new EndOfMonthAdjuster(EndOfMonthConventionEnum.EOM, LocalDateTime.of(2016, 02, 1, 0, 0), "P1ML1");

        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 30, 0, 0));

        // list of expected adjusted times
        List<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        expectedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedTimes.add(LocalDateTime.of(2016, 5, 30, 0, 0));

        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedTimes.add(adjuster.shift(t)));

        // finally compare unshifted and shifted times
        assertEquals(expectedTimes, shiftedTimes);
    }

    @Test
    public void test_EOM_StartDateIsEOM_CycleD() {
        thrown = ExpectedException.none();
        EndOfMonthAdjuster adjuster = new EndOfMonthAdjuster(EndOfMonthConventionEnum.EOM, LocalDateTime.of(2016, 02, 29, 0, 0), "P1DL1");

        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 30, 0, 0));

        // list of expected adjusted times
        List<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        expectedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedTimes.add(LocalDateTime.of(2016, 5, 30, 0, 0));

        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedTimes.add(adjuster.shift(t)));

        // finally compare unshifted and shifted times
        assertEquals(expectedTimes, shiftedTimes);
    }
    
        @Test
    public void test_EOM_StartDateIsEOM_CycleW() {
        thrown = ExpectedException.none();
        EndOfMonthAdjuster adjuster = new EndOfMonthAdjuster(EndOfMonthConventionEnum.EOM, LocalDateTime.of(2016, 02, 29, 0, 0), "P1WL1");

        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 30, 0, 0));

        // list of expected adjusted times
        List<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        expectedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedTimes.add(LocalDateTime.of(2016, 5, 30, 0, 0));

        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedTimes.add(adjuster.shift(t)));

        // finally compare unshifted and shifted times
        assertEquals(expectedTimes, shiftedTimes);
    }

    @Test
    public void test_EOM_StartDateIsEOM_CycleM() {
        thrown = ExpectedException.none();
        EndOfMonthAdjuster adjuster = new EndOfMonthAdjuster(EndOfMonthConventionEnum.EOM, LocalDateTime.of(2016, 02, 29, 0, 0), "P1ML1");

        // list of unadjusted times
        List<LocalDateTime> unadjustedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        unadjustedTimes.add(LocalDateTime.of(2016, 5, 30, 0, 0));

        // list of expected adjusted times
        List<LocalDateTime> expectedTimes = new ArrayList<LocalDateTime>();
        expectedTimes.add(LocalDateTime.of(2016, 4, 30, 0, 0));
        expectedTimes.add(LocalDateTime.of(2016, 5, 31, 0, 0));

        // now shift times to event times according to the business day convention, ...
        List<LocalDateTime> shiftedTimes = new ArrayList<LocalDateTime>();
        unadjustedTimes.forEach(t -> shiftedTimes.add(adjuster.shift(t)));

        // finally compare unshifted and shifted times
        assertEquals(expectedTimes, shiftedTimes);
    }

}

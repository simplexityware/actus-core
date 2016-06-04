/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.conventions.daycount;

import org.actus.conventions.daycount.DayCountConvention;
import org.actus.util.StringUtils;

import java.time.LocalDateTime;

/**
 * Component for the calculation of time-periods between two time-instances 
 * according to a {@link DayCountConvention}
 */
public class DayCountCalculator {
    private DayCountConvention convention;

    /**
     * Constructor
     * 
     * @param convention the {@link DayCountConvention}
     * @param calendar the {@link BusinessDayCalendar} to be used in case of {@code convention="B/252"}
     * @return
     */
    public DayCountCalculator(String convention, String calendar) {
        switch (convention) {
            case StringUtils.DayCountConvention_30E360:
                this.convention = new ThirtyEThreeSixty();
                break;
            case StringUtils.DayCountConvention_30E360ISDA:
                this.convention = new ThirtyEThreeSixtyISDA();
                break;
            case StringUtils.DayCountConvention_A360:
                this.convention = new ActualThreeSixty();
                break;
            case StringUtils.DayCountConvention_A365:
                this.convention = new ActualThreeSixtyFiveFixed();
                break;
            case StringUtils.DayCountConvention_AAISDA:
                this.convention = new ActualActualISDA();
                break;
            case StringUtils.DayCountConvention_B252:
                this.convention = new BusinessTwoFiftyTwo();
                // TODO: ((BusinessTwoFiftyTwo) this.convention).setCalendar(calendar);
                break;
        }
    }

    /**
     * Compute the number of days as a fraction of total number of days in the entire
     * reference year between two time-instances
     * 
     * @param startTime the start of the time period
     * @param endTime the end of the time period
     * @return the number of days as a fraction of total number of days in the entire
     * reference year between startTime and endTime
     */
    public double dayCountFraction(LocalDateTime startTime, LocalDateTime endTime) {
        return convention.dayCountFraction(startTime, endTime);
    }
}
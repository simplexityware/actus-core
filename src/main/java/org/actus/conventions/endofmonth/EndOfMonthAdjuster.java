/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.conventions.endofmonth;

import org.actus.AttributeConversionException;
import org.actus.util.StringUtils;
import org.actus.conventions.endofmonth.EndOfMonthConvention;
import org.actus.conventions.endofmonth.SameDay;
import org.actus.conventions.endofmonth.EndOfMonth;

import java.time.LocalDateTime;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

/**
 * Component for the adjustment of dates according to a {@link EndOfMonthConvention}
 */
public final class EndOfMonthAdjuster {
    private EndOfMonthConvention convention;

    /**
     * Constructor
     * 
     * {@link EndOfMonthConvention}-adjustments are made when creating a schedule of {@link ContractEvent}-dates
     * using the {@link ScheduleFactory}. Whether or not schedule dates are shifted to the end of month is 
     * determined by
     * <ul>
     *      <li> The convention: "SD" = same day and means not adjusting, "EM" = end of month and means adjusting</li>
     *      <li> The start date of the schedule: dates are only shifted if the schedule start date is an end-of-month date</li>
     *      <li> the cycle of the schedule: dates are only shifted if the schedule cycle is based on a "M" period unit or multiple thereof</li>
     * </ul>
     * 
     * @param convention indicates the {@link EndOfMonthConvention} to be applied
     * @param refDate the schedule start date
     * @param periodUnit the period unit of the cycle used in the schedule
     * @throws AttributeConversionException if {@code convention} or {@code periodUnit} does not conform with the ACTUS Data Dictionary
     * @return 
     */
    public EndOfMonthAdjuster(String convention, LocalDateTime refDate, char periodUnit) throws AttributeConversionException {
        switch (convention) {
            case StringUtils.EndOfMonthConvention_EndOfMonth:
                if (refDate.equals(refDate.with(lastDayOfMonth())) && periodUnit == 'M') {
                    this.convention = new EndOfMonth();
                } else {
                    this.convention = new SameDay();
                }
                break;
            case StringUtils.EndOfMonthConvention_SameDay:
                this.convention = new SameDay();
                break;
            default:
                throw new AttributeConversionException();
        }
    }

    /**
     * Shift a date to the end of the month according to a specific convention
     * 
     * @param date the date to be shifted
     * @return the shifted date
     */
    public LocalDateTime shift(LocalDateTime date) {
        return convention.shift(date);
    }
}
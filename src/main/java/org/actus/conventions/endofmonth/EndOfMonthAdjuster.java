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
import java.time.Period;

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
     *      <li> the period of the schedule cycle: dates are only shifted if the schedule cycle is based on an "M" period unit or multiple thereof</li>
     * </ul>
     * 
     * @param convention indicates the {@link EndOfMonthConvention} to be applied
     * @param refDate the schedule start date
     * @param period the period of the cycle used in the schedule
     * @throws AttributeConversionException if {@code convention} does not conform with the ACTUS Data Dictionary
     * @return 
     */
    public EndOfMonthAdjuster(String convention, LocalDateTime refDate, Period period) throws AttributeConversionException {
        switch (convention) {
            case StringUtils.EndOfMonthConvention_EndOfMonth:
                // note, internally, units which are a multiple of "1M" are converted to "XM" why here we only have to check
                // for period-unit M when deciding whether or not to shift a date
                if (refDate.equals(refDate.with(lastDayOfMonth())) && period.getMonths() > 0) {
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

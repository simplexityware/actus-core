/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.conventions.endofmonth;

import java.time.LocalDateTime;

/**
 * Component that represents a certain end of month day convention
 * <p>
 * Different conventions of how to handle the situation when creating a {@link ContractEvent}
 * date schedule (cf. {@link ScheduleFactory}) starting at an end-of-month day exist. E.g. if
 * the start date of the schedule falls on the 30st of April, do we really mean to create a
 * schedule of dates of the 30st of each month or do we mean to use the last day in each
 * month.
 * <p>
 * Make sure, new conventions implement this interface when adding to the library.
 */
public interface EndOfMonthConvention {
    
    /**
     * Shift a date to the end of the month according to a specific convention
     * 
     * @param date the date to be shifted
     * @return the shifted date
     */
	public LocalDateTime shift(LocalDateTime date);

}
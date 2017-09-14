/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.conventions.businessday;

import org.actus.externals.BusinessDayCalendarProvider;

import java.time.LocalDateTime;

/**
 * Implementation of the Following business day convention
 * <p>
 * This business day convention assumes that if a date falls on a non-business day
 * it is shifted to the next following business day. Hence, if {@code d} is the 
 * initial date and {@code d'} the shifted date we have that 
 * <ul>
 * <li> {@code d' = d} if {@code d} is a business day</li>
 * <li> {@code d' > d} if {@code d} is a non-business day</li>
 * </ul>
 */
public final class Following implements BusinessDayConvention {
    private BusinessDayCalendarProvider calendar;
    
    /**
     * Constructor
     * 
     * @param calendar the {@link BusinessDayCalendarProvider} to be used
     */
	public Following(BusinessDayCalendarProvider calendar) {
		this.calendar = calendar;
	}

	/**
	 * Shift the input date to the closest business day if it is a non-business day
	 * 
	 * @param date the date to be shifted
	 * @return the shifted date (a business day)
	 */
	@Override
	public LocalDateTime shift(LocalDateTime date) {
		LocalDateTime shiftedDate = LocalDateTime.from(date);
		while (!calendar.isBusinessDay(shiftedDate)) {
			shiftedDate = shiftedDate.plusDays(1);
		}
		return shiftedDate;
	}

}
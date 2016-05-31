/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.time.calendar;

import java.time.LocalDateTime;

/**
 * Component that represents a specific business day calendar
 * <p>
 * Whether a specific date is a business day or not depends on the 
 * calendar used in the respective business center. This component
 * implements a specific calendar which defines for each date
 * whether it is a business day or not.
 */
public interface BusinessDayCalendarProvider {
    
    /**
     * Returns whether a date is a business day or not
     * 
     * @param date the date which has to be checked
     * @return {@code true} if {@code date} is a business day or {@code false} otherwise
     */
	public boolean isBusinessDay(LocalDateTime date);
}

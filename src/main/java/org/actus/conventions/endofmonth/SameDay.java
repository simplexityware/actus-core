/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.conventions.endofmonth;

import java.time.LocalDateTime;

/**
 * Same Day end of month convention
 * <p>
 * This end of month convention assumes that the day of month used in the
 * schedule start date is kept throughout the schedule. Hence, if {@code d} 
 * is the schedule start date and {@code d'} the shifted date, we have that
 * <ul>
 * <li> {@code d' = d} always</li>
 * </ul>
 */
public class SameDay implements EndOfMonthConvention {
    
    @Override
	public LocalDateTime shift(LocalDateTime date) {
	    return date;
	}
}

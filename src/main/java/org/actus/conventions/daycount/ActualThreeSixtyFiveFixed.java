/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.conventions.daycount;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * ISDA A/365-Fixed day count convention
 * 
 * @see http://www2.isda.org
 */
public class ActualThreeSixtyFiveFixed implements DayCountConvention {

	@Override
	public double dayCount(LocalDateTime startTime, LocalDateTime endTime) {
		return ChronoUnit.DAYS.between(startTime, startTime);
	}

	@Override
	public double dayCountFraction(LocalDateTime startTime,
			LocalDateTime endTime) {
		return this.dayCount(startTime, endTime) / 365.0;
	}

}


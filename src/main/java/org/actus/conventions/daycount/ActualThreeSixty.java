/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.conventions.daycount;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * ISDA A/360 day count convention
 * 
 * @see http://www2.isda.org
 */
public final class ActualThreeSixty implements DayCountConventionProvider {

	@Override
	public double dayCount(LocalDateTime startTime, LocalDateTime endTime) {
		return ChronoUnit.DAYS.between(startTime, endTime);
	}

	@Override
	public double dayCountFraction(LocalDateTime startTime,
			LocalDateTime endTime) {
		return dayCount(startTime, endTime) / 360.0;
	}

}

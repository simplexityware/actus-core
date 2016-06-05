/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.time;

import org.actus.AttributeConversionException;
import org.actus.util.CommonUtils;
import org.actus.util.StringUtils;
import org.actus.conventions.endofmonth.EndOfMonthAdjuster;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.Set;
import java.util.HashSet;

/**
 * A factory for date-schedules
 * <p>
 * This component allows to create a schedule of dates from a [Start-Date]/[Cycle]/[End-Date]/
 * [EndOfMonthConvention] specification. The schedule consists of a series of dates starting 
 * at [Start-Date] and continuing with cyclical dates with periodicity according to [Cycle] 
 * up to [End-Date].
 * <p>
 * Cyclical dates (and cyclical dates only) are adjusted for end-of-month effects if [Cycle]
 * is based on a monthly (or multiple thereof) period, [Start-Date] represents the end of a
 * month, and [EndOfMonthConvention] indicates that such effects should be corrected for.
 * <p>
 * [Start-Date], [Cycle], [End-Date], and [EndOfMonthConvention] are expected as Strings 
 * representing dates, cycle, and end-of-month-convention, respectively, in the ACTUS format.
 * <p>
 * The class is a utility class with only static methods and no constructor, i.e. instances
 * of this class cannot be created but its methods only be accessed in a static way.
 * 
 * @see http://www.projectactus.org/projectactus/?page_id=356
 */
public final class ScheduleFactory {
    
    // this is a utility class so no need for instantiation
	private ScheduleFactory() {
	}

	/**
	 * Create a schedule of dates
	 * 
	 * @param startTime the start time of the schedule
	 * @param endTime the end time of the schedule
	 * @param cycle the schedule cycle
	 * @param endOfMonthConvention the convention to be applied
	 * @return an unordered set of schedule times
	 * 
	 */
	public static Set<LocalDateTime> createSchedule(LocalDateTime startTime, LocalDateTime endTime, String cycle, String endOfMonthConvention) throws AttributeConversionException {    
		EndOfMonthAdjuster adjuster;
		Set<LocalDateTime> timesSet = new HashSet<LocalDateTime>();
        int multiplier;
        char unit;
        Period period;
        char stub;
        
		// if no cycle then only start (if specified) and end dates
		if (CommonUtils.isNull(cycle)) {
		    if (!CommonUtils.isNull(startTime)) {
		      timesSet.add(startTime);
		    }
		    timesSet.add(endTime);
			return timesSet;
		}
        
        // parse cycle
        try {
            multiplier = Integer.parseInt(cycle.substring(0,cycle.length() - 2));
		    unit = cycle.charAt(cycle.length() - 3);
		    if(unit == 'Q') {
		      multiplier = 3;
		      unit = 'M';
		    } else if(unit == 'H') {
		      multiplier = 6;
		      unit = 'M';
		    } else if(unit == 'Y') {
		      multiplier = 12;
		      unit = 'M';
		    }
            stub = cycle.charAt(cycle.length() - 2);
		    period = Period.parse("P" + 1 + unit);
		} catch (Exception e) {
		  throw(new AttributeConversionException());
        }

        // parse end of month convention
        adjuster = new EndOfMonthAdjuster(endOfMonthConvention, startTime, unit);
        
		// init helpers for schedule creation
		int counter = 1;
		LocalDateTime newTime = LocalDateTime.from(startTime);
		
		// create schedule based on end-of-month-convention
		  while (newTime.isBefore(endTime)) {
		     timesSet.add(newTime);
			 period = period.multipliedBy(counter * multiplier);
			 newTime = adjuster.shift(startTime.plus(period));
			 counter++;
		  }		    
		timesSet.add(endTime);
		
        // now adjust for the last stub
		if (stub == StringUtils.LongStub && timesSet.size() > 2 && !endTime.equals(newTime)) {
			timesSet.remove(newTime.minus(period));
		}
		
		// return schedule
		return timesSet;
	}


	/**
	 * Create a schedule of dates comprised by sub-schedules for each startDate/cycle pair
	 * <p>
	 * This method expects {@code startDates} and {@code cycles} to represent a set of 
	 * comma-separated dates [date1,date2,...] and cycles [cycle1,cycle2,...], 
	 * respectively, of equal length. For each [date1/cycle1/date2], [date2/cycle2/date3], 
	 * etc. -pair, a schedule is created. {@code endDate} is used as the end date of the
	 * schedule based on the very last [dateXX/cycleXX]-pair. The overall set of dates is
	 * returned as the schedule.  
	 * 
	 * @param startTimes an array of start times
	 * @param endTime the end time of the schedule
	 * @param cycles an array of cycles
	 * @param endOfMonthConvention the convention to be applied
	 * @return an unordered set of schedule times
	 */
	public static Set<LocalDateTime> createArraySchedule(LocalDateTime[] startTimes,
			LocalDateTime endTime, String[] cycles, String endOfMonthConvention) {
        Set<LocalDateTime> timesSet = new HashSet<LocalDateTime>();
        
        // add schedules 1 to N-1
		for (int i = 0; i < startTimes.length - 1; i++) {
		    timesSet.addAll(
					createSchedule(startTimes[i], startTimes[i + 1], cycles[i], endOfMonthConvention));
		}
		
		// add last schedule
		timesSet.addAll(
				createSchedule(startTimes[startTimes.length - 1], endTime,
						cycles[startTimes.length - 1], endOfMonthConvention));
		
		// return schedule
		return timesSet;
	}
}

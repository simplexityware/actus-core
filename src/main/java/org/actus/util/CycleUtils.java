/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.util;

import org.actus.AttributeConversionException;

import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.DayOfWeek;
import static java.util.Locale.forLanguageTag;

/**
 * Utilities for handling ACTUS cycles
 * <p>
 * 
 */
public final class CycleUtils {
    
    /**
     *
     */
    public static boolean isPeriod(String cycle) {
        return cycle.replaceAll("\\P{L}+", "").length()==1;
    }

    /**
     * 
     */
    public static Period parsePeriod(String cycle, boolean stub) {
        Period period;

        // parse period from cycle
        if(stub) {
            period = parsePeriod(cycle);
        } else {
            period = parsePeriod(cycle + '-');       
        }
        // return period
        return period;
    }
    
    /**
     * 
     */
    public static Period parsePeriod(String cycle) {
        Period period;  
        char unit;
        int multiplier;
        // parse period from cycle
        try {
            multiplier = Integer.parseInt(cycle.substring(0,cycle.length() - 2));
            unit = cycle.charAt(cycle.length() - 2);
            if(unit == 'Q') {
              multiplier *= 3;
              unit = 'M';
            } else if(unit == 'H') {
              multiplier *= 6;
              unit = 'M';
            } else if(unit == 'Y') {
              multiplier *= 12;
              unit = 'M';
            }
            period = Period.parse("P" + multiplier + unit);
        } catch (Exception e) {
          throw(new AttributeConversionException());
        }
        // return period
        return period;
    }
    
    public static int parsePosition(String cycle) {
        int position;
        
        // parse position from cycle
        try {
          position = Character.getNumericValue(cycle.charAt(0));
        } catch (Exception e) {
          throw(new AttributeConversionException());
        }
        // return weekday position
        return position; 
    }

    public static DayOfWeek parseWeekday(String cycle) {
        DayOfWeek weekday;
        // parse weekday from cycle
        try {
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E", forLanguageTag("en"));
          TemporalAccessor accessor = formatter.parse(cycle.substring(1,cycle.length()-1));
          System.out.println(cycle.substring(1,cycle.length()-1));
          weekday= DayOfWeek.from(accessor);
        } catch (Exception e) {
          throw(new AttributeConversionException());
        }
        // return weekday
        return weekday; 
    }

    /**
     * 
     */
    public static char parseStub(String cycle) throws AttributeConversionException {
        char stub = cycle.charAt(cycle.length() - 1);
        if(stub=='-' || stub=='+') {
          return stub; 
        } else {
          throw(new AttributeConversionException());
        }   
    }
    
}

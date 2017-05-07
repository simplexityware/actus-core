/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.util;

import org.actus.AttributeConversionException;

import java.time.Period;

/**
 * Utilities for handling ACTUS cycles
 * <p>
 * 
 */
public final class CycleUtils {
    
    /**
     * 
     */
    public static Period parsePeriod(String cycle, boolean stub) {
        Period period;  
        char unit;
        int multiplier;
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
    
    /**
     * 
     */
    public static char parseStub(String cycle) {
        return cycle.charAt(cycle.length() - 1);    
    }
    
}

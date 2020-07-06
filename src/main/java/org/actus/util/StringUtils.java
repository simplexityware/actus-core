/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.util;


/**
 * Contains constants representing String-conventions
 */
public final class StringUtils {

    // cycle stubs
    public final static char LongStub = '+';
    public final static char ShortStub = '-';

    // day count conventions
    public final static String DayCountConvention_AAISDA = "A/AISDA";
    public final static String DayCountConvention_A360 = "A/360";
    public final static String DayCountConvention_A365 = "A/365";
    public final static String DayCountConvention_B252 = "B/252";
    public final static String DayCountConvention_30E360 = "30E/360";
    public final static String DayCountConvention_30E360ISDA = "30E/360ISDA";
    public final static String DayCountConvention_A336 = "A/336";
    public final static String DayCountConvention_28336 = "28/336";
    
    // calc/shift conventions
    public final static String CalcShiftConvention_CS = "CS";
    public final static String CalcShiftConvention_SC = "SC";
}

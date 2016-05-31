/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.util;

/**
 * Contains constants representing String-conventions
 */
public final class StringUtils {
    
    // general
    public final static String NullString = "NULL";
    
    // cycle stubs
    public final static char LongStub = '-';
    public final static char ShortStub = '+';
    
    // end of month conventions
    public final static String EndOfMonthConvention_SameDay = "SD";
    public final static String EndOfMonthConvention_EndOfMonth = "EOM";
    
    // day count conventions
    public final static String DayCountConvention_AAISDA = "A/AISDA";
    public final static String DayCountConvention_A360 = "A/360";
    public final static String DayCountConvention_A365 = "A/365";
    public final static String DayCountConvention_B252 = "B/252";
    public final static String DayCountConvention_30E360 = "30/E360";
    public final static String DayCountConvention_30E360ISDA = "30/E360ISDA";
    
    // business day conventions
    public final static String BusinessDayConvention_S = "SAME";
    public final static String BusinessDayConvention_F = "F";
    public final static String BusinessDayConvention_MF = "MF";
    public final static String BusinessDayConvention_P = "P";
    public final static String BusinessDayConvention_MP = "MP";
    
    // calc/shift conventions
    public final static String CalcShiftConvention_CS = "CS";
    public final static String CalcShiftConvention_SC = "SC";
    
    // event types
    public final static String EventType_SD = "SD";
    public final static String EventType_AD = "AD";
    public final static String EventType_IED = "IED";
    public final static String EventType_MD = "MD";
    public final static String EventType_IP = "IP";
    public final static String EventType_IPCI = "IPCI";
    public final static String EventType_RR = "RR";
    public final static String EventType_SC = "SC";
    public final static String EventType_PRD = "PRD";
    public final static String EventType_TD = "TD";
    public final static String EventType_CD = "CD";
    public final static String EventType_PR = "PR";
}

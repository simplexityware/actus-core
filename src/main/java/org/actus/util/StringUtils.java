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
    
    // contract types 
    public final static String ContractType_PAM = "PAM";
    public final static String ContractType_LAM = "LAM";
    public final static String ContractType_NAM = "NAM";
    public final static String ContractType_ANN = "ANN";
    public final static String ContractType_CLM = "CLM";
    public final static String ContractType_CSH = "CSH";
    public final static String ContractType_STK = "STK";
    public final static String ContractType_COM = "COM";
    public final static String ContractType_FXOUT = "FXOUT";
    public final static String ContractType_SWPPV = "SWPPV";
    
    // cycle stubs
    public final static char LongStub = '+';
    public final static char ShortStub = '-';
    
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
    public final static String EventType_PR = "PR";
    public final static String EventType_PP = "PP";
    public final static String EventType_PY = "PY";
    public final static String EventType_FP = "FP";
    public final static String EventType_IP = "IP";
    public final static String EventType_IPCI = "IPCI";
    public final static String EventType_RR = "RR";
    public final static String EventType_SC = "SC";
    public final static String EventType_PRD = "PRD";
    public final static String EventType_TD = "TD";
    public final static String EventType_CD = "CD";
    public final static String EventType_IPCB = "IPCB";
    public final static String EventType_DV = "DV";
    public final static String EventType_STD = "STD";
    
    // contract roles
    public final static String ContractRole_RPA = "RPA";
    public final static String ContractRole_RPL = "RPL";
    
    // settlement types
    public final static char Settlement_Physical = 'D';
    public final static char Settlement_Cash = 'S';
}

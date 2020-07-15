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
    public final static char LongStub = '0';
    public final static char ShortStub = '1';
  
    // day count conventions
    public final static String DayCountConvention_AAISDA = "AA";
    public final static String DayCountConvention_A360 = "A360";
    public final static String DayCountConvention_A365 = "A365";
    public final static String DayCountConvention_B252 = "B252";
    public final static String DayCountConvention_30E360 = "30E360";
    public final static String DayCountConvention_30E360ISDA = "30E360ISDA";
    public final static String DayCountConvention_A336 = "A336";
    public final static String DayCountConvention_28336 = "28336";
    
    // calc/shift conventions
    public final static String CalcShiftConvention_CS = "CS";
    public final static String CalcShiftConvention_SC = "SC";
    
    // event types
    public final static String EventType_SD = "SD";
    public final static String EventType_AD = "AD";
    public final static String EventType_IED = "IED";
    public final static String EventType_MD = "MD";
    public final static String EventType_PR = "PR";
    public final static String EventType_PP = "PP";
    public final static String EventType_PY = "PY";
    public final static String EventType_FP = "FP";
    public final static String EventType_IP = "IP";
    public final static String EventType_IPCI = "IPCI";
    public final static String EventType_RR = "RR";
    public final static String EventType_RRF = "RRF";
    public final static String EventType_SC = "SC";
    public final static String EventType_PRD = "PRD";
    public final static String EventType_TD = "TD";
    public final static String EventType_CD = "CD";
    public final static String EventType_IPCB = "IPCB";
    public final static String EventType_DV = "DV";
    public final static String EventType_STD = "STD";
    public final static String EventType_PI = "PI";
    
    // contract roles
    public final static String ContractRole_RPA = "RPA";
    public final static String ContractRole_RPL = "RPL";
    public final static String ContractRole_BUY = "BUY";
    public final static String ContractRole_SEL = "SEL";
    public final static String ContractRole_RFL = "RFL";
    public final static String ContractRole_PFL = "PFL";
    public final static String ContractRole_RF = "RF";
    public final static String ContractRole_PF = "PF";
    
    // settlement types
    public final static String Settlement_Physical = "D";
    public final static String Settlement_Cash = "S";

    // transaction-type events
    public final static Set<String> TransactionalEvents = Stream.of(
            StringUtils.EventType_IED,StringUtils.EventType_DV,StringUtils.EventType_FP,
            StringUtils.EventType_IP,StringUtils.EventType_PP,StringUtils.EventType_PR,
            StringUtils.EventType_PRD,StringUtils.EventType_PY,StringUtils.EventType_STD,
            StringUtils.EventType_TD).collect(Collectors.toSet());

    // risk factor state contingent events
    public final static Set<String> ContingentEvents = Stream.of(
            StringUtils.EventType_CD,StringUtils.EventType_RR,StringUtils.EventType_SC,
            StringUtils.EventType_DV,StringUtils.EventType_PP,StringUtils.EventType_STD).collect(Collectors.toSet());

    // contract status
    public final static String ContractStatus_Performant = "PF";
    public final static String ContractStatus_Delayed = "DL";
    public final static String ContractStatus_Delinquent = "DQ";
    public final static String ContractStatus_Default = "DF";

}

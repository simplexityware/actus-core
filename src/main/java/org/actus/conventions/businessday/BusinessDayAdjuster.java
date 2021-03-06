/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.conventions.businessday;

import org.actus.AttributeConversionException;
import org.actus.time.calendar.BusinessDayCalendarProvider;
import org.actus.util.StringUtils;
import org.actus.util.CommonUtils;

import java.time.LocalDateTime;

/**
 * Component for the adjustment of dates according to a {@link BusinessDayConvention} and {@link ShiftCalcConvention}
 */
public final class BusinessDayAdjuster {
    private BusinessDayConvention bdConvention;
    private ShiftCalcConvention   scConvention;

    /**
     * Constructor
     * 
     * {@link BusinessDayConvention}-adjustments are made when creating a schedule of {@link ContractEvent}-dates
     * using the {@link ScheduleFactory}. Whether or not schedule dates are shifted to a "nearest" business
     * day and whether this shift is only applied for the date of occurence of an event or also to the
     * calculation of related quantities, e.g. accrued interest, is determined by the actual convention.
     * 
     * @param convention indicates the {@link BusinessDayConvention} to be applied
     * @param calendar provides the {@link BusinessDayCalendarProvider} used with the convention
     * @throws AttributeConversionException if {@code convention} or {@code calendar} does not conform with the ACTUS Data Dictionary
     * @return 
     */
    public BusinessDayAdjuster(String convention, BusinessDayCalendarProvider calendar) {
        String suffix;
        String prefix;

        // convert contract attributes
        if (CommonUtils.isNull(convention) || convention.equals(StringUtils.BusinessDayConvention_S)) {
            
            this.bdConvention = new Same();
            this.scConvention = new ShiftCalc();

        } else {

            try {
                prefix = convention.substring(0, 2);
                suffix = convention.substring(2);
            } catch (Exception e) {
                throw new AttributeConversionException();
            }

            switch (prefix) {
                case StringUtils.CalcShiftConvention_CS:
                    scConvention = new CalcShift();
                    break;
                case StringUtils.CalcShiftConvention_SC:
                    scConvention = new ShiftCalc();
                    break;
                default:
                    throw new AttributeConversionException();
            }

            switch (suffix) {
                case StringUtils.BusinessDayConvention_F:
                    bdConvention = new Following(calendar);
                    break;
                case StringUtils.BusinessDayConvention_MF:
                    bdConvention = new ModifiedFollowing(calendar);
                    break;
                case StringUtils.BusinessDayConvention_P:
                    bdConvention = new Preceeding(calendar);
                    break;
                case StringUtils.BusinessDayConvention_MP:
                    bdConvention = new ModifiedPreceeding(calendar);
                    break;
                default:
                    throw new AttributeConversionException();
            }
        }

    }

    /**
     * Returns shifted {@link ContractEvent}-time according to the specified {@link BusinessDayConvention}
     * 
     * @param time the time to be shifted to an event time
     * @return the shifted time
     */
    public LocalDateTime shiftEventTime(LocalDateTime time) {
        return bdConvention.shift(time);
    }

    /**
     * Returns shifted {@link ContractEvent}-calculation time according to the specified {@link BusinessDayConvention}
     * 
     * @param time the time to be shifted to an event-calculation time
     * @return the shifted time
     */
    public LocalDateTime shiftCalcTime(LocalDateTime time) {
        return scConvention.shift(time, bdConvention);
    }
}

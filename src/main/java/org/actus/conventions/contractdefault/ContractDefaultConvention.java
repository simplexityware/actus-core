/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.conventions.contractdefault;

import org.actus.AttributeConversionException;
import org.actus.util.StringUtils;

/**
 * Convention determining contract performance (in default / performing) according to ContractStatus
 *
 */
public final class ContractDefaultConvention {

    // this is a utility class
    private ContractDefaultConvention() {
    }

    /**
     * Returns an integer indicating whether the contract is performing or in default
     * <p>
     *
     *
     * @param status the ContractStatus to be evaluated to performing or in default
     * @return an integer of 0 indicating the contract is "in default" or 1 if performing
     */
    public static int performanceIndicator(String status) {
        return (status.equals(StringUtils.ContractStatus_Default))? 0 : 1;
    }
}

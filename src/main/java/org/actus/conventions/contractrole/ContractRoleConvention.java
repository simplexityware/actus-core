/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.conventions.contractrole;

import org.actus.AttributeConversionException;
import org.actus.util.StringUtils;

/**
 * Convention determining the direction of cash flows based on attribute ContractRole
 * 
 */
public final class ContractRoleConvention {
    
    // this is a utility class
    private ContractRoleConvention() {
    }
    
    /**
     * Returns an integer indicating the direction of cash flows in the contract's pay-off
     * <p>
     * ACTUS contract attribute "ContractRole" codifies the direction of cash flows in a
     * contract's pay-off. The pay-off functions treat the direction of cash flows as
     * an integer of +1 for cash in-flows (cash flowing from LegalEntityIDCounterparty to 
     * LegalEntityIDRecordCreator) and -1 for cash out-flows (cash flowing from 
     * LegalEntityIDRecordCreator to LegalEntityIDCounterparty). This method translates
     * value of attribute ContractRole to an integer +1 or -1 indicating the direction of
     * cash flows.
     * 
     * @param role the value of attribute ContractRole
     * @return an integer of +1 (for cash in-flows) and -1 (for cash out-flows)
     * @throws AttributeConversionException if the value of the method argument is invalid
     */
    public static int roleSign(String role) throws AttributeConversionException {
        int sign;
        switch(role) {
            case StringUtils.ContractRole_RPA:
            sign = 1;
            break;
            case StringUtils.ContractRole_RPL:
            sign = -1;
            break;
            default:
                throw new AttributeConversionException();
        }
        return sign;
    }
}

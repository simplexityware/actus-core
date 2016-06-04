package org.actus.conventions.contractrole;

import org.actus.AttributeConversionException;
import org.actus.util.StringUtils;

public final class ContractRoleConvention {
    
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

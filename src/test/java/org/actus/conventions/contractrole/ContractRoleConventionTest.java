/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.conventions.contractrole;

import org.actus.AttributeConversionException;
import org.actus.conventions.contractrole.ContractRoleConvention;

import org.actus.types.ContractRole;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.assertEquals;

public class ContractRoleConventionTest {
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test_AttributeConversionException() {
        thrown.expect(AttributeConversionException.class);
        ContractRoleConvention.roleSign(null);
    }

    @Test
    public void test_RPA() {
        thrown = ExpectedException.none();
        int expectedSign = 1;
        assertEquals(expectedSign, ContractRoleConvention.roleSign(ContractRole.RPA));
    }
    
    @Test
    public void test_RPL() {
        thrown = ExpectedException.none();
        int expectedSign = -1;
        assertEquals(expectedSign, ContractRoleConvention.roleSign(ContractRole.RPL));
    }
    
}

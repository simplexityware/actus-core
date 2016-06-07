/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.conventions.contractrole;

import org.actus.AttributeConversionException;
import org.actus.conventions.contractrole.ContractRoleConvention;

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
        ContractRoleConvention.roleSign("INEXISTENT");
    }

    @Test
    public void test_RPA() {
        thrown = ExpectedException.none();
        int expectedSign = 1;
        assertEquals(expectedSign, ContractRoleConvention.roleSign("RPA"));
    }
    
    @Test
    public void test_RPL() {
        thrown = ExpectedException.none();
        int expectedSign = -1;
        assertEquals(expectedSign, ContractRoleConvention.roleSign("RPL"));
    }
    
}

/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.conventions.businessday;

import org.actus.AttributeConversionException;
import org.actus.conventions.businessday.BusinessDayAdjuster;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class BusinessDayAdjusterTest {
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void test_Constructor_SAME_NO() {
        thrown = ExpectedException.none();
        BusinessDayAdjuster adjuster = new BusinessDayAdjuster("SAME", "NO");
    }
    
    @Test
    public void test_AttributeConversionException() {
        thrown.expect(AttributeConversionException.class);
        BusinessDayAdjuster adjuster = new BusinessDayAdjuster("INEXISTENT", "NO");
    }
    
}

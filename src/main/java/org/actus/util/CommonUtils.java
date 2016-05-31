/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.util;

import org.actus.util.StringUtils;

/**
 * Contains general-purpose utilities used throughout the library
 */
public final class CommonUtils {
    
    public static boolean isNull(Object o) {
        return (o == null || String.valueOf(o).equals(StringUtils.NullString))? true : false;
    }
}

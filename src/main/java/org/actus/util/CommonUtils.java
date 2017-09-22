/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.util;

/**
 * Contains general-purpose utilities used throughout the library
 */
public final class CommonUtils {
    
    public static boolean isNull(Object o) {
        return (o == null || String.valueOf(o).equals("NULL"))? true : false;
    }
}

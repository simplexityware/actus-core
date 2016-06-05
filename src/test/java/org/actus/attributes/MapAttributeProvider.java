/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.attributes;

import java.util.Map;
import java.util.HashMap;

public class MapAttributeProvider implements AttributeProvider {
    Map<String, String> attr;
    
    public MapAttributeProvider() {
       attr = new HashMap<String, String>();
    }
    
    public void put(Map<String,String> attributes) {
        attr.putAll(attributes);    
    }
    
    public String get(String name) {
        return attr.get(name);
    }
}

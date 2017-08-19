/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.externals;

/**
 * A data structure representing the set of ACTUS contract attributes
 * <p>
 * This is an internal representation of the various contract attributes used when 
 * evaluating the contract pay-off function. Hence, as opposed to an external, 
 * representation, e.g. a file, database, etc., this component carries the values of
 * attributes in their respective Java-data type which is required when evaluating 
 * a {@link ContractType}.
 * 
 * @see <a href="http://www.projectactus.org/projectactus/?page_id=356">ACTUS Data Dictionary</a>
 */
public abstract interface ContractModelProvider {
    
    public <T> T getAs(String name);
    
}

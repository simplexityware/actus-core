/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.attributes;

/**
 * Component that provides a data structure for the ACTUS attributes input to a {@link ContractType}
 * <p>
 * Contract attributes represent an ACTUS {@link ContractType} in terms of its parameters. Thereby,
 * the set of attributes is unique for each {@link ContractType}. This component provides a
 * structured representation of the set of attributes. 
 * <p>
 * Once created, an {@code AttributeProvider}-object can be passed on to a {@link ContractType} upon
 * its {@code map}-method call as a specific parameterization.
 * <p>
 */
public interface AttributeProvider {
  
 /**
   * Returns the value of attribute with Long Name (according to the ACTUS Data Dictionary) 
   * specified in the method argument 'name' or 'null' if the respective attribute is undefined.
   * <p>
   * The attribute is returned as an Object. It is the caller's responsibility to type-cast 
   * the returned object into it's true data type.
   * 
   * @param longName  the attribute's Long Name
   * @return a String-representation of the attribute's value
   * @see <a href="http://www.projectactus.org/projectactus/?page_id=356">ACTUS Data Dictionary</a>
   */
  public String valueOf(String longName);
}

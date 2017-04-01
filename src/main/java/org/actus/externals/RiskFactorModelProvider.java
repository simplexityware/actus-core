/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.externals;

import org.actus.states.StateSpace;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * A representation of an external Comprehensive Risk Factor Model
 * <p>
 * Generally, the payoff of financial instruments is evaluated in the context of an environment that
 * embodies the dynamics (and therewith future states) of all relevant stochastic risk factors - or what
 * we call the Comprehensive Risk Factor Model. Thus, method {@code eval} of a {@link ContractType} produces 
 * a series of Risk Factor state contingent {@link ContractEvent}s. An external {@code RiskFactorModelProvider} 
 * implements these dynamics and allows to retrieve the state of all defined risk factors identified
 * through a unique {@code id} at any future time.
 * <p>
 * Note, all sorts of external variables on which the payoff of a {@link ContractType} is conditioned
 * must be implemented in the {@code RiskFactorModelProvider}. In particular, any source of market risk,
 * credit risk, or behavioral risk needs to be represented therein and identifiable through a unique ID.
 * Examples of such risk factors are the reference market rate linked to through contract attribute
 * {@code MarketObjectCodeOfRateReset}, the default indicator of a legal entity linked to e.g. through
 * attribute {@code LegalEntityIDCounterparty}, or the market value of an underlying linked to through
 * attribute {@code ContractID} of the underlying.
 * <p>
 * @see <a href="http://www.projectactus.org">ACTUS</a>
 */
public abstract interface RiskFactorModelProvider {
  
  /**
   * Returns the set of unique risk factor IDs
   * 
   * @return set of unique risk factor IDs
   */
  public Set<String> keys();
  
  /**
   * Returns the set of event times for a particular risk factor
   * 
   * @param id identifier of the risk factor
   * @return set of risk factor event times
   */
  public Set<LocalDateTime> times(String id);
  
  /**
   * Returns the state of a particular risk factor at a future time
   * 
   * @param id identifier of the risk factor
   * @param time future time for which to return the risk factor's state
   * @param contractStates the inner states of the contract as per @code{time} argument of the method
   * @param contractAttributes the attributes of the contract calling the method
   * @return double the state of the risk factor
   */
  public double stateAt(String id, LocalDateTime time, StateSpace contractStates, ContractModelProvider contractAttributes);
}
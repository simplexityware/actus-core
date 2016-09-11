/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.riskfactors;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * A representation of an exogenous Risk Factor Model
 * <p>
 * Generally, the payoff of financial instruments is dependent on the evolution of some stochastic
 * risk drivers. Thus, method {@code map} of an {@link ContractType} produces a series of risk factor
 * state contingent {@link ContractEvent}s. The future states of such risk factors is captured in a
 * model of their dynamics. The {@code RiskFactorProvider} provides these dynamics and allows to
 * retrieve the state of a set of risk factors identified through a unique {@code id} at any future
 * time. 
 * <p>
 * @see <a href="http://www.projectactus.org">ACTUS</a>
 */
public interface RiskFactorProvider {
  
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
   * @return double the state of the risk factor
   */
  public double stateAt(String id, LocalDateTime time);
}

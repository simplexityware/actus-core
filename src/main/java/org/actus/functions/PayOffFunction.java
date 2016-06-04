/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.functions;

import org.actus.attributes.ContractModel;
import org.actus.riskfactors.RiskFactorProvider;
import org.actus.states.StateSpace;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;

import java.time.LocalDateTime;

/**
 * Component that computes the payoff for a specific {@link ContractEvent}
 * <p>
 * This component implements the payoff function for a specific {@link ContractEvent} 
 * (and {@link ContractType}). Upon evaluation of a {@link ContractEvent} this component
 * and {@link StateTransitionFunction} are evaluated through method {@code eval}. Return
 * value of this component's {@code eval} function gives the payoff for the respective
 * {@link ContractEvent}.
 */
public interface PayOffFunction {
    
    /**
     * Evaluate the function
     * 
     * @param time the schedule time of this particular event
     * @param states the current state of conract states
     * @param model the model containing parsed contract attributes
     * @param riskFactors the risk factor model
     * @param dayCounter the day count convention used to calculate day count fractions
     * @param timeAdjuster the business day convention used to shift the schedule time
     * @return an array of post-event states of numerical contract states
     */
    public double eval(LocalDateTime time, StateSpace states, 
    ContractModel model, RiskFactorProvider riskFactors, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster);
}
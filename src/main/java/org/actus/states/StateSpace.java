/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.states;

import java.time.LocalDateTime;

/**
 * A data structure representing various states of a {@link ContractType}
 * <p>
 * The states of a {@link ContractType} contain important information used when evaluating 
 * {@link PayOffFunction}. Further, states themselves contain atomic analytical elements 
 * such as the nominal value or accrued interest for a lending instrument. On the other hand,
 * states are updated throughout an instrument's lifetime through evaluation of {@link StateTransitionFunction}
 * in the various {@link ContractEvent}s.
 */
public final class StateSpace {
    public LocalDateTime lastEventTime;
    public double timeFromLastEvent; // analytical result
    public double nominalValue; // analytical result
    public double nominalAccrued; // analytical result
    public double feeAccrued; // analytical result
    public double nominalRate; // analytical result
    public double interestCalculationBase;
    public double interestScalingMultiplier;
    public double nominalScalingMultiplier;
    public double nextPrincipalRedemptionPayment;
    public double secondaryNominalValue; // analytical result
    public double lastInterestPayment;
    public double payoffAtSettlement;
    public double variationMargin; // analytical result
    public double probabilityOfDefault; // analytical result
    public int contractRoleSign;
}

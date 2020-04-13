/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.states;

import org.actus.util.StringUtils;

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
    public double           accruedInterest; // analytical result
    public double           accruedInterest2;
    public String           contractPerformance = StringUtils.ContractStatus_Performant; // TODO: initialize together with other states
    public double           exerciseAmount;
    public LocalDateTime    exerciseDate;
    public double           feeAccrued; // analytical result
    public double           interestCalculationBaseAmount;
    public double           interestScalingMultiplier;
    public double           nextPrincipalRedemptionPayment;
    public double           nominalInterestRate; // analytical result
    public double           nominalInterestRate2;
    public LocalDateTime    nonPerformingDate;
    public double           notionalPrincipal; // analytical result
    public double           notionalPrincipal2; // analytical result
    public double           notionalScalingMultiplier;
    public LocalDateTime    statusDate;























}

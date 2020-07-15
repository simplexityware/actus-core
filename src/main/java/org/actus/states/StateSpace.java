/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.states;

import org.actus.types.ContractPerformance;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.StringJoiner;

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
    public double               accruedInterest; // analytical result
    public double               accruedInterest2;
    public ContractPerformance  contractPerformance = ContractPerformance.PF; // TODO: initialize together with other states
    public double               exerciseAmount;
    public LocalDateTime        exerciseDate;
    public double               feeAccrued; // analytical result
    public double               interestCalculationBaseAmount;
    public double               interestScalingMultiplier;
    public double               nextPrincipalRedemptionPayment;
    public double               nominalInterestRate; // analytical result
    public double               nominalInterestRate2;
    public LocalDateTime        nonPerformingDate;
    public double               notionalPrincipal; // analytical result
    public double               notionalPrincipal2; // analytical result
    public double               notionalScalingMultiplier;
    public LocalDateTime        statusDate;

    /**
     * Returns a String-representation of all analytical elements
     * @return a single String containing all analytical elements
     */
    @Override
    public String toString(){
        StringJoiner joiner = new StringJoiner(" ");

        joiner.add(Double.toString(accruedInterest))
                .add(Double.toString(accruedInterest2))
                .add(contractPerformance.toString())
                .add(Double.toString(exerciseAmount))
                //add empty string if not set
                .add(Objects.isNull(exerciseDate) ? "" : exerciseDate.toString())
                .add(Double.toString(feeAccrued))
                .add(Double.toString(interestCalculationBaseAmount))
                .add(Double.toString(interestScalingMultiplier))
                .add(Double.toString(nextPrincipalRedemptionPayment))
                .add(Double.toString(nominalInterestRate))
                .add(Double.toString(nominalInterestRate2))
                //add empty string if not set
                .add(Objects.isNull(nonPerformingDate) ? "" : nonPerformingDate.toString())
                .add(Double.toString(notionalPrincipal))
                .add(Double.toString(notionalPrincipal2))
                .add(Double.toString(notionalScalingMultiplier))
                //add empty string if not set
                .add(Objects.isNull(statusDate) ? "" : statusDate.toString())
        ;

        return joiner.toString();
    }
}

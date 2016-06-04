package org.actus.attributes;

import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.daycount.DayCountCalculator;

import java.time.LocalDateTime;

public class ContractModel {
        public String calendar;
    public BusinessDayAdjuster businessDayConvention;
    public String endOfMonthConvention;
    // public String contractType;
    public LocalDateTime statusDate;
    public String contractRole;
    public int contractRoleSign;
    // public String legalEntityIDRecordCreator;
    // public String contractID;
    public String legalEntityIDCounterparty;
    // public String contractStatus;
    // public String seniority;
    // public LocalDateTime nonPerformingDate;
    public double guaranteedExposure;
    public double coverageOfCreditEnhancement;
    public String minimumDelay;
    // public String coveredContracts;
    // public String coveringContracts;
    public String coveredLegalEntity;
    public LocalDateTime cycleAnchorDateOfDividend;
    public String cycleOfDividend;
    public String marketObjectCodeOfDividendRate;
    public LocalDateTime cycleAnchorDateOfFee;
    public String cycleOfFee;
    public double feeBasis;
    public double feeRate;
    public double feeAccrued;
    public LocalDateTime cycleAnchorDateOfInterestPayment;
    public LocalDateTime[] arrayCycleAnchorDateOfInterestPayment;
    public String cycleOfInterestPayment;
    public String[] arrayCycleOfInterestPayment;
    public double nominalInterestRate;
    public double nominalInterestRate2;
    public DayCountCalculator dayCountConvention;
    public double accruedInterest;
    public LocalDateTime capitalizationEndDate;
    public LocalDateTime cycleAnchorDateOfInterestCalculationBase;
    public String cycleOfInterestCalculationBase;
    public String interestCalculationBase;
    public double interestCalculationBaseAmount;
    public String cyclePointOfInterestPayment;
    // public double deferredInterest;
    // public double maximumDeferredInterest;
    public String clearingHouse;
    public double initialMargin;
    public double maintenanceMarginLowerBound;
    public double maintenanceMarginUpperrBound;
    public LocalDateTime cycleAnchorDateOfMargining;
    public String cycleOfMargining;
    public double variationMargin;
    public String currency;
    public String currency2;
    public LocalDateTime amortizationDate;
    // public LocalDateTime contractDealDate;
    public LocalDateTime initialExchangeDate;
    public double premiumDiscountAtIED;
    public LocalDateTime maturityDate;
    public double notionalPrincipal;
    public double notionalPrincipal2;
    public double quantity;
    public String unit;
    public double unitMultiple;
    public LocalDateTime cycleAnchorDateOfPrincipalRedemption;
    public LocalDateTime[] arrayCycleAnchorDateOfPrincipalRedemption;
    public String cycleOfPrincipalRedemption;
    public String[] arrayCycleOfPrincipalRedemption;
    public double nextPrincipalRedemptionPayment;
    public double[] arrayNextPrincipalRedemptionPayment;
    public String[] arrayIncreaseDecrease;
    public LocalDateTime purchaseDate;
    public double priceAtPurchaseDate;
    public LocalDateTime terminationDate;
    public double priceAtTerminationDate;
    public double prepaymentAmount; // TODO: review with prepayment concept
    public int prepaymentRule; // TODO: review with prepayment concept
    public String objectCodeOfPrepaymentModel; // TODO: review with prepayment concept
    public String xDayNotice;
    // public double remainingPrincipalDue;
    public String marketObjectCodeOfScalingIndex;
    public double scalingIndexAtStatusDate;
    public LocalDateTime cycleAnchorDateOfScalingIndex;
    public String cycleOfScalingIndex;
    public String scalingEffect;
    // public double marketValueObserved;
    public double conversionFactor;
    public String optionExecutionType;
    public LocalDateTime optionExerciseEndDate; // TODO: review with prepayment concept
    public double optionStrike1;
    public double optionStrike2;
    // public String optionStrikeDriver;
    public String optionType;
    public LocalDateTime cycleAnchorDateOfOptionality; // TODO: review with prepayment concept
    public String cycleOfOptionality; // TODO: review with prepayment concept
    public int cycleTriggerOfOptionality; // TODO: review with prepayment concept
    public double maximumPenaltyFreeDisbursement;
    public LocalDateTime cycleAnchorDateOfRateReset;
    public LocalDateTime[] arrayCycleAnchorDateOfRateReset;
    public String cycleOfRateReset;
    public String[] arrayCycleOfRateReset;
    public double rateSpread;
    public double[] arrayRate;
    public String[] arrayFixedVariable;
    public String marketObjectCodeRateReset;
    public String cyclePointOfRateReset;
    public String fixingDays;
    public double nextResetRate;
    public double rateMultiplier;
    public String rateTerm;
    // public double yieldCurveCorrection; // TODO: review
    public LocalDateTime settlementDate;
    public String deliverySettlement;
    public double futuresPrice;
}

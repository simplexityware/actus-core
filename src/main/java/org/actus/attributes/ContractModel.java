/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.attributes;

import org.actus.externals.ContractModelProvider;
import org.actus.externals.BusinessDayCalendarProvider;
import org.actus.time.calendar.NoHolidaysCalendar;
import org.actus.time.calendar.MondayToFridayCalendar;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.attributes.ContractModel;
import org.actus.AttributeConversionException;
import org.actus.ContractTypeUnknownException;
import org.actus.util.StringUtils;
import org.actus.util.CommonUtils;

import java.util.Map;
import java.time.LocalDateTime;

/**
 * A data structure representing the set of ACTUS contract attributes
 * <p>
 * This is a simple implementation of the {@link ContractModelProvider} interface representing
 * a generic data structure for the various ACTUS attributes parametrizing a {@link ContractType}.
 * Method {@code parse} allows parsing the attributes from an input {@code String} representation 
 * to the internal data types.
 * <p>
 * Note, an ACTUS {@link ContractType} can deal with any data structure implementing the 
 * {@link ContractModelProvider} interface. Thus, depending on the system ACTUS is embedded in,
 * more efficient data structures and parsing methods are possible.
 * 
 * @see <a href="http://www.projectactus.org/projectactus/?page_id=356">ACTUS Data Dictionary</a>
 */
public class ContractModel implements ContractModelProvider {
    private BusinessDayCalendarProvider calendar;
    private BusinessDayAdjuster businessDayConvention;
    private String endOfMonthConvention;
    private String contractType;
    private LocalDateTime statusDate;
    private String contractRole;
    // private String legalEntityIDRecordCreator;
    // private String contractID;
    private String legalEntityIDCounterparty;
    // private String contractStatus;
    // private String seniority;
    // private LocalDateTime nonPerformingDate;
    private double guaranteedExposure;
    private double coverageOfCreditEnhancement;
    private String minimumDelay;
    // private String coveredContracts;
    // private String coveringContracts;
    private String coveredLegalEntity;
    private LocalDateTime cycleAnchorDateOfDividendPayment;
    private String cycleOfDividendPayment;
    private String marketObjectCodeOfDividendRate;
    private LocalDateTime cycleAnchorDateOfFee;
    private String cycleOfFee;
    private char feeBasis;
    private double feeRate;
    private double feeAccrued;
    private LocalDateTime cycleAnchorDateOfInterestPayment;
    private LocalDateTime[] arrayCycleAnchorDateOfInterestPayment;
    private String cycleOfInterestPayment;
    private String[] arrayCycleOfInterestPayment;
    private double nominalInterestRate;
    private double nominalInterestRate2;
    private DayCountCalculator dayCountConvention;
    private double accruedInterest;
    private LocalDateTime capitalizationEndDate;
    private LocalDateTime cycleAnchorDateOfInterestCalculationBase;
    private String cycleOfInterestCalculationBase;
    private String interestCalculationBase;
    private double interestCalculationBaseAmount;
    private String cyclePointOfInterestPayment;
    // private double deferredInterest;
    // private double maximumDeferredInterest;
    private String clearingHouse;
    private double initialMargin;
    private double maintenanceMarginLowerBound;
    private double maintenanceMarginUpperrBound;
    private LocalDateTime cycleAnchorDateOfMargining;
    private String cycleOfMargining;
    private double variationMargin;
    private String currency;
    private String currency2;
    private LocalDateTime amortizationDate;
    // private LocalDateTime contractDealDate;
    private LocalDateTime initialExchangeDate;
    private double premiumDiscountAtIED;
    private LocalDateTime maturityDate;
    private double notionalPrincipal;
    private double notionalPrincipal2;
    private double quantity;
    // private String unit;
    private double unitMultiple;
    private LocalDateTime cycleAnchorDateOfPrincipalRedemption;
    private LocalDateTime[] arrayCycleAnchorDateOfPrincipalRedemption;
    private String cycleOfPrincipalRedemption;
    private String[] arrayCycleOfPrincipalRedemption;
    private Double nextPrincipalRedemptionPayment;
    private Double[] arrayNextPrincipalRedemptionPayment;
    private String[] arrayIncreaseDecrease;
    private LocalDateTime purchaseDate;
    private double priceAtPurchaseDate;
    private LocalDateTime terminationDate;
    private double priceAtTerminationDate;
    private double prepaymentAmount; // TODO: review with prepayment concept
    private int prepaymentRule; // TODO: review with prepayment concept
    private String objectCodeOfPrepaymentModel; // TODO: review with prepayment concept
    private String xDayNotice;
    // private double remainingPrincipalDue;
    private String marketObjectCodeOfScalingIndex;
    private double scalingIndexAtStatusDate;
    private LocalDateTime cycleAnchorDateOfScalingIndex;
    private String cycleOfScalingIndex;
    private String scalingEffect;
    private Double marketValueObserved;
    // private double conversionFactor;
    private String optionExecutionType;
    private LocalDateTime optionExerciseEndDate; // TODO: review with prepayment concept
    private double optionStrike1;
    private double optionStrike2;
    // private String optionStrikeDriver;
    private String optionType;
    private LocalDateTime cycleAnchorDateOfOptionality; // TODO: review with prepayment concept
    private String cycleOfOptionality; // TODO: review with prepayment concept
    private int cycleTriggerOfOptionality; // TODO: review with prepayment concept
    private char penaltyType; // TODO: review with prepayment concept
    private double penaltyRate; // TODO: review with prepayment concept
    private double maximumPenaltyFreeDisbursement;
    private LocalDateTime cycleAnchorDateOfRateReset;
    private LocalDateTime[] arrayCycleAnchorDateOfRateReset;
    private String cycleOfRateReset;
    private String[] arrayCycleOfRateReset;
    private double rateSpread;
    private double[] arrayRate;
    private String[] arrayFixedVariable;
    private String marketObjectCodeOfRateReset;
    private Double lifeCap; // values can be null so use Double instead of double
    private Double lifeFloor; // values can be null so use Double instead of double
    private Double periodCap; // values can be null so use Double instead of double
    private Double periodFloor; // values can be null so use Double instead of double
    private String cyclePointOfRateReset;
    private String fixingDays;
    private double nextResetRate;
    private double rateMultiplier;
    // private String rateTerm; // TODO: review
    // private double yieldCurveCorrection; // TODO: review
    private LocalDateTime settlementDate;
    private String deliverySettlement;
    private double futuresPrice;
    
    public BusinessDayCalendarProvider calendar() {
        return calendar;
    }
    
    public BusinessDayAdjuster businessDayConvention() {
        return businessDayConvention;   
    }    
    
    public String endOfMonthConvention() {
        return endOfMonthConvention;
    }
    
    public String contractType() {
        return contractType;
    }
    
    public LocalDateTime statusDate() {
        return statusDate;
    }
    
    public String contractRole() {
        return contractRole;   
    }
    
    // public String legalEntityIDRecordCreator;
    // public String contractID;
    
    public String legalEntityIDCounterparty() {
        return legalEntityIDCounterparty;
    }
    
    // public String contractStatus;
    // public String seniority;
    // public LocalDateTime nonPerformingDate;
    
    public double guaranteedExposure() {
        return guaranteedExposure;   
    }
    
    public double coverageOfCreditEnhancement() {
        return coverageOfCreditEnhancement;   
    }
    
    public String minimumDelay() {
        return minimumDelay;   
    }
    
    // public String coveredContracts;
    // public String coveringContracts;
    
    public String coveredLegalEntity() {
        return coveredLegalEntity;   
    }
    
    public LocalDateTime cycleAnchorDateOfDividendPayment() {
        return cycleAnchorDateOfDividendPayment;   
    }
    
    public String cycleOfDividendPayment() {
        return cycleOfDividendPayment;   
    }
    
    public String marketObjectCodeOfDividendRate() {
        return marketObjectCodeOfDividendRate;   
    }
    
    public LocalDateTime cycleAnchorDateOfFee() {
        return cycleAnchorDateOfFee;   
    }
    
    public String cycleOfFee() {
        return cycleOfFee;   
    }
    
    public char feeBasis() {
        return feeBasis;   
    }
    
    public double feeRate() {
        return feeRate;   
    }
    
    public double feeAccrued() {
        return feeAccrued;   
    }
    
    public LocalDateTime cycleAnchorDateOfInterestPayment() {
        return cycleAnchorDateOfInterestPayment;   
    }
    
    public LocalDateTime[] arrayCycleAnchorDateOfInterestPayment() {
        return arrayCycleAnchorDateOfInterestPayment;   
    }
    
    public String cycleOfInterestPayment() {
        return cycleOfInterestPayment;   
    }
    
    public String[] arrayCycleOfInterestPayment() {
        return arrayCycleOfInterestPayment;   
    }
    
    public double nominalInterestRate() {
        return nominalInterestRate;   
    }
    
    public double nominalInterestRate2() {
        return nominalInterestRate2;   
    }
    
    public DayCountCalculator dayCountConvention() {
        return dayCountConvention;   
    }
    
    public double accruedInterest() {
        return accruedInterest;   
    }
    
    public LocalDateTime capitalizationEndDate() {
        return capitalizationEndDate;   
    }
    
    public LocalDateTime cycleAnchorDateOfInterestCalculationBase() {
        return cycleAnchorDateOfInterestCalculationBase;   
    }
    
    public String cycleOfInterestCalculationBase() {
        return cycleOfInterestCalculationBase;   
    }
    
    public String interestCalculationBase() {
        return interestCalculationBase;   
    }
    
    public double interestCalculationBaseAmount() {
        return interestCalculationBaseAmount;   
    }
    
    public String cyclePointOfInterestPayment() {
        return cyclePointOfInterestPayment;   
    }
    
    // public double deferredInterest;
    // public double maximumDeferredInterest;
    
    public String clearingHouse() {
        return clearingHouse;   
    }
    
    public double initialMargin() {
        return initialMargin;   
    }
    
    public double maintenanceMarginLowerBound() {
        return maintenanceMarginLowerBound;   
    }
    
    public double maintenanceMarginUpperrBound() {
        return maintenanceMarginUpperrBound;   
    }
    
    public LocalDateTime cycleAnchorDateOfMargining() {
        return cycleAnchorDateOfMargining;   
    }
    
    public String cycleOfMargining() {
        return cycleOfMargining;   
    }
    
    public double variationMargin() {
        return variationMargin;   
    }
    
    public String currency() {
        return currency;   
    }
    
    public String currency2() {
        return currency2;   
    }
    
    public LocalDateTime amortizationDate() {
        return amortizationDate;   
    }
    
    // public LocalDateTime contractDealDate;
    
    public LocalDateTime initialExchangeDate() {
        return initialExchangeDate;   
    }
    
    public double premiumDiscountAtIED() {
        return premiumDiscountAtIED;   
    }
    
    public LocalDateTime maturityDate() {
        return maturityDate;   
    }
    
    public double notionalPrincipal() {
        return notionalPrincipal;   
    }
    
    public double notionalPrincipal2() {
        return notionalPrincipal2;   
    }
    
    public double quantity() {
        return quantity;   
    }
    
    // public String unit;
    
    public double unitMultiple() {
        return unitMultiple;   
    }
    
    public LocalDateTime cycleAnchorDateOfPrincipalRedemption() {
        return cycleAnchorDateOfPrincipalRedemption;   
    }
    
    public LocalDateTime[] arrayCycleAnchorDateOfPrincipalRedemption() {
        return arrayCycleAnchorDateOfPrincipalRedemption;   
    }
    
    public String cycleOfPrincipalRedemption() {
        return cycleOfPrincipalRedemption;   
    }
    
    public String[] arrayCycleOfPrincipalRedemption() {
        return arrayCycleOfPrincipalRedemption;   
    }
    
    public Double nextPrincipalRedemptionPayment() {
        return nextPrincipalRedemptionPayment;   
    }
    
    public Double[] arrayNextPrincipalRedemptionPayment() {
        return arrayNextPrincipalRedemptionPayment;   
    }
    
    public String[] arrayIncreaseDecrease() {
        return arrayIncreaseDecrease;   
    }
    
    public LocalDateTime purchaseDate() {
        return purchaseDate;   
    }
    
    public double priceAtPurchaseDate() {
        return priceAtPurchaseDate;   
    }
    
    public LocalDateTime terminationDate() {
        return terminationDate;   
    }
    
    public double priceAtTerminationDate() {
        return priceAtTerminationDate;   
    }
    
    public double prepaymentAmount() { // TODO: review with prepayment concept
        return prepaymentAmount;
    }
    
    public int prepaymentRule() { // TODO: review with prepayment concept
        return prepaymentRule;
    }
    
    public String objectCodeOfPrepaymentModel() { // TODO: review with prepayment concept
        return objectCodeOfPrepaymentModel;
    }
    
    public String xDayNotice() {
        return xDayNotice;   
    }
    
    // public double remainingPrincipalDue;
    
    public String marketObjectCodeOfScalingIndex() {
        return marketObjectCodeOfScalingIndex;   
    }
    
    public double scalingIndexAtStatusDate() {
        return scalingIndexAtStatusDate;   
    }
    
    public LocalDateTime cycleAnchorDateOfScalingIndex() {
        return cycleAnchorDateOfScalingIndex;   
    }
    
    public String cycleOfScalingIndex() {
        return cycleOfScalingIndex;   
    }
    
    public String scalingEffect() {
        return scalingEffect;   
    }
    
    public Double marketValueObserved() {
        return marketValueObserved;
    }
    
    // public double conversionFactor();
    
    public String optionExecutionType() {
        return optionExecutionType;
    }
    
    public LocalDateTime optionExerciseEndDate() { // TODO: review with prepayment concept
        return optionExerciseEndDate;
    }
    
    public double optionStrike1() {
        return optionStrike1;   
    }
    
    public double optionStrike2() {
        return optionStrike2;   
    }
    
    // public String optionStrikeDriver;
    
    public String optionType() {
        return optionType;   
    }
    
    public LocalDateTime cycleAnchorDateOfOptionality() { // TODO: review with prepayment concept
        return cycleAnchorDateOfOptionality;
    }
    
    public String cycleOfOptionality() { // TODO: review with prepayment concept
        return cycleOfOptionality;
    }
    
    public int cycleTriggerOfOptionality() { // TODO: review with prepayment concept
        return cycleTriggerOfOptionality;
    }
    
    public char penaltyType() { // TODO: review with prepayment concept
        return penaltyType;
    }
    
    public double penaltyRate() { // TODO: review with prepayment concept
        return penaltyRate;
    }
    
    public double maximumPenaltyFreeDisbursement() {
        return maximumPenaltyFreeDisbursement;   
    }
    
    public LocalDateTime cycleAnchorDateOfRateReset() {
        return cycleAnchorDateOfRateReset;   
    }
    
    public LocalDateTime[] arrayCycleAnchorDateOfRateReset() {
        return arrayCycleAnchorDateOfRateReset;   
    }
    
    public String cycleOfRateReset() {
        return cycleOfRateReset;   
    }
    
    public String[] arrayCycleOfRateReset() {
        return arrayCycleOfRateReset;   
    }
    
    public double rateSpread() {
        return rateSpread;   
    }
    
    public double[] arrayRate() {
        return arrayRate;   
    }
    
    public String[] arrayFixedVariable() {
        return arrayFixedVariable;   
    }
    
    public String marketObjectCodeOfRateReset() {
        return marketObjectCodeOfRateReset;   
    }
    
    public Double lifeCap() { // values can be null so use Double instead of double
        return lifeCap;
    }
    
    public Double lifeFloor() { // values can be null so use Double instead of double
        return lifeFloor;
    }
    
    public Double periodCap() { // values can be null so use Double instead of double
        return periodCap;
    }
    
    public Double periodFloor() { // values can be null so use Double instead of double
        return periodFloor;
    }
    
    public String cyclePointOfRateReset() {
        return cyclePointOfRateReset;   
    }
    
    public String fixingDays() {
        return fixingDays;   
    }
    
    public double nextResetRate() {
        return nextResetRate;   
    }
    
    public double rateMultiplier() {
        return rateMultiplier;   
    }
    
    // public String rateTerm; // TODO: review
    // public double yieldCurveCorrection; // TODO: review
    
    public LocalDateTime settlementDate() {
        return settlementDate;   
    }
    
    public String deliverySettlement() {
        return deliverySettlement;   
    }
    
    public double futuresPrice() {
        return futuresPrice;   
    }
    
    /**
     * Parse the attributes from external String-representation to internal, attribute-specific data types
     * <p>
     * For the {@link ContratType} indicated in attribute "ContractType" the method goes through the list
     * of supported attributes and tries to parse these to their respective data type as indicated in the 
     * ACTUS data dictionary ({@linktourl http://www.projectactus.org/projectactus/?page_id=356}). 
     * <p>
     * For all attributes mandatory to a certain "ContractType" the method expects a not-{@code null} return value
     * of method {@code get} of the {@code Map<String,String>} method parameter. For non-mandatory attributes, a
     * {@code null} return value is allowed and treated as that the attribute is not specified. Some attributes may
     * be mandatory conditional to the value of other attributes. Be referred to the ACTUS data dictionary
     * for details.
     * 
     * @param attributes an external, raw (String) data representation of the set of attributes
     * @throws AttributeConversionException if an attribute cannot be parsed to its data type
     */
    public static ContractModel parse(Map<String,String> attributes) {
        ContractModel model = new ContractModel();
        
        // parse all attributes known to the respective contract type
        try{
        switch(attributes.get("ContractType")) {
            case StringUtils.ContractType_PAM:
                model.calendar = (attributes.get("Calendar").equals("MondayToFriday"))? new MondayToFridayCalendar() : new NoHolidaysCalendar();
                model.businessDayConvention = new BusinessDayAdjuster(attributes.get("BusinessDayConvention"), model.calendar);
                model.endOfMonthConvention = (CommonUtils.isNull(attributes.get("EndOfMonthConvention")))? "SD" : attributes.get("EndOfMonthConvention");
                model.contractType = attributes.get("ContractType");
                model.statusDate = LocalDateTime.parse(attributes.get("StatusDate"));
                model.contractRole = attributes.get("ContractRole");
                model.legalEntityIDCounterparty = attributes.get("LegalEntityIDCounterparty");
                model.cycleAnchorDateOfFee = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfFee")))? ( (CommonUtils.isNull(attributes.get("CycleOfFee")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfFee"));
                model.cycleOfFee = attributes.get("CycleOfFee");
                model.feeBasis = (CommonUtils.isNull(attributes.get("FeeBasis")))? '0' : attributes.get("FeeBasis").charAt(0);
                model.feeRate = (CommonUtils.isNull(attributes.get("FeeRate")))? 0.0 : Double.parseDouble(attributes.get("FeeRate"));
                model.feeAccrued = (CommonUtils.isNull(attributes.get("FeeAccrued")))? 0.0 : Double.parseDouble(attributes.get("FeeAccrued"));
                model.cycleAnchorDateOfInterestPayment = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestPayment")))? ( (CommonUtils.isNull(attributes.get("CycleOfInterestPayment")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfInterestPayment"));
                model.cycleOfInterestPayment = attributes.get("CycleOfInterestPayment");
                model.nominalInterestRate = (CommonUtils.isNull(attributes.get("NominalInterestRate")))? 0.0 : Double.parseDouble(attributes.get("NominalInterestRate"));                
                model.dayCountConvention = new DayCountCalculator(attributes.get("DayCountConvention"), model.calendar);
                model.accruedInterest = (CommonUtils.isNull(attributes.get("AccruedInterest")))? 0.0 : Double.parseDouble(attributes.get("AccruedInterest"));  
                model.capitalizationEndDate = (CommonUtils.isNull(attributes.get("CapitalizationEndDate")))? null : LocalDateTime.parse(attributes.get("CapitalizationEndDate"));
                model.cyclePointOfInterestPayment = attributes.get("CyclePointOfInterestPayment");
                model.currency = attributes.get("Currency");
                model.initialExchangeDate = LocalDateTime.parse(attributes.get("InitialExchangeDate"));
                model.premiumDiscountAtIED = (CommonUtils.isNull(attributes.get("PremiumDiscountAtIED")))? 0.0 : Double.parseDouble(attributes.get("PremiumDiscountAtIED")); 
                model.maturityDate = LocalDateTime.parse(attributes.get("MaturityDate"));
                model.notionalPrincipal = Double.parseDouble(attributes.get("NotionalPrincipal")); 
                model.purchaseDate = (CommonUtils.isNull(attributes.get("PurchaseDate")))? null : LocalDateTime.parse(attributes.get("PurchaseDate"));
                model.priceAtPurchaseDate = (CommonUtils.isNull(attributes.get("PriceAtPurchaseDate")))? 0.0 : Double.parseDouble(attributes.get("PriceAtPurchaseDate")); 
                model.terminationDate = (CommonUtils.isNull(attributes.get("TerminationDate")))? null : LocalDateTime.parse(attributes.get("TerminationDate"));
                model.priceAtTerminationDate = (CommonUtils.isNull(attributes.get("PriceAtTerminationDate")))? 0.0 : Double.parseDouble(attributes.get("PriceAtTerminationDate")); 
                model.marketObjectCodeOfScalingIndex = attributes.get("MarketObjectCodeOfScalingIndex");
                model.scalingIndexAtStatusDate = (CommonUtils.isNull(attributes.get("ScalingIndexAtStatusDate")))? 0.0 : Double.parseDouble(attributes.get("ScalingIndexAtStatusDate"));
                model.cycleAnchorDateOfScalingIndex = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfScalingIndex")))? ( (CommonUtils.isNull(attributes.get("CycleOfScalingIndex")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfScalingIndex"));
                model.cycleOfScalingIndex = attributes.get("CycleOfScalingIndex");
                model.scalingEffect = attributes.get("ScalingEffect");
                // TODO: review prepayment mechanism and attributes
                model.cycleAnchorDateOfOptionality = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfOptionality")))? ( (CommonUtils.isNull(attributes.get("CycleOfOptionality")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfOptionality"));
                model.cycleOfOptionality = attributes.get("CycleOfOptionality");
                model.penaltyType = (CommonUtils.isNull(attributes.get("PenaltyType")))? 'N' : attributes.get("PenaltyType").charAt(0);
                model.penaltyRate = (CommonUtils.isNull(attributes.get("PenaltyRate")))? 0.0 : Double.parseDouble(attributes.get("PenaltyRate"));
                model.objectCodeOfPrepaymentModel = attributes.get("ObjectCodeOfPrepaymentModel");
                
                model.cycleAnchorDateOfRateReset = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfRateReset")))? ( (CommonUtils.isNull(attributes.get("CycleOfRateReset")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfRateReset"));
                model.cycleOfRateReset = attributes.get("CycleOfRateReset");
                model.rateSpread = (CommonUtils.isNull(attributes.get("RateSpread")))? 0.0 : Double.parseDouble(attributes.get("RateSpread"));
                model.marketObjectCodeOfRateReset = attributes.get("MarketObjectCodeOfRateReset");
                model.lifeCap = (CommonUtils.isNull(attributes.get("LifeCap")))? null : Double.parseDouble(attributes.get("LifeCap"));
                model.lifeCap = (CommonUtils.isNull(attributes.get("LifeFloor")))? null : Double.parseDouble(attributes.get("LifeFloor"));
                model.lifeCap = (CommonUtils.isNull(attributes.get("PeriodCap")))? null : Double.parseDouble(attributes.get("PeriodCap"));
                model.lifeCap = (CommonUtils.isNull(attributes.get("PeriodFloor")))? null : Double.parseDouble(attributes.get("PeriodFloor"));
                model.cyclePointOfRateReset = attributes.get("CyclePointOfRateReset");
                model.fixingDays = attributes.get("FixingDays");
                model.nextResetRate = (CommonUtils.isNull(attributes.get("NextResetRate")))? 0.0 : Double.parseDouble(attributes.get("NextResetRate"));
                model.rateMultiplier = (CommonUtils.isNull(attributes.get("RateMultiplier")))? 0.0 : Double.parseDouble(attributes.get("RateMultiplier"));
                // rateTerm = attributes.get("RateTerm"); // has been removed from the DD, check and remove here
                break;
            case StringUtils.ContractType_LAM:
                model.calendar = (attributes.get("Calendar").equals("MondayToFriday"))? new MondayToFridayCalendar() : new NoHolidaysCalendar();
                model.businessDayConvention = new BusinessDayAdjuster(attributes.get("BusinessDayConvention"), model.calendar);
                model.endOfMonthConvention = (CommonUtils.isNull(attributes.get("EndOfMonthConvention")))? "SD" : attributes.get("EndOfMonthConvention");
                model.contractType = attributes.get("ContractType");
                model.statusDate = LocalDateTime.parse(attributes.get("StatusDate"));
                model.contractRole = attributes.get("ContractRole");
                model.legalEntityIDCounterparty = attributes.get("LegalEntityIDCounterparty");
                model.cycleAnchorDateOfFee = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfFee")))? ( (CommonUtils.isNull(attributes.get("CycleOfFee")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfFee"));
                model.cycleOfFee = attributes.get("CycleOfFee");
                model.feeBasis = (CommonUtils.isNull(attributes.get("FeeBasis")))? '0' : attributes.get("FeeBasis").charAt(0);
                model.feeRate = (CommonUtils.isNull(attributes.get("FeeRate")))? 0.0 : Double.parseDouble(attributes.get("FeeRate"));
                model.feeAccrued = (CommonUtils.isNull(attributes.get("FeeAccrued")))? 0.0 : Double.parseDouble(attributes.get("FeeAccrued"));
                model.cycleAnchorDateOfInterestPayment = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestPayment")))? ( (CommonUtils.isNull(attributes.get("CycleOfInterestPayment")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfInterestPayment"));
                model.cycleOfInterestPayment = attributes.get("CycleOfInterestPayment");
                model.nominalInterestRate = (CommonUtils.isNull(attributes.get("NominalInterestRate")))? 0.0 : Double.parseDouble(attributes.get("NominalInterestRate"));                
                model.dayCountConvention = new DayCountCalculator(attributes.get("DayCountConvention"), model.calendar);
                model.accruedInterest = (CommonUtils.isNull(attributes.get("AccruedInterest")))? 0.0 : Double.parseDouble(attributes.get("AccruedInterest"));  
                model.capitalizationEndDate = (CommonUtils.isNull(attributes.get("CapitalizationEndDate")))? null : LocalDateTime.parse(attributes.get("CapitalizationEndDate"));
                
                model.cycleAnchorDateOfInterestCalculationBase = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestCalculationBase")))? ( (CommonUtils.isNull(attributes.get("CycleOfInterestCalculationBase")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfInterestCalculationBase"));
                model.cycleOfInterestCalculationBase = attributes.get("CycleOfInterestCalculationBase");
                model.interestCalculationBase = attributes.get("InterestCalculationBase");
                model.interestCalculationBaseAmount = (CommonUtils.isNull(attributes.get("InterestCalculationBaseAmount")))? 0.0 : Double.parseDouble(attributes.get("InterestCalculationBaseAmount"));
                
                model.cyclePointOfInterestPayment = attributes.get("CyclePointOfInterestPayment");
                model.currency = attributes.get("Currency");
                model.initialExchangeDate = LocalDateTime.parse(attributes.get("InitialExchangeDate"));
                model.premiumDiscountAtIED = (CommonUtils.isNull(attributes.get("PremiumDiscountAtIED")))? 0.0 : Double.parseDouble(attributes.get("PremiumDiscountAtIED")); 
                model.maturityDate = (CommonUtils.isNull(attributes.get("MaturityDate")))? null : LocalDateTime.parse(attributes.get("MaturityDate"));
                model.notionalPrincipal = Double.parseDouble(attributes.get("NotionalPrincipal")); 
                
                model.cycleAnchorDateOfPrincipalRedemption = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfPrincipalRedemption")))? LocalDateTime.parse(attributes.get("InitialExchangeDate")) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfPrincipalRedemption"));
                model.cycleOfPrincipalRedemption = attributes.get("CycleOfPrincipalRedemption");
                model.nextPrincipalRedemptionPayment = (CommonUtils.isNull(attributes.get("NextPrincipalRedemptionPayment")))? null : Double.parseDouble(attributes.get("NextPrincipalRedemptionPayment"));
                
                model.purchaseDate = (CommonUtils.isNull(attributes.get("PurchaseDate")))? null : LocalDateTime.parse(attributes.get("PurchaseDate"));
                model.priceAtPurchaseDate = (CommonUtils.isNull(attributes.get("PriceAtPurchaseDate")))? 0.0 : Double.parseDouble(attributes.get("PriceAtPurchaseDate")); 
                model.terminationDate = (CommonUtils.isNull(attributes.get("TerminationDate")))? null : LocalDateTime.parse(attributes.get("TerminationDate"));
                model.priceAtTerminationDate = (CommonUtils.isNull(attributes.get("PriceAtTerminationDate")))? 0.0 : Double.parseDouble(attributes.get("PriceAtTerminationDate")); 
                model.marketObjectCodeOfScalingIndex = attributes.get("MarketObjectCodeOfScalingIndex");
                model.scalingIndexAtStatusDate = (CommonUtils.isNull(attributes.get("ScalingIndexAtStatusDate")))? 0.0 : Double.parseDouble(attributes.get("ScalingIndexAtStatusDate"));
                model.cycleAnchorDateOfScalingIndex = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfScalingIndex")))? ( (CommonUtils.isNull(attributes.get("CycleOfScalingIndex")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfScalingIndex"));
                model.cycleOfScalingIndex = attributes.get("CycleOfScalingIndex");
                model.scalingEffect = attributes.get("ScalingEffect");
                // TODO: review prepayment mechanism and attributes
                model.cycleAnchorDateOfOptionality = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfOptionality")))? ( (CommonUtils.isNull(attributes.get("CycleOfOptionality")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfOptionality"));
                model.cycleOfOptionality = attributes.get("CycleOfOptionality");
                model.penaltyType = (CommonUtils.isNull(attributes.get("PenaltyType")))? 'N' : attributes.get("PenaltyType").charAt(0);
                model.penaltyRate = (CommonUtils.isNull(attributes.get("PenaltyRate")))? 0.0 : Double.parseDouble(attributes.get("PenaltyRate"));
                model.objectCodeOfPrepaymentModel = attributes.get("ObjectCodeOfPrepaymentModel");
                
                model.cycleAnchorDateOfRateReset = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfRateReset")))? ( (CommonUtils.isNull(attributes.get("CycleOfRateReset")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfRateReset"));
                model.cycleOfRateReset = attributes.get("CycleOfRateReset");
                model.rateSpread = (CommonUtils.isNull(attributes.get("RateSpread")))? 0.0 : Double.parseDouble(attributes.get("RateSpread"));
                model.marketObjectCodeOfRateReset = attributes.get("MarketObjectCodeOfRateReset");
                model.lifeCap = (CommonUtils.isNull(attributes.get("LifeCap")))? null : Double.parseDouble(attributes.get("LifeCap"));
                model.lifeCap = (CommonUtils.isNull(attributes.get("LifeFloor")))? null : Double.parseDouble(attributes.get("LifeFloor"));
                model.lifeCap = (CommonUtils.isNull(attributes.get("PeriodCap")))? null : Double.parseDouble(attributes.get("PeriodCap"));
                model.lifeCap = (CommonUtils.isNull(attributes.get("PeriodFloor")))? null : Double.parseDouble(attributes.get("PeriodFloor"));
                model.cyclePointOfRateReset = attributes.get("CyclePointOfRateReset");
                model.fixingDays = attributes.get("FixingDays");
                model.nextResetRate = (CommonUtils.isNull(attributes.get("NextResetRate")))? 0.0 : Double.parseDouble(attributes.get("NextResetRate"));
                model.rateMultiplier = (CommonUtils.isNull(attributes.get("RateMultiplier")))? 0.0 : Double.parseDouble(attributes.get("RateMultiplier"));
                break;
            case StringUtils.ContractType_NAM:
                model.calendar = (attributes.get("Calendar").equals("MondayToFriday"))? new MondayToFridayCalendar() : new NoHolidaysCalendar();
                model.businessDayConvention = new BusinessDayAdjuster(attributes.get("BusinessDayConvention"), model.calendar);
                model.endOfMonthConvention = (CommonUtils.isNull(attributes.get("EndOfMonthConvention")))? "SD" : attributes.get("EndOfMonthConvention");
                model.contractType = attributes.get("ContractType");
                model.statusDate = LocalDateTime.parse(attributes.get("StatusDate"));
                model.contractRole = attributes.get("ContractRole");
                model.legalEntityIDCounterparty = attributes.get("LegalEntityIDCounterparty");
                model.cycleAnchorDateOfFee = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfFee")))? ( (CommonUtils.isNull(attributes.get("CycleOfFee")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfFee"));
                model.cycleOfFee = attributes.get("CycleOfFee");
                model.feeBasis = (CommonUtils.isNull(attributes.get("FeeBasis")))? '0' : attributes.get("FeeBasis").charAt(0);
                model.feeRate = (CommonUtils.isNull(attributes.get("FeeRate")))? 0.0 : Double.parseDouble(attributes.get("FeeRate"));
                model.feeAccrued = (CommonUtils.isNull(attributes.get("FeeAccrued")))? 0.0 : Double.parseDouble(attributes.get("FeeAccrued"));
                model.cycleAnchorDateOfInterestPayment = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestPayment")))? ( (CommonUtils.isNull(attributes.get("CycleOfInterestPayment")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfInterestPayment"));
                model.cycleOfInterestPayment = attributes.get("CycleOfInterestPayment");
                model.nominalInterestRate = (CommonUtils.isNull(attributes.get("NominalInterestRate")))? 0.0 : Double.parseDouble(attributes.get("NominalInterestRate"));                
                model.dayCountConvention = new DayCountCalculator(attributes.get("DayCountConvention"), model.calendar);
                model.accruedInterest = (CommonUtils.isNull(attributes.get("AccruedInterest")))? 0.0 : Double.parseDouble(attributes.get("AccruedInterest"));  
                model.capitalizationEndDate = (CommonUtils.isNull(attributes.get("CapitalizationEndDate")))? null : LocalDateTime.parse(attributes.get("CapitalizationEndDate"));
                
                model.cycleAnchorDateOfInterestCalculationBase = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestCalculationBase")))? ( (CommonUtils.isNull(attributes.get("CycleOfInterestCalculationBase")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfInterestCalculationBase"));
                model.cycleOfInterestCalculationBase = attributes.get("CycleOfInterestCalculationBase");
                model.interestCalculationBase = attributes.get("InterestCalculationBase");
                model.interestCalculationBaseAmount = (CommonUtils.isNull(attributes.get("InterestCalculationBaseAmount")))? 0.0 : Double.parseDouble(attributes.get("InterestCalculationBaseAmount"));
                
                model.cyclePointOfInterestPayment = attributes.get("CyclePointOfInterestPayment");
                model.currency = attributes.get("Currency");
                model.initialExchangeDate = LocalDateTime.parse(attributes.get("InitialExchangeDate"));
                model.premiumDiscountAtIED = (CommonUtils.isNull(attributes.get("PremiumDiscountAtIED")))? 0.0 : Double.parseDouble(attributes.get("PremiumDiscountAtIED")); 
                model.maturityDate = (CommonUtils.isNull(attributes.get("MaturityDate")))? null : LocalDateTime.parse(attributes.get("MaturityDate"));
                model.notionalPrincipal = Double.parseDouble(attributes.get("NotionalPrincipal")); 
                
                model.cycleAnchorDateOfPrincipalRedemption = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfPrincipalRedemption")))? LocalDateTime.parse(attributes.get("InitialExchangeDate")) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfPrincipalRedemption"));
                model.cycleOfPrincipalRedemption = attributes.get("CycleOfPrincipalRedemption");
                model.nextPrincipalRedemptionPayment = Double.parseDouble(attributes.get("NextPrincipalRedemptionPayment"));
                
                model.purchaseDate = (CommonUtils.isNull(attributes.get("PurchaseDate")))? null : LocalDateTime.parse(attributes.get("PurchaseDate"));
                model.priceAtPurchaseDate = (CommonUtils.isNull(attributes.get("PriceAtPurchaseDate")))? 0.0 : Double.parseDouble(attributes.get("PriceAtPurchaseDate")); 
                model.terminationDate = (CommonUtils.isNull(attributes.get("TerminationDate")))? null : LocalDateTime.parse(attributes.get("TerminationDate"));
                model.priceAtTerminationDate = (CommonUtils.isNull(attributes.get("PriceAtTerminationDate")))? 0.0 : Double.parseDouble(attributes.get("PriceAtTerminationDate")); 
                model.marketObjectCodeOfScalingIndex = attributes.get("MarketObjectCodeOfScalingIndex");
                model.scalingIndexAtStatusDate = (CommonUtils.isNull(attributes.get("ScalingIndexAtStatusDate")))? 0.0 : Double.parseDouble(attributes.get("ScalingIndexAtStatusDate"));
                model.cycleAnchorDateOfScalingIndex = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfScalingIndex")))? ( (CommonUtils.isNull(attributes.get("CycleOfScalingIndex")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfScalingIndex"));
                model.cycleOfScalingIndex = attributes.get("CycleOfScalingIndex");
                model.scalingEffect = attributes.get("ScalingEffect");
                // TODO: review prepayment mechanism and attributes
                model.cycleAnchorDateOfOptionality = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfOptionality")))? ( (CommonUtils.isNull(attributes.get("CycleOfOptionality")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfOptionality"));
                model.cycleOfOptionality = attributes.get("CycleOfOptionality");
                model.penaltyType = (CommonUtils.isNull(attributes.get("PenaltyType")))? 'N' : attributes.get("PenaltyType").charAt(0);
                model.penaltyRate = (CommonUtils.isNull(attributes.get("PenaltyRate")))? 0.0 : Double.parseDouble(attributes.get("PenaltyRate"));
                model.objectCodeOfPrepaymentModel = attributes.get("ObjectCodeOfPrepaymentModel");
                
                model.cycleAnchorDateOfRateReset = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfRateReset")))? ( (CommonUtils.isNull(attributes.get("CycleOfRateReset")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfRateReset"));
                model.cycleOfRateReset = attributes.get("CycleOfRateReset");
                model.rateSpread = (CommonUtils.isNull(attributes.get("RateSpread")))? 0.0 : Double.parseDouble(attributes.get("RateSpread"));
                model.marketObjectCodeOfRateReset = attributes.get("MarketObjectCodeOfRateReset");
                model.lifeCap = (CommonUtils.isNull(attributes.get("LifeCap")))? null : Double.parseDouble(attributes.get("LifeCap"));
                model.lifeCap = (CommonUtils.isNull(attributes.get("LifeFloor")))? null : Double.parseDouble(attributes.get("LifeFloor"));
                model.lifeCap = (CommonUtils.isNull(attributes.get("PeriodCap")))? null : Double.parseDouble(attributes.get("PeriodCap"));
                model.lifeCap = (CommonUtils.isNull(attributes.get("PeriodFloor")))? null : Double.parseDouble(attributes.get("PeriodFloor"));
                model.cyclePointOfRateReset = attributes.get("CyclePointOfRateReset");
                model.fixingDays = attributes.get("FixingDays");
                model.nextResetRate = (CommonUtils.isNull(attributes.get("NextResetRate")))? 0.0 : Double.parseDouble(attributes.get("NextResetRate"));
                model.rateMultiplier = (CommonUtils.isNull(attributes.get("RateMultiplier")))? 0.0 : Double.parseDouble(attributes.get("RateMultiplier"));                
                break;
            case StringUtils.ContractType_ANN:
                model.calendar = (attributes.get("Calendar").equals("MondayToFriday"))? new MondayToFridayCalendar() : new NoHolidaysCalendar();
                model.businessDayConvention = new BusinessDayAdjuster(attributes.get("BusinessDayConvention"), model.calendar);
                model.endOfMonthConvention = (CommonUtils.isNull(attributes.get("EndOfMonthConvention")))? "SD" : attributes.get("EndOfMonthConvention");
                model.contractType = attributes.get("ContractType");
                model.statusDate = LocalDateTime.parse(attributes.get("StatusDate"));
                model.contractRole = attributes.get("ContractRole");
                model.legalEntityIDCounterparty = attributes.get("LegalEntityIDCounterparty");
                model.cycleAnchorDateOfFee = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfFee")))? ( (CommonUtils.isNull(attributes.get("CycleOfFee")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfFee"));
                model.cycleOfFee = attributes.get("CycleOfFee");
                model.feeBasis = (CommonUtils.isNull(attributes.get("FeeBasis")))? '0' : attributes.get("FeeBasis").charAt(0);
                model.feeRate = (CommonUtils.isNull(attributes.get("FeeRate")))? 0.0 : Double.parseDouble(attributes.get("FeeRate"));
                model.feeAccrued = (CommonUtils.isNull(attributes.get("FeeAccrued")))? 0.0 : Double.parseDouble(attributes.get("FeeAccrued"));
                model.cycleAnchorDateOfInterestPayment = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestPayment")))? ( (CommonUtils.isNull(attributes.get("CycleOfInterestPayment")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfInterestPayment"));
                model.cycleOfInterestPayment = attributes.get("CycleOfInterestPayment");
                model.nominalInterestRate = (CommonUtils.isNull(attributes.get("NominalInterestRate")))? 0.0 : Double.parseDouble(attributes.get("NominalInterestRate"));                
                model.dayCountConvention = new DayCountCalculator(attributes.get("DayCountConvention"), model.calendar);
                model.accruedInterest = (CommonUtils.isNull(attributes.get("AccruedInterest")))? 0.0 : Double.parseDouble(attributes.get("AccruedInterest"));  
                model.capitalizationEndDate = (CommonUtils.isNull(attributes.get("CapitalizationEndDate")))? null : LocalDateTime.parse(attributes.get("CapitalizationEndDate"));
                
                model.cycleAnchorDateOfInterestCalculationBase = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestCalculationBase")))? ( (CommonUtils.isNull(attributes.get("CycleOfInterestCalculationBase")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfInterestCalculationBase"));
                model.cycleOfInterestCalculationBase = attributes.get("CycleOfInterestCalculationBase");
                model.interestCalculationBase = attributes.get("InterestCalculationBase");
                model.interestCalculationBaseAmount = (CommonUtils.isNull(attributes.get("InterestCalculationBaseAmount")))? 0.0 : Double.parseDouble(attributes.get("InterestCalculationBaseAmount"));
                
                model.cyclePointOfInterestPayment = attributes.get("CyclePointOfInterestPayment");
                model.currency = attributes.get("Currency");
                model.initialExchangeDate = LocalDateTime.parse(attributes.get("InitialExchangeDate"));
                model.premiumDiscountAtIED = (CommonUtils.isNull(attributes.get("PremiumDiscountAtIED")))? 0.0 : Double.parseDouble(attributes.get("PremiumDiscountAtIED")); 
                model.maturityDate = (CommonUtils.isNull(attributes.get("MaturityDate")))? null : LocalDateTime.parse(attributes.get("MaturityDate"));
                model.notionalPrincipal = Double.parseDouble(attributes.get("NotionalPrincipal")); 
                
                model.cycleAnchorDateOfPrincipalRedemption = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfPrincipalRedemption")))? LocalDateTime.parse(attributes.get("InitialExchangeDate")) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfPrincipalRedemption"));
                model.cycleOfPrincipalRedemption = attributes.get("CycleOfPrincipalRedemption");
                model.nextPrincipalRedemptionPayment = (CommonUtils.isNull(attributes.get("NextPrincipalRedemptionPayment")))? null : Double.parseDouble(attributes.get("NextPrincipalRedemptionPayment"));
                
                model.purchaseDate = (CommonUtils.isNull(attributes.get("PurchaseDate")))? null : LocalDateTime.parse(attributes.get("PurchaseDate"));
                model.priceAtPurchaseDate = (CommonUtils.isNull(attributes.get("PriceAtPurchaseDate")))? 0.0 : Double.parseDouble(attributes.get("PriceAtPurchaseDate")); 
                model.terminationDate = (CommonUtils.isNull(attributes.get("TerminationDate")))? null : LocalDateTime.parse(attributes.get("TerminationDate"));
                model.priceAtTerminationDate = (CommonUtils.isNull(attributes.get("PriceAtTerminationDate")))? 0.0 : Double.parseDouble(attributes.get("PriceAtTerminationDate")); 
                model.marketObjectCodeOfScalingIndex = attributes.get("MarketObjectCodeOfScalingIndex");
                model.scalingIndexAtStatusDate = (CommonUtils.isNull(attributes.get("ScalingIndexAtStatusDate")))? 0.0 : Double.parseDouble(attributes.get("ScalingIndexAtStatusDate"));
                model.cycleAnchorDateOfScalingIndex = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfScalingIndex")))? ( (CommonUtils.isNull(attributes.get("CycleOfScalingIndex")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfScalingIndex"));
                model.cycleOfScalingIndex = attributes.get("CycleOfScalingIndex");
                model.scalingEffect = attributes.get("ScalingEffect");
                // TODO: review prepayment mechanism and attributes
                model.cycleAnchorDateOfOptionality = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfOptionality")))? ( (CommonUtils.isNull(attributes.get("CycleOfOptionality")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfOptionality"));
                model.cycleOfOptionality = attributes.get("CycleOfOptionality");
                model.penaltyType = (CommonUtils.isNull(attributes.get("PenaltyType")))? 'N' : attributes.get("PenaltyType").charAt(0);
                model.penaltyRate = (CommonUtils.isNull(attributes.get("PenaltyRate")))? 0.0 : Double.parseDouble(attributes.get("PenaltyRate"));
                model.objectCodeOfPrepaymentModel = attributes.get("ObjectCodeOfPrepaymentModel");
                
                model.cycleAnchorDateOfRateReset = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfRateReset")))? ( (CommonUtils.isNull(attributes.get("CycleOfRateReset")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfRateReset"));
                model.cycleOfRateReset = attributes.get("CycleOfRateReset");
                model.rateSpread = (CommonUtils.isNull(attributes.get("RateSpread")))? 0.0 : Double.parseDouble(attributes.get("RateSpread"));
                model.marketObjectCodeOfRateReset = attributes.get("MarketObjectCodeOfRateReset");
                model.lifeCap = (CommonUtils.isNull(attributes.get("LifeCap")))? null : Double.parseDouble(attributes.get("LifeCap"));
                model.lifeCap = (CommonUtils.isNull(attributes.get("LifeFloor")))? null : Double.parseDouble(attributes.get("LifeFloor"));
                model.lifeCap = (CommonUtils.isNull(attributes.get("PeriodCap")))? null : Double.parseDouble(attributes.get("PeriodCap"));
                model.lifeCap = (CommonUtils.isNull(attributes.get("PeriodFloor")))? null : Double.parseDouble(attributes.get("PeriodFloor"));
                model.cyclePointOfRateReset = attributes.get("CyclePointOfRateReset");
                model.fixingDays = attributes.get("FixingDays");
                model.nextResetRate = (CommonUtils.isNull(attributes.get("NextResetRate")))? 0.0 : Double.parseDouble(attributes.get("NextResetRate"));
                model.rateMultiplier = (CommonUtils.isNull(attributes.get("RateMultiplier")))? 0.0 : Double.parseDouble(attributes.get("RateMultiplier"));
                break;
                
            case StringUtils.ContractType_CLM:
            
                model.calendar = (CommonUtils.isNull(attributes.get("Calendar")))? new NoHolidaysCalendar() : (attributes.get("Calendar").equals("MondayToFriday"))? new MondayToFridayCalendar() : new NoHolidaysCalendar();
                model.businessDayConvention = new BusinessDayAdjuster(attributes.get("BusinessDayConvention"), model.calendar);
                model.endOfMonthConvention = (CommonUtils.isNull(attributes.get("EndOfMonthConvention")))? "SD" : attributes.get("EndOfMonthConvention");
                model.contractType = attributes.get("ContractType");
                model.statusDate = LocalDateTime.parse(attributes.get("StatusDate"));
                model.contractRole = attributes.get("ContractRole");
                model.legalEntityIDCounterparty = attributes.get("LegalEntityIDCounterparty");      
                model.cycleAnchorDateOfFee = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfFee")))? ( (CommonUtils.isNull(attributes.get("CycleOfFee")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfFee"));
                model.cycleOfFee = attributes.get("CycleOfFee");
                model.feeBasis = (CommonUtils.isNull(attributes.get("FeeBasis")))? '0' : attributes.get("FeeBasis").charAt(0);
                model.feeRate = (CommonUtils.isNull(attributes.get("FeeRate")))? 0.0 : Double.parseDouble(attributes.get("FeeRate"));
                model.feeAccrued = (CommonUtils.isNull(attributes.get("FeeAccrued")))? 0.0 : Double.parseDouble(attributes.get("FeeAccrued"));
                model.cycleAnchorDateOfInterestPayment = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestPayment")))? ( (CommonUtils.isNull(attributes.get("CycleOfInterestPayment")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfInterestPayment"));
                model.cycleOfInterestPayment = attributes.get("CycleOfInterestPayment");
                model.nominalInterestRate = Double.parseDouble(attributes.get("NominalInterestRate"));                
                model.dayCountConvention = new DayCountCalculator(attributes.get("DayCountConvention"), model.calendar);
                model.accruedInterest = (CommonUtils.isNull(attributes.get("AccruedInterest")))? 0.0 : Double.parseDouble(attributes.get("AccruedInterest"));  
                model.currency = attributes.get("Currency");
                model.initialExchangeDate = LocalDateTime.parse(attributes.get("InitialExchangeDate"));
                model.maturityDate = (CommonUtils.isNull(attributes.get("MaturityDate")))? null : LocalDateTime.parse(attributes.get("MaturityDate"));
                model.notionalPrincipal = Double.parseDouble(attributes.get("NotionalPrincipal")); 
                model.xDayNotice = attributes.get("XDayNotice");
                model.cycleAnchorDateOfRateReset = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfRateReset")))? ( (CommonUtils.isNull(attributes.get("CycleOfRateReset")))? null : LocalDateTime.parse(attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfRateReset"));
                model.cycleOfRateReset = attributes.get("CycleOfRateReset");
                model.rateSpread = (CommonUtils.isNull(attributes.get("RateSpread")))? 0.0 : Double.parseDouble(attributes.get("RateSpread"));
                model.marketObjectCodeOfRateReset = attributes.get("MarketObjectCodeOfRateReset");
                model.fixingDays = attributes.get("FixingDays");
                model.nextResetRate = (CommonUtils.isNull(attributes.get("NextResetRate")))? 0.0 : Double.parseDouble(attributes.get("NextResetRate"));
                model.rateMultiplier = (CommonUtils.isNull(attributes.get("RateMultiplier")))? 0.0 : Double.parseDouble(attributes.get("RateMultiplier"));
                break;
                
            case StringUtils.ContractType_CSH:
            
                model.contractType = attributes.get("ContractType");
                model.statusDate = LocalDateTime.parse(attributes.get("StatusDate"));
                model.contractRole = attributes.get("ContractRole");    
                model.currency = attributes.get("Currency");
                model.notionalPrincipal = Double.parseDouble(attributes.get("NotionalPrincipal"));
                break;
                
            case StringUtils.ContractType_STK:
            
                model.calendar = (attributes.get("Calendar").equals("MondayToFriday"))? new MondayToFridayCalendar() : new NoHolidaysCalendar();
                model.businessDayConvention = new BusinessDayAdjuster(attributes.get("BusinessDayConvention"), model.calendar);
                model.endOfMonthConvention = (CommonUtils.isNull(attributes.get("EndOfMonthConvention")))? "SD" : attributes.get("EndOfMonthConvention");
                model.contractType = attributes.get("ContractType");
                model.statusDate = LocalDateTime.parse(attributes.get("StatusDate"));
                model.contractRole = attributes.get("ContractRole");
                model.legalEntityIDCounterparty = attributes.get("LegalEntityIDCounterparty");      
                model.cycleAnchorDateOfDividendPayment = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfDividendPayment")))? ( (CommonUtils.isNull(attributes.get("CycleOfDividendPayment")))? null : LocalDateTime.parse(attributes.get("PurchaseDate")) ) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfDividendPayment"));
                model.cycleOfDividendPayment = attributes.get("CycleOfDividendPayment");
                model.marketObjectCodeOfDividendRate = attributes.get("MarketObjectCodeOfDividendRate");
                model.currency = attributes.get("Currency");
                model.quantity = (CommonUtils.isNull(attributes.get("Quantity")))? 1 : Integer.parseInt(attributes.get("Quantity"));
                model.marketValueObserved = (CommonUtils.isNull(attributes.get("MarketValueObserved")))? null : Double.parseDouble(attributes.get("MarketValueObserved"));
                model.purchaseDate = (CommonUtils.isNull(attributes.get("PurchaseDate")))? null : LocalDateTime.parse(attributes.get("PurchaseDate"));
                model.priceAtPurchaseDate = (CommonUtils.isNull(attributes.get("PriceAtPurchaseDate")))? 0.0 : Double.parseDouble(attributes.get("PriceAtPurchaseDate")); 
                model.terminationDate = (CommonUtils.isNull(attributes.get("TerminationDate")))? null : LocalDateTime.parse(attributes.get("TerminationDate"));
                model.priceAtTerminationDate = (CommonUtils.isNull(attributes.get("PriceAtTerminationDate")))? 0.0 : Double.parseDouble(attributes.get("PriceAtTerminationDate")); 
                model.dayCountConvention = new DayCountCalculator("A/AISDA", model.calendar);
               break;
               
            default:
                throw new ContractTypeUnknownException();
        }
        } catch(Exception e) {
            throw new AttributeConversionException();    
        }
        return model;
    }
}

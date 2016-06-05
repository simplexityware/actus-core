/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.attributes;

import org.actus.AttributeConversionException;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.util.StringUtils;
import org.actus.util.CommonUtils;

import java.time.LocalDateTime;

/**
 * Component that parses an external representation of attributes into an internal representation
 * <p>
 * Contract attributes refer to the terms of a contract, or, in other words, the pay-off function's
 * parameters. {@link AttributeProvider} provides the external representation of the set of 
 * attributes under which the pay-off is to be evaluated. Thereby, external representation refers
 * to the fact that an {@link AttributeProvider}-object carries the attributes in raw (String)-data
 * form, such that has been extracted e.g. from a data file. However, the raw data consists of 
 * various elements some referring to numerical, others to date, and again others to text variables. 
 * Thus, before the external representation can be evaluated in the pay-off function it has to be
 * "parsed" to the internal, {@link ContractModel} representation.
 * <p>
 * This class is a pure utility-class with only static methods and no public constructor.
 * 
 */
public final class AttributeParser {
    
    // private constructor as a pure utility-class
    private AttributeParser() {
    }
    
    /**
     * Parse the attributes from external String-representation to internal, attribute-specific data types
     * <p>
     * For the {@link ContratType} indicated in attribute "ContractType" the method goes through the list
     * of supported attributes and tries to parse these to their respective data type as indicated in the 
     * ACTUS data dictionary ({@linktourl http://www.projectactus.org/projectactus/?page_id=356}). 
     * <p>
     * For all attributes mandatory to a certain "ContractType" the method expects a not-{@code null} return value
     * of method {@code get} of the provided {@link AttributeProvider}. For non-mandatory attributes, a
     * {@code null} return value is treated as that the attribute is not specified. Some attributes may
     * be mandatory conditional to the value of other attributes. Be referred to the ACTUS data dictionary
     * for details.
     * 
     * @param attributes an external, raw (String) data representation of the set of attributes
     * @return a {@link ContractModel}-representation of the set of attributes
     * @throws AttributeConversionException if an attribute cannot be parsed to its data type
     */
    public static ContractModel parse(AttributeProvider attributes) {
        // init a new plain model
        ContractModel model = new ContractModel();
        
        // parse all attributes known to the respective contrac type
        switch(attributes.get("ContractType")) {
            case StringUtils.ContractType_PAM:
                model.calendar = attributes.get("Calendar");
                model.businessDayConvention = new BusinessDayAdjuster(attributes.get("BusinessDayConvention"), model.calendar);
                model.endOfMonthConvention = attributes.get("EndOfMonthConvention");
                model.statusDate = LocalDateTime.parse(attributes.get("StatusDate"));
                model.contractRole = attributes.get("ContractRole");
                model.contractRoleSign = ContractRoleConvention.roleSign(model.contractRole);
                model.legalEntityIDCounterparty = attributes.get("LegalEntityIDCounterparty");
                model.cycleAnchorDateOfFee = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfFee")))? null : LocalDateTime.parse(attributes.get("CycleAnchorDateOfFee"));
                model.cycleOfFee = attributes.get("CycleOfFee");
                model.feeBasis = (CommonUtils.isNull(attributes.get("FeeBasis")))? 0.0 : Double.parseDouble(attributes.get("FeeBasis"));
                model.feeRate = (CommonUtils.isNull(attributes.get("FeeRate")))? 0.0 : Double.parseDouble(attributes.get("FeeRate"));
                model.feeAccrued = (CommonUtils.isNull(attributes.get("FeeAccrued")))? 0.0 : Double.parseDouble(attributes.get("FeeAccrued"));
                model.cycleAnchorDateOfInterestPayment = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestPayment")))? null : LocalDateTime.parse(attributes.get("CycleAnchorDateOfInterestPayment"));
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
                model.cycleAnchorDateOfScalingIndex = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfScalingIndex")))? null : LocalDateTime.parse(attributes.get("CycleAnchorDateOfScalingIndex"));
                model.cycleOfScalingIndex = attributes.get("CycleOfScalingIndex");
                model.scalingEffect = attributes.get("ScalingEffect");
                model.cycleAnchorDateOfRateReset = (CommonUtils.isNull(attributes.get("CycleAnchorDateOfRateReset")))? null : LocalDateTime.parse(attributes.get("CycleAnchorDateOfRateReset"));
                model.cycleOfRateReset = attributes.get("CycleOfRateReset");
                model.rateSpread = (CommonUtils.isNull(attributes.get("RateSpread")))? 0.0 : Double.parseDouble(attributes.get("RateSpread"));
                model.marketObjectCodeRateReset = attributes.get("MarketObjectCodeRateReset");
                model.cyclePointOfRateReset = attributes.get("CyclePointOfRateReset");
                model.fixingDays = attributes.get("FixingDays");
                model.nextResetRate = (CommonUtils.isNull(attributes.get("NextResetRate")))? 0.0 : Double.parseDouble(attributes.get("NextResetRate"));
                model.rateMultiplier = (CommonUtils.isNull(attributes.get("RateMultiplier")))? 0.0 : Double.parseDouble(attributes.get("RateMultiplier"));
                model.rateTerm = attributes.get("RateTerm");
                break;
            default:
                throw new AttributeConversionException();
        }
        
        // return the model
        return model;
    }
}

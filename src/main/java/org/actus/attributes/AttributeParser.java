package org.actus.attributes;

import org.actus.AttributeConversionException;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.util.StringUtils;
import org.actus.util.CommonUtils;

import java.time.LocalDateTime;

public final class AttributeParser {
    
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

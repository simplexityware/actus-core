/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.attributes;

import org.actus.contracts.ContractType;
import org.actus.time.calendar.BusinessDayCalendarProvider;
import org.actus.time.calendar.NoHolidaysCalendar;
import org.actus.time.calendar.MondayToFridayCalendar;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.AttributeConversionException;
import org.actus.ContractTypeUnknownException;
import org.actus.types.*;
import org.actus.util.CommonUtils;
import org.actus.types.ContractReference;


import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

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
 * @see <a href="https://www.actusfrf.org/data-dictionary">ACTUS Data Dictionary</a>
 */
public class ContractModel implements ContractModelProvider {
    private Map<String, Object> attributes;

    /**
     * Constructor
     * <p>
     * The map provided as the constructor argument is expected to contain <key,value> pairs
     * of attributes using ACTUS attribute names (in long form) and data types of values
     * as per official ACTUS data dictionary.
     */
    public ContractModel(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * Create a new contract model from a java map
     * <p>
     * The map provided as the method argument is expected to contain <key,value> pairs
     * of attributes using ACTUS attribute names (in long form) and data types of values
     * as per official ACTUS data dictionary.
     *
     * @param attributes a java map of attributes as per ACTUS data dictionary
     * @return an instance of ContractModel containing the attributes provided with the method argument
     */
    public static ContractModel of(Map<String, Object> attributes) {
        return new ContractModel(attributes);
    }


    /**
     * Parse the attributes from external String-representation to internal, attribute-specific data types
     * <p>
     * For the {@link ContractType} indicated in attribute "ContractType" the method goes through the list
     * of supported attributes and tries to parse these to their respective data type as indicated in the
     * ACTUS data dictionary ({@linktourl https://www.actusfrf.org/data-dictionary}).
     * <p>
     * For all attributes mandatory to a certain "ContractType" the method expects a not-{@code null} return value
     * of method {@code get} of the {@code Map<String,String>} method parameter. For non-mandatory attributes, a
     * {@code null} return value is allowed and treated as that the attribute is not specified. Some attributes may
     * be mandatory conditional to the value of other attributes. Be referred to the ACTUS data dictionary
     * for details.
     *
     * @param attributes an external, raw (String) data representation of the set of attributes
     * @return an instance of ContractModel containing the attributes provided with the method argument
     * @throws AttributeConversionException if an attribute cannot be parsed to its data type
     */
    public static ContractModel parse(Map<String, Object> contractAttributes) {
        HashMap<String, Object> map = new HashMap<>();
        ContractTypeEnum contractType  = ContractTypeEnum.valueOf((String)contractAttributes.get("ContractType"));
        if(ContractTypeEnum.SWAPS.equals(contractType)){
            Map<String, Object> attributes = contractAttributes;
            // parse all attributes known to the respective contract type
            try {
                switch (ContractTypeEnum.valueOf((String)attributes.get("ContractType"))) {

                    case SWAPS:
                        // parse attributes (Swap) attributes
                        map.put("ContractID", attributes.get("ContractID"));
                        map.put("StatusDate", LocalDateTime.parse((String)attributes.get("StatusDate")));
                        map.put("ContractRole", ContractRole.valueOf((String)attributes.get("ContractRole")));
                        map.put("LegalEntityIDCounterparty", attributes.get("LegalEntityIDCounterparty"));
                        map.put("Currency", attributes.get("Currency"));
                        map.put("PurchaseDate", (CommonUtils.isNull(attributes.get("PurchaseDate"))) ? null : LocalDateTime.parse((String)attributes.get("PurchaseDate")));
                        map.put("PriceAtPurchaseDate", (CommonUtils.isNull(attributes.get("PriceAtPurchaseDate"))) ? 0.0 : Double.parseDouble((String)attributes.get("PriceAtPurchaseDate")));
                        map.put("TerminationDate", (CommonUtils.isNull(attributes.get("TerminationDate"))) ? null : LocalDateTime.parse((String)attributes.get("TerminationDate")));
                        map.put("PriceAtTerminationDate", (CommonUtils.isNull(attributes.get("PriceAtTerminationDate"))) ? 0.0 : Double.parseDouble((String)attributes.get("PriceAtTerminationDate")));
                        map.put("DeliverySettlement", attributes.get("DeliverySettlement"));

                        // parse child attributes
                        List<ContractReference> contractStructure = new ArrayList<>();
                        ((List<Map<String,Object>>)attributes.get("ContractStructure")).forEach(e->contractStructure.add(new ContractReference((Map<String,Object>)e, (ContractRole)map.get("ContractRole"))));
                        map.put("ContractStructure", contractStructure);

                        break;

                    default:
                        throw new ContractTypeUnknownException();
                }
            } catch (Exception e) {
                throw new AttributeConversionException();
            }
        } else{
            Map<String,String> attributes = contractAttributes.entrySet().stream().collect(Collectors.toMap(e->e.getKey(),e->e.getValue().toString()));
            // parse all attributes known to the respective contract type
            try {
                map.put("ContractID", attributes.get("ContractID"));
                switch (ContractTypeEnum.valueOf(attributes.get("ContractType"))) {
                    case PAM:
                        map.put("Calendar", (!CommonUtils.isNull(attributes.get("Calendar")) && attributes.get("Calendar").equals("MF")) ? new MondayToFridayCalendar() : new NoHolidaysCalendar());
                        map.put("BusinessDayConvention", new BusinessDayAdjuster(CommonUtils.isNull(attributes.get("BusinessDayConvention")) ? null : BusinessDayConventionEnum.valueOf(attributes.get("BusinessDayConvention")), (BusinessDayCalendarProvider) map.get("Calendar")));
                        map.put("EndOfMonthConvention", (CommonUtils.isNull(attributes.get("EndOfMonthConvention"))) ? EndOfMonthConventionEnum.SD : EndOfMonthConventionEnum.valueOf(attributes.get("EndOfMonthConvention")));
                        map.put("ContractType", ContractTypeEnum.valueOf(attributes.get("ContractType")));
                        map.put("StatusDate", LocalDateTime.parse(attributes.get("StatusDate")));
                        map.put("ContractRole", (!CommonUtils.isNull(attributes.get("ContractRole"))) ? ContractRole.valueOf(attributes.get("ContractRole")) : null);
                        map.put("LegalEntityIDCounterparty", attributes.get("LegalEntityIDCounterparty"));
                        map.put("CycleAnchorDateOfFee", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfFee"))) ? ((CommonUtils.isNull(attributes.get("CycleOfFee"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfFee")));
                        map.put("CycleOfFee", attributes.get("CycleOfFee"));
                        map.put("FeeBasis", (CommonUtils.isNull(attributes.get("FeeBasis"))) ? null : FeeBasis.valueOf(attributes.get("FeeBasis")));
                        map.put("FeeRate", (CommonUtils.isNull(attributes.get("FeeRate"))) ? 0.0 : Double.parseDouble(attributes.get("FeeRate")));
                        map.put("FeeAccrued", (CommonUtils.isNull(attributes.get("FeeAccrued"))) ? 0.0 : Double.parseDouble(attributes.get("FeeAccrued")));
                        map.put("CycleAnchorDateOfInterestPayment", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestPayment"))) ? ((CommonUtils.isNull(attributes.get("CycleOfInterestPayment"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfInterestPayment")));
                        map.put("CycleOfInterestPayment", attributes.get("CycleOfInterestPayment"));
                        map.put("NominalInterestRate", (CommonUtils.isNull(attributes.get("NominalInterestRate"))) ? 0.0 : Double.parseDouble(attributes.get("NominalInterestRate")));
                        map.put("DayCountConvention", new DayCountCalculator(attributes.get("DayCountConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                        map.put("AccruedInterest", (CommonUtils.isNull(attributes.get("AccruedInterest"))) ? 0.0 : Double.parseDouble(attributes.get("AccruedInterest")));
                        map.put("CapitalizationEndDate", (CommonUtils.isNull(attributes.get("CapitalizationEndDate"))) ? null : LocalDateTime.parse(attributes.get("CapitalizationEndDate")));
                        map.put("CyclePointOfInterestPayment", CommonUtils.isNull(attributes.get("CyclePointOfInterestPayment")) ? null : CyclePointOfInterestPayment.valueOf(attributes.get("CyclePointOfInterestPayment")));
                        map.put("Currency", attributes.get("Currency"));
                        map.put("InitialExchangeDate", LocalDateTime.parse(attributes.get("InitialExchangeDate")));
                        map.put("PremiumDiscountAtIED", (CommonUtils.isNull(attributes.get("PremiumDiscountAtIED"))) ? 0.0 : Double.parseDouble(attributes.get("PremiumDiscountAtIED")));
                        map.put("NotionalPrincipal", Double.parseDouble(attributes.get("NotionalPrincipal")));
                        map.put("PurchaseDate", (CommonUtils.isNull(attributes.get("PurchaseDate"))) ? null : LocalDateTime.parse(attributes.get("PurchaseDate")));
                        map.put("PriceAtPurchaseDate", (CommonUtils.isNull(attributes.get("PriceAtPurchaseDate"))) ? 0.0 : Double.parseDouble(attributes.get("PriceAtPurchaseDate")));
                        map.put("TerminationDate", (CommonUtils.isNull(attributes.get("TerminationDate"))) ? null : LocalDateTime.parse(attributes.get("TerminationDate")));
                        map.put("PriceAtTerminationDate", (CommonUtils.isNull(attributes.get("PriceAtTerminationDate"))) ? 0.0 : Double.parseDouble(attributes.get("PriceAtTerminationDate")));
                        map.put("MarketObjectCodeOfScalingIndex", attributes.get("MarketObjectCodeOfScalingIndex"));
                        map.put("ScalingIndexAtContractDealDate", (CommonUtils.isNull(attributes.get("ScalingIndexAtContractDealDate"))) ? 0.0 : Double.parseDouble(attributes.get("ScalingIndexAtContractDealDate")));
                        map.put("NotionalScalingMultiplier", (CommonUtils.isNull(attributes.get("NotionalScalingMultiplier"))) ? 1.0 : Double.parseDouble(attributes.get("NotionalScalingMultiplier")));
                        map.put("InterestScalingMultiplier", (CommonUtils.isNull(attributes.get("InterestScalingMultiplier"))) ? 1.0 : Double.parseDouble(attributes.get("InterestScalingMultiplier")));
                        map.put("CycleAnchorDateOfScalingIndex", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfScalingIndex"))) ? ((CommonUtils.isNull(attributes.get("CycleOfScalingIndex"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfScalingIndex")));
                        map.put("CycleOfScalingIndex", attributes.get("CycleOfScalingIndex"));
                        map.put("ScalingEffect", CommonUtils.isNull(attributes.get("ScalingEffect")) ? null : ScalingEffect.valueOf(attributes.get("ScalingEffect")));
                        // TODO: review prepayment mechanism and attributes
                        map.put("CycleAnchorDateOfOptionality", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfOptionality"))) ? ((CommonUtils.isNull(attributes.get("CycleOfOptionality"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfOptionality")));
                        map.put("CycleOfOptionality", attributes.get("CycleOfOptionality"));
                        map.put("PenaltyType", (CommonUtils.isNull(attributes.get("PenaltyType"))) ? PenaltyType.valueOf("N") : PenaltyType.valueOf(attributes.get("PenaltyType")));
                        map.put("PenaltyRate", (CommonUtils.isNull(attributes.get("PenaltyRate"))) ? 0.0 : Double.parseDouble(attributes.get("PenaltyRate")));
                        map.put("ObjectCodeOfPrepaymentModel", attributes.get("ObjectCodeOfPrepaymentModel"));
                        map.put("CycleAnchorDateOfRateReset", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfRateReset"))) ? ((CommonUtils.isNull(attributes.get("CycleOfRateReset"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfRateReset")));
                        map.put("CycleOfRateReset", attributes.get("CycleOfRateReset"));
                        map.put("RateSpread", (CommonUtils.isNull(attributes.get("RateSpread"))) ? 0.0 : Double.parseDouble(attributes.get("RateSpread")));
                        map.put("MarketObjectCodeOfRateReset", attributes.get("MarketObjectCodeOfRateReset"));
                        map.put("LifeCap", (CommonUtils.isNull(attributes.get("LifeCap"))) ? Double.POSITIVE_INFINITY : Double.parseDouble(attributes.get("LifeCap")));
                        map.put("LifeFloor", (CommonUtils.isNull(attributes.get("LifeFloor"))) ? Double.NEGATIVE_INFINITY : Double.parseDouble(attributes.get("LifeFloor")));
                        map.put("PeriodCap", (CommonUtils.isNull(attributes.get("PeriodCap"))) ? Double.POSITIVE_INFINITY : Double.parseDouble(attributes.get("PeriodCap")));
                        map.put("PeriodFloor", (CommonUtils.isNull(attributes.get("PeriodFloor"))) ? Double.POSITIVE_INFINITY : Double.parseDouble(attributes.get("PeriodFloor")));
                        map.put("CyclePointOfRateReset", CommonUtils.isNull(attributes.get("CyclePointOfRateReset")) ? null : CyclePointOfRateReset.valueOf(attributes.get("CyclePointOfRateReset")));
                        map.put("FixingDays", attributes.get("FixingDays"));
                        map.put("NextResetRate", (CommonUtils.isNull(attributes.get("NextResetRate"))) ? null : Double.parseDouble(attributes.get("NextResetRate")));
                        map.put("RateMultiplier", (CommonUtils.isNull(attributes.get("RateMultiplier"))) ? 0.0 : Double.parseDouble(attributes.get("RateMultiplier")));
                        map.put("MaturityDate", LocalDateTime.parse(attributes.get("MaturityDate")));

                        break; // nothing else to do for PAM
                    case LAM:
                        map.put("Calendar", (!CommonUtils.isNull(attributes.get("Calendar")) && attributes.get("Calendar").equals("MF")) ? new MondayToFridayCalendar() : new NoHolidaysCalendar());
                        map.put("BusinessDayConvention", new BusinessDayAdjuster(CommonUtils.isNull(attributes.get("BusinessDayConvention")) ? null : BusinessDayConventionEnum.valueOf(attributes.get("BusinessDayConvention")), (BusinessDayCalendarProvider) map.get("Calendar")));
                        map.put("EndOfMonthConvention", (CommonUtils.isNull(attributes.get("EndOfMonthConvention"))) ? EndOfMonthConventionEnum.SD : EndOfMonthConventionEnum.valueOf(attributes.get("EndOfMonthConvention")));
                        map.put("ContractType", ContractTypeEnum.valueOf(attributes.get("ContractType")));
                        map.put("StatusDate", LocalDateTime.parse(attributes.get("StatusDate")));
                        map.put("ContractRole", ContractRole.valueOf(attributes.get("ContractRole")));
                        map.put("LegalEntityIDCounterparty", attributes.get("LegalEntityIDCounterparty"));
                        map.put("CycleAnchorDateOfFee", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfFee"))) ? ((CommonUtils.isNull(attributes.get("CycleOfFee"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfFee")));
                        map.put("CycleOfFee", attributes.get("CycleOfFee"));
                        map.put("FeeBasis", (CommonUtils.isNull(attributes.get("FeeBasis"))) ? null : FeeBasis.valueOf(attributes.get("FeeBasis")));
                        map.put("FeeRate", (CommonUtils.isNull(attributes.get("FeeRate"))) ? 0.0 : Double.parseDouble(attributes.get("FeeRate")));
                        map.put("FeeAccrued", (CommonUtils.isNull(attributes.get("FeeAccrued"))) ? 0.0 : Double.parseDouble(attributes.get("FeeAccrued")));
                        map.put("CycleAnchorDateOfInterestPayment", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestPayment"))) ? ((CommonUtils.isNull(attributes.get("CycleOfInterestPayment"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfInterestPayment")));
                        map.put("CycleOfInterestPayment", attributes.get("CycleOfInterestPayment"));
                        map.put("NominalInterestRate", (CommonUtils.isNull(attributes.get("NominalInterestRate"))) ? 0.0 : Double.parseDouble(attributes.get("NominalInterestRate")));
                        map.put("DayCountConvention", new DayCountCalculator(attributes.get("DayCountConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                        map.put("AccruedInterest", (CommonUtils.isNull(attributes.get("AccruedInterest"))) ? 0.0 : Double.parseDouble(attributes.get("AccruedInterest")));
                        map.put("CapitalizationEndDate", (CommonUtils.isNull(attributes.get("CapitalizationEndDate"))) ? null : LocalDateTime.parse(attributes.get("CapitalizationEndDate")));
                        map.put("CyclePointOfRateReset", CommonUtils.isNull(attributes.get("CyclePointOfRateReset")) ? null : map.get("CyclePointOfInterestPayment") == CyclePointOfInterestPayment.B ? CyclePointOfRateReset.E : CyclePointOfRateReset.valueOf(attributes.get("CyclePointOfRateReset")));
                        map.put("Currency", attributes.get("Currency"));
                        map.put("InitialExchangeDate", LocalDateTime.parse(attributes.get("InitialExchangeDate")));
                        map.put("PremiumDiscountAtIED", (CommonUtils.isNull(attributes.get("PremiumDiscountAtIED"))) ? 0.0 : Double.parseDouble(attributes.get("PremiumDiscountAtIED")));
                        map.put("NotionalPrincipal", Double.parseDouble(attributes.get("NotionalPrincipal")));
                        map.put("PurchaseDate", (CommonUtils.isNull(attributes.get("PurchaseDate"))) ? null : LocalDateTime.parse(attributes.get("PurchaseDate")));
                        map.put("PriceAtPurchaseDate", (CommonUtils.isNull(attributes.get("PriceAtPurchaseDate"))) ? 0.0 : Double.parseDouble(attributes.get("PriceAtPurchaseDate")));
                        map.put("TerminationDate", (CommonUtils.isNull(attributes.get("TerminationDate"))) ? null : LocalDateTime.parse(attributes.get("TerminationDate")));
                        map.put("PriceAtTerminationDate", (CommonUtils.isNull(attributes.get("PriceAtTerminationDate"))) ? 0.0 : Double.parseDouble(attributes.get("PriceAtTerminationDate")));
                        map.put("MarketObjectCodeOfScalingIndex", attributes.get("MarketObjectCodeOfScalingIndex"));
                        map.put("ScalingIndexAtContractDealDate", (CommonUtils.isNull(attributes.get("ScalingIndexAtContractDealDate"))) ? 0.0 : Double.parseDouble(attributes.get("ScalingIndexAtContractDealDate")));
                        map.put("NotionalScalingMultiplier", (CommonUtils.isNull(attributes.get("NotionalScalingMultiplier"))) ? 1.0 : Double.parseDouble(attributes.get("NotionalScalingMultiplier")));
                        map.put("InterestScalingMultiplier", (CommonUtils.isNull(attributes.get("InterestScalingMultiplier"))) ? 1.0 : Double.parseDouble(attributes.get("InterestScalingMultiplier")));
                        map.put("CycleAnchorDateOfScalingIndex", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfScalingIndex"))) ? ((CommonUtils.isNull(attributes.get("CycleOfScalingIndex"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfScalingIndex")));
                        map.put("CycleOfScalingIndex", attributes.get("CycleOfScalingIndex"));
                        map.put("ScalingEffect", CommonUtils.isNull(attributes.get("ScalingEffect")) ? null : ScalingEffect.valueOf(attributes.get("ScalingEffect")));
                        // TODO: review prepayment mechanism and attributes
                        map.put("CycleAnchorDateOfOptionality", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfOptionality"))) ? ((CommonUtils.isNull(attributes.get("CycleOfOptionality"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfOptionality")));
                        map.put("CycleOfOptionality", attributes.get("CycleOfOptionality"));
                        map.put("PenaltyType", (CommonUtils.isNull(attributes.get("PenaltyType"))) ? PenaltyType.valueOf("N") : PenaltyType.valueOf(attributes.get("PenaltyType")));
                        map.put("PenaltyRate", (CommonUtils.isNull(attributes.get("PenaltyRate"))) ? 0.0 : Double.parseDouble(attributes.get("PenaltyRate")));
                        map.put("ObjectCodeOfPrepaymentModel", attributes.get("ObjectCodeOfPrepaymentModel"));
                        map.put("CycleAnchorDateOfRateReset", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfRateReset"))) ? ((CommonUtils.isNull(attributes.get("CycleOfRateReset"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfRateReset")));
                        map.put("CycleOfRateReset", attributes.get("CycleOfRateReset"));
                        map.put("RateSpread", (CommonUtils.isNull(attributes.get("RateSpread"))) ? 0.0 : Double.parseDouble(attributes.get("RateSpread")));
                        map.put("MarketObjectCodeOfRateReset", attributes.get("MarketObjectCodeOfRateReset"));
                        map.put("LifeCap", (CommonUtils.isNull(attributes.get("LifeCap"))) ? Double.POSITIVE_INFINITY : Double.parseDouble(attributes.get("LifeCap")));
                        map.put("LifeFloor", (CommonUtils.isNull(attributes.get("LifeFloor"))) ? Double.NEGATIVE_INFINITY : Double.parseDouble(attributes.get("LifeFloor")));
                        map.put("PeriodCap", (CommonUtils.isNull(attributes.get("PeriodCap"))) ? Double.POSITIVE_INFINITY : Double.parseDouble(attributes.get("PeriodCap")));
                        map.put("PeriodFloor", (CommonUtils.isNull(attributes.get("PeriodFloor"))) ? Double.POSITIVE_INFINITY : Double.parseDouble(attributes.get("PeriodFloor")));
                        map.put("FixingDays", attributes.get("FixingDays"));
                        map.put("NextResetRate", (CommonUtils.isNull(attributes.get("NextResetRate"))) ? null : Double.parseDouble(attributes.get("NextResetRate")));
                        map.put("RateMultiplier", (CommonUtils.isNull(attributes.get("RateMultiplier"))) ? 0.0 : Double.parseDouble(attributes.get("RateMultiplier")));
                        map.put("MaturityDate", (CommonUtils.isNull(attributes.get("MaturityDate")) ? null : LocalDateTime.parse(attributes.get("MaturityDate"))));
                        map.put("CycleAnchorDateOfInterestCalculationBase", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestCalculationBase"))) ? ((CommonUtils.isNull(attributes.get("CycleOfInterestCalculationBase"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfInterestCalculationBase")));
                        map.put("CycleOfInterestCalculationBase", attributes.get("CycleOfInterestCalculationBase"));
                        map.put("InterestCalculationBase", CommonUtils.isNull(attributes.get("InterestCalculationBase")) ? null : InterestCalculationBase.valueOf(attributes.get("InterestCalculationBase")));
                        map.put("InterestCalculationBaseAmount", (CommonUtils.isNull(attributes.get("InterestCalculationBaseAmount"))) ? 0.0 : Double.parseDouble(attributes.get("InterestCalculationBaseAmount")));
                        map.put("CycleAnchorDateOfPrincipalRedemption", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfPrincipalRedemption"))) ? LocalDateTime.parse(attributes.get("InitialExchangeDate")) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfPrincipalRedemption")));
                        map.put("CycleOfPrincipalRedemption", attributes.get("CycleOfPrincipalRedemption"));
                        map.put("NextPrincipalRedemptionPayment", (CommonUtils.isNull(attributes.get("NextPrincipalRedemptionPayment"))) ? null : Double.parseDouble(attributes.get("NextPrincipalRedemptionPayment")));

                    case NAM:
                        map.put("Calendar", (!CommonUtils.isNull(attributes.get("Calendar")) && attributes.get("Calendar").equals("MF")) ? new MondayToFridayCalendar() : new NoHolidaysCalendar());
                        map.put("BusinessDayConvention", new BusinessDayAdjuster(CommonUtils.isNull(attributes.get("BusinessDayConvention")) ? null : BusinessDayConventionEnum.valueOf(attributes.get("BusinessDayConvention")), (BusinessDayCalendarProvider) map.get("Calendar")));
                        map.put("EndOfMonthConvention", (CommonUtils.isNull(attributes.get("EndOfMonthConvention"))) ? EndOfMonthConventionEnum.SD : EndOfMonthConventionEnum.valueOf(attributes.get("EndOfMonthConvention")));
                        map.put("ContractType", ContractTypeEnum.valueOf(attributes.get("ContractType")));
                        map.put("StatusDate", LocalDateTime.parse(attributes.get("StatusDate")));
                        map.put("ContractRole", ContractRole.valueOf(attributes.get("ContractRole")));
                        map.put("LegalEntityIDCounterparty", attributes.get("LegalEntityIDCounterparty"));
                        map.put("CycleAnchorDateOfFee", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfFee"))) ? ((CommonUtils.isNull(attributes.get("CycleOfFee"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfFee")));
                        map.put("CycleOfFee", attributes.get("CycleOfFee"));
                        map.put("FeeBasis", (CommonUtils.isNull(attributes.get("FeeBasis"))) ? null : FeeBasis.valueOf(attributes.get("FeeBasis")));
                        map.put("FeeRate", (CommonUtils.isNull(attributes.get("FeeRate"))) ? 0.0 : Double.parseDouble(attributes.get("FeeRate")));
                        map.put("FeeAccrued", (CommonUtils.isNull(attributes.get("FeeAccrued"))) ? 0.0 : Double.parseDouble(attributes.get("FeeAccrued")));
                        map.put("CycleAnchorDateOfInterestPayment", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestPayment"))) ? ((CommonUtils.isNull(attributes.get("CycleOfInterestPayment"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfInterestPayment")));
                        map.put("CycleOfInterestPayment", attributes.get("CycleOfInterestPayment"));
                        map.put("NominalInterestRate", (CommonUtils.isNull(attributes.get("NominalInterestRate"))) ? 0.0 : Double.parseDouble(attributes.get("NominalInterestRate")));
                        map.put("DayCountConvention", new DayCountCalculator(attributes.get("DayCountConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                        map.put("AccruedInterest", (CommonUtils.isNull(attributes.get("AccruedInterest"))) ? 0.0 : Double.parseDouble(attributes.get("AccruedInterest")));
                        map.put("CapitalizationEndDate", (CommonUtils.isNull(attributes.get("CapitalizationEndDate"))) ? null : LocalDateTime.parse(attributes.get("CapitalizationEndDate")));
                        map.put("CyclePointOfRateReset", CommonUtils.isNull(attributes.get("CyclePointOfRateReset")) ? null : map.get("CyclePointOfInterestPayment") == CyclePointOfInterestPayment.B ? CyclePointOfRateReset.E : CyclePointOfRateReset.valueOf(attributes.get("CyclePointOfRateReset")));
                        map.put("Currency", attributes.get("Currency"));
                        map.put("InitialExchangeDate", LocalDateTime.parse(attributes.get("InitialExchangeDate")));
                        map.put("PremiumDiscountAtIED", (CommonUtils.isNull(attributes.get("PremiumDiscountAtIED"))) ? 0.0 : Double.parseDouble(attributes.get("PremiumDiscountAtIED")));
                        map.put("NotionalPrincipal", Double.parseDouble(attributes.get("NotionalPrincipal")));
                        map.put("PurchaseDate", (CommonUtils.isNull(attributes.get("PurchaseDate"))) ? null : LocalDateTime.parse(attributes.get("PurchaseDate")));
                        map.put("PriceAtPurchaseDate", (CommonUtils.isNull(attributes.get("PriceAtPurchaseDate"))) ? 0.0 : Double.parseDouble(attributes.get("PriceAtPurchaseDate")));
                        map.put("TerminationDate", (CommonUtils.isNull(attributes.get("TerminationDate"))) ? null : LocalDateTime.parse(attributes.get("TerminationDate")));
                        map.put("PriceAtTerminationDate", (CommonUtils.isNull(attributes.get("PriceAtTerminationDate"))) ? 0.0 : Double.parseDouble(attributes.get("PriceAtTerminationDate")));
                        map.put("MarketObjectCodeOfScalingIndex", attributes.get("MarketObjectCodeOfScalingIndex"));
                        map.put("ScalingIndexAtContractDealDate", (CommonUtils.isNull(attributes.get("ScalingIndexAtContractDealDate"))) ? 0.0 : Double.parseDouble(attributes.get("ScalingIndexAtContractDealDate")));
                        map.put("NotionalScalingMultiplier", (CommonUtils.isNull(attributes.get("NotionalScalingMultiplier"))) ? 1.0 : Double.parseDouble(attributes.get("NotionalScalingMultiplier")));
                        map.put("InterestScalingMultiplier", (CommonUtils.isNull(attributes.get("InterestScalingMultiplier"))) ? 1.0 : Double.parseDouble(attributes.get("InterestScalingMultiplier")));
                        map.put("CycleAnchorDateOfScalingIndex", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfScalingIndex"))) ? ((CommonUtils.isNull(attributes.get("CycleOfScalingIndex"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfScalingIndex")));
                        map.put("CycleOfScalingIndex", attributes.get("CycleOfScalingIndex"));
                        map.put("ScalingEffect", CommonUtils.isNull(attributes.get("ScalingEffect")) ? null : ScalingEffect.valueOf(attributes.get("ScalingEffect")));
                        // TODO: review prepayment mechanism and attributes
                        map.put("CycleAnchorDateOfOptionality", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfOptionality"))) ? ((CommonUtils.isNull(attributes.get("CycleOfOptionality"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfOptionality")));
                        map.put("CycleOfOptionality", attributes.get("CycleOfOptionality"));
                        map.put("PenaltyType", (CommonUtils.isNull(attributes.get("PenaltyType"))) ? PenaltyType.valueOf("N") : PenaltyType.valueOf(attributes.get("PenaltyType")));
                        map.put("PenaltyRate", (CommonUtils.isNull(attributes.get("PenaltyRate"))) ? 0.0 : Double.parseDouble(attributes.get("PenaltyRate")));
                        map.put("ObjectCodeOfPrepaymentModel", attributes.get("ObjectCodeOfPrepaymentModel"));
                        map.put("CycleAnchorDateOfRateReset", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfRateReset"))) ? ((CommonUtils.isNull(attributes.get("CycleOfRateReset"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfRateReset")));
                        map.put("CycleOfRateReset", attributes.get("CycleOfRateReset"));
                        map.put("RateSpread", (CommonUtils.isNull(attributes.get("RateSpread"))) ? 0.0 : Double.parseDouble(attributes.get("RateSpread")));
                        map.put("MarketObjectCodeOfRateReset", attributes.get("MarketObjectCodeOfRateReset"));
                        map.put("LifeCap", (CommonUtils.isNull(attributes.get("LifeCap"))) ? Double.POSITIVE_INFINITY : Double.parseDouble(attributes.get("LifeCap")));
                        map.put("LifeFloor", (CommonUtils.isNull(attributes.get("LifeFloor"))) ? Double.NEGATIVE_INFINITY : Double.parseDouble(attributes.get("LifeFloor")));
                        map.put("PeriodCap", (CommonUtils.isNull(attributes.get("PeriodCap"))) ? Double.POSITIVE_INFINITY : Double.parseDouble(attributes.get("PeriodCap")));
                        map.put("PeriodFloor", (CommonUtils.isNull(attributes.get("PeriodFloor"))) ? Double.POSITIVE_INFINITY : Double.parseDouble(attributes.get("PeriodFloor")));
                        map.put("FixingDays", attributes.get("FixingDays"));
                        map.put("NextResetRate", (CommonUtils.isNull(attributes.get("NextResetRate"))) ? null : Double.parseDouble(attributes.get("NextResetRate")));
                        map.put("RateMultiplier", (CommonUtils.isNull(attributes.get("RateMultiplier"))) ? 0.0 : Double.parseDouble(attributes.get("RateMultiplier")));

                        // present for LAM, NAM, ANN but not PAM
                        map.put("MaturityDate", (CommonUtils.isNull(attributes.get("MaturityDate")) ? null : LocalDateTime.parse(attributes.get("MaturityDate"))));
                        map.put("CycleAnchorDateOfInterestCalculationBase", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestCalculationBase"))) ? ((CommonUtils.isNull(attributes.get("CycleOfInterestCalculationBase"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfInterestCalculationBase")));
                        map.put("CycleOfInterestCalculationBase", attributes.get("CycleOfInterestCalculationBase"));
                        map.put("InterestCalculationBase", CommonUtils.isNull(attributes.get("InterestCalculationBase"))?null:InterestCalculationBase.valueOf(attributes.get("InterestCalculationBase")));
                        map.put("InterestCalculationBaseAmount", (CommonUtils.isNull(attributes.get("InterestCalculationBaseAmount"))) ? 0.0 : Double.parseDouble(attributes.get("InterestCalculationBaseAmount")));
                        map.put("CycleAnchorDateOfPrincipalRedemption", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfPrincipalRedemption"))) ? LocalDateTime.parse(attributes.get("InitialExchangeDate")) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfPrincipalRedemption")));
                        map.put("CycleOfPrincipalRedemption", attributes.get("CycleOfPrincipalRedemption"));
                        map.put("NextPrincipalRedemptionPayment", (CommonUtils.isNull(attributes.get("NextPrincipalRedemptionPayment"))) ? null : Double.parseDouble(attributes.get("NextPrincipalRedemptionPayment")));

                    case ANN: // almost identical with LAM, NAM, ANN
                        map.put("Calendar", (!CommonUtils.isNull(attributes.get("Calendar")) && attributes.get("Calendar").equals("MF")) ? new MondayToFridayCalendar() : new NoHolidaysCalendar());
                        map.put("BusinessDayConvention", new BusinessDayAdjuster(CommonUtils.isNull(attributes.get("BusinessDayConvention")) ? null : BusinessDayConventionEnum.valueOf(attributes.get("BusinessDayConvention")), (BusinessDayCalendarProvider) map.get("Calendar")));
                        map.put("EndOfMonthConvention", (CommonUtils.isNull(attributes.get("EndOfMonthConvention"))) ? EndOfMonthConventionEnum.SD : EndOfMonthConventionEnum.valueOf(attributes.get("EndOfMonthConvention")));
                        map.put("ContractType", ContractTypeEnum.valueOf(attributes.get("ContractType")));
                        map.put("StatusDate", LocalDateTime.parse(attributes.get("StatusDate")));
                        map.put("ContractRole", ContractRole.valueOf(attributes.get("ContractRole")));
                        map.put("LegalEntityIDCounterparty", attributes.get("LegalEntityIDCounterparty"));
                        map.put("CycleAnchorDateOfFee", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfFee"))) ? ((CommonUtils.isNull(attributes.get("CycleOfFee"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfFee")));
                        map.put("CycleOfFee", attributes.get("CycleOfFee"));
                        map.put("FeeBasis", (CommonUtils.isNull(attributes.get("FeeBasis"))) ? null : FeeBasis.valueOf(attributes.get("FeeBasis")));
                        map.put("FeeRate", (CommonUtils.isNull(attributes.get("FeeRate"))) ? 0.0 : Double.parseDouble(attributes.get("FeeRate")));
                        map.put("FeeAccrued", (CommonUtils.isNull(attributes.get("FeeAccrued"))) ? 0.0 : Double.parseDouble(attributes.get("FeeAccrued")));
                        map.put("CycleAnchorDateOfInterestPayment", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestPayment"))) ? ((CommonUtils.isNull(attributes.get("CycleOfInterestPayment"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfInterestPayment")));
                        map.put("CycleOfInterestPayment", attributes.get("CycleOfInterestPayment"));
                        map.put("NominalInterestRate", (CommonUtils.isNull(attributes.get("NominalInterestRate"))) ? 0.0 : Double.parseDouble(attributes.get("NominalInterestRate")));
                        map.put("DayCountConvention", new DayCountCalculator(attributes.get("DayCountConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                        map.put("AccruedInterest", (CommonUtils.isNull(attributes.get("AccruedInterest"))) ? 0.0 : Double.parseDouble(attributes.get("AccruedInterest")));
                        map.put("CapitalizationEndDate", (CommonUtils.isNull(attributes.get("CapitalizationEndDate"))) ? null : LocalDateTime.parse(attributes.get("CapitalizationEndDate")));
                        map.put("CyclePointOfRateReset", CommonUtils.isNull(attributes.get("CyclePointOfRateReset")) ? null : map.get("CyclePointOfInterestPayment") == CyclePointOfInterestPayment.B ? CyclePointOfRateReset.E : CyclePointOfRateReset.valueOf(attributes.get("CyclePointOfRateReset")));
                        map.put("Currency", attributes.get("Currency"));
                        map.put("InitialExchangeDate", LocalDateTime.parse(attributes.get("InitialExchangeDate")));
                        map.put("PremiumDiscountAtIED", (CommonUtils.isNull(attributes.get("PremiumDiscountAtIED"))) ? 0.0 : Double.parseDouble(attributes.get("PremiumDiscountAtIED")));
                        map.put("NotionalPrincipal", Double.parseDouble(attributes.get("NotionalPrincipal")));
                        map.put("PurchaseDate", (CommonUtils.isNull(attributes.get("PurchaseDate"))) ? null : LocalDateTime.parse(attributes.get("PurchaseDate")));
                        map.put("PriceAtPurchaseDate", (CommonUtils.isNull(attributes.get("PriceAtPurchaseDate"))) ? 0.0 : Double.parseDouble(attributes.get("PriceAtPurchaseDate")));
                        map.put("TerminationDate", (CommonUtils.isNull(attributes.get("TerminationDate"))) ? null : LocalDateTime.parse(attributes.get("TerminationDate")));
                        map.put("PriceAtTerminationDate", (CommonUtils.isNull(attributes.get("PriceAtTerminationDate"))) ? 0.0 : Double.parseDouble(attributes.get("PriceAtTerminationDate")));
                        map.put("MarketObjectCodeOfScalingIndex", attributes.get("MarketObjectCodeOfScalingIndex"));
                        map.put("ScalingIndexAtContractDealDate", (CommonUtils.isNull(attributes.get("ScalingIndexAtContractDealDate"))) ? 0.0 : Double.parseDouble(attributes.get("ScalingIndexAtContractDealDate")));
                        map.put("NotionalScalingMultiplier", (CommonUtils.isNull(attributes.get("NotionalScalingMultiplier"))) ? 1.0 : Double.parseDouble(attributes.get("NotionalScalingMultiplier")));
                        map.put("InterestScalingMultiplier", (CommonUtils.isNull(attributes.get("InterestScalingMultiplier"))) ? 1.0 : Double.parseDouble(attributes.get("InterestScalingMultiplier")));
                        map.put("CycleAnchorDateOfScalingIndex", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfScalingIndex"))) ? ((CommonUtils.isNull(attributes.get("CycleOfScalingIndex"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfScalingIndex")));
                        map.put("CycleOfScalingIndex", attributes.get("CycleOfScalingIndex"));
                        map.put("ScalingEffect", CommonUtils.isNull(attributes.get("ScalingEffect")) ? null : ScalingEffect.valueOf(attributes.get("ScalingEffect")));
                        // TODO: review prepayment mechanism and attributes
                        map.put("CycleAnchorDateOfOptionality", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfOptionality"))) ? ((CommonUtils.isNull(attributes.get("CycleOfOptionality"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfOptionality")));
                        map.put("CycleOfOptionality", attributes.get("CycleOfOptionality"));
                        map.put("PenaltyType", (CommonUtils.isNull(attributes.get("PenaltyType"))) ? PenaltyType.valueOf("N") : PenaltyType.valueOf(attributes.get("PenaltyType")));
                        map.put("PenaltyRate", (CommonUtils.isNull(attributes.get("PenaltyRate"))) ? 0.0 : Double.parseDouble(attributes.get("PenaltyRate")));
                        map.put("ObjectCodeOfPrepaymentModel", attributes.get("ObjectCodeOfPrepaymentModel"));
                        map.put("CycleAnchorDateOfRateReset", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfRateReset"))) ? ((CommonUtils.isNull(attributes.get("CycleOfRateReset"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfRateReset")));
                        map.put("CycleOfRateReset", attributes.get("CycleOfRateReset"));
                        map.put("RateSpread", (CommonUtils.isNull(attributes.get("RateSpread"))) ? 0.0 : Double.parseDouble(attributes.get("RateSpread")));
                        map.put("MarketObjectCodeOfRateReset", attributes.get("MarketObjectCodeOfRateReset"));
                        map.put("LifeCap", (CommonUtils.isNull(attributes.get("LifeCap"))) ? Double.POSITIVE_INFINITY : Double.parseDouble(attributes.get("LifeCap")));
                        map.put("LifeFloor", (CommonUtils.isNull(attributes.get("LifeFloor"))) ? Double.NEGATIVE_INFINITY : Double.parseDouble(attributes.get("LifeFloor")));
                        map.put("PeriodCap", (CommonUtils.isNull(attributes.get("PeriodCap"))) ? Double.POSITIVE_INFINITY : Double.parseDouble(attributes.get("PeriodCap")));
                        map.put("PeriodFloor", (CommonUtils.isNull(attributes.get("PeriodFloor"))) ? Double.POSITIVE_INFINITY : Double.parseDouble(attributes.get("PeriodFloor")));
                        map.put("FixingDays", attributes.get("FixingDays"));
                        map.put("NextResetRate", (CommonUtils.isNull(attributes.get("NextResetRate"))) ? null : Double.parseDouble(attributes.get("NextResetRate")));
                        map.put("RateMultiplier", (CommonUtils.isNull(attributes.get("RateMultiplier"))) ? 0.0 : Double.parseDouble(attributes.get("RateMultiplier")));

                        // present for LAM, NAM, ANN but not PAM
                        map.put("MaturityDate", (CommonUtils.isNull(attributes.get("MaturityDate")) ? null : LocalDateTime.parse(attributes.get("MaturityDate"))));
                        map.put("CycleAnchorDateOfInterestCalculationBase", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestCalculationBase"))) ? ((CommonUtils.isNull(attributes.get("CycleOfInterestCalculationBase"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfInterestCalculationBase")));
                        map.put("CycleOfInterestCalculationBase", attributes.get("CycleOfInterestCalculationBase"));
                        map.put("InterestCalculationBase", CommonUtils.isNull(attributes.get("InterestCalculationBase"))?null:InterestCalculationBase.valueOf(attributes.get("InterestCalculationBase")));
                        map.put("InterestCalculationBaseAmount", (CommonUtils.isNull(attributes.get("InterestCalculationBaseAmount"))) ? 0.0 : Double.parseDouble(attributes.get("InterestCalculationBaseAmount")));
                        map.put("CycleAnchorDateOfPrincipalRedemption", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfPrincipalRedemption"))) ? LocalDateTime.parse(attributes.get("InitialExchangeDate")) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfPrincipalRedemption")));
                        map.put("CycleOfPrincipalRedemption", attributes.get("CycleOfPrincipalRedemption"));
                        map.put("NextPrincipalRedemptionPayment", (CommonUtils.isNull(attributes.get("NextPrincipalRedemptionPayment"))) ? null : Double.parseDouble(attributes.get("NextPrincipalRedemptionPayment")));

                        // present for ANN but not for LAM, NAM
                        map.put("AmortizationDate", (CommonUtils.isNull(attributes.get("AmortizationDate")) ? null : LocalDateTime.parse(attributes.get("AmortizationDate"))));

                        break;
                    case CLM:
                        map.put("Calendar", (!CommonUtils.isNull(attributes.get("Calendar")) && attributes.get("Calendar").equals("MF")) ? new MondayToFridayCalendar() : new NoHolidaysCalendar());
                        map.put("BusinessDayConvention", new BusinessDayAdjuster(CommonUtils.isNull(attributes.get("BusinessDayConvention")) ? null : BusinessDayConventionEnum.valueOf(attributes.get("BusinessDayConvention")), (BusinessDayCalendarProvider) map.get("Calendar")));
                        map.put("EndOfMonthConvention", (CommonUtils.isNull(attributes.get("EndOfMonthConvention"))) ? EndOfMonthConventionEnum.SD : EndOfMonthConventionEnum.valueOf(attributes.get("EndOfMonthConvention")));
                        map.put("ContractType", ContractTypeEnum.valueOf(attributes.get("ContractType")));
                        map.put("StatusDate", LocalDateTime.parse(attributes.get("StatusDate")));
                        map.put("ContractRole", ContractRole.valueOf(attributes.get("ContractRole")));
                        map.put("LegalEntityIDCounterparty", attributes.get("LegalEntityIDCounterparty"));
                        map.put("CycleAnchorDateOfFee", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfFee"))) ? ((CommonUtils.isNull(attributes.get("CycleOfFee"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfFee")));
                        map.put("CycleOfFee", attributes.get("CycleOfFee"));
                        map.put("FeeBasis", (CommonUtils.isNull(attributes.get("FeeBasis"))) ? null : FeeBasis.valueOf(attributes.get("FeeBasis")));
                        map.put("FeeRate", (CommonUtils.isNull(attributes.get("FeeRate"))) ? 0.0 : Double.parseDouble(attributes.get("FeeRate")));
                        map.put("FeeAccrued", (CommonUtils.isNull(attributes.get("FeeAccrued"))) ? 0.0 : Double.parseDouble(attributes.get("FeeAccrued")));
                        map.put("CycleAnchorDateOfInterestPayment", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestPayment"))) ? ((CommonUtils.isNull(attributes.get("CycleOfInterestPayment"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfInterestPayment")));
                        map.put("CycleOfInterestPayment", attributes.get("CycleOfInterestPayment"));
                        map.put("NominalInterestRate", (CommonUtils.isNull(attributes.get("NominalInterestRate"))) ? 0.0 : Double.parseDouble(attributes.get("NominalInterestRate")));
                        map.put("DayCountConvention", new DayCountCalculator(attributes.get("DayCountConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                        map.put("AccruedInterest", (CommonUtils.isNull(attributes.get("AccruedInterest"))) ? 0.0 : Double.parseDouble(attributes.get("AccruedInterest")));
                        map.put("Currency", attributes.get("Currency"));
                        map.put("InitialExchangeDate", LocalDateTime.parse(attributes.get("InitialExchangeDate")));
                        map.put("NotionalPrincipal", Double.parseDouble(attributes.get("NotionalPrincipal")));
                        map.put("MaturityDate", (CommonUtils.isNull(attributes.get("MaturityDate")) ? null : LocalDateTime.parse(attributes.get("MaturityDate"))));
                        map.put("XDayNotice", attributes.get("XDayNotice"));
                        map.put("CycleAnchorDateOfRateReset", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfRateReset"))) ? ((CommonUtils.isNull(attributes.get("CycleOfRateReset"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfRateReset")));
                        map.put("CycleOfRateReset", attributes.get("CycleOfRateReset"));
                        map.put("RateSpread", (CommonUtils.isNull(attributes.get("RateSpread"))) ? 0.0 : Double.parseDouble(attributes.get("RateSpread")));
                        map.put("MarketObjectCodeOfRateReset", attributes.get("MarketObjectCodeOfRateReset"));
                        map.put("FixingDays", attributes.get("FixingDays"));
                        map.put("NextResetRate", (CommonUtils.isNull(attributes.get("NextResetRate"))) ? null : Double.parseDouble(attributes.get("NextResetRate")));
                        map.put("RateMultiplier", (CommonUtils.isNull(attributes.get("RateMultiplier"))) ? 0.0 : Double.parseDouble(attributes.get("RateMultiplier")));
                        map.put("LifeCap", CommonUtils.isNull(attributes.get("LifeCap")) ? null : Double.parseDouble(attributes.get("LifeCap")));
                        map.put("LifeFloor", CommonUtils.isNull(attributes.get("LifeFloor")) ? null : Double.parseDouble(attributes.get("LifeFloor")));
                        map.put("PeriodCap", CommonUtils.isNull(attributes.get("PeriodCap")) ? null : Double.parseDouble(attributes.get("PeriodCap")));
                        map.put("PeriodFloor", CommonUtils.isNull(attributes.get("PeriodFloor")) ? null : Double.parseDouble(attributes.get("PeriodFloor")));
                        break;
                    case UMP:

                        map.put("Calendar", (!CommonUtils.isNull(attributes.get("Calendar")) && attributes.get("Calendar").equals("MF")) ? new MondayToFridayCalendar() : new NoHolidaysCalendar());
                        map.put("BusinessDayConvention", new BusinessDayAdjuster(CommonUtils.isNull(attributes.get("BusinessDayConvention")) ? null : BusinessDayConventionEnum.valueOf(attributes.get("BusinessDayConvention")), (BusinessDayCalendarProvider) map.get("Calendar")));
                        map.put("EndOfMonthConvention", (CommonUtils.isNull(attributes.get("EndOfMonthConvention"))) ? EndOfMonthConventionEnum.SD : EndOfMonthConventionEnum.valueOf(attributes.get("EndOfMonthConvention")));
                        map.put("ContractType", ContractTypeEnum.valueOf(attributes.get("ContractType")));
                        map.put("StatusDate", LocalDateTime.parse(attributes.get("StatusDate")));
                        map.put("ContractRole", ContractRole.valueOf(attributes.get("ContractRole")));
                        map.put("LegalEntityIDCounterparty", attributes.get("LegalEntityIDCounterparty"));
                        map.put("CycleAnchorDateOfFee", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfFee"))) ? ((CommonUtils.isNull(attributes.get("CycleOfFee"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfFee")));
                        map.put("CycleOfFee", attributes.get("CycleOfFee"));
                        map.put("FeeBasis", (CommonUtils.isNull(attributes.get("FeeBasis"))) ? null : FeeBasis.valueOf(attributes.get("FeeBasis")));
                        map.put("FeeRate", (CommonUtils.isNull(attributes.get("FeeRate"))) ? 0.0 : Double.parseDouble(attributes.get("FeeRate")));
                        map.put("FeeAccrued", (CommonUtils.isNull(attributes.get("FeeAccrued"))) ? 0.0 : Double.parseDouble(attributes.get("FeeAccrued")));
                        map.put("CycleAnchorDateOfInterestPayment", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestPayment"))) ? ((CommonUtils.isNull(attributes.get("CycleOfInterestPayment"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfInterestPayment")));
                        map.put("CycleOfInterestPayment", attributes.get("CycleOfInterestPayment"));
                        map.put("NominalInterestRate", (CommonUtils.isNull(attributes.get("NominalInterestRate"))) ? 0.0 : Double.parseDouble(attributes.get("NominalInterestRate")));
                        map.put("DayCountConvention", new DayCountCalculator(attributes.get("DayCountConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                        map.put("AccruedInterest", (CommonUtils.isNull(attributes.get("AccruedInterest"))) ? 0.0 : Double.parseDouble(attributes.get("AccruedInterest")));
                        map.put("Currency", attributes.get("Currency"));
                        map.put("InitialExchangeDate", LocalDateTime.parse(attributes.get("InitialExchangeDate")));
                        map.put("NotionalPrincipal", Double.parseDouble(attributes.get("NotionalPrincipal")));
                        map.put("TerminationDate", (CommonUtils.isNull(attributes.get("TerminationDate"))) ? null : LocalDateTime.parse(attributes.get("TerminationDate")));
                        map.put("PriceAtTerminationDate", (CommonUtils.isNull(attributes.get("PriceAtTerminationDate"))) ? 0.0 : Double.parseDouble(attributes.get("PriceAtTerminationDate")));
                        map.put("XDayNotice", attributes.get("XDayNotice"));
                        map.put("MaximumPenaltyFreeDisbursement", (CommonUtils.isNull(attributes.get("MaximumPenaltyFreeDisbursement"))) ? attributes.get("NotionalPrincipal") : attributes.get("MaximumPenaltyFreeDisbursement"));
                        map.put("CycleAnchorDateOfRateReset", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfRateReset"))) ? ((CommonUtils.isNull(attributes.get("CycleOfRateReset"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfRateReset")));
                        map.put("CycleOfRateReset", attributes.get("CycleOfRateReset"));
                        map.put("RateSpread", (CommonUtils.isNull(attributes.get("RateSpread"))) ? 0.0 : Double.parseDouble(attributes.get("RateSpread")));
                        map.put("MarketObjectCodeOfRateReset", attributes.get("MarketObjectCodeOfRateReset"));
                        map.put("FixingDays", attributes.get("FixingDays"));
                        map.put("NextResetRate", (CommonUtils.isNull(attributes.get("NextResetRate"))) ? null : Double.parseDouble(attributes.get("NextResetRate")));
                        map.put("RateMultiplier", (CommonUtils.isNull(attributes.get("RateMultiplier"))) ? 1.0 : Double.parseDouble(attributes.get("RateMultiplier")));

                        break;
                    case CSH:

                        map.put("ContractType", ContractTypeEnum.valueOf(attributes.get("ContractType")));
                        map.put("StatusDate", LocalDateTime.parse(attributes.get("StatusDate")));
                        map.put("ContractRole", ContractRole.valueOf(attributes.get("ContractRole")));
                        map.put("Currency", attributes.get("Currency"));
                        map.put("NotionalPrincipal", Double.parseDouble(attributes.get("NotionalPrincipal")));

                        break;
                    case COM: // almost identical with STK

                        map.put("ContractType", ContractTypeEnum.valueOf(attributes.get("ContractType")));
                        map.put("StatusDate", LocalDateTime.parse(attributes.get("StatusDate")));
                        map.put("ContractRole", ContractRole.valueOf(attributes.get("ContractRole")));
                        map.put("LegalEntityIDCounterparty", attributes.get("LegalEntityIDCounterparty"));
                        map.put("Currency", attributes.get("Currency"));
                        map.put("Quantity", (CommonUtils.isNull(attributes.get("Quantity"))) ? 1 : Integer.parseInt(attributes.get("Quantity")));
                        map.put("PurchaseDate", (CommonUtils.isNull(attributes.get("PurchaseDate"))) ? null : LocalDateTime.parse(attributes.get("PurchaseDate")));
                        map.put("PriceAtPurchaseDate", (CommonUtils.isNull(attributes.get("PriceAtPurchaseDate"))) ? 0.0 : Double.parseDouble(attributes.get("PriceAtPurchaseDate")));
                        map.put("TerminationDate", (CommonUtils.isNull(attributes.get("TerminationDate"))) ? null : LocalDateTime.parse(attributes.get("TerminationDate")));
                        map.put("PriceAtTerminationDate", (CommonUtils.isNull(attributes.get("PriceAtTerminationDate"))) ? 0.0 : Double.parseDouble(attributes.get("PriceAtTerminationDate")));
                        map.put("MarketValueObserved", (CommonUtils.isNull(attributes.get("MarketValueObserved"))) ? 0.0 : Double.parseDouble(attributes.get("MarketValueObserved")));

                        break;
                    case STK:

                        map.put("ContractType", ContractTypeEnum.valueOf(attributes.get("ContractType")));
                        map.put("StatusDate", LocalDateTime.parse(attributes.get("StatusDate")));
                        map.put("ContractRole", ContractRole.valueOf(attributes.get("ContractRole")));
                        map.put("LegalEntityIDCounterparty", attributes.get("LegalEntityIDCounterparty"));
                        map.put("Currency", attributes.get("Currency"));
                        map.put("Quantity", (CommonUtils.isNull(attributes.get("Quantity"))) ? 1 : Integer.parseInt(attributes.get("Quantity")));
                        map.put("PurchaseDate", (CommonUtils.isNull(attributes.get("PurchaseDate"))) ? null : LocalDateTime.parse(attributes.get("PurchaseDate")));
                        map.put("PriceAtPurchaseDate", (CommonUtils.isNull(attributes.get("PriceAtPurchaseDate"))) ? 0.0 : Double.parseDouble(attributes.get("PriceAtPurchaseDate")));
                        map.put("TerminationDate", (CommonUtils.isNull(attributes.get("TerminationDate"))) ? null : LocalDateTime.parse(attributes.get("TerminationDate")));
                        map.put("PriceAtTerminationDate", (CommonUtils.isNull(attributes.get("PriceAtTerminationDate"))) ? 0.0 : Double.parseDouble(attributes.get("PriceAtTerminationDate")));
                        map.put("MarketValueObserved", (CommonUtils.isNull(attributes.get("MarketValueObserved"))) ? 0.0 : Double.parseDouble(attributes.get("MarketValueObserved")));

                        // present for STK but not COM
                        map.put("Calendar", (!CommonUtils.isNull(attributes.get("Calendar")) && attributes.get("Calendar").equals("MF")) ? new MondayToFridayCalendar() : new NoHolidaysCalendar());
                        map.put("BusinessDayConvention", new BusinessDayAdjuster(CommonUtils.isNull(attributes.get("BusinessDayConvention")) ? null : BusinessDayConventionEnum.valueOf(attributes.get("BusinessDayConvention")), (BusinessDayCalendarProvider) map.get("Calendar")));
                        map.put("EndOfMonthConvention", (CommonUtils.isNull(attributes.get("EndOfMonthConvention"))) ? EndOfMonthConventionEnum.SD : EndOfMonthConventionEnum.valueOf(attributes.get("EndOfMonthConvention")));
                        map.put("CycleAnchorDateOfDividendPayment", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfDividendPayment"))) ? ((CommonUtils.isNull(attributes.get("CycleOfDividendPayment"))) ? null : LocalDateTime.parse(attributes.get("PurchaseDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfDividendPayment")));
                        map.put("CycleOfDividendPayment", attributes.get("CycleOfDividendPayment"));
                        map.put("MarketObjectCodeOfDividendRate", attributes.get("MarketObjectCodeOfDividendRate"));

                        break;
                    case FXOUT:

                        map.put("Calendar", (!CommonUtils.isNull(attributes.get("Calendar")) && attributes.get("Calendar").equals("MF")) ? new MondayToFridayCalendar() : new NoHolidaysCalendar());
                        map.put("BusinessDayConvention", new BusinessDayAdjuster(CommonUtils.isNull(attributes.get("BusinessDayConvention")) ? null : BusinessDayConventionEnum.valueOf(attributes.get("BusinessDayConvention")), (BusinessDayCalendarProvider) map.get("Calendar")));
                        map.put("EndOfMonthConvention", (CommonUtils.isNull(attributes.get("EndOfMonthConvention"))) ? EndOfMonthConventionEnum.SD : EndOfMonthConventionEnum.valueOf(attributes.get("EndOfMonthConvention")));
                        map.put("ContractType", ContractTypeEnum.valueOf(attributes.get("ContractType")));
                        map.put("StatusDate", LocalDateTime.parse(attributes.get("StatusDate")));
                        map.put("ContractRole", ContractRole.valueOf(attributes.get("ContractRole")));
                        map.put("LegalEntityIDCounterparty", attributes.get("LegalEntityIDCounterparty"));
                        map.put("Currency", attributes.get("Currency"));
                        map.put("Currency2", attributes.get("Currency2"));
                        map.put("MaturityDate", LocalDateTime.parse(attributes.get("MaturityDate")));
                        map.put("NotionalPrincipal", Double.parseDouble(attributes.get("NotionalPrincipal")));
                        map.put("NotionalPrincipal2", Double.parseDouble(attributes.get("NotionalPrincipal2")));
                        map.put("PurchaseDate", (CommonUtils.isNull(attributes.get("PurchaseDate"))) ? null : LocalDateTime.parse(attributes.get("PurchaseDate")));
                        map.put("PriceAtPurchaseDate", (CommonUtils.isNull(attributes.get("PriceAtPurchaseDate"))) ? 0.0 : Double.parseDouble(attributes.get("PriceAtPurchaseDate")));
                        map.put("TerminationDate", (CommonUtils.isNull(attributes.get("TerminationDate"))) ? null : LocalDateTime.parse(attributes.get("TerminationDate")));
                        map.put("PriceAtTerminationDate", (CommonUtils.isNull(attributes.get("PriceAtTerminationDate"))) ? 0.0 : Double.parseDouble(attributes.get("PriceAtTerminationDate")));
                        map.put("DeliverySettlement", CommonUtils.isNull(attributes.get("DeliverySettlement")) ? null : DeliverySettlement.valueOf(attributes.get("DeliverySettlement")));
                        map.put("SettlementDate", (CommonUtils.isNull(attributes.get("SettlementDate"))) ? null : LocalDateTime.parse(attributes.get("SettlementDate")));

                        break;
                    case SWPPV:
                        map.put("Calendar", (!CommonUtils.isNull(attributes.get("Calendar")) && attributes.get("Calendar").equals("MF")) ? new MondayToFridayCalendar() : new NoHolidaysCalendar());
                        map.put("BusinessDayConvention", new BusinessDayAdjuster(CommonUtils.isNull(attributes.get("BusinessDayConvention")) ? null : BusinessDayConventionEnum.valueOf(attributes.get("BusinessDayConvention")), (BusinessDayCalendarProvider) map.get("Calendar")));
                        map.put("EndOfMonthConvention", (CommonUtils.isNull(attributes.get("EndOfMonthConvention"))) ? EndOfMonthConventionEnum.SD : EndOfMonthConventionEnum.valueOf(attributes.get("EndOfMonthConvention")));
                        map.put("ContractType", ContractTypeEnum.valueOf(attributes.get("ContractType")));
                        map.put("StatusDate", LocalDateTime.parse(attributes.get("StatusDate")));
                        map.put("ContractRole", ContractRole.valueOf(attributes.get("ContractRole")));
                        map.put("LegalEntityIDCounterparty", attributes.get("LegalEntityIDCounterparty"));
                        map.put("CycleAnchorDateOfInterestPayment", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestPayment"))) ? ((CommonUtils.isNull(attributes.get("CycleOfInterestPayment"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfInterestPayment")));
                        map.put("CycleOfInterestPayment", attributes.get("CycleOfInterestPayment"));
                        map.put("NominalInterestRate", (CommonUtils.isNull(attributes.get("NominalInterestRate"))) ? 0.0 : Double.parseDouble(attributes.get("NominalInterestRate")));
                        map.put("DayCountConvention", new DayCountCalculator(attributes.get("DayCountConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                        map.put("AccruedInterest", (CommonUtils.isNull(attributes.get("AccruedInterest"))) ? 0.0 : Double.parseDouble(attributes.get("AccruedInterest")));
                        map.put("CapitalizationEndDate", (CommonUtils.isNull(attributes.get("CapitalizationEndDate"))) ? null : LocalDateTime.parse(attributes.get("CapitalizationEndDate")));
                        map.put("CyclePointOfRateReset", CommonUtils.isNull(attributes.get("CyclePointOfRateReset")) ? null : map.get("CyclePointOfInterestPayment") == CyclePointOfInterestPayment.B ? CyclePointOfRateReset.E : CyclePointOfRateReset.valueOf(attributes.get("CyclePointOfRateReset")));
                        map.put("Currency", attributes.get("Currency"));
                        map.put("InitialExchangeDate", LocalDateTime.parse(attributes.get("InitialExchangeDate")));
                        map.put("PremiumDiscountAtIED", (CommonUtils.isNull(attributes.get("PremiumDiscountAtIED"))) ? 0.0 : Double.parseDouble(attributes.get("PremiumDiscountAtIED")));
                        map.put("NotionalPrincipal", Double.parseDouble(attributes.get("NotionalPrincipal")));
                        map.put("PurchaseDate", (CommonUtils.isNull(attributes.get("PurchaseDate"))) ? null : LocalDateTime.parse(attributes.get("PurchaseDate")));
                        map.put("PriceAtPurchaseDate", (CommonUtils.isNull(attributes.get("PriceAtPurchaseDate"))) ? 0.0 : Double.parseDouble(attributes.get("PriceAtPurchaseDate")));
                        map.put("TerminationDate", (CommonUtils.isNull(attributes.get("TerminationDate"))) ? null : LocalDateTime.parse(attributes.get("TerminationDate")));
                        map.put("PriceAtTerminationDate", (CommonUtils.isNull(attributes.get("PriceAtTerminationDate"))) ? 0.0 : Double.parseDouble(attributes.get("PriceAtTerminationDate")));
                        map.put("CycleAnchorDateOfRateReset", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfRateReset"))) ? ((CommonUtils.isNull(attributes.get("CycleOfRateReset"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfRateReset")));
                        map.put("CycleOfRateReset", attributes.get("CycleOfRateReset"));
                        map.put("RateSpread", (CommonUtils.isNull(attributes.get("RateSpread"))) ? 0.0 : Double.parseDouble(attributes.get("RateSpread")));
                        map.put("MarketObjectCodeOfRateReset", attributes.get("MarketObjectCodeOfRateReset"));
                        map.put("FixingDays", attributes.get("FixingDays"));
                        map.put("NextResetRate", (CommonUtils.isNull(attributes.get("NextResetRate"))) ? null : Double.parseDouble(attributes.get("NextResetRate")));
                        map.put("RateMultiplier", (CommonUtils.isNull(attributes.get("RateMultiplier"))) ? 0.0 : Double.parseDouble(attributes.get("RateMultiplier")));
                        map.put("NominalInterestRate2", (CommonUtils.isNull(attributes.get("NominalInterestRate2"))) ? 0.0 : Double.parseDouble(attributes.get("NominalInterestRate2")));
                        map.put("MaturityDate", LocalDateTime.parse(attributes.get("MaturityDate")));
                        map.put("DeliverySettlement", CommonUtils.isNull(attributes.get("DeliverySettlement")) ? null : DeliverySettlement.valueOf(attributes.get("DeliverySettlement")));
                        break;

                    case LAX:
                        map.put("Calendar", (!CommonUtils.isNull(attributes.get("Calendar")) && attributes.get("Calendar").equals("MF")) ? new MondayToFridayCalendar() : new NoHolidaysCalendar());
                        map.put("BusinessDayConvention", new BusinessDayAdjuster(CommonUtils.isNull(attributes.get("BusinessDayConvention")) ? null : BusinessDayConventionEnum.valueOf(attributes.get("BusinessDayConvention")), (BusinessDayCalendarProvider) map.get("Calendar")));
                        map.put("StatusDate", LocalDateTime.parse(attributes.get("StatusDate")));
                        map.put("ContractRole", ContractRole.valueOf(attributes.get("ContractRole")));
                        map.put("LegalEntityIDCounterparty", attributes.get("LegalEntityIDCounterparty"));
                        map.put("LegalEntityIDRecordCreator", attributes.get("LegalEntityIDRecordCreator"));
                        map.put("Currency", attributes.get("Currency"));
                        map.put("InitialExchangeDate", LocalDateTime.parse(attributes.get("InitialExchangeDate")));
                        map.put("PremiumDiscountAtIED", (CommonUtils.isNull(attributes.get("PremiumDiscountAtIED"))) ? 0.0 : Double.parseDouble(attributes.get("PremiumDiscountAtIED")));
                        map.put("MaturityDate", LocalDateTime.parse(attributes.get("MaturityDate")));
                        map.put("NotionalPrincipal", Double.parseDouble(attributes.get("NotionalPrincipal")));
                        map.put("ArrayCycleAnchorDateOfPrincipalRedemption", attributes.get("ArrayCycleAnchorDateOfPrincipalRedemption"));
                        map.put("ArrayCycleOfPrincipalRedemption", attributes.get("ArrayCycleOfPrincipalRedemption"));
                        map.put("ArrayNextPrincipalRedemptionPayment", (CommonUtils.isNull(attributes.get("ArrayNextPrincipalRedemptionPayment"))) ? 0 : attributes.get("ArrayNextPrincipalRedemptionPayment"));
                        map.put("ArrayIncreaseDecrease", attributes.get("ArrayIncreaseDecrease"));
                        map.put("ArrayCycleAnchorDateOfInterestPayment", attributes.get("ArrayCycleAnchorDateOfInterestPayment"));
                        map.put("ArrayCycleOfInterestPayment", attributes.get("ArrayCycleOfInterestPayment"));
                        map.put("NominalInterestRate", (CommonUtils.isNull(attributes.get("NominalInterestRate"))) ? 0.0 : Double.parseDouble(attributes.get("NominalInterestRate")));
                        map.put("DayCountConvention", new DayCountCalculator(attributes.get("DayCountConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                        map.put("ArrayCycleAnchorDateOfRateReset", attributes.get("ArrayCycleAnchorDateOfRateReset"));
                        map.put("ArrayCycleOfRateReset", attributes.get("ArrayCycleOfRateReset"));
                        map.put("ArrayRate", attributes.get("ArrayRate"));
                        map.put("ArrayFixedVariable", attributes.get("ArrayFixedVariable"));
                        map.put("MarketObjectCodeOfRateReset", attributes.get("MarketObjectCodeOfRateReset"));
                        map.put("ContractType", ContractTypeEnum.valueOf(attributes.get("ContractType")));
                        map.put("FeeRate", (CommonUtils.isNull(attributes.get("FeeRate"))) ? 0.0 : Double.parseDouble(attributes.get("FeeRate")));
                        map.put("EndOfMonthConvention", (CommonUtils.isNull(attributes.get("EndOfMonthConvention"))) ? EndOfMonthConventionEnum.SD : EndOfMonthConventionEnum.valueOf(attributes.get("EndOfMonthConvention")));
                        map.put("RateMultiplier", (CommonUtils.isNull(attributes.get("RateMultiplier"))) ? 0.0 : Double.parseDouble(attributes.get("RateMultiplier")));
                        map.put("RateSpread", (CommonUtils.isNull(attributes.get("RateSpread"))) ? 0.0 : Double.parseDouble(attributes.get("RateSpread")));
                        map.put("PeriodFloor", (CommonUtils.isNull(attributes.get("PeriodFloor"))) ? Double.POSITIVE_INFINITY : Double.parseDouble(attributes.get("PeriodFloor")));
                        map.put("LifeCap", (CommonUtils.isNull(attributes.get("LifeCap"))) ? Double.POSITIVE_INFINITY : Double.parseDouble(attributes.get("LifeCap")));
                        map.put("LifeFloor", (CommonUtils.isNull(attributes.get("LifeFloor"))) ? Double.NEGATIVE_INFINITY : Double.parseDouble(attributes.get("LifeFloor")));
                        map.put("CycleAnchorDateOfInterestCalculationBase", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestCalculationBase"))) ? ((CommonUtils.isNull(attributes.get("CycleOfInterestCalculationBase"))) ? null : LocalDateTime.parse(attributes.get("InitialExchangeDate"))) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfInterestCalculationBase")));
                        map.put("CycleOfInterestCalculationBase", attributes.get("CycleOfInterestCalculationBase"));
                        map.put("InterestCalculationBase", CommonUtils.isNull(attributes.get("InterestCalculationBase")) ? null : InterestCalculationBase.valueOf(attributes.get("InterestCalculationBase")));
                        map.put("InterestCalculationBaseAmount", (CommonUtils.isNull(attributes.get("InterestCalculationBaseAmount"))) ? 0.0 : Double.parseDouble(attributes.get("InterestCalculationBaseAmount")));
                        map.put("CycleAnchorDateOfPrincipalRedemption", (CommonUtils.isNull(attributes.get("CycleAnchorDateOfPrincipalRedemption"))) ? LocalDateTime.parse(attributes.get("InitialExchangeDate")) : LocalDateTime.parse(attributes.get("CycleAnchorDateOfPrincipalRedemption")));
                        break;

                    default:
                        throw new ContractTypeUnknownException();
                }
            } catch (Exception e) {
                throw new AttributeConversionException();
            }
        }
        return new ContractModel(map);
    }

    @Override
    public <T> T getAs(String name) {
        return (T) attributes.get(name);
    }

    public void addAttribute(String Key, Object value){
        attributes.put(Key,value);
    }
}

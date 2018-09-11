/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.attributes;

import org.actus.AttributeConversionException;
import org.actus.ContractTypeUnknownException;
import org.actus.contracts.ContractType;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.time.calendar.BusinessDayCalendarProvider;
import org.actus.time.calendar.MondayToFridayCalendar;
import org.actus.time.calendar.NoHolidaysCalendar;
import org.actus.util.CommonUtils;
import org.actus.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * @see <a href="http://www.projectactus.org/projectactus/?page_id=356">ACTUS Data Dictionary</a>
 */
public class ContractModel2 implements ContractModelProvider {
    private Map<String,Object> attributes;

    /**
     * Constructor
     * <p>
     * The map provided as the constructor argument is expected to contain <key,value> pairs
     * of attributes using ACTUS attribute names (in long form) and data types of values
     * as per official ACTUS data dictionary.
     *
     */
    public ContractModel2(Map<String,Object> attributes) {
        this.attributes=attributes;
    }

    /**
     * Create a new contract model from a java map
     * <p>
     * The map provided as the method argument is expected to contain <key,value> pairs
     * of attributes using ACTUS attribute names (in long form) and data types of values
     * as per official ACTUS data dictionary.
     *
     * @param attributes a java map of attributes as per ACTUS data dictionary
     *
     * @return an instance of ContractModel containing the attributes provided with the method argument
     */
    public static ContractModel2 of(Map<String,Object> attributes) {
        return new ContractModel2(attributes);
    }


    /**
     * Parse the attributes from external String-representation to internal, attribute-specific data types
     * <p>
     * For the {@link ContractType} indicated in attribute "ContractType" the method goes through the list
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
     *
     * @return an instance of ContractModel containing the attributes provided with the method argument
     *
     * @throws AttributeConversionException if an attribute cannot be parsed to its data type
     */
    public static ContractModel2 parse(Map<String,Object> attributes) {
        HashMap<String,Object> map = new HashMap<>();

        // parse all attributes known to the respective contract type
        try{
        switch((String) attributes.get("ContractType")) {
            case StringUtils.ContractType_PAM:
                map.put("Calendar",(!CommonUtils.isNull(attributes.get("Calendar")) && attributes.get("Calendar").equals("MondayToFriday"))? new MondayToFridayCalendar() : new NoHolidaysCalendar());
                map.put("BusinessDayConvention",new BusinessDayAdjuster((String) attributes.get("BusinessDayConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                map.put("EndOfMonthConvention",(CommonUtils.isNull(attributes.get("EndOfMonthConvention")))? "SD" : attributes.get("EndOfMonthConvention"));
                map.put("ContractType",attributes.get("ContractType"));
                map.put("StatusDate",LocalDateTime.parse((String) attributes.get("StatusDate")));
                map.put("ContractRole",attributes.get("ContractRole"));
                map.put("LegalEntityIDCounterparty",attributes.get("LegalEntityIDCounterparty"));

                map.put("MarketObjectCode",attributes.get("MarketObjectCode"));

                map.put("CycleAnchorDateOfFee",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfFee")))? ( (CommonUtils.isNull(attributes.get("CycleOfFee")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfFee")));
                map.put("CycleOfFee",attributes.get("CycleOfFee"));
                map.put("FeeBasis",(CommonUtils.isNull(attributes.get("FeeBasis")))? "0" : attributes.get("FeeBasis"));
                map.put("FeeRate",(CommonUtils.isNull(attributes.get("FeeRate")))? 0.0 : Double.parseDouble((String) attributes.get("FeeRate")));
                map.put("FeeAccrued",(CommonUtils.isNull(attributes.get("FeeAccrued")))? 0.0 : Double.parseDouble((String) attributes.get("FeeAccrued")));
                map.put("CycleAnchorDateOfInterestPayment",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestPayment")))? ( (CommonUtils.isNull(attributes.get("CycleOfInterestPayment")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfInterestPayment")));
                map.put("CycleOfInterestPayment",attributes.get("CycleOfInterestPayment"));
                map.put("NominalInterestRate",(CommonUtils.isNull(attributes.get("NominalInterestRate")))? 0.0 : Double.parseDouble((String) attributes.get("NominalInterestRate")));
                map.put("DayCountConvention",new DayCountCalculator((String) attributes.get("DayCountConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                map.put("AccruedInterest",(CommonUtils.isNull(attributes.get("AccruedInterest")))? 0.0 : Double.parseDouble((String) attributes.get("AccruedInterest")));
                map.put("CapitalizationEndDate",(CommonUtils.isNull(attributes.get("CapitalizationEndDate")))? null : LocalDateTime.parse((String) attributes.get("CapitalizationEndDate")));
                map.put("CyclePointOfInterestPayment",attributes.get("CyclePointOfInterestPayment"));
                map.put("Currency",attributes.get("Currency"));
                map.put("InitialExchangeDate",LocalDateTime.parse((String) attributes.get("InitialExchangeDate")));
                map.put("PremiumDiscountAtIED",(CommonUtils.isNull(attributes.get("PremiumDiscountAtIED")))? 0.0 : Double.parseDouble((String) attributes.get("PremiumDiscountAtIED")));
                map.put("NotionalPrincipal",Double.parseDouble((String) attributes.get("NotionalPrincipal")));
                map.put("PurchaseDate",(CommonUtils.isNull(attributes.get("PurchaseDate")))? null : LocalDateTime.parse((String) attributes.get("PurchaseDate")));
                map.put("PriceAtPurchaseDate",(CommonUtils.isNull(attributes.get("PriceAtPurchaseDate")))? 0.0 : Double.parseDouble((String) attributes.get("PriceAtPurchaseDate")));
                map.put("TerminationDate",(CommonUtils.isNull(attributes.get("TerminationDate")))? null : LocalDateTime.parse((String) attributes.get("TerminationDate")));
                map.put("PriceAtTerminationDate",(CommonUtils.isNull(attributes.get("PriceAtTerminationDate")))? 0.0 : Double.parseDouble((String) attributes.get("PriceAtTerminationDate")));
                map.put("MarketObjectCodeOfScalingIndex",attributes.get("MarketObjectCodeOfScalingIndex"));
                map.put("ScalingIndexAtStatusDate",(CommonUtils.isNull(attributes.get("ScalingIndexAtStatusDate")))? 0.0 : Double.parseDouble((String) attributes.get("ScalingIndexAtStatusDate")));
                map.put("CycleAnchorDateOfScalingIndex",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfScalingIndex")))? ( (CommonUtils.isNull(attributes.get("CycleOfScalingIndex")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfScalingIndex")));
                map.put("CycleOfScalingIndex",attributes.get("CycleOfScalingIndex"));
                map.put("ScalingEffect",attributes.get("ScalingEffect"));
                // TODO: review prepayment mechanism and attributes
                map.put("CycleAnchorDateOfOptionality",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfOptionality")))? ( (CommonUtils.isNull(attributes.get("CycleOfOptionality")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfOptionality")));
                map.put("CycleOfOptionality",attributes.get("CycleOfOptionality"));
                map.put("PenaltyType",(CommonUtils.isNull(attributes.get("PenaltyType")))? 'N' : ((String) attributes.get("PenaltyType")).charAt(0));
                map.put("PenaltyRate",(CommonUtils.isNull(attributes.get("PenaltyRate")))? 0.0 : Double.parseDouble((String) attributes.get("PenaltyRate")));
                map.put("ObjectCodeOfPrepaymentModel",attributes.get("ObjectCodeOfPrepaymentModel"));

                map.put("CycleAnchorDateOfRateReset",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfRateReset")))? ( (CommonUtils.isNull(attributes.get("CycleOfRateReset")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfRateReset")));
                map.put("CycleOfRateReset",attributes.get("CycleOfRateReset"));
                map.put("RateSpread",(CommonUtils.isNull(attributes.get("RateSpread")))? 0.0 : Double.parseDouble((String) attributes.get("RateSpread")));
                map.put("MarketObjectCodeOfRateReset",attributes.get("MarketObjectCodeOfRateReset"));
                map.put("LifeCap",(CommonUtils.isNull(attributes.get("LifeCap")))? Double.POSITIVE_INFINITY : Double.parseDouble((String) attributes.get("LifeCap")));
                map.put("LifeFloor",(CommonUtils.isNull(attributes.get("LifeFloor")))? Double.NEGATIVE_INFINITY : Double.parseDouble((String) attributes.get("LifeFloor")));
                map.put("PeriodCap",(CommonUtils.isNull(attributes.get("PeriodCap")))? Double.POSITIVE_INFINITY : Double.parseDouble((String) attributes.get("PeriodCap")));
                map.put("PeriodFloor",(CommonUtils.isNull(attributes.get("PeriodFloor")))? Double.POSITIVE_INFINITY : Double.parseDouble((String) attributes.get("PeriodFloor")));
                map.put("CyclePointOfRateReset",attributes.get("CyclePointOfRateReset"));
                map.put("FixingDays",attributes.get("FixingDays"));
                map.put("NextResetRate",(CommonUtils.isNull(attributes.get("NextResetRate")))? null : Double.parseDouble((String) attributes.get("NextResetRate")));
                map.put("RateMultiplier",(CommonUtils.isNull(attributes.get("RateMultiplier")))? 0.0 : Double.parseDouble((String) attributes.get("RateMultiplier")));

                map.put("MaturityDate",LocalDateTime.parse((String) attributes.get("MaturityDate")));

                break; // nothing else to do for PAM
            case StringUtils.ContractType_LAM:
                map.put("Calendar",(!CommonUtils.isNull(attributes.get("Calendar")) && attributes.get("Calendar").equals("MondayToFriday"))? new MondayToFridayCalendar() : new NoHolidaysCalendar());
                map.put("BusinessDayConvention",new BusinessDayAdjuster((String) attributes.get("BusinessDayConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                map.put("EndOfMonthConvention",(CommonUtils.isNull(attributes.get("EndOfMonthConvention")))? "SD" : attributes.get("EndOfMonthConvention"));
                map.put("ContractType",attributes.get("ContractType"));
                map.put("StatusDate",LocalDateTime.parse((String) attributes.get("StatusDate")));
                map.put("ContractRole",attributes.get("ContractRole"));
                map.put("LegalEntityIDCounterparty",attributes.get("LegalEntityIDCounterparty"));

                map.put("MarketObjectCode",attributes.get("MarketObjectCode"));

                map.put("CycleAnchorDateOfFee",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfFee")))? ( (CommonUtils.isNull(attributes.get("CycleOfFee")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfFee")));
                map.put("CycleOfFee",attributes.get("CycleOfFee"));
                map.put("FeeBasis",(CommonUtils.isNull(attributes.get("FeeBasis")))? "0" : attributes.get("FeeBasis"));
                map.put("FeeRate",(CommonUtils.isNull(attributes.get("FeeRate")))? 0.0 : Double.parseDouble((String) attributes.get("FeeRate")));
                map.put("FeeAccrued",(CommonUtils.isNull(attributes.get("FeeAccrued")))? 0.0 : Double.parseDouble((String) attributes.get("FeeAccrued")));
                map.put("CycleAnchorDateOfInterestPayment",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestPayment")))? ( (CommonUtils.isNull(attributes.get("CycleOfInterestPayment")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfInterestPayment")));
                map.put("CycleOfInterestPayment",attributes.get("CycleOfInterestPayment"));
                map.put("NominalInterestRate",(CommonUtils.isNull(attributes.get("NominalInterestRate")))? 0.0 : Double.parseDouble((String) attributes.get("NominalInterestRate")));
                map.put("DayCountConvention",new DayCountCalculator((String) attributes.get("DayCountConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                map.put("AccruedInterest",(CommonUtils.isNull(attributes.get("AccruedInterest")))? 0.0 : Double.parseDouble((String) attributes.get("AccruedInterest")));
                map.put("CapitalizationEndDate",(CommonUtils.isNull(attributes.get("CapitalizationEndDate")))? null : LocalDateTime.parse((String) attributes.get("CapitalizationEndDate")));
                map.put("CyclePointOfInterestPayment",attributes.get("CyclePointOfInterestPayment"));
                map.put("Currency",attributes.get("Currency"));
                map.put("InitialExchangeDate",LocalDateTime.parse((String) attributes.get("InitialExchangeDate")));
                map.put("PremiumDiscountAtIED",(CommonUtils.isNull(attributes.get("PremiumDiscountAtIED")))? 0.0 : Double.parseDouble((String) attributes.get("PremiumDiscountAtIED")));
                map.put("NotionalPrincipal",Double.parseDouble((String) attributes.get("NotionalPrincipal")));
                map.put("PurchaseDate",(CommonUtils.isNull(attributes.get("PurchaseDate")))? null : LocalDateTime.parse((String) attributes.get("PurchaseDate")));
                map.put("PriceAtPurchaseDate",(CommonUtils.isNull(attributes.get("PriceAtPurchaseDate")))? 0.0 : Double.parseDouble((String) attributes.get("PriceAtPurchaseDate")));
                map.put("TerminationDate",(CommonUtils.isNull(attributes.get("TerminationDate")))? null : LocalDateTime.parse((String) attributes.get("TerminationDate")));
                map.put("PriceAtTerminationDate",(CommonUtils.isNull(attributes.get("PriceAtTerminationDate")))? 0.0 : Double.parseDouble((String) attributes.get("PriceAtTerminationDate")));
                map.put("MarketObjectCodeOfScalingIndex",attributes.get("MarketObjectCodeOfScalingIndex"));
                map.put("ScalingIndexAtStatusDate",(CommonUtils.isNull(attributes.get("ScalingIndexAtStatusDate")))? 0.0 : Double.parseDouble((String) attributes.get("ScalingIndexAtStatusDate")));
                map.put("CycleAnchorDateOfScalingIndex",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfScalingIndex")))? ( (CommonUtils.isNull(attributes.get("CycleOfScalingIndex")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfScalingIndex")));
                map.put("CycleOfScalingIndex",attributes.get("CycleOfScalingIndex"));
                map.put("ScalingEffect",attributes.get("ScalingEffect"));
                // TODO: review prepayment mechanism and attributes
                map.put("CycleAnchorDateOfOptionality",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfOptionality")))? ( (CommonUtils.isNull(attributes.get("CycleOfOptionality")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfOptionality")));
                map.put("CycleOfOptionality",attributes.get("CycleOfOptionality"));
                map.put("PenaltyType",(CommonUtils.isNull(attributes.get("PenaltyType")))? 'N' : ((String) attributes.get("PenaltyType")).charAt(0));
                map.put("PenaltyRate",(CommonUtils.isNull(attributes.get("PenaltyRate")))? 0.0 : Double.parseDouble((String) attributes.get("PenaltyRate")));
                map.put("ObjectCodeOfPrepaymentModel",attributes.get("ObjectCodeOfPrepaymentModel"));

                map.put("CycleAnchorDateOfRateReset",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfRateReset")))? ( (CommonUtils.isNull(attributes.get("CycleOfRateReset")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfRateReset")));
                map.put("CycleOfRateReset",attributes.get("CycleOfRateReset"));
                map.put("RateSpread",(CommonUtils.isNull(attributes.get("RateSpread")))? 0.0 : Double.parseDouble((String) attributes.get("RateSpread")));
                map.put("MarketObjectCodeOfRateReset",attributes.get("MarketObjectCodeOfRateReset"));
                map.put("LifeCap",(CommonUtils.isNull(attributes.get("LifeCap")))? Double.POSITIVE_INFINITY : Double.parseDouble((String) attributes.get("LifeCap")));
                map.put("LifeFloor",(CommonUtils.isNull(attributes.get("LifeFloor")))? Double.NEGATIVE_INFINITY : Double.parseDouble((String) attributes.get("LifeFloor")));
                map.put("PeriodCap",(CommonUtils.isNull(attributes.get("PeriodCap")))? Double.POSITIVE_INFINITY : Double.parseDouble((String) attributes.get("PeriodCap")));
                map.put("PeriodFloor",(CommonUtils.isNull(attributes.get("PeriodFloor")))? Double.POSITIVE_INFINITY : Double.parseDouble((String) attributes.get("PeriodFloor")));
                map.put("CyclePointOfRateReset",attributes.get("CyclePointOfRateReset"));
                map.put("FixingDays",attributes.get("FixingDays"));
                map.put("NextResetRate",(CommonUtils.isNull(attributes.get("NextResetRate")))? null : Double.parseDouble((String) attributes.get("NextResetRate")));
                map.put("RateMultiplier",(CommonUtils.isNull(attributes.get("RateMultiplier")))? 0.0 : Double.parseDouble((String) attributes.get("RateMultiplier")));


                map.put("MaturityDate",(CommonUtils.isNull(attributes.get("MaturityDate"))? null : LocalDateTime.parse((String) attributes.get("MaturityDate"))));
                map.put("CycleAnchorDateOfInterestCalculationBase",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestCalculationBase")))? ( (CommonUtils.isNull(attributes.get("CycleOfInterestCalculationBase")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfInterestCalculationBase")));
                map.put("CycleOfInterestCalculationBase", attributes.get("CycleOfInterestCalculationBase"));
                map.put("InterestCalculationBase",attributes.get("InterestCalculationBase"));
                map.put("InterestCalculationBaseAmount",(CommonUtils.isNull(attributes.get("InterestCalculationBaseAmount")))? 0.0 : Double.parseDouble((String) attributes.get("InterestCalculationBaseAmount")));
                map.put("CycleAnchorDateOfPrincipalRedemption",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfPrincipalRedemption")))? LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfPrincipalRedemption")));
                map.put("CycleOfPrincipalRedemption",attributes.get("CycleOfPrincipalRedemption"));
                map.put("NextPrincipalRedemptionPayment",(CommonUtils.isNull(attributes.get("NextPrincipalRedemptionPayment")))? null : Double.parseDouble((String) attributes.get("NextPrincipalRedemptionPayment")));

            case StringUtils.ContractType_NAM:
                map.put("Calendar",(!CommonUtils.isNull(attributes.get("Calendar")) && attributes.get("Calendar").equals("MondayToFriday"))? new MondayToFridayCalendar() : new NoHolidaysCalendar());
                map.put("BusinessDayConvention",new BusinessDayAdjuster((String) attributes.get("BusinessDayConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                map.put("EndOfMonthConvention",(CommonUtils.isNull(attributes.get("EndOfMonthConvention")))? "SD" : attributes.get("EndOfMonthConvention"));
                map.put("ContractType",attributes.get("ContractType"));
                map.put("StatusDate",LocalDateTime.parse((String) attributes.get("StatusDate")));
                map.put("ContractRole",attributes.get("ContractRole"));
                map.put("LegalEntityIDCounterparty",attributes.get("LegalEntityIDCounterparty"));

                map.put("MarketObjectCode",attributes.get("MarketObjectCode"));

                map.put("CycleAnchorDateOfFee",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfFee")))? ( (CommonUtils.isNull(attributes.get("CycleOfFee")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfFee")));
                map.put("CycleOfFee",attributes.get("CycleOfFee"));
                map.put("FeeBasis",(CommonUtils.isNull(attributes.get("FeeBasis")))? "0" : attributes.get("FeeBasis"));
                map.put("FeeRate",(CommonUtils.isNull(attributes.get("FeeRate")))? 0.0 : Double.parseDouble((String) attributes.get("FeeRate")));
                map.put("FeeAccrued",(CommonUtils.isNull(attributes.get("FeeAccrued")))? 0.0 : Double.parseDouble((String) attributes.get("FeeAccrued")));
                map.put("CycleAnchorDateOfInterestPayment",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestPayment")))? ( (CommonUtils.isNull(attributes.get("CycleOfInterestPayment")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfInterestPayment")));
                map.put("CycleOfInterestPayment",attributes.get("CycleOfInterestPayment"));
                map.put("NominalInterestRate",(CommonUtils.isNull(attributes.get("NominalInterestRate")))? 0.0 : Double.parseDouble((String) attributes.get("NominalInterestRate")));
                map.put("DayCountConvention",new DayCountCalculator((String) attributes.get("DayCountConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                map.put("AccruedInterest",(CommonUtils.isNull(attributes.get("AccruedInterest")))? 0.0 : Double.parseDouble((String) attributes.get("AccruedInterest")));
                map.put("CapitalizationEndDate",(CommonUtils.isNull(attributes.get("CapitalizationEndDate")))? null : LocalDateTime.parse((String) attributes.get("CapitalizationEndDate")));
                map.put("CyclePointOfInterestPayment",attributes.get("CyclePointOfInterestPayment"));
                map.put("Currency",attributes.get("Currency"));
                map.put("InitialExchangeDate",LocalDateTime.parse((String) attributes.get("InitialExchangeDate")));
                map.put("PremiumDiscountAtIED",(CommonUtils.isNull(attributes.get("PremiumDiscountAtIED")))? 0.0 : Double.parseDouble((String) attributes.get("PremiumDiscountAtIED")));
                map.put("NotionalPrincipal",Double.parseDouble((String) attributes.get("NotionalPrincipal")));
                map.put("PurchaseDate",(CommonUtils.isNull(attributes.get("PurchaseDate")))? null : LocalDateTime.parse((String) attributes.get("PurchaseDate")));
                map.put("PriceAtPurchaseDate",(CommonUtils.isNull(attributes.get("PriceAtPurchaseDate")))? 0.0 : Double.parseDouble((String) attributes.get("PriceAtPurchaseDate")));
                map.put("TerminationDate",(CommonUtils.isNull(attributes.get("TerminationDate")))? null : LocalDateTime.parse((String) attributes.get("TerminationDate")));
                map.put("PriceAtTerminationDate",(CommonUtils.isNull(attributes.get("PriceAtTerminationDate")))? 0.0 : Double.parseDouble((String) attributes.get("PriceAtTerminationDate")));
                map.put("MarketObjectCodeOfScalingIndex",attributes.get("MarketObjectCodeOfScalingIndex"));
                map.put("ScalingIndexAtStatusDate",(CommonUtils.isNull(attributes.get("ScalingIndexAtStatusDate")))? 0.0 : Double.parseDouble((String) attributes.get("ScalingIndexAtStatusDate")));
                map.put("CycleAnchorDateOfScalingIndex",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfScalingIndex")))? ( (CommonUtils.isNull(attributes.get("CycleOfScalingIndex")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfScalingIndex")));
                map.put("CycleOfScalingIndex",attributes.get("CycleOfScalingIndex"));
                map.put("ScalingEffect",attributes.get("ScalingEffect"));
                // TODO: review prepayment mechanism and attributes
                map.put("CycleAnchorDateOfOptionality",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfOptionality")))? ( (CommonUtils.isNull(attributes.get("CycleOfOptionality")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfOptionality")));
                map.put("CycleOfOptionality",attributes.get("CycleOfOptionality"));
                map.put("PenaltyType",(CommonUtils.isNull(attributes.get("PenaltyType")))? 'N' : ((String) attributes.get("PenaltyType")).charAt(0));
                map.put("PenaltyRate",(CommonUtils.isNull(attributes.get("PenaltyRate")))? 0.0 : Double.parseDouble((String) attributes.get("PenaltyRate")));
                map.put("ObjectCodeOfPrepaymentModel",attributes.get("ObjectCodeOfPrepaymentModel"));

                map.put("CycleAnchorDateOfRateReset",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfRateReset")))? ( (CommonUtils.isNull(attributes.get("CycleOfRateReset")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfRateReset")));
                map.put("CycleOfRateReset",attributes.get("CycleOfRateReset"));
                map.put("RateSpread",(CommonUtils.isNull(attributes.get("RateSpread")))? 0.0 : Double.parseDouble((String) attributes.get("RateSpread")));
                map.put("MarketObjectCodeOfRateReset",attributes.get("MarketObjectCodeOfRateReset"));
                map.put("LifeCap",(CommonUtils.isNull(attributes.get("LifeCap")))? Double.POSITIVE_INFINITY : Double.parseDouble((String) attributes.get("LifeCap")));
                map.put("LifeFloor",(CommonUtils.isNull(attributes.get("LifeFloor")))? Double.NEGATIVE_INFINITY : Double.parseDouble((String) attributes.get("LifeFloor")));
                map.put("PeriodCap",(CommonUtils.isNull(attributes.get("PeriodCap")))? Double.POSITIVE_INFINITY : Double.parseDouble((String) attributes.get("PeriodCap")));
                map.put("PeriodFloor",(CommonUtils.isNull(attributes.get("PeriodFloor")))? Double.POSITIVE_INFINITY : Double.parseDouble((String) attributes.get("PeriodFloor")));
                map.put("CyclePointOfRateReset",attributes.get("CyclePointOfRateReset"));
                map.put("FixingDays",attributes.get("FixingDays"));
                map.put("NextResetRate",(CommonUtils.isNull(attributes.get("NextResetRate")))? null : Double.parseDouble((String) attributes.get("NextResetRate")));
                map.put("RateMultiplier",(CommonUtils.isNull(attributes.get("RateMultiplier")))? 0.0 : Double.parseDouble((String) attributes.get("RateMultiplier")));

                // present for LAM, NAM, ANN but not PAM
                map.put("MaturityDate",(CommonUtils.isNull(attributes.get("MaturityDate"))? null : LocalDateTime.parse((String) attributes.get("MaturityDate"))));
                map.put("CycleAnchorDateOfInterestCalculationBase",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestCalculationBase")))? ( (CommonUtils.isNull(attributes.get("CycleOfInterestCalculationBase")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfInterestCalculationBase")));
                map.put("CycleOfInterestCalculationBase", attributes.get("CycleOfInterestCalculationBase"));
                map.put("InterestCalculationBase",attributes.get("InterestCalculationBase"));
                map.put("InterestCalculationBaseAmount",(CommonUtils.isNull(attributes.get("InterestCalculationBaseAmount")))? 0.0 : Double.parseDouble((String) attributes.get("InterestCalculationBaseAmount")));
                map.put("CycleAnchorDateOfPrincipalRedemption",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfPrincipalRedemption")))? LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfPrincipalRedemption")));
                map.put("CycleOfPrincipalRedemption",attributes.get("CycleOfPrincipalRedemption"));
                map.put("NextPrincipalRedemptionPayment",(CommonUtils.isNull(attributes.get("NextPrincipalRedemptionPayment")))? null : Double.parseDouble((String) attributes.get("NextPrincipalRedemptionPayment")));

            case StringUtils.ContractType_ANN: // almost identical with LAM, NAM, ANN
                map.put("Calendar",(!CommonUtils.isNull(attributes.get("Calendar")) && attributes.get("Calendar").equals("MondayToFriday"))? new MondayToFridayCalendar() : new NoHolidaysCalendar());
                map.put("BusinessDayConvention",new BusinessDayAdjuster((String) attributes.get("BusinessDayConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                map.put("EndOfMonthConvention",(CommonUtils.isNull(attributes.get("EndOfMonthConvention")))? "SD" : attributes.get("EndOfMonthConvention"));
                map.put("ContractType",attributes.get("ContractType"));
                map.put("StatusDate",LocalDateTime.parse((String) attributes.get("StatusDate")));
                map.put("ContractRole",attributes.get("ContractRole"));
                map.put("LegalEntityIDCounterparty",attributes.get("LegalEntityIDCounterparty"));

                map.put("MarketObjectCode",attributes.get("MarketObjectCode"));

                map.put("CycleAnchorDateOfFee",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfFee")))? ( (CommonUtils.isNull(attributes.get("CycleOfFee")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfFee")));
                map.put("CycleOfFee",attributes.get("CycleOfFee"));
                map.put("FeeBasis",(CommonUtils.isNull(attributes.get("FeeBasis")))? "0" : attributes.get("FeeBasis"));
                map.put("FeeRate",(CommonUtils.isNull(attributes.get("FeeRate")))? 0.0 : Double.parseDouble((String) attributes.get("FeeRate")));
                map.put("FeeAccrued",(CommonUtils.isNull(attributes.get("FeeAccrued")))? 0.0 : Double.parseDouble((String) attributes.get("FeeAccrued")));
                map.put("CycleAnchorDateOfInterestPayment",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestPayment")))? ( (CommonUtils.isNull(attributes.get("CycleOfInterestPayment")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfInterestPayment")));
                map.put("CycleOfInterestPayment",attributes.get("CycleOfInterestPayment"));
                map.put("NominalInterestRate",(CommonUtils.isNull(attributes.get("NominalInterestRate")))? 0.0 : Double.parseDouble((String) attributes.get("NominalInterestRate")));
                map.put("DayCountConvention",new DayCountCalculator((String) attributes.get("DayCountConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                map.put("AccruedInterest",(CommonUtils.isNull(attributes.get("AccruedInterest")))? 0.0 : Double.parseDouble((String) attributes.get("AccruedInterest")));
                map.put("CapitalizationEndDate",(CommonUtils.isNull(attributes.get("CapitalizationEndDate")))? null : LocalDateTime.parse((String) attributes.get("CapitalizationEndDate")));
                map.put("CyclePointOfInterestPayment",attributes.get("CyclePointOfInterestPayment"));
                map.put("Currency",attributes.get("Currency"));
                map.put("InitialExchangeDate",LocalDateTime.parse((String) attributes.get("InitialExchangeDate")));
                map.put("PremiumDiscountAtIED",(CommonUtils.isNull(attributes.get("PremiumDiscountAtIED")))? 0.0 : Double.parseDouble((String) attributes.get("PremiumDiscountAtIED")));
                map.put("NotionalPrincipal",Double.parseDouble((String) attributes.get("NotionalPrincipal")));
                map.put("PurchaseDate",(CommonUtils.isNull(attributes.get("PurchaseDate")))? null : LocalDateTime.parse((String) attributes.get("PurchaseDate")));
                map.put("PriceAtPurchaseDate",(CommonUtils.isNull(attributes.get("PriceAtPurchaseDate")))? 0.0 : Double.parseDouble((String) attributes.get("PriceAtPurchaseDate")));
                map.put("TerminationDate",(CommonUtils.isNull(attributes.get("TerminationDate")))? null : LocalDateTime.parse((String) attributes.get("TerminationDate")));
                map.put("PriceAtTerminationDate",(CommonUtils.isNull(attributes.get("PriceAtTerminationDate")))? 0.0 : Double.parseDouble((String) attributes.get("PriceAtTerminationDate")));
                map.put("MarketObjectCodeOfScalingIndex",attributes.get("MarketObjectCodeOfScalingIndex"));
                map.put("ScalingIndexAtStatusDate",(CommonUtils.isNull(attributes.get("ScalingIndexAtStatusDate")))? 0.0 : Double.parseDouble((String) attributes.get("ScalingIndexAtStatusDate")));
                map.put("CycleAnchorDateOfScalingIndex",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfScalingIndex")))? ( (CommonUtils.isNull(attributes.get("CycleOfScalingIndex")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfScalingIndex")));
                map.put("CycleOfScalingIndex",attributes.get("CycleOfScalingIndex"));
                map.put("ScalingEffect",attributes.get("ScalingEffect"));
                // TODO: review prepayment mechanism and attributes
                map.put("CycleAnchorDateOfOptionality",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfOptionality")))? ( (CommonUtils.isNull(attributes.get("CycleOfOptionality")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfOptionality")));
                map.put("CycleOfOptionality",attributes.get("CycleOfOptionality"));
                map.put("PenaltyType",(CommonUtils.isNull(attributes.get("PenaltyType")))? 'N' : ((String) attributes.get("PenaltyType")).charAt(0));
                map.put("PenaltyRate",(CommonUtils.isNull(attributes.get("PenaltyRate")))? 0.0 : Double.parseDouble((String) attributes.get("PenaltyRate")));
                map.put("ObjectCodeOfPrepaymentModel",attributes.get("ObjectCodeOfPrepaymentModel"));

                map.put("CycleAnchorDateOfRateReset",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfRateReset")))? ( (CommonUtils.isNull(attributes.get("CycleOfRateReset")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfRateReset")));
                map.put("CycleOfRateReset",attributes.get("CycleOfRateReset"));
                map.put("RateSpread",(CommonUtils.isNull(attributes.get("RateSpread")))? 0.0 : Double.parseDouble((String) attributes.get("RateSpread")));
                map.put("MarketObjectCodeOfRateReset",attributes.get("MarketObjectCodeOfRateReset"));
                map.put("LifeCap",(CommonUtils.isNull(attributes.get("LifeCap")))? Double.POSITIVE_INFINITY : Double.parseDouble((String) attributes.get("LifeCap")));
                map.put("LifeFloor",(CommonUtils.isNull(attributes.get("LifeFloor")))? Double.NEGATIVE_INFINITY : Double.parseDouble((String) attributes.get("LifeFloor")));
                map.put("PeriodCap",(CommonUtils.isNull(attributes.get("PeriodCap")))? Double.POSITIVE_INFINITY : Double.parseDouble((String) attributes.get("PeriodCap")));
                map.put("PeriodFloor",(CommonUtils.isNull(attributes.get("PeriodFloor")))? Double.POSITIVE_INFINITY : Double.parseDouble((String) attributes.get("PeriodFloor")));
                map.put("CyclePointOfRateReset",attributes.get("CyclePointOfRateReset"));
                map.put("FixingDays",attributes.get("FixingDays"));
                map.put("NextResetRate",(CommonUtils.isNull(attributes.get("NextResetRate")))? null : Double.parseDouble((String) attributes.get("NextResetRate")));
                map.put("RateMultiplier",(CommonUtils.isNull(attributes.get("RateMultiplier")))? 0.0 : Double.parseDouble((String) attributes.get("RateMultiplier")));

                // present for LAM, NAM, ANN but not PAM
                map.put("MaturityDate",(CommonUtils.isNull(attributes.get("MaturityDate"))? null : LocalDateTime.parse((String) attributes.get("MaturityDate"))));
                map.put("CycleAnchorDateOfInterestCalculationBase",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestCalculationBase")))? ( (CommonUtils.isNull(attributes.get("CycleOfInterestCalculationBase")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfInterestCalculationBase")));
                map.put("CycleOfInterestCalculationBase", attributes.get("CycleOfInterestCalculationBase"));
                map.put("InterestCalculationBase",attributes.get("InterestCalculationBase"));
                map.put("InterestCalculationBaseAmount",(CommonUtils.isNull(attributes.get("InterestCalculationBaseAmount")))? 0.0 : Double.parseDouble((String) attributes.get("InterestCalculationBaseAmount")));
                map.put("CycleAnchorDateOfPrincipalRedemption",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfPrincipalRedemption")))? LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfPrincipalRedemption")));
                map.put("CycleOfPrincipalRedemption",attributes.get("CycleOfPrincipalRedemption"));
                map.put("NextPrincipalRedemptionPayment",(CommonUtils.isNull(attributes.get("NextPrincipalRedemptionPayment")))? null : Double.parseDouble((String) attributes.get("NextPrincipalRedemptionPayment")));

                // present for ANN but not for LAM, NAM
                map.put("AmortizationDate",(CommonUtils.isNull(attributes.get("AmortizationDate"))? null : LocalDateTime.parse((String) attributes.get("AmortizationDate"))));

                break;
            case StringUtils.ContractType_CLM:

                map.put("Calendar",(!CommonUtils.isNull(attributes.get("Calendar")) && attributes.get("Calendar").equals("MondayToFriday"))? new MondayToFridayCalendar() : new NoHolidaysCalendar());
                map.put("BusinessDayConvention",new BusinessDayAdjuster((String) attributes.get("BusinessDayConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                map.put("EndOfMonthConvention",(CommonUtils.isNull(attributes.get("EndOfMonthConvention")))? "SD" : attributes.get("EndOfMonthConvention"));
                map.put("ContractType",attributes.get("ContractType"));
                map.put("StatusDate",LocalDateTime.parse((String) attributes.get("StatusDate")));
                map.put("ContractRole",attributes.get("ContractRole"));
                map.put("LegalEntityIDCounterparty",attributes.get("LegalEntityIDCounterparty"));
                map.put("CycleAnchorDateOfFee",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfFee")))? ( (CommonUtils.isNull(attributes.get("CycleOfFee")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfFee")));
                map.put("CycleOfFee",attributes.get("CycleOfFee"));
                map.put("FeeBasis",(CommonUtils.isNull(attributes.get("FeeBasis")))? "0" : attributes.get("FeeBasis"));
                map.put("FeeRate",(CommonUtils.isNull(attributes.get("FeeRate")))? 0.0 : Double.parseDouble((String) attributes.get("FeeRate")));
                map.put("FeeAccrued",(CommonUtils.isNull(attributes.get("FeeAccrued")))? 0.0 : Double.parseDouble((String) attributes.get("FeeAccrued")));
                map.put("CycleAnchorDateOfInterestPayment",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestPayment")))? ( (CommonUtils.isNull(attributes.get("CycleOfInterestPayment")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfInterestPayment")));
                map.put("CycleOfInterestPayment",attributes.get("CycleOfInterestPayment"));
                map.put("NominalInterestRate",(CommonUtils.isNull(attributes.get("NominalInterestRate")))? 0.0 : Double.parseDouble((String) attributes.get("NominalInterestRate")));
                map.put("DayCountConvention",new DayCountCalculator((String) attributes.get("DayCountConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                map.put("AccruedInterest",(CommonUtils.isNull(attributes.get("AccruedInterest")))? 0.0 : Double.parseDouble((String) attributes.get("AccruedInterest")));
                map.put("Currency",attributes.get("Currency"));
                map.put("InitialExchangeDate",LocalDateTime.parse((String) attributes.get("InitialExchangeDate")));
                map.put("NotionalPrincipal",Double.parseDouble((String) attributes.get("NotionalPrincipal")));
                map.put("MaturityDate",(CommonUtils.isNull(attributes.get("MaturityDate"))? null : LocalDateTime.parse((String) attributes.get("MaturityDate"))));

                map.put("XDayNotice", attributes.get("XDayNotice"));

                map.put("CycleAnchorDateOfRateReset",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfRateReset")))? ( (CommonUtils.isNull(attributes.get("CycleOfRateReset")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfRateReset")));
                map.put("CycleOfRateReset",attributes.get("CycleOfRateReset"));
                map.put("RateSpread",(CommonUtils.isNull(attributes.get("RateSpread")))? 0.0 : Double.parseDouble((String) attributes.get("RateSpread")));
                map.put("MarketObjectCodeOfRateReset",attributes.get("MarketObjectCodeOfRateReset"));
                map.put("FixingDays",attributes.get("FixingDays"));
                map.put("NextResetRate",(CommonUtils.isNull(attributes.get("NextResetRate")))? null : Double.parseDouble((String) attributes.get("NextResetRate")));
                map.put("RateMultiplier",(CommonUtils.isNull(attributes.get("RateMultiplier")))? 0.0 : Double.parseDouble((String) attributes.get("RateMultiplier")));

                break;
            case StringUtils.ContractType_UMP:

                map.put("Calendar",(!CommonUtils.isNull(attributes.get("Calendar")) && attributes.get("Calendar").equals("MondayToFriday"))? new MondayToFridayCalendar() : new NoHolidaysCalendar());
                map.put("BusinessDayConvention",new BusinessDayAdjuster((String) attributes.get("BusinessDayConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                map.put("EndOfMonthConvention",(CommonUtils.isNull(attributes.get("EndOfMonthConvention")))? "SD" : attributes.get("EndOfMonthConvention"));
                map.put("ContractType",attributes.get("ContractType"));
                map.put("StatusDate",LocalDateTime.parse((String) attributes.get("StatusDate")));
                map.put("ContractRole",attributes.get("ContractRole"));
                map.put("LegalEntityIDCounterparty",attributes.get("LegalEntityIDCounterparty"));
                map.put("CycleAnchorDateOfFee",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfFee")))? ( (CommonUtils.isNull(attributes.get("CycleOfFee")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfFee")));
                map.put("CycleOfFee",attributes.get("CycleOfFee"));
                map.put("FeeBasis",(CommonUtils.isNull(attributes.get("FeeBasis")))? "0" : attributes.get("FeeBasis"));
                map.put("FeeRate",(CommonUtils.isNull(attributes.get("FeeRate")))? 0.0 : Double.parseDouble((String) attributes.get("FeeRate")));
                map.put("FeeAccrued",(CommonUtils.isNull(attributes.get("FeeAccrued")))? 0.0 : Double.parseDouble((String) attributes.get("FeeAccrued")));
                map.put("CycleAnchorDateOfInterestPayment",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestPayment")))? ( (CommonUtils.isNull(attributes.get("CycleOfInterestPayment")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfInterestPayment")));
                map.put("CycleOfInterestPayment",attributes.get("CycleOfInterestPayment"));
                map.put("NominalInterestRate",(CommonUtils.isNull(attributes.get("NominalInterestRate")))? 0.0 : Double.parseDouble((String) attributes.get("NominalInterestRate")));
                map.put("DayCountConvention",new DayCountCalculator((String) attributes.get("DayCountConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                map.put("AccruedInterest",(CommonUtils.isNull(attributes.get("AccruedInterest")))? 0.0 : Double.parseDouble((String) attributes.get("AccruedInterest")));
                map.put("Currency",attributes.get("Currency"));
                map.put("InitialExchangeDate",LocalDateTime.parse((String) attributes.get("InitialExchangeDate")));
                map.put("NotionalPrincipal",Double.parseDouble((String) attributes.get("NotionalPrincipal")));
                map.put("TerminationDate",(CommonUtils.isNull(attributes.get("TerminationDate")))? null : LocalDateTime.parse((String) attributes.get("TerminationDate")));
                map.put("PriceAtTerminationDate",(CommonUtils.isNull(attributes.get("PriceAtTerminationDate")))? 0.0 : Double.parseDouble((String) attributes.get("PriceAtTerminationDate")));

                map.put("XDayNotice", attributes.get("XDayNotice"));
                map.put("MaximumPenaltyFreeDisbursement", (CommonUtils.isNull(attributes.get("MaximumPenaltyFreeDisbursement")))? attributes.get("NotionalPrincipal") : attributes.get("MaximumPenaltyFreeDisbursement"));

                map.put("CycleAnchorDateOfRateReset",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfRateReset")))? ( (CommonUtils.isNull(attributes.get("CycleOfRateReset")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfRateReset")));
                map.put("CycleOfRateReset",attributes.get("CycleOfRateReset"));
                map.put("RateSpread",(CommonUtils.isNull(attributes.get("RateSpread")))? 0.0 : Double.parseDouble((String) attributes.get("RateSpread")));
                map.put("MarketObjectCodeOfRateReset",attributes.get("MarketObjectCodeOfRateReset"));
                map.put("FixingDays",attributes.get("FixingDays"));
                map.put("NextResetRate",(CommonUtils.isNull(attributes.get("NextResetRate")))? null : Double.parseDouble((String) attributes.get("NextResetRate")));
                map.put("RateMultiplier",(CommonUtils.isNull(attributes.get("RateMultiplier")))? 1.0 : Double.parseDouble((String) attributes.get("RateMultiplier")));

                break;
            case StringUtils.ContractType_CSH:

                map.put("ContractType",attributes.get("ContractType"));
                map.put("StatusDate",LocalDateTime.parse((String) attributes.get("StatusDate")));
                map.put("ContractRole",attributes.get("ContractRole"));
                map.put("Currency",attributes.get("Currency"));
                map.put("NotionalPrincipal",Double.parseDouble((String) attributes.get("NotionalPrincipal")));

                break;
            case StringUtils.ContractType_COM: // almost identical with STK

                map.put("ContractType",attributes.get("ContractType"));
                map.put("StatusDate",LocalDateTime.parse((String) attributes.get("StatusDate")));
                map.put("ContractRole",attributes.get("ContractRole"));
                map.put("LegalEntityIDCounterparty",attributes.get("LegalEntityIDCounterparty"));

                map.put("MarketObjectCode",attributes.get("MarketObjectCode"));

                map.put("Currency",attributes.get("Currency"));
                map.put("Quantity",(CommonUtils.isNull(attributes.get("Quantity")))? 1 : Integer.parseInt((String) attributes.get("Quantity")));
                map.put("PurchaseDate",(CommonUtils.isNull(attributes.get("PurchaseDate")))? null : LocalDateTime.parse((String) attributes.get("PurchaseDate")));
                map.put("PriceAtPurchaseDate",(CommonUtils.isNull(attributes.get("PriceAtPurchaseDate")))? 0.0 : Double.parseDouble((String) attributes.get("PriceAtPurchaseDate")));
                map.put("TerminationDate",(CommonUtils.isNull(attributes.get("TerminationDate")))? null : LocalDateTime.parse((String) attributes.get("TerminationDate")));
                map.put("PriceAtTerminationDate",(CommonUtils.isNull(attributes.get("PriceAtTerminationDate")))? 0.0 : Double.parseDouble((String) attributes.get("PriceAtTerminationDate")));
                map.put("MarketValueObserved",(CommonUtils.isNull(attributes.get("MarketValueObserved")))? 0.0 : Double.parseDouble((String) attributes.get("MarketValueObserved")));

                break;
            case StringUtils.ContractType_STK:

                map.put("ContractType",attributes.get("ContractType"));
                map.put("StatusDate",LocalDateTime.parse((String) attributes.get("StatusDate")));
                map.put("ContractRole",attributes.get("ContractRole"));
                map.put("LegalEntityIDCounterparty",attributes.get("LegalEntityIDCounterparty"));

                map.put("MarketObjectCode",attributes.get("MarketObjectCode"));

                map.put("Currency",attributes.get("Currency"));
                map.put("Quantity",(CommonUtils.isNull(attributes.get("Quantity")))? 1 : Integer.parseInt((String) attributes.get("Quantity")));
                map.put("PurchaseDate",(CommonUtils.isNull(attributes.get("PurchaseDate")))? null : LocalDateTime.parse((String) attributes.get("PurchaseDate")));
                map.put("PriceAtPurchaseDate",(CommonUtils.isNull(attributes.get("PriceAtPurchaseDate")))? 0.0 : Double.parseDouble((String) attributes.get("PriceAtPurchaseDate")));
                map.put("TerminationDate",(CommonUtils.isNull(attributes.get("TerminationDate")))? null : LocalDateTime.parse((String) attributes.get("TerminationDate")));
                map.put("PriceAtTerminationDate",(CommonUtils.isNull(attributes.get("PriceAtTerminationDate")))? 0.0 : Double.parseDouble((String) attributes.get("PriceAtTerminationDate")));
                map.put("MarketValueObserved",(CommonUtils.isNull(attributes.get("MarketValueObserved")))? 0.0 : Double.parseDouble((String) attributes.get("MarketValueObserved")));

                // present for STK but not COM
                map.put("Calendar",(!CommonUtils.isNull(attributes.get("Calendar")) && attributes.get("Calendar").equals("MondayToFriday"))? new MondayToFridayCalendar() : new NoHolidaysCalendar());
                map.put("BusinessDayConvention",new BusinessDayAdjuster((String) attributes.get("BusinessDayConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                map.put("EndOfMonthConvention",(CommonUtils.isNull(attributes.get("EndOfMonthConvention")))? "SD" : attributes.get("EndOfMonthConvention"));
                map.put("CycleAnchorDateOfDividendPayment",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfDividendPayment")))? ( (CommonUtils.isNull(attributes.get("CycleOfDividendPayment")))? null : LocalDateTime.parse((String) attributes.get("PurchaseDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfDividendPayment")));
                map.put("CycleOfDividendPayment",attributes.get("CycleOfDividendPayment"));
                map.put("MarketObjectCodeOfDividendRate",attributes.get("MarketObjectCodeOfDividendRate"));

                break;
            case StringUtils.ContractType_FXOUT:

                map.put("Calendar",(!CommonUtils.isNull(attributes.get("Calendar")) && attributes.get("Calendar").equals("MondayToFriday"))? new MondayToFridayCalendar() : new NoHolidaysCalendar());
                map.put("BusinessDayConvention",new BusinessDayAdjuster((String) attributes.get("BusinessDayConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                map.put("EndOfMonthConvention",(CommonUtils.isNull(attributes.get("EndOfMonthConvention")))? "SD" : attributes.get("EndOfMonthConvention"));
                map.put("ContractType",attributes.get("ContractType"));
                map.put("StatusDate",LocalDateTime.parse((String) attributes.get("StatusDate")));
                map.put("ContractRole",attributes.get("ContractRole"));
                map.put("LegalEntityIDCounterparty",attributes.get("LegalEntityIDCounterparty"));

                map.put("MarketObjectCode",attributes.get("MarketObjectCode"));

                map.put("Currency",attributes.get("Currency"));
                map.put("Currency2",attributes.get("Currency2"));
                map.put("MaturityDate",LocalDateTime.parse((String) attributes.get("MaturityDate")));
                map.put("NotionalPrincipal",Double.parseDouble((String) attributes.get("NotionalPrincipal")));
                map.put("NotionalPrincipal2",Double.parseDouble((String) attributes.get("NotionalPrincipal2")));
                map.put("PurchaseDate",(CommonUtils.isNull(attributes.get("PurchaseDate")))? null : LocalDateTime.parse((String) attributes.get("PurchaseDate")));
                map.put("PriceAtPurchaseDate",(CommonUtils.isNull(attributes.get("PriceAtPurchaseDate")))? 0.0 : Double.parseDouble((String) attributes.get("PriceAtPurchaseDate")));
                map.put("TerminationDate",(CommonUtils.isNull(attributes.get("TerminationDate")))? null : LocalDateTime.parse((String) attributes.get("TerminationDate")));
                map.put("PriceAtTerminationDate",(CommonUtils.isNull(attributes.get("PriceAtTerminationDate")))? 0.0 : Double.parseDouble((String) attributes.get("PriceAtTerminationDate")));
                map.put("DeliverySettlement",attributes.get("DeliverySettlement"));
                map.put("SettlementDate",(CommonUtils.isNull(attributes.get("SettlementDate")))? null : LocalDateTime.parse((String) attributes.get("SettlementDate")));

                break;
            case StringUtils.ContractType_SWPPV:
                map.put("Calendar",(!CommonUtils.isNull(attributes.get("Calendar")) && attributes.get("Calendar").equals("MondayToFriday"))? new MondayToFridayCalendar() : new NoHolidaysCalendar());
                map.put("BusinessDayConvention",new BusinessDayAdjuster((String) attributes.get("BusinessDayConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                map.put("EndOfMonthConvention",(CommonUtils.isNull(attributes.get("EndOfMonthConvention")))? "SD" : attributes.get("EndOfMonthConvention"));
                map.put("ContractType",attributes.get("ContractType"));
                map.put("StatusDate",LocalDateTime.parse((String) attributes.get("StatusDate")));
                map.put("ContractRole",attributes.get("ContractRole"));
                map.put("LegalEntityIDCounterparty",attributes.get("LegalEntityIDCounterparty"));

                map.put("MarketObjectCode",attributes.get("MarketObjectCode"));

                map.put("CycleAnchorDateOfInterestPayment",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestPayment")))? ( (CommonUtils.isNull(attributes.get("CycleOfInterestPayment")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfInterestPayment")));
                map.put("CycleOfInterestPayment",attributes.get("CycleOfInterestPayment"));
                map.put("NominalInterestRate",(CommonUtils.isNull(attributes.get("NominalInterestRate")))? 0.0 : Double.parseDouble((String) attributes.get("NominalInterestRate")));
                map.put("DayCountConvention",new DayCountCalculator((String) attributes.get("DayCountConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                map.put("AccruedInterest",(CommonUtils.isNull(attributes.get("AccruedInterest")))? 0.0 : Double.parseDouble((String) attributes.get("AccruedInterest")));
                map.put("CapitalizationEndDate",(CommonUtils.isNull(attributes.get("CapitalizationEndDate")))? null : LocalDateTime.parse((String) attributes.get("CapitalizationEndDate")));
                map.put("CyclePointOfInterestPayment",attributes.get("CyclePointOfInterestPayment"));
                map.put("Currency",attributes.get("Currency"));
                map.put("InitialExchangeDate",LocalDateTime.parse((String) attributes.get("InitialExchangeDate")));
                map.put("PremiumDiscountAtIED",(CommonUtils.isNull(attributes.get("PremiumDiscountAtIED")))? 0.0 : Double.parseDouble((String) attributes.get("PremiumDiscountAtIED")));
                map.put("NotionalPrincipal",Double.parseDouble((String) attributes.get("NotionalPrincipal")));
                map.put("PurchaseDate",(CommonUtils.isNull(attributes.get("PurchaseDate")))? null : LocalDateTime.parse((String) attributes.get("PurchaseDate")));
                map.put("PriceAtPurchaseDate",(CommonUtils.isNull(attributes.get("PriceAtPurchaseDate")))? 0.0 : Double.parseDouble((String) attributes.get("PriceAtPurchaseDate")));
                map.put("TerminationDate",(CommonUtils.isNull(attributes.get("TerminationDate")))? null : LocalDateTime.parse((String) attributes.get("TerminationDate")));
                map.put("PriceAtTerminationDate",(CommonUtils.isNull(attributes.get("PriceAtTerminationDate")))? 0.0 : Double.parseDouble((String) attributes.get("PriceAtTerminationDate")));
                map.put("CycleAnchorDateOfRateReset",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfRateReset")))? ( (CommonUtils.isNull(attributes.get("CycleOfRateReset")))? null : LocalDateTime.parse((String) attributes.get("InitialExchangeDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfRateReset")));
                map.put("CycleOfRateReset",attributes.get("CycleOfRateReset"));
                map.put("RateSpread",(CommonUtils.isNull(attributes.get("RateSpread")))? 0.0 : Double.parseDouble((String) attributes.get("RateSpread")));
                map.put("MarketObjectCodeOfRateReset",attributes.get("MarketObjectCodeOfRateReset"));
                map.put("CyclePointOfRateReset",attributes.get("CyclePointOfRateReset"));
                map.put("FixingDays",attributes.get("FixingDays"));
                map.put("NextResetRate",(CommonUtils.isNull(attributes.get("NextResetRate")))? null : Double.parseDouble((String) attributes.get("NextResetRate")));
                map.put("RateMultiplier",(CommonUtils.isNull(attributes.get("RateMultiplier")))? 0.0 : Double.parseDouble((String) attributes.get("RateMultiplier")));

                map.put("NominalInterestRate2",(CommonUtils.isNull(attributes.get("NominalInterestRate")))? 0.0 : Double.parseDouble((String) attributes.get("NominalInterestRate")));
                map.put("MaturityDate",LocalDateTime.parse((String) attributes.get("MaturityDate")));
                map.put("DeliverySettlement",attributes.get("DeliverySettlement"));

             break;

            case StringUtils.ContractType_SWAPS:

                // parent attributes
                map.put("StatusDate",LocalDateTime.parse((String) attributes.get("StatusDate")));
                map.put("ContractRole",attributes.get("ContractRole"));
                map.put("LegalEntityIDCounterparty",attributes.get("LegalEntityIDCounterparty"));

                map.put("MarketObjectCode",attributes.get("MarketObjectCode"));

                map.put("Currency",attributes.get("Currency"));
                map.put("PurchaseDate",(CommonUtils.isNull(attributes.get("PurchaseDate")))? null : LocalDateTime.parse((String) attributes.get("PurchaseDate")));
                map.put("PriceAtPurchaseDate",(CommonUtils.isNull(attributes.get("PriceAtPurchaseDate")))? 0.0 : Double.parseDouble((String) attributes.get("PriceAtPurchaseDate")));
                map.put("TerminationDate",(CommonUtils.isNull(attributes.get("TerminationDate")))? null : LocalDateTime.parse((String) attributes.get("TerminationDate")));
                map.put("PriceAtTerminationDate",(CommonUtils.isNull(attributes.get("PriceAtTerminationDate")))? 0.0 : Double.parseDouble((String) attributes.get("PriceAtTerminationDate")));
                map.put("DeliverySettlement",attributes.get("DeliverySettlement"));

                // child attributes
                ContractModelProvider child1 = ContractModel2.parse((Map<String,Object>) attributes.get(((String) attributes.get("ContractID"))+"_C1"));
                ContractModelProvider child2 = ContractModel2.parse((Map<String,Object>) attributes.get(((String) attributes.get("ContractID"))+"_C2"));

                if(map.get("ContractRole").equals("RFL")) {

                } else {

                }
                map.put("Child1",child1);
                map.put("Child2",child2);

                break;

            case StringUtils.ContractType_MRGNG:
                map.put("Calendar",(!CommonUtils.isNull(attributes.get("Calendar")) && attributes.get("Calendar").equals("MondayToFriday"))? new MondayToFridayCalendar() : new NoHolidaysCalendar());
                map.put("BusinessDayConvention",new BusinessDayAdjuster((String) attributes.get("BusinessDayConvention"), (BusinessDayCalendarProvider) map.get("Calendar")));
                map.put("EndOfMonthConvention",(CommonUtils.isNull(attributes.get("EndOfMonthConvention")))? "SD" : attributes.get("EndOfMonthConvention"));
                map.put("ContractType",attributes.get("ContractType"));
                map.put("StatusDate",LocalDateTime.parse((String) attributes.get("StatusDate")));
                map.put("ContractRole",attributes.get("ContractRole"));
                map.put("LegalEntityIDCounterparty",attributes.get("LegalEntityIDCounterparty"));

                map.put("InitialMargin",Double.parseDouble((String) attributes.get("InitialMargin")));
                map.put("MaintenanceMarginLowerBound",(CommonUtils.isNull(attributes.get("MaintenanceMarginLowerBound")))? null : Double.parseDouble((String) attributes.get("MaintenanceMarginLowerBound")));
                map.put("MaintenanceMarginUpperBound",(CommonUtils.isNull(attributes.get("MaintenanceMarginUpperBound")))? null : Double.parseDouble((String) attributes.get("MaintenanceMarginUpperBound")));
                map.put("CycleAnchorDateOfMargining",(CommonUtils.isNull(attributes.get("CycleAnchorDateOfInterestPayment")))? ( (CommonUtils.isNull(attributes.get("CycleOfMargining")))? null : LocalDateTime.parse((String) attributes.get("ContractDealDate")) ) : LocalDateTime.parse((String) attributes.get("CycleAnchorDateOfMargining")));
                map.put("CycleOfMargining",attributes.get("CycleOfMargining"));
                map.put("VariationMargin",Double.parseDouble((String) attributes.get("VariationMargin")));

                map.put("Currency",attributes.get("Currency"));
                map.put("ContractDealDate",LocalDateTime.parse((String) attributes.get("ContractDealDate")));
                map.put("SettlementDate",LocalDateTime.parse((String) attributes.get("SettlementDate")));
                map.put("MarketValueObserved",Double.parseDouble((String) attributes.get("MarketValueObserved")));

                break;


            default:
                throw new ContractTypeUnknownException();
        }
        } catch(Exception e) {
            throw new AttributeConversionException();
        }

        return new ContractModel2(map);
    }
                    
    @Override 
    public <T> T getAs(String name) {
        return (T) attributes.get(name);
    }
}

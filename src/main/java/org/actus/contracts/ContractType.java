/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import org.actus.ContractTypeUnknownException;
import org.actus.AttributeConversionException;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.attributes.ContractModelProvider;
import org.actus.events.ContractEvent;
import org.actus.states.StateSpace;
import org.actus.util.StringUtils;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.Set;
import java.util.ArrayList;

/**
 * A representation of an ACTUS Contract Type algorithm
 * <p>
 * ACTUS Contract Types represent classes of financial instrument payoff functions. This component 
 * is a representation of the algorithm mapping a set of attributes (cf. {@link ContractModelProvider}),
 * an external market model (cf. {@link RiskFactorModelProvider}) and analysis times 
 * (cf. {@code analysisTimes}) onto a vector of contingent contract events (cf. {@link ContractEvent}).
 * </p>
 * @see <a href="https://www.actusfrf.org">ACTUS Website</a>
 */
public final class ContractType {
    
  /**
   * Evaluates the contingent lifecycle of the contract
   * <p>
   *     The set of contract attributes are mapped to a stream of contract events according
   *     to the legal logic of the respective Contract Type and contingent
   *     to the risk factor dynamics provided with the {@link RiskFactorModelProvider}. The
   *     generated stream of contract events contains all post-status date events that occur
   *     during the lifetime of the contract thus resulting in the contract's
   *     risk factor contingent lifecycle.
   * </p>
   * <p>
   *    For each time in argument {@code analysisTimes} an analytical event is added to
   *    the lifecycle with the sole purpose of "collecting" the contract's inner states
   *    at that time. Therefore, the generated analytical events do not affect the
   *    contract's lifecycle.
   * </p>
   * <p>
   *     This method invokes the {@code lifecycle(Set<LocalDateTime> analysisTimes,ContractModel model, RiskFactorModelProvider riskFactorModel)}
   *     method of the respective Contract Type-class as indicated by the {@code ContractType} attribute.
   *     If the {@code ContractType} attribute cannot be resolved to an ACTUS Contract Type the method
   *     throws a {@link ContractTypeUnknownException}.
   * </p>
   * 
   * @param analysisTimes  a series of analysis times
   * @param model the model carrying the contract attributes
   * @param riskFactorModel an external model of the risk factor dynamics
   * @return the contract's contingent lifecycle
   * @throws ContractTypeUnknownException if the provided ContractType field in the {@link ContractModelProvider} cannot be resolved
   * @throws AttributeConversionException if and attribute in {@link ContractModelProvider} cannot be converted to its target data type
   * 
   */
  public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                   ContractModelProvider model,
                                                   RiskFactorModelProvider riskFactorModel) throws ContractTypeUnknownException,AttributeConversionException {
            switch((String) model.getAs("ContractType")) {
                case StringUtils.ContractType_PAM: 
                    return PrincipalAtMaturity.lifecycle(analysisTimes,model,riskFactorModel);
                case StringUtils.ContractType_LAM: 
                    return LinearAmortizer.lifecycle(analysisTimes,model,riskFactorModel);
                case StringUtils.ContractType_NAM: 
                    return NegativeAmortizer.lifecycle(analysisTimes,model,riskFactorModel);
                case StringUtils.ContractType_ANN: 
                    return Annuity.lifecycle(analysisTimes,model,riskFactorModel);
                case StringUtils.ContractType_CLM: 
                    return CallMoney.lifecycle(analysisTimes,model,riskFactorModel);
                case StringUtils.ContractType_UMP:
                    return UndefinedMaturityProfile.lifecycle(analysisTimes,model,riskFactorModel);
                case StringUtils.ContractType_CSH: 
                    return Cash.lifecycle(analysisTimes,model,riskFactorModel);
                case StringUtils.ContractType_STK: 
                    return Stock.lifecycle(analysisTimes,model,riskFactorModel);
                case StringUtils.ContractType_COM: 
                    return Commodity.lifecycle(analysisTimes,model,riskFactorModel);
                case StringUtils.ContractType_FXOUT: 
                    return ForeignExchangeOutright.lifecycle(analysisTimes,model,riskFactorModel);
                case StringUtils.ContractType_SWPPV:
                    return PlainVanillaInterestRateSwap.lifecycle(analysisTimes,model,riskFactorModel);
                case StringUtils.ContractType_SWAPS:
                    return Swap.lifecycle(analysisTimes,model,riskFactorModel);
                default:
                    throw new ContractTypeUnknownException();
            }
  }

    /**
     * Evaluates the contingent payoff of the contract
     * <p>
     *     The set of contract attributes are mapped to a stream of transaction events according
     *     to the legal logic of the respective Contract Type and contingent
     *     to the risk factor dynamics provided with the {@link RiskFactorModelProvider}. The
     *     generated stream of transaction events contains all post-status date transaction events that 
     *     occur during the lifetime of the contract thus resulting in the contract's
     *     risk factor contingent payoff.
     * </p>
     * <p>
     *     Transaction events are a subset of contract events evaluated through the {@code lifecycle} 
     *     methods that trigger actual transactions of units of assets (for financial contracts
     *     strictly cash in some currency) from one party to another. Therefore, a contract's
     *     {@code payoff} evaluates to a subset of contract events from its {@code lifecycle}.
     * </p>
     * <p>
     *    For each time in argument {@code analysisTimes} an analytical event is added to
     *    the lifecycle with the sole purpose of "collecting" the contract's inner states
     *    at that time. Therefore, the generated analytical events do not affect the
     *    contract's lifecycle.
     * </p>
     * <p>
     *     This method invokes the {@code payoff(Set<LocalDateTime> analysisTimes,ContractModel model, RiskFactorModelProvider riskFactorModel)}
     *     method of the respective Contract Type-class as indicated by the {@code ContractType} attribute.
     *     If the {@code ContractType} attribute cannot be resolved to an ACTUS Contract Type the method
     *     throws a {@link ContractTypeUnknownException}.
     * </p>
     *
     * @param analysisTimes  a series of analysis times
     * @param model the model carrying the contract attributes
     * @param riskFactorModel an external model of the risk factor dynamics
     * @return the contract's contingent payoff
     * @throws ContractTypeUnknownException if the provided ContractType field in the {@link ContractModelProvider} cannot be resolved
     * @throws AttributeConversionException if and attribute in {@link ContractModelProvider} cannot be converted to its target data type
     *
     */
    public static ArrayList<ContractEvent> payoff(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws ContractTypeUnknownException,AttributeConversionException {
        switch((String) model.getAs("ContractType")) {
            case StringUtils.ContractType_PAM:
                return PrincipalAtMaturity.payoff(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_LAM:
                return LinearAmortizer.payoff(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_NAM:
                return NegativeAmortizer.payoff(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_ANN:
                return Annuity.payoff(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_CLM:
                return CallMoney.payoff(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_UMP:
                return UndefinedMaturityProfile.payoff(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_CSH:
                return Cash.payoff(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_STK:
                return Stock.payoff(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_COM:
                return Commodity.payoff(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_FXOUT:
                return ForeignExchangeOutright.payoff(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_SWPPV:
                return PlainVanillaInterestRateSwap.payoff(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_SWAPS:
                return Swap.payoff(analysisTimes,model,riskFactorModel);
            default:
                throw new ContractTypeUnknownException();
        }
    }

    /**
     * Evaluates the contract's schedule
     * <p>
     *     The set of contract attributes are mapped to the schedule of the contract according
     *     to the legal logic of the respective Contract Type. The
     *     generated contract schedule contains all post-status date events that occur
     *     during the lifetime of the contract. The events are evaluated up to the first
     *     contingent event.
     * </p>
     * <p>
     *     This method invokes the {@code schedule(ContractModel model)}
     *     method of the respective Contract Type-class as indicated by the {@code ContractType} attribute.
     *     If the {@code ContractType} attribute cannot be resolved to an ACTUS Contract Type the method
     *     throws a {@link ContractTypeUnknownException}.
     * </p>
     *
     * @param model the model carrying the contract attributes
     * @return the non-contingent part of the contract's lifecycle
     * @throws ContractTypeUnknownException if the provided ContractType field in the {@link ContractModelProvider} cannot be resolved
     * @throws AttributeConversionException if and attribute in {@link ContractModelProvider} cannot be converted to its target data type
     *
     */
    public static ArrayList<ContractEvent> schedule(ContractModelProvider model) throws ContractTypeUnknownException,AttributeConversionException {
        switch((String) model.getAs("ContractType")) {
            case StringUtils.ContractType_PAM:
                return PrincipalAtMaturity.schedule(model);
            case StringUtils.ContractType_LAM:
                return LinearAmortizer.schedule(model);
            case StringUtils.ContractType_NAM:
                return NegativeAmortizer.schedule(model);
            case StringUtils.ContractType_ANN:
                return Annuity.schedule(model);
            case StringUtils.ContractType_CLM:
                return CallMoney.schedule(model);
            case StringUtils.ContractType_UMP:
                return UndefinedMaturityProfile.schedule(model);
            case StringUtils.ContractType_CSH:
                return Cash.schedule(model);
            case StringUtils.ContractType_STK:
                return Stock.schedule(model);
            case StringUtils.ContractType_COM:
                return Commodity.schedule(model);
            case StringUtils.ContractType_FXOUT:
                return ForeignExchangeOutright.schedule(model);
            case StringUtils.ContractType_SWPPV:
                return PlainVanillaInterestRateSwap.schedule(model);
            case StringUtils.ContractType_SWAPS:
                return Swap.schedule(model);
            default:
                throw new ContractTypeUnknownException();
        }
    }

    /**
     * Evaluates the next contract events within a certain time period
     * <p>
     *     The set of contract attributes are mapped to the stream of next contract events
     *     within a specified time period according
     *     to the legal logic of the respective Contract Type and contingent
     *     to the risk factor dynamics provided with the {@link RiskFactorModelProvider}.
     *     The contract's {@code StatusDate} is used as the reference time as from which
     *     the {@code within} period is evaluated.
     * </p>
     * <p>
     *     Note, the stream of the next non-contingent contract events matches the portion
     *     of the stream of the next contingent events up to the first contingent event.
     *     Further, for a contract with purely non-contingent events
     *     (e.g. a {@link PrincipalAtMaturity} without {@code RateReset}, {@code Scaling},
     *     {@code CreditDefault}, etc.) contingent and non-contingent event streams are
     *     the same.
     * </p>
     * <p>
     *     If the {@code ContractType} attribute cannot be resolved to an ACTUS Contract Type the method
     *     throws a {@link ContractTypeUnknownException}.
     * </p>
     *
     * @param within the period within the 'next' events are to be evaluated
     * @param model the model carrying the contract attributes
     * @return the next 'n' contract events
     * @throws ContractTypeUnknownException if the provided ContractType field in the {@link ContractModelProvider} cannot be resolved
     * @throws AttributeConversionException if and attribute in {@link ContractModelProvider} cannot be converted to its target data type
     *
     */
    public static ArrayList<ContractEvent> next(Period within,
                                                ContractModelProvider model) throws ContractTypeUnknownException,AttributeConversionException {
        switch((String) model.getAs("ContractType")) {
            case StringUtils.ContractType_PAM:
                return PrincipalAtMaturity.next(within,model);
            case StringUtils.ContractType_LAM:
                return LinearAmortizer.next(within,model);
            case StringUtils.ContractType_NAM:
                return NegativeAmortizer.next(within,model);
            case StringUtils.ContractType_ANN:
                return Annuity.next(within,model);
            case StringUtils.ContractType_CLM:
                return CallMoney.next(within,model);
            case StringUtils.ContractType_UMP:
                return UndefinedMaturityProfile.next(within,model);
            case StringUtils.ContractType_CSH:
                return Cash.next(within,model);
            case StringUtils.ContractType_STK:
                return Stock.next(within,model);
            case StringUtils.ContractType_COM:
                return Commodity.next(within,model);
            case StringUtils.ContractType_FXOUT:
                return ForeignExchangeOutright.next(within,model);
            case StringUtils.ContractType_SWPPV:
                return PlainVanillaInterestRateSwap.next(within,model);
            case StringUtils.ContractType_SWAPS:
                return Swap.next(within,model);
            default:
                throw new ContractTypeUnknownException();
        }
    }

    /**
     * Applies a Set of contract events to the current state of the contract
     * <p>
     *     The {@code Set} of {@link ContractEvent}s is applied to the current contract
     *     state (i.e. as per attribute {@code StatusDate} in the {@link org.actus.attributes.ContractModel})
     *     in timely sequence of the provided events. The {@link StateSpace} carrying the
     *     contract's post-events state is returned.
     * </p>
     * <p>
     *     If the {@code ContractType} attribute cannot be resolved to an ACTUS Contract Type the method
     *     throws a {@link ContractTypeUnknownException}.
     * </p>
     * <p>
     *     Note, this method is not yet available for Contract Type {@link Swap} and
     *     throws a {@link ContractTypeUnknownException} if attempted to be evaluated for the same.
     * </p>
     *
     * @param events a Set of contract events that should be applied in time sequence
     * @param model the model carrying the contract attributes
     * @return the post-events contract StateSpace
     * @throws ContractTypeUnknownException if the provided ContractType field in the {@link ContractModelProvider} cannot be resolved
     * @throws AttributeConversionException if and attribute in {@link ContractModelProvider} cannot be converted to its target data type
     *
     */
    public static StateSpace apply(Set<ContractEvent> events,
                                   ContractModelProvider model) throws ContractTypeUnknownException,AttributeConversionException {
        switch((String) model.getAs("ContractType")) {
            case StringUtils.ContractType_PAM:
                return PrincipalAtMaturity.apply(events,model);
            case StringUtils.ContractType_LAM:
                return LinearAmortizer.apply(events,model);
            case StringUtils.ContractType_NAM:
                return NegativeAmortizer.apply(events,model);
            case StringUtils.ContractType_ANN:
                return Annuity.apply(events,model);
            case StringUtils.ContractType_CLM:
                return CallMoney.apply(events,model);
            case StringUtils.ContractType_UMP:
                return UndefinedMaturityProfile.apply(events,model);
            case StringUtils.ContractType_CSH:
                return Cash.apply(events,model);
            case StringUtils.ContractType_STK:
                return Stock.apply(events,model);
            case StringUtils.ContractType_COM:
                return Commodity.apply(events,model);
            case StringUtils.ContractType_FXOUT:
                return ForeignExchangeOutright.apply(events,model);
            case StringUtils.ContractType_SWPPV:
                return PlainVanillaInterestRateSwap.apply(events,model);
            case StringUtils.ContractType_SWAPS:
                // TODO: implement (see also Swap class)
            default:
                throw new ContractTypeUnknownException();
        }
    }
}

/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import org.actus.ContractTypeUnknownException;
import org.actus.AttributeConversionException;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.externals.ContractModelProvider;
import org.actus.events.ContractEvent;
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
 * @see <a href="http://www.projectactus.org">ACTUS</a>
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
                default:
                    throw new ContractTypeUnknownException();
            }
  }

    /**
     * Evaluates the non-contingent part of the contract's lifecycle
     * <p>
     *     The set of contract attributes are mapped to a stream of contract events according
     *     to the legal logic of the respective Contract Type. The
     *     generated stream of contract events contains all post-status date events that occur
     *     during the lifetime of the contract but are not contingent upon any risk factor
     *     dynamics thus resulting in the contract's non-contingent lifecycle.
     * </p>
     * <p>
     *     The non-contingent contract lifecycle evaluates the legal logic of the respective
     *     Contract Type along the contract's lifetime up to the point in time where the first
     *     risk factor contingent contract event occurs. Therefore, the non-contingent contract
     *     lifecycle matches the portion of the contingent lifecycle up to the first contingent
     *     event. Further, for a contract with purely non-contingent events (e.g. a
     *     {@link PrincipalAtMaturity} without {@code RateReset}, {@code Scaling},
     *     {@code CreditDefault}, etc.) contingent and non-contingent lifecycle are the same.
     * </p>
     * <p>
     *    For each time in argument {@code analysisTimes} an analytical event is added to
     *    the lifecycle with the sole purpose of "collecting" the contract's inner states
     *    at that time. Therefore, the generated analytical events do not affect the
     *    contract's lifecycle.
     * </p>
     * <p>
     *     This method invokes the {@code lifecycle(Set<LocalDateTime> analysisTimes,ContractModel model)}
     *     method of the respective Contract Type-class as indicated by the {@code ContractType} attribute.
     *     If the {@code ContractType} attribute cannot be resolved to an ACTUS Contract Type the method
     *     throws a {@link ContractTypeUnknownException}.
     * </p>
     *
     * @param analysisTimes  a series of analysis times
     * @param model the model carrying the contract attributes
     * @return the non-contingent part of the contract's lifecycle
     * @throws ContractTypeUnknownException if the provided ContractType field in the {@link ContractModelProvider} cannot be resolved
     * @throws AttributeConversionException if and attribute in {@link ContractModelProvider} cannot be converted to its target data type
     *
     */
    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model) throws ContractTypeUnknownException,AttributeConversionException {
        switch((String) model.getAs("ContractType")) {
            case StringUtils.ContractType_PAM:
                return PrincipalAtMaturity.lifecycle(analysisTimes,model);
            case StringUtils.ContractType_LAM:
                return LinearAmortizer.lifecycle(analysisTimes,model);
            case StringUtils.ContractType_NAM:
                return NegativeAmortizer.lifecycle(analysisTimes,model);
            case StringUtils.ContractType_ANN:
                return Annuity.lifecycle(analysisTimes,model);
            case StringUtils.ContractType_CLM:
                return CallMoney.lifecycle(analysisTimes,model);
            case StringUtils.ContractType_CSH:
                return Cash.lifecycle(analysisTimes,model);
            case StringUtils.ContractType_STK:
                return Stock.lifecycle(analysisTimes,model);
            case StringUtils.ContractType_COM:
                return Commodity.lifecycle(analysisTimes,model);
            case StringUtils.ContractType_FXOUT:
                return ForeignExchangeOutright.lifecycle(analysisTimes,model);
            case StringUtils.ContractType_SWPPV:
                return PlainVanillaInterestRateSwap.lifecycle(analysisTimes,model);
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
            default:
                throw new ContractTypeUnknownException();
        }
    }

    /**
     * Evaluates the non-contingent part of the contract's payoff
     * <p>
     *     The set of contract attributes are mapped to a stream of transaction events according
     *     to the legal logic of the respective Contract Type. The
     *     generated stream of transaction events contains all post-status date events that occur
     *     during the lifetime of the contract but are not contingent upon any risk factor
     *     dynamics thus resulting in the contract's non-contingent payoff.
     * </p>
     * <p>
     *     The non-contingent contract payoff evaluates the legal logic of the respective
     *     Contract Type along the contract's lifetime up to the point in time where the first
     *     risk factor contingent contract event (note, this can be any contract event, not only
     *     transaction events) occurs. Therefore, the non-contingent contract
     *     payoff matches the portion of the contingent payoff up to the first contingent
     *     event. Further, for a contract with purely non-contingent events (e.g. a
     *     {@link PrincipalAtMaturity} without {@code RateReset}, {@code Scaling},
     *     {@code CreditDefault}, etc.) contingent and non-contingent payoff are the same.
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
     *    contract's payoff.
     * </p>
     * <p>
     *     This method invokes the {@code payoff(Set<LocalDateTime> analysisTimes,ContractModel model)}
     *     method of the respective Contract Type-class as indicated by the {@code ContractType} attribute.
     *     If the {@code ContractType} attribute cannot be resolved to an ACTUS Contract Type the method
     *     throws a {@link ContractTypeUnknownException}.
     * </p>
     *
     * @param analysisTimes  a series of analysis times
     * @param model the model carrying the contract attributes
     * @return the non-contingent part of the contract's payoff
     * @throws ContractTypeUnknownException if the provided ContractType field in the {@link ContractModelProvider} cannot be resolved
     * @throws AttributeConversionException if and attribute in {@link ContractModelProvider} cannot be converted to its target data type
     *
     */
    public static ArrayList<ContractEvent> payoff(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model) throws ContractTypeUnknownException,AttributeConversionException {
        switch((String) model.getAs("ContractType")) {
            case StringUtils.ContractType_PAM:
                return PrincipalAtMaturity.payoff(analysisTimes,model);
            case StringUtils.ContractType_LAM:
                return LinearAmortizer.payoff(analysisTimes,model);
            case StringUtils.ContractType_NAM:
                return NegativeAmortizer.payoff(analysisTimes,model);
            case StringUtils.ContractType_ANN:
                return Annuity.payoff(analysisTimes,model);
            case StringUtils.ContractType_CLM:
                return CallMoney.payoff(analysisTimes,model);
            case StringUtils.ContractType_CSH:
                return Cash.payoff(analysisTimes,model);
            case StringUtils.ContractType_STK:
                return Stock.payoff(analysisTimes,model);
            case StringUtils.ContractType_COM:
                return Commodity.payoff(analysisTimes,model);
            case StringUtils.ContractType_FXOUT:
                return ForeignExchangeOutright.payoff(analysisTimes,model);
            case StringUtils.ContractType_SWPPV:
                return PlainVanillaInterestRateSwap.payoff(analysisTimes,model);
            default:
                throw new ContractTypeUnknownException();
        }
    }

    /**
     * Evaluates all contract events within a specified time window
     * <p>
     *     The set of contract attributes are mapped to a stream of contract events according
     *     to the legal logic of the respective Contract Type and contingent
     *     to the risk factor dynamics provided with the {@link RiskFactorModelProvider}. The
     *     generated stream of contract events contains all post-status date events that occur
     *     within the window of times specified.
     * </p>
     * <p>
     *     The window of contract events is taken as the time period from time 
     *     {@code analysisTimes.stream().min(Comparator.naturalOrder())} to time
     *     {@code analysisTimes.stream().max(Comparator.naturalOrder())} including
     *     limits.
     * </p>
     * <p>
     *    For each time in argument {@code analysisTimes} an analytical event is added to
     *    the lifecycle with the sole purpose of "collecting" the contract's inner states
     *    at that time. Therefore, the generated analytical events do not affect the
     *    contract's lifecycle.
     * </p>
     * <p>
     *     This method invokes the {@code events(Set<LocalDateTime> analysisTimes,ContractModel model, RiskFactorModelProvider riskFactorModel)}
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
    public static ArrayList<ContractEvent> events(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws ContractTypeUnknownException,AttributeConversionException {
        switch((String) model.getAs("ContractType")) {
            case StringUtils.ContractType_PAM:
                return PrincipalAtMaturity.events(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_LAM:
                return LinearAmortizer.events(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_NAM:
                return NegativeAmortizer.events(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_ANN:
                return Annuity.events(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_CLM:
                return CallMoney.events(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_CSH:
                return Cash.events(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_STK:
                return Stock.events(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_COM:
                return Commodity.events(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_FXOUT:
                return ForeignExchangeOutright.events(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_SWPPV:
                return PlainVanillaInterestRateSwap.events(analysisTimes,model,riskFactorModel);
            default:
                throw new ContractTypeUnknownException();
        }
    }

    /**
     * Evaluates the non-contingent contract events within a time window
     * <p>
     *     The set of contract attributes are mapped to a stream of contract events according
     *     to the legal logic of the respective Contract Type. The
     *     generated stream of contract events contains all post-status date events that occur
     *     within the window of times specified.
     * </p>
     * <p>
     *     The window of times is taken as the time period from time 
     *     {@code analysisTimes.stream().min(Comparator.naturalOrder())} to time
     *     {@code analysisTimes.stream().max(Comparator.naturalOrder())} including
     *     limits.
     * </p>
     * <p>
     *     The non-contingent window of contract events evaluates the legal logic of the respective
     *     Contract Type within the time window up to the point in time where the first
     *     risk factor contingent contract event occurs. Therefore, the non-contingent window of
     *     contract events matches the portion of the contingent window of contract events up to 
     *     the first contingent event. Further, for a contract with purely non-contingent events 
     *     (e.g. a {@link PrincipalAtMaturity} without {@code RateReset}, {@code Scaling},
     *     {@code CreditDefault}, etc.) contingent and non-contingent window of contract events are 
     *     the same.
     * </p>
     * <p>
     *    For each time in argument {@code analysisTimes} an analytical event is added to
     *    the lifecycle with the sole purpose of "collecting" the contract's inner states
     *    at that time. Therefore, the generated analytical events do not affect the
     *    contract's lifecycle.
     * </p>
     * <p>
     *     This method invokes the {@code events(Set<LocalDateTime> analysisTimes,ContractModel model)}
     *     method of the respective Contract Type-class as indicated by the {@code ContractType} attribute.
     *     If the {@code ContractType} attribute cannot be resolved to an ACTUS Contract Type the method
     *     throws a {@link ContractTypeUnknownException}.
     * </p>
     *
     * @param analysisTimes  a series of analysis times
     * @param model the model carrying the contract attributes
     * @return the non-contingent part of the contract's lifecycle
     * @throws ContractTypeUnknownException if the provided ContractType field in the {@link ContractModelProvider} cannot be resolved
     * @throws AttributeConversionException if and attribute in {@link ContractModelProvider} cannot be converted to its target data type
     *
     */
    public static ArrayList<ContractEvent> events(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model) throws ContractTypeUnknownException,AttributeConversionException {
        switch((String) model.getAs("ContractType")) {
            case StringUtils.ContractType_PAM:
                return PrincipalAtMaturity.events(analysisTimes,model);
            case StringUtils.ContractType_LAM:
                return LinearAmortizer.events(analysisTimes,model);
            case StringUtils.ContractType_NAM:
                return NegativeAmortizer.events(analysisTimes,model);
            case StringUtils.ContractType_ANN:
                return Annuity.events(analysisTimes,model);
            case StringUtils.ContractType_CLM:
                return CallMoney.events(analysisTimes,model);
            case StringUtils.ContractType_CSH:
                return Cash.events(analysisTimes,model);
            case StringUtils.ContractType_STK:
                return Stock.events(analysisTimes,model);
            case StringUtils.ContractType_COM:
                return Commodity.events(analysisTimes,model);
            case StringUtils.ContractType_FXOUT:
                return ForeignExchangeOutright.events(analysisTimes,model);
            case StringUtils.ContractType_SWPPV:
                return PlainVanillaInterestRateSwap.events(analysisTimes,model);
            default:
                throw new ContractTypeUnknownException();
        }
    }

    /**
     * Evaluates all contract events within a time period
     * <p>
     *     The set of contract attributes are mapped to a stream of contract events according
     *     to the legal logic of the respective Contract Type and contingent
     *     to the risk factor dynamics provided with the {@link RiskFactorModelProvider}. The
     *     generated stream of contract events contains all post-status date events that occur
     *     within the window of times specified through a window start date and time period.
     * </p>
     * <p>
     *     The window of contract events is taken as the time period from time 
     *     {@code analysisTimes} to time {@code analysisTimes.plus(period)} including
     *     limits.
     * </p>
     * <p>
     *    For each time in argument {@code analysisTimes} an analytical event is added to
     *    the lifecycle with the sole purpose of "collecting" the contract's inner states
     *    at that time. Therefore, the generated analytical events do not affect the
     *    contract's lifecycle.
     * </p>
     * <p>
     *     This method invokes the {@code events(LocalDateTime analysisTime,Period period, ContractModel model, RiskFactorModelProvider riskFactorModel)}
     *     method of the respective Contract Type-class as indicated by the {@code ContractType} attribute.
     *     If the {@code ContractType} attribute cannot be resolved to an ACTUS Contract Type the method
     *     throws a {@link ContractTypeUnknownException}.
     * </p>
     *
     * @param analysisTime  a single analysis time marking the window start
     * @param period the period within which events are evaluated 
     * @param model the model carrying the contract attributes
     * @param riskFactorModel an external model of the risk factor dynamics
     * @return the contract's contingent lifecycle
     * @throws ContractTypeUnknownException if the provided ContractType field in the {@link ContractModelProvider} cannot be resolved
     * @throws AttributeConversionException if and attribute in {@link ContractModelProvider} cannot be converted to its target data type
     *
     */
    public static ArrayList<ContractEvent> events(LocalDateTime analysisTime,
                                                  Period period,
                                                  ContractModelProvider model,
                                                  RiskFactorModelProvider riskFactorModel) throws ContractTypeUnknownException,AttributeConversionException {
        switch((String) model.getAs("ContractType")) {
            case StringUtils.ContractType_PAM:
                return PrincipalAtMaturity.events(analysisTime,period,model,riskFactorModel);
            case StringUtils.ContractType_LAM:
                return LinearAmortizer.events(analysisTime,period,model,riskFactorModel);
            case StringUtils.ContractType_NAM:
                return NegativeAmortizer.events(analysisTime,period,model,riskFactorModel);
            case StringUtils.ContractType_ANN:
                return Annuity.events(analysisTime,period,model,riskFactorModel);
            case StringUtils.ContractType_CLM:
                return CallMoney.events(analysisTime,period,model,riskFactorModel);
            case StringUtils.ContractType_CSH:
                return Cash.events(analysisTime,period,model,riskFactorModel);
            case StringUtils.ContractType_STK:
                return Stock.events(analysisTime,period,model,riskFactorModel);
            case StringUtils.ContractType_COM:
                return Commodity.events(analysisTime,period,model,riskFactorModel);
            case StringUtils.ContractType_FXOUT:
                return ForeignExchangeOutright.events(analysisTime,period,model,riskFactorModel);
            case StringUtils.ContractType_SWPPV:
                return PlainVanillaInterestRateSwap.events(analysisTime,period,model,riskFactorModel);
            default:
                throw new ContractTypeUnknownException();
        }
    }

    /**
     * Evaluates the non-contingent contract events within a time period
     * <p>
     *     The set of contract attributes are mapped to a stream of contract events according
     *     to the legal logic of the respective Contract Type. The
     *     generated stream of contract events contains all post-status date events that occur
     *     within the window of times specified through a window start date and time period.
     * </p>
     * <p>
     *     The window of contract events is taken as the time period from time 
     *     {@code analysisTimes} to time {@code analysisTimes.plus(period)} including
     *     limits.
     * </p>
     * <p>
     *     The non-contingent window of contract events evaluates the legal logic of the respective
     *     Contract Type within the time window up to the point in time where the first
     *     risk factor contingent contract event occurs. Therefore, the non-contingent window of
     *     contract events matches the portion of the contingent window of contract events up to 
     *     the first contingent event. Further, for a contract with purely non-contingent events 
     *     (e.g. a {@link PrincipalAtMaturity} without {@code RateReset}, {@code Scaling},
     *     {@code CreditDefault}, etc.) contingent and non-contingent window of contract events are 
     *     the same.
     * </p>
     * <p>
     *    For time {@code analysisTime} an analytical event is added to
     *    the lifecycle with the sole purpose of "collecting" the contract's inner states
     *    at that time. Therefore, the generated analytical events do not affect the
     *    contract's lifecycle.
     * </p>
     * <p>
     *     This method invokes the {@code events(LocalDateTime analysisTime,Period period,ContractModel model)}
     *     method of the respective Contract Type-class as indicated by the {@code ContractType} attribute.
     *     If the {@code ContractType} attribute cannot be resolved to an ACTUS Contract Type the method
     *     throws a {@link ContractTypeUnknownException}.
     * </p>
     *
     * @param analysisTime  a single analysis time marking the window start
     * @param period the period within which events are evaluated
     * @param model the model carrying the contract attributes
     * @return the non-contingent part of the contract's lifecycle
     * @throws ContractTypeUnknownException if the provided ContractType field in the {@link ContractModelProvider} cannot be resolved
     * @throws AttributeConversionException if and attribute in {@link ContractModelProvider} cannot be converted to its target data type
     *
     */
    public static ArrayList<ContractEvent> events(LocalDateTime analysisTime,
                                                  Period period,
                                                  ContractModelProvider model) throws ContractTypeUnknownException,AttributeConversionException {
        switch((String) model.getAs("ContractType")) {
            case StringUtils.ContractType_PAM:
                return PrincipalAtMaturity.events(analysisTime,period,model);
            case StringUtils.ContractType_LAM:
                return LinearAmortizer.events(analysisTime,period,model);
            case StringUtils.ContractType_NAM:
                return NegativeAmortizer.events(analysisTime,period,model);
            case StringUtils.ContractType_ANN:
                return Annuity.events(analysisTime,period,model);
            case StringUtils.ContractType_CLM:
                return CallMoney.events(analysisTime,period,model);
            case StringUtils.ContractType_CSH:
                return Cash.events(analysisTime,period,model);
            case StringUtils.ContractType_STK:
                return Stock.events(analysisTime,period,model);
            case StringUtils.ContractType_COM:
                return Commodity.events(analysisTime,period,model);
            case StringUtils.ContractType_FXOUT:
                return ForeignExchangeOutright.events(analysisTime,period,model);
            case StringUtils.ContractType_SWPPV:
                return PlainVanillaInterestRateSwap.events(analysisTime,period,model);
            default:
                throw new ContractTypeUnknownException();
        }
    }

    /**
     * Evaluates all transaction events within a specified time window
     * <p>
     *     The set of contract attributes are mapped to a stream of transaction events according
     *     to the legal logic of the respective Contract Type and contingent
     *     to the risk factor dynamics provided with the {@link RiskFactorModelProvider}. The
     *     generated stream of transaction events contains all post-status date transaction
     *     events that occur within the window of times specified.
     * </p>
     * <p>
     *     The window of transaction events is taken as the time period from time 
     *     {@code analysisTimes.stream().min(Comparator.naturalOrder())} to time
     *     {@code analysisTimes.stream().max(Comparator.naturalOrder())} including
     *     limits.
     * </p>
     * <p>
     *    For each time in argument {@code analysisTimes} an analytical event is added to
     *    the lifecycle with the sole purpose of "collecting" the contract's inner states
     *    at that time. Therefore, the generated analytical events do not affect the
     *    contract's lifecycle.
     * </p>
     * <p>
     *     This method invokes the {@code transactions(Set<LocalDateTime> analysisTimes, ContractModel model, RiskFactorModelProvider riskFactorModel)}
     *     method of the respective Contract Type-class as indicated by the {@code ContractType} attribute.
     *     If the {@code ContractType} attribute cannot be resolved to an ACTUS Contract Type the method
     *     throws a {@link ContractTypeUnknownException}.
     * </p>
     *
     * @param analysisTimes  a series of analysis times
     * @param model the model carrying the contract attributes
     * @param riskFactorModel an external model of the risk factor dynamics
     * @return the transaction events within the time window
     * @throws ContractTypeUnknownException if the provided ContractType field in the {@link ContractModelProvider} cannot be resolved
     * @throws AttributeConversionException if and attribute in {@link ContractModelProvider} cannot be converted to its target data type
     *
     */
    public static ArrayList<ContractEvent> transactions(Set<LocalDateTime> analysisTimes,
                                                  ContractModelProvider model,
                                                  RiskFactorModelProvider riskFactorModel) throws ContractTypeUnknownException,AttributeConversionException {
        switch((String) model.getAs("ContractType")) {
            case StringUtils.ContractType_PAM:
                return PrincipalAtMaturity.transactions(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_LAM:
                return LinearAmortizer.transactions(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_NAM:
                return NegativeAmortizer.transactions(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_ANN:
                return Annuity.transactions(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_CLM:
                return CallMoney.transactions(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_CSH:
                return Cash.transactions(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_STK:
                return Stock.transactions(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_COM:
                return Commodity.transactions(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_FXOUT:
                return ForeignExchangeOutright.transactions(analysisTimes,model,riskFactorModel);
            case StringUtils.ContractType_SWPPV:
                return PlainVanillaInterestRateSwap.transactions(analysisTimes,model,riskFactorModel);
            default:
                throw new ContractTypeUnknownException();
        }
    }

    /**
     * Evaluates the non-contingent transaction events within a time window
     * <p>
     *     The set of contract attributes are mapped to a stream of transaction events according
     *     to the legal logic of the respective Contract Type. The
     *     generated stream of transaction events contains all post-status date transaction events 
     *     that occur within the window of times specified.
     * </p>
     * <p>
     *     The window of times is taken as the time period from time 
     *     {@code analysisTimes.stream().min(Comparator.naturalOrder())} to time
     *     {@code analysisTimes.stream().max(Comparator.naturalOrder())} including
     *     limits.
     * </p>
     * <p>
     *     The non-contingent window of transaction events evaluates the legal logic of the respective
     *     Contract Type within the time window up to the point in time where the first
     *     risk factor contingent contract event occurs. Therefore, the non-contingent window of
     *     transaction events matches the portion of the contingent window of transaction events up to 
     *     the first contingent contract event. Further, for a contract with purely non-contingent events 
     *     (e.g. a {@link PrincipalAtMaturity} without {@code RateReset}, {@code Scaling},
     *     {@code CreditDefault}, etc.) contingent and non-contingent window of transaction events are 
     *     the same.
     * </p>
     * <p>
     *    For each time in argument {@code analysisTimes} an analytical event is added to
     *    the lifecycle with the sole purpose of "collecting" the contract's inner states
     *    at that time. Therefore, the generated analytical events do not affect the
     *    contract's lifecycle.
     * </p>
     * <p>
     *     This method invokes the {@code transactions(Set<LocalDateTime> analysisTimes, ContractModel model)}
     *     method of the respective Contract Type-class as indicated by the {@code ContractType} attribute.
     *     If the {@code ContractType} attribute cannot be resolved to an ACTUS Contract Type the method
     *     throws a {@link ContractTypeUnknownException}.
     * </p>
     *
     * @param analysisTimes  a series of analysis times
     * @param model the model carrying the contract attributes
     * @return the non-contingent transaction events within the time window
     * @throws ContractTypeUnknownException if the provided ContractType field in the {@link ContractModelProvider} cannot be resolved
     * @throws AttributeConversionException if and attribute in {@link ContractModelProvider} cannot be converted to its target data type
     *
     */
    public static ArrayList<ContractEvent> transactions(Set<LocalDateTime> analysisTimes,
                                                  ContractModelProvider model) throws ContractTypeUnknownException,AttributeConversionException {
        switch((String) model.getAs("ContractType")) {
            case StringUtils.ContractType_PAM:
                return PrincipalAtMaturity.transactions(analysisTimes,model);
            case StringUtils.ContractType_LAM:
                return LinearAmortizer.transactions(analysisTimes,model);
            case StringUtils.ContractType_NAM:
                return NegativeAmortizer.transactions(analysisTimes,model);
            case StringUtils.ContractType_ANN:
                return Annuity.transactions(analysisTimes,model);
            case StringUtils.ContractType_CLM:
                return CallMoney.transactions(analysisTimes,model);
            case StringUtils.ContractType_CSH:
                return Cash.transactions(analysisTimes,model);
            case StringUtils.ContractType_STK:
                return Stock.transactions(analysisTimes,model);
            case StringUtils.ContractType_COM:
                return Commodity.transactions(analysisTimes,model);
            case StringUtils.ContractType_FXOUT:
                return ForeignExchangeOutright.transactions(analysisTimes,model);
            case StringUtils.ContractType_SWPPV:
                return PlainVanillaInterestRateSwap.transactions(analysisTimes,model);
            default:
                throw new ContractTypeUnknownException();
        }
    }

    /**
     * Evaluates all transaction events within a time period
     * <p>
     *     The set of contract attributes are mapped to a stream of transaction events according
     *     to the legal logic of the respective Contract Type and contingent
     *     to the risk factor dynamics provided with the {@link RiskFactorModelProvider}. The
     *     generated stream of transaction events contains all post-status date transaction events 
     *     that occur within the window of times specified through a window start date and time period.
     * </p>
     * <p>
     *     The window of transaction events is taken as the time period from time 
     *     {@code analysisTimes} to time {@code analysisTimes.plus(period)} including
     *     limits.
     * </p>
     * <p>
     *    For each time in argument {@code analysisTimes} an analytical event is added to
     *    the lifecycle with the sole purpose of "collecting" the contract's inner states
     *    at that time. Therefore, the generated analytical events do not affect the
     *    contract's lifecycle.
     * </p>
     * <p>
     *     This method invokes the {@code transactions(LocalDateTime analysisTime, Period period, ContractModel model, RiskFactorModelProvider riskFactorModel)}
     *     method of the respective Contract Type-class as indicated by the {@code ContractType} attribute.
     *     If the {@code ContractType} attribute cannot be resolved to an ACTUS Contract Type the method
     *     throws a {@link ContractTypeUnknownException}.
     * </p>
     *
     * @param analysisTime  a single analysis time marking the window start
     * @param period the period within which events are evaluated 
     * @param model the model carrying the contract attributes
     * @param riskFactorModel an external model of the risk factor dynamics
     * @return the transaction events within the time period specified
     * @throws ContractTypeUnknownException if the provided ContractType field in the {@link ContractModelProvider} cannot be resolved
     * @throws AttributeConversionException if and attribute in {@link ContractModelProvider} cannot be converted to its target data type
     *
     */
    public static ArrayList<ContractEvent> transactions(LocalDateTime analysisTime,
                                                  Period period,
                                                  ContractModelProvider model,
                                                  RiskFactorModelProvider riskFactorModel) throws ContractTypeUnknownException,AttributeConversionException {
        switch((String) model.getAs("ContractType")) {
            case StringUtils.ContractType_PAM:
                return PrincipalAtMaturity.transactions(analysisTime,period,model,riskFactorModel);
            case StringUtils.ContractType_LAM:
                return LinearAmortizer.transactions(analysisTime,period,model,riskFactorModel);
            case StringUtils.ContractType_NAM:
                return NegativeAmortizer.transactions(analysisTime,period,model,riskFactorModel);
            case StringUtils.ContractType_ANN:
                return Annuity.transactions(analysisTime,period,model,riskFactorModel);
            case StringUtils.ContractType_CLM:
                return CallMoney.transactions(analysisTime,period,model,riskFactorModel);
            case StringUtils.ContractType_CSH:
                return Cash.transactions(analysisTime,period,model,riskFactorModel);
            case StringUtils.ContractType_STK:
                return Stock.transactions(analysisTime,period,model,riskFactorModel);
            case StringUtils.ContractType_COM:
                return Commodity.transactions(analysisTime,period,model,riskFactorModel);
            case StringUtils.ContractType_FXOUT:
                return ForeignExchangeOutright.transactions(analysisTime,period,model,riskFactorModel);
            case StringUtils.ContractType_SWPPV:
                return PlainVanillaInterestRateSwap.transactions(analysisTime,period,model,riskFactorModel);
            default:
                throw new ContractTypeUnknownException();
        }
    }

    /**
     * Evaluates the non-contingent transaction events within a time period
     * <p>
     *     The set of contract attributes are mapped to a stream of transaction events according
     *     to the legal logic of the respective Contract Type. The
     *     generated stream of transaction events contains all post-status date transaction events 
     *     that occur within the window of times specified through a window start date and time period.
     * </p>
     * <p>
     *     The window of transaction events is taken as the time period from time 
     *     {@code analysisTimes} to time {@code analysisTimes.plus(period)} including
     *     limits.
     * </p>
     * <p>
     *     The non-contingent window of transaction events evaluates the legal logic of the respective
     *     Contract Type within the time window up to the point in time where the first
     *     risk factor contingent contract event occurs. Therefore, the non-contingent window of
     *     transaction events matches the portion of the window of contingent transaction events up to 
     *     the first contingent event. Further, for a contract with purely non-contingent events 
     *     (e.g. a {@link PrincipalAtMaturity} without {@code RateReset}, {@code Scaling},
     *     {@code CreditDefault}, etc.) contingent and non-contingent window of transaction events are 
     *     the same.
     * </p>
     * <p>
     *    For time {@code analysisTime} an analytical event is added to
     *    the lifecycle with the sole purpose of "collecting" the contract's inner states
     *    at that time. Therefore, the generated analytical events do not affect the
     *    contract's lifecycle.
     * </p>
     * <p>
     *     This method invokes the {@code transactions(LocalDateTime analysisTime, Period period, ContractModel model)}
     *     method of the respective Contract Type-class as indicated by the {@code ContractType} attribute.
     *     If the {@code ContractType} attribute cannot be resolved to an ACTUS Contract Type the method
     *     throws a {@link ContractTypeUnknownException}.
     * </p>
     *
     * @param analysisTime  a single analysis time marking the window start
     * @param period the period within which events are evaluated
     * @param model the model carrying the contract attributes
     * @return the non-contingent transaction events within the time period specified
     * @throws ContractTypeUnknownException if the provided ContractType field in the {@link ContractModelProvider} cannot be resolved
     * @throws AttributeConversionException if and attribute in {@link ContractModelProvider} cannot be converted to its target data type
     *
     */
    public static ArrayList<ContractEvent> transactions(LocalDateTime analysisTime,
                                                  Period period,
                                                  ContractModelProvider model) throws ContractTypeUnknownException,AttributeConversionException {
        switch((String) model.getAs("ContractType")) {
            case StringUtils.ContractType_PAM:
                return PrincipalAtMaturity.transactions(analysisTime,period,model);
            case StringUtils.ContractType_LAM:
                return LinearAmortizer.transactions(analysisTime,period,model);
            case StringUtils.ContractType_NAM:
                return NegativeAmortizer.transactions(analysisTime,period,model);
            case StringUtils.ContractType_ANN:
                return Annuity.transactions(analysisTime,period,model);
            case StringUtils.ContractType_CLM:
                return CallMoney.transactions(analysisTime,period,model);
            case StringUtils.ContractType_CSH:
                return Cash.transactions(analysisTime,period,model);
            case StringUtils.ContractType_STK:
                return Stock.transactions(analysisTime,period,model);
            case StringUtils.ContractType_COM:
                return Commodity.transactions(analysisTime,period,model);
            case StringUtils.ContractType_FXOUT:
                return ForeignExchangeOutright.transactions(analysisTime,period,model);
            case StringUtils.ContractType_SWPPV:
                return PlainVanillaInterestRateSwap.transactions(analysisTime,period,model);
            default:
                throw new ContractTypeUnknownException();
        }
    }
    
}

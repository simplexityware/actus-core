/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
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
import java.util.Set;
import java.util.ArrayList;

/**
 * A representation of an ACTUS Contract Type algorithm
 * <p>
 * ACTUS Contract Types represent classes of financial instrument payoff functions. This component 
 * is a representation of the algorithm mapping a set of attributes (cf. {@link ContractModelProvider}),
 * an external market model (cf. {@link RiskFactorModelProvider}) and analysis times 
 * (cf. {@code analysisTimes}) onto a vector of contingent contract events (cf. {@link ContractEvent}).
 * <p>
 * @see <a href="http://www.projectactus.org">ACTUS</a>
 */
public final class ContractType {
    
   /**
   * Evaluates the payoff of the instrument
   * <p>
   * For a series of analysis times, set of attributes, and model of market dynamics
   * this method computes the contingent contract events, i.e. instrument payoff.
   * <p>
   * This method invokes the {@code evalAll(Set<LocalDateTime> analysisTimes,ContractModel model,RiskFactorModelProvider riskFactorModel)}
   * method of the respective Contract Type-class as indicated by the {@code ContractType} attribute.
   * If the {@code ContractType} attribute cannot be resolved to an ACTUS Contract Type the method
   * throws a {@link ContractTypeUnknownException}.
   * 
   * @param analysisTimes  a series of analysis times
   * @param model the model carrying the contract attributes
   * @param riskFactorModel an external model of the risk factor dynamics
   * @return the instrument's contingent payoff
   * @throws ContractTypeUnknownException if the provided ContractType field in the {@link ContractModelProvider} cannot be resolved
   * @throws AttributeConversionException if and attribute in {@link ContractModelProvider} cannot be converted to its target data type
   * 
   */
  public static ArrayList<ContractEvent> evalAll(Set<LocalDateTime> analysisTimes,
                                                 ContractModelProvider model,
                                                 RiskFactorModelProvider riskFactorModel) throws ContractTypeUnknownException,AttributeConversionException {
            switch((String) model.getAs("ContractType")) {
                case StringUtils.ContractType_PAM: 
                    return PrincipalAtMaturity.evalAll(analysisTimes,model,riskFactorModel);
                case StringUtils.ContractType_LAM: 
                    return LinearAmortizer.evalAll(analysisTimes,model,riskFactorModel);
                case StringUtils.ContractType_NAM: 
                    return NegativeAmortizer.evalAll(analysisTimes,model,riskFactorModel);
                case StringUtils.ContractType_ANN: 
                    return Annuity.evalAll(analysisTimes,model,riskFactorModel);
                case StringUtils.ContractType_CLM: 
                    return CallMoney.evalAll(analysisTimes,model,riskFactorModel);
                case StringUtils.ContractType_CSH: 
                    return Cash.evalAll(analysisTimes,model,riskFactorModel);
                case StringUtils.ContractType_STK: 
                    return Stock.evalAll(analysisTimes,model,riskFactorModel);
                case StringUtils.ContractType_COM: 
                    return Commodity.evalAll(analysisTimes,model,riskFactorModel);
                case StringUtils.ContractType_FXOUT: 
                    return ForeignExchangeOutright.evalAll(analysisTimes,model,riskFactorModel);
                default:
                    throw new ContractTypeUnknownException();
            }
  }
}

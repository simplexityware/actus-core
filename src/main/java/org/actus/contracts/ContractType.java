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
 * is a representation of the algorithm mapping a set of attributes (cf. {@link riskFactorModel}),
 * an external market model (cf. {@link RiskFactorModelProvider}) and analysis times 
 * (cf. {@link AnalysisEvent}) onto a vector of contingent contract events (cf. {@link ContractEvent}).
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
   * This method invokes the {@code eval(Set<LocalDateTime> analysisTimes,ContractModel model,RiskFactorModelProvider riskFactorModel)}
   * method of the respective Contract Type-class as indicated by the {@code ContractType} attribute.
   * If the {@code ContractType} attribute cannot be resolved to an ACTUS Contract Type the method
   * throws a {@link ContractTypeUnknownException}.
   * 
   * @param analysisTimes  a series of analysis times
   * @param model the model carrying the contract attributes
   * @param riskFactorModel an external model of the risk factor dynamics
   * @return the instrument's contingent payoff
   * @throws AttributeConversionException if and attribute in {@link AttributesProvider} cannot be converted to its target data type
   * 
   */
  public static ArrayList<ContractEvent> eval(Set<LocalDateTime> analysisTimes, 
                        		 ContractModelProvider model, 
                        		 RiskFactorModelProvider riskFactorModel) throws ContractTypeUnknownException,AttributeConversionException {
            switch(model.contractType()) {
                case StringUtils.ContractType_PAM: 
                    return PrincipalAtMaturity.eval(analysisTimes,model,riskFactorModel);
                case StringUtils.ContractType_LAM: 
                    return LinearAmortizer.eval(analysisTimes,model,riskFactorModel);
                case StringUtils.ContractType_NAM: 
                    return NegativeAmortizer.eval(analysisTimes,model,riskFactorModel);
                case StringUtils.ContractType_ANN: 
                    return Annuity.eval(analysisTimes,model,riskFactorModel);
                default:
                    throw new ContractTypeUnknownException();
            }
  }
}

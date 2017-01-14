/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import org.actus.ContractTypeUnknownException;
import org.actus.AttributeConversionException;
import org.actus.externals.MarketModelProvider;
import org.actus.externals.AttributeParser;
import org.actus.attributes.ContractModel;
import org.actus.events.ContractEvent;
import org.actus.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.ArrayList;
import java.util.Map;

/**
 * A representation of an ACTUS Contract Type algorithm
 * <p>
 * ACTUS Contract Types represent classes of financial instrument payoff functions. This component 
 * is a representation of the algorithm mapping a set of attributes (cf. {@link MarketModel}),
 * an external market model (cf. {@link MarketModelProvider}) and analysis times 
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
   * This method parses the input attributes from their raw representation to a {@link ContractModel}. 
   * If an attribute cannot be parsed the method throws a {@link AttributeConversionException},
   * otherwise it invokes the {@code eval(Set<LocalDateTime> analysisTimes,ContractModel model,RiskFactorProvider riskFactors)}
   * method of the respective Contract Type-class as indicated by the {@code ContractType} attribute.
   * 
   * @param analysisTimes  a series of analysis times
   * @param attributes the contract attributes in un-parsed form
   * @param marketModel an external model of the market dynamics
   * @return the instrument's contingent payoff
   * @throws AttributeConversionException if and attribute in {@link AttributesProvider} cannot be converted to its target data type
   * 
   */
   public static ArrayList<ContractEvent> eval(Set<LocalDateTime> analysisTimes, 
                        		 Map<String,String> attributes, 
                        		 MarketModelProvider marketModel) throws ContractTypeUnknownException,AttributeConversionException {
        return eval(analysisTimes,AttributeParser.parse(attributes),marketModel);
    }
  
    
   /**
   * Evaluates the payoff of the instrument
   * <p>
   * For a series of analysis times, set of attributes, and model of market dynamics
   * this method computes the contingent contract events, i.e. instrument payoff.
   * <p>
   * This method invokes the {@code eval(Set<LocalDateTime> analysisTimes,ContractModel model,MarketModelProvider marketModel)}
   * method of the respective Contract Type-class as indicated by the {@code ContractType} attribute.
   * If the {@code ContractType} attribute cannot be resolved to an ACTUS Contract Type the method
   * throws a {@link ContractTypeUnknownException}.
   * 
   * @param analysisTimes  a series of analysis times
   * @param model the model carrying the contract attributes
   * @param marketModel an external model of the market dynamics
   * @return the instrument's contingent payoff
   * @throws AttributeConversionException if and attribute in {@link AttributesProvider} cannot be converted to its target data type
   * 
   */
  public static ArrayList<ContractEvent> eval(Set<LocalDateTime> analysisTimes, 
                        		 ContractModel model, 
                        		 MarketModelProvider marketModel) throws ContractTypeUnknownException,AttributeConversionException {
            switch(model.contractType) {
                case StringUtils.ContractType_PAM: 
                    return PrincipalAtMaturity.eval(analysisTimes,model,marketModel);
                default:
                    throw new ContractTypeUnknownException();
            }
  }
}

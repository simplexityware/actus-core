/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.contracttypes;

import org.actus.AttributeConversionException;
import org.actus.riskfactors.RiskFactorProvider;
import org.actus.attributes.AttributeProvider;
import org.actus.attributes.AttributeParser;
import org.actus.attributes.ContractModel;
import org.actus.events.ContractEvent;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.ArrayList;

/**
 * A representation of an ACTUS Contract Type algorithm
 * <p>
 * ACTUS Contract Types represent classes of financial instrument payoff functions. This component 
 * is a representation of the algorithm mapping a set of attributes (cf. {@link AttributeProvider}),
 * risk factors (cf. {@link RiskFactorProvider}) and analysis times (cf. {@link AnalysisEvent}) onto
 * a vector of contingent contract events (cf. {@link ContractEvent}).
 * <p>
 * @see <a href="http://www.projectactus.org">ACTUS</a>
 */
public interface ContractType {
    
   /**
   * Evaluates the payoff of the instrument
   * <p>
   * For a series of analysis times, set of attributes, and model of risk factor dynamics
   * this method computes the contingent contract events, i.e. instrument payoff. In fact,
   * this method performs {@code init} and {@code eval} in one step.
   * <p>
   * This method is not thread-save. The caller is responsible for imposing thread-safety if 
   * necessary.
   * 
   * @param analysisTimes  a series of analysis times
   * @param attributes the contract attributes in un-parsed form
   * @param riskFactors a model of risk factor dynamics
   * @return the instrument's contingent payoff
   * @throws AttributeConversionException if and attribute in {@link AttributesProvider} cannot be converted to its target data type
   * 
   */
  public default ArrayList<ContractEvent> eval(Set<LocalDateTime> analysisTimes, 
                        		 AttributeProvider attributes, 
                        		 RiskFactorProvider riskFactors) throws AttributeConversionException {
        init(analysisTimes, AttributeParser.parse(attributes));
        return eval(riskFactors);
    }
  
  /**
   * Initializes the payoff algorithm
   * <p>
   * Upon initialization of a {@code ContractType} all non-Risk Factor-dependent sub-routines
   * are executed. Hence, in some sense this step performs parameterization of the algorithm
   * such that in a next step only the remaining sub-routines have to be executed for a 
   * {@link RiskFactorProvider} (i.e. different assumptions on the future evolution of 
   * risk factors). 
   * <p>
   * This method is not thread-save. The caller is responsible for imposing thread-safety if 
   * necessary.
   * 
   * @param analysisTimes  a series of analysis times
   * @param attributes the contract attributes in un-parsed form
   * @return
   * @throws AttributeConversionException if and attribute in {@link AttributesProvider} cannot be converted to its target data type
   * 
   */
   public default void init(Set<LocalDateTime> analysisTimes, 
                        AttributeProvider attributes) throws AttributeConversionException {
        init(analysisTimes, AttributeParser.parse(attributes));                         
    }
    
    
   /**
   * Evaluates the payoff of the instrument
   * <p>
   * For a series of analysis times, set of attributes, and model of risk factor dynamics
   * this method computes the contingent contract events, i.e. instrument payoff. In fact,
   * this method performs {@code init} and {@code eval} in one step.
   * <p>
   * This method is not thread-save. The caller is responsible for imposing thread-safety if 
   * necessary.
   * 
   * @param analysisTimes  a series of analysis times
   * @param model the model carrying the contract attributes
   * @param riskFactors a model of risk factor dynamics
   * @return the instrument's contingent payoff
   * @throws AttributeConversionException if and attribute in {@link AttributesProvider} cannot be converted to its target data type
   * 
   */
  public default ArrayList<ContractEvent> eval(Set<LocalDateTime> analysisTimes, 
                        		 ContractModel model, 
                        		 RiskFactorProvider riskFactors) throws AttributeConversionException {
        init(analysisTimes, model);
        return eval(riskFactors);
    }
  
  /**
   * Initializes the payoff algorithm
   * <p>
   * Upon initialization of a {@code ContractType} all non-Risk Factor-dependent sub-routines
   * are executed. Hence, in some sense this step performs parameterization of the algorithm
   * such that in a next step only the remaining sub-routines have to be executed for a 
   * {@link RiskFactorProvider} (i.e. different assumptions on the future evolution of 
   * risk factors). 
   * <p>
   * This method is not thread-save. The caller is responsible for imposing thread-safety if 
   * necessary.
   * 
   * @param analysisTimes  a series of analysis times
   * @param model the model carrying the contract attributes
   * @return
   * @throws AttributeConversionException if and attribute in {@link AttributesProvider} cannot be converted to its target data type
   * 
   */
   public void init(Set<LocalDateTime> analysisTimes, 
                      ContractModel model) throws AttributeConversionException;
   
    /**
   * Maps an initialized Contract Type onto contingent contract events
   * <p>
   * Takes an initialized {@code ContractType} and a {@link RiskFactorProvider} and maps them
   * onto a set of contingent {@link ContractEvent}s, i.e. cash flows. Hence, prior to calling
   * this method, method {@code init} has to be called for initialization of the 
   * {@code ContractType}. 
   * <p>
   * If the {@code ContractType} is to be evaluated under multiple assumptions (e.g. What-If or 
   * Monte-Carlo scenarios) on the future evolution of risk factors, only this method has to
   * be re-evaluated but not method {@code init}. This can considerably improve performance of
   * larger simulations. 
   * <p>
   * This method is not thread-save. The caller is responsible for imposing thread-safety if 
   * necessary.
   * 
   * @param analysisTimes  a series of analysis times
   * @param attributes a set of contract attributes
   * @return the instrument's contingent payoff
   * @throws AttributeConversionException if and attribute in {@link AttributesProvider} cannot be converted to its target data type
   * 
   */
   public ArrayList<ContractEvent> eval(RiskFactorProvider riskFactors) throws AttributeConversionException;
  
}

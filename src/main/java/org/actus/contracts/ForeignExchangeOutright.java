/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import org.actus.AttributeConversionException;
import org.actus.externals.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.events.ContractEvent;
import org.actus.states.StateSpace;
import org.actus.events.EventFactory;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.util.CommonUtils;
import org.actus.util.StringUtils;
import org.actus.functions.pam.POF_AD_PAM;
import org.actus.functions.pam.STF_AD_PAM;
import org.actus.functions.fxout.POF_PRD_FXOUT;
import org.actus.functions.stk.STF_PRD_STK;
import org.actus.functions.fxout.POF_TD_FXOUT;
import org.actus.functions.stk.STF_TD_STK;
import org.actus.functions.fxout.POF_STD_FXOUT;
import org.actus.functions.fxout.STF_STD_FXOUT;
import org.actus.functions.fxout.POF_STD1_FXOUT;
import org.actus.functions.fxout.STF_STD1_FXOUT;
import org.actus.functions.fxout.POF_STD2_FXOUT;
import org.actus.functions.fxout.STF_STD2_FXOUT;
import org.actus.functions.pam.POF_CD_PAM;
import org.actus.functions.fxout.STF_CD_FXOUT;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents the Foreign Exchange Outright payoff algorithm
 * 
 * @see <a href="http://www.projectactus.org/"></a>
 */
public final class ForeignExchangeOutright {

    public static ArrayList<ContractEvent> eval(Set<LocalDateTime> analysisTimes, 
                        		         ContractModelProvider model, 
                        		         RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        
        // determine settlement date (maturity) of the contract
        LocalDateTime settlement = model.settlementDate();
        if (CommonUtils.isNull(settlement)) {
            settlement = model.maturityDate();
        }
        
        // init day count calculator 
        DayCountCalculator dayCount = new DayCountCalculator("A/AISDA", null);
        
        // compute events
        ArrayList<ContractEvent> payoff = new ArrayList<ContractEvent>();
        // analysis events
        payoff.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.currency(), new POF_AD_PAM(), new STF_AD_PAM()));
        // purchase
        if (!CommonUtils.isNull(model.purchaseDate())) {
            payoff.add(EventFactory.createEvent(model.purchaseDate(), StringUtils.EventType_PRD, model.currency(), new POF_PRD_FXOUT(), new STF_PRD_STK()));
        }
        // termination
        if (!CommonUtils.isNull(model.terminationDate())) {
            payoff.add(EventFactory.createEvent(model.terminationDate(), StringUtils.EventType_TD, model.currency(), new POF_TD_FXOUT(), new STF_TD_STK()));
        }
        // settlement
        if (CommonUtils.isNull(model.deliverySettlement()) || model.deliverySettlement().equals(StringUtils.Settlement_Physical)) {
            payoff.add(EventFactory.createEvent(settlement, StringUtils.EventType_STD, model.currency(), new POF_STD1_FXOUT(), new STF_STD1_FXOUT(), model.businessDayConvention()));
            payoff.add(EventFactory.createEvent(settlement, StringUtils.EventType_STD, model.currency2(), new POF_STD2_FXOUT(), new STF_STD2_FXOUT(), model.businessDayConvention()));    
        } else {
            payoff.add(EventFactory.createEvent(settlement, StringUtils.EventType_STD, model.currency(), new POF_STD_FXOUT(), new STF_STD_FXOUT(), model.businessDayConvention()));
        }
        // add counterparty default risk-factor contingent events
        if(riskFactorModel.keys().contains(model.legalEntityIDCounterparty())) {
            payoff.addAll(EventFactory.createEvents(riskFactorModel.times(model.legalEntityIDCounterparty()),
                                             StringUtils.EventType_CD, model.currency(), new POF_CD_PAM(), new STF_CD_FXOUT()));
        }
        // remove all pre-status date events
        payoff.removeIf(e -> e.compareTo(EventFactory.createEvent(model.statusDate(), StringUtils.EventType_SD, model.currency(), null,
                                                                  null)) == -1);
        // initialize state space per status date
        StateSpace states = new StateSpace();
        states.contractRoleSign = ContractRoleConvention.roleSign(model.contractRole());
        states.lastEventTime = model.statusDate();
        
        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(payoff);

        // evaluate events
        payoff.forEach(e -> e.eval(states, model, riskFactorModel, dayCount, model.businessDayConvention()));
        
        // return all evaluated post-StatusDate events as the payoff
        return payoff;
    }
}

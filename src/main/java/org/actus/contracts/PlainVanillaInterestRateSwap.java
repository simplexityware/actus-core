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
import org.actus.util.CommonUtils;
import org.actus.util.StringUtils;
import org.actus.time.ScheduleFactory;
import org.actus.functions.pam.POF_AD_PAM;
import org.actus.functions.swppv.STF_AD_SWPPV;
import org.actus.functions.swppv.POF_IED_SWPPV;
import org.actus.functions.swppv.STF_IED_SWPPV;
import org.actus.functions.swppv.POF_PR_SWPPV;
import org.actus.functions.swppv.STF_PR_SWPPV;
import org.actus.functions.fxout.POF_PRD_FXOUT;
import org.actus.functions.swppv.STF_PRD_SWPPV;
import org.actus.functions.fxout.POF_TD_FXOUT;
import org.actus.functions.swppv.STF_TD_SWPPV;
import org.actus.functions.swppv.POF_IP_SWPPV;
import org.actus.functions.swppv.STF_IP_SWPPV;
import org.actus.functions.swppv.POF_IPFix_SWPPV;
import org.actus.functions.swppv.STF_IPFix_SWPPV;
import org.actus.functions.swppv.POF_IPFloat_SWPPV;
import org.actus.functions.swppv.STF_IPFloat_SWPPV;
import org.actus.functions.pam.POF_RR_PAM;
import org.actus.functions.swppv.STF_RR_SWPPV;
import org.actus.functions.pam.POF_CD_PAM;
import org.actus.functions.swppv.STF_CD_SWPPV;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents the Plain Vanilla Interest Rate Swap payoff algorithm
 * 
 * @see <a href="http://www.projectactus.org/"></a>
 */
public final class PlainVanillaInterestRateSwap {

    public static ArrayList<ContractEvent> lifecycle(Set<LocalDateTime> analysisTimes,
                                                     ContractModelProvider model,
                                                     RiskFactorModelProvider riskFactorModel) throws AttributeConversionException {
        
        // compute events
        ArrayList<ContractEvent> payoff = new ArrayList<ContractEvent>();
        // analysis events
        payoff.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_SWPPV()));
        // initial exchange
        payoff.add(EventFactory.createEvent(model.getAs("InitialExchangeDate"), StringUtils.EventType_IED, model.getAs("Currency"), new POF_IED_SWPPV(), new STF_IED_SWPPV()));
        // principal redemption
        payoff.add(EventFactory.createEvent(model.getAs("MaturityDate"), StringUtils.EventType_PR, model.getAs("Currency"), new POF_PR_SWPPV(), new STF_PR_SWPPV()));
        // purchase
        if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            payoff.add(EventFactory.createEvent(model.getAs("PurchaseDate"), StringUtils.EventType_PRD, model.getAs("Currency"), new POF_PRD_FXOUT(), new STF_PRD_SWPPV()));
        }
        // termination
        if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
            payoff.add(EventFactory.createEvent(model.getAs("TerminationDate"), StringUtils.EventType_TD, model.getAs("Currency"), new POF_TD_FXOUT(), new STF_TD_SWPPV()));
        }
        // interest payment events
        if (CommonUtils.isNull(model.getAs("DeliverySettlement")) || model.getAs("DeliverySettlement").equals(StringUtils.Settlement_Physical)) {
            // in case of physical delivery (delivery of individual cash flows) 
            // interest payment schedule
            Set<LocalDateTime> interestSchedule = ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfInterestPayment"),
                                                                                 model.getAs("MaturityDate"),
                                                                                 model.getAs("CycleOfInterestPayment"),
                                                                                 model.getAs("EndOfMonthConvention"));
            // fixed rate events                                                                                                    model.getAs("MaturityDate"),                                                                                                  model.getAs("EndOfMonthConvention"))
            payoff.addAll(EventFactory.createEvents(interestSchedule, StringUtils.EventType_IP, model.getAs("Currency"), new POF_IPFix_SWPPV(), new STF_IPFix_SWPPV(), model.getAs("BusinessDayConvention")));
            // floating rate events                                                                                                    model.getAs("MaturityDate"),                                                                                                  model.getAs("EndOfMonthConvention"))
            payoff.addAll(EventFactory.createEvents(interestSchedule, StringUtils.EventType_IP, model.getAs("Currency"), new POF_IPFloat_SWPPV(), new STF_IPFloat_SWPPV(), model.getAs("BusinessDayConvention")));
        } else {
            // in case of cash delivery (cash settlement)                                                                                                model.getAs("MaturityDate"),                                                                                                  model.getAs("EndOfMonthConvention"))
            payoff.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfInterestPayment"),
                                                                                 model.getAs("MaturityDate"),
                                                                                 model.getAs("CycleOfInterestPayment"),
                                                                                 model.getAs("EndOfMonthConvention")), 
                            StringUtils.EventType_IP, model.getAs("Currency"), new POF_IP_SWPPV(), new STF_IP_SWPPV(), model.getAs("BusinessDayConvention")));
            
        }
        
        // rate reset         
        payoff.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfRateReset"), model.getAs("MaturityDate"),
                                                                                model.getAs("CycleOfRateReset"), model.getAs("EndOfMonthConvention")),
                                                 StringUtils.EventType_RR, model.getAs("Currency"), new POF_RR_PAM(), new STF_RR_SWPPV(), model.getAs("BusinessDayConvention")));
        // add counterparty default risk-factor contingent events
        if(riskFactorModel.keys().contains(model.getAs("LegalEntityIDCounterparty"))) {
            payoff.addAll(EventFactory.createEvents(riskFactorModel.times(model.getAs("LegalEntityIDCounterparty")),
                                             StringUtils.EventType_CD, model.getAs("Currency"), new POF_CD_PAM(), new STF_CD_SWPPV()));
        }
        // remove all pre-status date events
        payoff.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD, model.getAs("Currency"), null,
                                                                  null)) == -1);
        // initialize state space per status date
        StateSpace states = new StateSpace();
        states.contractRoleSign = ContractRoleConvention.roleSign(model.getAs("ContractRole"));
        states.lastEventTime = model.getAs("StatusDate");
        if (!model.<LocalDateTime>getAs("InitialExchangeDate").isAfter(model.getAs("StatusDate"))) {
            states.nominalValue = model.getAs("NotionalPrincipal");
            states.nominalRate = model.getAs("NominalInterestRate");
            states.nominalAccrued = model.getAs("AccruedInterest");
        }
        
        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(payoff);

        // evaluate events
        payoff.forEach(e -> e.eval(states, model, riskFactorModel, model.getAs("DayCountConvention"), model.getAs("BusinessDayConvention")));
        
        // return all evaluated post-StatusDate events as the payoff
        return payoff;
    }
}

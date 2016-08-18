/**
 * Copyright (C) 2016 - present by ACTUS Foundation for Financial Research
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import org.actus.AttributeConversionException;
import org.actus.attributes.ContractModel;
import org.actus.riskfactors.RiskFactorProvider;
import org.actus.events.ContractEvent;
import org.actus.states.StateSpace;
import org.actus.events.EventFactory;
import org.actus.time.ScheduleFactory;
import org.actus.util.CommonUtils;
import org.actus.util.StringUtils;
import org.actus.functions.pam.POF_AD_PAM;
import org.actus.functions.pam.STF_AD_PAM;


import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents the Principal-At-Maturity payoff algorithm
 * 
 * @see <a href="http://www.projectactus.org/"></a>
 */
public class PrincipalAtMaturity implements ContractType {
    private HashSet<ContractEvent> events;
    private ContractModel            model;
    private StateSpace               states;

    @Override
    public void init(Set<LocalDateTime> analysisTimes, ContractModel model) throws AttributeConversionException {
        this.model = model;
        events = new HashSet<ContractEvent>();

        // create contract event schedules
        // analysis events
        events.addAll(EventFactory.createEvents(analysisTimes, StringUtils.EventType_AD, model.currency, new POF_AD_PAM(), new STF_AD_PAM()));
        // initial exchange
        events.add(EventFactory.createEvent(model.initialExchangeDate, StringUtils.EventType_IED, model.currency, new POF_AD_PAM(), new STF_AD_PAM()));
        // maturity
        events.add(EventFactory.createEvent(model.maturityDate, StringUtils.EventType_MD, model.currency, new POF_AD_PAM(), new STF_AD_PAM()));
        // purchase
        if (!CommonUtils.isNull(model.purchaseDate)) {
            events.add(EventFactory.createEvent(model.purchaseDate, StringUtils.EventType_PRD, model.currency, new POF_AD_PAM(), new STF_AD_PAM()));
        }
        // interest payment related
        if (!CommonUtils.isNull(model.nominalInterestRate)) {
            // raw interest payment events
            Set<ContractEvent> interestEvents =
                                                        EventFactory.createEvents(ScheduleFactory.createSchedule(model.cycleAnchorDateOfInterestPayment,
                                                                                                                 model.maturityDate,
                                                                                                                 model.cycleOfInterestPayment,
                                                                                                                 model.endOfMonthConvention),
                                                                                  StringUtils.EventType_IED, model.currency, new POF_AD_PAM(), new STF_AD_PAM(), model.businessDayConvention);
            // adapt if interest capitalization set
            if (!CommonUtils.isNull(model.capitalizationEndDate)) {
                // for all events with time <= IPCED && type == "IP" do
                // change type to IPCI and payoff/state-trans functions
                ContractEvent capitalizationEnd =
                                                  EventFactory.createEvent(model.capitalizationEndDate, StringUtils.EventType_IPCI,
                                                                            model.currency, new POF_AD_PAM(), new STF_AD_PAM(), model.businessDayConvention);
                interestEvents.forEach(e -> {
                    if (e.type().equals(StringUtils.EventType_IP) && e.compareTo(capitalizationEnd) == -1) {
                        e.type(StringUtils.EventType_IPCI);
                        e.fPayOff(new POF_AD_PAM());
                        e.fStateTrans(new STF_AD_PAM());
                    }
                });
                interestEvents.remove(EventFactory.createEvent(model.capitalizationEndDate, StringUtils.EventType_IP,
                                                                            model.currency, new POF_AD_PAM(), new STF_AD_PAM(), model.businessDayConvention));
                interestEvents.add(capitalizationEnd);
            }
            events.addAll(interestEvents);
            // rate reset (if specified)
            if (!CommonUtils.isNull(model.cycleOfRateReset)) {            
            events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.cycleAnchorDateOfRateReset, model.maturityDate,
                                                                                model.cycleOfRateReset, model.endOfMonthConvention),
                                                 StringUtils.EventType_RR, model.currency, new POF_AD_PAM(), new STF_AD_PAM(), model.businessDayConvention));
            }
        }
        // scaling (if specified)
        if (!CommonUtils.isNull(model.cycleOfScalingIndex)) { 
        events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.cycleAnchorDateOfScalingIndex, model.maturityDate,
                                                                            model.cycleOfScalingIndex, model.endOfMonthConvention),
                                             StringUtils.EventType_SC, model.currency, new POF_AD_PAM(), new STF_AD_PAM(), model.businessDayConvention));
        }
        // optionality i.e. prepayment right (if specified)
        if (!CommonUtils.isNull(model.cycleOfOptionality)) { 
        events.addAll(EventFactory.createEvents(ScheduleFactory.createSchedule(model.cycleAnchorDateOfOptionality, model.maturityDate,
                                                                            model.cycleOfOptionality, model.endOfMonthConvention),
                                             StringUtils.EventType_SC, model.currency, new POF_AD_PAM(), new STF_AD_PAM(), model.businessDayConvention));
        }
        // termination
        if (!CommonUtils.isNull(model.terminationDate)) {
            ContractEvent termination =
                                        EventFactory.createEvent(model.terminationDate, StringUtils.EventType_TD, model.currency, new POF_AD_PAM(), new STF_AD_PAM());
            events.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
            events.add(termination);
        }
        
        // remove all pre-status date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.statusDate, StringUtils.EventType_SD, model.currency, null,
                                                                  null)) == -1);
        
        // init state space per status date
        initStateSpace();
    }

    @Override
    public ArrayList<ContractEvent> eval(RiskFactorProvider riskFactors) throws AttributeConversionException {
        ArrayList<ContractEvent> payoff = new ArrayList<ContractEvent>(events);

        // add counterparty default risk-factor contingent events
        if(riskFactors.keys().contains(model.legalEntityIDCounterparty)) {
            events.addAll(EventFactory.createEvents(riskFactors.times(model.legalEntityIDCounterparty),
                                             StringUtils.EventType_CD, model.currency, new POF_AD_PAM(), new STF_AD_PAM()));
        }
        
        // add counterparty prepayment risk-factor contingent events
        if(riskFactors.keys().contains(model.objectCodeOfPrepaymentModel) && CommonUtils.isNull(model.cycleOfOptionality)) {
            events.addAll(EventFactory.createEvents(riskFactors.times(model.objectCodeOfPrepaymentModel),
                                             StringUtils.EventType_PR, model.currency, new POF_AD_PAM(), new STF_AD_PAM()));                    
        }
        
        // sort the events in the payoff-list according to their time of occurence
        Collections.sort(payoff);

        // evaluate events
        payoff.forEach(e -> e.eval(states, model, riskFactors, model.dayCountConvention, model.businessDayConvention));
        
        // remove pre-purchase events if purchase date set (we only consider post-purchase events for analysis)
        if(!CommonUtils.isNull(model.purchaseDate)) {
            payoff.removeIf(e -> !e.type().equals(StringUtils.EventType_AD) && e.compareTo(EventFactory.createEvent(model.purchaseDate, StringUtils.EventType_PRD, model.currency, null, null)) == -1);    
        }
        
        // return all evaluated post-StatusDate events as the payoff
        return payoff;
    }

    private void initStateSpace() throws AttributeConversionException {
        states = new StateSpace();

        // TODO: some attributes can be null
        states.lastEventTime = model.statusDate;
        if (!model.initialExchangeDate.isAfter(model.statusDate)) {
            states.nominalValue = model.notionalPrincipal;
            states.nominalRate = model.nominalInterestRate;
            states.nominalAccrued = model.accruedInterest;
            states.scalingMultiplier = 1;
        }
    }

}

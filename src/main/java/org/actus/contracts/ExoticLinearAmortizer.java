/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Set;

import org.actus.AttributeConversionException;
import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.events.ContractEvent;
import org.actus.events.EventFactory;
import org.actus.externals.RiskFactorModelProvider;

import org.actus.functions.PayOffFunction;
import org.actus.functions.StateTransitionFunction;
import org.actus.functions.lam.POF_IPCB_LAM;
import org.actus.functions.lam.POF_IP_LAM;
import org.actus.functions.lam.POF_PRD_LAM;
import org.actus.functions.lam.POF_TD_LAM;
import org.actus.functions.lam.STF_FP_LAM;
import org.actus.functions.lam.STF_IED_LAM;
import org.actus.functions.lam.STF_IPCB_LAM;
import org.actus.functions.lam.STF_IPCI2_LAM;
import org.actus.functions.lam.STF_IPCI_LAM;
import org.actus.functions.lam.STF_PRD_LAM;
import org.actus.functions.lam.STF_SC_LAM;
import org.actus.functions.lax.POF_PI_LAX;
import org.actus.functions.lax.POF_PR_LAX;
import org.actus.functions.lax.STF_PI_LAX;
import org.actus.functions.lax.STF_PI_LAX2;
import org.actus.functions.lax.STF_PR_LAX;
import org.actus.functions.lax.STF_PR_LAX2;
import org.actus.functions.lax.STF_RRF_LAX;
import org.actus.functions.lax.STF_RRY_LAM;
import org.actus.functions.lax.STF_RR_LAX;
import org.actus.functions.pam.POF_AD_PAM;
import org.actus.functions.pam.POF_FP_PAM;
import org.actus.functions.pam.POF_IED_PAM;
import org.actus.functions.pam.POF_IPCI_PAM;
import org.actus.functions.pam.POF_RR_PAM;
import org.actus.functions.pam.POF_SC_PAM;
import org.actus.functions.pam.STF_AD_PAM;
import org.actus.functions.pam.STF_IP_PAM;
import org.actus.functions.pam.STF_TD_PAM;
import org.actus.functions.lam.STF_MD_LAM;
import org.actus.functions.pam.POF_MD_PAM;

import org.actus.states.StateSpace;
import org.actus.time.ScheduleFactory;
import org.actus.types.EventType;
import org.actus.types.InterestCalculationBase;
import org.actus.util.CommonUtils;

/**
 * Represents the Exotic Linear Amortizer payoff algorithm
 * 
 * @see <a href="https://www.actusfrf.org"></a>
 */
public final class ExoticLinearAmortizer {

	// compute next n non-contingent events
	public static ArrayList<ContractEvent> schedule(LocalDateTime to, ContractModelProvider model)
			throws AttributeConversionException {
		ArrayList<ContractEvent> events = new ArrayList<ContractEvent>();

		// determine maturity of the contract
		LocalDateTime maturity = maturity(model);

		// initial exchange
		events.add(EventFactory.createEvent(
				model.getAs("InitialExchangeDate"),
				EventType.IED,
				model.getAs("Currency"),
				new POF_IED_PAM(),
				new STF_IED_LAM(),
				model.getAs("ContractID"))
		);
		
		// purchase event
		if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
			events.add(EventFactory.createEvent(
					model.getAs("PurchaseDate"),
					EventType.PRD,
					model.getAs("Currency"),
					new POF_PRD_LAM(),
					new STF_PRD_LAM(),
					model.getAs("ContractID"))
			);
		}

		// create principal redemption schedule
		if (!CommonUtils.isNull(model.getAs("ArrayCycleAnchorDateOfPrincipalRedemption"))) {

			// parse array-type attributes
			LocalDateTime[] prAnchor = Arrays
					.asList(model.getAs("ArrayCycleAnchorDateOfPrincipalRedemption").toString().replaceAll("\\[", "").replaceAll("\\]","").split(",")).stream()
					.map(d -> LocalDateTime.parse(d.trim())).toArray(LocalDateTime[]::new);
			String[] prCycle = {};
			if (!CommonUtils.isNull(model.getAs("ArrayCycleOfPrincipalRedemption"))) {
				prCycle = Arrays.asList(model.getAs("ArrayCycleOfPrincipalRedemption").toString().replaceAll("\\[", "").replaceAll("\\]","").split(","))
					.stream().map(d -> d.trim()).toArray(String[]::new);
			}
			String[] prPayment = Arrays.asList(model.getAs("ArrayNextPrincipalRedemptionPayment").toString().replaceAll("\\[", "").replaceAll("\\]","").split(","))
					.stream().map(d -> d).toArray(String[]::new);
			String[] prIncDec = Arrays.asList(model.getAs("ArrayIncreaseDecrease").toString().replaceAll("\\[", "").replaceAll("\\]","").split(",")).stream()
					.map(d -> d.trim()).toArray(String[]::new);

			// create array-type schedule with respective increase/decrease features
			EventType prType;
			StateTransitionFunction prStf;
			PayOffFunction prPof;
			int prLen = prAnchor.length + 1;
			LocalDateTime prLocalDate[] = new LocalDateTime[prLen];
			prLocalDate[prLen - 1] = model.getAs("MaturityDate");
			for (int i = 0; i < prAnchor.length; i++) {
				prLocalDate[i] = prAnchor[i];
			}
			for (int i = 0; i < prAnchor.length; i++) {
				
				if (prIncDec[i].trim().equalsIgnoreCase("DEC")) {
					prType = EventType.PR;
					prStf = (!CommonUtils.isNull(model.getAs("InterestCalculationBase"))
							&& model.getAs("InterestCalculationBase").equals(InterestCalculationBase.NTL)) ?
							new STF_PR_LAX(Double.parseDouble(prPayment[i])) : new STF_PR_LAX2(Double.parseDouble(prPayment[i]));
					prPof = new POF_PR_LAX(Double.parseDouble(prPayment[i]));
				} else {
					prType = EventType.PI;
					prStf = (!CommonUtils.isNull(model.getAs("InterestCalculationBase"))
							&& model.getAs("InterestCalculationBase").equals(InterestCalculationBase.NTL)) ?
							new STF_PI_LAX(Double.parseDouble(prPayment[i])) : new STF_PI_LAX2(Double.parseDouble(prPayment[i]));
					prPof = new POF_PI_LAX(Double.parseDouble(prPayment[i]));
				}
				events.addAll(EventFactory.createEvents(
						ScheduleFactory.createSchedule(
								prLocalDate[i],
								prLocalDate[i + 1],
								(prCycle.length>0)? prCycle[i] : null, model.getAs("EndOfMonthConvention"),
								false
						),
						prType,
						model.getAs("Currency"),
						prPof,
						prStf,
						model.getAs("BusinessDayConvention"),
						model.getAs("ContractID"))
				);
			}
		}

		// add maturity event
		events.add(EventFactory.createEvent(
	        maturity,
            EventType.MD,
            model.getAs("Currency"),
            new POF_MD_PAM(),
            new STF_MD_LAM(),
            model.getAs("BusinessDayConvention"),
            model.getAs("ContractID"))
        );

		// create interest payment schedule
		if (!CommonUtils.isNull(model.getAs("ArrayCycleAnchorDateOfInterestPayment"))) {

			// parse array-type attributes
			LocalDateTime[] ipAnchor = Arrays
					.asList(model.getAs("ArrayCycleAnchorDateOfInterestPayment").toString().replaceAll("\\[", "").replaceAll("\\]","").split(",")).stream()
					.map(d -> LocalDateTime.parse(d.trim())).toArray(LocalDateTime[]::new);
			String[] ipCycle = {};
			if (!CommonUtils.isNull(model.getAs("ArrayCycleOfInterestPayment"))) {
				ipCycle = Arrays.asList(model.getAs("ArrayCycleOfInterestPayment").toString().replaceAll("\\[", "").replaceAll("\\]","").split(","))
					.stream().map(d -> d.trim()).toArray(String[]::new);
			}

			// raw interest payment events
			Set<ContractEvent> interestEvents = EventFactory.createEvents(
					ScheduleFactory.createArraySchedule(
							ipAnchor,
							model.getAs("MaturityDate"),
							(ipCycle.length>0)? ipCycle : null,
							model.getAs("EndOfMonthConvention")
					),
					EventType.IP,
					model.getAs("Currency"),
					new POF_IP_LAM(),
					new STF_IP_PAM(),
					model.getAs("BusinessDayConvention"),
					model.getAs("ContractID")
			);
			
			// adapt if interest capitalization set
			if (!CommonUtils.isNull(model.getAs("CapitalizationEndDate"))) {
				
				// define ipci state-transition function
				StateTransitionFunction stf_ipci = (!CommonUtils.isNull(model.getAs("InterestCalculationBase"))
						&& model.getAs("InterestCalculationBase").equals(InterestCalculationBase.NTL)) ? new STF_IPCI_LAM() : new STF_IPCI2_LAM();
						
				// for all events with time <= IPCED && type == "IP" do
				// change type to IPCI and payoff/state-trans functions
				ContractEvent capitalizationEnd = EventFactory.createEvent(
						model.getAs("CapitalizationEndDate"),
						EventType.IPCI,
						model.getAs("Currency"),
						new POF_IPCI_PAM(),
						stf_ipci,
						model.getAs("BusinessDayConvention"),
						model.getAs("ContractID")
				);
				interestEvents.forEach(e -> {
					if (e.eventType().equals(EventType.IP) && e.compareTo(capitalizationEnd) == -1) {
						e.eventType(EventType.IPCI);
						e.fPayOff(new POF_IPCI_PAM());
						e.fStateTrans(stf_ipci);
					}
				});
				
				// also, remove any IP event exactly at IPCED and replace with an IPCI event
				interestEvents.remove(EventFactory.createEvent(model.getAs("CapitalizationEndDate"),
						EventType.IP, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM(),
						model.getAs("BusinessDayConvention"), model.getAs("ContractID")));
			}
			events.addAll(interestEvents);
		} else 
			
			// if no interest schedule defined, still add a capitalization event
			if (!CommonUtils.isNull(model.getAs("CapitalizationEndDate"))) {
			
				// define ipci state-transition function
				StateTransitionFunction stf_ipci = (!CommonUtils.isNull(model.getAs("InterestCalculationBase"))
						&& model.getAs("InterestCalculationBase").equals(InterestCalculationBase.NTL)) ? new STF_IPCI_LAM() : new STF_IPCI2_LAM();
						
				// add single event
				events.add(EventFactory.createEvent(
						model.getAs("CapitalizationEndDate"),
						EventType.IPCI,
						model.getAs("Currency"),
						new POF_IPCI_PAM(),
						stf_ipci,
						model.getAs("BusinessDayConvention"),
						model.getAs("ContractID"))
				);
		}
				
		// create rate reset schedule
		if (!CommonUtils.isNull(model.getAs("ArrayCycleAnchorDateOfRateReset"))) {
			
			// parse array-type attributes
			LocalDateTime[] rrAnchor = Arrays
					.asList(model.getAs("ArrayCycleAnchorDateOfRateReset").toString().replaceAll("\\[", "").replaceAll("\\]","").split(",")).stream()
					.map(d -> LocalDateTime.parse(d.trim())).toArray(LocalDateTime[]::new);
			String[] rrCycle = {};
			if (!CommonUtils.isNull(model.getAs("ArrayCycleOfRateReset"))) {
				rrCycle = Arrays.asList(model.getAs("ArrayCycleOfRateReset").toString().replaceAll("\\[", "").replaceAll("\\]","").split(","))
					.stream().map(d -> d.trim()).toArray(String[]::new);
			}
			String[] rrRate = Arrays.asList(model.getAs("ArrayRate").toString().replaceAll("\\[", "").replaceAll("\\]","").split(",")).stream().map(d -> d.trim())
					.toArray(String[]::new);
			String[] rrFidedVar = Arrays.asList(model.getAs("ArrayFixedVariable").toString().replaceAll("\\[", "").replaceAll("\\]","").split(",")).stream()
					.map(d -> d.trim()).toArray(String[]::new);
			
			// create array-type schedule with fix/var features
			EventType rrType;
			StateTransitionFunction rrStf;
			Set<ContractEvent> rateResetEvents = null;
			int rrLen = rrAnchor.length + 1;
			LocalDateTime rrLocalDate[] = new LocalDateTime[rrLen];
			rrLocalDate[rrLen - 1] = model.getAs("MaturityDate");
			for (int i = 0; i < rrAnchor.length; i++) {
				rrLocalDate[i] = rrAnchor[i];
			}
			for (int i = 0; i < rrAnchor.length; i++) {
				if (rrFidedVar[i].trim().equalsIgnoreCase("FIX")) {
					rrType = EventType.RRF;
					rrStf = new STF_RRF_LAX(Double.parseDouble(rrRate[i]));
				} else {
					rrType = EventType.RR;
					rrStf = new STF_RR_LAX(Double.parseDouble(rrRate[i]));
				}
				rateResetEvents = EventFactory.createEvents(
						ScheduleFactory.createSchedule(
								rrLocalDate[i],
								rrLocalDate[i + 1],
								(rrCycle.length>0)? rrCycle[i] : null,
								model.getAs("EndOfMonthConvention"),
								false
						),
						rrType,
						model.getAs("Currency"),
						new POF_RR_PAM(),
						rrStf,
						model.getAs("BusinessDayConvention"),
						model.getAs("ContractID")
				);
				events.addAll(rateResetEvents);
			}
			
			// adjust for already fixed reset rates
			if (!CommonUtils.isNull(model.getAs("NextResetRate"))) {
				rateResetEvents.stream().sorted()
						.filter(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"),
								EventType.AD, model.getAs("Currency"), null, null, model.getAs("ContractID"))) == 1)
						.findFirst().get().fStateTrans(new STF_RRY_LAM());
				events.addAll(rateResetEvents);
			}	
		}
				
		// fee schedule
		if (!CommonUtils.isNull(model.getAs("CycleOfFee"))) {
			events.addAll(EventFactory.createEvents(
					ScheduleFactory.createSchedule(
							model.getAs("CycleAnchorDateOfFee"),
							maturity,
							model.getAs("CycleOfFee"),
							model.getAs("EndOfMonthConvention")
					),
					EventType.FP,
					model.getAs("Currency"),
					new POF_FP_PAM(),
					new STF_FP_LAM(),
					model.getAs("BusinessDayConvention"),
					model.getAs("ContractID"))
			);
		}
		
		// scaling (if specified)
		if (!CommonUtils.isNull(model.getAs("ScalingEffect")) && (model.getAs("ScalingEffect").toString().contains("I")
				|| model.getAs("ScalingEffect").toString().contains("N"))) {
			events.addAll(EventFactory.createEvents(
					ScheduleFactory.createSchedule(
							model.getAs("CycleAnchorDateOfScalingIndex"),
							maturity,
							model.getAs("CycleOfScalingIndex"),
							model.getAs("EndOfMonthConvention"),
							false
					),
					EventType.SC,
					model.getAs("Currency"),
					new POF_SC_PAM(),
					new STF_SC_LAM(),
					model.getAs("BusinessDayConvention"),
					model.getAs("ContractID"))
			);
		}
		
		// interest calculation base (if specified)
		if (!CommonUtils.isNull(model.getAs("InterestCalculationBase"))
				&& model.getAs("InterestCalculationBase").equals(InterestCalculationBase.NTL)) {
			events.addAll(EventFactory.createEvents(
					ScheduleFactory.createSchedule(
							model.getAs("CycleAnchorDateOfInterestCalculationBase"),
							maturity,
							model.getAs("CycleOfInterestCalculationBase"),
							model.getAs("EndOfMonthConvention"),
							false
					),
					EventType.IPCB,
					model.getAs("Currency"),
					new POF_IPCB_LAM(),
					new STF_IPCB_LAM(),
					model.getAs("BusinessDayConvention"),
					model.getAs("ContractID"))
			);
		}
		
		// termination
		if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
			ContractEvent termination = EventFactory.createEvent(
					model.getAs("TerminationDate"),
					EventType.TD,
					model.getAs("Currency"),
					new POF_TD_LAM(),
					new STF_TD_PAM(),
					model.getAs("ContractID")
			);
			events.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
			events.add(termination);
		}

		// remove all pre-status date events
		events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), EventType.AD,model.getAs("Currency"), null, null, model.getAs("ContractID"))) == -1);

		// remove all post to-date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(to, EventType.AD, model.getAs("Currency"), null, null, model.getAs("ContractID"))) == 1);

		// sort the events in the payoff-list according to their time of occurence
		Collections.sort(events);

		return events;
	}

	// apply a set of events to the current state of a contract and return the post
	// events state
	public static ArrayList<ContractEvent> apply(ArrayList<ContractEvent> events, ContractModelProvider model,
			RiskFactorModelProvider observer) throws AttributeConversionException {

        // initialize state space per status date
        StateSpace states = initStateSpace(model,maturity(model));

        // sort the events according to their time sequence
        Collections.sort(events);

        // apply events according to their time sequence to current state
		ListIterator<ContractEvent> eventIterator = events.listIterator();
        //while (( states.statusDate.isBefore(initialExchangeDate) || states.notionalPrincipal != 0.0) && eventIterator.hasNext()) {
        while (eventIterator.hasNext()) {
                ((ContractEvent) eventIterator.next()).eval(states, model, observer, model.getAs("DayCountConvention"),
                    model.getAs("BusinessDayConvention"));
        }
        
        // remove pre-purchase events if purchase date set
        if(!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            events.removeIf(e -> !e.eventType().equals(EventType.AD) && e.compareTo(EventFactory.createEvent(model.getAs("PurchaseDate"), EventType.PRD, model.getAs("Currency"), null, null, model.getAs("ContractID"))) == -1);
		}
		
        // return evaluated events
        return events;
	}

	private static LocalDateTime maturity(ContractModelProvider model) {

		// determine maturity of the contract
		LocalDateTime maturity = model.getAs("MaturityDate");

		if(CommonUtils.isNull(maturity)){
			LocalDateTime calculatedTime;
			double notionalPrincipal = model.getAs("NotionalPrincipal");
			LocalDateTime[] prAnchor = Arrays
					.asList(model.getAs("ArrayCycleAnchorDateOfPrincipalRedemption").toString().split(",")).stream()
					.map(LocalDateTime::parse).toArray(LocalDateTime[]::new);
			int upperBound;
		}
		return maturity;
	}

	private static StateSpace initStateSpace(ContractModelProvider model, LocalDateTime maturity)
			throws AttributeConversionException {
		StateSpace states = new StateSpace();

		// general states to be initialized
		states.statusDate = model.getAs("StatusDate");
		states.notionalScalingMultiplier = 1;
		states.interestScalingMultiplier = 1;
		
		if(model.<LocalDateTime>getAs("InitialExchangeDate").isAfter(model.getAs("StatusDate"))){
            states.notionalPrincipal = 0.0;
            states.nominalInterestRate = 0.0;
            states.interestCalculationBaseAmount = 0.0;
        }else{
			states.notionalPrincipal = ContractRoleConvention.roleSign(model.getAs("ContractRole"))
					* model.<Double>getAs("NotionalPrincipal");
			states.nominalInterestRate = model.getAs("NominalInterestRate");
			states.accruedInterest = ContractRoleConvention.roleSign(model.getAs("ContractRole"))
					* model.<Double>getAs("AccruedInterest");
			states.feeAccrued = model.getAs("FeeAccrued");
			if(InterestCalculationBase.NT.equals(model.getAs("InterestCalculationBase"))){
                states.interestCalculationBaseAmount = states.notionalPrincipal; // contractRole applied at notionalPrincipal init
            }else{
                states.interestCalculationBaseAmount = ContractRoleConvention.roleSign(model.getAs("ContractRole")) * model.<Double>getAs("InterestCalculationBaseAmount");
            }
		}
		return states;
	}

}

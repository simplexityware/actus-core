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
import java.util.Set;

import org.actus.AttributeConversionException;
import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.events.ContractEvent;
import org.actus.events.EventFactory;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.fu.STF_RR_LAX;
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
import org.actus.functions.pam.POF_AD_PAM;
import org.actus.functions.pam.POF_FP_PAM;
import org.actus.functions.pam.POF_IED_PAM;
import org.actus.functions.pam.POF_IPCI_PAM;
import org.actus.functions.pam.POF_RR_PAM;
import org.actus.functions.pam.POF_SC_PAM;
import org.actus.functions.pam.STF_AD_PAM;
import org.actus.functions.pam.STF_IP_PAM;
import org.actus.functions.pam.STF_TD_PAM;
import org.actus.states.StateSpace;
import org.actus.time.ScheduleFactory;
import org.actus.util.CommonUtils;
import org.actus.util.StringUtils;

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
		events.add(EventFactory.createEvent(model.getAs("InitialExchangeDate"), StringUtils.EventType_IED,
				model.getAs("Currency"), new POF_IED_PAM(), new STF_IED_LAM()));
		
		// purchase event
		if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
			events.add(EventFactory.createEvent(model.getAs("PurchaseDate"), StringUtils.EventType_PRD,
					model.getAs("Currency"), new POF_PRD_LAM(), new STF_PRD_LAM()));
		}

		// create principal redemption schedule
		if (!CommonUtils.isNull(model.getAs("ArrayCycleAnchorDateOfPrincipalRedemption"))) {

			// parse array-type attributes
			LocalDateTime[] prAnchor = Arrays
					.asList(model.getAs("ArrayCycleAnchorDateOfPrincipalRedemption").toString().split(",")).stream()
					.map(d -> LocalDateTime.parse(d)).toArray(LocalDateTime[]::new);
			String[] prCycle = Arrays.asList(model.getAs("ArrayCycleOfPrincipalRedemption").toString().split(","))
					.stream().map(d -> d).toArray(String[]::new);
			String[] prPayment = Arrays.asList(model.getAs("ArrayNextPrincipalRedemptionPayment").toString().split(","))
					.stream().map(d -> d).toArray(String[]::new);
			String[] prIncDec = Arrays.asList(model.getAs("ArrayIncreaseDecrease").toString().split(",")).stream()
					.map(d -> d).toArray(String[]::new);

			// create array-type schedule with respective increase/decrease features
			String prType;
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
					prType = StringUtils.EventType_PR;
					prStf = (!CommonUtils.isNull(model.getAs("InterestCalculationBase"))
							&& model.getAs("InterestCalculationBase").equals("NTL")) ? 
							new STF_PR_LAX(Double.parseDouble(prPayment[i])) : new STF_PR_LAX2(Double.parseDouble(prPayment[i]));
					prPof = new POF_PR_LAX(Double.parseDouble(prPayment[i]));
				} else {
					prType = StringUtils.EventType_PI;
					prStf = (!CommonUtils.isNull(model.getAs("InterestCalculationBase"))
							&& model.getAs("InterestCalculationBase").equals("NTL")) ? 
							new STF_PI_LAX(Double.parseDouble(prPayment[i])) : new STF_PI_LAX2(Double.parseDouble(prPayment[i]));
					prPof = new POF_PI_LAX(Double.parseDouble(prPayment[i]));
				}
				events.addAll(EventFactory.createEvents(
						ScheduleFactory.createSchedule(prLocalDate[i], prLocalDate[i + 1], prCycle[i],
								model.getAs("EndOfMonthConvention"), false),
						prType, model.getAs("Currency"), prPof, prStf, model.getAs("BusinessDayConvention")));
			}
		}

		// create interest payment schedule
		if (!CommonUtils.isNull(model.getAs("ArrayCycleAnchorDateOfInterestPayment"))) {

			// parse array-type attributes
			LocalDateTime[] ipAnchor = Arrays
					.asList(model.getAs("ArrayCycleAnchorDateOfInterestPayment").toString().split(",")).stream()
					.map(d -> LocalDateTime.parse(d)).toArray(LocalDateTime[]::new);
			String[] ipCycle = Arrays.asList(model.getAs("ArrayCycleOfInterestPayment").toString().split(",")).stream()
					.map(d -> d).toArray(String[]::new);

			// raw interest payment events
			Set<ContractEvent> interestEvents = EventFactory.createEvents(
					ScheduleFactory.createArraySchedule(ipAnchor, model.getAs("MaturityDate"), ipCycle,
							model.getAs("EndOfMonthConvention")),
					StringUtils.EventType_IP, model.getAs("Currency"), new POF_IP_LAM(), new STF_IP_PAM(),
					model.getAs("BusinessDayConvention"));
			
			// adapt if interest capitalization set
			if (!CommonUtils.isNull(model.getAs("CapitalizationEndDate"))) {
				
				// define ipci state-transition function
				StateTransitionFunction stf_ipci = (!CommonUtils.isNull(model.getAs("InterestCalculationBase"))
						&& model.getAs("InterestCalculationBase").equals("NTL")) ? new STF_IPCI_LAM() : new STF_IPCI2_LAM();
						
				// for all events with time <= IPCED && type == "IP" do
				// change type to IPCI and payoff/state-trans functions
				ContractEvent capitalizationEnd = EventFactory.createEvent(model.getAs("CapitalizationEndDate"),
						StringUtils.EventType_IPCI, model.getAs("Currency"), new POF_IPCI_PAM(), stf_ipci,
						model.getAs("BusinessDayConvention"));
				interestEvents.forEach(e -> {
					if (e.type().equals(StringUtils.EventType_IP) && e.compareTo(capitalizationEnd) == -1) {
						e.type(StringUtils.EventType_IPCI);
						e.fPayOff(new POF_IPCI_PAM());
						e.fStateTrans(stf_ipci);
					}
				});
				
				// also, remove any IP event exactly at IPCED and replace with an IPCI event
				interestEvents.remove(EventFactory.createEvent(model.getAs("CapitalizationEndDate"),
						StringUtils.EventType_IP, model.getAs("Currency"), new POF_AD_PAM(), new STF_AD_PAM(),
						model.getAs("BusinessDayConvention")));
			}
			events.addAll(interestEvents);
		} else 
			
			// if no interest schedule defined, still add a capitalization event
			if (!CommonUtils.isNull(model.getAs("CapitalizationEndDate"))) {
			
				// define ipci state-transition function
				StateTransitionFunction stf_ipci = (!CommonUtils.isNull(model.getAs("InterestCalculationBase"))
						&& model.getAs("InterestCalculationBase").equals("NTL")) ? new STF_IPCI_LAM() : new STF_IPCI2_LAM();
						
				// add single event
				events.add(EventFactory.createEvent(model.getAs("CapitalizationEndDate"), StringUtils.EventType_IPCI,
						model.getAs("Currency"), new POF_IPCI_PAM(), stf_ipci, model.getAs("BusinessDayConvention")));
		}
		
		// create rate reset schedule
		if (!CommonUtils.isNull(model.getAs("ArrayCycleAnchorDateOfRateReset"))) {
			
			// parse array-type attributes
			LocalDateTime[] rrAnchor = Arrays
					.asList(model.getAs("ArrayCycleAnchorDateOfRateReset").toString().split(",")).stream()
					.map(d -> LocalDateTime.parse(d)).toArray(LocalDateTime[]::new);
			String[] rrRate = Arrays.asList(model.getAs("ArrayRate").toString().split(",")).stream().map(d -> d)
					.toArray(String[]::new);
			String[] rrCycle = Arrays.asList(model.getAs("ArrayCycleOfRateReset").toString().split(",")).stream()
					.map(d -> d).toArray(String[]::new);
			String[] rrFidedVar = Arrays.asList(model.getAs("ArrayFixedVariable").toString().split(",")).stream()
					.map(d -> d).toArray(String[]::new);
			
			// create array-type schedule with fix/var features
			String rrType;
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
					rrType = StringUtils.EventType_RRF;
					rrStf = new STF_RRF_LAX(Double.parseDouble(rrRate[i]));
				} else {
					rrType = StringUtils.EventType_RR;
					rrStf = new STF_RR_LAX(Double.parseDouble(rrRate[i]));
				}
				rateResetEvents = EventFactory.createEvents(
						ScheduleFactory.createSchedule(rrLocalDate[i], rrLocalDate[i + 1], rrCycle[i],
								model.getAs("EndOfMonthConvention"), false),
						rrType, model.getAs("Currency"), new POF_RR_PAM(), rrStf, model.getAs("BusinessDayConvention"));
				events.addAll(rateResetEvents);
			}
			
			// adjust for already fixed reset rates
			if (!CommonUtils.isNull(model.getAs("NextResetRate"))) {
				rateResetEvents.stream().sorted()
						.filter(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"),
								StringUtils.EventType_SD, model.getAs("Currency"), null, null)) == 1)
						.findFirst().get().fStateTrans(new STF_RRY_LAM());
				events.addAll(rateResetEvents);
			}	
		}
		
		// fee schedule
		if (!CommonUtils.isNull(model.getAs("CycleOfFee"))) {
			events.addAll(EventFactory.createEvents(
					ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfFee"), maturity,
							model.getAs("CycleOfFee"), model.getAs("EndOfMonthConvention")),
					StringUtils.EventType_FP, model.getAs("Currency"), new POF_FP_PAM(), new STF_FP_LAM(),
					model.getAs("BusinessDayConvention")));
		}
		
		// scaling (if specified)
		if (!CommonUtils.isNull(model.getAs("ScalingEffect")) && (model.<String>getAs("ScalingEffect").contains("I")
				|| model.<String>getAs("ScalingEffect").contains("N"))) {
			events.addAll(EventFactory.createEvents(
					ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfScalingIndex"), maturity,
							model.getAs("CycleOfScalingIndex"), model.getAs("EndOfMonthConvention"), false),
					StringUtils.EventType_SC, model.getAs("Currency"), new POF_SC_PAM(), new STF_SC_LAM(),
					model.getAs("BusinessDayConvention")));
		}
		
		// interest calculation base (if specified)
		if (!CommonUtils.isNull(model.getAs("InterestCalculationBase"))
				&& model.getAs("InterestCalculationBase").equals("NTL")) {
			events.addAll(EventFactory.createEvents(
					ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfInterestCalculationBase"), maturity,
							model.getAs("CycleOfInterestCalculationBase"), model.getAs("EndOfMonthConvention"), false),
					StringUtils.EventType_IPCB, model.getAs("Currency"), new POF_IPCB_LAM(), new STF_IPCB_LAM(),
					model.getAs("BusinessDayConvention")));
		}
		
		// termination
		if (!CommonUtils.isNull(model.getAs("TerminationDate"))) {
			ContractEvent termination = EventFactory.createEvent(model.getAs("TerminationDate"),
					StringUtils.EventType_TD, model.getAs("Currency"), new POF_TD_LAM(), new STF_TD_PAM());
			events.removeIf(e -> e.compareTo(termination) == 1); // remove all post-termination events
			events.add(termination);
		}

		// remove all pre-status date events
		events.removeIf(e -> e.compareTo(EventFactory.createEvent(model.getAs("StatusDate"), StringUtils.EventType_SD,model.getAs("Currency"), null, null)) == -1);

		// remove all post to-date events
        events.removeIf(e -> e.compareTo(EventFactory.createEvent(to, StringUtils.EventType_AD, model.getAs("Currency"), null, null)) == 1);

		// sort the events in the payoff-list according to their time of occurence
		Collections.sort(events);
		
		return events;
	}

	// apply a set of events to the current state of a contract and return the post
	// events state
	public static ArrayList<ContractEvent> apply(ArrayList<ContractEvent> events, ContractModelProvider model,
			RiskFactorModelProvider observer) throws AttributeConversionException {

		// initialize state space per status date
		StateSpace states = initStateSpace(model, maturity(model));

		// sort the events according to their time sequence
		Collections.sort(events);

		// apply events according to their time sequence to current state
		events.forEach(e -> e.eval(states, model, observer, model.getAs("DayCountConvention"),
				model.getAs("BusinessDayConvention")));

		// return evaluated events
		return events;
	}

	private static LocalDateTime maturity(ContractModelProvider model) {

		// determine maturity of the contract
		LocalDateTime maturity = model.getAs("MaturityDate");

		// TODO: what if maturity is null?

		return maturity;
	}

	private static StateSpace initStateSpace(ContractModelProvider model, LocalDateTime maturity)
			throws AttributeConversionException {
		StateSpace states = new StateSpace();

		// general states to be initialized
		states.lastEventTime = model.getAs("StatusDate");
		states.nominalScalingMultiplier = 1;
		states.interestScalingMultiplier = 1;

		
		if (!model.<LocalDateTime>getAs("InitialExchangeDate").isAfter(model.getAs("StatusDate"))) {
			states.nominalValue = ContractRoleConvention.roleSign(model.getAs("ContractRole"))
					* model.<Double>getAs("NotionalPrincipal");
			states.nominalRate = model.getAs("NominalInterestRate");
			states.nominalAccrued = ContractRoleConvention.roleSign(model.getAs("ContractRole"))
					* model.<Double>getAs("AccruedInterest");
			states.feeAccrued = model.getAs("FeeAccrued");
			if (CommonUtils.isNull(model.getAs("InterestCalculationBase"))
					|| model.getAs("InterestCalculationBase").equals("NT")) {
				states.interestCalculationBase = ContractRoleConvention.roleSign(model.getAs("ContractRole"))
						* model.<Double>getAs("NotionalPrincipal");
			} else {
				states.interestCalculationBase = ContractRoleConvention.roleSign(model.getAs("ContractRole"))
						* model.<Double>getAs("InterestCalculationBaseAmount");
			}
		}
		return states;
	}

}

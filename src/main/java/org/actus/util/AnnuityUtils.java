/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.util;

import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.states.StateSpace;
import org.actus.time.ScheduleFactory;

import java.util.Set;
import java.util.Arrays;
import java.time.LocalDateTime;

/**
 * A utility class for Annuity
 * <p>
 */
public class AnnuityUtils {

    // this is a pure utility class
	private AnnuityUtils() {
	}

	/**
	 * Calculate the NextPrincipalRedemption amount
	 * <p>
	 * @param model
	 *            the model carrying the contract attributes
	 * @param state
	 * @return the annuity payment amount
	 */
	public static double annuityPayment(ContractModelProvider model, StateSpace state) {

		Double annuityPayment;


		LocalDateTime statusDate = state.statusDate;
		LocalDateTime maturity = state.maturityDate;
		double accruedInterest = state.accruedInterest;
		double outstandingNotional = state.notionalPrincipal;
		double interestRate = state.nominalInterestRate;

		// extract day count convention
		DayCountCalculator dayCounter = model.getAs("DayCountConvention");

		// determine remaining PR schedule
		Set<LocalDateTime> eventTimes = ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfPrincipalRedemption"), maturity, model.getAs("CycleOfPrincipalRedemption"), model.getAs("EndOfMonthConvention"),true);
		eventTimes.removeIf(d -> d.isBefore(statusDate));
		eventTimes.remove(statusDate);
		LocalDateTime[] eventTimesSorted = eventTimes.toArray(new LocalDateTime[eventTimes.size()]);
		Arrays.sort(eventTimesSorted);

		// compute annuityPayment
		int lb = 1;
		int ub = eventTimesSorted.length;
		double scale = outstandingNotional + accruedInterest;
		double sum = sum(lb, ub, eventTimesSorted, interestRate, dayCounter);
		double frac = product(lb, ub, eventTimesSorted, interestRate, dayCounter) / (1.0 + sum);
		annuityPayment = scale * frac;

		// finally, return the annuity payment
		return annuityPayment;
	}

	// private method
	private static double product(int lb, int ub, LocalDateTime[] times,
			double ir, DayCountCalculator dayCounter) {
		double prod = 1.0;
		for (int i = lb; i < ub; i++) {
			prod *= effectiveRate(i, times, ir, dayCounter);
		}
		return prod;
	}

	// private method
	private static double sum(int lb, int ub, LocalDateTime[] times, double ir,
			DayCountCalculator dayCounter) {
		double sum = 0.0;
		for (int i = lb; i < ub; i++) {
			sum += product(i, ub, times, ir, dayCounter);
		}
		return sum;
	}

	// private method
	private static double effectiveRate(int index, LocalDateTime[] times,
			double ir, DayCountCalculator dayCounter) {
		double yf;
		yf = dayCounter.dayCountFraction(times[index - 1], times[index]);
		return 1.0 + ir * yf;
	}
}

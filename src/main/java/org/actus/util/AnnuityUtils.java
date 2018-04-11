/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.util;

import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
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
	 * @param outstandingNotional
	 *            the outstanding notional value
	 * @param accruedInterest
	 *            current balance of accrued interest
	 * @param interestRate
	 *            the nominal interest rate valid at this date
	 * @param dayCounter
	 *            the day counter used for interest calculation
	 * @param model
	 *            the model carrying the contract attributes
	 * @return the annuity payment amount
	 */
	public static double annuityPayment(double outstandingNotional,
			double accruedInterest, double interestRate,
			DayCountCalculator dayCounter, ContractModelProvider model) {
		LocalDateTime maturity = null;
		if(!CommonUtils.isNull(model.getAs("MaturityDate"))) {
			maturity = model.getAs("MaturityDate");
		} else if(!CommonUtils.isNull(model.getAs("AmortizationDate"))) {
			maturity = model.getAs("AmortizationDate");
		}
		Set<LocalDateTime> eventTimes = ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfPrincipalRedemption"),maturity,model.getAs("CycleOfPrincipalRedemption"), model.getAs("EndOfMonthConvention"));
        eventTimes.removeIf( d -> d.isBefore(model.getAs("StatusDate")) );
        eventTimes.remove(model.getAs("StatusDate"));
        LocalDateTime[] eventTimesSorted = eventTimes.toArray(new LocalDateTime[eventTimes.size()]);
        Arrays.sort(eventTimesSorted);
		int lb = 1;
		int ub = eventTimesSorted.length;
		double scale = (outstandingNotional + accruedInterest);
		double sum = sum(lb, ub, eventTimesSorted, interestRate, dayCounter);
		double frac = product(lb, ub, eventTimesSorted, interestRate, dayCounter) / (1 + sum);
		return scale * frac;
	}

	// private method
	private static double product(int lb, int ub, LocalDateTime[] times,
			double ir, DayCountCalculator dayCounter) {
		double prod = 1;
		for (int i = lb; i < ub; i++) {
			prod *= effectiveRate(i, times, ir, dayCounter);
		}
		return prod;
	}

	// private method
	private static double sum(int lb, int ub, LocalDateTime[] times, double ir,
			DayCountCalculator dayCounter) {
		double sum = 0;
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
		return 1 + ir * yf;
	}
}

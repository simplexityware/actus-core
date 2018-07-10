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
	 * @param model
	 *            the model carrying the contract attributes
	 * @param accruedInterest
	 * 	 *            current (as per StatusDate) balance of accrued interest
	 * @return the annuity payment amount
	 */
	public static double annuityPayment(ContractModelProvider model, double outstandingNotional, double accruedInterest, double interestRate) {

		// extract PRNXT from model
		Double annuityPayment = model.<Double>getAs("NextPrincipalRedemptionPayment");

		// if PRNXT not defined, then calculate
		if(CommonUtils.isNull(annuityPayment)) {

			LocalDateTime statusDate = model.getAs("StatusDate");

			// determine maturity
			// note, if PRNXT=NULL then either MD or AMD has to be set
			LocalDateTime maturity = model.getAs("MaturityDate");
			if (CommonUtils.isNull(maturity)) {
				maturity = model.getAs("AmortizationDate");
			}

			// extract day count convention
			DayCountCalculator dayCounter = model.getAs("DayCountConvention");

			// determine remaining PR schedule
			Set<LocalDateTime> eventTimes = ScheduleFactory.createSchedule(model.getAs("CycleAnchorDateOfPrincipalRedemption"), maturity, model.getAs("CycleOfPrincipalRedemption"), model.getAs("EndOfMonthConvention"));
			eventTimes.removeIf(d -> d.isBefore(statusDate));
			eventTimes.remove(statusDate);
			LocalDateTime[] eventTimesSorted = eventTimes.toArray(new LocalDateTime[eventTimes.size()]);
			Arrays.sort(eventTimesSorted);

			// determine accrued interest as per next PR event date
			accruedInterest += outstandingNotional * interestRate * dayCounter.dayCountFraction(statusDate,eventTimesSorted[0]);

			// compute annuityPayment
			int lb = 1;
			int ub = eventTimesSorted.length;
			double scale = Math.abs(outstandingNotional + accruedInterest); // for CNTRL=RPL this is negative
			double sum = sum(lb, ub, eventTimesSorted, interestRate, dayCounter);
			double frac = product(lb, ub, eventTimesSorted, interestRate, dayCounter) / (1 + sum);
			annuityPayment = scale*frac;
		}

		// finally, return the annuity payment
		return annuityPayment;
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

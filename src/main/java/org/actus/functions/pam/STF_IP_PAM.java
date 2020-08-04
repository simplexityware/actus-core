/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.functions.pam;

import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.functions.StateTransitionFunction;
import org.actus.states.StateSpace;
import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;
import org.actus.time.ScheduleFactory;
import org.actus.types.ContractRole;
import org.actus.types.FeeBasis;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class STF_IP_PAM implements StateTransitionFunction {
    
    @Override
    public StateSpace eval(LocalDateTime time, StateSpace states,
    ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        StateSpace postEventStates = new StateSpace();
        Set<LocalDateTime> feePaymentSchedule = ScheduleFactory.createSchedule(
                model.getAs("CycleAnchorDateOfFee"),
                states.maturityDate,
                model.getAs("CycleOfFee"),
                model.getAs("EndOfMonthConvention")
        );
        LocalDateTime prevTimePoint = null;
        LocalDateTime nextTimePoint = null;
        Iterator<LocalDateTime> schedule = feePaymentSchedule.iterator();
        while(schedule.hasNext()){
            LocalDateTime timePoint = schedule.next();
            if(timePoint.isBefore(time)){
                prevTimePoint = timePoint;
            }
            if(timePoint.isAfter(time)){
                nextTimePoint = timePoint;
                break;
            }
        }
        // update state space
        states.accruedInterest = 0.0;
        //if(FeeBasis.N.equals(model.<FeeBasis>getAs("FeeBasis"))) {
            states.feeAccrued += dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(states.statusDate), timeAdjuster.shiftCalcTime(time))
                    * model.<Double>getAs("FeeRate")
                    * states.notionalPrincipal;
        /*}else {
            states.feeAccrued += (dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(prevTimePoint), timeAdjuster.shiftCalcTime(time))
                    / dayCounter.dayCountFraction(timeAdjuster.shiftCalcTime(prevTimePoint), timeAdjuster.shiftCalcTime(nextTimePoint)))
                    * ContractRoleConvention.roleSign(model.<ContractRole>getAs("ContractRole"))
                    * model.<Double>getAs("FeeRate");
        }*/

        states.statusDate = time;
        
        // copy post-event-states
        postEventStates.notionalPrincipal = states.notionalPrincipal;
        postEventStates.nominalInterestRate = states.nominalInterestRate;
        postEventStates.feeAccrued = states.feeAccrued;
        postEventStates.statusDate = states.statusDate;
        
        // return post-event-states
        return postEventStates;
        }
    
}

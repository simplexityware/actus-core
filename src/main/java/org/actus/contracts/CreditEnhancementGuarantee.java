/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.contracts;

import org.actus.AttributeConversionException;
import org.actus.attributes.ContractModel;
import org.actus.attributes.ContractModelProvider;
import org.actus.conventions.contractrole.ContractRoleConvention;
import org.actus.events.ContractEvent;
import org.actus.events.EventFactory;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.functions.ceg.*;
import org.actus.functions.optns.*;
import org.actus.functions.stk.POF_PRD_STK;
import org.actus.functions.stk.STF_TD_STK;
import org.actus.states.StateSpace;
import org.actus.time.ScheduleFactory;
import org.actus.types.ContractReference;
import org.actus.types.EventType;
import org.actus.types.GuaranteedExposure;
import org.actus.types.ReferenceType;
import org.actus.util.CommonUtils;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the Credit Enhancement Guarantee contract algorithm
 *
 * @see <a https://www.actusfrf.org"></a>
 */
public class CreditEnhancementGuarantee {
    // forward projection of the entire lifecycle of the contract
    public static ArrayList<ContractEvent> schedule(LocalDateTime to,
                                                    ContractModelProvider model) throws AttributeConversionException {
        ArrayList<ContractEvent> events = new ArrayList<>();
        // purchase
        if (!CommonUtils.isNull(model.getAs("PurchaseDate"))) {
            events.add(EventFactory.createEvent(model.getAs("PurchaseDate"), EventType.PRD, model.getAs("Currency"), new POF_PRD_STK(), new STF_PRD_CEG(), model.getAs("ContractID")));
        }

        // fees (if specified)
        if(!(CommonUtils.isNull(model.getAs("FeeRate")) || model.<Double>getAs("FeeRate") == 0.0)){
            LocalDateTime startDate;
            LocalDateTime endDate;
            if(CommonUtils.isNull(model.getAs("CycleAnchorDateOfFee")) && CommonUtils.isNull(model.getAs("CycleOfFee"))){
                startDate = null;
            }else if(CommonUtils.isNull(model.getAs("CycleAnchorDateOfFee"))){
                startDate = model.<LocalDateTime>getAs("PurchaseDate").plus(Period.parse(model.getAs("CycleOfFee")));
            }else{
                startDate = model.getAs("CycleAnchorDateOfFee");
            }
            if(CommonUtils.isNull(model.getAs("ExerciseDate"))){
                endDate = model.getAs("MaturityDate");
            }else {
                endDate = model.getAs("ExerciseDate");
            }
            events.addAll(EventFactory.createEvents(
                    ScheduleFactory.createSchedule(
                            startDate,
                            endDate,
                            model.getAs("CycleOfFee"),
                            model.getAs("EndOfMonthConvention")),
                    EventType.FP,
                    model.getAs("Currency"),
                    new POF_FP_CEG(),
                    new STF_FP_CEG(),
                    model.getAs("BusinessDayConvention"),
                    model.getAs("ContractID"))
            );
        }

        // maturity
        if(CommonUtils.isNull(model.getAs("ExerciseDate"))){
            events.add(EventFactory.createEvent(model.getAs("MaturityDate"), EventType.MD, model.getAs("Currency"), new POF_MD_CEG(), new STF_MD_CEG(), model.getAs("ContractID")));
        }

        //exercise
        if(!CommonUtils.isNull(model.getAs("ExerciseDate"))){
            events.add(EventFactory.createEvent(model.getAs("ExerciseDate"), EventType.XD, model.getAs("Currency"), new POF_XD_OPTNS(), new STF_XD_CEG(), model.getAs("BusinessDayConvention"), model.getAs("ContractID")));
            events.add(EventFactory.createEvent(model.<LocalDateTime>getAs("ExerciseDate").plus(model.getAs("SettlementPeriod")), EventType.STD, model.getAs("Currency"), new POF_STD_CEG(), new STF_STD_CEG(), model.getAs("BusinessDayConvention"), model.getAs("ContractID")));
        }
        return events;
    }

    // apply a set of events to the current state of a contract and return the post events state
    public static ArrayList<ContractEvent> apply(ArrayList<ContractEvent> events,
                                                 ContractModelProvider model,
                                                 RiskFactorModelProvider observer) throws AttributeConversionException {
        //Add external XD-event
        events.addAll(observer.events(model));

        // initialize state space per status date
        StateSpace states = initStateSpace(model);

        // sort the events according to their time sequence
        Collections.sort(events);

        // apply events according to their time sequence to current state
        events.forEach(e -> e.eval(states, model, observer, model.getAs("DayCountConvention"), model.getAs("BusinessDayConvention")));

        // return post events states
        return events;
    }

    // determine maturity of the contract
    private static LocalDateTime maturity(ContractModelProvider model) {
        LocalDateTime maturityDate = null;
        if(!CommonUtils.isNull(model.getAs("MaturityDate"))){
            maturityDate = model.getAs("MaturityDate");
        } else{
            List<ContractReference> contractStructure = model.getAs("ContractStructure");
            List<LocalDateTime> maturityDates = new ArrayList<>();
            contractStructure.forEach(c -> {
                maturityDates.add(LocalDateTime.parse(c.getContractAttribute("MaturityDate")));
            }) ;
            Collections.sort(maturityDates);
            maturityDate = maturityDates.get(maturityDates.size()-1);
        }
        return maturityDate;
    }

    private static StateSpace initStateSpace(ContractModelProvider model) throws AttributeConversionException {
        StateSpace states = new StateSpace();
        states.maturityDate = maturity(model);
        states.statusDate = model.getAs("StatusDate");
        if(states.statusDate.isAfter(states.maturityDate)){
            states.notionalPrincipal = 0.0;
        }else if(model.<Double>getAs("NotionalPrincipal") >= 0.0){
            states.notionalPrincipal = model.<Double>getAs("CoverageOfCreditEnhancement")
                    * ContractRoleConvention.roleSign(model.getAs("ContractRole"))
                    * model.<Double>getAs("NotionalPrincipal");
        } else{
            List<ContractReference> contractStructure = model.getAs("ContractStructure");
            List<StateSpace> stateAtT0 = new ArrayList<>();
            if(GuaranteedExposure.NO.equals(model.getAs("GuaranteedExposure"))){
                stateAtT0 = contractStructure.stream().map(c -> c.getStateSpaceAtTimepoint(states.statusDate,null)).collect(Collectors.toList());
                states.notionalPrincipal = model.<Double>getAs("CoverageOfCreditEnhancement")
                        * ContractRoleConvention.roleSign(model.getAs("ContractRole"))
                        * stateAtT0.stream().map(s -> s.notionalPrincipal).reduce(0.0, Double::sum);
            }else if(GuaranteedExposure.NI.equals(model.getAs("GuaranteedExposure"))){

            }else{

            }
        }
        // return the initialized state space
        return states;
    }
}

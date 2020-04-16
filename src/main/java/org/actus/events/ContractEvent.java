/*
 * Copyright (C) 2016 - present by ACTUS Financial Research Foundation
 *
 * Please see distribution for license.
 */
package org.actus.events;

import org.actus.contracts.ContractType;
import org.actus.functions.StateTransitionFunction;
import org.actus.functions.PayOffFunction;
import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.states.StateSpace;
import org.actus.conventions.daycount.DayCountCalculator;
import org.actus.conventions.businessday.BusinessDayAdjuster;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.StringJoiner;

/**
 * Component that provides a data structure for a single event generated during the lifetime of a
 * {@link ContractType}
 * <p>
 * Contract events represent the atomic analytical elements comprising all events that possibly
 * occur during the lifetime of a {@link ContractType}. That is, contract events mark specific
 * times at which either a state transition, a payoff, or both is generated from a 
 * {@link ContractType}. 
 * <p>
 * Contract events are a generic representation of a specific {@link StateTransitionFunction} and
 * {@link PayOffFunction} with an event time according to which all events in a contract are ordered
 * (in an {@link EventSeries}) and processed sequentially. Thereby, processing an event in fact
 * means that its {@link StateTransitionFunction} and {@link PayOffFunction} are evaluated.
 * <p>
 */
public final class ContractEvent implements Comparable<ContractEvent> {
    protected long                  epochOffset;
    private StateTransitionFunction fStateTrans;
    private PayOffFunction          fPayOff;
    private LocalDateTime           eventTime;
    private LocalDateTime           scheduleTime;
    private String                  type;
    private String                  currency;
    private double                  payoff;
    private StateSpace              states;

  /**
   * Constructor
   * 
   * @param scheduleTime the plain schedule time of this particular event
   * @param eventTime the actual event time of this particular event
   * @param type the event type
   * @param currency the event currency
   * @param payOff the event pay-off function
   * @param stateTrans the event state-transition function
   * @return
   */
    public ContractEvent(LocalDateTime scheduleTime, LocalDateTime eventTime, String type, String currency, PayOffFunction payOff, StateTransitionFunction stateTrans) {
        this.epochOffset = eventTime.toEpochSecond(ZoneOffset.UTC) + EventSequence.timeOffset(type);
        this.eventTime = eventTime;
        this.scheduleTime = scheduleTime;
        this.type = type;
        this.currency = currency;
        this.fPayOff = payOff;
        this.fStateTrans = stateTrans;
        this.states = new StateSpace();
    }

    /**
     * Returns the time of this event (adjusted for the business-day-convention)
     */
    public LocalDateTime time() {
        return eventTime;    
    }
    
    /**
     * Returns the type of this event
     */
    public String type() {
        return type;    
    }
    
    /**
     * Change the type of this event
     * <p>
     * Note that this does also update the event's natural order
     * 
     * @param type the new event type
     */
    public void type(String type) {
        this.type = type;  
        this.epochOffset = eventTime.toEpochSecond(ZoneOffset.UTC) + EventSequence.timeOffset(type);
    }
    
    /**
     * Returns the currency of this event's payoff
     */
    public String currency() {
        return currency;    
    }
    
    /**
     * Returns this event's payoff
     */
    public double payoff() {
        return payoff;    
    }

    /**
     * Returns the post-event state-variables
     * <p>
     * Note that the length of the returned array may change going forward as new states
     * may be added with the addition of new {@link ContractType}s. Thus, it is recommended
     * to use the getter-methods for desired states (e.g. {@code time}, {@code type}, etc.) 
     * individually.
     */
    public StateSpace states() {
        return states;    
    }
       
    /**
     * Change the payoff function of this event
     * 
     * @param function the new payoff function
     */
    public void fPayOff(PayOffFunction function) {
        this.fPayOff = function;
    }
    
    /**
     * Change the state-transition function of this event
     * 
     * @param function the new state-transition function
     */
    public void fStateTrans(StateTransitionFunction function) {
        this.fStateTrans = function;
    }

    /**
     * Imposes the natural ordering of events in an instrument's payoff amongst each other
     * <p>
     * The natural ordering of contract events in an instrument's payoff is defined by a combination of
     * the event's time (cf. {@link time}) and type (cf. {@link type}). In fact, the sum of event-time
     * measured as epoch-seconds and an event-type specific time-offset according to {@link EventSequence}
     * give a unique, event-specific index providing the order of events. Hence, their natural ordering
     * imposes a time-consistent sequence of events (or a time-series).
     * <p>
     * The imposed natural ordering is consistent with equals.
     * <p>
     * Note that consistency of the natural ordering of events with equal is only guaranteed within the 
     * payoff (i.e. set of events) of a single instrument. If the events of multiple instruments are
     * combined in a collection, {@code o1.compareTo(o2)==0} of two events {@code o1} from the payoff of
     * an instrument and {@code o2} from the payoff of another instrument does not imply that the two 
     * events are the same, i.e. equal.
     * 
     * @param o an event to which to compare this event
     * @return -1 if this event is earlier than {@code o}, 0 if this event is equal than {@code o}, 1 if this event is after {@code o}
     * 
     * @see {@link EventSequence}
     */
    public int compareTo(ContractEvent o) {
        return (int) Math.signum(this.epochOffset - o.epochOffset);
    }
    
  /**
   * Evaluation of the event
   * <p>
   * Upon evaluation of an event, it's {@link PayOffFunction} and {@link StateTransitionFunction} get evaluated
   * in order to compute cash flow and update state variables.
   * 
   * @param states the current state of contract states
   * @param model the model containing parsed contract attributes
   * @param riskFactorModel an external market model
   * @param dayCounter the day counter to be used for calculating day count fractions
   * @param timeAdjuster the business day convention to be used for adjusting times in day count fraction calculations
   * @return
   */
    public void eval(StateSpace states, ContractModelProvider model, RiskFactorModelProvider riskFactorModel, DayCountCalculator dayCounter, BusinessDayAdjuster timeAdjuster) {
        this.payoff = fPayOff.eval(scheduleTime, states, model, riskFactorModel, dayCounter, timeAdjuster);
        this.states = fStateTrans.eval(scheduleTime, states, model, riskFactorModel, dayCounter, timeAdjuster);
    }

    /**
     * Returns a String-representation of all analytical elements
     * <p>
     * Note that the number of analytical elements may change going forward as e.g. new states
     * may be added with the addition of new {@link ContractType}s. Thus, it is recommended
     * to use the getter-methods for desired states (e.g. {@code time}, {@code type}, etc.)
     * individually and parse to a String manually.
     *
     * @return a single String containing all analytical elements
     */
    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(" ");

        joiner.add(Long.toString(epochOffset))
                .add(eventTime.toString())
                .add(scheduleTime.toString())
                .add(type)
                .add(currency)
                .add(Double.toString(payoff))
                .add(states.toString())
        ;

        return joiner.toString();
    }
}

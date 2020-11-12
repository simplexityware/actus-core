package org.actus.testutils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import org.actus.types.EventType;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ResultSet {
    LocalDateTime eventDate;
    EventType eventType;
    double payoff;
    String currency;
    double notionalPrincipal;
    double nominalInterestRate;
    double accruedInterest;
    double exerciseAmount;
    LocalDateTime exerciseDate;

    public LocalDateTime getExerciseDate() {return exerciseDate;}

    public void setExerciseDate(String exerciseDate) {this.exerciseDate = LocalDateTime.parse(exerciseDate); }

    public Double getExerciseAmount() {return exerciseAmount; }

    public void setExerciseAmount(Double exerciseAmount) {this.exerciseAmount = exerciseAmount;}

    public String getEventDate() {
        return eventDate.toString();
    }

    public void setEventDate(String eventDate) {
        this.eventDate = LocalDateTime.parse(eventDate);
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Double getPayoff() {
        return payoff;
    }

    public void setPayoff(Double payoff) {
        this.payoff = payoff;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getNotionalPrincipal() {
        return notionalPrincipal;
    }

    public void setNotionalPrincipal(Double notionalPrincipal) {
        this.notionalPrincipal = notionalPrincipal;
    }

    public Double getNominalInterestRate() {
        return nominalInterestRate;
    }

    public void setNominalInterestRate(Double nominalInterestRate) {
        this.nominalInterestRate = nominalInterestRate;
    }

    public Double getAccruedInterest() {
        return accruedInterest;
    }

    public void setAccruedInterest(Double accruedInterest) {
        this.accruedInterest = accruedInterest;
    }

    public void roundTo(int decimals) {
        // round payoff

        BigDecimal bd = new BigDecimal(Double.toString(this.payoff));
        this.payoff = bd.setScale(decimals, RoundingMode.FLOOR).doubleValue();

        // round notional principal
        bd = new BigDecimal(Double.toString(this.notionalPrincipal));
        this.notionalPrincipal = bd.setScale(decimals, RoundingMode.FLOOR).doubleValue();

        // round interest rate
        bd = new BigDecimal(Double.toString(this.nominalInterestRate));
        this.nominalInterestRate = bd.setScale(decimals, RoundingMode.FLOOR).doubleValue();

        // round accrued interest
        bd = new BigDecimal(Double.toString(this.accruedInterest));
        this.accruedInterest = bd.setScale(decimals, RoundingMode.FLOOR).doubleValue();
    }

    public String toString() {
        return "eventDate: " + eventDate + ", " +
                "eventType: " + eventType + ", " +
                "payoff: " + payoff + ", " +
                "currency: " + currency + "," +
                "notionalPrincipal: " + notionalPrincipal + ", " +
                "nominalInterestRate: " + nominalInterestRate + ", " +
                "accruedInterest: " + accruedInterest+ ", " +
                "exerciseDate" + exerciseDate + ", " +
                "exerciseAmount" + exerciseAmount;
    }

    // for assertEquals in JUnit testing
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResultSet resultsToCompare = (ResultSet) o;
        return eventDate.equals(resultsToCompare.eventDate) &&
                eventType.equals(resultsToCompare.eventType) &&
                payoff == resultsToCompare.payoff &&
                currency.equals(resultsToCompare.currency) &&
                notionalPrincipal == resultsToCompare.notionalPrincipal &&
                nominalInterestRate == resultsToCompare.nominalInterestRate &&
                accruedInterest == resultsToCompare.accruedInterest;
    }

}
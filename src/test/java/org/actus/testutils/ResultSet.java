package org.actus.testutils;

import java.time.LocalDateTime;

public class ResultSet {
    LocalDateTime eventDate;
    String eventType;
    Double payoff;
    Double notionalPrincipal;
    Double nominalInterestRate;
    Double accruedInterest;

    public String getEventDate() {
        return eventDate.toString();
    }

    public void setEventDate(String eventDate) {
        this.eventDate = LocalDateTime.parse(eventDate);
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Double getPayoff() {
        return payoff;
    }

    public void setPayoff(Double payoff) {
        this.payoff = payoff;
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

    public String toString() {
        return "Date: " + eventDate + ", " +
            "Type: " + eventType + ", " +
            "Payoff: " + payoff + ", " +
            "Notinoal: " + notionalPrincipal + ", " +
            "Rate: " + nominalInterestRate + ", " +
            "Accrued: " + accruedInterest;
    }
}
package com.bookmyticket.transport.flight;

public enum CabinClass {
    ECONOMY("Economy"),
    PREMIUM_ECONOMY("Premium Economy"),
    BUSINESS("Business"),
    FIRST("First Class");

    private final String label;

    CabinClass(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

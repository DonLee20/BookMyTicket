package com.bookmyticket.transport.train;

public enum TravelClass {
    AC_FIRST("AC First Class"),
    AC_2_TIER("AC 2 Tier"),
    AC_3_TIER("AC 3 Tier"),
    AC_3_TIER_ECONOMY("AC 3 Tier Economy"),
    SLEEPER("Sleeper Class"),
    SECOND_SITTING("Second Sitting"),
    CHAIR_CAR("Chair Car"),
    EXECUTIVE_CHAIR_CAR("Executive Chair Car");

    private final String label;

    TravelClass(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

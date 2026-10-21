package com.bookmyticket.booking;

public enum BookingCategory {
    BUS("Intercity bus"),
    PREMIUM_BUS("Premium bus"),
    TRAIN("Train (request)"),
    FLIGHT("Flight (demo hold)"),
    MTC("Chennai MTC"),
    HOLIDAY("Holiday package");

    private final String label;

    BookingCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

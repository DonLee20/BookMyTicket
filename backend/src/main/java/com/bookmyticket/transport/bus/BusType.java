package com.bookmyticket.transport.bus;

public enum BusType {
    NON_AC_SEATER("Non-AC seater"),
    AC_SEATER("AC seater"),
    SEMI_SLEEPER("Semi-sleeper"),
    SLEEPER("Sleeper"),
    AC_SLEEPER("AC sleeper"),
    VOLVO_AC_SEMI_SLEEPER("Premium AC semi-sleeper"),
    MULTI_AXLE_AC_SLEEPER("Multi-axle AC sleeper");

    private final String label;

    BusType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isPremium() {
        return this == VOLVO_AC_SEMI_SLEEPER || this == MULTI_AXLE_AC_SLEEPER || this == AC_SLEEPER;
    }
}

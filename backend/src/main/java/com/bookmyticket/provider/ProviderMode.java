package com.bookmyticket.provider;

/**
 * Operational mode of a provider integration. Every API response that carries
 * provider data includes the mode so that the UI can never confuse demo data
 * with live, bookable inventory.
 */
public enum ProviderMode {
    /** Data and booking operations come from a real authorized provider. */
    LIVE,
    /** Clearly labelled sample data. Never a real-world reservation. */
    DEMO,
    /** The user is redirected to a verified external booking channel. */
    EXTERNAL_HANDOFF,
    /** The capability is not currently supported. */
    UNAVAILABLE;

    public static ProviderMode from(String value, ProviderMode fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }
}

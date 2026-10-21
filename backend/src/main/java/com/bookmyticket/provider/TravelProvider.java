package com.bookmyticket.provider;

import java.util.List;

/** Common contract for transport data providers (bus, rail, flight, city transport). */
public interface TravelProvider {

    String id();

    String displayName();

    /** Category key: BUS, RAILWAY, FLIGHT, MTC, PAYMENT. */
    String category();

    ProviderMode mode();

    /** Whether this deployment can create real reservations through the provider. */
    boolean bookingSupported();

    default List<String> capabilities() {
        return List.of("SEARCH");
    }

    default String statusMessage() {
        return switch (mode()) {
            case LIVE -> "Connected to a live provider.";
            case DEMO -> "Running on clearly labelled sample data. No real-world reservation is created.";
            case EXTERNAL_HANDOFF -> "Booking is handed off to a verified external channel.";
            case UNAVAILABLE -> "Capability not currently available in this deployment.";
        };
    }

    default ProviderStatus toStatus() {
        return ProviderStatus.of(id(), displayName(), category(), mode(),
                java.util.Set.copyOf(capabilities()), bookingSupported(), false, statusMessage());
    }
}

package com.bookmyticket.provider;

/**
 * Chennai MTC boundary. Route planning and static service information are
 * supported with clearly labelled data; ticket issuance is only possible
 * through an authorized ticketing integration.
 */
public interface CityTransportProvider extends TravelProvider {

    record TicketingStatus(
            boolean supported,
            ProviderMode mode,
            String message,
            String officialUrl
    ) {
    }

    TicketingStatus ticketingStatus();

    String officialInfoUrl();

    /** SAMPLE, VERIFIED_STATIC or LIVE — the freshness of route data. */
    String dataFreshness();
}

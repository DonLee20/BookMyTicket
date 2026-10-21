package com.bookmyticket.provider;

import com.bookmyticket.transport.flight.dto.FlightOffer;
import com.bookmyticket.transport.flight.dto.FlightSearchQuery;

import java.util.List;
import java.util.Optional;

/**
 * Flight distribution provider boundary. A live adapter (e.g. an authorized
 * global distribution API) would implement offer search, fare revalidation,
 * reservation creation and ticketing here. The demo adapter returns clearly
 * labelled sample offers and never issues a ticket.
 */
public interface FlightProvider extends TravelProvider {

    List<FlightOffer> search(FlightSearchQuery query);

    Optional<FlightOffer> revalidate(String offerRef);

    /** Verified external booking channel, when one is configured. */
    Optional<String> externalHandoffUrl();
}

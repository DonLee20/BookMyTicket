package com.bookmyticket.transport.flight.dto;

import com.bookmyticket.transport.flight.CabinClass;

import java.time.LocalDate;

public record FlightSearchQuery(
        String originIata,
        String destinationIata,
        LocalDate departureDate,
        LocalDate returnDate,
        CabinClass cabin,
        int adults,
        int children,
        int infants,
        boolean nonStopOnly,
        String sort
) {
    public int totalTravellers() {
        return adults + children + infants;
    }
}

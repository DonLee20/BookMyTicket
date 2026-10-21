package com.bookmyticket.transport.flight.dto;

import com.bookmyticket.transport.flight.CabinClass;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public final class FlightDtos {

    private FlightDtos() {
    }

    public record AirportOption(String iata, String name, String city, String country, String region) {
    }

    public record SearchLeg(
            @NotBlank String origin,
            @NotBlank String destination,
            @NotNull LocalDate date
    ) {
    }

    public record SearchRequest(
            @NotEmpty @Valid List<SearchLeg> legs,
            @NotNull CabinClass cabin,
            @Min(1) @Max(9) int adults,
            @Min(0) @Max(8) int children,
            @Min(0) @Max(4) int infants,
            Boolean nonStopOnly,
            String sort
    ) {
    }

    public record LegResult(
            int index,
            String originIata,
            String destinationIata,
            String originCity,
            String destinationCity,
            LocalDate date,
            List<FlightOffer> offers
    ) {
    }

    public record SearchResponse(
            List<LegResult> legs,
            String providerMode,
            String providerMessage,
            String sourceTimestamp,
            boolean ticketIssuanceAvailable,
            String externalHandoffUrl,
            List<CabinOption> cabins
    ) {
    }

    public record CabinOption(String code, String label) {
    }

    public record RevalidateRequest(@NotBlank String offerRef) {
    }
}

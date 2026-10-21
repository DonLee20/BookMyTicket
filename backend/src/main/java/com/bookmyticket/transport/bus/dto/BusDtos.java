package com.bookmyticket.transport.bus.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public final class BusDtos {

    private BusDtos() {
    }

    public record SearchRequest(
            @NotBlank String from,
            @NotBlank String to,
            @NotNull LocalDate date,
            @Min(1) @Max(6) int passengers,
            Boolean premiumOnly,
            Long maxFareMinor,
            List<String> busTypes,
            List<String> amenities,
            String operator,
            String sort,
            Boolean sleeperOnly,
            Boolean seaterOnly
    ) {
    }

    public record TripResult(
            Long id,
            String operatorName,
            String operatorCode,
            boolean premium,
            String busType,
            String busTypeLabel,
            String departureCity,
            String arrivalCity,
            String departTime,
            String arriveTime,
            int arriveDayOffset,
            String arriveDayOffsetLabel,
            String journeyDate,
            int durationMinutes,
            String durationLabel,
            long fareMinor,
            String fareDisplay,
            String currency,
            int availableSeats,
            int totalSeats,
            String availabilityStatus,
            List<Amenity> amenities,
            List<String> boardingPoints,
            List<String> droppingPoints,
            String cancellationPolicy,
            String providerMode,
            String source,
            boolean seatSelectionSupported
    ) {
    }

    public record Amenity(String code, String label) {
    }

    public record SeatView(
            Long id,
            String label,
            String type,
            String deck,
            int rowNo,
            int colNo,
            String status,
            long priceMinor,
            String priceDisplay,
            boolean window
    ) {
    }

    public record SeatRow(int rowNo, List<SeatView> seats) {
    }

    public record DeckView(String deck, String deckLabel, List<SeatRow> rows) {
    }

    public record SeatMap(
            Long tripId,
            String layout,
            String layoutLabel,
            long pricePerSeatMinor,
            String pricePerSeatDisplay,
            String currency,
            int availableCount,
            int totalSeats,
            List<DeckView> decks,
            String providerMode
    ) {
    }

    /** Why a search returned no bookable rows. Route coverage is not the same as an empty date. */
    public enum SearchStatus {
        OK,
        ORIGIN_NOT_COVERED,
        DESTINATION_NOT_COVERED,
        ROUTE_NOT_COVERED,
        OUTSIDE_DEMO_WINDOW,
        NO_TRIPS_ON_DATE,
        FILTERED_OUT
    }

    public record RouteSuggestion(String from, String to) {
    }

    public record SearchDiagnostics(
            SearchStatus status,
            boolean originCovered,
            boolean destinationCovered,
            boolean routeCovered,
            boolean dateInInventoryWindow,
            LocalDate inventoryFrom,
            LocalDate inventoryTo,
            int tripsOnDate,
            int tripsAfterFilters,
            String message,
            List<RouteSuggestion> suggestions
    ) {
    }

    public record SearchResponse(
            List<TripResult> trips,
            String providerMode,
            String providerMessage,
            String sourceTimestamp,
            boolean liveDataConnected,
            boolean liveProviderConfigured,
            SearchDiagnostics diagnostics
    ) {
    }

    public record PremiumHighlights(
            List<TripResult> trips,
            List<String> comfortCategories,
            String providerMode,
            String providerMessage,
            boolean liveDataConnected,
            boolean liveProviderConfigured
    ) {
    }

    public record SeatMapResponse(TripResult trip, SeatMap seatMap) {
    }
}

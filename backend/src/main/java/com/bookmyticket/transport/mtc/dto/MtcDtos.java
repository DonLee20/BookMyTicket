package com.bookmyticket.transport.mtc.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public final class MtcDtos {

    private MtcDtos() {
    }

    public record StopOption(Long id, String name, String locality, String zone) {
    }

    public record PlanRequest(@NotNull Long originStopId, @NotNull Long destinationStopId) {
    }

    public record RouteStopView(
            Long stopId,
            String name,
            int sequence,
            int stageNumber,
            long fareFromOriginMinor,
            String fareFromOriginDisplay,
            String distanceKm,
            boolean origin,
            boolean destination
    ) {
    }

    public record RoutePlan(
            Long routeId,
            String routeNumber,
            String direction,
            String directionLabel,
            String originName,
            String destinationName,
            String firstService,
            String lastService,
            Integer frequencyMinutes,
            String serviceDays,
            boolean accessible,
            long segmentFareMinor,
            String segmentFareDisplay,
            int travelStages,
            List<RouteStopView> stops,
            String source,
            String dataNote
    ) {
    }

    public record Ticketing(
            boolean supported,
            String mode,
            String message,
            String officialUrl
    ) {
    }

    public record PlanResponse(
            List<RoutePlan> routes,
            Ticketing ticketing,
            String providerMode,
            String dataFreshness,
            String message,
            String sourceTimestamp,
            String transferSuggestion
    ) {
    }
}

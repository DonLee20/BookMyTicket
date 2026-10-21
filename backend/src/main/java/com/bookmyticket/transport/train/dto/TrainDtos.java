package com.bookmyticket.transport.train.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public final class TrainDtos {

    private TrainDtos() {
    }

    public record StationOption(String code, String name, String city, String state) {
    }

    public record SearchRequest(
            @NotBlank String from,
            @NotBlank String to,
            @NotNull LocalDate date,
            @Min(1) @Max(6) int passengers,
            String travelClass,
            String quota
    ) {
    }

    public record ClassAvailability(
            String code,
            String label,
            Long fareMinor,
            String fareDisplay,
            String availabilityStatus,
            Integer availableSeats,
            Integer wlPosition,
            String note
    ) {
    }

    public record RouteStop(
            String stationCode,
            String stationName,
            String arriveTime,
            String departTime,
            int dayNumber,
            Integer distanceKm
    ) {
    }

    public record TrainResult(
            Long id,
            String trainNumber,
            String name,
            String fromCode,
            String fromName,
            String toCode,
            String toName,
            String departTime,
            String arriveTime,
            int arriveDayOffset,
            String arriveDayOffsetLabel,
            int durationMinutes,
            String durationLabel,
            String runningDays,
            boolean runsOnDate,
            Integer distanceKm,
            List<ClassAvailability> classes,
            List<RouteStop> routeStops
    ) {
    }

    public record SearchResponse(
            List<TrainResult> trains,
            String providerMode,
            String providerMessage,
            String sourceTimestamp,
            String officialHandoffUrl,
            String dataNote,
            List<QuotaOption> quotas
    ) {
    }

    public record QuotaOption(String code, String label) {
    }

    public record AvailabilityNote(String message, boolean live) {
    }
}

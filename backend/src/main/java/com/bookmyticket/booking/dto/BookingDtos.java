package com.bookmyticket.booking.dto;

import com.bookmyticket.booking.Gender;
import com.bookmyticket.booking.PassengerType;
import com.bookmyticket.payment.PaymentDtos;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public final class BookingDtos {

    private BookingDtos() {
    }

    public record PassengerInput(
            @NotBlank @Size(max = 120) String fullName,
            @Min(value = 0, message = "Age cannot be negative.") @Max(value = 120, message = "Enter a valid age.") int age,
            @NotNull Gender gender,
            PassengerType passengerType
    ) {
    }

    public record BusBookingRequest(
            @NotNull Long tripId,
            @NotEmpty @Size(max = 6) List<Long> seatIds,
            @NotBlank @Size(max = 150) String boardingPoint,
            @NotBlank @Size(max = 150) String droppingPoint,
            @NotBlank @Size(max = 120) String contactName,
            @NotBlank @Email @Size(max = 190) String contactEmail,
            @Pattern(regexp = "^$|^[0-9+\\- ]{6,20}$", message = "Enter a valid phone number.") String contactPhone,
            @Valid @NotEmpty @Size(max = 6) List<PassengerInput> passengers,
            Long expectedTotalMinor,
            @Size(max = 80) String idempotencyKey
    ) {
    }

    public record TrainBookingRequest(
            @NotNull Long serviceId,
            @NotBlank @Size(max = 30) String travelClass,
            @Size(max = 20) String quota,
            @NotNull LocalDate journeyDate,
            @NotBlank @Size(max = 120) String fromStation,
            @NotBlank @Size(max = 120) String toStation,
            @NotBlank @Size(max = 120) String contactName,
            @NotBlank @Email @Size(max = 190) String contactEmail,
            @Pattern(regexp = "^$|^[0-9+\\- ]{6,20}$", message = "Enter a valid phone number.") String contactPhone,
            @Valid @NotEmpty @Size(max = 6) List<PassengerInput> passengers,
            @Size(max = 80) String idempotencyKey
    ) {
    }

    public record FlightBookingRequest(
            @NotBlank @Size(max = 80) String offerRef,
            boolean acceptPriceChange,
            @NotBlank @Size(max = 120) String contactName,
            @NotBlank @Email @Size(max = 190) String contactEmail,
            @Pattern(regexp = "^$|^[0-9+\\- ]{6,20}$", message = "Enter a valid phone number.") String contactPhone,
            @Valid @NotEmpty @Size(max = 9) List<PassengerInput> passengers,
            @Size(max = 80) String idempotencyKey
    ) {
    }

    public record HolidayBookingRequest(
            @NotBlank @Size(max = 120) String packageSlug,
            @NotNull LocalDate startDate,
            @Min(1) @Max(20) int travellers,
            @Min(1) @Max(10) int rooms,
            @NotBlank @Size(max = 120) String contactName,
            @NotBlank @Email @Size(max = 190) String contactEmail,
            @Pattern(regexp = "^$|^[0-9+\\- ]{6,20}$", message = "Enter a valid phone number.") String contactPhone,
            @Size(max = 500) String notes,
            @Size(max = 80) String idempotencyKey
    ) {
    }

    public record BusDetails(
            Long tripId,
            String operatorName,
            String busType,
            boolean premium,
            String travelDate,
            String departureCity,
            String arrivalCity,
            String departAt,
            String arriveAt,
            String boardingPoint,
            String droppingPoint,
            List<String> seatNumbers
    ) {
    }

    public record TrainDetails(
            Long serviceId,
            String trainNumber,
            String trainName,
            String travelClass,
            String quota,
            String journeyDate,
            String fromStation,
            String toStation,
            String officialHandoffUrl
    ) {
    }

    public record FlightDetails(
            Long scheduleId,
            String offerRef,
            String airlineName,
            String flightNumber,
            String cabin,
            String journeyDate,
            String originIata,
            String destinationIata,
            String departLocal,
            String arriveLocal,
            int stops,
            String baggageNote,
            boolean refundable,
            boolean changeable,
            String providerConditions
    ) {
    }

    public record HolidayDetails(
            Long packageId,
            String packageSlug,
            String packageTitle,
            String destinationName,
            String imageUrl,
            String startDate,
            String endDate,
            int travellers,
            int rooms,
            boolean landOnly
    ) {
    }

    public record BookingDetails(BusDetails bus, TrainDetails train, FlightDetails flight, HolidayDetails holiday) {
    }

    public record PassengerView(
            int seq,
            String fullName,
            int age,
            String gender,
            String passengerType,
            String seatNumber
    ) {
    }

    public record CancelRequest(@Size(max = 300) String reason) {
    }

    public record BookingResponse(
            String id,
            String category,
            String categoryLabel,
            String status,
            String statusLabel,
            String paymentStatus,
            String currency,
            long totalAmountMinor,
            String totalDisplay,
            String providerMode,
            String providerReference,
            String contactName,
            String contactEmail,
            String contactPhone,
            int travellerCount,
            String notes,
            String createdAt,
            String updatedAt,
            String holdExpiresAt,
            Long holdSecondsRemaining,
            String cancelledAt,
            String cancellationReason,
            Long refundAmountMinor,
            String refundDisplay,
            PaymentDtos.PaymentSummary payment,
            BookingDetails details,
            List<PassengerView> passengers,
            String nextAction,
            String message,
            String officialHandoffUrl
    ) {
    }
}

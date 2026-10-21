package com.bookmyticket.admin;

import com.bookmyticket.provider.ProviderRegistry;

import java.util.List;
import java.util.Map;

public final class AdminDtos {

    private AdminDtos() {
    }

    public record OverviewResponse(
            long totalUsers,
            long totalBookings,
            long confirmedBookings,
            long cancelledBookings,
            long pendingPayments,
            long revenueMinor,
            String revenueDisplay,
            long newMessages,
            long totalPackages,
            long activePackages,
            long busTripsToday,
            long busSeatsAvailableToday,
            Map<String, Long> bookingsByStatus,
            Map<String, Long> bookingsByCategory,
            String paymentProviderMode,
            String generatedAt
    ) {
    }

    public record UserRow(
            Long id,
            String name,
            String email,
            String phone,
            boolean enabled,
            List<String> roles,
            String createdAt
    ) {
    }

    public record PaymentRow(
            String id,
            String bookingId,
            String userEmail,
            String provider,
            String mode,
            String status,
            long amountMinor,
            String amountDisplay,
            String currency,
            String providerOrderId,
            String providerPaymentId,
            String createdAt,
            String failureReason
    ) {
    }

    public record PackageRow(
            Long id,
            String slug,
            String title,
            String destinationName,
            String category,
            String categoryLabel,
            int durationDays,
            long basePriceMinor,
            String priceDisplay,
            String currency,
            boolean active,
            boolean featured,
            String imageUrl
    ) {
    }

    public record PackageUpdateRequest(Boolean active, Boolean featured) {
    }

    public record BusInventoryRow(
            Long tripId,
            String operatorName,
            String busType,
            boolean premium,
            String direction,
            String departureCity,
            String arrivalCity,
            String travelDate,
            String departTime,
            String arriveTime,
            long fareMinor,
            String fareDisplay,
            int totalSeats,
            int availableSeats,
            String status,
            String source
    ) {
    }

    public record MessageRow(
            Long id,
            String name,
            String email,
            String subject,
            String message,
            String status,
            String createdAt,
            String handledAt
    ) {
    }

    public record MaintenanceSummary(
            long totalUsers,
            long totalBookings,
            long totalPayments,
            long totalProviderCalls,
            long totalMessages
    ) {
    }

    public record HealthResponse(
            String status,
            String database,
            String schemaVersion,
            String applicationVersion,
            long uptimeSeconds,
            String paymentProviderMode,
            List<ProviderRegistry.ProviderInfo> providers,
            MaintenanceSummary maintenance,
            String serverTime
    ) {
    }
}

package com.bookmyticket.payment;

public final class PaymentDtos {

    private PaymentDtos() {
    }

    public record PaymentSummary(
            String id,
            String provider,
            String mode,
            String status,
            long amountMinor,
            String amountDisplay,
            String currency,
            String providerOrderId,
            String providerPaymentId,
            String nextAction,
            String message
    ) {
    }

    public record DemoCompleteRequest(String outcome) {
    }

    public record CompletionResult(
            boolean success,
            boolean duplicate,
            String bookingId,
            String bookingStatus,
            String paymentStatus,
            String message
    ) {
    }

    public record WebhookPayload(
            String eventId,
            String eventType,
            String providerOrderId,
            String providerPaymentId,
            Long amountMinor,
            String currency
    ) {
    }

    public record WebhookResult(boolean accepted, boolean duplicate, String message) {
    }

    public record PaymentProviderStatus(String id, String displayName, String mode, String message) {
    }
}

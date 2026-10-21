package com.bookmyticket.payment;

import com.bookmyticket.provider.ProviderMode;

/**
 * Payment provider abstraction. Only the demo adapter ships with this release;
 * a configured live provider without an adapter is reported as UNAVAILABLE and
 * no charge is ever attempted.
 */
public interface PaymentProvider {

    String id();

    String displayName();

    ProviderMode mode();

    default boolean supported() {
        return mode() == ProviderMode.DEMO;
    }

    ProviderIntent createIntent(String bookingId, long amountMinor, String currency);

    RefundResult refund(String providerPaymentId, long amountMinor, String currency, String reason);

    record ProviderIntent(String providerOrderId, String checkoutHint, String message) {
    }

    record RefundResult(String providerRefundId, long amountMinor, boolean simulated, String message) {
    }
}

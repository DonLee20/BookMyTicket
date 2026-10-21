package com.bookmyticket.payment;

import com.bookmyticket.config.AppProperties;
import com.bookmyticket.provider.ProviderMode;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Simulated payment adapter. It never contacts a payment network, never moves
 * money and never issues a real receipt. Completion is explicit and clearly
 * labelled in the UI.
 */
@Component
public class DemoPaymentProvider implements PaymentProvider {

    private final AppProperties properties;

    public DemoPaymentProvider(AppProperties properties) {
        this.properties = properties;
    }

    @Override
    public String id() {
        return "demo-payment-simulator";
    }

    @Override
    public String displayName() {
        return "Demo payment simulator (no real money)";
    }

    @Override
    public ProviderMode mode() {
        String configured = properties.getPayment().getProvider();
        if (configured == null || configured.isBlank() || "demo".equalsIgnoreCase(configured)) {
            return ProviderMode.DEMO;
        }
        // Razorpay/Stripe/etc. credentials may be present, but no live adapter
        // is implemented in this release. Fail closed instead of faking charges.
        return ProviderMode.UNAVAILABLE;
    }

    @Override
    public ProviderIntent createIntent(String bookingId, long amountMinor, String currency) {
        String orderId = "demo_order_" + UUID.randomUUID().toString().replace("-", "");
        return new ProviderIntent(orderId, "DEMO_COMPLETE",
                "Simulated payment intent for " + currency + " " + (amountMinor / 100.0)
                        + ". No money will be charged. Complete it from the demo checkout panel.");
    }

    @Override
    public RefundResult refund(String providerPaymentId, long amountMinor, String currency, String reason) {
        String refundId = "demo_refund_" + UUID.randomUUID().toString().replace("-", "");
        return new RefundResult(refundId, amountMinor, true,
                "Simulated refund recorded for " + currency + " " + (amountMinor / 100.0) + ". " + reason);
    }

    @Override
    public String toString() {
        return "DemoPaymentProvider{mode=" + mode() + "}";
    }
}

package com.bookmyticket.payment;

import com.bookmyticket.booking.Booking;
import com.bookmyticket.booking.BookingLifecycleService;
import com.bookmyticket.booking.PaymentStatus;
import com.bookmyticket.common.ApiException;
import com.bookmyticket.common.MoneyUtils;
import com.bookmyticket.config.AppProperties;
import com.bookmyticket.provider.ProviderCallRecorder;
import com.bookmyticket.provider.ProviderMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private static final List<PaymentStatus> REUSABLE = List.of(PaymentStatus.PENDING, PaymentStatus.PAID);

    private final PaymentRepository paymentRepository;
    private final PaymentEventRepository eventRepository;
    private final PaymentProvider paymentProvider;
    private final BookingLifecycleService lifecycleService;
    private final ProviderCallRecorder callRecorder;
    private final AppProperties properties;
    private final ObjectMapper objectMapper;

    public PaymentService(PaymentRepository paymentRepository, PaymentEventRepository eventRepository,
                          PaymentProvider paymentProvider, BookingLifecycleService lifecycleService,
                          ProviderCallRecorder callRecorder, AppProperties properties, ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.eventRepository = eventRepository;
        this.paymentProvider = paymentProvider;
        this.lifecycleService = lifecycleService;
        this.callRecorder = callRecorder;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /** Creates (or reuses) the single demo payment intent for a booking. */
    @Transactional
    public Payment createIntent(Booking booking) {
        if (paymentProvider.mode() != ProviderMode.DEMO) {
            throw ApiException.unavailable("PAYMENT_PROVIDER_UNAVAILABLE",
                    "No live payment adapter is available in this deployment. Configure PAYMENT_PROVIDER=demo.");
        }
        List<Payment> existing = paymentRepository.findByBookingIdOrderByCreatedAtDesc(booking.getId());
        for (Payment payment : existing) {
            if (REUSABLE.contains(payment.getStatus())) {
                return payment;
            }
        }
        Payment payment = new Payment();
        payment.setId("PAY-" + UUID.randomUUID().toString().replace("-", ""));
        payment.setBooking(booking);
        payment.setProvider(paymentProvider.id());
        payment.setMode(paymentProvider.mode());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmountMinor(booking.getTotalAmountMinor());
        payment.setCurrency(booking.getCurrency());
        PaymentProvider.ProviderIntent intent = callRecorder.timed(paymentProvider.id(), "PAYMENT", "CREATE_INTENT",
                booking.getId(), () -> paymentProvider.createIntent(booking.getId(), booking.getTotalAmountMinor(),
                        booking.getCurrency()));
        payment.setProviderOrderId(intent.providerOrderId());
        payment.setIdempotencyKey("pay:" + booking.getId());
        return paymentRepository.save(payment);
    }

    /**
     * Explicit demo checkout. The caller must own the booking. A second call is
     * idempotent and never charges twice (there is no real charge at all).
     */
    @Transactional
    public PaymentDtos.CompletionResult completeDemo(String paymentId, Long userId, String outcome) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> ApiException.notFound("PAYMENT_NOT_FOUND", "That payment could not be found."));
        Booking booking = payment.getBooking();
        if (!booking.getUser().getId().equals(userId)) {
            throw ApiException.forbidden("You do not have access to this payment.");
        }
        if (payment.getMode() != ProviderMode.DEMO) {
            throw ApiException.conflict("DEMO_PAYMENT_DISABLED",
                    "Demo payment completion is disabled for the configured payment provider.");
        }
        if (payment.getStatus() == PaymentStatus.PAID) {
            return new PaymentDtos.CompletionResult(true, true, booking.getId(), booking.getStatus().name(),
                    payment.getStatus().name(), "This demo payment was already completed.");
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw ApiException.conflict("PAYMENT_NOT_PENDING",
                    "This payment is " + payment.getStatus().name().toLowerCase() + " and cannot be completed.");
        }
        if (booking.getHoldExpiresAt() != null && booking.getHoldExpiresAt().isBefore(Instant.now())
                && booking.getStatus() == com.bookmyticket.booking.BookingStatus.PAYMENT_PENDING) {
            lifecycleService.expire(booking, "Payment hold expired before checkout was completed.");
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment hold expired.");
            paymentRepository.save(payment);
            throw ApiException.conflict("PAYMENT_HOLD_EXPIRED",
                    "The seat hold expired. Please search again and create a new booking.");
        }

        boolean success = outcome == null || outcome.isBlank() || !"FAILURE".equalsIgnoreCase(outcome.trim());
        if (!success) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Simulated payment failure requested by the customer.");
            paymentRepository.save(payment);
            recordEvent(payment, "PAYMENT_FAILED", "demo-complete-failure", "{\"outcome\":\"FAILURE\"}");
            lifecycleService.onPaymentFailed(booking, "Demo payment failed. Inventory was released.");
            return new PaymentDtos.CompletionResult(false, false, booking.getId(), booking.getStatus().name(),
                    payment.getStatus().name(), "Demo payment marked as failed. No money moved.");
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setProviderPaymentId("demopay_" + UUID.randomUUID().toString().replace("-", ""));
        paymentRepository.save(payment);
        recordEvent(payment, "PAYMENT_SUCCESS", "demo-complete-success", "{\"outcome\":\"SUCCESS\"}");
        lifecycleService.onPaymentSucceeded(booking);
        return new PaymentDtos.CompletionResult(true, false, booking.getId(), booking.getStatus().name(),
                payment.getStatus().name(),
                "Demo payment complete. This is a simulated transaction and not a real payment receipt.");
    }

    /**
     * HMAC-SHA256 signed demo webhook. Event ids are unique per payment, so
     * delivery retries are safe and never confirm a booking twice.
     */
    @Transactional
    public PaymentDtos.WebhookResult handleWebhook(String rawBody, String signature) {
        verifySignature(rawBody, signature);
        PaymentDtos.WebhookPayload payload;
        try {
            payload = objectMapper.readValue(rawBody, PaymentDtos.WebhookPayload.class);
        } catch (Exception ex) {
            throw ApiException.badRequest("INVALID_WEBHOOK", "The webhook payload could not be parsed.");
        }
        if (payload.eventId() == null || payload.eventId().isBlank()
                || payload.providerOrderId() == null || payload.providerOrderId().isBlank()
                || payload.eventType() == null || payload.eventType().isBlank()) {
            throw ApiException.badRequest("INVALID_WEBHOOK",
                    "eventId, eventType and providerOrderId are required.");
        }
        Payment payment = paymentRepository.findByProviderOrderId(payload.providerOrderId())
                .orElseThrow(() -> ApiException.notFound("PAYMENT_NOT_FOUND",
                        "No payment matches the supplied provider order id."));
        if (eventRepository.existsByPaymentIdAndEventRef(payment.getId(), payload.eventId())) {
            return new PaymentDtos.WebhookResult(true, true, "This event was already processed.");
        }
        if (payload.amountMinor() != null && payload.amountMinor() != payment.getAmountMinor()) {
            throw ApiException.badRequest("AMOUNT_MISMATCH",
                    "The webhook amount does not match the payment amount.");
        }
        if (!"PAYMENT_SUCCESS".equals(payload.eventType()) && !"PAYMENT_FAILED".equals(payload.eventType())) {
            throw ApiException.badRequest("UNKNOWN_EVENT", "Unsupported webhook event type: " + payload.eventType());
        }

        recordEvent(payment, payload.eventType(), payload.eventId(), rawBody);

        if ("PAYMENT_SUCCESS".equals(payload.eventType())) {
            if (payment.getStatus() == PaymentStatus.PAID) {
                return new PaymentDtos.WebhookResult(true, true, "Payment was already marked as paid.");
            }
            if (payment.getStatus() != PaymentStatus.PENDING) {
                throw ApiException.conflict("PAYMENT_NOT_PENDING",
                        "This payment cannot be completed from its current state.");
            }
            payment.setStatus(PaymentStatus.PAID);
            payment.setProviderPaymentId(payload.providerPaymentId() == null || payload.providerPaymentId().isBlank()
                    ? "demo_webhook_" + UUID.randomUUID().toString().replace("-", "")
                    : payload.providerPaymentId());
            paymentRepository.save(payment);
            lifecycleService.onPaymentSucceeded(payment.getBooking());
            return new PaymentDtos.WebhookResult(true, false, "Demo payment confirmed from signed webhook.");
        }

        if (payment.getStatus() == PaymentStatus.FAILED) {
            return new PaymentDtos.WebhookResult(true, true, "Payment failure was already processed.");
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw ApiException.conflict("PAYMENT_NOT_PENDING",
                    "This payment cannot be failed from its current state.");
        }
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason("Failure reported by signed demo webhook.");
        paymentRepository.save(payment);
        lifecycleService.onPaymentFailed(payment.getBooking(), "Payment failed. Inventory was released.");
        return new PaymentDtos.WebhookResult(true, false, "Demo payment failure recorded.");
    }

    /**
     * Simulated refund for a paid booking. A zero refund leaves the payment as
     * PAID so the record clearly shows nothing was returned.
     */
    @Transactional
    public Payment refundIfPaid(Booking booking, long refundAmountMinor, String reason) {
        if (refundAmountMinor <= 0) {
            return null;
        }
        Payment payment = paymentRepository.findByBookingIdAndStatus(booking.getId(), PaymentStatus.PAID)
                .orElse(null);
        if (payment == null) {
            return null;
        }
        PaymentProvider.RefundResult result = callRecorder.timed(paymentProvider.id(), "PAYMENT", "REFUND",
                payment.getId(), () -> paymentProvider.refund(payment.getProviderPaymentId(), refundAmountMinor,
                        payment.getCurrency(), reason));
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        recordEvent(payment, "REFUND", "refund", "{\"refundId\":\"" + result.providerRefundId()
                + "\",\"amountMinor\":" + refundAmountMinor + "}");
        return payment;
    }

    public PaymentDtos.PaymentSummary summarize(Payment payment) {
        if (payment == null) {
            return null;
        }
        boolean demo = payment.getMode() == ProviderMode.DEMO;
        String nextAction = demo && payment.getStatus() == PaymentStatus.PENDING ? "DEMO_COMPLETE" : "NONE";
        String message = switch (payment.getStatus()) {
            case PENDING -> demo
                    ? "Simulated payment pending. No money will be charged."
                    : "Payment pending with the configured provider.";
            case PAID -> demo
                    ? "Simulated payment marked successful. This is not a real payment receipt."
                    : "Payment captured.";
            case FAILED -> "Payment failed. " + orEmpty(payment.getFailureReason());
            case REFUND_PENDING -> "Refund is being processed.";
            case REFUNDED -> "Refund recorded for this demo payment.";
            case NOT_REQUIRED -> "No payment is required for this request.";
        };
        return new PaymentDtos.PaymentSummary(payment.getId(), payment.getProvider(), payment.getMode().name(),
                payment.getStatus().name(), payment.getAmountMinor(),
                MoneyUtils.format(payment.getAmountMinor(), payment.getCurrency()), payment.getCurrency(),
                payment.getProviderOrderId(), payment.getProviderPaymentId(), nextAction, message);
    }

    /** Marks any pending payments as failed when a booking hold expires or is abandoned. */
    @Transactional
    public void failPendingForBooking(Booking booking, String reason) {
        paymentRepository.findByBookingIdOrderByCreatedAtDesc(booking.getId()).stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.PENDING)
                .forEach(payment -> {
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setFailureReason(reason);
                    paymentRepository.save(payment);
                });
    }

    public PaymentDtos.PaymentProviderStatus status() {        String message = switch (paymentProvider.mode()) {
            case DEMO -> "Simulated payments only. No card, UPI or bank details are collected and no money moves.";
            case UNAVAILABLE -> "The configured payment provider has no adapter in this release. Payments are disabled.";
            case LIVE -> "Live payments are handled by the configured adapter.";
            case EXTERNAL_HANDOFF -> "Payment is completed on the external provider channel.";
        };
        return new PaymentDtos.PaymentProviderStatus(paymentProvider.id(), paymentProvider.displayName(),
                paymentProvider.mode().name(), message);
    }

    public ProviderMode mode() {
        return paymentProvider.mode();
    }

    private void recordEvent(Payment payment, String type, String eventRef, String payload) {
        if (eventRepository.existsByPaymentIdAndEventRef(payment.getId(), eventRef)) {
            return;
        }
        PaymentEvent event = new PaymentEvent();
        event.setPayment(payment);
        event.setEventType(type);
        event.setEventRef(eventRef);
        event.setPayloadJson(payload != null && payload.length() > 60000 ? payload.substring(0, 60000) : payload);
        eventRepository.save(event);
    }

    private void verifySignature(String rawBody, String signature) {
        String secret = properties.getPayment().getWebhookSecret();
        if (secret == null || secret.isBlank()) {
            throw ApiException.unavailable("WEBHOOK_NOT_CONFIGURED",
                    "Webhook processing is not configured for this deployment.");
        }
        if (signature == null || signature.isBlank()) {
            throw ApiException.forbidden("INVALID_SIGNATURE", "The webhook signature is missing.");
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String expected = HexFormat.of().formatHex(digest);
            if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                    signature.trim().toLowerCase().getBytes(StandardCharsets.US_ASCII))) {
                throw ApiException.forbidden("INVALID_SIGNATURE", "The webhook signature is invalid.");
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ApiException.forbidden("INVALID_SIGNATURE", "The webhook signature could not be validated.");
        }
    }

    private String orEmpty(String value) {
        return value == null ? "" : value;
    }
}

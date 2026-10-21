package com.bookmyticket.payment;

import com.bookmyticket.security.SecurityUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Explicit demo checkout. This endpoint does not exist for live providers
     * and never charges money; it only advances the simulated payment state.
     */
    @PostMapping("/{paymentId}/demo-complete")
    public PaymentDtos.CompletionResult complete(@PathVariable String paymentId,
                                                 @RequestBody(required = false) PaymentDtos.DemoCompleteRequest request) {
        return paymentService.completeDemo(paymentId, SecurityUtils.requireUserId(),
                request == null ? null : request.outcome());
    }

    /**
     * Signed provider webhook (HMAC-SHA256 over the raw body in the
     * X-Payment-Signature header). Event ids make redelivery idempotent.
     */
    @PostMapping(value = "/webhook/demo", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PaymentDtos.WebhookResult webhook(@RequestBody String rawBody,
                                             @RequestHeader(value = "X-Payment-Signature", required = false)
                                             String signature) {
        return paymentService.handleWebhook(rawBody, signature);
    }
}

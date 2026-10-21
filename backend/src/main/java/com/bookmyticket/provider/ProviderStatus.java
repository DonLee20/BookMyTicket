package com.bookmyticket.provider;

import java.util.List;
import java.util.Set;

/**
 * Status of one provider integration, safe for admin display (no secrets).
 */
public record ProviderStatus(
        String id,
        String displayName,
        String category,
        ProviderMode mode,
        List<String> capabilities,
        boolean bookingSupported,
        boolean paymentsSupported,
        String message
) {

    public record Capability(String key, String label, boolean enabled, String note) {
    }

    public static ProviderStatus of(String id, String displayName, String category, ProviderMode mode,
                                    Set<String> capabilities, boolean bookingSupported, boolean paymentsSupported,
                                    String message) {
        return new ProviderStatus(id, displayName, category, mode, List.copyOf(capabilities),
                bookingSupported, paymentsSupported, message);
    }
}

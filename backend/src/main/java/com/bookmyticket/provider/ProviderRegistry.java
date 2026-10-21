package com.bookmyticket.provider;

import com.bookmyticket.config.AppProperties;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Central provider registry. It reports the configured mode and the effective
 * mode. A provider configured as LIVE but without an implemented live adapter
 * is reported as UNAVAILABLE instead of silently falling back to demo data.
 */
@Service
public class ProviderRegistry {

    public record ProviderInfo(
            String id,
            String displayName,
            String category,
            ProviderMode configuredMode,
            ProviderMode effectiveMode,
            String message,
            boolean bookingSupported,
            List<String> capabilities
    ) {
    }

    private final AppProperties properties;
    private final List<TravelProvider> travelProviders;

    public ProviderRegistry(AppProperties properties, List<TravelProvider> travelProviders) {
        this.properties = properties;
        this.travelProviders = travelProviders;
    }

    public ProviderInfo resolve(String providerId) {
        ProviderMode configured = configuredMode(providerId);
        ProviderMode effective = effectiveMode(providerId, configured);
        return new ProviderInfo(providerId, displayName(providerId), category(providerId), configured, effective,
                message(providerId, configured, effective), bookingSupported(providerId, effective),
                capabilities(providerId));
    }

    public List<ProviderInfo> all() {
        return List.of(resolve(BusProvider.class.getSimpleName()), resolve(RailwayProvider.class.getSimpleName()),
                resolve(FlightProvider.class.getSimpleName()), resolve(CityTransportProvider.class.getSimpleName()));
    }

    public ProviderMode effectiveModeFor(Class<? extends TravelProvider> providerType) {
        return resolve(providerType.getSimpleName()).effectiveMode();
    }

    /** Services call this before serving data so unavailable providers fail loudly. */
    public void ensureAvailable(Class<? extends TravelProvider> providerType) {
        ProviderInfo info = resolve(providerType.getSimpleName());
        if (info.effectiveMode() == ProviderMode.UNAVAILABLE) {
            String label = "BusProvider".equals(info.id()) && info.configuredMode() == ProviderMode.LIVE
                    ? "Live bus inventory"
                    : info.displayName();
            throw com.bookmyticket.common.ApiException.unavailable(info.id().toUpperCase() + "_UNAVAILABLE",
                    label + " is not available in this deployment. " + info.message());
        }
    }

    /** Whether backend-only credentials for a live adapter are present. Never exposes their values. */
    public boolean liveCredentialsConfigured(Class<? extends TravelProvider> providerType) {
        return credentialsPresent(providerType.getSimpleName());
    }

    private ProviderMode configuredMode(String providerId) {
        if (providerId.equals(FlightProvider.class.getSimpleName())) {
            return ProviderMode.from(properties.getProviders().getFlight().getMode(), ProviderMode.DEMO);
        }
        if (providerId.equals(RailwayProvider.class.getSimpleName())) {
            return ProviderMode.from(properties.getProviders().getRailway().getMode(), ProviderMode.DEMO);
        }
        if (providerId.equals(CityTransportProvider.class.getSimpleName())) {
            return ProviderMode.from(properties.getProviders().getMtc().getMode(), ProviderMode.DEMO);
        }
        return ProviderMode.from(properties.getProviders().getBus().getMode(), ProviderMode.DEMO);
    }

    private ProviderMode effectiveMode(String providerId, ProviderMode configured) {
        if (configured != ProviderMode.LIVE) {
            return configured;
        }
        // No live bus/flight/rail adapter ships with this release. Never pretend.
        return ProviderMode.UNAVAILABLE;
    }

    private String message(String providerId, ProviderMode configured, ProviderMode effective) {
        if (configured == ProviderMode.LIVE && effective == ProviderMode.UNAVAILABLE) {
            if ("BusProvider".equals(providerId)) {
                return credentialsPresent(providerId)
                        ? "Live bus credentials are configured, but no live bus adapter is installed in this build, "
                        + "so the provider is UNAVAILABLE. No demo fallback is applied."
                        : "Live bus data is not connected: no authorized bus provider credentials are configured. "
                        + "No demo fallback is applied. Use DEMO mode for clearly labelled sample data, or install a "
                        + "live bus adapter and set BUS_LIVE_* in the backend environment.";
            }
            return "LIVE mode was requested, but no live booking adapter ships with this release, so the provider is "
                    + "reported as UNAVAILABLE. No demo fallback is applied. Configure DEMO mode to use sample data.";
        }
        return switch (effective) {
            case DEMO -> "Clearly labelled sample data. No real-world reservation or ticket is created.";
            case EXTERNAL_HANDOFF -> "Booking is handed off to a verified external channel.";
            case LIVE -> "Connected to a live authorized provider.";
            case UNAVAILABLE -> "Capability not currently available in this deployment.";
        };
    }

    private boolean credentialsPresent(String providerId) {
        return switch (providerId) {
            case "BusProvider" -> hasText(properties.getProviders().getBus().getLiveBaseUrl())
                    && hasText(properties.getProviders().getBus().getLiveApiKey());
            case "FlightProvider" -> hasText(properties.getProviders().getFlight().getAmadeusClientId())
                    && hasText(properties.getProviders().getFlight().getAmadeusClientSecret());
            default -> true;
        };
    }

    private boolean bookingSupported(String providerId, ProviderMode effective) {
        return switch (effective) {
            case DEMO -> providerId.equals("BusProvider") || providerId.equals("FlightProvider")
                    || providerId.equals("RailwayProvider");
            case LIVE -> true;
            case EXTERNAL_HANDOFF -> providerId.equals("RailwayProvider") || providerId.equals("FlightProvider");
            case UNAVAILABLE -> false;
        };
    }

    private List<String> capabilities(String providerId) {
        return travelProviders.stream()
                .filter(p -> p.getClass().getSimpleName().contains(baseName(providerId)))
                .findFirst()
                .map(TravelProvider::capabilities)
                .orElse(List.of());
    }

    private String baseName(String providerId) {
        return providerId.replace("Provider", "");
    }

    private String displayName(String providerId) {
        return travelProviders.stream()
                .filter(p -> p.getClass().getSimpleName().contains(baseName(providerId)))
                .findFirst()
                .map(TravelProvider::displayName)
                .orElse(providerId);
    }

    private String category(String providerId) {
        return travelProviders.stream()
                .filter(p -> p.getClass().getSimpleName().contains(baseName(providerId)))
                .findFirst()
                .map(TravelProvider::category)
                .orElse("OTHER");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

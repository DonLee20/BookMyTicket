package com.bookmyticket;

import com.bookmyticket.config.AppProperties;
import com.bookmyticket.provider.BusProvider;
import com.bookmyticket.provider.DemoBusProvider;
import com.bookmyticket.provider.ProviderMode;
import com.bookmyticket.provider.ProviderRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Live bus mode must never fall back to demo results, and the reported status
 * must distinguish missing credentials from a missing adapter.
 */
class ProviderRegistryTest {

    @Test
    void liveBusModeWithoutCredentialsIsUnavailableAndExplainsConfiguration() {
        AppProperties properties = new AppProperties();
        properties.getProviders().getBus().setMode("LIVE");
        ProviderRegistry registry = registry(properties);

        ProviderRegistry.ProviderInfo info = registry.resolve(BusProvider.class.getSimpleName());

        assertThat(info.configuredMode()).isEqualTo(ProviderMode.LIVE);
        assertThat(info.effectiveMode()).isEqualTo(ProviderMode.UNAVAILABLE);
        assertThat(info.message())
                .contains("Live bus data is not connected")
                .contains("BUS_LIVE_")
                .contains("No demo fallback");
        assertThat(registry.liveCredentialsConfigured(BusProvider.class)).isFalse();
    }

    @Test
    void liveBusCredentialsWithoutAdapterStillReportUnavailable() {
        AppProperties properties = new AppProperties();
        properties.getProviders().getBus().setMode("LIVE");
        properties.getProviders().getBus().setLiveProvider("Authorized Bus Partner");
        properties.getProviders().getBus().setLiveBaseUrl("https://bus-provider.example.test");
        properties.getProviders().getBus().setLiveApiKey("backend-only-key");
        ProviderRegistry registry = registry(properties);

        ProviderRegistry.ProviderInfo info = registry.resolve(BusProvider.class.getSimpleName());

        assertThat(info.effectiveMode()).isEqualTo(ProviderMode.UNAVAILABLE);
        assertThat(info.message())
                .contains("credentials are configured")
                .contains("no live bus adapter")
                .contains("No demo fallback");
        assertThat(registry.liveCredentialsConfigured(BusProvider.class)).isTrue();
    }

    private ProviderRegistry registry(AppProperties properties) {
        return new ProviderRegistry(properties, List.of(new DemoBusProvider(properties)));
    }
}

package com.bookmyticket.provider;

import com.bookmyticket.config.AppProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Chennai MTC adapter: static route information (sample / verified static) and
 * an explicit statement that ticket issuance is not enabled without an
 * authorized ticketing integration.
 */
@Component
public class DemoCityTransportProvider implements CityTransportProvider {

    private final AppProperties properties;

    public DemoCityTransportProvider(AppProperties properties) {
        this.properties = properties;
    }

    @Override
    public String id() {
        return "demo-mtc";
    }

    @Override
    public String displayName() {
        return "Chennai MTC route information (sample data)";
    }

    @Override
    public String category() {
        return "MTC";
    }

    @Override
    public ProviderMode mode() {
        return ProviderMode.from(properties.getProviders().getMtc().getMode(), ProviderMode.DEMO);
    }

    @Override
    public boolean bookingSupported() {
        return false;
    }

    @Override
    public List<String> capabilities() {
        return List.of("ROUTE_SEARCH", "STOP_SEARCH", "SAMPLE_TIMETABLE", "NO_TICKETING");
    }

    @Override
    public TicketingStatus ticketingStatus() {
        return new TicketingStatus(false, mode(),
                "MTC ticket purchase is not enabled in this deployment. Ticketing requires an authorized MTC "
                        + "ticketing integration or official partner channel. Use the official MTC channel for tickets.",
                officialInfoUrl());
    }

    @Override
    public String officialInfoUrl() {
        return properties.getUrls().getMtcOfficial();
    }

    @Override
    public String dataFreshness() {
        return "SAMPLE";
    }

    @Override
    public String statusMessage() {
        return "Route planning uses clearly labelled sample data. No live bus location, live ETA or official fare "
                + "feed is connected.";
    }
}

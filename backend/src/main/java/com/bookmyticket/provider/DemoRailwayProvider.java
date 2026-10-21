package com.bookmyticket.provider;

import com.bookmyticket.config.AppProperties;
import com.bookmyticket.transport.train.TrainService;
import com.bookmyticket.transport.train.TravelClass;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Demonstration railway adapter. Indian Railways reservations require an
 * authorized reservation channel, so this adapter only produces: sample search
 * data and an official booking handoff. It never issues a PNR or ticket.
 */
@Component
public class DemoRailwayProvider implements RailwayProvider {

    private final AppProperties properties;

    public DemoRailwayProvider(AppProperties properties) {
        this.properties = properties;
    }

    @Override
    public String id() {
        return "demo-railway";
    }

    @Override
    public String displayName() {
        return "Demo railway data + official IRCTC handoff";
    }

    @Override
    public String category() {
        return "RAILWAY";
    }

    @Override
    public ProviderMode mode() {
        return ProviderMode.from(properties.getProviders().getRailway().getMode(), ProviderMode.DEMO);
    }

    @Override
    public boolean bookingSupported() {
        return false;
    }

    @Override
    public List<String> capabilities() {
        return List.of("SEARCH", "SAMPLE_AVAILABILITY", "OFFICIAL_HANDOFF", "NO_TICKET_ISSUANCE");
    }

    @Override
    public String officialHandoffUrl() {
        return properties.getUrls().getIrctcOfficial();
    }

    @Override
    public Handoff createHandoff(TrainService service, LocalDate journeyDate, TravelClass travelClass, int passengers) {
        String reference = "REQ-" + journeyDate.toString().replace("-", "") + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return new Handoff(reference, officialHandoffUrl(),
                "This is a booking request record, not an Indian Railways ticket. No PNR has been created. "
                        + "Complete the reservation on the official IRCTC channel, where live availability and fares apply.",
                false);
    }

    @Override
    public String statusMessage() {
        return "Sample train data for browsing. A real reservation requires the official IRCTC channel; this platform "
                + "records a request and links you there.";
    }
}

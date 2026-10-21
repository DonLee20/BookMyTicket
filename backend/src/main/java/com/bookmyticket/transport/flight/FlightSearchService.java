package com.bookmyticket.transport.flight;

import com.bookmyticket.common.ApiException;
import com.bookmyticket.provider.FlightProvider;
import com.bookmyticket.provider.ProviderCallRecorder;
import com.bookmyticket.provider.ProviderMode;
import com.bookmyticket.provider.ProviderRegistry;
import com.bookmyticket.transport.flight.dto.FlightDtos;
import com.bookmyticket.transport.flight.dto.FlightOffer;
import com.bookmyticket.transport.flight.dto.FlightSearchQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class FlightSearchService {

    private final AirportRepository airportRepository;
    private final FlightProvider flightProvider;
    private final ProviderRegistry providerRegistry;
    private final ProviderCallRecorder callRecorder;

    public FlightSearchService(AirportRepository airportRepository, FlightProvider flightProvider,
                               ProviderRegistry providerRegistry, ProviderCallRecorder callRecorder) {
        this.airportRepository = airportRepository;
        this.flightProvider = flightProvider;
        this.providerRegistry = providerRegistry;
        this.callRecorder = callRecorder;
    }

    public List<FlightDtos.AirportOption> airports(String query) {
        String q = query == null || query.isBlank() ? "a" : query.trim();
        return airportRepository.search(q).stream()
                .limit(12)
                .map(a -> new FlightDtos.AirportOption(a.getIata(), a.getName(), a.getCity(), a.getCountry(), a.getRegion()))
                .toList();
    }

    @Transactional(readOnly = true)
    public FlightDtos.SearchResponse search(FlightDtos.SearchRequest request) {
        providerRegistry.ensureAvailable(FlightProvider.class);
        ProviderMode mode = providerRegistry.effectiveModeFor(FlightProvider.class);

        if (request.legs().size() > 4) {
            throw ApiException.badRequest("TOO_MANY_LEGS", "A maximum of four flight legs can be searched at once.");
        }
        int travellers = request.adults() + request.children() + request.infants();
        if (travellers > 9) {
            throw ApiException.badRequest("TOO_MANY_TRAVELLERS", "Up to nine travellers can be searched at once.");

        }
        List<FlightDtos.LegResult> results = new ArrayList<>();
        for (int i = 0; i < request.legs().size(); i++) {
            FlightDtos.SearchLeg leg = request.legs().get(i);
            if (leg.date().isBefore(LocalDate.now())) {
                throw ApiException.badRequest("PAST_DATE", "Flight dates cannot be in the past.");
            }
            String origin = leg.origin().trim().toUpperCase();
            String destination = leg.destination().trim().toUpperCase();
            if (origin.equals(destination)) {
                throw ApiException.badRequest("SAME_AIRPORT", "Origin and destination airports must differ.");
            }
            FlightSearchQuery query = new FlightSearchQuery(origin, destination, leg.date(), null,
                    request.cabin(), request.adults(), request.children(), request.infants(),
                    Boolean.TRUE.equals(request.nonStopOnly()), request.sort());
            List<FlightOffer> offers = callRecorder.timed(flightProvider.id(), "FLIGHT", "SEARCH",
                    origin + "-" + destination + "-" + leg.date(),
                    () -> flightProvider.search(query));
            String originCity = offers.isEmpty() ? origin : offers.get(0).originCity();
            String destinationCity = offers.isEmpty() ? destination : offers.get(0).destinationCity();
            results.add(new FlightDtos.LegResult(i, origin, destination, originCity, destinationCity, leg.date(), offers));
        }

        List<FlightDtos.CabinOption> cabins = List.of(
                new FlightDtos.CabinOption("ECONOMY", "Economy"),
                new FlightDtos.CabinOption("PREMIUM_ECONOMY", "Premium Economy"),
                new FlightDtos.CabinOption("BUSINESS", "Business"),
                new FlightDtos.CabinOption("FIRST", "First Class"));

        return new FlightDtos.SearchResponse(results, mode.name(), message(mode), Instant.now().toString(),
                false, flightProvider.externalHandoffUrl().orElse(null), cabins);
    }

    @Transactional(readOnly = true)
    public FlightOffer revalidate(String offerRef) {
        providerRegistry.ensureAvailable(FlightProvider.class);
        return callRecorder.timed(flightProvider.id(), "FLIGHT", "REVALIDATE", offerRef,
                () -> flightProvider.revalidate(offerRef)
                        .orElseThrow(() -> ApiException.conflict("OFFER_EXPIRED",
                                "This flight offer is no longer available. Please search again.")));
    }

    private String message(ProviderMode mode) {
        return switch (mode) {
            case DEMO -> "Demo offers with sample fares. Fares are revalidated before a hold request, but no ticket "
                    + "is issued and no airline confirmation is created.";
            case LIVE -> "Live offers from the connected flight provider.";
            case EXTERNAL_HANDOFF -> "Offers are shown here; ticketing continues on the provider channel.";
            case UNAVAILABLE -> "The flight provider is not available in this deployment.";
        };
    }
}

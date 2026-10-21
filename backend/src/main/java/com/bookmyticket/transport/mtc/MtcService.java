package com.bookmyticket.transport.mtc;

import com.bookmyticket.common.ApiException;
import com.bookmyticket.common.MoneyUtils;
import com.bookmyticket.provider.CityTransportProvider;
import com.bookmyticket.provider.ProviderMode;
import com.bookmyticket.provider.ProviderRegistry;
import com.bookmyticket.transport.mtc.dto.MtcDtos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class MtcService {

    private static final String SAMPLE_NOTE =
            "Sample route and timing data for planning only. Not live MTC operational information.";

    private final MtcStopRepository stopRepository;
    private final MtcRouteRepository routeRepository;
    private final MtcRouteStopRepository routeStopRepository;
    private final CityTransportProvider cityTransportProvider;
    private final ProviderRegistry providerRegistry;

    public MtcService(MtcStopRepository stopRepository, MtcRouteRepository routeRepository,
                      MtcRouteStopRepository routeStopRepository, CityTransportProvider cityTransportProvider,
                      ProviderRegistry providerRegistry) {
        this.stopRepository = stopRepository;
        this.routeRepository = routeRepository;
        this.routeStopRepository = routeStopRepository;
        this.cityTransportProvider = cityTransportProvider;
        this.providerRegistry = providerRegistry;
    }

    public List<MtcDtos.StopOption> stops(String query) {
        providerRegistry.ensureAvailable(CityTransportProvider.class);
        String q = query == null || query.isBlank() ? "a" : query.trim();
        return stopRepository.search(q).stream()
                .limit(12)
                .map(s -> new MtcDtos.StopOption(s.getId(), s.getName(), s.getLocality(), s.getZone()))
                .toList();
    }

    @Transactional(readOnly = true)
    public MtcDtos.PlanResponse plan(MtcDtos.PlanRequest request) {
        providerRegistry.ensureAvailable(CityTransportProvider.class);
        if (request.originStopId().equals(request.destinationStopId())) {
            throw ApiException.badRequest("SAME_STOP", "Choose two different stops.");
        }
        MtcStop origin = stopRepository.findById(request.originStopId())
                .orElseThrow(() -> ApiException.notFound("STOP_NOT_FOUND", "Origin stop not found."));
        MtcStop destination = stopRepository.findById(request.destinationStopId())
                .orElseThrow(() -> ApiException.notFound("STOP_NOT_FOUND", "Destination stop not found."));

        List<MtcRoute> candidateRoutes = routeRepository.findServingBothStops(List.of(origin.getId()), List.of(destination.getId()));
        List<MtcDtos.RoutePlan> plans = new ArrayList<>();
        for (MtcRoute route : candidateRoutes) {
            List<MtcRouteStop> routeStops = routeStopRepository.findWithStopByRouteId(route.getId());
            int originIndex = indexOf(routeStops, origin.getId());
            int destinationIndex = indexOf(routeStops, destination.getId());
            if (originIndex < 0 || destinationIndex < 0 || originIndex >= destinationIndex) {
                continue;
            }
            MtcRouteStop originStage = routeStops.get(originIndex);
            MtcRouteStop destinationStage = routeStops.get(destinationIndex);
            long fare = Math.max(0, destinationStage.getFareFromOriginMinor() - originStage.getFareFromOriginMinor());
            int stages = Math.max(1, destinationStage.getStageNumber() - originStage.getStageNumber());

            List<MtcDtos.RouteStopView> stopViews = new ArrayList<>();
            for (int i = originIndex; i <= destinationIndex; i++) {
                MtcRouteStop stop = routeStops.get(i);
                stopViews.add(new MtcDtos.RouteStopView(
                        stop.getStop().getId(),
                        stop.getStop().getName(),
                        stop.getStopSequence(),
                        stop.getStageNumber(),
                        stop.getFareFromOriginMinor(),
                        MoneyUtils.format(stop.getFareFromOriginMinor(), "INR"),
                        stop.getDistanceKm() == null ? null : stop.getDistanceKm().toPlainString(),
                        i == originIndex,
                        i == destinationIndex));
            }

            plans.add(new MtcDtos.RoutePlan(
                    route.getId(),
                    route.getRouteNumber(),
                    route.getDirection(),
                    "UP".equals(route.getDirection()) ? origin.getName() + " → " + destination.getName() + " direction"
                            : origin.getName() + " → " + destination.getName() + " direction",
                    origin.getName(),
                    destination.getName(),
                    route.getFirstService(),
                    route.getLastService(),
                    route.getFrequencyMinutes(),
                    route.getServiceDays(),
                    route.isAccessible(),
                    fare,
                    MoneyUtils.format(fare, "INR"),
                    stages,
                    stopViews,
                    route.getSource(),
                    SAMPLE_NOTE));
        }

        String transferSuggestion = plans.isEmpty()
                ? "No direct route between these stops exists in the sample dataset. Popular transfer points include "
                + "Chennai Central, Egmore, CMBT Koyambedu and Tambaram. Verify with official MTC information before travel."
                : null;

        CityTransportProvider.TicketingStatus ticketing = cityTransportProvider.ticketingStatus();
        ProviderMode mode = providerRegistry.effectiveModeFor(CityTransportProvider.class);

        return new MtcDtos.PlanResponse(plans,
                new MtcDtos.Ticketing(ticketing.supported(), ticketing.mode().name(), ticketing.message(), ticketing.officialUrl()),
                mode.name(),
                cityTransportProvider.dataFreshness(),
                "Route planning uses " + cityTransportProvider.dataFreshness().toLowerCase() + " data. "
                        + "No live bus location, live ETA or ticket confirmation is provided.",
                Instant.now().toString(),
                transferSuggestion);
    }

    private int indexOf(List<MtcRouteStop> stops, Long stopId) {
        for (int i = 0; i < stops.size(); i++) {
            if (stops.get(i).getStop().getId().equals(stopId)) {
                return i;
            }
        }
        return -1;
    }
}

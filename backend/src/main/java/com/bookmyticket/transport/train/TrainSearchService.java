package com.bookmyticket.transport.train;

import com.bookmyticket.common.ApiException;
import com.bookmyticket.common.MoneyUtils;
import com.bookmyticket.common.TimeUtils;
import com.bookmyticket.provider.ProviderCallRecorder;
import com.bookmyticket.provider.ProviderMode;
import com.bookmyticket.provider.ProviderRegistry;
import com.bookmyticket.provider.RailwayProvider;
import com.bookmyticket.transport.train.dto.TrainDtos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrainSearchService {

    private final RailwayStationRepository stationRepository;
    private final TrainServiceRepository serviceRepository;
    private final TrainServiceClassRepository classRepository;
    private final TrainRouteStopRepository routeStopRepository;
    private final ProviderRegistry providerRegistry;
    private final RailwayProvider railwayProvider;
    private final ProviderCallRecorder callRecorder;
    private final DemoTrainInventoryService demoTrainInventoryService;

    public TrainSearchService(RailwayStationRepository stationRepository, TrainServiceRepository serviceRepository,
                              TrainServiceClassRepository classRepository, TrainRouteStopRepository routeStopRepository,
                              ProviderRegistry providerRegistry, RailwayProvider railwayProvider,
                              ProviderCallRecorder callRecorder,
                              DemoTrainInventoryService demoTrainInventoryService) {
        this.stationRepository = stationRepository;
        this.serviceRepository = serviceRepository;
        this.classRepository = classRepository;
        this.routeStopRepository = routeStopRepository;
        this.providerRegistry = providerRegistry;
        this.railwayProvider = railwayProvider;
        this.callRecorder = callRecorder;
        this.demoTrainInventoryService = demoTrainInventoryService;
    }

    public List<TrainDtos.StationOption> stations(String query) {
        providerRegistry.ensureAvailable(RailwayProvider.class);
        String q = query == null || query.isBlank() ? "a" : query.trim();
        return stationRepository.search(q).stream()
                .limit(12)
                .map(s -> new TrainDtos.StationOption(s.getCode(), s.getName(), s.getCity(), s.getState()))
                .toList();
    }

    @Transactional
    public TrainDtos.SearchResponse search(TrainDtos.SearchRequest request) {
        providerRegistry.ensureAvailable(RailwayProvider.class);
        ProviderMode mode = providerRegistry.effectiveModeFor(RailwayProvider.class);

        if (request.date().isBefore(LocalDate.now())) {
            throw ApiException.badRequest("PAST_DATE", "Please choose today or a future travel date.");
        }
        List<Long> originIds = demoTrainInventoryService.resolveStationIds(request.from(), "origin");
        List<Long> destinationIds = demoTrainInventoryService.resolveStationIds(request.to(), "destination");
        if (originIds.equals(destinationIds)) {
            throw ApiException.badRequest("SAME_STATION", "Origin and destination stations must differ.");
        }
        if (mode == ProviderMode.DEMO) {
            demoTrainInventoryService.ensureServices(originIds, destinationIds, request.date(),
                    request.from(), request.to());
        }

        List<TrainService> services = callRecorder.timed(railwayProvider.id(), "RAILWAY", "SEARCH",
                request.from() + "->" + request.to() + "@" + request.date(),
                () -> serviceRepository.findBetweenStations(originIds, destinationIds));

        List<TrainDtos.TrainResult> results = new ArrayList<>();
        for (TrainService service : services) {
            results.add(toResult(service, request));
        }
        long runningCount = results.stream().filter(TrainDtos.TrainResult::runsOnDate).count();

        List<TrainDtos.QuotaOption> quotas = List.of(
                new TrainDtos.QuotaOption("GENERAL", "General"),
                new TrainDtos.QuotaOption("LADIES", "Ladies"),
                new TrainDtos.QuotaOption("SENIOR_CITIZEN", "Senior citizen"),
                new TrainDtos.QuotaOption("TATKAL", "Tatkal (subject to official channel rules)"));

        String note = runningCount == 0 && !results.isEmpty()
                ? "None of the sample trains run on the selected day. Change the date to see running days."
                : "Availability shown is SAMPLE data for demonstration, not live Indian Railways availability. "
                + "Confirm real availability on the official channel before travelling.";

        return new TrainDtos.SearchResponse(results, mode.name(), message(mode), Instant.now().toString(),
                railwayProvider.officialHandoffUrl(), note, quotas);
    }

    private TrainDtos.TrainResult toResult(TrainService service, TrainDtos.SearchRequest request) {
        List<TrainDtos.ClassAvailability> classes = classRepository
                .findByServiceIdOrderByFareMinorAsc(service.getId()).stream()
                .filter(clazz -> request.travelClass() == null || request.travelClass().isBlank()
                        || clazz.getTravelClass().name().equals(request.travelClass()))
                .map(clazz -> new TrainDtos.ClassAvailability(
                        clazz.getTravelClass().name(),
                        clazz.getTravelClass().getLabel(),
                        clazz.getFareMinor(),
                        clazz.getFareMinor() == null ? null : MoneyUtils.format(clazz.getFareMinor(), "INR"),
                        clazz.getAvailabilityStatus(),
                        clazz.getAvailableSeats(),
                        clazz.getWlPosition(),
                        "Sample availability — not live"))
                .toList();

        List<TrainDtos.RouteStop> stops = routeStopRepository.findWithStationByServiceId(service.getId()).stream()
                .map(stop -> new TrainDtos.RouteStop(
                        stop.getStation().getCode(),
                        stop.getStation().getName(),
                        stop.getArriveOffsetMinutes() == null ? null
                                : TimeUtils.clock(service.getDepartMinutes() + stop.getArriveOffsetMinutes()),
                        stop.getDepartOffsetMinutes() == null ? null
                                : TimeUtils.clock(service.getDepartMinutes() + stop.getDepartOffsetMinutes()),
                        stop.getDayNumber(),
                        stop.getDistanceKm()))
                .toList();

        int arriveOffset = TimeUtils.dayOffset(service.getDepartMinutes(), service.getArriveMinutes());
        return new TrainDtos.TrainResult(
                service.getId(),
                service.getTrainNumber(),
                service.getName(),
                service.getOriginStation().getCode(),
                service.getOriginStation().getName(),
                service.getDestinationStation().getCode(),
                service.getDestinationStation().getName(),
                TimeUtils.clock(service.getDepartMinutes()),
                TimeUtils.clock(service.getArriveMinutes()),
                arriveOffset,
                TimeUtils.dayOffsetLabel(arriveOffset),
                service.getDurationMinutes(),
                durationLabel(service.getDurationMinutes()),
                service.getRunningDays(),
                service.runsOn(request.date().getDayOfWeek()),
                service.getTotalDistanceKm(),
                classes,
                stops);
    }

    static String durationLabel(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (hours >= 24) {
            return (hours / 24) + "d " + (hours % 24) + "h" + (mins > 0 ? " " + mins + "m" : "");
        }
        return mins == 0 ? hours + "h" : hours + "h " + mins + "m";
    }

    private String message(ProviderMode mode) {
        return switch (mode) {
            case DEMO -> "Sample train data. No live availability, PNR or ticket is created here; a booking request "
                    + "links you to the official IRCTC channel.";
            case LIVE -> "Live railway availability from the authorized provider.";
            case EXTERNAL_HANDOFF -> "Search is available here; reservation continues on the official railway channel.";
            case UNAVAILABLE -> "The railway provider is not available in this deployment.";
        };
    }
}

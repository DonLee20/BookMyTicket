package com.bookmyticket.transport.bus;

import com.bookmyticket.common.ApiException;
import com.bookmyticket.common.MoneyUtils;
import com.bookmyticket.common.TimeUtils;
import com.bookmyticket.config.AppProperties;
import com.bookmyticket.config.DemoInventorySeeder;
import com.bookmyticket.provider.ProviderMode;
import com.bookmyticket.provider.ProviderRegistry;
import com.bookmyticket.transport.bus.dto.BusDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BusSearchService {

    private static final Logger log = LoggerFactory.getLogger(BusSearchService.class);

    private final BusTripRepository tripRepository;
    private final BusSeatRepository seatRepository;
    private final BusScheduleRepository scheduleRepository;
    private final DemoInventorySeeder demoInventorySeeder;
    private final ProviderRegistry providerRegistry;
    private final AppProperties properties;

    public BusSearchService(BusTripRepository tripRepository, BusSeatRepository seatRepository,
                            BusScheduleRepository scheduleRepository, DemoInventorySeeder demoInventorySeeder,
                            ProviderRegistry providerRegistry, AppProperties properties) {
        this.tripRepository = tripRepository;
        this.seatRepository = seatRepository;
        this.scheduleRepository = scheduleRepository;
        this.demoInventorySeeder = demoInventorySeeder;
        this.providerRegistry = providerRegistry;
        this.properties = properties;
    }

    public List<String> cityOptions(String query, boolean origin) {
        providerRegistry.ensureAvailable(BusProviderType.TYPE);
        String q = query == null ? "" : query.trim();
        return origin ? tripRepository.searchDepartureCities(q) : tripRepository.searchArrivalCities(q);
    }

    @Transactional
    public BusDtos.SearchResponse search(BusDtos.SearchRequest request) {
        ensureBusAvailable();
        ProviderMode mode = providerRegistry.effectiveModeFor(BusProviderType.TYPE);
        AmenityCatalog.validateRequested(request.amenities());

        LocalDate date = request.date();
        if (date.isBefore(LocalDate.now(TimeUtils.INDIA))) {
            throw ApiException.badRequest("PAST_DATE", "Please choose today or a future date.");
        }

        String from = request.from().trim();
        String to = request.to().trim();
        if (from.equalsIgnoreCase(to)) {
            throw ApiException.badRequest("SAME_CITY", "Boarding and destination cities must differ.");
        }
        if (mode == ProviderMode.DEMO) {
            demoInventorySeeder.ensureRouteInventory(from, to, date);
        }
        List<BusTrip> trips = tripRepository.search(from, to, date);
        Map<Long, Integer> availability = availabilityCounts(trips);

        List<BusDtos.TripResult> results = trips.stream()
                .map(trip -> toResult(trip, availability.getOrDefault(trip.getId(), 0)))
                .filter(result -> matchesFilters(result, request))
                .sorted(comparator(request.sort()))
                .toList();

        BusDtos.SearchDiagnostics diagnostics = diagnostics(request, trips, results, mode);
        boolean liveConnected = mode == ProviderMode.LIVE;
        boolean liveConfigured = providerRegistry.liveCredentialsConfigured(BusProviderType.TYPE);

        log.info("Bus search mode={} from={} to={} date={} tripsOnDate={} results={} status={} "
                        + "liveConnected={} liveCredentialsConfigured={}",
                mode, from, to, date, trips.size(), results.size(), diagnostics.status(),
                liveConnected, liveConfigured);
        if (diagnostics.status() != BusDtos.SearchStatus.OK) {
            log.warn("Bus search has no bookable rows: status={} from={} to={} date={} "
                            + "originCovered={} destinationCovered={} routeCovered={} reason={}",
                    diagnostics.status(), from, to, date, diagnostics.originCovered(),
                    diagnostics.destinationCovered(), diagnostics.routeCovered(), diagnostics.message());
        }

        return new BusDtos.SearchResponse(results, mode.name(), modeMessage(mode), Instant.now().toString(),
                liveConnected, liveConfigured, diagnostics);
    }

    @Transactional(readOnly = true)
    public BusDtos.PremiumHighlights premiumHighlights() {
        providerRegistry.ensureAvailable(BusProviderType.TYPE);
        LocalDate date = LocalDate.now().plusDays(1);
        List<BusTrip> trips = tripRepository.search("Chennai", "Bengaluru", date).stream()
                .filter(BusTrip::isPremium)
                .limit(6)
                .toList();
        Map<Long, Integer> availability = availabilityCounts(trips);
        List<BusDtos.TripResult> results = trips.stream()
                .map(trip -> toResult(trip, availability.getOrDefault(trip.getId(), 0)))
                .toList();
        ProviderMode mode = providerRegistry.effectiveModeFor(BusProviderType.TYPE);
        return new BusDtos.PremiumHighlights(results,
                List.of("Volvo-style AC coaches", "Luxury AC sleepers", "Multi-axle premium coaches",
                        "Premium semi-sleepers with extra legroom"),
                mode.name(), modeMessage(mode), mode == ProviderMode.LIVE,
                providerRegistry.liveCredentialsConfigured(BusProviderType.TYPE));
    }

    public BusDtos.TripResult toResult(BusTrip trip, int availableSeats) {
        ProviderMode mode = providerRegistry.effectiveModeFor(BusProviderType.TYPE);
        int dayOffset = TimeUtils.dayOffset(trip.getSchedule().getDepartMinutes(), trip.getSchedule().getDepartMinutes()
                + trip.getSchedule().getDurationMinutes());
        List<BusDtos.Amenity> amenities = AmenityCatalog.parse(trip.getAmenities()).stream()
                .map(code -> new BusDtos.Amenity(code, AmenityCatalog.label(code)))
                .toList();
        String availabilityStatus = availableSeats == 0 ? "SOLD_OUT" : availableSeats <= 6 ? "FILLING_FAST" : "AVAILABLE";
        return new BusDtos.TripResult(
                trip.getId(),
                trip.getSchedule().getOperator().getName(),
                trip.getSchedule().getOperator().getCode(),
                trip.isPremium(),
                trip.getBusType().name(),
                trip.getBusType().getLabel(),
                trip.getDepartureCity(),
                trip.getArrivalCity(),
                TimeUtils.localClock(trip.getDepartAt(), TimeUtils.INDIA),
                TimeUtils.localClock(trip.getArriveAt(), TimeUtils.INDIA),
                dayOffset,
                TimeUtils.dayOffsetLabel(dayOffset),
                trip.getTravelDate().toString(),
                trip.getSchedule().getDurationMinutes(),
                durationLabel(trip.getSchedule().getDurationMinutes()),
                trip.getFareMinor(),
                MoneyUtils.format(trip.getFareMinor(), trip.getCurrency()),
                trip.getCurrency(),
                availableSeats,
                trip.getTotalSeats(),
                availabilityStatus,
                amenities,
                AmenityCatalog.parse(trip.getBoardingPoints()),
                AmenityCatalog.parse(trip.getDroppingPoints()),
                cancellationPolicy(trip.isPremium()),
                mode.name(),
                trip.getSource(),
                true
        );
    }

    private BusDtos.SearchDiagnostics diagnostics(BusDtos.SearchRequest request, List<BusTrip> trips,
                                                   List<BusDtos.TripResult> results, ProviderMode mode) {
        String from = request.from().trim();
        String to = request.to().trim();
        LocalDate today = LocalDate.now(TimeUtils.INDIA);
        LocalDate inventoryFrom = null;
        LocalDate inventoryTo = null;
        boolean dateInWindow = true;
        if (mode == ProviderMode.DEMO) {
            inventoryFrom = today;
            // Lazily generated inventory covers any future date, so the reported
            // window always includes the date the customer searched for.
            inventoryTo = today.plusDays(demoDaysAhead());
            if (request.date().isAfter(inventoryTo)) {
                inventoryTo = request.date();
            }
            dateInWindow = !request.date().isBefore(inventoryFrom) && !request.date().isAfter(inventoryTo);
        }

        boolean routeCovered = !trips.isEmpty();
        boolean originCovered = routeCovered;
        boolean destinationCovered = routeCovered;
        List<BusDtos.RouteSuggestion> suggestions = List.of();
        if (trips.isEmpty()) {
            List<BusCorridor> corridors = scheduleRepository.findActiveCorridors();
            originCovered = corridors.stream().anyMatch(corridor -> involves(corridor, from));
            destinationCovered = corridors.stream().anyMatch(corridor -> involves(corridor, to));
            routeCovered = corridors.stream().anyMatch(corridor -> connects(corridor, from, to));
            suggestions = suggestions(corridors, from, to);
        }

        BusDtos.SearchStatus status;
        if (!originCovered) {
            status = BusDtos.SearchStatus.ORIGIN_NOT_COVERED;
        } else if (!destinationCovered) {
            status = BusDtos.SearchStatus.DESTINATION_NOT_COVERED;
        } else if (!routeCovered) {
            status = BusDtos.SearchStatus.ROUTE_NOT_COVERED;
        } else if (!dateInWindow) {
            status = BusDtos.SearchStatus.OUTSIDE_DEMO_WINDOW;
        } else if (trips.isEmpty()) {
            status = BusDtos.SearchStatus.NO_TRIPS_ON_DATE;
        } else if (results.isEmpty()) {
            status = BusDtos.SearchStatus.FILTERED_OUT;
        } else {
            status = BusDtos.SearchStatus.OK;
        }

        String message = diagnosticMessage(status, from, to, request.date(), mode, results.size(),
                trips.size(), inventoryFrom, inventoryTo);
        return new BusDtos.SearchDiagnostics(status, originCovered, destinationCovered, routeCovered,
                dateInWindow, inventoryFrom, inventoryTo, trips.size(), results.size(), message, suggestions);
    }

    private String diagnosticMessage(BusDtos.SearchStatus status, String from, String to, LocalDate date,
                                     ProviderMode mode, int resultCount, int tripCount,
                                     LocalDate inventoryFrom, LocalDate inventoryTo) {
        String inventory = mode == ProviderMode.LIVE ? "Live inventory" : "Demo inventory";
        String noun = mode == ProviderMode.LIVE ? "live trips" : "demo trips";
        return switch (status) {
            case OK -> resultCount + " " + noun + " found for " + from + " to " + to + " on " + date + ".";
            case ORIGIN_NOT_COVERED -> inventory + " has no services departing from " + from
                    + ". Check the boarding city or browse another demo corridor.";
            case DESTINATION_NOT_COVERED -> inventory + " has no services arriving in " + to
                    + ". Check the destination or browse another demo corridor.";
            case ROUTE_NOT_COVERED -> inventory + " covers " + from + " and " + to
                    + " separately, but there is no direct route between them. Choose one of the demo routes below "
                    + "or change your search. This does not mean no real-world buses exist.";
            case OUTSIDE_DEMO_WINDOW -> inventory + " is generated from " + inventoryFrom + " to " + inventoryTo
                    + ". Choose a date inside that window.";
            case NO_TRIPS_ON_DATE -> "The demo route is covered, but no sample trips were generated for " + date
                    + ". Try another date.";
            case FILTERED_OUT -> inventory + " has " + tripCount + " trip(s) on this route and date, but none match "
                    + "the selected filters.";
        };
    }

    private List<BusDtos.RouteSuggestion> suggestions(List<BusCorridor> corridors, String from, String to) {
        LinkedHashMap<String, BusDtos.RouteSuggestion> suggestions = new LinkedHashMap<>();
        // Routes arriving at the requested destination first, then departures from the requested origin.
        corridors.stream()
                .filter(corridor -> involves(corridor, to))
                .map(corridor -> routeTo(corridor, to))
                .filter(route -> route != null && !connects(route.from(), route.to(), from, to))
                .forEach(route -> suggestions.putIfAbsent(routeKey(route), route));
        corridors.stream()
                .filter(corridor -> involves(corridor, from))
                .map(corridor -> routeFrom(corridor, from))
                .filter(route -> route != null && !connects(route.from(), route.to(), from, to))
                .forEach(route -> suggestions.putIfAbsent(routeKey(route), route));
        return suggestions.values().stream().limit(4).toList();
    }

    private BusDtos.RouteSuggestion routeTo(BusCorridor corridor, String destination) {
        if (corridor.getCityA().equalsIgnoreCase(destination)) {
            return new BusDtos.RouteSuggestion(corridor.getCityB(), corridor.getCityA());
        }
        if (corridor.getCityB().equalsIgnoreCase(destination)) {
            return new BusDtos.RouteSuggestion(corridor.getCityA(), corridor.getCityB());
        }
        return null;
    }

    private BusDtos.RouteSuggestion routeFrom(BusCorridor corridor, String origin) {
        if (corridor.getCityA().equalsIgnoreCase(origin)) {
            return new BusDtos.RouteSuggestion(corridor.getCityA(), corridor.getCityB());
        }
        if (corridor.getCityB().equalsIgnoreCase(origin)) {
            return new BusDtos.RouteSuggestion(corridor.getCityB(), corridor.getCityA());
        }
        return null;
    }

    private boolean involves(BusCorridor corridor, String city) {
        return corridor.getCityA().equalsIgnoreCase(city) || corridor.getCityB().equalsIgnoreCase(city);
    }

    private boolean connects(BusCorridor corridor, String from, String to) {
        return connects(corridor.getCityA(), corridor.getCityB(), from, to)
                || connects(corridor.getCityB(), corridor.getCityA(), from, to);
    }

    private boolean connects(String cityA, String cityB, String from, String to) {
        return cityA.equalsIgnoreCase(from) && cityB.equalsIgnoreCase(to);
    }

    private String routeKey(BusDtos.RouteSuggestion route) {
        return route.from().toLowerCase(Locale.ROOT) + "|" + route.to().toLowerCase(Locale.ROOT);
    }

    private int demoDaysAhead() {
        return Math.min(Math.max(properties.getDemo().getBusDaysAhead(), 1), 90);
    }

    private void ensureBusAvailable() {
        try {
            providerRegistry.ensureAvailable(BusProviderType.TYPE);
        } catch (ApiException ex) {
            log.warn("Bus provider unavailable: {}", ex.getMessage());
            throw ex;
        }
    }

    private Map<Long, Integer> availabilityCounts(List<BusTrip> trips) {
        if (trips.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = trips.stream().map(BusTrip::getId).toList();
        Map<Long, Integer> map = new HashMap<>();
        for (Object[] row : seatRepository.countAvailableByTripIds(ids)) {
            map.put((Long) row[0], ((Number) row[1]).intValue());
        }
        return map;
    }

    private boolean matchesFilters(BusDtos.TripResult result, BusDtos.SearchRequest request) {
        if (Boolean.TRUE.equals(request.premiumOnly()) && !result.premium()) {
            return false;
        }
        if (request.maxFareMinor() != null && result.fareMinor() > request.maxFareMinor()) {
            return false;
        }
        if (request.busTypes() != null && !request.busTypes().isEmpty() && !request.busTypes().contains(result.busType())) {
            return false;
        }
        if (Boolean.TRUE.equals(request.sleeperOnly()) && !"SLEEPER".equals(result.busType()) ) {
            return false;
        }
        if (Boolean.TRUE.equals(request.seaterOnly()) && "SLEEPER".equals(result.busType())) {
            return false;
        }
        if (request.operator() != null && !request.operator().isBlank()
                && !result.operatorName().toLowerCase().contains(request.operator().trim().toLowerCase())) {
            return false;
        }
        if (request.amenities() != null && !request.amenities().isEmpty()) {
            Set<String> tripAmenities = result.amenities().stream()
                    .map(BusDtos.Amenity::code).collect(Collectors.toSet());
            if (!tripAmenities.containsAll(request.amenities())) {
                return false;
            }
        }
        return true;
    }

    private Comparator<BusDtos.TripResult> comparator(String sort) {
        String key = sort == null ? "price" : sort;
        return switch (key) {
            case "departure" -> Comparator.comparing(BusDtos.TripResult::departTime);
            case "duration" -> Comparator.comparingInt(BusDtos.TripResult::durationMinutes);
            case "arrival" -> Comparator.comparing(BusDtos.TripResult::arriveTime);
            case "price" -> Comparator.comparingLong(BusDtos.TripResult::fareMinor);
            default -> throw ApiException.badRequest("INVALID_SORT", "Unsupported sort option: " + sort);
        };
    }

    static String durationLabel(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return mins == 0 ? hours + "h" : hours + "h " + mins + "m";
    }

    public static String cancellationPolicy(boolean premium) {
        return premium
                ? "Free cancellation up to 24 hours before departure (90% refund). 50% refund between 6 and 24 hours. "
                + "No refund within 6 hours of departure. Operator cancellation is always fully refunded."
                : "90% refund up to 24 hours before departure, 50% between 6 and 24 hours, no refund within 6 hours.";
    }

    private String modeMessage(ProviderMode mode) {
        return switch (mode) {
            case DEMO -> "Demo inventory: schedules, fares and seat availability are sample data, not a live operator "
                    + "feed. Live bus data is not connected in this deployment.";
            case LIVE -> "Live inventory from the connected bus provider.";
            case EXTERNAL_HANDOFF -> "Search is available here; booking continues on the provider channel.";
            case UNAVAILABLE -> "The bus provider is not available in this deployment.";
        };
    }

    static BigDecimal percentage(long amount, int percent) {
        return BigDecimal.valueOf(amount).multiply(BigDecimal.valueOf(percent))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
    }

    /** Small indirection so the registry can guard the provider category. */
    public static final class BusProviderType {
        public static final Class<com.bookmyticket.provider.BusProvider> TYPE = com.bookmyticket.provider.BusProvider.class;

        private BusProviderType() {
        }
    }
}

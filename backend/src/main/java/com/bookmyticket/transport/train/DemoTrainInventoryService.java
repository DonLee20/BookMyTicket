package com.bookmyticket.transport.train;

import com.bookmyticket.common.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Lazily creates clearly labelled sample railway stations, services, classes and
 * route stops. This keeps demo train search useful for any city pair or date
 * instead of failing because the seeded timetable does not cover a route.
 */
@Service
public class DemoTrainInventoryService {

    private static final Logger log = LoggerFactory.getLogger(DemoTrainInventoryService.class);

    private final RailwayStationRepository stationRepository;
    private final TrainServiceRepository serviceRepository;
    private final TrainServiceClassRepository classRepository;
    private final TrainRouteStopRepository routeStopRepository;

    public DemoTrainInventoryService(RailwayStationRepository stationRepository,
                                     TrainServiceRepository serviceRepository,
                                     TrainServiceClassRepository classRepository,
                                     TrainRouteStopRepository routeStopRepository) {
        this.stationRepository = stationRepository;
        this.serviceRepository = serviceRepository;
        this.classRepository = classRepository;
        this.routeStopRepository = routeStopRepository;
    }

    @Transactional
    public List<Long> resolveStationIds(String query, String role) {
        String trimmed = query == null ? "" : query.trim();
        if (trimmed.isEmpty()) {
            throw ApiException.badRequest("STATION_REQUIRED",
                    "Enter a station code or city for the " + role + " station.");
        }

        var byCode = stationRepository.findByCodeIgnoreCase(trimmed);
        if (byCode.isPresent()) {
            return List.of(byCode.get().getId());
        }

        List<RailwayStation> matches = stationRepository.search(trimmed).stream()
                .filter(station -> station.getCity().equalsIgnoreCase(trimmed)
                        || station.getName().toLowerCase(Locale.ROOT)
                                .contains(trimmed.toLowerCase(Locale.ROOT)))
                .toList();
        if (!matches.isEmpty()) {
            return matches.stream().map(RailwayStation::getId).toList();
        }

        RailwayStation created = createStation(trimmed);
        log.info("Created demo railway station {} for '{}' as the {} station.", created.getCode(), trimmed, role);
        return List.of(created.getId());
    }

    /**
     * Makes sure a direct sample service exists for the requested station pair
     * and that at least one option runs on the selected day.
     */
    @Transactional
    public void ensureServices(List<Long> originIds, List<Long> destinationIds, LocalDate date,
                               String originQuery, String destinationQuery) {
        if (originIds.isEmpty() || destinationIds.isEmpty() || originIds.equals(destinationIds)) {
            return;
        }
        List<TrainService> existing = serviceRepository.findBetweenStations(originIds, destinationIds);
        if (!existing.isEmpty()
                && existing.stream().anyMatch(service -> service.runsOn(date.getDayOfWeek()))) {
            return;
        }

        RailwayStation origin = stationRepository.findById(originIds.get(0))
                .orElseThrow(() -> ApiException.notFound("STATION_NOT_FOUND",
                        "No railway station matched '" + originQuery + "'."));
        RailwayStation destination = stationRepository.findById(destinationIds.get(0))
                .orElseThrow(() -> ApiException.notFound("STATION_NOT_FOUND",
                        "No railway station matched '" + destinationQuery + "'."));
        if (origin.getId().equals(destination.getId())) {
            throw ApiException.badRequest("SAME_STATION",
                    "Origin and destination stations must differ.");
        }

        int seed = Objects.hash(origin.getCode(), destination.getCode());
        int distanceKm = 120 + Math.floorMod(seed, 1_400);
        int durationMinutes = Math.min(1_800, Math.max(75, Math.round(distanceKm * 60.0f / 65.0f)));
        List<String> names = List.of("Demo Express", "Sample Superfast", "Demo Intercity");

        for (int index = 0; index < names.size(); index++) {
            int departMinutes = Math.floorMod(seed + index * 347, 24 * 60);
            TrainService service = new TrainService();
            service.setTrainNumber(nextTrainNumber(seed + index * 7_919));
            service.setName(trimTo(origin.getCity() + " – " + destination.getCity() + " " + names.get(index), 150));
            service.setOriginStation(origin);
            service.setDestinationStation(destination);
            service.setDepartMinutes(departMinutes);
            service.setArriveMinutes(departMinutes + durationMinutes);
            service.setDurationMinutes(durationMinutes);
            service.setRunningDays("DAILY");
            service.setTotalDistanceKm(distanceKm);
            service.setActive(true);
            TrainService saved = serviceRepository.save(service);
            createClasses(saved, distanceKm, seed + index * 13);
            createRouteStops(saved, origin, destination, durationMinutes, distanceKm);
        }
        log.info("Generated demo rail services from {} to {} for query '{}' -> '{}'.",
                origin.getCode(), destination.getCode(), originQuery, destinationQuery);
    }

    private RailwayStation createStation(String query) {
        String display = titleCase(query);
        String base = display.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        if (base.isEmpty()) {
            base = "DEMO";
        }
        base = base.substring(0, Math.min(5, base.length()));

        int seed = Math.abs(query.toLowerCase(Locale.ROOT).hashCode());
        String code = null;
        for (int attempt = 0; attempt < 1_000; attempt++) {
            String candidate = base + String.format("%03d", Math.floorMod(seed + attempt, 1_000));
            if (stationRepository.findByCodeIgnoreCase(candidate).isEmpty()) {
                code = candidate;
                break;
            }
        }
        if (code == null) {
            throw ApiException.conflict("STATION_CODE_EXHAUSTED",
                    "Could not create a demo station code for '" + query + "'.");
        }

        RailwayStation station = new RailwayStation();
        station.setCode(code);
        station.setName(trimTo(display + " Demo Station", 150));
        station.setCity(trimTo(display, 80));
        station.setState("Demo Network");
        station.setZone("DEMO");
        return stationRepository.save(station);
    }

    private void createClasses(TrainService service, int distanceKm, int seed) {
        List<TravelClass> templates = List.of(
                TravelClass.AC_2_TIER,
                TravelClass.AC_3_TIER,
                TravelClass.SLEEPER,
                TravelClass.CHAIR_CAR,
                TravelClass.SECOND_SITTING);

        for (TravelClass travelClass : templates) {
            long fareMinor = Math.max(minimumFare(travelClass),
                    Math.round(distanceKm * perKilometreFare(travelClass) / 100.0) * 100L);
            int availableSeats = 8 + Math.floorMod(seed + travelClass.ordinal() * 17, 72);

            TrainServiceClass clazz = new TrainServiceClass();
            clazz.setService(service);
            clazz.setTravelClass(travelClass);
            clazz.setFareMinor(fareMinor);
            clazz.setAvailabilityStatus("AVAILABLE");
            clazz.setAvailableSeats(availableSeats);
            classRepository.save(clazz);
        }
    }

    private void createRouteStops(TrainService service, RailwayStation origin, RailwayStation destination,
                                  int durationMinutes, int distanceKm) {
        List<TrainRouteStop> stops = new ArrayList<>();

        TrainRouteStop originStop = new TrainRouteStop();
        originStop.setService(service);
        originStop.setStation(origin);
        originStop.setStopNumber(1);
        originStop.setDepartOffsetMinutes(0);
        originStop.setDistanceKm(0);
        originStop.setDayNumber(1);
        stops.add(originStop);

        TrainRouteStop destinationStop = new TrainRouteStop();
        destinationStop.setService(service);
        destinationStop.setStation(destination);
        destinationStop.setStopNumber(2);
        destinationStop.setArriveOffsetMinutes(durationMinutes);
        destinationStop.setDistanceKm(distanceKm);
        destinationStop.setDayNumber(1 + durationMinutes / (24 * 60));
        stops.add(destinationStop);

        routeStopRepository.saveAll(stops);
    }

    private String nextTrainNumber(int seed) {
        int candidate = 90_000 + Math.floorMod(seed, 9_999);
        for (int attempt = 0; attempt < 10_000; attempt++) {
            String number = String.format("%05d", candidate);
            if (!serviceRepository.existsByTrainNumber(number)) {
                return number;
            }
            candidate = 90_000 + Math.floorMod(candidate - 90_000 + 1, 9_999);
        }
        throw ApiException.conflict("TRAIN_NUMBER_EXHAUSTED",
                "Could not allocate a demo train number.");
    }

    private long perKilometreFare(TravelClass travelClass) {
        return switch (travelClass) {
            case AC_FIRST -> 380L;
            case AC_2_TIER -> 250L;
            case AC_3_TIER -> 160L;
            case AC_3_TIER_ECONOMY -> 145L;
            case SLEEPER -> 75L;
            case SECOND_SITTING -> 50L;
            case CHAIR_CAR -> 110L;
            case EXECUTIVE_CHAIR_CAR -> 180L;
        };
    }

    private long minimumFare(TravelClass travelClass) {
        return switch (travelClass) {
            case AC_FIRST -> 120_000L;
            case AC_2_TIER -> 60_000L;
            case AC_3_TIER -> 40_000L;
            case AC_3_TIER_ECONOMY -> 35_000L;
            case SLEEPER -> 18_000L;
            case SECOND_SITTING -> 10_000L;
            case CHAIR_CAR -> 25_000L;
            case EXECUTIVE_CHAIR_CAR -> 45_000L;
        };
    }

    private String titleCase(String raw) {
        String value = raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
        if (value.length() > 80) {
            value = value.substring(0, 80).trim();
        }
        StringBuilder builder = new StringBuilder();
        for (String word : value.split(" ")) {
            if (word.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            if (word.equals(word.toUpperCase(Locale.ROOT)) || word.equals(word.toLowerCase(Locale.ROOT))) {
                builder.append(word.substring(0, 1).toUpperCase(Locale.ROOT))
                        .append(word.substring(1).toLowerCase(Locale.ROOT));
            } else {
                builder.append(word);
            }
        }
        return builder.length() == 0 ? "Demo City" : builder.toString();
    }

    private String trimTo(String value, int max) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.length() > max ? trimmed.substring(0, max) : trimmed;
    }
}

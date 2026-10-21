package com.bookmyticket.config;

import com.bookmyticket.common.TimeUtils;
import com.bookmyticket.transport.bus.BusCorridor;
import com.bookmyticket.transport.bus.BusCorridorRepository;
import com.bookmyticket.transport.bus.BusOperator;
import com.bookmyticket.transport.bus.BusOperatorRepository;
import com.bookmyticket.transport.bus.BusSchedule;
import com.bookmyticket.transport.bus.BusScheduleRepository;
import com.bookmyticket.transport.bus.BusSeat;
import com.bookmyticket.transport.bus.BusTrip;
import com.bookmyticket.transport.bus.BusTripRepository;
import com.bookmyticket.transport.bus.BusType;
import com.bookmyticket.transport.bus.SeatDeck;
import com.bookmyticket.transport.bus.SeatLayout;
import com.bookmyticket.transport.bus.SeatStatus;
import com.bookmyticket.transport.bus.SeatType;
import com.bookmyticket.transport.bus.TripDirection;
import com.bookmyticket.transport.bus.TripStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds the rolling demo bus inventory (trips and seat maps) for the sample
 * schedules. Every row is explicitly marked source = DEMO. Existing rows are
 * never modified, so bookings survive restarts.
 */
@Component
public class DemoInventorySeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoInventorySeeder.class);

    private final AppProperties properties;
    private final BusScheduleRepository scheduleRepository;
    private final BusTripRepository tripRepository;
    private final BusCorridorRepository corridorRepository;
    private final BusOperatorRepository operatorRepository;
    private final JdbcTemplate jdbcTemplate;

    public DemoInventorySeeder(AppProperties properties, BusScheduleRepository scheduleRepository,
                              BusTripRepository tripRepository, BusCorridorRepository corridorRepository,
                              BusOperatorRepository operatorRepository, JdbcTemplate jdbcTemplate) {
        this.properties = properties;
        this.scheduleRepository = scheduleRepository;
        this.tripRepository = tripRepository;
        this.corridorRepository = corridorRepository;
        this.operatorRepository = operatorRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.getDemo().isAutoSeedInventory()) {
            log.info("Demo bus inventory seeding is disabled.");
            return;
        }
        int daysAhead = Math.min(Math.max(properties.getDemo().getBusDaysAhead(), 1), 90);
        LocalDate today = LocalDate.now(TimeUtils.INDIA);
        LocalDate until = today.plusDays(daysAhead);

        List<BusSchedule> schedules = scheduleRepository.findAllActiveDetailed();
        if (schedules.isEmpty()) {
            log.warn("No active bus schedules found; demo inventory was not generated.");
            return;
        }
        Set<String> existing = tripRepository.findBetweenDates(today, until).stream()
                .map(trip -> key(trip.getSchedule().getId(), trip.getTravelDate(), trip.getDirection()))
                .collect(Collectors.toSet());

        int created = 0;
        for (BusSchedule schedule : schedules) {
            for (LocalDate date = today; !date.isAfter(until); date = date.plusDays(1)) {
                for (TripDirection direction : TripDirection.values()) {
                    if (existing.contains(key(schedule.getId(), date, direction))) {
                        continue;
                    }
                    createTrip(schedule, date, direction);
                    created++;
                }
            }
        }
        if (created > 0) {
            log.info("Demo bus inventory ready: {} new trip(s) across {} schedule(s), window {} to {}.",
                    created, schedules.size(), today, until);
        } else {
            log.info("Demo bus inventory already up to date for {} to {}.", today, until);
        }
    }

    /**
     * Lazily fills in sample inventory for a route/date that is not part of the
     * seeded network or falls outside the rolling startup window. Every row is
     * still explicitly marked as DEMO, so searches for any city pair return
     * clearly labelled sample coaches rather than an empty result.
     */
    @Transactional
    public int ensureRouteInventory(String fromCity, String toCity, LocalDate date) {
        String from = normalizeCity(fromCity);
        String to = normalizeCity(toCity);
        if (from.equalsIgnoreCase(to)) {
            return 0;
        }
        List<BusTrip> existing = tripRepository.search(from, to, date);
        if (!existing.isEmpty()) {
            return existing.size();
        }

        BusCorridor corridor = corridorRepository.findPair(from, to).stream().findFirst()
                .orElseGet(() -> createCorridor(from, to));
        List<BusSchedule> schedules = scheduleRepository.findActiveByCorridorId(corridor.getId());
        if (schedules.isEmpty()) {
            schedules = createSchedules(corridor, from, to);
        }
        if (schedules.isEmpty()) {
            log.warn("No demo bus schedules could be created for {} to {}.", from, to);
            return 0;
        }

        TripDirection direction = corridor.getCityA().equalsIgnoreCase(from)
                ? TripDirection.OUTBOUND : TripDirection.RETURN;
        for (BusSchedule schedule : schedules) {
            createTrip(schedule, date, direction);
        }
        log.info("Generated {} demo bus trip(s) for {} to {} on {}.",
                schedules.size(), from, to, date);
        return schedules.size();
    }

    private BusCorridor createCorridor(String from, String to) {
        int seed = Objects.hash(from.toLowerCase(Locale.ROOT), to.toLowerCase(Locale.ROOT));
        int distanceKm = 120 + Math.floorMod(seed, 780);
        int durationMinutes = Math.min(900, Math.max(150, Math.round(distanceKm * 60.0f / 55.0f)));
        BusCorridor corridor = new BusCorridor();
        corridor.setCityA(from);
        corridor.setCityB(to);
        corridor.setDistanceKm(distanceKm);
        corridor.setDurationMinutes(durationMinutes);
        return corridorRepository.save(corridor);
    }

    private List<BusSchedule> createSchedules(BusCorridor corridor, String from, String to) {
        List<BusOperator> operators = operatorRepository.findAll().stream()
                .sorted(Comparator.comparing(BusOperator::getId))
                .toList();
        if (operators.isEmpty()) {
            return List.of();
        }
        List<ScheduleTemplate> templates = List.of(
                new ScheduleTemplate(360, BusType.VOLVO_AC_SEMI_SLEEPER, true, SeatLayout.SEMI_SLEEPER_2X2,
                        "AC,WIFI,CHARGING_POINT,WATER_BOTTLE", 30),
                new ScheduleTemplate(780, BusType.AC_SLEEPER, false, SeatLayout.SLEEPER_2X1,
                        "AC,CHARGING_POINT,BLANKET", 10),
                new ScheduleTemplate(1020, BusType.SEMI_SLEEPER, false, SeatLayout.SEMI_SLEEPER_2X2,
                        "AC,CHARGING_POINT", 0),
                new ScheduleTemplate(1290, BusType.NON_AC_SEATER, false, SeatLayout.SEATER_2X2,
                        "CHARGING_POINT", -10));

        int seed = Objects.hash(corridor.getCityA(), corridor.getCityB());
        List<BusSchedule> schedules = new ArrayList<>();
        for (int index = 0; index < templates.size(); index++) {
            ScheduleTemplate template = templates.get(index);
            BusOperator operator = pickOperator(operators, template.premium(), seed + index * 31);
            BusSchedule schedule = new BusSchedule();
            schedule.setCorridor(corridor);
            schedule.setOperator(operator);
            schedule.setDepartMinutes(template.departMinutes());
            schedule.setDurationMinutes(corridor.getDurationMinutes() + template.durationAdjustment());
            schedule.setFareMinor(generatedFare(corridor.getDistanceKm(), template.premium()));
            schedule.setBusType(template.busType());
            schedule.setPremium(template.premium());
            schedule.setLayout(template.layout());
            schedule.setAmenities(template.amenities());
            schedule.setBoardingPoints(cityPoint(from, "Central") + "," + cityPoint(from, "City Centre"));
            schedule.setDroppingPoints(cityPoint(to, "Bus Stand") + "," + cityPoint(to, "City Centre"));
            schedule.setActive(true);
            schedules.add(scheduleRepository.save(schedule));
        }
        return schedules;
    }

    private BusOperator pickOperator(List<BusOperator> operators, boolean premium, int seed) {
        List<BusOperator> candidates = operators.stream()
                .filter(operator -> operator.isPremium() == premium)
                .toList();
        if (candidates.isEmpty()) {
            candidates = operators;
        }
        return candidates.get(Math.floorMod(seed, candidates.size()));
    }

    private long generatedFare(int distanceKm, boolean premium) {
        long fare = 20_000L + distanceKm * (premium ? 320L : 190L);
        return Math.max(15_000L, Math.round(fare / 100.0) * 100L);
    }

    private String cityPoint(String city, String suffix) {
        return city.replace(',', ' ') + " " + suffix;
    }

    private String normalizeCity(String raw) {
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
        return builder.length() == 0 ? "Unknown" : builder.toString();
    }

    private void createTrip(BusSchedule schedule, LocalDate date, TripDirection direction) {
        BusCorridor corridor = schedule.getCorridor();
        boolean outbound = direction == TripDirection.OUTBOUND;
        String from = outbound ? corridor.getCityA() : corridor.getCityB();
        String to = outbound ? corridor.getCityB() : corridor.getCityA();

        ZonedDateTime depart = date.atStartOfDay(TimeUtils.INDIA).plusMinutes(schedule.getDepartMinutes());
        ZonedDateTime arrive = depart.plusMinutes(schedule.getDurationMinutes());

        BusTrip trip = new BusTrip();
        trip.setSchedule(schedule);
        trip.setDirection(direction);
        trip.setTravelDate(date);
        trip.setDepartureCity(from);
        trip.setArrivalCity(to);
        trip.setDepartAt(depart.toInstant());
        trip.setArriveAt(arrive.toInstant());
        trip.setFareMinor(schedule.getFareMinor());
        trip.setCurrency("INR");
        trip.setBusType(schedule.getBusType());
        trip.setPremium(schedule.isPremium());
        trip.setLayout(schedule.getLayout());
        trip.setAmenities(schedule.getAmenities());
        trip.setBoardingPoints(schedule.getBoardingPoints());
        trip.setDroppingPoints(schedule.getDroppingPoints());
        trip.setTotalSeats(seatPlans(schedule.getLayout()).size());
        trip.setStatus(TripStatus.SCHEDULED);
        trip.setSource("DEMO");
        BusTrip saved = tripRepository.save(trip);

        insertSeats(saved, schedule, direction, date);
    }

    private void insertSeats(BusTrip trip, BusSchedule schedule, TripDirection direction, LocalDate date) {
        List<SeatPlan> plans = seatPlans(schedule.getLayout());
        int occupancyPercent = 25 + Math.floorMod(Objects.hash(schedule.getId(), date.toString(), direction.name()), 30);
        long baseFare = schedule.getFareMinor();
        List<Object[]> batch = new ArrayList<>(plans.size());
        for (SeatPlan plan : plans) {
            long price = baseFare;
            if (plan.window()) {
                price = price + Math.max(1000, Math.round(baseFare * 0.05 / 100.0) * 100);
            }
            if (plan.deck() == SeatDeck.UPPER) {
                price = Math.max(Math.round(baseFare * 0.6), price - 5000);
            }
            boolean occupied = Math.floorMod(Objects.hash(trip.getId(), plan.label()), 100) < occupancyPercent;
            batch.add(new Object[]{trip.getId(), plan.label(), plan.seatType().name(), plan.deck().name(),
                    plan.row(), plan.col(), price, occupied ? SeatStatus.BOOKED.name() : SeatStatus.AVAILABLE.name()});
        }
        jdbcTemplate.batchUpdate("""
                INSERT INTO bus_seats (trip_id, seat_number, seat_type, deck, row_no, col_no, price_minor, status, version)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)
                """, batch);
    }

    private List<SeatPlan> seatPlans(com.bookmyticket.transport.bus.SeatLayout layout) {
        List<SeatPlan> plans = new ArrayList<>();
        switch (layout) {
            case SEATER_2X2, SEMI_SLEEPER_2X2 -> {
                for (int row = 1; row <= layout.rowsPerDeck(); row++) {
                    for (int col = 1; col <= layout.seatsPerRow(); col++) {
                        plans.add(new SeatPlan(row + letter(col), layout.seatType(), SeatDeck.LOWER, row, col,
                                isWindow(col, layout)));
                    }
                }
            }
            case SLEEPER_2X1 -> {
                for (SeatDeck deck : List.of(SeatDeck.LOWER, SeatDeck.UPPER)) {
                    String prefix = deck == SeatDeck.LOWER ? "L" : "U";
                    for (int row = 1; row <= layout.rowsPerDeck(); row++) {
                        for (int col = 1; col <= layout.seatsPerRow(); col++) {
                            plans.add(new SeatPlan(prefix + row + letter(col), SeatType.SLEEPER, deck, row, col,
                                    isWindow(col, layout)));
                        }
                    }
                }
            }
        }
        return plans;
    }

    private boolean isWindow(int col, com.bookmyticket.transport.bus.SeatLayout layout) {
        return col == 1 || col == layout.seatsPerRow();
    }

    private String letter(int col) {
        return String.valueOf((char) ('A' + col - 1));
    }

    private String key(Long scheduleId, LocalDate date, TripDirection direction) {
        return scheduleId + "|" + date + "|" + direction.name();
    }

    private record SeatPlan(String label, SeatType seatType, SeatDeck deck, int row, int col, boolean window) {
    }

    private record ScheduleTemplate(int departMinutes, BusType busType, boolean premium, SeatLayout layout,
                                    String amenities, int durationAdjustment) {
    }
}

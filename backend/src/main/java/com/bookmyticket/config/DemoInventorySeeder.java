package com.bookmyticket.config;

import com.bookmyticket.common.TimeUtils;
import com.bookmyticket.transport.bus.BusCorridor;
import com.bookmyticket.transport.bus.BusSchedule;
import com.bookmyticket.transport.bus.BusScheduleRepository;
import com.bookmyticket.transport.bus.BusSeat;
import com.bookmyticket.transport.bus.BusTrip;
import com.bookmyticket.transport.bus.BusTripRepository;
import com.bookmyticket.transport.bus.SeatDeck;
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
import java.util.List;
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
    private final JdbcTemplate jdbcTemplate;

    public DemoInventorySeeder(AppProperties properties, BusScheduleRepository scheduleRepository,
                              BusTripRepository tripRepository, JdbcTemplate jdbcTemplate) {
        this.properties = properties;
        this.scheduleRepository = scheduleRepository;
        this.tripRepository = tripRepository;
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
}

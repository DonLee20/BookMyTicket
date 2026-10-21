package com.bookmyticket.provider;

import com.bookmyticket.config.AppProperties;
import com.bookmyticket.transport.flight.Airport;
import com.bookmyticket.transport.flight.AirportRepository;
import com.bookmyticket.transport.flight.CabinClass;
import com.bookmyticket.transport.flight.FlightSchedule;
import com.bookmyticket.transport.flight.FlightScheduleRepository;
import com.bookmyticket.transport.flight.dto.FlightOffer;
import com.bookmyticket.transport.flight.dto.FlightSearchQuery;
import com.bookmyticket.common.MoneyUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Demonstration flight distribution adapter. It produces deterministic sample
 * offers from the configured schedule dataset with day-of-week pricing, and
 * revalidates offers before booking. It never issues a ticket and never claims
 * a real airline confirmation.
 */
@Component
public class DemoFlightProvider implements FlightProvider {

    private static final DateTimeFormatter REF_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final FlightScheduleRepository scheduleRepository;
    private final AirportRepository airportRepository;
    private final AppProperties properties;

    public DemoFlightProvider(FlightScheduleRepository scheduleRepository, AirportRepository airportRepository,
                              AppProperties properties) {
        this.scheduleRepository = scheduleRepository;
        this.airportRepository = airportRepository;
        this.properties = properties;
    }

    @Override
    public String id() {
        return "demo-flight-distribution";
    }

    @Override
    public String displayName() {
        return "Demo flight offers (sample fares, no ticketing)";
    }

    @Override
    public String category() {
        return "FLIGHT";
    }

    @Override
    public ProviderMode mode() {
        return ProviderMode.from(properties.getProviders().getFlight().getMode(), ProviderMode.DEMO);
    }

    @Override
    public boolean bookingSupported() {
        return false;
    }

    @Override
    public List<String> capabilities() {
        return List.of("OFFER_SEARCH", "FARE_REVALIDATION", "DEMO_HOLD_REQUEST", "NO_TICKETING");
    }

    @Override
    public Optional<String> externalHandoffUrl() {
        return Optional.empty();
    }

    @Override
    public List<FlightOffer> search(FlightSearchQuery query) {
        List<Airport> origins = airportRepository.findByIataInIgnoreCase(List.of(query.originIata()));
        List<Airport> destinations = airportRepository.findByIataInIgnoreCase(List.of(query.destinationIata()));
        if (origins.isEmpty() || destinations.isEmpty()) {
            return List.of();
        }
        List<FlightSchedule> schedules = scheduleRepository.findBetweenAirports(
                origins.stream().map(Airport::getId).toList(),
                destinations.stream().map(Airport::getId).toList());
        List<FlightOffer> offers = schedules.stream()
                .filter(schedule -> query.nonStopOnly() ? schedule.getStops() == 0 : true)
                .map(schedule -> buildOffer(schedule, query.departureDate(), query.cabin(), null, query.totalTravellers()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        return sort(offers, query.sort());
    }

    @Override
    public Optional<FlightOffer> revalidate(String offerRef) {
        Optional<OfferParts> parts = parseOfferRef(offerRef);
        if (parts.isEmpty()) {
            return Optional.empty();
        }
        OfferParts p = parts.get();
        return scheduleRepository.findById(p.scheduleId())
                .flatMap(schedule -> buildOffer(schedule, p.date(), p.cabin(), p.priceMinor(), 1));
    }

    private List<FlightOffer> sort(List<FlightOffer> offers, String sort) {
        String key = sort == null ? "price" : sort;
        return switch (key) {
            case "price" -> offers.stream().sorted(Comparator.comparingLong(FlightOffer::pricePerTravellerMinor)).toList();
            case "duration" -> offers.stream().sorted(Comparator.comparingInt(FlightOffer::durationMinutes)).toList();
            case "departure" -> offers.stream().sorted(Comparator.comparing(FlightOffer::departLocal)).toList();
            case "arrival" -> offers.stream().sorted(Comparator.comparing(FlightOffer::arriveLocal)).toList();
            default -> offers;
        };
    }

    private Optional<FlightOffer> buildOffer(FlightSchedule schedule, LocalDate date, CabinClass cabin,
                                             Long revalidatedFromMinor, int travellers) {
        Long base = schedule.baseFareMinor(cabin);
        if (base == null) {
            return Optional.empty();
        }
        long price = variedPrice(base, schedule.getFlightNumber(), date, cabin);
        long previous = revalidatedFromMinor == null ? price : revalidatedFromMinor;
        boolean stale = previous != price;

        Airport origin = schedule.getOriginAirport();
        Airport destination = schedule.getDestinationAirport();
        ZoneId originZone = ZoneId.of(origin.getTimezone());
        ZoneId destinationZone = ZoneId.of(destination.getTimezone());

        ZonedDateTime localDepart = ZonedDateTime.of(date,
                LocalTime.of(schedule.getDepartMinutes() / 60, schedule.getDepartMinutes() % 60), originZone);
        ZonedDateTime localArrive = localDepart.toInstant().plusSeconds(schedule.getDurationMinutes() * 60L)
                .atZone(destinationZone);
        int dayOffset = localArrive.toLocalDate().compareTo(date);
        String arriveLabel = localArrive.format(DateTimeFormatter.ofPattern("HH:mm"))
                + (dayOffset > 0 ? " (+" + dayOffset + " day" + (dayOffset > 1 ? "s" : "") + ")" : "");

        int seatsLeft = 1 + (hash(schedule.getFlightNumber() + "|" + date + "|" + cabin) % 9);
        String conditions = "Demo fare rules: cabin baggage " + schedule.getBaggageNote()
                + ". " + (schedule.isRefundable() ? "Refundable per fare rules." : "Non-refundable in the sample fare.")
                + (schedule.isChangeable() ? " Date change permitted with fee." : " Date change not permitted.")
                + " Ticket issuance is not available in demo mode.";

        return Optional.of(new FlightOffer(
                offerRef(schedule.getId(), date, cabin, price),
                schedule.getId(),
                schedule.getAirlineCode(),
                schedule.getAirlineName(),
                schedule.getFlightNumber(),
                origin.getIata(),
                destination.getIata(),
                origin.getCity(),
                destination.getCity(),
                localDepart.format(DateTimeFormatter.ofPattern("HH:mm")),
                arriveLabel,
                origin.getTimezone(),
                destination.getTimezone(),
                date.toString(),
                schedule.getDurationMinutes(),
                schedule.getStops(),
                schedule.getAircraft(),
                cabin.name(),
                cabin.getLabel(),
                price * Math.max(1, travellers),
                price,
                "INR",
                MoneyUtils.format(price, "INR"),
                schedule.getBaggageNote(),
                schedule.isRefundable(),
                schedule.isChangeable(),
                conditions,
                seatsLeft,
                mode().name(),
                Instant.now().toString(),
                stale,
                null));
    }

    private long variedPrice(long base, String flightNumber, LocalDate date, CabinClass cabin) {
        int h = hash(flightNumber + "|" + date + "|" + cabin);
        long factorBps = 10000 + (h % 2401) - 1200; // ±12%
        switch (date.getDayOfWeek()) {
            case FRIDAY, SATURDAY, SUNDAY -> factorBps += 750;
            default -> {
            }
        }
        long price = base * factorBps / 10000;
        long rounded = Math.max(50000, Math.round(price / 100.0) * 100);
        return rounded;
    }

    private String offerRef(Long scheduleId, LocalDate date, CabinClass cabin, long priceMinor) {
        return "FL-" + scheduleId + "-" + date.format(REF_DATE) + "-" + cabin.name() + "-" + priceMinor;
    }

    private Optional<OfferParts> parseOfferRef(String offerRef) {
        if (offerRef == null || !offerRef.startsWith("FL-")) {
            return Optional.empty();
        }
        String[] parts = offerRef.split("-");
        if (parts.length != 5) {
            return Optional.empty();
        }
        try {
            return Optional.of(new OfferParts(Long.parseLong(parts[1]),
                    LocalDate.parse(parts[2], REF_DATE), CabinClass.valueOf(parts[3]), Long.parseLong(parts[4])));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    private int hash(String value) {
        return Math.floorMod(value.hashCode(), 100000);
    }

    private record OfferParts(Long scheduleId, LocalDate date, CabinClass cabin, long priceMinor) {
    }

    @Override
    public String statusMessage() {
        return "Sample flight offers with revalidation. No ticket, airline confirmation or seat allocation is created.";
    }
}

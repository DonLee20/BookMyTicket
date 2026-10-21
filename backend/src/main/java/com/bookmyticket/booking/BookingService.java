package com.bookmyticket.booking;

import com.bookmyticket.booking.dto.BookingDtos;
import com.bookmyticket.catalog.HolidayPackage;
import com.bookmyticket.catalog.HolidayPackageRepository;
import com.bookmyticket.catalog.PackageDeparture;
import com.bookmyticket.catalog.PackageDepartureRepository;
import com.bookmyticket.common.ApiException;
import com.bookmyticket.common.MoneyUtils;
import com.bookmyticket.common.PageResponse;
import com.bookmyticket.common.TimeUtils;
import com.bookmyticket.config.AppProperties;
import com.bookmyticket.identity.User;
import com.bookmyticket.identity.UserRepository;
import com.bookmyticket.payment.Payment;
import com.bookmyticket.payment.PaymentRepository;
import com.bookmyticket.payment.PaymentService;
import com.bookmyticket.provider.BusProvider;
import com.bookmyticket.provider.ProviderMode;
import com.bookmyticket.provider.ProviderRegistry;
import com.bookmyticket.provider.RailwayProvider;
import com.bookmyticket.security.SecurityUtils;
import com.bookmyticket.transport.bus.BusTrip;
import com.bookmyticket.transport.bus.BusSeat;
import com.bookmyticket.transport.bus.BusSeatRepository;
import com.bookmyticket.transport.bus.BusTripRepository;
import com.bookmyticket.transport.bus.SeatStatus;
import com.bookmyticket.transport.bus.TripStatus;
import com.bookmyticket.transport.flight.FlightSchedule;
import com.bookmyticket.transport.flight.FlightScheduleRepository;
import com.bookmyticket.transport.flight.FlightSearchService;
import com.bookmyticket.transport.flight.dto.FlightOffer;
import com.bookmyticket.transport.train.TrainService;
import com.bookmyticket.transport.train.TrainServiceClass;
import com.bookmyticket.transport.train.TrainServiceClassRepository;
import com.bookmyticket.transport.train.TrainServiceRepository;
import com.bookmyticket.transport.train.TravelClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final DateTimeFormatter ID_DATE = DateTimeFormatter.ofPattern("yyMMdd");
    private static final int MAX_PAGE_SIZE = 50;

    private final BookingRepository bookingRepository;
    private final BookingPassengerRepository passengerRepository;
    private final BusTripRepository busTripRepository;
    private final BusSeatRepository busSeatRepository;
    private final BusBookingDetailRepository busDetailRepository;
    private final TrainServiceRepository trainServiceRepository;
    private final TrainServiceClassRepository trainClassRepository;
    private final TrainBookingDetailRepository trainDetailRepository;
    private final FlightScheduleRepository flightScheduleRepository;
    private final FlightBookingDetailRepository flightDetailRepository;
    private final HolidayPackageRepository holidayPackageRepository;
    private final PackageDepartureRepository departureRepository;
    private final HolidayBookingDetailRepository holidayDetailRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final BookingLifecycleService lifecycleService;
    private final FlightSearchService flightSearchService;
    private final ProviderRegistry providerRegistry;
    private final AppProperties properties;

    public BookingService(BookingRepository bookingRepository, BookingPassengerRepository passengerRepository,
                          BusTripRepository busTripRepository, BusSeatRepository busSeatRepository,
                          BusBookingDetailRepository busDetailRepository,
                          TrainServiceRepository trainServiceRepository,
                          TrainServiceClassRepository trainClassRepository,
                          TrainBookingDetailRepository trainDetailRepository,
                          FlightScheduleRepository flightScheduleRepository,
                          FlightBookingDetailRepository flightDetailRepository,
                          HolidayPackageRepository holidayPackageRepository,
                          PackageDepartureRepository departureRepository,
                          HolidayBookingDetailRepository holidayDetailRepository,
                          UserRepository userRepository, PaymentRepository paymentRepository,
                          PaymentService paymentService, BookingLifecycleService lifecycleService,
                          FlightSearchService flightSearchService, ProviderRegistry providerRegistry,
                          AppProperties properties) {
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.busTripRepository = busTripRepository;
        this.busSeatRepository = busSeatRepository;
        this.busDetailRepository = busDetailRepository;
        this.trainServiceRepository = trainServiceRepository;
        this.trainClassRepository = trainClassRepository;
        this.trainDetailRepository = trainDetailRepository;
        this.flightScheduleRepository = flightScheduleRepository;
        this.flightDetailRepository = flightDetailRepository;
        this.holidayPackageRepository = holidayPackageRepository;
        this.departureRepository = departureRepository;
        this.holidayDetailRepository = holidayDetailRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
        this.lifecycleService = lifecycleService;
        this.flightSearchService = flightSearchService;
        this.providerRegistry = providerRegistry;
        this.properties = properties;
    }

    // ------------------------------------------------------------------
    // Creation
    // ------------------------------------------------------------------

    @Transactional
    public BookingDtos.BookingResponse createBus(BookingDtos.BusBookingRequest request) {
        Long userId = SecurityUtils.requireUserId();
        Booking existing = findIdempotent(request.idempotencyKey(), userId);
        if (existing != null) {
            return toResponse(existing);
        }
        providerRegistry.ensureAvailable(BusProvider.class);

        BusTrip trip = busTripRepository.findDetailedById(request.tripId())
                .orElseThrow(() -> ApiException.notFound("TRIP_NOT_FOUND", "That bus service could not be found."));
        if (trip.getStatus() != TripStatus.SCHEDULED) {
            throw ApiException.conflict("TRIP_NOT_BOOKABLE", "This bus service is not open for booking.");
        }
        if (trip.getDepartAt().isBefore(Instant.now())) {
            throw ApiException.conflict("TRIP_DEPARTED", "This bus has already departed. Please search again.");
        }
        if (request.seatIds().size() != request.passengers().size()) {
            throw ApiException.badRequest("PASSENGER_SEAT_MISMATCH",
                    "Select exactly one seat for each traveller.");
        }
        Set<Long> seatIds = new LinkedHashSet<>(request.seatIds());
        if (seatIds.size() != request.seatIds().size()) {
            throw ApiException.badRequest("DUPLICATE_SEAT", "The same seat was selected more than once.");
        }

        List<BusSeat> seats = busSeatRepository.lockAllById(seatIds);
        if (seats.size() != seatIds.size()) {
            throw ApiException.conflict("SEAT_NOT_FOUND", "One or more selected seats could not be found.");
        }
        Map<Long, BusSeat> seatById = seats.stream().collect(Collectors.toMap(BusSeat::getId, Function.identity()));
        long total = 0;
        List<String> seatNumbers = new ArrayList<>();
        for (Long seatId : request.seatIds()) {
            BusSeat seat = seatById.get(seatId);
            if (!seat.getTrip().getId().equals(trip.getId())) {
                throw ApiException.badRequest("SEAT_TRIP_MISMATCH", "A selected seat does not belong to this bus.");
            }
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw ApiException.conflict("SEAT_UNAVAILABLE",
                        "Seat " + seat.getSeatNumber() + " is no longer available. Please choose another seat.");
            }
            total += seat.getPriceMinor();
            seatNumbers.add(seat.getSeatNumber());
        }
        if (request.expectedTotalMinor() != null && request.expectedTotalMinor() != total) {
            throw ApiException.conflict("PRICE_CHANGED",
                    "The fare changed while you were booking. Please review the updated total and try again.");
        }

        String boarding = validatePoint(request.boardingPoint(), trip.getBoardingPoints(), "boarding");
        String dropping = validatePoint(request.droppingPoint(), trip.getDroppingPoints(), "dropping");

        BookingCategory category = trip.isPremium() ? BookingCategory.PREMIUM_BUS : BookingCategory.BUS;
        Booking booking = newBooking(userId, category, PaymentStatus.PENDING, total, trip.getCurrency(),
                request.idempotencyKey(), request.contactName(), request.contactEmail(), request.contactPhone(),
                request.passengers().size());
        booking.setProviderMode(providerRegistry.effectiveModeFor(BusProvider.class));
        booking.setProviderReference("DEMO-BUS-" + trip.getId() + "-" + String.join("/", seatNumbers));
        booking.setNotes("Demo booking created against sample inventory. No real bus operator reservation exists.");
        bookingRepository.save(booking);

        for (BusSeat seat : seats) {
            seat.setStatus(SeatStatus.HELD);
        }
        busSeatRepository.saveAll(seats);

        BusBookingDetail detail = new BusBookingDetail();
        detail.setBookingId(booking.getId());
        detail.setTrip(trip);
        detail.setOperatorName(trip.getSchedule().getOperator().getName());
        detail.setBusType(trip.getBusType().name());
        detail.setTravelDate(trip.getTravelDate());
        detail.setDepartureCity(trip.getDepartureCity());
        detail.setArrivalCity(trip.getArrivalCity());
        detail.setDepartAt(trip.getDepartAt());
        detail.setArriveAt(trip.getArriveAt());
        detail.setBoardingPoint(boarding);
        detail.setDroppingPoint(dropping);
        detail.setSeatNumbers(String.join(",", seatNumbers));
        busDetailRepository.save(detail);

        savePassengers(booking, request.passengers(),
                request.seatIds().stream().map(seatById::get).toList());
        paymentService.createIntent(booking);
        return toResponse(booking);
    }

    @Transactional
    public BookingDtos.BookingResponse createTrain(BookingDtos.TrainBookingRequest request) {
        Long userId = SecurityUtils.requireUserId();
        Booking existing = findIdempotent(request.idempotencyKey(), userId);
        if (existing != null) {
            return toResponse(existing);
        }
        providerRegistry.ensureAvailable(RailwayProvider.class);
        if (request.journeyDate().isBefore(LocalDate.now())) {
            throw ApiException.badRequest("PAST_DATE", "Please choose today or a future travel date.");
        }
        TrainService service = trainServiceRepository.findDetailedById(request.serviceId())
                .orElseThrow(() -> ApiException.notFound("TRAIN_NOT_FOUND", "That train service could not be found."));
        if (!service.isActive()) {
            throw ApiException.conflict("TRAIN_NOT_ACTIVE", "This train service is not currently listed.");
        }
        if (!service.runsOn(request.journeyDate().getDayOfWeek())) {
            throw ApiException.conflict("TRAIN_NOT_RUNNING",
                    "This sample train does not run on " + request.journeyDate().getDayOfWeek() + ". Choose another date.");
        }
        TravelClass travelClass = parseTravelClass(request.travelClass());
        TrainServiceClass serviceClass = trainClassRepository.findByServiceIdOrderByFareMinorAsc(service.getId()).stream()
                .filter(clazz -> clazz.getTravelClass() == travelClass)
                .findFirst()
                .orElseThrow(() -> ApiException.badRequest("CLASS_NOT_FOUND",
                        "Class " + request.travelClass() + " is not offered on this train."));
        if (serviceClass.getFareMinor() == null) {
            throw ApiException.conflict("FARE_UNAVAILABLE", "No sample fare is published for this class.");
        }
        long total = serviceClass.getFareMinor() * request.passengers().size();

        String quota = request.quota() == null || request.quota().isBlank()
                ? "GENERAL" : request.quota().trim().toUpperCase();

        Booking booking = newBooking(userId, BookingCategory.TRAIN, PaymentStatus.NOT_REQUIRED, total, "INR",
                request.idempotencyKey(), request.contactName(), request.contactEmail(), request.contactPhone(),
                request.passengers().size());
        booking.setProviderMode(providerRegistry.effectiveModeFor(RailwayProvider.class));
        booking.setProviderReference("IRCTC-HANDOFF");
        booking.setHoldExpiresAt(null);
        booking.setNotes("Reservation request only. No PNR, seat allocation or ticket is created here. "
                + "Complete the booking on the official IRCTC channel.");
        bookingRepository.save(booking);

        TrainBookingDetail detail = new TrainBookingDetail();
        detail.setBookingId(booking.getId());
        detail.setService(service);
        detail.setTravelClass(travelClass.name());
        detail.setQuota(quota);
        detail.setJourneyDate(request.journeyDate());
        detail.setFromStation(request.fromStation().trim());
        detail.setToStation(request.toStation().trim());
        detail.setTrainNumber(service.getTrainNumber());
        detail.setTrainName(service.getName());
        detail.setOfficialHandoffUrl(properties.getUrls().getIrctcOfficial());
        trainDetailRepository.save(detail);

        savePassengers(booking, request.passengers(), null);
        return toResponse(booking);
    }

    @Transactional
    public BookingDtos.BookingResponse createFlight(BookingDtos.FlightBookingRequest request) {
        Long userId = SecurityUtils.requireUserId();
        Booking existing = findIdempotent(request.idempotencyKey(), userId);
        if (existing != null) {
            return toResponse(existing);
        }
        FlightOffer offer = flightSearchService.revalidate(request.offerRef());
        if (offer.stale() && !request.acceptPriceChange()) {
            long oldPrice = parseOfferPrice(request.offerRef());
            throw ApiException.conflict("OFFER_PRICE_CHANGED",
                    "The demo fare changed from " + MoneyUtils.format(oldPrice, offer.currency()) + " to "
                            + offer.priceDisplay() + " per traveller since you searched. "
                            + "Review the new fare and confirm again.");
        }
        FlightSchedule schedule = flightScheduleRepository.findById(offer.scheduleId())
                .orElseThrow(() -> ApiException.notFound("FLIGHT_NOT_FOUND", "That flight could not be found."));
        if (LocalDate.parse(offer.journeyDate()).isBefore(LocalDate.now())) {
            throw ApiException.badRequest("PAST_DATE", "Flight dates cannot be in the past.");
        }
        int travellers = request.passengers().size();
        long total = offer.pricePerTravellerMinor() * travellers;

        Booking booking = newBooking(userId, BookingCategory.FLIGHT, PaymentStatus.NOT_REQUIRED, total, offer.currency(),
                request.idempotencyKey(), request.contactName(), request.contactEmail(), request.contactPhone(),
                travellers);
        booking.setProviderMode(providerRegistry.effectiveModeFor(com.bookmyticket.provider.FlightProvider.class));
        booking.setProviderReference(offer.offerRef());
        booking.setHoldExpiresAt(null);
        booking.setNotes("Demo flight hold request. No ticket, airline confirmation or seat allocation is created.");
        bookingRepository.save(booking);

        FlightBookingDetail detail = new FlightBookingDetail();
        detail.setBookingId(booking.getId());
        detail.setSchedule(schedule);
        detail.setOfferRef(offer.offerRef());
        detail.setCabin(offer.cabin());
        detail.setJourneyDate(LocalDate.parse(offer.journeyDate()));
        detail.setOriginIata(offer.originIata());
        detail.setDestinationIata(offer.destinationIata());
        detail.setAirlineName(offer.airlineName());
        detail.setFlightNumber(offer.flightNumber());
        detail.setDepartLocal(offer.departLocal());
        detail.setArriveLocal(offer.arriveLocal());
        detail.setStops(offer.stops());
        detail.setBaggageNote(offer.baggageNote());
        detail.setRefundable(offer.refundable());
        detail.setChangeable(offer.changeable());
        detail.setProviderConditions(offer.fareConditions());
        flightDetailRepository.save(detail);

        savePassengers(booking, request.passengers(), null);
        return toResponse(booking);
    }

    @Transactional
    public BookingDtos.BookingResponse createHoliday(BookingDtos.HolidayBookingRequest request) {
        Long userId = SecurityUtils.requireUserId();
        Booking existing = findIdempotent(request.idempotencyKey(), userId);
        if (existing != null) {
            return toResponse(existing);
        }
        HolidayPackage holidayPackage = holidayPackageRepository.findDetailedBySlug(request.packageSlug())
                .orElseThrow(() -> ApiException.notFound("PACKAGE_NOT_FOUND", "That holiday package could not be found."));
        LocalDate startDate = request.startDate();
        if (!startDate.isAfter(LocalDate.now())) {
            throw ApiException.badRequest("INVALID_START_DATE", "Choose a departure date from tomorrow onwards.");
        }
        int capacity = properties.getBooking().getHolidayCapacityPerDeparture();
        departureRepository.ensureDeparture(holidayPackage.getId(), startDate, capacity);
        PackageDeparture departure = departureRepository
                .lockByPackageAndDate(holidayPackage.getId(), startDate)
                .orElseThrow(() -> ApiException.conflict("PACKAGE_DEPARTURE_MISSING",
                        "This departure is not open for booking yet."));
        if (!"OPEN".equalsIgnoreCase(departure.getStatus())) {
            throw ApiException.conflict("PACKAGE_CLOSED", "This departure is closed for new bookings.");
        }
        if (departure.seatsRemaining() < request.travellers()) {
            throw ApiException.conflict("PACKAGE_SOLD_OUT",
                    "Only " + departure.seatsRemaining() + " place(s) remain on this departure.");
        }
        long pricePerTraveller = departure.getPriceOverrideMinor() != null
                ? departure.getPriceOverrideMinor() : holidayPackage.getBasePriceMinor();
        long total = pricePerTraveller * request.travellers();

        Booking booking = newBooking(userId, BookingCategory.HOLIDAY, PaymentStatus.PENDING, total,
                holidayPackage.getCurrency(), request.idempotencyKey(), request.contactName(), request.contactEmail(),
                request.contactPhone(), request.travellers());
        booking.setProviderMode(ProviderMode.DEMO);
        booking.setProviderReference("DEMO-HOLIDAY-" + holidayPackage.getSlug());
        booking.setNotes(request.notes() == null || request.notes().isBlank()
                ? "Demo holiday booking against sample inventory. No supplier reservation is created."
                : trimTo(request.notes(), 500));
        bookingRepository.save(booking);

        HolidayBookingDetail detail = new HolidayBookingDetail();
        detail.setBookingId(booking.getId());
        detail.setHolidayPackage(holidayPackage);
        detail.setDeparture(departure);
        detail.setPackageTitle(holidayPackage.getTitle());
        detail.setStartDate(startDate);
        detail.setEndDate(startDate.plusDays(Math.max(1, holidayPackage.getDurationDays()) - 1L));
        detail.setTravellers(request.travellers());
        detail.setRooms(request.rooms());
        detail.setLandOnly(holidayPackage.isLandOnly());
        holidayDetailRepository.save(detail);

        paymentService.createIntent(booking);
        return toResponse(booking);
    }

    // ------------------------------------------------------------------
    // Cancellation and holds
    // ------------------------------------------------------------------

    @Transactional
    public BookingDtos.BookingResponse cancel(String bookingId, String reason) {
        Long userId = SecurityUtils.requireUserId();
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> ApiException.notFound("BOOKING_NOT_FOUND", "That booking could not be found."));
        return cancelInternal(booking, reason, false);
    }

    @Transactional
    public BookingDtos.BookingResponse adminCancel(String bookingId, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> ApiException.notFound("BOOKING_NOT_FOUND", "That booking could not be found."));
        return cancelInternal(booking, reason, true);
    }

    private BookingDtos.BookingResponse cancelInternal(Booking booking, String reason, boolean byAdmin) {
        if (!booking.getStatus().isCancellable()) {
            throw ApiException.conflict("BOOKING_NOT_CANCELLABLE",
                    "A booking in status " + booking.getStatus().name().toLowerCase() + " cannot be cancelled.");
        }
        String effectiveReason = reason == null || reason.isBlank()
                ? (byAdmin ? "Cancelled by an administrator." : "Cancelled by the customer.")
                : trimTo(reason.trim(), 300);
        long refund = refundAmount(booking);
        lifecycleService.releaseOnCancellation(booking);
        if (refund > 0) {
            paymentService.refundIfPaid(booking, refund,
                    "Cancellation refund for booking " + booking.getId() + ".");
            booking.setPaymentStatus(PaymentStatus.REFUNDED);
        }
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(Instant.now());
        booking.setCancellationReason(effectiveReason);
        booking.setRefundAmountMinor(refund);
        booking.setHoldExpiresAt(null);
        bookingRepository.save(booking);
        return toResponse(booking);
    }

    /** Releases expired seat holds every minute. Safe to run repeatedly. */
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    @Transactional
    public void expireStaleHolds() {
        List<Booking> stale = bookingRepository.findByStatusAndHoldExpiresAtBefore(
                BookingStatus.PAYMENT_PENDING, Instant.now());
        for (Booking booking : stale) {
            lifecycleService.expire(booking, "The payment hold expired automatically.");
            paymentService.failPendingForBooking(booking, "Payment hold expired before completion.");
        }
        if (!stale.isEmpty()) {
            org.slf4j.LoggerFactory.getLogger(BookingService.class)
                    .info("Expired {} stale booking hold(s)", stale.size());
        }
    }

    // ------------------------------------------------------------------
    // Reads
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PageResponse<BookingDtos.BookingResponse> list(String category, String status, int page, int size) {
        Long userId = SecurityUtils.requireUserId();
        Pageable pageable = PageRequest.of(Math.max(0, page), clamp(size),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Booking> result = bookingRepository.userSearch(userId, parseCategory(category), parseStatus(status),
                pageable);
        return PageResponse.from(result, this::toResponse);
    }

    @Transactional(readOnly = true)
    public BookingDtos.BookingResponse get(String bookingId) {
        Long userId = SecurityUtils.requireUserId();
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> ApiException.notFound("BOOKING_NOT_FOUND", "That booking could not be found."));
        return toResponse(booking);
    }

    @Transactional(readOnly = true)
    public BookingDtos.BookingResponse toResponse(Booking booking) {
        List<BookingPassenger> passengers = passengerRepository.findByBookingIdOrderBySeq(booking.getId());
        List<BookingDtos.PassengerView> passengerViews = passengers.stream()
                .map(p -> new BookingDtos.PassengerView(p.getSeq(), p.getFullName(), p.getAge(),
                        p.getGender().name(), p.getPassengerType().name(), p.getSeatNumber()))
                .toList();

        BookingDtos.BookingDetails details = switch (booking.getCategory()) {
            case BUS, PREMIUM_BUS -> new BookingDtos.BookingDetails(busDetails(booking.getId()), null, null, null);
            case TRAIN -> new BookingDtos.BookingDetails(null, trainDetails(booking.getId()), null, null);
            case FLIGHT -> new BookingDtos.BookingDetails(null, null, flightDetails(booking.getId()), null);
            case HOLIDAY, MTC -> new BookingDtos.BookingDetails(null, null, null, holidayDetails(booking.getId()));
        };

        Payment payment = paymentRepository.findByBookingIdOrderByCreatedAtDesc(booking.getId()).stream()
                .findFirst().orElse(null);

        String handoffUrl = details.train() == null ? null : details.train().officialHandoffUrl();
        Long holdSeconds = booking.getHoldExpiresAt() == null ? null
                : Math.max(0, Duration.between(Instant.now(), booking.getHoldExpiresAt()).toSeconds());

        return new BookingDtos.BookingResponse(
                booking.getId(),
                booking.getCategory().name(),
                booking.getCategory().getLabel(),
                booking.getStatus().name(),
                statusLabel(booking.getStatus()),
                booking.getPaymentStatus().name(),
                booking.getCurrency(),
                booking.getTotalAmountMinor(),
                MoneyUtils.format(booking.getTotalAmountMinor(), booking.getCurrency()),
                booking.getProviderMode().name(),
                booking.getProviderReference(),
                booking.getContactName(),
                booking.getContactEmail(),
                booking.getContactPhone(),
                booking.getTravellerCount(),
                booking.getNotes(),
                iso(booking.getCreatedAt()),
                iso(booking.getUpdatedAt()),
                iso(booking.getHoldExpiresAt()),
                holdSeconds,
                iso(booking.getCancelledAt()),
                booking.getCancellationReason(),
                booking.getRefundAmountMinor(),
                booking.getRefundAmountMinor() == null ? null
                        : MoneyUtils.format(booking.getRefundAmountMinor(), booking.getCurrency()),
                paymentService.summarize(payment),
                details,
                passengerViews,
                nextAction(booking),
                message(booking),
                handoffUrl);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private BookingDtos.BusDetails busDetails(String bookingId) {
        return busDetailRepository.findByBookingId(bookingId).map(detail -> new BookingDtos.BusDetails(
                detail.getTrip().getId(),
                detail.getOperatorName(),
                detail.getBusType(),
                detail.getTrip().isPremium(),
                detail.getTravelDate().toString(),
                detail.getDepartureCity(),
                detail.getArrivalCity(),
                detail.getDepartAt().toString(),
                detail.getArriveAt().toString(),
                detail.getBoardingPoint(),
                detail.getDroppingPoint(),
                List.of(detail.getSeatNumbers().split(","))
        )).orElse(null);
    }

    private BookingDtos.TrainDetails trainDetails(String bookingId) {
        return trainDetailRepository.findByBookingId(bookingId).map(detail -> new BookingDtos.TrainDetails(
                detail.getService().getId(),
                detail.getTrainNumber(),
                detail.getTrainName(),
                detail.getTravelClass(),
                detail.getQuota(),
                detail.getJourneyDate().toString(),
                detail.getFromStation(),
                detail.getToStation(),
                detail.getOfficialHandoffUrl()
        )).orElse(null);
    }

    private BookingDtos.FlightDetails flightDetails(String bookingId) {
        return flightDetailRepository.findByBookingId(bookingId).map(detail -> new BookingDtos.FlightDetails(
                detail.getSchedule().getId(),
                detail.getOfferRef(),
                detail.getAirlineName(),
                detail.getFlightNumber(),
                detail.getCabin(),
                detail.getJourneyDate().toString(),
                detail.getOriginIata(),
                detail.getDestinationIata(),
                detail.getDepartLocal(),
                detail.getArriveLocal(),
                detail.getStops(),
                detail.getBaggageNote(),
                detail.isRefundable(),
                detail.isChangeable(),
                detail.getProviderConditions()
        )).orElse(null);
    }

    private BookingDtos.HolidayDetails holidayDetails(String bookingId) {
        return holidayDetailRepository.findByBookingId(bookingId).map(detail -> new BookingDtos.HolidayDetails(
                detail.getHolidayPackage().getId(),
                detail.getHolidayPackage().getSlug(),
                detail.getPackageTitle(),
                detail.getHolidayPackage().getDestination().getName(),
                detail.getHolidayPackage().getImageUrl(),
                detail.getStartDate().toString(),
                detail.getEndDate().toString(),
                detail.getTravellers(),
                detail.getRooms(),
                detail.isLandOnly()
        )).orElse(null);
    }

    private Booking newBooking(Long userId, BookingCategory category, PaymentStatus paymentStatus, long total,
                               String currency, String idempotencyKey, String contactName, String contactEmail,
                               String contactPhone, int travellers) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("Please sign in to continue."));
        Booking booking = new Booking();
        booking.setId(generateBookingId(category));
        booking.setUser(user);
        booking.setCategory(category);
        booking.setStatus(paymentStatus == PaymentStatus.NOT_REQUIRED ? BookingStatus.PENDING
                : BookingStatus.PAYMENT_PENDING);
        booking.setPaymentStatus(paymentStatus);
        booking.setCurrency(currency);
        booking.setTotalAmountMinor(total);
        booking.setProviderMode(ProviderMode.DEMO);
        booking.setIdempotencyKey(idempotencyKey == null || idempotencyKey.isBlank() ? null
                : idempotencyKey.trim());
        booking.setContactName(contactName.trim());
        booking.setContactEmail(contactEmail.trim().toLowerCase());
        booking.setContactPhone(contactPhone == null || contactPhone.isBlank() ? null : contactPhone.trim());
        booking.setTravellerCount(travellers);
        if (paymentStatus == PaymentStatus.PENDING) {
            booking.setHoldExpiresAt(Instant.now().plus(Duration.ofMinutes(properties.getBooking().getHoldMinutes())));
        }
        return booking;
    }

    private void savePassengers(Booking booking, List<BookingDtos.PassengerInput> inputs, List<BusSeat> seats) {
        if (inputs == null || inputs.isEmpty()) {
            return;
        }
        int seq = 1;
        for (int i = 0; i < inputs.size(); i++) {
            BookingDtos.PassengerInput input = inputs.get(i);
            BookingPassenger passenger = new BookingPassenger();
            passenger.setBooking(booking);
            passenger.setSeq(seq++);
            passenger.setFullName(input.fullName().trim());
            passenger.setAge(input.age());
            passenger.setGender(input.gender());
            passenger.setPassengerType(input.passengerType() == null ? PassengerType.ADULT : input.passengerType());
            if (seats != null && i < seats.size()) {
                BusSeat seat = seats.get(i);
                passenger.setSeatId(seat.getId());
                passenger.setSeatNumber(seat.getSeatNumber());
            }
            passengerRepository.save(passenger);
        }
    }

    private Booking findIdempotent(String idempotencyKey, Long userId) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }
        Optional<Booking> existing = bookingRepository.findByIdempotencyKey(idempotencyKey.trim());
        if (existing.isEmpty()) {
            return null;
        }
        Booking booking = existing.get();
        if (!booking.getUser().getId().equals(userId)) {
            throw ApiException.conflict("IDEMPOTENCY_KEY_REUSED",
                    "This idempotency key was already used for a different account.");
        }
        return booking;
    }

    private String validatePoint(String requested, String csvPoints, String kind) {
        List<String> points = csvPoints == null || csvPoints.isBlank() ? List.of()
                : List.of(csvPoints.split(",")).stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
        String match = points.stream().filter(p -> p.equalsIgnoreCase(requested.trim())).findFirst()
                .orElseThrow(() -> ApiException.badRequest("INVALID_" + kind.toUpperCase() + "_POINT",
                        "Choose one of the listed " + kind + " points: " + String.join(", ", points)));
        return match;
    }

    private long refundAmount(Booking booking) {
        if (booking.getPaymentStatus() != PaymentStatus.PAID) {
            return 0;
        }
        int percent;
        if (booking.getCategory() == BookingCategory.HOLIDAY) {
            LocalDate start = holidayDetailRepository.findByBookingId(booking.getId())
                    .map(HolidayBookingDetail::getStartDate).orElse(null);
            if (start == null) {
                return 0;
            }
            long days = Duration.between(LocalDate.now().atStartOfDay(), start.atStartOfDay()).toDays();
            percent = days >= 15 ? 100 : days >= 7 ? 50 : 0;
        } else {
            Instant departAt = busDetailRepository.findByBookingId(booking.getId())
                    .map(BusBookingDetail::getDepartAt).orElse(null);
            if (departAt == null) {
                return 0;
            }
            long hours = Duration.between(Instant.now(), departAt).toHours();
            percent = hours >= 24 ? 90 : hours >= 6 ? 50 : 0;
        }
        return booking.getTotalAmountMinor() * percent / 100;
    }

    private String nextAction(Booking booking) {
        return switch (booking.getStatus()) {
            case PAYMENT_PENDING -> "COMPLETE_DEMO_PAYMENT";
            case PENDING -> switch (booking.getCategory()) {
                case TRAIN -> "EXTERNAL_HANDOFF";
                case FLIGHT -> "DEMO_HOLD_ONLY";
                default -> "NONE";
            };
            default -> "NONE";
        };
    }

    private String message(Booking booking) {
        return switch (booking.getStatus()) {
            case PAYMENT_PENDING -> "Inventory is held until the demo payment is completed. "
                    + "No real operator reservation exists and no money will be charged.";
            case PENDING -> switch (booking.getCategory()) {
                case TRAIN -> "Reservation request recorded. This is not an IRCTC ticket. "
                        + "Complete the booking on the official IRCTC channel.";
                case FLIGHT -> "Demo flight hold request recorded. No ticket is issued and no airline confirmation exists.";
                default -> "Request recorded.";
            };
            case CONFIRMED -> "Demo booking confirmed against sample inventory. "
                    + "This simulated confirmation is not valid for travel.";
            case TICKETED -> "A provider ticket reference is attached to this booking.";
            case FAILED -> "The payment failed and held inventory was released. You can create a new booking.";
            case EXPIRED -> "The payment hold expired and the inventory was released. Please book again.";
            case CANCELLED -> booking.getRefundAmountMinor() != null && booking.getRefundAmountMinor() > 0
                    ? "Booking cancelled. A simulated refund of "
                            + MoneyUtils.format(booking.getRefundAmountMinor(), booking.getCurrency())
                            + " was recorded."
                    : "Booking cancelled. No refund is due under the applicable policy.";
            case REFUND_PENDING -> "Cancellation recorded; the refund is being processed.";
            case REFUNDED -> "Booking cancelled and the refund has been recorded.";
            case DRAFT -> "Draft booking.";
        };
    }

    private String statusLabel(BookingStatus status) {
        return switch (status) {
            case DRAFT -> "Draft";
            case PENDING -> "Request pending";
            case PAYMENT_PENDING -> "Awaiting payment";
            case CONFIRMED -> "Confirmed (demo)";
            case TICKETED -> "Ticketed";
            case FAILED -> "Failed";
            case CANCELLED -> "Cancelled";
            case REFUND_PENDING -> "Refund pending";
            case REFUNDED -> "Refunded";
            case EXPIRED -> "Expired";
        };
    }

    private BookingCategory parseCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        try {
            return BookingCategory.valueOf(category.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("INVALID_CATEGORY", "Unknown booking category: " + category);
        }
    }

    private BookingStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return BookingStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("INVALID_STATUS", "Unknown booking status: " + status);
        }
    }

    private TravelClass parseTravelClass(String value) {
        try {
            return TravelClass.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("INVALID_CLASS", "Unknown travel class: " + value);
        }
    }

    private long parseOfferPrice(String offerRef) {
        try {
            String[] parts = offerRef.split("-");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (RuntimeException ex) {
            return 0;
        }
    }

    private String generateBookingId(BookingCategory category) {
        String prefix = switch (category) {
            case BUS -> "BUS";
            case PREMIUM_BUS -> "PRM";
            case TRAIN -> "TRN";
            case FLIGHT -> "FLT";
            case MTC -> "MTC";
            case HOLIDAY -> "HOL";
        };
        String date = LocalDate.now(TimeUtils.INDIA).format(ID_DATE);
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "BMT-" + prefix + "-" + date + "-" + suffix;
    }

    private String iso(Instant instant) {
        return instant == null ? null : instant.toString();
    }

    private int clamp(int size) {
        return Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    }

    private String trimTo(String value, int max) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.length() > max ? trimmed.substring(0, max) : trimmed;
    }
}

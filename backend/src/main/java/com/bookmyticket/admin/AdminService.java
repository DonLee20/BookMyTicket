package com.bookmyticket.admin;

import com.bookmyticket.booking.Booking;
import com.bookmyticket.booking.BookingRepository;
import com.bookmyticket.booking.BookingService;
import com.bookmyticket.booking.BookingStatus;
import com.bookmyticket.booking.dto.BookingDtos;
import com.bookmyticket.catalog.HolidayPackage;
import com.bookmyticket.catalog.HolidayPackageRepository;
import com.bookmyticket.common.ApiException;
import com.bookmyticket.common.MoneyUtils;
import com.bookmyticket.common.PageResponse;
import com.bookmyticket.common.TimeUtils;
import com.bookmyticket.config.AppProperties;
import com.bookmyticket.contact.ContactMessage;
import com.bookmyticket.contact.ContactMessageRepository;
import com.bookmyticket.payment.Payment;
import com.bookmyticket.payment.PaymentRepository;
import com.bookmyticket.payment.PaymentService;
import com.bookmyticket.payment.ProviderTransactionRepository;
import com.bookmyticket.provider.ProviderRegistry;
import com.bookmyticket.transport.bus.BusSeatRepository;
import com.bookmyticket.transport.bus.BusTrip;
import com.bookmyticket.transport.bus.BusTripRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private static final int MAX_PAGE_SIZE = 50;

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final com.bookmyticket.identity.UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final HolidayPackageRepository packageRepository;
    private final BusTripRepository busTripRepository;
    private final BusSeatRepository busSeatRepository;
    private final ContactMessageRepository contactRepository;
    private final ProviderTransactionRepository providerTransactionRepository;
    private final ProviderRegistry providerRegistry;
    private final AppProperties properties;
    private final JdbcTemplate jdbcTemplate;

    public AdminService(BookingRepository bookingRepository, BookingService bookingService,
                        com.bookmyticket.identity.UserRepository userRepository,
                        PaymentRepository paymentRepository, PaymentService paymentService,
                        HolidayPackageRepository packageRepository, BusTripRepository busTripRepository,
                        BusSeatRepository busSeatRepository, ContactMessageRepository contactRepository,
                        ProviderTransactionRepository providerTransactionRepository,
                        ProviderRegistry providerRegistry, AppProperties properties, JdbcTemplate jdbcTemplate) {
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
        this.packageRepository = packageRepository;
        this.busTripRepository = busTripRepository;
        this.busSeatRepository = busSeatRepository;
        this.contactRepository = contactRepository;
        this.providerTransactionRepository = providerTransactionRepository;
        this.providerRegistry = providerRegistry;
        this.properties = properties;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public AdminDtos.OverviewResponse overview() {
        Map<String, Long> byStatus = grouped(bookingRepository.countGroupedByStatus());
        Map<String, Long> byCategory = grouped(bookingRepository.countGroupedByCategory());
        LocalDate today = LocalDate.now(TimeUtils.INDIA);
        long tripsToday = busTripRepository.countByTravelDate(today);
        long seatsToday = busSeatRepository.countAvailableForDate(today);
        List<HolidayPackage> activePackages = packageRepository.findAllActive();
        long revenue = bookingRepository.sumConfirmedRevenueMinor();
        return new AdminDtos.OverviewResponse(
                userRepository.count(),
                bookingRepository.count(),
                bookingRepository.countByStatus(BookingStatus.CONFIRMED)
                        + bookingRepository.countByStatus(BookingStatus.TICKETED),
                bookingRepository.countByStatus(BookingStatus.CANCELLED),
                paymentRepository.countByStatus(com.bookmyticket.booking.PaymentStatus.PENDING),
                revenue,
                MoneyUtils.format(revenue, "INR"),
                contactRepository.countByStatus("NEW"),
                packageRepository.count(),
                activePackages.size(),
                tripsToday,
                seatsToday,
                byStatus,
                byCategory,
                paymentService.mode().name(),
                Instant.now().toString());
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminDtos.UserRow> users(String query, int page, int size) {
        Pageable pageable = pageable(page, size);
        Page<com.bookmyticket.identity.User> result = userRepository.adminSearch(blankToNull(query), pageable);
        return PageResponse.from(result, user -> new AdminDtos.UserRow(user.getId(), user.getName(), user.getEmail(),
                user.getPhone(), user.isEnabled(),
                user.getRoles().stream().map(com.bookmyticket.identity.Role::getName).sorted().toList(),
                user.getCreatedAt().toString()));
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingDtos.BookingResponse> bookings(String category, String status, String query,
                                                              int page, int size) {
        Pageable pageable = pageable(page, size);
        Page<Booking> result = bookingRepository.adminSearch(parseCategory(category), parseStatus(status),
                blankToNull(query), pageable);
        return PageResponse.from(result, bookingService::toResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminDtos.PaymentRow> payments(String status, String query, int page, int size) {
        Pageable pageable = pageable(page, size);
        Page<Payment> result = paymentRepository.adminSearch(parsePaymentStatus(status), blankToNull(query), pageable);
        return PageResponse.from(result, this::toPaymentRow);
    }

    @Transactional
    public BookingDtos.BookingResponse cancelBooking(String bookingId, String reason) {
        return bookingService.adminCancel(bookingId, reason);
    }

    @Transactional(readOnly = true)
    public List<AdminDtos.PackageRow> packages() {
        return packageRepository.findAll().stream()
                .sorted((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()))
                .map(this::toPackageRow)
                .toList();
    }

    @Transactional
    public AdminDtos.PackageRow updatePackage(Long id, AdminDtos.PackageUpdateRequest request) {
        HolidayPackage holidayPackage = packageRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("PACKAGE_NOT_FOUND", "That holiday package could not be found."));
        if (request.active() != null) {
            holidayPackage.setActive(request.active());
        }
        if (request.featured() != null) {
            holidayPackage.setFeatured(request.featured());
        }
        return toPackageRow(packageRepository.save(holidayPackage));
    }

    @Transactional(readOnly = true)
    public List<AdminDtos.BusInventoryRow> busInventory(LocalDate date, boolean premiumOnly) {
        LocalDate effectiveDate = date == null ? LocalDate.now(TimeUtils.INDIA) : date;
        List<BusTrip> trips = busTripRepository.findForDate(effectiveDate);
        Map<Long, Integer> availability = new java.util.HashMap<>();
        if (!trips.isEmpty()) {
            for (Object[] row : busSeatRepository.countAvailableByTripIds(
                    trips.stream().map(BusTrip::getId).toList())) {
                availability.put((Long) row[0], ((Number) row[1]).intValue());
            }
        }
        return trips.stream()
                .filter(trip -> !premiumOnly || trip.isPremium())
                .map(trip -> new AdminDtos.BusInventoryRow(
                        trip.getId(),
                        trip.getSchedule().getOperator().getName(),
                        trip.getBusType().name(),
                        trip.isPremium(),
                        trip.getDirection().name(),
                        trip.getDepartureCity(),
                        trip.getArrivalCity(),
                        trip.getTravelDate().toString(),
                        TimeUtils.localClock(trip.getDepartAt(), TimeUtils.INDIA),
                        TimeUtils.localClock(trip.getArriveAt(), TimeUtils.INDIA),
                        trip.getFareMinor(),
                        MoneyUtils.format(trip.getFareMinor(), trip.getCurrency()),
                        trip.getTotalSeats(),
                        availability.getOrDefault(trip.getId(), 0),
                        trip.getStatus().name(),
                        trip.getSource()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminDtos.MessageRow> messages(String status, int page, int size) {
        Pageable pageable = pageable(page, size);
        Page<ContactMessage> result = status == null || status.isBlank()
                ? contactRepository.findAllByOrderByCreatedAtDesc(pageable)
                : contactRepository.findByStatusOrderByCreatedAtDesc(status.trim().toUpperCase(), pageable);
        return PageResponse.from(result, this::toMessageRow);
    }

    @Transactional
    public AdminDtos.MessageRow handleMessage(Long id) {
        ContactMessage message = contactRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("MESSAGE_NOT_FOUND", "That message could not be found."));
        message.setStatus("HANDLED");
        message.setHandledAt(Instant.now());
        return toMessageRow(contactRepository.save(message));
    }

    @Transactional(readOnly = true)
    public AdminDtos.HealthResponse health() {
        String database = "DOWN";
        String schemaVersion = "unknown";
        try {
            Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            database = one != null && one == 1 ? "UP" : "DEGRADED";
            schemaVersion = jdbcTemplate.queryForObject(
                    "SELECT version FROM flyway_schema_history WHERE success = 1 ORDER BY installed_rank DESC LIMIT 1",
                    String.class);
        } catch (Exception ex) {
            database = "DOWN";
        }
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        AdminDtos.MaintenanceSummary maintenance = new AdminDtos.MaintenanceSummary(
                userRepository.count(), bookingRepository.count(), paymentRepository.count(),
                providerTransactionRepository.count(), contactRepository.count());
        return new AdminDtos.HealthResponse(
                "UP".equals(database) ? "UP" : "DOWN",
                database,
                schemaVersion == null ? "unknown" : schemaVersion,
                "2.0.0",
                uptime,
                paymentService.status().mode(),
                providerRegistry.all(),
                maintenance,
                Instant.now().toString());
    }

    // ------------------------------------------------------------------

    private AdminDtos.PaymentRow toPaymentRow(Payment payment) {
        return new AdminDtos.PaymentRow(
                payment.getId(),
                payment.getBooking().getId(),
                payment.getBooking().getUser().getEmail(),
                payment.getProvider(),
                payment.getMode().name(),
                payment.getStatus().name(),
                payment.getAmountMinor(),
                MoneyUtils.format(payment.getAmountMinor(), payment.getCurrency()),
                payment.getCurrency(),
                payment.getProviderOrderId(),
                payment.getProviderPaymentId(),
                payment.getCreatedAt().toString(),
                payment.getFailureReason());
    }

    private AdminDtos.PackageRow toPackageRow(HolidayPackage p) {
        return new AdminDtos.PackageRow(p.getId(), p.getSlug(), p.getTitle(), p.getDestination().getName(),
                p.getCategory().name(), p.getCategory().getLabel(), p.getDurationDays(), p.getBasePriceMinor(),
                MoneyUtils.format(p.getBasePriceMinor(), p.getCurrency()), p.getCurrency(), p.isActive(),
                p.isFeatured(), p.getImageUrl());
    }

    private AdminDtos.MessageRow toMessageRow(ContactMessage message) {
        return new AdminDtos.MessageRow(message.getId(), message.getName(), message.getEmail(), message.getSubject(),
                message.getMessage(), message.getStatus(),
                message.getCreatedAt() == null ? null : message.getCreatedAt().toString(),
                message.getHandledAt() == null ? null : message.getHandledAt().toString());
    }

    private Map<String, Long> grouped(List<Object[]> rows) {
        Map<String, Long> result = new java.util.LinkedHashMap<>();
        for (Object[] row : rows) {
            result.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        return result;
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(Math.max(0, page), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt"));
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

    private com.bookmyticket.booking.BookingCategory parseCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        try {
            return com.bookmyticket.booking.BookingCategory.valueOf(category.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("INVALID_CATEGORY", "Unknown booking category: " + category);
        }
    }

    private com.bookmyticket.booking.PaymentStatus parsePaymentStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return com.bookmyticket.booking.PaymentStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("INVALID_PAYMENT_STATUS", "Unknown payment status: " + status);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

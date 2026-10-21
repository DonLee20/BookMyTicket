package com.bookmyticket.booking;

import com.bookmyticket.catalog.PackageDeparture;
import com.bookmyticket.catalog.PackageDepartureRepository;
import com.bookmyticket.common.ApiException;
import com.bookmyticket.transport.bus.BusSeat;
import com.bookmyticket.transport.bus.BusSeatRepository;
import com.bookmyticket.transport.bus.SeatStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Applies booking state changes to inventory. Keeping this in one place makes
 * payment confirmation, expiry and cancellation idempotent and auditable.
 */
@Service
public class BookingLifecycleService {

    private final BookingRepository bookingRepository;
    private final BookingPassengerRepository passengerRepository;
    private final BusSeatRepository seatRepository;
    private final HolidayBookingDetailRepository holidayDetailRepository;
    private final PackageDepartureRepository departureRepository;

    public BookingLifecycleService(BookingRepository bookingRepository,
                                   BookingPassengerRepository passengerRepository,
                                   BusSeatRepository seatRepository,
                                   HolidayBookingDetailRepository holidayDetailRepository,
                                   PackageDepartureRepository departureRepository) {
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.seatRepository = seatRepository;
        this.holidayDetailRepository = holidayDetailRepository;
        this.departureRepository = departureRepository;
    }

    @Transactional
    public void onPaymentSucceeded(Booking booking) {
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return; // idempotent retry of the same payment event
        }
        if (booking.getStatus() != BookingStatus.PAYMENT_PENDING) {
            throw ApiException.conflict("BOOKING_NOT_PAYABLE",
                    "This booking is " + booking.getStatus().name().toLowerCase()
                            + " and can no longer be confirmed by a payment.");
        }
        List<BusSeat> seats = lockSeats(booking);
        for (BusSeat seat : seats) {
            if (seat.getStatus() != SeatStatus.HELD && seat.getStatus() != SeatStatus.BOOKED) {
                throw ApiException.conflict("SEAT_HOLD_LOST",
                        "One or more selected seats are no longer held for this booking. Please start again.");
            }
            seat.setStatus(SeatStatus.BOOKED);
        }

        adjustHolidayCapacity(booking, true);

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setHoldExpiresAt(null);
        bookingRepository.save(booking);
    }

    @Transactional
    public void onPaymentFailed(Booking booking, String reason) {
        if (booking.getStatus() != BookingStatus.PAYMENT_PENDING) {
            return;
        }
        releaseSeats(booking);
        booking.setStatus(BookingStatus.FAILED);
        booking.setPaymentStatus(PaymentStatus.FAILED);
        booking.setHoldExpiresAt(null);
        booking.setNotes(appendNote(booking.getNotes(), reason));
        bookingRepository.save(booking);
    }

    @Transactional
    public void expire(Booking booking, String reason) {
        if (booking.getStatus() != BookingStatus.PAYMENT_PENDING) {
            return;
        }
        releaseSeats(booking);
        booking.setStatus(BookingStatus.EXPIRED);
        booking.setPaymentStatus(PaymentStatus.FAILED);
        booking.setHoldExpiresAt(null);
        booking.setNotes(appendNote(booking.getNotes(), reason));
        bookingRepository.save(booking);
    }

    /**
     * Releases inventory when a booking is cancelled. Holiday capacity is only
     * returned when the booking had actually been confirmed (the capacity was
     * incremented on payment success).
     */
    @Transactional
    public void releaseOnCancellation(Booking booking) {
        boolean wasConfirmed = booking.getStatus() == BookingStatus.CONFIRMED
                || booking.getStatus() == BookingStatus.TICKETED;
        releaseSeats(booking);
        if (wasConfirmed) {
            adjustHolidayCapacity(booking, false);
        }
    }

    private List<BusSeat> lockSeats(Booking booking) {
        List<Long> seatIds = passengerRepository.findByBookingIdOrderBySeq(booking.getId()).stream()
                .map(BookingPassenger::getSeatId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (seatIds.isEmpty()) {
            return List.of();
        }
        List<BusSeat> seats = seatRepository.lockAllById(seatIds);
        if (seats.size() != seatIds.size()) {
            throw ApiException.conflict("SEAT_NOT_FOUND",
                    "One or more selected seats could not be found. Please search again.");
        }
        return seats;
    }

    private void releaseSeats(Booking booking) {
        for (BusSeat seat : lockSeats(booking)) {
            if (seat.getStatus() == SeatStatus.HELD || seat.getStatus() == SeatStatus.BOOKED) {
                seat.setStatus(SeatStatus.AVAILABLE);
            }
        }
    }

    private void adjustHolidayCapacity(Booking booking, boolean increment) {
        holidayDetailRepository.findByBookingId(booking.getId()).ifPresent(detail -> {
            if (detail.getDeparture() == null) {
                return;
            }
            Long packageId = detail.getHolidayPackage().getId();
            PackageDeparture departure = departureRepository
                    .lockByPackageAndDate(packageId, detail.getStartDate())
                    .orElseThrow(() -> ApiException.conflict("PACKAGE_DEPARTURE_MISSING",
                            "The package departure for this booking is no longer available."));
            int travellers = detail.getTravellers();
            if (increment) {
                if (departure.seatsRemaining() < travellers) {
                    throw ApiException.conflict("PACKAGE_SOLD_OUT",
                            "This departure no longer has enough seats for the requested travellers.");
                }
                departure.setSeatsBooked(departure.getSeatsBooked() + travellers);
            } else {
                departure.setSeatsBooked(Math.max(0, departure.getSeatsBooked() - travellers));
            }
            departureRepository.save(departure);
        });
    }

    private String appendNote(String current, String note) {
        String combined = (current == null || current.isBlank()) ? note : current + " " + note;
        return combined.length() > 500 ? combined.substring(0, 500) : combined;
    }
}

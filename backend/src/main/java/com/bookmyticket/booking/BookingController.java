package com.bookmyticket.booking;

import com.bookmyticket.booking.dto.BookingDtos;
import com.bookmyticket.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@Validated
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/bus")
    public ResponseEntity<BookingDtos.BookingResponse> createBus(
            @Valid @RequestBody BookingDtos.BusBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBus(request));
    }

    @PostMapping("/train")
    public ResponseEntity<BookingDtos.BookingResponse> createTrain(
            @Valid @RequestBody BookingDtos.TrainBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createTrain(request));
    }

    @PostMapping("/flight")
    public ResponseEntity<BookingDtos.BookingResponse> createFlight(
            @Valid @RequestBody BookingDtos.FlightBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createFlight(request));
    }

    @PostMapping("/holiday")
    public ResponseEntity<BookingDtos.BookingResponse> createHoliday(
            @Valid @RequestBody BookingDtos.HolidayBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createHoliday(request));
    }

    @GetMapping
    public PageResponse<BookingDtos.BookingResponse> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return bookingService.list(category, status, page, size);
    }

    @GetMapping("/{bookingId}")
    public BookingDtos.BookingResponse get(@PathVariable String bookingId) {
        return bookingService.get(bookingId);
    }

    @PostMapping("/{bookingId}/cancel")
    public BookingDtos.BookingResponse cancel(@PathVariable String bookingId,
                                              @Valid @RequestBody(required = false) BookingDtos.CancelRequest request) {
        return bookingService.cancel(bookingId, request == null ? null : request.reason());
    }
}

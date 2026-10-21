package com.bookmyticket.admin;

import com.bookmyticket.booking.dto.BookingDtos;
import com.bookmyticket.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Administrative API. All endpoints require ROLE_ADMIN (enforced in
 * SecurityConfig) and every change is executed server-side.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/overview")
    public AdminDtos.OverviewResponse overview() {
        return adminService.overview();
    }

    @GetMapping("/users")
    public PageResponse<AdminDtos.UserRow> users(@RequestParam(required = false) String query,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return adminService.users(query, page, size);
    }

    @GetMapping("/bookings")
    public PageResponse<BookingDtos.BookingResponse> bookings(@RequestParam(required = false) String category,
                                                              @RequestParam(required = false) String status,
                                                              @RequestParam(required = false) String query,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        return adminService.bookings(category, status, query, page, size);
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public BookingDtos.BookingResponse cancelBooking(@PathVariable String bookingId,
                                                     @RequestBody(required = false)
                                                     BookingDtos.CancelRequest request) {
        return adminService.cancelBooking(bookingId, request == null ? null : request.reason());
    }

    @GetMapping("/payments")
    public PageResponse<AdminDtos.PaymentRow> payments(@RequestParam(required = false) String status,
                                                       @RequestParam(required = false) String query,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        return adminService.payments(status, query, page, size);
    }

    @GetMapping("/packages")
    public List<AdminDtos.PackageRow> packages() {
        return adminService.packages();
    }

    @PatchMapping("/packages/{id}")
    public AdminDtos.PackageRow updatePackage(@PathVariable Long id,
                                              @Valid @RequestBody AdminDtos.PackageUpdateRequest request) {
        return adminService.updatePackage(id, request);
    }

    @GetMapping("/bus-inventory")
    public List<AdminDtos.BusInventoryRow> busInventory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "false") boolean premiumOnly) {
        return adminService.busInventory(date, premiumOnly);
    }

    @GetMapping("/messages")
    public PageResponse<AdminDtos.MessageRow> messages(@RequestParam(required = false) String status,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        return adminService.messages(status, page, size);
    }

    @PostMapping("/messages/{id}/handle")
    public AdminDtos.MessageRow handleMessage(@PathVariable Long id) {
        return adminService.handleMessage(id);
    }

    @GetMapping("/health")
    public AdminDtos.HealthResponse health() {
        return adminService.health();
    }
}

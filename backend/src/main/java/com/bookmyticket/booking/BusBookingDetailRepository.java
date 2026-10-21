package com.bookmyticket.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusBookingDetailRepository extends JpaRepository<BusBookingDetail, String> {

    Optional<BusBookingDetail> findByBookingId(String bookingId);
}

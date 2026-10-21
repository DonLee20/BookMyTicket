package com.bookmyticket.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FlightBookingDetailRepository extends JpaRepository<FlightBookingDetail, String> {

    Optional<FlightBookingDetail> findByBookingId(String bookingId);
}

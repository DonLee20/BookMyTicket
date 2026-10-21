package com.bookmyticket.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainBookingDetailRepository extends JpaRepository<TrainBookingDetail, String> {

    Optional<TrainBookingDetail> findByBookingId(String bookingId);
}

package com.bookmyticket.booking;

import com.bookmyticket.transport.bus.BusTrip;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "bus_booking_details")
@Getter
@Setter
@NoArgsConstructor
public class BusBookingDetail {

    @Id
    @Column(name = "booking_id", length = 36, nullable = false)
    private String bookingId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private BusTrip trip;

    @Column(name = "operator_name", nullable = false, length = 150)
    private String operatorName;

    @Column(name = "bus_type", nullable = false, length = 40)
    private String busType;

    @Column(name = "travel_date", nullable = false)
    private LocalDate travelDate;

    @Column(name = "departure_city", nullable = false, length = 80)
    private String departureCity;

    @Column(name = "arrival_city", nullable = false, length = 80)
    private String arrivalCity;

    @Column(name = "depart_at", nullable = false)
    private Instant departAt;

    @Column(name = "arrive_at", nullable = false)
    private Instant arriveAt;

    @Column(name = "boarding_point", length = 150)
    private String boardingPoint;

    @Column(name = "dropping_point", length = 150)
    private String droppingPoint;

    @Column(name = "seat_numbers", nullable = false, length = 200)
    private String seatNumbers;
}

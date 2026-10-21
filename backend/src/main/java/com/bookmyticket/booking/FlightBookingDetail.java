package com.bookmyticket.booking;

import com.bookmyticket.transport.flight.FlightSchedule;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "flight_booking_details")
@Getter
@Setter
@NoArgsConstructor
public class FlightBookingDetail {

    @Id
    @Column(name = "booking_id", length = 36, nullable = false)
    private String bookingId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private FlightSchedule schedule;

    @Column(name = "offer_ref", nullable = false, length = 80)
    private String offerRef;

    @Column(nullable = false, length = 30)
    private String cabin;

    @Column(name = "journey_date", nullable = false)
    private LocalDate journeyDate;

    @Column(name = "origin_iata", nullable = false, length = 3)
    private String originIata;

    @Column(name = "destination_iata", nullable = false, length = 3)
    private String destinationIata;

    @Column(name = "airline_name", nullable = false, length = 120)
    private String airlineName;

    @Column(name = "flight_number", nullable = false, length = 10)
    private String flightNumber;

    @Column(name = "depart_local", nullable = false, length = 30)
    private String departLocal;

    @Column(name = "arrive_local", nullable = false, length = 30)
    private String arriveLocal;

    @Column(nullable = false)
    private int stops;

    @Column(name = "baggage_note", length = 200)
    private String baggageNote;

    @Column(nullable = false)
    private boolean refundable;

    @Column(nullable = false)
    private boolean changeable;

    @Column(name = "provider_conditions", length = 500)
    private String providerConditions;
}

package com.bookmyticket.booking;

import com.bookmyticket.transport.train.TrainService;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "train_booking_details")
@Getter
@Setter
@NoArgsConstructor
public class TrainBookingDetail {

    @Id
    @Column(name = "booking_id", length = 36, nullable = false)
    private String bookingId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private TrainService service;

    @Column(name = "travel_class", nullable = false, length = 30)
    private String travelClass;

    @Column(nullable = false, length = 20)
    private String quota = "GENERAL";

    @Column(name = "journey_date", nullable = false)
    private LocalDate journeyDate;

    @Column(name = "from_station", nullable = false, length = 120)
    private String fromStation;

    @Column(name = "to_station", nullable = false, length = 120)
    private String toStation;

    @Column(name = "train_number", nullable = false, length = 10)
    private String trainNumber;

    @Column(name = "train_name", nullable = false, length = 150)
    private String trainName;

    @Column(name = "official_handoff_url", nullable = false, length = 400)
    private String officialHandoffUrl;
}

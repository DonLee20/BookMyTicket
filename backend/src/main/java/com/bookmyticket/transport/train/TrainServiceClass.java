package com.bookmyticket.transport.train;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "train_service_classes")
@Getter
@Setter
@NoArgsConstructor
public class TrainServiceClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private TrainService service;

    @Enumerated(EnumType.STRING)
    @Column(name = "travel_class", nullable = false, length = 30)
    private TravelClass travelClass;

    @Column(name = "fare_minor")
    private Long fareMinor;

    /**
     * Sample availability snapshot from the demo dataset. This is NOT live
     * Indian Railways availability and is labelled as sample data in the UI.
     */
    @Column(name = "availability_status", nullable = false, length = 40)
    private String availabilityStatus = "SAMPLE";

    @Column(name = "available_seats")
    private Integer availableSeats;

    @Column(name = "wl_position")
    private Integer wlPosition;
}

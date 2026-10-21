package com.bookmyticket.transport.train;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "train_route_stops")
@Getter
@Setter
@NoArgsConstructor
public class TrainRouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private TrainService service;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    private RailwayStation station;

    @Column(name = "stop_number", nullable = false)
    private int stopNumber;

    @Column(name = "arrive_offset_minutes")
    private Integer arriveOffsetMinutes;

    @Column(name = "depart_offset_minutes")
    private Integer departOffsetMinutes;

    @Column(name = "distance_km")
    private Integer distanceKm;

    @Column(name = "day_number", nullable = false)
    private int dayNumber = 1;
}

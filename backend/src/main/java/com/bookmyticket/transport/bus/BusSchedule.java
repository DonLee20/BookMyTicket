package com.bookmyticket.transport.bus;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bus_schedules")
@Getter
@Setter
@NoArgsConstructor
public class BusSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "corridor_id", nullable = false)
    private BusCorridor corridor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operator_id", nullable = false)
    private BusOperator operator;

    @Column(name = "depart_minutes", nullable = false)
    private int departMinutes;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "fare_minor", nullable = false)
    private long fareMinor;

    @Enumerated(EnumType.STRING)
    @Column(name = "bus_type", nullable = false, length = 40)
    private BusType busType;

    @Column(nullable = false)
    private boolean premium;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SeatLayout layout;

    @Column(length = 500)
    private String amenities;

    @Column(name = "boarding_points", length = 500)
    private String boardingPoints;

    @Column(name = "dropping_points", length = 500)
    private String droppingPoints;

    @Column(nullable = false)
    private boolean active = true;
}

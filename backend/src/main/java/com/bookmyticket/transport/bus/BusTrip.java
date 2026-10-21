package com.bookmyticket.transport.bus;

import com.bookmyticket.provider.ProviderMode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "bus_trips")
@Getter
@Setter
@NoArgsConstructor
public class BusTrip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private BusSchedule schedule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private TripDirection direction;

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

    @Column(name = "fare_minor", nullable = false)
    private long fareMinor;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

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

    @Column(name = "total_seats", nullable = false)
    private int totalSeats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TripStatus status = TripStatus.SCHEDULED;

    @Column(nullable = false, length = 20)
    private String source = "DEMO";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public ProviderMode providerMode() {
        return ProviderMode.DEMO;
    }
}

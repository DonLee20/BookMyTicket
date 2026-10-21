package com.bookmyticket.catalog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Per-date capacity bucket for a holiday package. Created on demand when a
 * booking is requested; the service locks the row to avoid overbooking.
 */
@Entity
@Table(name = "package_departures")
@Getter
@Setter
@NoArgsConstructor
public class PackageDeparture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private HolidayPackage holidayPackage;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "seats_booked", nullable = false)
    private int seatsBooked;

    @Column(name = "price_override_minor")
    private Long priceOverrideMinor;

    @Column(nullable = false, length = 20)
    private String status = "OPEN";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public int seatsRemaining() {
        return Math.max(0, capacity - seatsBooked);
    }
}

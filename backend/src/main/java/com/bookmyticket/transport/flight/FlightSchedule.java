package com.bookmyticket.transport.flight;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "flight_schedules")
@Getter
@Setter
@NoArgsConstructor
public class FlightSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "airline_code", nullable = false, length = 4)
    private String airlineCode;

    @Column(name = "airline_name", nullable = false, length = 120)
    private String airlineName;

    @Column(name = "flight_number", nullable = false, length = 10)
    private String flightNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_airport_id", nullable = false)
    private Airport originAirport;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_airport_id", nullable = false)
    private Airport destinationAirport;

    @Column(name = "depart_minutes", nullable = false)
    private int departMinutes;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(length = 40)
    private String aircraft;

    @Column(nullable = false)
    private int stops;

    @Column(name = "base_fare_economy_minor")
    private Long baseFareEconomyMinor;

    @Column(name = "base_fare_premium_minor")
    private Long baseFarePremiumMinor;

    @Column(name = "base_fare_business_minor")
    private Long baseFareBusinessMinor;

    @Column(name = "base_fare_first_minor")
    private Long baseFareFirstMinor;

    @Column(name = "baggage_note", length = 200)
    private String baggageNote;

    @Column(nullable = false)
    private boolean refundable;

    @Column(nullable = false)
    private boolean changeable = true;

    @Column(nullable = false)
    private boolean active = true;

    public Long baseFareMinor(CabinClass cabin) {
        return switch (cabin) {
            case ECONOMY -> baseFareEconomyMinor;
            case PREMIUM_ECONOMY -> baseFarePremiumMinor;
            case BUSINESS -> baseFareBusinessMinor;
            case FIRST -> baseFareFirstMinor;
        };
    }
}

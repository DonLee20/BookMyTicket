package com.bookmyticket.transport.bus;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bus_corridors")
@Getter
@Setter
@NoArgsConstructor
public class BusCorridor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city_a", nullable = false, length = 80)
    private String cityA;

    @Column(name = "city_b", nullable = false, length = 80)
    private String cityB;

    @Column(name = "distance_km", nullable = false)
    private int distanceKm;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;
}

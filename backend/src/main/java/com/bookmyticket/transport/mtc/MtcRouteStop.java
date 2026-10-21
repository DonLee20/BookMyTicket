package com.bookmyticket.transport.mtc;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "mtc_route_stops")
@Getter
@Setter
@NoArgsConstructor
public class MtcRouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private MtcRoute route;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stop_id", nullable = false)
    private MtcStop stop;

    @Column(name = "stop_sequence", nullable = false)
    private int stopSequence;

    @Column(name = "stage_number", nullable = false)
    private int stageNumber;

    @Column(name = "fare_from_origin_minor", nullable = false)
    private long fareFromOriginMinor;

    @Column(name = "distance_km", precision = 6, scale = 1)
    private BigDecimal distanceKm;
}

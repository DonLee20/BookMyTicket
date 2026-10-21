package com.bookmyticket.transport.mtc;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mtc_routes")
@Getter
@Setter
@NoArgsConstructor
public class MtcRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_number", nullable = false, length = 12)
    private String routeNumber;

    @Column(nullable = false, length = 20)
    private String direction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_stop_id", nullable = false)
    private MtcStop originStop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_stop_id", nullable = false)
    private MtcStop destinationStop;

    @Column(name = "first_service", nullable = false, length = 10)
    private String firstService;

    @Column(name = "last_service", nullable = false, length = 10)
    private String lastService;

    @Column(name = "frequency_minutes")
    private Integer frequencyMinutes;

    @Column(name = "service_days", nullable = false, length = 60)
    private String serviceDays = "DAILY";

    @Column(name = "wheelchair_accessible", nullable = false)
    private boolean accessible;

    @Column(name = "fare_min_minor", nullable = false)
    private long fareMinMinor;

    @Column(name = "fare_max_minor", nullable = false)
    private long fareMaxMinor;

    /** SAMPLE or VERIFIED_STATIC. Never live operational data. */
    @Column(nullable = false, length = 20)
    private String source = "SAMPLE";
}

package com.bookmyticket.transport.train;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "train_services")
@Getter
@Setter
@NoArgsConstructor
public class TrainService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "train_number", nullable = false, length = 10)
    private String trainNumber;

    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_station_id", nullable = false)
    private RailwayStation originStation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_station_id", nullable = false)
    private RailwayStation destinationStation;

    @Column(name = "depart_minutes", nullable = false)
    private int departMinutes;

    @Column(name = "arrive_minutes", nullable = false)
    private int arriveMinutes;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    /** Comma separated: MON..SUN, or DAILY. */
    @Column(name = "running_days", nullable = false, length = 80)
    private String runningDays;

    @Column(name = "total_distance_km")
    private Integer totalDistanceKm;

    @Column(nullable = false)
    private boolean active = true;

    public boolean runsOn(java.time.DayOfWeek day) {
        if ("DAILY".equalsIgnoreCase(runningDays)) {
            return true;
        }
        String shortName = day.name().substring(0, 3);
        for (String part : runningDays.split(",")) {
            if (part.trim().equalsIgnoreCase(shortName)) {
                return true;
            }
        }
        return false;
    }
}

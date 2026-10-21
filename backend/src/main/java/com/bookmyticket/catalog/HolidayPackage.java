package com.bookmyticket.catalog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "holiday_packages")
@Getter
@Setter
@NoArgsConstructor
public class HolidayPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String slug;

    @Column(nullable = false, length = 200)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_id", nullable = false)
    private Destination destination;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PackageCategory category;

    @Column(nullable = false, length = 400)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @Column(name = "duration_nights", nullable = false)
    private int durationNights;

    @Column(name = "base_price_minor", nullable = false)
    private long basePriceMinor;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "land_only", nullable = false)
    private boolean landOnly = true;

    @Column(name = "flights_included", nullable = false)
    private boolean flightsIncluded;

    @Column(name = "hotels_included", nullable = false)
    private boolean hotelsIncluded = true;

    @Column(name = "transport_included", nullable = false)
    private boolean transportIncluded;

    @Column(name = "meals_included", nullable = false)
    private boolean mealsIncluded;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean featured;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "package_inclusions", joinColumns = @JoinColumn(name = "package_id"))
    @Column(name = "line", nullable = false, length = 400)
    private List<String> inclusions = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "package_exclusions", joinColumns = @JoinColumn(name = "package_id"))
    @Column(name = "line", nullable = false, length = 400)
    private List<String> exclusions = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "package_highlights", joinColumns = @JoinColumn(name = "package_id"))
    @Column(name = "line", nullable = false, length = 400)
    private List<String> highlights = new ArrayList<>();

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
}

package com.bookmyticket.booking;

import com.bookmyticket.catalog.HolidayPackage;
import com.bookmyticket.catalog.PackageDeparture;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "holiday_booking_details")
@Getter
@Setter
@NoArgsConstructor
public class HolidayBookingDetail {

    @Id
    @Column(name = "booking_id", length = 36, nullable = false)
    private String bookingId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private HolidayPackage holidayPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departure_id")
    private PackageDeparture departure;

    @Column(name = "package_title", nullable = false, length = 200)
    private String packageTitle;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private int travellers;

    @Column(nullable = false)
    private int rooms = 1;

    @Column(name = "land_only", nullable = false)
    private boolean landOnly = true;
}

package com.bookmyticket.catalog;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface PackageDepartureRepository extends JpaRepository<PackageDeparture, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from PackageDeparture d where d.holidayPackage.id = :packageId and d.departureDate = :date")
    Optional<PackageDeparture> lockByPackageAndDate(@Param("packageId") Long packageId, @Param("date") LocalDate date);

    /**
     * Idempotently creates the capacity row for a package/date pair. The unique
     * key on (package_id, departure_date) makes concurrent inserts safe.
     */
    @Modifying
    @Query(value = """
            INSERT INTO package_departures
                (package_id, departure_date, capacity, seats_booked, status, created_at, updated_at)
            VALUES (:packageId, :date, :capacity, 0, 'OPEN', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6))
            ON DUPLICATE KEY UPDATE updated_at = updated_at
            """, nativeQuery = true)
    void ensureDeparture(@Param("packageId") Long packageId,
                         @Param("date") LocalDate date,
                         @Param("capacity") int capacity);

    long countByHolidayPackageIdAndStatus(Long packageId, String status);
}

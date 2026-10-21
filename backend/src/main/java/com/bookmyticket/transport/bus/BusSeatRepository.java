package com.bookmyticket.transport.bus;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface BusSeatRepository extends JpaRepository<BusSeat, Long> {

    List<BusSeat> findByTripIdOrderByDeckAscRowNoAscColNoAsc(Long tripId);

    long countByTripIdAndStatus(Long tripId, SeatStatus status);

    @Query("""
            select s.trip.id, count(s) from BusSeat s
            where s.trip.id in :tripIds and s.status = com.bookmyticket.transport.bus.SeatStatus.AVAILABLE
            group by s.trip.id
            """)
    List<Object[]> countAvailableByTripIds(@Param("tripIds") Collection<Long> tripIds);

    @Query("""
            select count(s) from BusSeat s
            where s.trip.travelDate = :date and s.status = com.bookmyticket.transport.bus.SeatStatus.AVAILABLE
            """)
    long countAvailableForDate(@Param("date") LocalDate date);

    /**
     * Pessimistic write lock: prevents double-selling the same seat across
     * concurrent booking requests.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from BusSeat s where s.id in :ids order by s.id")
    List<BusSeat> lockAllById(@Param("ids") Collection<Long> ids);
}

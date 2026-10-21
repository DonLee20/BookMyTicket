package com.bookmyticket.transport.bus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BusCorridorRepository extends JpaRepository<BusCorridor, Long> {

    /** Finds a corridor in either direction, so generated routes never duplicate a known pair. */
    @Query("""
            select c from BusCorridor c
            where (lower(c.cityA) = lower(:cityA) and lower(c.cityB) = lower(:cityB))
               or (lower(c.cityA) = lower(:cityB) and lower(c.cityB) = lower(:cityA))
            """)
    List<BusCorridor> findPair(@Param("cityA") String cityA, @Param("cityB") String cityB);
}

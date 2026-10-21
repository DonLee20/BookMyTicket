package com.bookmyticket.transport.bus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BusScheduleRepository extends JpaRepository<BusSchedule, Long> {

    List<BusSchedule> findByActiveTrue();

    @Query("""
            select s from BusSchedule s
            join fetch s.corridor c
            join fetch s.operator o
            where s.active = true
            order by s.id
            """)
    List<BusSchedule> findAllActiveDetailed();

    /** Active corridors regardless of the selected travel date, for route diagnostics. */
    @Query("""
            select distinct s.corridor from BusSchedule s
            join s.corridor c
            where s.active = true
            order by c.id
            """)
    List<BusCorridor> findActiveCorridors();
}

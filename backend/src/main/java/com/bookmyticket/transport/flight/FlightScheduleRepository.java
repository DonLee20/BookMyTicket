package com.bookmyticket.transport.flight;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface FlightScheduleRepository extends JpaRepository<FlightSchedule, Long> {

    @Query("""
            select f from FlightSchedule f
            join fetch f.originAirport oa
            join fetch f.destinationAirport da
            where f.originAirport.id in :originIds
              and f.destinationAirport.id in :destinationIds
              and f.active = true
            order by f.departMinutes
            """)
    List<FlightSchedule> findBetweenAirports(@Param("originIds") Collection<Long> originIds,
                                             @Param("destinationIds") Collection<Long> destinationIds);
}

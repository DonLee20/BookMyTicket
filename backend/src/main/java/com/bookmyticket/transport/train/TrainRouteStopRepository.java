package com.bookmyticket.transport.train;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrainRouteStopRepository extends JpaRepository<TrainRouteStop, Long> {

    @Query("""
            select rs from TrainRouteStop rs
            join fetch rs.station
            where rs.service.id = :serviceId
            order by rs.stopNumber
            """)
    List<TrainRouteStop> findWithStationByServiceId(@Param("serviceId") Long serviceId);
}

package com.bookmyticket.transport.mtc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MtcRouteStopRepository extends JpaRepository<MtcRouteStop, Long> {

    @Query("""
            select rs from MtcRouteStop rs
            join fetch rs.stop
            where rs.route.id = :routeId
            order by rs.stopSequence
            """)
    List<MtcRouteStop> findWithStopByRouteId(@Param("routeId") Long routeId);
}

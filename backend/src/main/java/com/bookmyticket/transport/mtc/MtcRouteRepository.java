package com.bookmyticket.transport.mtc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface MtcRouteRepository extends JpaRepository<MtcRoute, Long> {

    @Query("""
            select distinct r from MtcRoute r
            join fetch r.originStop
            join fetch r.destinationStop
            where exists (select 1 from MtcRouteStop a where a.route = r and a.stop.id in :originIds)
              and exists (select 1 from MtcRouteStop b where b.route = r and b.stop.id in :destinationIds)
            order by r.routeNumber
            """)
    List<MtcRoute> findServingBothStops(@Param("originIds") Collection<Long> originIds,
                                        @Param("destinationIds") Collection<Long> destinationIds);
}

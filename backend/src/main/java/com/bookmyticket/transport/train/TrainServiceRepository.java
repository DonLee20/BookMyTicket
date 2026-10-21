package com.bookmyticket.transport.train;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TrainServiceRepository extends JpaRepository<TrainService, Long> {

    @Query("""
            select distinct t from TrainService t
            join fetch t.originStation os
            join fetch t.destinationStation ds
            where t.originStation.id in :originIds
              and t.destinationStation.id in :destinationIds
              and t.active = true
            order by t.departMinutes
            """)
    List<TrainService> findBetweenStations(@Param("originIds") Collection<Long> originIds,
                                           @Param("destinationIds") Collection<Long> destinationIds);

    @Query("""
            select distinct t from TrainService t
            join fetch t.originStation os
            join fetch t.destinationStation ds
            where t.trainNumber like concat(:query, '%')
               or lower(t.name) like lower(concat('%', :query, '%'))
            order by t.trainNumber
            """)
    List<TrainService> search(@Param("query") String query);

    @Query("""
            select t from TrainService t
            join fetch t.originStation os
            join fetch t.destinationStation ds
            where t.id = :id
            """)
    java.util.Optional<TrainService> findDetailedById(@Param("id") Long id);
}

package com.bookmyticket.transport.train;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RailwayStationRepository extends JpaRepository<RailwayStation, Long> {

    Optional<RailwayStation> findByCodeIgnoreCase(String code);

    @Query("""
            select s from RailwayStation s
            where lower(s.name) like lower(concat('%', :query, '%'))
               or lower(s.city) like lower(concat('%', :query, '%'))
               or upper(s.code) like upper(concat(:query, '%'))
            order by s.city, s.name
            """)
    List<RailwayStation> search(@Param("query") String query);
}

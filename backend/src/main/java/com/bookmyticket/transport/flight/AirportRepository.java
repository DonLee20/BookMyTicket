package com.bookmyticket.transport.flight;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AirportRepository extends JpaRepository<Airport, Long> {

    @Query("""
            select a from Airport a
            where upper(a.iata) like upper(concat(:query, '%'))
               or lower(a.city) like lower(concat(:query, '%'))
               or lower(a.name) like lower(concat('%', :query, '%'))
            order by a.city
            """)
    List<Airport> search(@Param("query") String query);

    List<Airport> findByIataInIgnoreCase(Collection<String> iataCodes);
}

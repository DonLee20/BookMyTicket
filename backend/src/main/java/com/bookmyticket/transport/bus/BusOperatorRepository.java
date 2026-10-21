package com.bookmyticket.transport.bus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BusOperatorRepository extends JpaRepository<BusOperator, Long> {

    @Query("""
            select distinct o from BusOperator o
            where lower(o.name) like lower(concat(:query, '%'))
            order by o.name
            """)
    List<BusOperator> searchByName(@Param("query") String query);
}

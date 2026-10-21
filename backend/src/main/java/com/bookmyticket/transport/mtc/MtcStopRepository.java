package com.bookmyticket.transport.mtc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MtcStopRepository extends JpaRepository<MtcStop, Long> {

    @Query("""
            select s from MtcStop s
            where lower(s.name) like lower(concat(:query, '%'))
               or lower(s.locality) like lower(concat('%', :query, '%'))
            order by s.name
            """)
    List<MtcStop> search(@Param("query") String query);
}

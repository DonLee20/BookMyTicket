package com.bookmyticket.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DestinationRepository extends JpaRepository<Destination, Long> {

    Optional<Destination> findBySlug(String slug);

    List<Destination> findByFeaturedTrueOrderByNameAsc();

    @Query("""
            select d from Destination d
            where lower(d.name) like lower(concat('%', :query, '%'))
               or lower(d.region) like lower(concat('%', :query, '%'))
               or lower(d.country) like lower(concat('%', :query, '%'))
            order by d.featured desc, d.name
            """)
    List<Destination> search(@Param("query") String query);
}

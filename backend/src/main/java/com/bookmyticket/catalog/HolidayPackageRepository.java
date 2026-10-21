package com.bookmyticket.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HolidayPackageRepository extends JpaRepository<HolidayPackage, Long> {

    @Query("""
            select p from HolidayPackage p
            join fetch p.destination d
            where p.slug = :slug and p.active = true
            """)
    Optional<HolidayPackage> findDetailedBySlug(@Param("slug") String slug);

    @Query("""
            select p from HolidayPackage p
            join fetch p.destination d
            where p.active = true
            order by p.featured desc, p.basePriceMinor
            """)
    List<HolidayPackage> findAllActive();

    @Query("""
            select p from HolidayPackage p
            join fetch p.destination d
            where p.featured = true and p.active = true
            order by p.basePriceMinor
            """)
    List<HolidayPackage> findFeatured();

    @Query("""
            select p from HolidayPackage p
            join fetch p.destination d
            where p.active = true
              and (:destinationSlug is null or d.slug = :destinationSlug)
              and (:category is null or p.category = :category)
              and (:kind is null or d.kind = :kind)
              and (:maxPriceMinor is null or p.basePriceMinor <= :maxPriceMinor)
              and (:minDays is null or p.durationDays >= :minDays)
              and (:maxDays is null or p.durationDays <= :maxDays)
              and (:query is null or lower(p.title) like lower(concat('%', :query, '%'))
                   or lower(d.name) like lower(concat('%', :query, '%')))
            order by p.featured desc, p.basePriceMinor
            """)
    List<HolidayPackage> search(@Param("query") String query,
                                @Param("destinationSlug") String destinationSlug,
                                @Param("category") PackageCategory category,
                                @Param("kind") DestinationKind kind,
                                @Param("maxPriceMinor") Long maxPriceMinor,
                                @Param("minDays") Integer minDays,
                                @Param("maxDays") Integer maxDays);
}

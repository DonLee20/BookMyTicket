package com.bookmyticket.transport.bus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BusTripRepository extends JpaRepository<BusTrip, Long> {

    @Query("""
            select distinct t.departureCity from BusTrip t
            where lower(t.departureCity) like lower(concat(:query, '%'))
            order by t.departureCity
            """)
    List<String> searchDepartureCities(@Param("query") String query);

    @Query("""
            select distinct t.arrivalCity from BusTrip t
            where lower(t.arrivalCity) like lower(concat(:query, '%'))
            order by t.arrivalCity
            """)
    List<String> searchArrivalCities(@Param("query") String query);

    @Query("""
            select t from BusTrip t
            join fetch t.schedule s
            join fetch s.operator o
            where lower(t.departureCity) = lower(:fromCity)
              and lower(t.arrivalCity) = lower(:toCity)
              and t.travelDate = :date
              and t.status = com.bookmyticket.transport.bus.TripStatus.SCHEDULED
            order by t.departAt
            """)
    List<BusTrip> search(@Param("fromCity") String fromCity,
                         @Param("toCity") String toCity,
                         @Param("date") LocalDate date);

    @Query("""
            select t from BusTrip t
            join fetch t.schedule s
            join fetch s.operator o
            where t.id = :id
            """)
    java.util.Optional<BusTrip> findDetailedById(@Param("id") Long id);

    @Query("""
            select count(t) from BusTrip t
            where t.travelDate = :date
            """)
    long countByTravelDate(@Param("date") LocalDate date);

    @Query("""
            select t from BusTrip t
            join fetch t.schedule s
            join fetch s.operator o
            where t.travelDate = :date
            order by t.departAt
            """)
    List<BusTrip> findForDate(@Param("date") LocalDate date);

    @Query("""
            select t from BusTrip t
            join fetch t.schedule s
            where t.travelDate between :from and :to
            """)
    List<BusTrip> findBetweenDates(@Param("from") LocalDate from, @Param("to") LocalDate to);
}

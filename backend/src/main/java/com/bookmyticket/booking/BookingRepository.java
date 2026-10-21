package com.bookmyticket.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, String> {

    Page<Booking> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Booking> findByUserIdAndCategoryOrderByCreatedAtDesc(Long userId, BookingCategory category, Pageable pageable);

    Optional<Booking> findByIdAndUserId(String id, Long userId);

    @Query("""
            select b from Booking b
            where b.user.id = :userId
              and (:category is null or b.category = :category)
              and (:status is null or b.status = :status)
            order by b.createdAt desc
            """)
    Page<Booking> userSearch(@Param("userId") Long userId,
                             @Param("category") BookingCategory category,
                             @Param("status") BookingStatus status,
                             Pageable pageable);

    long countByStatus(BookingStatus status);

    Optional<Booking> findByIdempotencyKey(String idempotencyKey);

    List<Booking> findByStatusAndHoldExpiresAtBefore(BookingStatus status, Instant cutoff);

    long countByCategory(BookingCategory category);

    @Query("""
            select b.status, count(b) from Booking b group by b.status
            """)
    List<Object[]> countGroupedByStatus();

    @Query("""
            select b.category, count(b) from Booking b group by b.category
            """)
    List<Object[]> countGroupedByCategory();

    @Query("""
            select coalesce(sum(b.totalAmountMinor), 0) from Booking b
            where b.status in (com.bookmyticket.booking.BookingStatus.CONFIRMED,
                               com.bookmyticket.booking.BookingStatus.TICKETED,
                               com.bookmyticket.booking.BookingStatus.REFUND_PENDING)
            """)
    long sumConfirmedRevenueMinor();

    @Query("""
            select b from Booking b
            join fetch b.user u
            where (:category is null or b.category = :category)
              and (:status is null or b.status = :status)
              and (:query is null or lower(b.id) like lower(concat('%', :query, '%'))
                   or lower(u.email) like lower(concat('%', :query, '%')))
            order by b.createdAt desc
            """)
    Page<Booking> adminSearch(@Param("category") BookingCategory category,
                              @Param("status") BookingStatus status,
                              @Param("query") String query,
                              Pageable pageable);
}

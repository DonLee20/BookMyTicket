package com.bookmyticket.payment;

import com.bookmyticket.booking.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    List<Payment> findByBookingIdOrderByCreatedAtDesc(String bookingId);

    Optional<Payment> findByProviderOrderId(String providerOrderId);

    Optional<Payment> findByBookingIdAndStatus(String bookingId, PaymentStatus status);

    long countByStatus(PaymentStatus status);

    @Query("""
            select p from Payment p
            join fetch p.booking b
            join fetch b.user u
            where (:status is null or p.status = :status)
              and (:query is null or lower(p.id) like lower(concat('%', :query, '%'))
                   or lower(p.providerOrderId) like lower(concat('%', :query, '%'))
                   or lower(b.id) like lower(concat('%', :query, '%'))
                   or lower(u.email) like lower(concat('%', :query, '%')))
            order by p.createdAt desc
            """)
    Page<Payment> adminSearch(@Param("status") PaymentStatus status,
                              @Param("query") String query,
                              Pageable pageable);
}

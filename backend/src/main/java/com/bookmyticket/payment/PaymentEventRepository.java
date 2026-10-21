package com.bookmyticket.payment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentEventRepository extends JpaRepository<PaymentEvent, Long> {

    boolean existsByPaymentIdAndEventRef(String paymentId, String eventRef);
}

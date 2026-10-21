package com.bookmyticket.payment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderTransactionRepository extends JpaRepository<ProviderTransaction, Long> {

    long countBySuccessFalse();
}

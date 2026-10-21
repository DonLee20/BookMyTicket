package com.bookmyticket.transport.train;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainServiceClassRepository extends JpaRepository<TrainServiceClass, Long> {

    List<TrainServiceClass> findByServiceIdOrderByFareMinorAsc(Long serviceId);
}

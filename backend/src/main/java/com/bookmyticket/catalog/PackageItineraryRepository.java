package com.bookmyticket.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackageItineraryRepository extends JpaRepository<PackageItineraryDay, Long> {

    List<PackageItineraryDay> findByHolidayPackageIdOrderByDayNumberAsc(Long packageId);
}

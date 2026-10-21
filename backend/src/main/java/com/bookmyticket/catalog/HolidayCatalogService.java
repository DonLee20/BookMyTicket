package com.bookmyticket.catalog;

import com.bookmyticket.catalog.dto.HolidayDtos;
import com.bookmyticket.common.ApiException;
import com.bookmyticket.common.MoneyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class HolidayCatalogService {

    private final DestinationRepository destinationRepository;
    private final HolidayPackageRepository packageRepository;
    private final PackageItineraryRepository itineraryRepository;

    public HolidayCatalogService(DestinationRepository destinationRepository, HolidayPackageRepository packageRepository,
                                 PackageItineraryRepository itineraryRepository) {
        this.destinationRepository = destinationRepository;
        this.packageRepository = packageRepository;
        this.itineraryRepository = itineraryRepository;
    }

    @Transactional(readOnly = true)
    public HolidayDtos.SearchResponse search(String query, String destinationSlug, String category, String kind,
                                             Long maxPriceMinor, Integer minDays, Integer maxDays) {
        PackageCategory packageCategory = parseCategory(category);
        DestinationKind destinationKind = parseKind(kind);
        List<HolidayDtos.PackageSummary> packages = packageRepository
                .search(blankToNull(query), blankToNull(destinationSlug), packageCategory, destinationKind,
                        maxPriceMinor, minDays, maxDays)
                .stream()
                .map(this::toSummary)
                .toList();
        List<HolidayDtos.DestinationSummary> destinations = destinationRepository.findAll().stream()
                .map(this::toDestinationSummary)
                .toList();
        return new HolidayDtos.SearchResponse(packages,
                Arrays.stream(PackageCategory.values()).map(Enum::name).toList(),
                destinations,
                "DEMO_CATALOG");
    }

    @Transactional(readOnly = true)
    public HolidayDtos.PackageDetail packageDetail(String slug) {
        HolidayPackage holidayPackage = packageRepository.findDetailedBySlug(slug)
                .orElseThrow(() -> ApiException.notFound("PACKAGE_NOT_FOUND", "That holiday package could not be found."));
        List<HolidayDtos.ItineraryDay> itinerary = itineraryRepository
                .findByHolidayPackageIdOrderByDayNumberAsc(holidayPackage.getId()).stream()
                .map(day -> new HolidayDtos.ItineraryDay(day.getDayNumber(), day.getTitle(), day.getDescription(), day.getMeals()))
                .toList();
        String inclusionNote = inclusionNote(holidayPackage);
        return new HolidayDtos.PackageDetail(toSummary(holidayPackage), holidayPackage.getDescription(), itinerary,
                List.copyOf(holidayPackage.getInclusions()), List.copyOf(holidayPackage.getExclusions()),
                List.copyOf(holidayPackage.getHighlights()), cancellationPolicy(holidayPackage), inclusionNote);
    }

    @Transactional(readOnly = true)
    public HolidayDtos.DestinationDetail destinationDetail(String slug) {
        Destination destination = destinationRepository.findBySlug(slug)
                .orElseThrow(() -> ApiException.notFound("DESTINATION_NOT_FOUND", "That destination could not be found."));
        List<HolidayDtos.PackageSummary> packages = packageRepository.search(null, slug, null, null, null, null, null)
                .stream().map(this::toSummary).toList();
        return new HolidayDtos.DestinationDetail(toDestinationSummary(destination), destination.getDescription(), packages);
    }

    @Transactional(readOnly = true)
    public HolidayDtos.HomeResponse home() {
        List<HolidayDtos.PackageSummary> featured = packageRepository.findFeatured().stream()
                .limit(6).map(this::toSummary).toList();
        List<HolidayDtos.DestinationSummary> destinations = destinationRepository.findByFeaturedTrueOrderByNameAsc()
                .stream().limit(8).map(this::toDestinationSummary).toList();
        List<HolidayDtos.TransportTeaser> teasers = List.of(
                new HolidayDtos.TransportTeaser("BUS", "Intercity & premium buses",
                        "Compare AC sleepers, semi-sleepers and premium coaches on popular Indian routes.",
                        "/buses/search"),
                new HolidayDtos.TransportTeaser("TRAIN", "Indian Railways search",
                        "Look up sample train schedules and classes, then reserve on the official IRCTC channel.",
                        "/trains/search"),
                new HolidayDtos.TransportTeaser("FLIGHT", "Domestic & international flights",
                        "Search demo flight offers worldwide with fare revalidation before you request a hold.",
                        "/flights/search"),
                new HolidayDtos.TransportTeaser("MTC", "Chennai MTC city buses",
                        "Plan city bus journeys across Chennai using sample route and fare information.",
                        "/mtc"));
        return new HolidayDtos.HomeResponse(featured, destinations, teasers);
    }

    public HolidayDtos.PackageSummary toSummary(HolidayPackage p) {
        Destination d = p.getDestination();
        return new HolidayDtos.PackageSummary(p.getId(), p.getSlug(), p.getTitle(), p.getSummary(), p.getImageUrl(),
                d.getName(), d.getSlug(), d.getRegion(), d.getCountry(), d.getKind().name(), p.getCategory().name(),
                p.getCategory().getLabel(), p.getDurationDays(), p.getDurationNights(), p.getBasePriceMinor(),
                MoneyUtils.format(p.getBasePriceMinor(), p.getCurrency()), p.getCurrency(), p.isFeatured(), p.isLandOnly(),
                p.isFlightsIncluded(), p.isHotelsIncluded(), p.isTransportIncluded(), p.isMealsIncluded());
    }

    private HolidayDtos.DestinationSummary toDestinationSummary(Destination d) {
        return new HolidayDtos.DestinationSummary(d.getId(), d.getSlug(), d.getName(), d.getRegion(), d.getCountry(),
                d.getKind().name(), d.getHeroImageUrl(), d.getSummary(), d.getBestSeason(), d.isFeatured());
    }

    private String inclusionNote(HolidayPackage p) {
        if (p.isLandOnly()) {
            return "This is a land-only package. International or domestic airfare is not included unless explicitly stated.";
        }
        if (p.isFlightsIncluded()) {
            return "Airfare is included as stated in the itinerary. Hotel, transport and meal inclusions are listed above.";
        }
        return "Inclusions are exactly as listed. Anything not listed is excluded.";
    }

    private String cancellationPolicy(HolidayPackage p) {
        return "Package cancellation: full refund up to 15 days before departure, 50% between 7 and 15 days, "
                + "no refund within 7 days. Refund processing uses the original payment method. "
                + "Supplier-specific rules may apply for " + p.getCategory().getLabel().toLowerCase() + " packages.";
    }

    private PackageCategory parseCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        try {
            return PackageCategory.valueOf(category.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("INVALID_CATEGORY", "Unknown package category: " + category);
        }
    }

    private DestinationKind parseKind(String kind) {
        if (kind == null || kind.isBlank()) {
            return null;
        }
        try {
            return DestinationKind.valueOf(kind.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("INVALID_KIND", "Unknown destination kind: " + kind);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

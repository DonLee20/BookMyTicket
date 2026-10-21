package com.bookmyticket.catalog.dto;

import java.util.List;

public final class HolidayDtos {

    private HolidayDtos() {
    }

    public record DestinationSummary(
            Long id,
            String slug,
            String name,
            String region,
            String country,
            String kind,
            String heroImageUrl,
            String summary,
            String bestSeason,
            boolean featured
    ) {
    }

    public record DestinationDetail(
            DestinationSummary destination,
            String description,
            List<PackageSummary> packages
    ) {
    }

    public record PackageSummary(
            Long id,
            String slug,
            String title,
            String summary,
            String imageUrl,
            String destinationName,
            String destinationSlug,
            String stateOrRegion,
            String country,
            String kind,
            String category,
            String categoryLabel,
            int durationDays,
            int durationNights,
            long priceMinor,
            String priceDisplay,
            String currency,
            boolean featured,
            boolean landOnly,
            boolean flightsIncluded,
            boolean hotelsIncluded,
            boolean transportIncluded,
            boolean mealsIncluded
    ) {
    }

    public record ItineraryDay(int dayNumber, String title, String description, String meals) {
    }

    public record PackageDetail(
            PackageSummary summary,
            String description,
            List<ItineraryDay> itinerary,
            List<String> inclusions,
            List<String> exclusions,
            List<String> highlights,
            String cancellationPolicy,
            String inclusionNote
    ) {
    }

    public record SearchResponse(
            List<PackageSummary> packages,
            List<String> categories,
            List<DestinationSummary> destinations,
            String source
    ) {
    }

    public record HomeResponse(
            List<PackageSummary> featuredPackages,
            List<DestinationSummary> featuredDestinations,
            List<TransportTeaser> transportTeasers
    ) {
    }

    public record TransportTeaser(String category, String title, String description, String searchPath) {
    }
}

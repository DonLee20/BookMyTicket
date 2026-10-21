package com.bookmyticket.transport.bus;

import com.bookmyticket.common.ApiException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AmenityCatalog {

    private static final Map<String, String> LABELS = new LinkedHashMap<>();

    static {
        LABELS.put("AC", "Air conditioning");
        LABELS.put("WIFI", "Wi-Fi");
        LABELS.put("CHARGING_POINT", "Charging point");
        LABELS.put("BLANKET", "Blanket");
        LABELS.put("READING_LIGHT", "Reading light");
        LABELS.put("ENTERTAINMENT", "Entertainment");
        LABELS.put("WATER_BOTTLE", "Water bottle");
        LABELS.put("SNACKS", "Snacks");
    }

    private AmenityCatalog() {
    }

    public static List<String> parse(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return List.of(csv.split(",")).stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    public static String label(String code) {
        return LABELS.getOrDefault(code, code.replace('_', ' '));
    }

    public static Map<String, String> labels() {
        return LABELS;
    }

    public static void validateRequested(List<String> requested) {
        if (requested == null) {
            return;
        }
        for (String amenity : requested) {
            if (!LABELS.containsKey(amenity)) {
                throw ApiException.badRequest("UNKNOWN_AMENITY", "Unknown amenity filter: " + amenity);
            }
        }
    }
}

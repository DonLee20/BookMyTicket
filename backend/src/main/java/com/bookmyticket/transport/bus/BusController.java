package com.bookmyticket.transport.bus;

import com.bookmyticket.transport.bus.dto.BusDtos;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/buses")
@Validated
public class BusController {

    private final BusSearchService searchService;
    private final BusSeatService seatService;

    public BusController(BusSearchService searchService, BusSeatService seatService) {
        this.searchService = searchService;
        this.seatService = seatService;
    }

    @GetMapping("/cities")
    public Map<String, Object> cities(@RequestParam(required = false) String query,
                                      @RequestParam(defaultValue = "true") boolean origin) {
        List<String> cities = searchService.cityOptions(query, origin);
        return Map.of("cities", cities, "origin", origin);
    }

    @PostMapping("/search")
    public BusDtos.SearchResponse search(@Valid @RequestBody BusDtos.SearchRequest request) {
        return searchService.search(request);
    }

    @GetMapping("/trips/{tripId}/seats")
    public BusDtos.SeatMapResponse seats(@PathVariable Long tripId) {
        return seatService.seatMap(tripId);
    }

    @GetMapping("/amenities")
    public List<BusDtos.Amenity> amenities() {
        return AmenityCatalog.labels().entrySet().stream()
                .map(entry -> new BusDtos.Amenity(entry.getKey(), entry.getValue()))
                .toList();
    }
}

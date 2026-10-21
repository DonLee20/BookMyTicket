package com.bookmyticket.transport.bus;

import com.bookmyticket.transport.bus.dto.BusDtos;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Premium coach discovery. Search is the same sample dataset as /api/buses with
 * the premium-only flag enforced; booking follows the standard bus flow.
 */
@RestController
@RequestMapping("/api/premium")
@Validated
public class PremiumController {

    private final BusSearchService searchService;

    public PremiumController(BusSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/highlights")
    public BusDtos.PremiumHighlights highlights() {
        return searchService.premiumHighlights();
    }

    @PostMapping("/search")
    public BusDtos.SearchResponse search(@Valid @RequestBody BusDtos.SearchRequest request) {
        BusDtos.SearchRequest premiumRequest = new BusDtos.SearchRequest(
                request.from(), request.to(), request.date(), request.passengers(), true,
                request.maxFareMinor(), request.busTypes(), request.amenities(), request.operator(),
                request.sort(), request.sleeperOnly(), request.seaterOnly());
        return searchService.search(premiumRequest);
    }
}

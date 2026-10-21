package com.bookmyticket.transport.flight;

import com.bookmyticket.transport.flight.dto.FlightDtos;
import com.bookmyticket.transport.flight.dto.FlightOffer;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightSearchService flightSearchService;

    public FlightController(FlightSearchService flightSearchService) {
        this.flightSearchService = flightSearchService;
    }

    @GetMapping("/airports")
    public List<FlightDtos.AirportOption> airports(@RequestParam(name = "q", required = false) String query) {
        return flightSearchService.airports(query);
    }

    @PostMapping("/search")
    public FlightDtos.SearchResponse search(@Valid @RequestBody FlightDtos.SearchRequest request) {
        return flightSearchService.search(request);
    }

    @GetMapping("/offers/revalidate")
    public FlightOffer revalidate(@RequestParam("offerRef") String offerRef) {
        return flightSearchService.revalidate(offerRef);
    }
}

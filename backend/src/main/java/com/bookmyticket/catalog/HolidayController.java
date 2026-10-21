package com.bookmyticket.catalog;

import com.bookmyticket.catalog.dto.HolidayDtos;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/holidays")
public class HolidayController {

    private final HolidayCatalogService catalogService;

    public HolidayController(HolidayCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/home")
    public HolidayDtos.HomeResponse home() {
        return catalogService.home();
    }

    @GetMapping("/packages")
    public HolidayDtos.SearchResponse search(@RequestParam(required = false) String q,
                                             @RequestParam(required = false) String destination,
                                             @RequestParam(required = false) String category,
                                             @RequestParam(required = false) String kind,
                                             @RequestParam(required = false) Long maxPriceMinor,
                                             @RequestParam(required = false) Integer minDays,
                                             @RequestParam(required = false) Integer maxDays) {
        return catalogService.search(q, destination, category, kind, maxPriceMinor, minDays, maxDays);
    }

    @GetMapping("/packages/{slug}")
    public HolidayDtos.PackageDetail packageDetail(@PathVariable String slug) {
        return catalogService.packageDetail(slug);
    }

    @GetMapping("/destinations/{slug}")
    public HolidayDtos.DestinationDetail destinationDetail(@PathVariable String slug) {
        return catalogService.destinationDetail(slug);
    }
}

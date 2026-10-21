package com.bookmyticket.transport.train;

import com.bookmyticket.transport.train.dto.TrainDtos;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trains")
public class TrainController {

    private final TrainSearchService trainSearchService;

    public TrainController(TrainSearchService trainSearchService) {
        this.trainSearchService = trainSearchService;
    }

    @GetMapping("/stations")
    public List<TrainDtos.StationOption> stations(@RequestParam(name = "q", required = false) String query) {
        return trainSearchService.stations(query);
    }

    @PostMapping("/search")
    public TrainDtos.SearchResponse search(@Valid @RequestBody TrainDtos.SearchRequest request) {
        return trainSearchService.search(request);
    }
}

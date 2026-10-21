package com.bookmyticket.transport.mtc;

import com.bookmyticket.transport.mtc.dto.MtcDtos;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mtc")
public class MtcController {

    private final MtcService mtcService;

    public MtcController(MtcService mtcService) {
        this.mtcService = mtcService;
    }

    @GetMapping("/stops")
    public List<MtcDtos.StopOption> stops(@RequestParam(name = "q", required = false) String query) {
        return mtcService.stops(query);
    }

    @PostMapping("/plan")
    public MtcDtos.PlanResponse plan(@Valid @RequestBody MtcDtos.PlanRequest request) {
        return mtcService.plan(request);
    }
}

package com.example.seating.controller;

import com.example.seating.common.ApiResponse;
import com.example.seating.dto.FloorResponse;
import com.example.seating.dto.SeatResponse;
import com.example.seating.service.FloorService;
import com.example.seating.service.SeatService;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/floors")
public class FloorController {
    private final FloorService floorService;
    private final SeatService seatService;

    public FloorController(FloorService floorService, SeatService seatService) {
        this.floorService = floorService;
        this.seatService = seatService;
    }

    @GetMapping
    public ApiResponse<List<FloorResponse>> getFloors() {
        return ApiResponse.ok(floorService.getFloors());
    }

    @GetMapping("/{floorId}/seats")
    public ApiResponse<List<SeatResponse>> getSeatsByFloor(
            @PathVariable @Positive(message = "must be a positive integer") Integer floorId
    ) {
        return ApiResponse.ok(seatService.getSeatsByFloor(floorId));
    }
}

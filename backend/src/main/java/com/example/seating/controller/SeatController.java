package com.example.seating.controller;

import com.example.seating.common.ApiResponse;
import com.example.seating.dto.CreateSeatRequest;
import com.example.seating.dto.SeatResponse;
import com.example.seating.service.SeatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/seats")
public class SeatController {
    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping
    public ApiResponse<List<SeatResponse>> getSeats(
            @RequestParam @Positive(message = "must be a positive integer") Integer floorId
    ) {
        return ApiResponse.ok(seatService.getSeatsByFloor(floorId));
    }

    @PostMapping
    public ApiResponse<SeatResponse> createSeat(@Valid @RequestBody CreateSeatRequest request) {
        return ApiResponse.ok(seatService.createSeat(
                request.floorId(),
                request.seatNo()
        ));
    }

    @DeleteMapping("/{seatId}")
    public ApiResponse<Void> deleteSeat(
            @PathVariable @Positive(message = "must be a positive integer") Integer seatId
    ) {
        seatService.deleteSeat(seatId);
        return ApiResponse.ok(null);
    }
}

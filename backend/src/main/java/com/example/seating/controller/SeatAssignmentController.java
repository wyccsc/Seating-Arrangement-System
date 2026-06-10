package com.example.seating.controller;

import com.example.seating.common.ApiResponse;
import com.example.seating.dto.AssignSeatRequest;
import com.example.seating.dto.AssignmentResponse;
import com.example.seating.dto.ClearSeatRequest;
import com.example.seating.dto.PendingSeatChangeResponse;
import com.example.seating.dto.SubmitSeatAssignmentsRequest;
import com.example.seating.dto.SubmitSeatAssignmentsResponse;
import com.example.seating.service.SeatAssignmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seat-assignments")
public class SeatAssignmentController {
    private final SeatAssignmentService seatAssignmentService;

    public SeatAssignmentController(SeatAssignmentService seatAssignmentService) {
        this.seatAssignmentService = seatAssignmentService;
    }

    @PostMapping("/assign")
    public ApiResponse<PendingSeatChangeResponse> assignSeat(@Valid @RequestBody AssignSeatRequest request) {
        return ApiResponse.ok(seatAssignmentService.addPendingAssignment(request));
    }

    @PostMapping("/clear")
    public ApiResponse<PendingSeatChangeResponse> clearSeat(@Valid @RequestBody ClearSeatRequest request) {
        return ApiResponse.ok(seatAssignmentService.addPendingClear(request));
    }

    @PostMapping("/submit")
    public ApiResponse<SubmitSeatAssignmentsResponse> submitSeatAssignments(
            @Valid @RequestBody SubmitSeatAssignmentsRequest request
    ) {
        return ApiResponse.ok(seatAssignmentService.submit(request));
    }

    @PostMapping("/reset")
    public ApiResponse<PendingSeatChangeResponse> resetPending(
            @Valid @RequestBody SubmitSeatAssignmentsRequest request
    ) {
        return ApiResponse.ok(seatAssignmentService.resetPending(request.submittedBy()));
    }
}

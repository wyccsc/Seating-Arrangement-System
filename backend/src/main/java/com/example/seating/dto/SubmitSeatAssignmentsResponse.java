package com.example.seating.dto;

import java.util.List;

public record SubmitSeatAssignmentsResponse(
        String submittedBy,
        Integer clearedCount,
        Integer assignedCount,
        List<AssignmentResponse> clearedAssignments,
        List<AssignmentResponse> assignedSeats
) {
}

package com.example.seating.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitSeatAssignmentsRequest(
        @NotBlank(message = "is required")
        @Size(max = 50, message = "must be 50 characters or less")
        String submittedBy
) {
}

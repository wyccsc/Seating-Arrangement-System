package com.example.seating.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ClearSeatRequest(
        @NotNull(message = "is required")
        @Positive(message = "must be a positive integer")
        Integer seatId,

        @Pattern(regexp = "\\d{5}", message = "must be exactly 5 digits")
        String employeeNo,

        @NotBlank(message = "is required")
        @Size(max = 50, message = "must be 50 characters or less")
        String releasedBy
) {
}

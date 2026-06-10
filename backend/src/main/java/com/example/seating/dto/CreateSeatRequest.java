package com.example.seating.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateSeatRequest(
        @Positive
        Integer floorId,

        @NotBlank
        @Size(min = 1, max = 20)
        String seatNo
) {
}

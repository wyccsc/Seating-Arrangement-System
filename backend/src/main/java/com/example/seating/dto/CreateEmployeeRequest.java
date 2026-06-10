package com.example.seating.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateEmployeeRequest(
        @NotBlank
        @Size(min = 5, max = 5)
        @Pattern(regexp = "\\d{5}")
        String employeeNo,

        @NotBlank
        @Size(min = 1, max = 50)
        String employeeName
) {
}

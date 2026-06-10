package com.example.seating.dto;

public record EmployeeResponse(
        Integer employeeId,
        String employeeNo,
        String employeeName,
        Boolean active
) {
}

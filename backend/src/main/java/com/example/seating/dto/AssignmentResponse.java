package com.example.seating.dto;

import java.time.LocalDateTime;

public record AssignmentResponse(
        Integer assignmentId,
        Integer seatId,
        String seatNo,
        Integer floorId,
        String floorName,
        Integer employeeId,
        String employeeNo,
        String employeeName,
        String assignmentStatus,
        LocalDateTime assignedAt
) {
}

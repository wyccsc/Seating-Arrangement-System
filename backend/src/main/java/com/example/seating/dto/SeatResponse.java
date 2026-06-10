package com.example.seating.dto;

public record SeatResponse(
        Integer seatId,
        Integer floorId,
        String floorName,
        String seatNo,
        String seatStatus,
        Boolean active,
        Integer employeeId,
        String employeeNo,
        String employeeName
) {
}

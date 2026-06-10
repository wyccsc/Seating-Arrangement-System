package com.example.seating.dto;

public record FloorResponse(
        Integer floorId,
        String floorName,
        Integer displayOrder,
        Boolean active
) {
}

package com.example.seating.dto;

public record PendingSeatChangeResponse(
        String operator,
        Integer pendingAssignCount,
        Integer pendingClearCount
) {
}

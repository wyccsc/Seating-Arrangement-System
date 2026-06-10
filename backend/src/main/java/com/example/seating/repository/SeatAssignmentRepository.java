package com.example.seating.repository;

import com.example.seating.dto.AssignmentResponse;
import com.example.seating.dto.ClearSeatRequest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;

@Repository
public class SeatAssignmentRepository {
    private final JdbcTemplate jdbcTemplate;

    public SeatAssignmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AssignmentResponse assignSeat(Integer seatId, String employeeNo, String assignedBy) {
        List<AssignmentResponse> results = jdbcTemplate.query("EXEC dbo.sp_assign_seat ?, ?, ?", ps -> {
            ps.setInt(1, seatId);
            ps.setString(2, employeeNo);
            ps.setString(3, assignedBy);
        }, (rs, rowNum) -> new AssignmentResponse(
                rs.getInt("assignment_id"),
                rs.getInt("seat_id"),
                rs.getString("seat_no"),
                rs.getInt("floor_id"),
                rs.getString("floor_name"),
                rs.getInt("employee_id"),
                rs.getString("employee_no"),
                rs.getString("employee_name"),
                rs.getString("assignment_status"),
                rs.getTimestamp("assigned_at").toLocalDateTime()
        ));
        return results.stream()
                .findFirst()
                .orElseThrow(() -> new EmptyResultDataAccessException(1));
    }

    public AssignmentResponse clearSeat(ClearSeatRequest request) {
        List<AssignmentResponse> results = jdbcTemplate.query("EXEC dbo.sp_clear_seat ?, ?, ?", ps -> {
            if (request.seatId() == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, request.seatId());
            }
            ps.setString(2, request.employeeNo());
            ps.setString(3, request.releasedBy());
        }, (rs, rowNum) -> new AssignmentResponse(
                rs.getInt("assignment_id"),
                rs.getInt("seat_id"),
                null,
                null,
                null,
                rs.getInt("employee_id"),
                null,
                null,
                rs.getString("assignment_status"),
                null
        ));
        return results.stream()
                .findFirst()
                .orElseThrow(() -> new EmptyResultDataAccessException(1));
    }
}

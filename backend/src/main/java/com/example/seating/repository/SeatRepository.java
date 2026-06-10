package com.example.seating.repository;

import com.example.seating.dto.SeatResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;

@Repository
public class SeatRepository {
    private final JdbcTemplate jdbcTemplate;

    public SeatRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SeatResponse> findSeats(Integer floorId) {
        return jdbcTemplate.query("EXEC dbo.sp_get_seats ?", ps -> {
            if (floorId == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, floorId);
            }
        }, (rs, rowNum) -> new SeatResponse(
                rs.getInt("seat_id"),
                rs.getInt("floor_id"),
                rs.getString("floor_name"),
                rs.getString("seat_no"),
                rs.getString("seat_status"),
                rs.getBoolean("is_active"),
                rs.getObject("employee_id", Integer.class),
                rs.getString("employee_no"),
                rs.getString("employee_name")
        ));
    }

    public SeatResponse createSeat(Integer floorId, String seatNo) {
        String insertSql = "INSERT INTO seats (floor_id, seat_no, seat_status, is_active) " +
                "VALUES (?, ?, 'AVAILABLE', 1)";

        jdbcTemplate.update(insertSql, ps -> {
            ps.setInt(1, floorId);
            ps.setString(2, seatNo);
        });

        String selectSql = "SELECT s.seat_id, s.floor_id, f.floor_name, s.seat_no, s.seat_status, " +
                "s.is_active, NULL as employee_id, NULL as employee_no, NULL as employee_name " +
                "FROM seats s INNER JOIN floors f ON f.floor_id = s.floor_id " +
                "WHERE s.seat_no = ? AND s.floor_id = ?";

        return jdbcTemplate.queryForObject(
                selectSql,
                new Object[]{seatNo, floorId},
                (rs, rowNum) -> new SeatResponse(
                        rs.getInt("seat_id"),
                        rs.getInt("floor_id"),
                        rs.getString("floor_name"),
                        rs.getString("seat_no"),
                        rs.getString("seat_status"),
                        rs.getBoolean("is_active"),
                        rs.getObject("employee_id", Integer.class),
                        rs.getString("employee_no"),
                        rs.getString("employee_name")
                )
        );
    }

    public void deleteSeat(Integer seatId) {
        jdbcTemplate.update("EXEC dbo.sp_delete_seat ?", seatId);
    }
}

package com.example.seating.repository;

import com.example.seating.dto.FloorResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FloorRepository {
    private final JdbcTemplate jdbcTemplate;

    public FloorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<FloorResponse> findAllActive() {
        return jdbcTemplate.query("EXEC dbo.sp_get_floors", (rs, rowNum) -> new FloorResponse(
                rs.getInt("floor_id"),
                rs.getString("floor_name"),
                rs.getInt("display_order"),
                rs.getBoolean("is_active")
        ));
    }
}

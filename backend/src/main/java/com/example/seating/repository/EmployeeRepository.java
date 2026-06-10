package com.example.seating.repository;

import com.example.seating.dto.EmployeeResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EmployeeRepository {
    private final JdbcTemplate jdbcTemplate;

    public EmployeeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<EmployeeResponse> findEmployees(String keyword) {
        return jdbcTemplate.query("EXEC dbo.sp_get_employees ?", ps -> ps.setString(1, keyword),
                (rs, rowNum) -> new EmployeeResponse(
                        rs.getInt("employee_id"),
                        rs.getString("employee_no"),
                        rs.getString("employee_name"),
                        rs.getBoolean("is_active")
                ));
    }

    public EmployeeResponse createEmployee(String employeeNo, String employeeName) {
        String insertSql = "INSERT INTO employees (employee_no, employee_name, is_active) VALUES (?, ?, 1)";

        jdbcTemplate.update(insertSql, ps -> {
            ps.setString(1, employeeNo);
            ps.setString(2, employeeName);
        });

        String selectSql = "SELECT employee_id, employee_no, employee_name, is_active " +
                "FROM employees WHERE employee_no = ?";

        return jdbcTemplate.queryForObject(
                selectSql,
                new Object[]{employeeNo},
                (rs, rowNum) -> new EmployeeResponse(
                        rs.getInt("employee_id"),
                        rs.getString("employee_no"),
                        rs.getString("employee_name"),
                        rs.getBoolean("is_active")
                )
        );
    }

    public void deleteEmployee(String employeeNo) {
        jdbcTemplate.update("EXEC dbo.sp_delete_employee ?", employeeNo);
    }
}

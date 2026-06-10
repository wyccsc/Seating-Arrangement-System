package com.example.seating.controller;

import com.example.seating.common.ApiResponse;
import com.example.seating.dto.CreateEmployeeRequest;
import com.example.seating.dto.EmployeeResponse;
import com.example.seating.service.EmployeeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ApiResponse<List<EmployeeResponse>> getEmployees(
            @RequestParam(required = false) @Size(max = 50) String keyword
    ) {
        return ApiResponse.ok(employeeService.getEmployees(keyword));
    }

    @PostMapping
    public ApiResponse<EmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        return ApiResponse.ok(employeeService.createEmployee(
                request.employeeNo(),
                request.employeeName()
        ));
    }

    @DeleteMapping("/{employeeNo}")
    public ApiResponse<Void> deleteEmployee(
            @PathVariable
            @Pattern(regexp = "\\d{5}")
            String employeeNo
    ) {
        employeeService.deleteEmployee(employeeNo);
        return ApiResponse.ok(null);
    }
}

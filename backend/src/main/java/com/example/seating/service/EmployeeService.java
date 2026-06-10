package com.example.seating.service;

import com.example.seating.dto.EmployeeResponse;
import com.example.seating.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<EmployeeResponse> getEmployees(String keyword) {
        String normalizedKeyword = keyword == null || keyword.isBlank()
                ? null
                : keyword.trim();
        return employeeRepository.findEmployees(normalizedKeyword);
    }

    public EmployeeResponse createEmployee(String employeeNo, String employeeName) {
        return employeeRepository.createEmployee(employeeNo, employeeName);
    }

    public void deleteEmployee(String employeeNo) {
        employeeRepository.deleteEmployee(employeeNo);
    }
}

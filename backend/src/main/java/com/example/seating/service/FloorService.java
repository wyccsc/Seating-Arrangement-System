package com.example.seating.service;

import com.example.seating.dto.FloorResponse;
import com.example.seating.repository.FloorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FloorService {
    private final FloorRepository floorRepository;

    public FloorService(FloorRepository floorRepository) {
        this.floorRepository = floorRepository;
    }

    public List<FloorResponse> getFloors() {
        return floorRepository.findAllActive();
    }
}

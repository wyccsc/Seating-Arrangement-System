package com.example.seating.service;

import com.example.seating.common.ErrorCode;
import com.example.seating.dto.SeatResponse;
import com.example.seating.exception.BusinessException;
import com.example.seating.repository.SeatRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatService {
    private final SeatRepository seatRepository;

    public SeatService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    public List<SeatResponse> getSeatsByFloor(Integer floorId) {
        if (floorId == null || floorId <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "floorId must be a positive integer.");
        }
        return seatRepository.findSeats(floorId);
    }

    public SeatResponse createSeat(Integer floorId, String seatNo) {
        if (floorId == null || floorId <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "floorId must be a positive integer.");
        }
        return seatRepository.createSeat(floorId, seatNo);
    }

    public void deleteSeat(Integer seatId) {
        if (seatId == null || seatId <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "seatId must be a positive integer.");
        }
        seatRepository.deleteSeat(seatId);
    }
}

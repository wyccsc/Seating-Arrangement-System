package com.example.seating.service;

import com.example.seating.common.ErrorCode;
import com.example.seating.dto.AssignSeatRequest;
import com.example.seating.dto.AssignmentResponse;
import com.example.seating.dto.ClearSeatRequest;
import com.example.seating.dto.PendingSeatChangeResponse;
import com.example.seating.dto.SeatResponse;
import com.example.seating.dto.SubmitSeatAssignmentsRequest;
import com.example.seating.dto.SubmitSeatAssignmentsResponse;
import com.example.seating.exception.BusinessException;
import com.example.seating.repository.SeatAssignmentRepository;
import com.example.seating.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SeatAssignmentService {
    private final SeatAssignmentRepository seatAssignmentRepository;
    private final SeatRepository seatRepository;
    private final Map<String, SeatAssignmentDraft> drafts = new ConcurrentHashMap<>();

    public SeatAssignmentService(
            SeatAssignmentRepository seatAssignmentRepository,
            SeatRepository seatRepository
    ) {
        this.seatAssignmentRepository = seatAssignmentRepository;
        this.seatRepository = seatRepository;
    }

    public PendingSeatChangeResponse addPendingAssignment(AssignSeatRequest request) {
        SeatAssignmentDraft draft = getDraft(request.assignedBy());

        synchronized (draft) {
            List<SeatResponse> currentSeats = seatRepository.findSeats(null);
            SeatResponse targetSeat = findSeat(currentSeats, request.seatId());

            if (!"AVAILABLE".equals(targetSeat.seatStatus())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Seat is not available.");
            }

            if (isEmployeeAlreadyAssigned(currentSeats, request.employeeNo(), draft)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Employee already has an active seat.");
            }

            if (draft.pendingAssignmentsByEmployeeNo.containsKey(request.employeeNo())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Employee already has a pending seat.");
            }

            if (draft.pendingAssignmentsBySeatId.containsKey(request.seatId())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Seat already has a pending employee.");
            }

            PendingAssignment pendingAssignment = new PendingAssignment(
                    request.seatId(),
                    request.employeeNo(),
                    request.assignedBy()
            );
            draft.pendingAssignmentsBySeatId.put(request.seatId(), pendingAssignment);
            draft.pendingAssignmentsByEmployeeNo.put(request.employeeNo(), pendingAssignment);
            return toPendingResponse(request.assignedBy(), draft);
        }
    }

    public PendingSeatChangeResponse addPendingClear(ClearSeatRequest request) {
        if (request.seatId() == null && request.employeeNo() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "seatId or employeeNo is required.");
        }

        SeatAssignmentDraft draft = getDraft(request.releasedBy());

        synchronized (draft) {
            List<SeatResponse> currentSeats = seatRepository.findSeats(null);
            SeatResponse targetSeat = findSeatForClear(currentSeats, request);

            PendingAssignment pendingAssignment = draft.pendingAssignmentsBySeatId.remove(targetSeat.seatId());
            if (pendingAssignment != null) {
                draft.pendingAssignmentsByEmployeeNo.remove(pendingAssignment.employeeNo());
                return toPendingResponse(request.releasedBy(), draft);
            }

            if (draft.pendingClearSeatIds.remove(targetSeat.seatId()) != null) {
                return toPendingResponse(request.releasedBy(), draft);
            }

            if (targetSeat.employeeNo() == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Seat has no active employee.");
            }

            draft.pendingClearSeatIds.put(targetSeat.seatId(), targetSeat.seatId());
            return toPendingResponse(request.releasedBy(), draft);
        }
    }

    public PendingSeatChangeResponse resetPending(String operator) {
        String normalizedOperator = normalizeOperator(operator);
        drafts.remove(normalizedOperator);
        return new PendingSeatChangeResponse(normalizedOperator, 0, 0);
    }

    @Transactional(rollbackFor = Exception.class)
    public SubmitSeatAssignmentsResponse submit(SubmitSeatAssignmentsRequest request) {
        SeatAssignmentDraft draft = drafts.get(normalizeOperator(request.submittedBy()));
        if (draft == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "No pending seat changes.");
        }

        synchronized (draft) {
            if (draft.pendingClearSeatIds.isEmpty() && draft.pendingAssignmentsBySeatId.isEmpty()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "No pending seat changes.");
            }

            List<AssignmentResponse> clearedAssignments = new ArrayList<>();
            for (Integer seatId : draft.pendingClearSeatIds.keySet()) {
                clearedAssignments.add(seatAssignmentRepository.clearSeat(
                        new ClearSeatRequest(seatId, null, request.submittedBy())
                ));
            }

            List<AssignmentResponse> assignedSeats = new ArrayList<>();
            for (PendingAssignment pendingAssignment : draft.pendingAssignmentsBySeatId.values()) {
                assignedSeats.add(seatAssignmentRepository.assignSeat(
                        pendingAssignment.seatId(),
                        pendingAssignment.employeeNo(),
                        request.submittedBy()
                ));
            }

            SubmitSeatAssignmentsResponse response = new SubmitSeatAssignmentsResponse(
                    request.submittedBy(),
                    clearedAssignments.size(),
                    assignedSeats.size(),
                    clearedAssignments,
                    assignedSeats
            );
            removeDraftAfterCommit(request.submittedBy());
            return response;
        }
    }

    private void removeDraftAfterCommit(String operator) {
        String normalizedOperator = normalizeOperator(operator);

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            drafts.remove(normalizedOperator);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                drafts.remove(normalizedOperator);
            }
        });
    }

    private SeatAssignmentDraft getDraft(String operator) {
        return drafts.computeIfAbsent(normalizeOperator(operator), key -> new SeatAssignmentDraft());
    }

    private String normalizeOperator(String operator) {
        if (operator == null || operator.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "operator is required.");
        }
        return operator.trim();
    }

    private SeatResponse findSeat(List<SeatResponse> seats, Integer seatId) {
        return seats.stream()
                .filter(seat -> seat.seatId().equals(seatId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Seat does not exist."));
    }

    private SeatResponse findSeatForClear(List<SeatResponse> seats, ClearSeatRequest request) {
        return seats.stream()
                .filter(seat -> request.seatId() == null || seat.seatId().equals(request.seatId()))
                .filter(seat -> request.employeeNo() == null || request.employeeNo().equals(seat.employeeNo()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Active seat assignment was not found."));
    }

    private boolean isEmployeeAlreadyAssigned(
            List<SeatResponse> currentSeats,
            String employeeNo,
            SeatAssignmentDraft draft
    ) {
        return currentSeats.stream()
                .filter(seat -> employeeNo.equals(seat.employeeNo()))
                .anyMatch(seat -> !draft.pendingClearSeatIds.containsKey(seat.seatId()));
    }

    private PendingSeatChangeResponse toPendingResponse(String operator, SeatAssignmentDraft draft) {
        return new PendingSeatChangeResponse(
                operator,
                draft.pendingAssignmentsBySeatId.size(),
                draft.pendingClearSeatIds.size()
        );
    }

    private static class SeatAssignmentDraft {
        private final Map<Integer, PendingAssignment> pendingAssignmentsBySeatId = new LinkedHashMap<>();
        private final Map<String, PendingAssignment> pendingAssignmentsByEmployeeNo = new LinkedHashMap<>();
        private final Map<Integer, Integer> pendingClearSeatIds = new LinkedHashMap<>();
    }

    private record PendingAssignment(
            Integer seatId,
            String employeeNo,
            String assignedBy
    ) {
    }
}

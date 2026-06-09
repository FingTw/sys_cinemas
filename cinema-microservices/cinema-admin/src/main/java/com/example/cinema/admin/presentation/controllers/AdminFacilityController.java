package com.example.cinema.admin.presentation.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.cinema.admin.application.ports.in.AdminFacilityUseCase;
import com.example.cinema.admin.application.dto.RoomDTO;
import com.example.cinema.admin.application.dto.SeatDTO;
import com.example.cinema.admin.application.dto.CreateRoomRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/admin/facilities")
@RequiredArgsConstructor
@Slf4j
public class AdminFacilityController {

    private final AdminFacilityUseCase adminFacilityUseCase;

    @PreAuthorize("hasAuthority('FACILITY_CREATE')")
    @PostMapping("/rooms")
    public ResponseEntity<RoomDTO> createRoom(@RequestBody CreateRoomRequest request) {
        log.info("Admin creating room [{}] with {}x{} grid", request.getName(), request.getGridRows(), request.getGridCols());
        return ResponseEntity.ok(adminFacilityUseCase.createCustomRoom(
                request.getName(), request.getGridRows(), request.getGridCols(), request.getSeats()));
    }

    @PreAuthorize("hasAuthority('FACILITY_READ')")
    @GetMapping("/rooms")
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        log.info("Fetching all rooms for admin");
        return ResponseEntity.ok(adminFacilityUseCase.getAllRooms());
    }

    @PreAuthorize("hasAuthority('FACILITY_DELETE')")
    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable String id) {
        log.info("Admin deleting room ID: [{}]", id);
        adminFacilityUseCase.deleteRoom(id);
        return ResponseEntity.ok(Map.of("message", "Room and its seats deleted successfully"));
    }

    @PreAuthorize("hasAuthority('FACILITY_READ')")
    @GetMapping("/rooms/{roomId}/seats")
    public ResponseEntity<List<SeatDTO>> getSeatsByRoom(@PathVariable String roomId) {
        log.info("Fetching seat layout for room [{}]", roomId);
        return ResponseEntity.ok(adminFacilityUseCase.getSeatsByRoom(roomId));
    }

    @PreAuthorize("hasAuthority('FACILITY_UPDATE')")
    @PutMapping("/seats/{seatId}")
    public ResponseEntity<SeatDTO> updateSeat(@PathVariable String seatId, @RequestBody Map<String, String> body) {
        String type = body.get("type");
        String status = body.get("status");
        log.info("Admin updating seat ID [{}]: type={}, status={}", seatId, type, status);
        return ResponseEntity.ok(adminFacilityUseCase.updateSeat(seatId, type, status));
    }
}

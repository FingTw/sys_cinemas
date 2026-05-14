package com.example.cinema.presentation.controllers;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cinema.application.dto.CreateRoomRequest;
import com.example.cinema.application.dto.RoomDTO;
import com.example.cinema.application.dto.SeatDTO;
import com.example.cinema.application.usecases.FacilityUseCase;

@RestController
@RequestMapping("/api/v1/admin/facilities")
public class FacilityController {

    private static final Logger log = LoggerFactory.getLogger(FacilityController.class);

    private final FacilityUseCase facilityUseCase;

    public FacilityController(FacilityUseCase facilityUseCase) {
        this.facilityUseCase = facilityUseCase;
    }

    @PreAuthorize("hasAuthority('FACILITY_CREATE')")
    @PostMapping("/rooms")
    public ResponseEntity<RoomDTO> createRoom(@RequestBody CreateRoomRequest request) {
        log.info("Admin requested to create custom room [{}] with grid {}x{}, containing {} seats",
                request.getName(), request.getGridRows(), request.getGridCols(), request.getSeats().size());
        try {
            return ResponseEntity.ok(facilityUseCase.createCustomRoom(
                    request.getName(), request.getGridRows(), request.getGridCols(), request.getSeats()));
        } catch (Exception e) {
            log.error("Failed to create room [{}]: {}", request.getName(), e.getMessage());
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('FACILITY_READ')")
    @GetMapping("/rooms")
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        log.info("[GET /rooms] - Fetching all rooms");
        return ResponseEntity.ok(facilityUseCase.getAllRooms());
    }

    @PreAuthorize("hasAuthority('FACILITY_DELETE')")
    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable String id) {
        log.info("[DELETE /rooms/{}] - Admin requested to delete room", id);
        try {
            facilityUseCase.deleteRoom(id);
            log.info("Successfully deleted room [{}]", id);
            return ResponseEntity.ok(Map.of("message", "Room and its seats deleted successfully"));
        } catch (Exception e) {
            log.error("Failed to delete room [{}]: {}", id, e.getMessage());
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('FACILITY_READ')")
    @GetMapping("/rooms/{roomId}/seats")
    public ResponseEntity<List<SeatDTO>> getSeatsByRoom(@PathVariable String roomId) {
        log.info("[GET /rooms/{}/seats] - Fetching seat map for room [{}]", roomId);
        return ResponseEntity.ok(facilityUseCase.getSeatsByRoom(roomId));
    }

    @PreAuthorize("hasAuthority('FACILITY_UPDATE')")
    @PutMapping("/seats/{seatId}")
    public ResponseEntity<SeatDTO> updateSeat(@PathVariable String seatId, @RequestBody Map<String, String> body) {
        String type = body.get("type");
        String status = body.get("status");
        log.info("Admin updating seat ID [{}]: type={}, status={}", seatId, type, status);
        try {
            return ResponseEntity.ok(facilityUseCase.updateSeat(seatId, type, status));
        } catch (Exception e) {
            log.error("Failed to update seat [{}]: {}", seatId, e.getMessage());
            throw e;
        }
    }
}

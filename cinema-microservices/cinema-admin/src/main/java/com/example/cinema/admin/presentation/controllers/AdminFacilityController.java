package com.example.cinema.admin.presentation.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final StringRedisTemplate redisTemplate;

    private void clearFacilityCache() {
        try {
            // 1. Xóa cache Gateway aggregation BFF
            redisTemplate.delete("cache:home-overview");

            // 2. Xóa các cache Phòng chiếu của Facility Service
            java.util.Set<String> roomsKeys = redisTemplate.keys("rooms::*");
            if (roomsKeys != null && !roomsKeys.isEmpty()) {
                redisTemplate.delete(roomsKeys);
                log.info("[CINEMA-ADMIN] Evicted rooms cache keys: {}", roomsKeys);
            }

            java.util.Set<String> roomKeys = redisTemplate.keys("room::*");
            if (roomKeys != null && !roomKeys.isEmpty()) {
                redisTemplate.delete(roomKeys);
                log.info("[CINEMA-ADMIN] Evicted room detail cache keys: {}", roomKeys);
            }

            // 3. Xóa các cache Ghế ngồi của Facility Service
            java.util.Set<String> seatsKeys = redisTemplate.keys("seats::*");
            if (seatsKeys != null && !seatsKeys.isEmpty()) {
                redisTemplate.delete(seatsKeys);
                log.info("[CINEMA-ADMIN] Evicted seats cache keys: {}", seatsKeys);
            }
        } catch (Exception e) {
            log.warn("[CINEMA-ADMIN] Failed to clear facility caches: {}", e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('FACILITY_CREATE')")
    @PostMapping("/rooms")
    public ResponseEntity<RoomDTO> createRoom(@RequestBody CreateRoomRequest request) {
        log.info("Admin creating room [{}] with {}x{} grid for cinema [{}]", request.getName(), request.getGridRows(), request.getGridCols(), request.getCinemaId());
        RoomDTO response = adminFacilityUseCase.createCustomRoom(
                request.getName(), request.getGridRows(), request.getGridCols(), request.getSeats(), request.getCinemaId());
        clearFacilityCache();
        return ResponseEntity.ok(response);
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
        clearFacilityCache();
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
        SeatDTO response = adminFacilityUseCase.updateSeat(seatId, type, status);
        clearFacilityCache();
        return ResponseEntity.ok(response);
    }
}

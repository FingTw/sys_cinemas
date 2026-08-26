package com.example.cinema.admin.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.example.cinema.admin.services.AdminCinemaUseCaseImpl;
import com.example.cinema.admin.dto.CinemaComplexDTO;
import com.example.cinema.admin.dto.CinemaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/admin/facilities")
@RequiredArgsConstructor
@Slf4j
public class AdminCinemaController {

    private final AdminCinemaUseCaseImpl adminCinemaUseCase;
    private final StringRedisTemplate redisTemplate;

    private void clearCinemaCache() {
        try {
            // 1. Xóa cache Gateway aggregation BFF
            redisTemplate.delete("cache:home-overview");

            // 2. Xóa các cache Cụm rạp
            java.util.Set<String> complexKeys = redisTemplate.keys("cinemaComplexes::*");
            if (complexKeys != null && !complexKeys.isEmpty()) {
                redisTemplate.delete(complexKeys);
            }

            // 3. Xóa các cache Rạp phim
            java.util.Set<String> cinemaKeys = redisTemplate.keys("cinemas::*");
            if (cinemaKeys != null && !cinemaKeys.isEmpty()) {
                redisTemplate.delete(cinemaKeys);
            }
            java.util.Set<String> cinemasByComplexKeys = redisTemplate.keys("cinemasByComplex::*");
            if (cinemasByComplexKeys != null && !cinemasByComplexKeys.isEmpty()) {
                redisTemplate.delete(cinemasByComplexKeys);
            }
            java.util.Set<String> cinemaDetailKeys = redisTemplate.keys("cinema::*");
            if (cinemaDetailKeys != null && !cinemaDetailKeys.isEmpty()) {
                redisTemplate.delete(cinemaDetailKeys);
            }

            // 4. Xóa cache phòng chiếu để cập nhật thông tin rạp đính kèm
            java.util.Set<String> roomsKeys = redisTemplate.keys("rooms::*");
            if (roomsKeys != null && !roomsKeys.isEmpty()) {
                redisTemplate.delete(roomsKeys);
            }
            java.util.Set<String> roomKeys = redisTemplate.keys("room::*");
            if (roomKeys != null && !roomKeys.isEmpty()) {
                redisTemplate.delete(roomKeys);
            }
        } catch (Exception e) {
            log.warn("[CINEMA-ADMIN] Failed to clear cinema caches: {}", e.getMessage());
        }
    }

    // --- COMPLEX API ---

    @PreAuthorize("hasAuthority('FACILITY_CREATE')")
    @PostMapping("/complexes")
    public ResponseEntity<CinemaComplexDTO> createComplex(@RequestBody CinemaComplexDTO request) {
        log.info("Admin creating complex [{}]", request.getName());
        CinemaComplexDTO response = adminCinemaUseCase.createComplex(request);
        clearCinemaCache();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('FACILITY_READ')")
    @GetMapping("/complexes")
    public ResponseEntity<List<CinemaComplexDTO>> getAllComplexes() {
        log.info("Fetching all complexes for admin");
        return ResponseEntity.ok(adminCinemaUseCase.getAllComplexes());
    }

    @PreAuthorize("hasAuthority('FACILITY_UPDATE')")
    @PutMapping("/complexes/{id}")
    public ResponseEntity<CinemaComplexDTO> updateComplex(@PathVariable String id, @RequestBody CinemaComplexDTO request) {
        log.info("Admin updating complex ID: [{}]", id);
        CinemaComplexDTO response = adminCinemaUseCase.updateComplex(id, request);
        clearCinemaCache();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('FACILITY_DELETE')")
    @DeleteMapping("/complexes/{id}")
    public ResponseEntity<?> deleteComplex(@PathVariable String id) {
        log.info("Admin deleting complex ID: [{}]", id);
        adminCinemaUseCase.deleteComplex(id);
        clearCinemaCache();
        return ResponseEntity.ok(Map.of("message", "Complex deleted successfully"));
    }

    // --- CINEMA API ---

    @PreAuthorize("hasAuthority('FACILITY_CREATE')")
    @PostMapping("/cinemas")
    public ResponseEntity<CinemaDTO> createCinema(@RequestBody CinemaDTO request) {
        log.info("Admin creating cinema [{}]", request.getName());
        CinemaDTO response = adminCinemaUseCase.createCinema(request);
        clearCinemaCache();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('FACILITY_READ')")
    @GetMapping("/cinemas")
    public ResponseEntity<List<CinemaDTO>> getAllCinemas() {
        log.info("Fetching all cinemas for admin");
        return ResponseEntity.ok(adminCinemaUseCase.getAllCinemas());
    }

    @PreAuthorize("hasAuthority('FACILITY_UPDATE')")
    @PutMapping("/cinemas/{id}")
    public ResponseEntity<CinemaDTO> updateCinema(@PathVariable String id, @RequestBody CinemaDTO request) {
        log.info("Admin updating cinema ID: [{}]", id);
        CinemaDTO response = adminCinemaUseCase.updateCinema(id, request);
        clearCinemaCache();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('FACILITY_DELETE')")
    @DeleteMapping("/cinemas/{id}")
    public ResponseEntity<?> deleteCinema(@PathVariable String id) {
        log.info("Admin deleting cinema ID: [{}]", id);
        adminCinemaUseCase.deleteCinema(id);
        clearCinemaCache();
        return ResponseEntity.ok(Map.of("message", "Cinema deleted successfully"));
    }
}

package com.example.cinema.facility.presentation.controllers;

import com.example.cinema.facility.application.ports.in.FacilityService;
import com.example.cinema.facility.application.dto.RoomDTO;
import com.example.cinema.facility.application.dto.SeatDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final FacilityService facilityService;

    public RoomController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @GetMapping
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        return ResponseEntity.ok(facilityService.getAllRooms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable String id) {
        return ResponseEntity.ok(facilityService.getRoomById(id));
    }

    @GetMapping("/{roomId}/seats")
    public ResponseEntity<List<SeatDTO>> getSeatsByRoom(@PathVariable String roomId) {
        return ResponseEntity.ok(facilityService.getSeatsByRoom(roomId));
    }

    @GetMapping("/seats/{id}")
    public ResponseEntity<SeatDTO> getSeatById(@PathVariable String id) {
        return ResponseEntity.ok(facilityService.getSeatById(id));
    }
}

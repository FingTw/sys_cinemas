package com.example.cinema.facility.presentation.controllers;

import com.example.cinema.facility.application.dto.RoomDTO;
import com.example.cinema.facility.application.dto.SeatDTO;
import com.example.cinema.facility.application.usecases.FacilityUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final FacilityUseCase facilityUseCase;

    public RoomController(FacilityUseCase facilityUseCase) {
        this.facilityUseCase = facilityUseCase;
    }

    @GetMapping
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        return ResponseEntity.ok(facilityUseCase.getAllRooms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable String id) {
        return ResponseEntity.ok(facilityUseCase.getRoomById(id));
    }

    @GetMapping("/{roomId}/seats")
    public ResponseEntity<List<SeatDTO>> getSeatsByRoom(@PathVariable String roomId) {
        return ResponseEntity.ok(facilityUseCase.getSeatsByRoom(roomId));
    }

    @GetMapping("/seats/{id}")
    public ResponseEntity<SeatDTO> getSeatById(@PathVariable String id) {
        return ResponseEntity.ok(facilityUseCase.getSeatById(id));
    }
}

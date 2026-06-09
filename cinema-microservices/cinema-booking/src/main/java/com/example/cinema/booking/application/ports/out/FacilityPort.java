package com.example.cinema.booking.application.ports.out;

import com.example.cinema.booking.application.dto.feign.SeatDTO;
import java.util.List;
import java.util.Optional;

public interface FacilityPort {
    Optional<SeatDTO> getSeatById(String id);
    List<SeatDTO> getSeatsByRoomId(String roomId);
}

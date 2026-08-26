package com.example.cinema.booking.application.port;

import com.example.cinema.booking.application.dto.SeatDTO;
import java.util.List;
import java.util.Optional;

public interface FacilityClientPort {
    Optional<SeatDTO> getSeatById(String id);
    List<SeatDTO> getSeatsByRoomId(String roomId);
}

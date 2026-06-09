package com.example.cinema.scheduling.application.ports.out;

import com.example.cinema.scheduling.application.dto.feign.RoomDTO;
import java.util.Optional;

public interface FacilityPort {
    Optional<RoomDTO> getRoomById(String id);
}

package com.example.cinema.booking.application.ports.out;

import com.example.cinema.booking.application.dto.feign.ShowtimeDTO;
import java.util.Optional;

public interface ShowtimePort {
    Optional<ShowtimeDTO> getShowtimeById(String id);
}

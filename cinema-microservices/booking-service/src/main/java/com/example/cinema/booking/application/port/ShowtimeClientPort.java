package com.example.cinema.booking.application.port;

import com.example.cinema.booking.application.dto.ShowtimeDTO;
import java.util.Optional;

public interface ShowtimeClientPort {
    Optional<ShowtimeDTO> getShowtimeById(String id);
}

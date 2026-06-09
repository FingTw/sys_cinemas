package com.example.cinema.scheduling.application.ports.in;

import com.example.cinema.scheduling.application.dto.ShowtimeDTO;
import java.util.List;

public interface ShowtimeService {
    List<ShowtimeDTO> getAllShowtimes();
    ShowtimeDTO getShowtimeById(String id);
    List<ShowtimeDTO> getShowtimesByMovie(String movieId);
}

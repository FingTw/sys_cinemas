package com.example.cinema.admin.services;

import com.example.cinema.admin.dto.ShowtimeDTO;
import com.example.cinema.admin.dto.ShowtimeRequest;
import java.util.List;

public interface AdminShowtimeUseCase {
    ShowtimeDTO createShowtime(ShowtimeRequest request);
    List<ShowtimeDTO> getAllShowtimes();
    void cancelShowtime(String id);
    void deleteShowtime(String id);
}

package com.example.cinema.admin.application.ports.in;

import com.example.cinema.admin.application.dto.ShowtimeDTO;
import com.example.cinema.admin.application.dto.ShowtimeRequest;
import java.util.List;

public interface AdminShowtimeUseCase {
    ShowtimeDTO createShowtime(ShowtimeRequest request);
    List<ShowtimeDTO> getAllShowtimes();
    void cancelShowtime(String id);
    void deleteShowtime(String id);
}

package com.example.cinema.booking.adapter.feign.clients;

import com.example.cinema.booking.application.dto.ShowtimeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Optional;

@FeignClient(name = "cinema-scheduling", url = "${app.services.scheduling.url}")
public interface ShowtimeClient {
    @GetMapping("/api/v1/showtimes/{id}")
    Optional<ShowtimeDTO> getShowtimeById(@PathVariable("id") String id);
}

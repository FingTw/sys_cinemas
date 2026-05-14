package com.example.cinema.booking.infrastructure.feign;

import com.example.cinema.booking.application.dto.feign.ShowtimeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Optional;

@FeignClient(name = "cinema-scheduling", url = "${app.services.scheduling.url:http://localhost:8084}")
public interface ShowtimeClient {
    @GetMapping("/api/v1/showtimes/{id}")
    Optional<ShowtimeDTO> getShowtimeById(@PathVariable("id") String id);
}

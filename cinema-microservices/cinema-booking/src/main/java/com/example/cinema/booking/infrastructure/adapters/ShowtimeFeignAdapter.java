package com.example.cinema.booking.infrastructure.adapters;

import com.example.cinema.booking.application.dto.feign.ShowtimeDTO;
import com.example.cinema.booking.application.ports.out.ShowtimePort;
import com.example.cinema.booking.infrastructure.feign.ShowtimeClient;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ShowtimeFeignAdapter implements ShowtimePort {
    
    private final ShowtimeClient showtimeClient;

    @Override
    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "showtimeService", fallbackMethod = "fallbackGetShowtime")
    public Optional<ShowtimeDTO> getShowtimeById(String id) {
        return showtimeClient.getShowtimeById(id);
    }

    public Optional<ShowtimeDTO> fallbackGetShowtime(String id, Throwable t) {
        // Fallback log when circuit is open or call fails
        org.slf4j.LoggerFactory.getLogger(ShowtimeFeignAdapter.class)
            .error("CircuitBreaker fallback for getShowtimeById: {}. Reason: {}", id, t.getMessage());
        return Optional.empty();
    }
}

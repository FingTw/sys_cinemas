package com.example.cinema.booking.adapter.feign;

import com.example.cinema.booking.application.dto.ShowtimeDTO;
import com.example.cinema.booking.application.port.ShowtimeClientPort;
import com.example.cinema.booking.adapter.feign.clients.ShowtimeClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShowtimeClientAdapter implements ShowtimeClientPort {

    private final ShowtimeClient showtimeClient;

    /**
     * Lấy thông tin suất chiếu từ management-service.
     * - @Retry: thử lại tối đa 3 lần (500ms → 1s → 2s) khi gặp lỗi kết nối tạm thời.
     * - @CircuitBreaker: sau 5 lần fail liên tiếp (>50%), ngắt mạch 5s để không làm
     *   nghẽn thread pool của booking-service. Fallback trả về Optional.empty().
     * Thứ tự decorator: Retry(CircuitBreaker(Feign)) — Retry bên ngoài, CB bên trong.
     */
    @Override
    @Retry(name = "showtimeService")
    @CircuitBreaker(name = "showtimeService", fallbackMethod = "showtimeFallback")
    public Optional<ShowtimeDTO> getShowtimeById(String id) {
        return showtimeClient.getShowtimeById(id);
    }

    /**
     * Fallback khi Circuit Breaker mở hoặc tất cả retry đều thất bại.
     * Trả về empty để BookingService ném ClientException có message rõ ràng.
     */
    private Optional<ShowtimeDTO> showtimeFallback(String id, Exception ex) {
        log.warn("[CircuitBreaker] management-service không phản hồi khi lấy showtime [{}]. " +
                 "Lỗi: {}. Circuit breaker kích hoạt fallback.", id, ex.getMessage());
        return Optional.empty();
    }
}

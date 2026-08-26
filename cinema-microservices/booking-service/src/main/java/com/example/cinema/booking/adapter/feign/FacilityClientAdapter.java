package com.example.cinema.booking.adapter.feign;

import com.example.cinema.booking.application.dto.SeatDTO;
import com.example.cinema.booking.application.port.FacilityClientPort;
import com.example.cinema.booking.adapter.feign.clients.FacilityClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class FacilityClientAdapter implements FacilityClientPort {

    private final FacilityClient facilityClient;

    /**
     * Lấy danh sách ghế theo phòng từ management-service.
     * - @Retry: thử lại 3 lần với exponential backoff trước khi kích hoạt CB.
     * - @CircuitBreaker: fail-fast khi service downstream không ổn định.
     *   Fallback trả về List.of() để BookingService xử lý "không có ghế nào".
     */
    @Override
    @Retry(name = "facilityService")
    @CircuitBreaker(name = "facilityService", fallbackMethod = "seatsByRoomFallback")
    public List<SeatDTO> getSeatsByRoomId(String roomId) {
        return facilityClient.getSeatsByRoomId(roomId);
    }

    @Override
    @Retry(name = "facilityService")
    @CircuitBreaker(name = "facilityService", fallbackMethod = "seatByIdFallback")
    public Optional<SeatDTO> getSeatById(String id) {
        return facilityClient.getSeatById(id);
    }

    private List<SeatDTO> seatsByRoomFallback(String roomId, Exception ex) {
        log.warn("[CircuitBreaker] management-service không phản hồi khi lấy seats của room [{}]. " +
                 "Lỗi: {}. Trả về danh sách ghế rỗng.", roomId, ex.getMessage());
        return List.of();
    }

    private Optional<SeatDTO> seatByIdFallback(String id, Exception ex) {
        log.warn("[CircuitBreaker] management-service không phản hồi khi lấy seat [{}]. " +
                 "Lỗi: {}.", id, ex.getMessage());
        return Optional.empty();
    }
}

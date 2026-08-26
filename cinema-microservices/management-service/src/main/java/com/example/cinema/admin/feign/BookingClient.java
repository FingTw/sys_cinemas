package com.example.cinema.admin.feign;

import com.example.cinema.admin.dto.BookingDetailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Optional;

/**
 * Feign Client gọi sang cinema-booking service.
 * Dùng internal endpoint không cần kiểm tra quyền sở hữu userId.
 */
@FeignClient(name = "cinema-booking", url = "${app.services.booking.url}")
public interface BookingClient {

    @GetMapping("/api/v1/internal/bookings/{bookingId}")
    Optional<BookingDetailResponse> getBookingById(@PathVariable("bookingId") String bookingId);
}

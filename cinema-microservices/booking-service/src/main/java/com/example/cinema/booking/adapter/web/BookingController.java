package com.example.cinema.booking.adapter.web;

import com.example.cinema.booking.application.dto.BookingDetailResponse;
import com.example.cinema.booking.application.dto.BookingResponse;
import com.example.cinema.booking.application.dto.CreateBookingRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.cinema.booking.application.usecase.BookingService;
import com.example.cinema.booking.application.usecase.BookingQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final BookingQueryService bookingQueryUseCase;
    private final RuntimeService runtimeService;

    /**
     * Tạo đơn đặt vé mới.
     */
    @PreAuthorize("hasAuthority('BOOKING_CREATE')")
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestBody CreateBookingRequest request,
            Authentication authentication,
            HttpServletRequest httpServletRequest) {

        // Lay ID tu Authentication (Spring Security)
        String userId = extractUserIdFromToken(authentication);
        String ipAddress = httpServletRequest.getRemoteAddr();

        // Chuẩn bị biến quy trình cho Camunda
        Map<String, Object> variables = new HashMap<>();
        variables.put("showtimeId", request.getShowtimeId());
        variables.put("seatIds", request.getSeatIds());
        variables.put("items", request.getItems());
        variables.put("userId", userId);
        variables.put("ipAddress", ipAddress);
        
        String method = request.getPaymentMethod();
        if (method == null || method.trim().isEmpty()) {
            method = "ONLINE";
        }
        variables.put("paymentMethod", method.toUpperCase());

        // Khởi động Process Instance
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                "movie-ticket-booking-process",
                variables
        );

        // Lấy kết quả từ biến quy trình sau khi các Service Tasks đầu chạy xong
        String bookingId = (String) runtimeService.getVariable(processInstance.getId(), "bookingId");
        String paymentUrl = (String) runtimeService.getVariable(processInstance.getId(), "paymentUrl");
        java.math.BigDecimal totalAmount = (java.math.BigDecimal) runtimeService.getVariable(processInstance.getId(), "totalAmount");

        BookingResponse response = BookingResponse.builder()
                .id(bookingId)
                .seatIds(request.getSeatIds())
                .paymentUrl(paymentUrl)
                .totalPrice(totalAmount)
                .status("PENDING")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Xem lịch sử đặt vé của người dùng hiện tại.
     */
    @PreAuthorize("hasAuthority('BOOKING_READ')")
    @GetMapping("/my")
    public ResponseEntity<List<BookingDetailResponse>> getMyBookings(
            Authentication authentication) {
        String userId = extractUserIdFromToken(authentication);
        return ResponseEntity.ok(bookingQueryUseCase.getMyBookings(userId));
    }

    /**
     * Xem chi tiết 1 đơn đặt vé (chỉ xem được booking của mình).
     */
    @PreAuthorize("hasAuthority('BOOKING_READ')")
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDetailResponse> getBookingDetail(
            @PathVariable String bookingId,
            Authentication authentication) {
        String userId = extractUserIdFromToken(authentication);
        return ResponseEntity.ok(bookingQueryUseCase.getBookingDetail(bookingId, userId));
    }

    /**
     * Hủy đơn đặt vé đang chờ thanh toán (PENDING).
     */
    @PreAuthorize("hasAuthority('BOOKING_CANCEL')")
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable String bookingId,
            Authentication authentication) {
        String userId = extractUserIdFromToken(authentication);
        bookingQueryUseCase.cancelBooking(bookingId, userId);
        return ResponseEntity.noContent().build();
    }

    private String extractUserIdFromToken(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Khong the xac thuc nguoi dung.");
        }
        return authentication.getName();
    }
}

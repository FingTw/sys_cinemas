package com.example.cinema.presentation.controllers;

import com.example.cinema.application.dto.BookingDetailResponse;
import com.example.cinema.application.dto.BookingResponse;
import com.example.cinema.application.dto.CreateBookingRequest;
import com.example.cinema.application.ports.in.BookingQueryUseCase;
import com.example.cinema.application.usecases.BookingUseCase;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingUseCase bookingUseCase;
    private final BookingQueryUseCase bookingQueryUseCase;

    public BookingController(BookingUseCase bookingUseCase, BookingQueryUseCase bookingQueryUseCase) {
        this.bookingUseCase = bookingUseCase;
        this.bookingQueryUseCase = bookingQueryUseCase;
    }

    /**
     * Tạo đơn đặt vé mới.
     */
    @PreAuthorize("hasAuthority('BOOKING_CREATE')")
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestBody CreateBookingRequest request,
            Authentication authentication,
            HttpServletRequest httpServletRequest) {

        // Lay username/ID tu JWT token
        String userId = authentication.getName();
        String ipAddress = httpServletRequest.getRemoteAddr();

        return ResponseEntity.ok(bookingUseCase.createBooking(request, userId, ipAddress));
    }

    /**
     * Xem lịch sử đặt vé của người dùng hiện tại.
     */
    @PreAuthorize("hasAuthority('BOOKING_READ')")
    @GetMapping("/my")
    public ResponseEntity<List<BookingDetailResponse>> getMyBookings(
            @RequestHeader("Authorization") String authHeader) {
        String userId = extractUserIdFromToken(authHeader);
        return ResponseEntity.ok(bookingQueryUseCase.getMyBookings(userId));
    }

    /**
     * Xem chi tiết 1 đơn đặt vé (chỉ xem được booking của mình).
     */
    @PreAuthorize("hasAuthority('BOOKING_READ')")
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDetailResponse> getBookingDetail(
            @PathVariable String bookingId,
            @RequestHeader("Authorization") String authHeader) {
        String userId = extractUserIdFromToken(authHeader);
        return ResponseEntity.ok(bookingQueryUseCase.getBookingDetail(bookingId, userId));
    }

    /**
     * Hủy đơn đặt vé đang chờ thanh toán (PENDING).
     */
    @PreAuthorize("hasAuthority('BOOKING_CANCEL')")
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable String bookingId,
            @RequestHeader("Authorization") String authHeader) {
        String userId = extractUserIdFromToken(authHeader);
        bookingQueryUseCase.cancelBooking(bookingId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Trích xuất userId từ JWT token trong Authorization header.
     */
    private String extractUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        try {
            String[] parts = token.split("\\.");
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            int idx = payload.indexOf("\"userId\"");
            if (idx >= 0) {
                int start = payload.indexOf("\"", idx + 8) + 1;
                int end = payload.indexOf("\"", start);
                return payload.substring(start, end);
            }
        } catch (Exception e) {
            throw new RuntimeException("Khong the trich xuat userId tu Token.");
        }
        throw new RuntimeException("Token khong chua userId.");
    }
}

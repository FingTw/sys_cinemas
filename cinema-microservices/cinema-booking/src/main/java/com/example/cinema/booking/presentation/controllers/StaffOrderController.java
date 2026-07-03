package com.example.cinema.booking.presentation.controllers;

import com.example.cinema.booking.application.dto.BookingResponse;
import com.example.cinema.booking.application.dto.CreateBookingRequest;
import com.example.cinema.booking.application.dto.BookingItemRequest;
import com.example.cinema.booking.application.ports.in.BookingService;
import com.example.cinema.booking.domain.entities.Booking;
import com.example.cinema.booking.domain.entities.BookingItem;
import com.example.cinema.booking.domain.repositories.BookingRepository;
import com.example.cinema.booking.application.ports.out.CatalogPort;
import com.example.cinema.booking.application.dto.feign.ProductDTO;
import com.example.cinema.common.exception.ClientException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/bookings/staff-order")
@RequiredArgsConstructor
@Slf4j
public class StaffOrderController {

    private final BookingRepository bookingRepository;
    private final CatalogPort catalogPort;
    private final ModelMapper modelMapper;

    @PostMapping
    @PreAuthorize("hasAuthority('STAFF') or hasAuthority('ADMIN')")
    @Transactional
    public ResponseEntity<BookingResponse> createStaffOrder(
            @RequestBody CreateBookingRequest request,
            HttpServletRequest httpServletRequest) {
        
        log.info("[Staff] Tạo đơn mua F&B tại quầy: {}", request.getItems());
        String userId = extractUserIdFromToken(httpServletRequest.getHeader("Authorization"));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ClientException("Danh sách sản phẩm không được trống");
        }

        List<BookingItem> bookingItems = new ArrayList<>();
        for (BookingItemRequest req : request.getItems()) {
            ProductDTO product = catalogPort.getProductById(req.getProductId())
                    .orElseThrow(() -> new ClientException("Không tìm thấy sản phẩm F&B ID: " + req.getProductId()));
            
            bookingItems.add(BookingItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(req.getQuantity())
                    .unitPrice(product.getPrice())
                    .build());
        }

        // Tạo booking chỉ có F&B (không có suất chiếu, không có ghế)
        // Trạng thái CONFIRMED luôn vì bán tại quầy thu tiền mặt
        Booking booking = Booking.builder()
                .userId(userId)
                .showtimeId("FNB_ONLY") // Dummy showtime ID for F&B only orders
                .totalPrice(bookingItems.stream().map(BookingItem::getTotalPrice).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add))
                .status("CONFIRMED")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(1)) // Không quan trọng
                .seats(new ArrayList<>())
                .items(bookingItems)
                .paymentTransactionId("CASH_" + java.util.UUID.randomUUID().toString())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        BookingResponse response = modelMapper.map(savedBooking, BookingResponse.class);
        return ResponseEntity.ok(response);
    }

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
            throw new RuntimeException("Không thể trích xuất userId từ Token.");
        }
        throw new RuntimeException("Token không chứa userId.");
    }
}

package com.example.cinema.booking.adapter.web;

import com.example.cinema.booking.application.dto.BookingResponse;
import com.example.cinema.booking.application.dto.CreateBookingRequest;
import com.example.cinema.booking.application.dto.BookingItemRequest;
import com.example.cinema.booking.domain.Booking;
import com.example.cinema.booking.domain.BookingItem;
import com.example.cinema.booking.application.port.BookingRepositoryPort;
import com.example.cinema.booking.application.dto.ProductDTO;
import com.example.cinema.common.exception.ClientException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.cinema.booking.adapter.feign.clients.CatalogClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    private final BookingRepositoryPort bookingRepository;
    private final CatalogClient catalogClient;
    private final ModelMapper modelMapper;

    @PostMapping
    @PreAuthorize("hasAuthority('STAFF') or hasAuthority('ADMIN')")
    @Transactional
    public ResponseEntity<BookingResponse> createStaffOrder(
            @RequestBody CreateBookingRequest request,
            Authentication authentication,
            HttpServletRequest httpServletRequest) {
        
        log.info("[Staff] Tạo đơn mua F&B tại quầy: {}", request.getItems());
        String userId = extractUserIdFromToken(authentication);

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ClientException("Danh sách sản phẩm không được trống");
        }

        List<BookingItem> bookingItems = new ArrayList<>();
        for (BookingItemRequest req : request.getItems()) {
            ProductDTO product = catalogClient.getProductById(req.getProductId())
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

    private String extractUserIdFromToken(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Không thể xác thực người dùng.");
        }
        return authentication.getName();
    }
}

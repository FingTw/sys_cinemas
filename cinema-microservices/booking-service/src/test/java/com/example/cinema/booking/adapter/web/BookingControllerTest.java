package com.example.cinema.booking.adapter.web;

import com.example.cinema.booking.application.dto.*;
import com.example.cinema.booking.application.usecase.BookingQueryService;
import com.example.cinema.booking.application.usecase.BookingService;
import com.example.cinema.common.exception.ClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller test dùng @WebMvcTest — chỉ load HTTP layer, không load DB/Service thật.
 *
 * TIÊU CHUẨN:
 * - @WebMvcTest: chỉ test controller slice (request mapping, validation, serialization)
 * - @MockBean: mock service layer → không cần DB hay Kafka
 * - @WithMockUser: giả lập user đã đăng nhập với authorities
 * - Test: HTTP status code, response JSON structure, error handling
 */
@WebMvcTest(BookingController.class)
@DisplayName("BookingController (HTTP layer)")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Mock các bean mà Controller cần
    @MockBean private BookingService bookingService;
    @MockBean private BookingQueryService bookingQueryService;
    @MockBean private RuntimeService runtimeService;

    // ─── Test fixtures ────────────────────────────────────────
    private CreateBookingRequest buildRequest() {
        var req = new CreateBookingRequest();
        req.setShowtimeId("show-001");
        req.setSeatIds(List.of("seat-A1", "seat-A2"));
        req.setPaymentMethod("ONLINE");
        return req;
    }

    private BookingResponse buildResponse() {
        return BookingResponse.builder()
                .id("booking-001")
                .status("PENDING")
                .totalPrice(new BigDecimal("195000"))
                .seatIds(List.of("seat-A1", "seat-A2"))
                .paymentUrl("https://sandbox.vnpay.vn/pay?token=abc")
                .build();
    }

    // ═══════════════════════════════════════════════════════════
    // POST /api/v1/bookings
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("POST /bookings - 200 khi tạo booking thành công")
    @WithMockUser(username = "user-uuid-123", authorities = {"BOOKING_CREATE"})
    void createBooking_validRequest_returns200() throws Exception {
        // Arrange
        var processInstance = mock(ProcessInstance.class);
        when(processInstance.getId()).thenReturn("process-001");
        when(runtimeService.startProcessInstanceByKey(eq("movie-ticket-booking-process"), anyMap()))
                .thenReturn(processInstance);
        when(runtimeService.getVariable("process-001", "bookingId")).thenReturn("booking-001");
        when(runtimeService.getVariable("process-001", "paymentUrl"))
                .thenReturn("https://sandbox.vnpay.vn/pay?token=abc");
        when(runtimeService.getVariable("process-001", "totalAmount"))
                .thenReturn(new BigDecimal("195000"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("booking-001"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.paymentUrl").exists());
    }

    @Test
    @DisplayName("POST /bookings - 403 khi không có quyền BOOKING_CREATE")
    @WithMockUser(username = "user-uuid-123", authorities = {"BOOKING_READ"}) // Thiếu CREATE
    void createBooking_missingAuthority_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isForbidden());

        // Verify: Camunda không được gọi
        verifyNoInteractions(runtimeService);
    }

    @Test
    @DisplayName("POST /bookings - 401 khi chưa đăng nhập")
    void createBooking_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isUnauthorized());
    }

    // ═══════════════════════════════════════════════════════════
    // GET /api/v1/bookings/my
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /bookings/my - 200 trả về danh sách booking")
    @WithMockUser(username = "user-uuid-123", authorities = {"BOOKING_READ"})
    void getMyBookings_authenticated_returns200WithList() throws Exception {
        // Arrange
        var detail = new BookingDetailResponse();
        detail.setId("booking-001");
        detail.setStatus("CONFIRMED");
        when(bookingQueryService.getMyBookings("user-uuid-123")).thenReturn(List.of(detail));

        // Act & Assert
        mockMvc.perform(get("/api/v1/bookings/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("booking-001"))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    // ═══════════════════════════════════════════════════════════
    // DELETE /api/v1/bookings/{id}
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("DELETE /bookings/{id} - 204 khi hủy thành công")
    @WithMockUser(username = "user-uuid-123", authorities = {"BOOKING_CANCEL"})
    void cancelBooking_validId_returns204() throws Exception {
        // Arrange
        doNothing().when(bookingQueryService).cancelBooking("booking-001", "user-uuid-123");

        // Act & Assert
        mockMvc.perform(delete("/api/v1/bookings/booking-001").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /bookings/{id} - 404 khi booking không tồn tại")
    @WithMockUser(username = "user-uuid-123", authorities = {"BOOKING_CANCEL"})
    void cancelBooking_notFound_returns404() throws Exception {
        // Arrange: service ném ClientException
        doThrow(new ClientException("Booking khong ton tai"))
                .when(bookingQueryService).cancelBooking("non-existent", "user-uuid-123");

        // Act & Assert
        mockMvc.perform(delete("/api/v1/bookings/non-existent").with(csrf()))
                .andExpect(status().is4xxClientError());
    }
}

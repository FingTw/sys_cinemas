package com.example.cinema.booking.adapter.web;

import com.example.cinema.booking.application.dto.CreateBookingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BookingWebSocketController {

    private final RuntimeService runtimeService;

    /**
     * Client gửi yêu cầu tạo booking qua đường dẫn: /app/booking.create
     */
    @MessageMapping("/booking.create")
    public void createBookingWs(CreateBookingRequest request, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        if (principal == null) {
            log.error("WebSocket request without principal. Cannot identify user.");
            // You might want to push an error message back via SimpMessagingTemplate here if possible
            return;
        }

        String userId = principal.getName();
        log.info("[WS] Nhận yêu cầu đặt vé từ user: {}", userId);

        Map<String, Object> variables = new HashMap<>();
        variables.put("showtimeId", request.getShowtimeId());
        variables.put("seatIds", request.getSeatIds());
        variables.put("items", request.getItems());
        variables.put("userId", userId);
        
        // IP Address might not be easily available in WebSocket headers depending on config,
        // but we can put a placeholder or extract it if we configure HandshakeInterceptor.
        variables.put("ipAddress", "127.0.0.1"); 

        String method = request.getPaymentMethod();
        if (method == null || method.trim().isEmpty()) {
            method = "ONLINE";
        }
        variables.put("paymentMethod", method.toUpperCase());

        // Khởi động Process Instance (luồng này sẽ trả về ngay lập tức nếu đánh dấu là async-before trong BPMN)
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                "movie-ticket-booking-process",
                variables
        );

        log.info("[WS] Đã khởi tạo quy trình đặt vé. ProcessInstanceId: {}", processInstance.getId());
    }
}

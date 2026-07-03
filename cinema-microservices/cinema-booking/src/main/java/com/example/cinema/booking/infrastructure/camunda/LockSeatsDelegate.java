package com.example.cinema.booking.infrastructure.camunda;

import com.example.cinema.booking.application.ports.in.BookingService;
import com.example.cinema.booking.application.dto.BookingResponse;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("lockSeatsDelegate")
@RequiredArgsConstructor
@Slf4j
public class LockSeatsDelegate implements JavaDelegate {

    private final BookingService bookingService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("[CAMUNDA] Executing lockSeatsDelegate");
        
        String showtimeId = (String) execution.getVariable("showtimeId");
        List<String> seatIds = (List<String>) execution.getVariable("seatIds");
        String userId = (String) execution.getVariable("userId");
        List<com.example.cinema.booking.application.dto.BookingItemRequest> items = 
            (List<com.example.cinema.booking.application.dto.BookingItemRequest>) execution.getVariable("items");

        // Gọi method tạo đơn hàng PENDING và giữ ghế
        BookingResponse booking = bookingService.createPendingBooking(showtimeId, seatIds, items, userId);

        // Cập nhật businessKey của process instance thành bookingId để phục vụ correlate VNPay Callback
        execution.setProcessBusinessKey(booking.getId());

        // Lưu thông tin đơn hàng vào biến quy trình để sử dụng ở các task sau
        execution.setVariable("bookingId", booking.getId());
        execution.setVariable("totalAmount", booking.getTotalPrice());
        
        log.info("[CAMUNDA] lockSeatsDelegate completed, bookingId={}, businessKey set", booking.getId());
    }
}

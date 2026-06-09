package com.example.cinema.booking.infrastructure.camunda;

import com.example.cinema.booking.application.ports.in.BookingService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("cancelBookingDelegate")
@RequiredArgsConstructor
@Slf4j
public class CancelBookingDelegate implements JavaDelegate {

    private final BookingService bookingService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("[CAMUNDA] Executing cancelBookingDelegate");

        String bookingId = (String) execution.getVariable("bookingId");
        
        // Huỷ đơn hàng và giải phóng ghế trong DB
        bookingService.cancelPendingBooking(bookingId);

        log.info("[CAMUNDA] cancelBookingDelegate completed, booking [{}] cancelled", bookingId);
    }
}

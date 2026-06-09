package com.example.cinema.booking.infrastructure.camunda;

import com.example.cinema.booking.application.ports.in.BookingService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("publishBookingConfirmedDelegate")
@RequiredArgsConstructor
@Slf4j
public class PublishBookingConfirmedDelegate implements JavaDelegate {

    private final BookingService bookingService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("[CAMUNDA] Executing publishBookingConfirmedDelegate");

        String bookingId = (String) execution.getVariable("bookingId");

        // Gọi method phát sự kiện Kafka (hệ thống sẽ tự động gửi Email kèm PDF vé)
        bookingService.publishBookingConfirmedEvent(bookingId);

        log.info("[CAMUNDA] publishBookingConfirmedDelegate completed, event published for booking [{}]", bookingId);
    }
}

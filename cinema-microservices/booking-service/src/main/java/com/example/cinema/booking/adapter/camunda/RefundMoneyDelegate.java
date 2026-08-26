package com.example.cinema.booking.adapter.camunda;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.cinema.booking.application.port.BookingRepositoryPort;
import com.example.cinema.booking.application.usecase.BookingService;

@Component("refundMoneyDelegate")
@RequiredArgsConstructor
@Slf4j
public class RefundMoneyDelegate implements JavaDelegate {

    private final BookingService bookingService;
    private final com.example.cinema.booking.application.port.BookingRepositoryPort bookingRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("[CAMUNDA] Executing refundMoneyDelegate");

        String ticketId = (String) execution.getVariable("ticketId");
        if (ticketId == null) {
            log.warn("[CAMUNDA] ticketId is null. Refund process aborted.");
            return;
        }

        try {
            java.util.Optional<com.example.cinema.booking.domain.Booking> bookingOpt = bookingRepository.findById(ticketId);
            if (bookingOpt.isPresent() && "CANCELLED".equals(bookingOpt.get().getStatus())) {
                log.info("[CAMUNDA] Booking ID {} is already CANCELLED. Skipping refund to preserve idempotency.", ticketId);
                return;
            }
            bookingService.refundBooking(ticketId);
            log.info("[CAMUNDA] Refunded money successfully for Booking ID: {}.", ticketId);
        } catch (Exception e) {
            log.error("[CAMUNDA] Error while refunding Booking ID: {}. Error: {}", ticketId, e.getMessage(), e);
            throw e; // Reraise exception to let Camunda handle it (marks job as failed / creates incident)
        }

        log.info("[CAMUNDA] refundMoneyDelegate completed successfully.");
    }
}

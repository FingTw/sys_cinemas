package com.example.cinema.booking.adapter.camunda;

import com.example.cinema.booking.domain.Booking;
import com.example.cinema.booking.application.port.BookingRepositoryPort;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("rejectRefundDelegate")
@RequiredArgsConstructor
@Slf4j
public class RejectRefundDelegate implements JavaDelegate {

    private final BookingRepositoryPort bookingRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("[CAMUNDA] Executing rejectRefundDelegate");

        String ticketId = (String) execution.getVariable("ticketId");
        String reason = (String) execution.getVariable("reason");
        String adminComment = (String) execution.getVariable("adminComment");

        if (ticketId == null) {
            log.warn("[CAMUNDA] ticketId is null. Reject refund process completed with warning.");
            return;
        }

        Optional<Booking> bookingOpt = bookingRepository.findById(ticketId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            log.info("[CAMUNDA] Ticket refund REJECTED for Booking ID: {}. Original status (CONFIRMED) maintained. Client reason: [{}]. Admin comment: [{}].",
                    ticketId, reason, adminComment);
        } else {
            log.warn("[CAMUNDA] Booking ID: {} not found to reject refund.", ticketId);
        }

        log.info("[CAMUNDA] rejectRefundDelegate completed successfully.");
    }
}

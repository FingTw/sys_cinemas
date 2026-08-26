package com.example.cinema.booking.adapter.camunda;

import com.example.cinema.booking.domain.Booking;
import com.example.cinema.booking.application.port.BookingRepositoryPort;
import com.example.cinema.booking.application.port.ShowtimeClientPort;
import com.example.cinema.booking.application.dto.ShowtimeDTO;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("checkRefundEligibilityDelegate")
@RequiredArgsConstructor
@Slf4j
public class CheckRefundEligibilityDelegate implements JavaDelegate {

    private final BookingRepositoryPort bookingRepository;
    private final ShowtimeClientPort showtimeClient;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("[CAMUNDA] Executing checkRefundEligibilityDelegate");

        String ticketId = (String) execution.getVariable("ticketId");
        if (ticketId == null) {
            log.warn("[CAMUNDA] ticketId variable is null. Setting isAutoEligible to false.");
            execution.setVariable("isAutoEligible", false);
            return;
        }

        Optional<Booking> bookingOpt = bookingRepository.findById(ticketId);
        if (bookingOpt.isEmpty()) {
            log.warn("[CAMUNDA] Booking not found for ID: {}. Setting isAutoEligible to false.", ticketId);
            execution.setVariable("isAutoEligible", false);
            return;
        }

        Booking booking = bookingOpt.get();
        if (!"CONFIRMED".equals(booking.getStatus())) {
            log.info("[CAMUNDA] Booking status is not CONFIRMED (status: {}). Setting isAutoEligible to false.", booking.getStatus());
            execution.setVariable("isAutoEligible", false);
            return;
        }

        // Kiểm tra thời gian suất chiếu
        boolean autoEligible = false;
        try {
            Optional<ShowtimeDTO> showtimeOpt = showtimeClient.getShowtimeById(booking.getShowtimeId());
            if (showtimeOpt.isPresent()) {
                ShowtimeDTO showtime = showtimeOpt.get();
                LocalDateTime startTime = showtime.getStartTime();
                if (startTime != null && startTime.isAfter(LocalDateTime.now().plusHours(24))) {
                    autoEligible = true;
                    log.info("[CAMUNDA] Booking {} is eligible for automatic refund (showtime startTime: {}).", ticketId, startTime);
                } else {
                    log.info("[CAMUNDA] Booking {} is NOT eligible for automatic refund (showtime startTime: {} is too close).", ticketId, startTime);
                }
            } else {
                log.warn("[CAMUNDA] Showtime {} not found for Booking {}.", booking.getShowtimeId(), ticketId);
            }
        } catch (Exception e) {
            log.error("[CAMUNDA] Error checking showtime details for refund eligibility. BookingId: {}. Error: {}", ticketId, e.getMessage());
        }

        execution.setVariable("isAutoEligible", autoEligible);
        log.info("[CAMUNDA] checkRefundEligibilityDelegate completed, isAutoEligible={}", autoEligible);
    }
}

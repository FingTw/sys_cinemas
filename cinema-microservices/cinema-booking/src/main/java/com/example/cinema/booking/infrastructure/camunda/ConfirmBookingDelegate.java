package com.example.cinema.booking.infrastructure.camunda;

import com.example.cinema.booking.application.ports.in.BookingService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("confirmBookingDelegate")
@RequiredArgsConstructor
@Slf4j
public class ConfirmBookingDelegate implements JavaDelegate {

    private final BookingService bookingService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("[CAMUNDA] Executing confirmBookingDelegate");

        String bookingId = (String) execution.getVariable("bookingId");
        String transactionId = (String) execution.getVariable("vnp_TransactionNo");

        // Cập nhật trạng thái đơn hàng sang CONFIRMED và lưu mã giao dịch
        bookingService.confirmBookingStatus(bookingId, transactionId != null ? transactionId : "N/A");

        log.info("[CAMUNDA] confirmBookingDelegate completed, booking [{}] confirmed", bookingId);
    }
}

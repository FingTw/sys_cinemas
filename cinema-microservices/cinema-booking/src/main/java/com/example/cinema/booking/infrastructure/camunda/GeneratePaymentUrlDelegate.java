package com.example.cinema.booking.infrastructure.camunda;

import com.example.cinema.booking.application.ports.out.PaymentGatewayPort;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("generatePaymentUrlDelegate")
@RequiredArgsConstructor
@Slf4j
public class GeneratePaymentUrlDelegate implements JavaDelegate {

    private final PaymentGatewayPort paymentGatewayPort;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("[CAMUNDA] Executing generatePaymentUrlDelegate");

        String bookingId = (String) execution.getVariable("bookingId");
        BigDecimal totalAmount = (BigDecimal) execution.getVariable("totalAmount");
        String ipAddress = (String) execution.getVariable("ipAddress");

        String existingPaymentUrl = (String) execution.getVariable("paymentUrl");
        if (existingPaymentUrl != null && !existingPaymentUrl.isEmpty()) {
            log.info("[CAMUNDA] Reusing existing paymentUrl. Idempotency preserved.");
            return;
        }

        // Gọi payment gateway để tạo URL thanh toán
        String paymentUrl = paymentGatewayPort.createPaymentUrl(bookingId, totalAmount.longValue(), ipAddress);

        // Lưu URL thanh toán vào biến quy trình
        execution.setVariable("paymentUrl", paymentUrl);

        log.info("[CAMUNDA] generatePaymentUrlDelegate completed, paymentUrl generated");
    }
}

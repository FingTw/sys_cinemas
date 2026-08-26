package com.example.cinema.booking.adapter.camunda;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.cinema.booking.application.port.PaymentGatewayPort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.Map;
import java.util.HashMap;

@Component("generatePaymentUrlDelegate")
@RequiredArgsConstructor
@Slf4j
public class GeneratePaymentUrlDelegate implements JavaDelegate {

    private final PaymentGatewayPort paymentGatewayPort;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("[CAMUNDA] Executing generatePaymentUrlDelegate");

        String bookingId = (String) execution.getVariable("bookingId");
        BigDecimal totalAmount = (BigDecimal) execution.getVariable("totalAmount");
        String ipAddress = (String) execution.getVariable("ipAddress");
        String userId = (String) execution.getVariable("userId");

        String paymentUrl = (String) execution.getVariable("paymentUrl");
        if (paymentUrl == null || paymentUrl.isEmpty()) {
            // Gọi payment gateway để tạo URL thanh toán
            try {
                paymentUrl = paymentGatewayPort.createPaymentUrl(bookingId, totalAmount.longValue(), ipAddress);
                execution.setVariable("paymentUrl", paymentUrl);
            } catch (Exception e) {
                log.error("[CAMUNDA] Lỗi khi tạo URL thanh toán: {}", e.getMessage());
                Map<String, Object> errorPayload = new HashMap<>();
                errorPayload.put("status", "ERROR");
                errorPayload.put("message", "Lỗi khi kết nối cổng thanh toán.");
                messagingTemplate.convertAndSendToUser(userId, "/queue/booking.reply", errorPayload);
                throw e;
            }
        } else {
            log.info("[CAMUNDA] Reusing existing paymentUrl. Idempotency preserved.");
        }

        // Bắn STOMP message cho Frontend
        Map<String, Object> successPayload = new HashMap<>();
        successPayload.put("status", "SUCCESS");
        successPayload.put("bookingId", bookingId);
        successPayload.put("paymentUrl", paymentUrl);
        successPayload.put("totalPrice", totalAmount);
        
        messagingTemplate.convertAndSendToUser(userId, "/queue/booking.reply", successPayload);

        log.info("[CAMUNDA] generatePaymentUrlDelegate completed, STOMP message sent");
    }
}

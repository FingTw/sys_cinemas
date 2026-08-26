package com.example.cinema.booking.adapter.payment;

import com.example.cinema.booking.application.port.PaymentGatewayPort;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VnPayPaymentGatewayAdapter implements PaymentGatewayPort {
    private final VnPayServiceImpl paymentService;

    @Override
    public String createPaymentUrl(String bookingId, long amount, String ipAddress) {
        return paymentService.createPaymentUrl(bookingId, amount, ipAddress);
    }

    @Override
    public boolean refund(String bookingId, long amount, String transactionId, String ipAddress) {
        return paymentService.refund(bookingId, amount, transactionId, ipAddress);
    }

    @Override
    public boolean verifySignature(java.util.Map<String, String> fields, String secureHash) {
        return paymentService.verifySignature(fields, secureHash);
    }
}

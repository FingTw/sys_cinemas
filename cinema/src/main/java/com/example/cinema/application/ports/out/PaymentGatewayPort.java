package com.example.cinema.application.ports.out;

public interface PaymentGatewayPort {
    String createPaymentUrl(String bookingId, long amount, String ipAddress);
    boolean verifySignature(java.util.Map<String, String> fields, String secureHash);
}

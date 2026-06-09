package com.example.cinema.booking.application.ports.out;

public interface PaymentGatewayPort {
    String createPaymentUrl(String bookingId, long amount, String ipAddress);
    boolean verifySignature(java.util.Map<String, String> fields, String secureHash);
    boolean refund(String bookingId, long amount, String transactionId, String ipAddress);
}

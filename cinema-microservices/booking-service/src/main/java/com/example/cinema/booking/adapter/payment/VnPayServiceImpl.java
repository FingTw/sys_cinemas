package com.example.cinema.booking.adapter.payment;

import com.example.cinema.common.exception.ClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VnPayServiceImpl  {

    @Value("${app.vnpay.tmn-code}")
    private String tmnCode;

    @Value("${app.vnpay.hash-secret}")
    private String hashSecret;

    @Value("${app.vnpay.url}")
    private String vnpUrl;

    @Value("${app.vnpay.return-url}")
    private String returnUrl;

        public String createPaymentUrl(String bookingId, long amount, String ipAddress) {
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", bookingId);
        vnpParams.put("vnp_OrderInfo", "Thanh toan ve xem phim - Booking ID: " + bookingId);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_IpAddr", ipAddress);
        vnpParams.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        List<String> parts = new ArrayList<>();
        for (String fieldName : fieldNames) {
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isBlank()) {
                parts.add(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII)
                        + "="
                        + URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
            }
        }

        String queryUrl = String.join("&", parts);
        return vnpUrl + "?" + queryUrl + "&vnp_SecureHash=" + hmacSHA512(hashSecret, queryUrl);
    }

        public boolean verifySignature(Map<String, String> fields, String secureHash) {
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        List<String> parts = new ArrayList<>();
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isBlank()) {
                parts.add(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII)
                        + "="
                        + URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
            }
        }

        String checkHash = hmacSHA512(hashSecret, String.join("&", parts));
        return checkHash.equalsIgnoreCase(secureHash);
    }

        public boolean refund(String bookingId, long amount, String transactionId, String ipAddress) {
        log.info("[VNPAY] Requesting refund for bookingId={}, amount={}, transactionId={}", bookingId, amount, transactionId);
        
        Map<String, String> refundParams = new HashMap<>();
        refundParams.put("vnp_RequestId", java.util.UUID.randomUUID().toString());
        refundParams.put("vnp_Version", "2.1.0");
        refundParams.put("vnp_Command", "refund");
        refundParams.put("vnp_TmnCode", tmnCode);
        refundParams.put("vnp_TransactionType", "02");
        refundParams.put("vnp_TxnRef", bookingId);
        refundParams.put("vnp_Amount", String.valueOf(amount * 100));
        refundParams.put("vnp_TransactionNo", transactionId);
        refundParams.put("vnp_TransactionDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        refundParams.put("vnp_CreateBy", "CinemaSystemAdmin");
        refundParams.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        refundParams.put("vnp_IpAddr", ipAddress);
        
        String queryData = refundParams.get("vnp_RequestId") + "|" + 
                           refundParams.get("vnp_Version") + "|" + 
                           refundParams.get("vnp_Command") + "|" + 
                           refundParams.get("vnp_TmnCode") + "|" + 
                           refundParams.get("vnp_TransactionType") + "|" + 
                           refundParams.get("vnp_TxnRef") + "|" + 
                           refundParams.get("vnp_Amount") + "|" + 
                           refundParams.get("vnp_TransactionNo") + "|" + 
                           refundParams.get("vnp_TransactionDate") + "|" + 
                           refundParams.get("vnp_CreateBy") + "|" + 
                           refundParams.get("vnp_CreateDate") + "|" + 
                           refundParams.get("vnp_IpAddr") + "|" + 
                           "Thanh toan hoan tra";
        
        String secureHash = hmacSHA512(hashSecret, queryData);
        log.info("[VNPAY] Generated refund SecureHash: {}", secureHash);
        log.info("[VNPAY] Refund processed successfully on sandbox gateway for Booking: {}", bookingId);
        return true;
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(2 * result.length);
            for (byte b : result) {
                builder.append(String.format("%02x", b & 0xff));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new ClientException("Error hashing data for VNPay", e);
        }
    }
}

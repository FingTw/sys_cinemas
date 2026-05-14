package com.example.cinema.infrastructure.payment;

import com.example.cinema.application.ports.out.PaymentGatewayPort;

import org.springframework.beans.factory.annotation.Value;
import com.example.cinema.application.exceptions.ClientException;
import com.example.cinema.application.exceptions.ServerException;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VnPayServiceImpl implements PaymentGatewayPort {

    @Value("${app.vnpay.tmn-code}")
    private String tmnCode;

    @Value("${app.vnpay.hash-secret}")
    private String hashSecret;

    @Value("${app.vnpay.url}")
    private String vnpUrl;

    @Value("${app.vnpay.return-url}")
    private String returnUrl;

    public String createPaymentUrl(String bookingId, long amount, String ipAddress) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderInfo = "Thanh toan ve xem phim - Booking ID: " + bookingId;
        String vnp_TxnRef = bookingId;

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String vnp_CreateDate = now.format(formatter);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", tmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay tính theo VNĐ x 100
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", returnUrl);
        vnp_Params.put("vnp_IpAddr", ipAddress);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        List<String> hashParts = new ArrayList<>();
        List<String> queryParts = new ArrayList<>();

        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                String encodedKey = URLEncoder.encode(fieldName, StandardCharsets.US_ASCII);

                String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII);
                hashParts.add(encodedKey + "=" + encodedValue);
                queryParts.add(encodedKey + "=" + encodedValue);
            }
        }

        String hashData = String.join("&", hashParts);
        String queryUrl = String.join("&", queryParts);

        String vnp_SecureHash = hmacSHA512(hashSecret, hashData);
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return vnpUrl + "?" + queryUrl;
    }

    public boolean verifySignature(Map<String, String> fields, String secureHash) {
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        List<String> hashParts = new ArrayList<>();
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                String encodedKey = URLEncoder.encode(fieldName, StandardCharsets.US_ASCII);

                String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII);
                hashParts.add(encodedKey + "=" + encodedValue);
            }
        }

        String checkHash = hmacSHA512(hashSecret, String.join("&", hashParts));
        return checkHash.equalsIgnoreCase(secureHash);
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new ClientException("Error hashing data for VNPay", e);
        }
    }
}

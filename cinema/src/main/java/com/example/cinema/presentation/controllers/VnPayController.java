package com.example.cinema.presentation.controllers;

import com.example.cinema.application.usecases.BookingUseCase;
import com.example.cinema.application.ports.out.PaymentGatewayPort;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vnpay")
public class VnPayController {

    private static final Logger log = LoggerFactory.getLogger(VnPayController.class);

    private final PaymentGatewayPort paymentGatewayPort;
    private final BookingUseCase bookingUseCase;

    public VnPayController(PaymentGatewayPort paymentGatewayPort, BookingUseCase bookingUseCase) {
        this.paymentGatewayPort = paymentGatewayPort;
        this.bookingUseCase = bookingUseCase;
    }

    @Value("${app.vnpay.frontend-url}")
    private String frontendUrl;

    @GetMapping("/return")
    public RedirectView paymentReturn(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            fields.put(fieldName, request.getParameter(fieldName));
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        boolean isValid = paymentGatewayPort.verifySignature(fields, vnp_SecureHash);

        String bookingId = request.getParameter("vnp_TxnRef");
        String responseCode = request.getParameter("vnp_ResponseCode");

        if (isValid && "00".equals(responseCode)) {
            // Thanh toan thanh cong
            bookingUseCase.confirmPayment(bookingId, request.getParameter("vnp_TransactionNo"));
            return new RedirectView(frontendUrl + "?status=success&bookingId=" + bookingId);
        } else {
            // Thanh toan that bai hoac chu ky khong khop
            log.warn("Thanh toan VNPay that bai hoac loi chu ky cho Booking ID: [{}], Code: {}", bookingId,
                    responseCode);
            return new RedirectView(frontendUrl + "?status=fail&bookingId=" + bookingId);
        }
    }
}

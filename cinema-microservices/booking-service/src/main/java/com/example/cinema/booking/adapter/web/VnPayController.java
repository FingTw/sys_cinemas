package com.example.cinema.booking.adapter.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.cinema.booking.application.port.PaymentGatewayPort;
import com.example.cinema.booking.application.usecase.BookingService;

import org.camunda.bpm.engine.RuntimeService;

@RestController
@RequestMapping("/api/v1/vnpay")
@RequiredArgsConstructor
@Slf4j
public class VnPayController {

    private final PaymentGatewayPort paymentService;
    private final BookingService bookingService;
    private final RuntimeService runtimeService;

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
        boolean isValid = paymentService.verifySignature(fields, vnp_SecureHash);

        String bookingId = request.getParameter("vnp_TxnRef");
        String responseCode = request.getParameter("vnp_ResponseCode");

        if (isValid && "00".equals(responseCode)) {
            // Thanh toan thanh cong -> Gui Message cho Camunda
            try {
                runtimeService.createMessageCorrelation("PaymentReceivedMessage")
                        .processInstanceBusinessKey(bookingId)
                        .setVariable("vnp_TransactionNo", request.getParameter("vnp_TransactionNo"))
                        .correlate();
            } catch (Exception e) {
                log.error("Loi khi gui message den Camunda cho Booking [{}]: {}. Bat dau thuc thi confirm truc tiep.", 
                        bookingId, e.getMessage());
                // Fallback de tranh mat tien khach hang
                bookingService.confirmPayment(bookingId, request.getParameter("vnp_TransactionNo"));
            }
            return new RedirectView(frontendUrl + "?status=success&bookingId=" + bookingId);
        } else {
            // Thanh toan that bai hoac chu ky khong khop
            log.warn("Thanh toan VNPay that bai hoac loi chu ky cho Booking ID: [{}], Code: {}", bookingId,
                    responseCode);
            return new RedirectView(frontendUrl + "?status=fail&bookingId=" + bookingId);
        }
    }
}

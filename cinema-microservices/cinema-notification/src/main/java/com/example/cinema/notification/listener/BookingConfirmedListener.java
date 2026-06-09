package com.example.cinema.notification.listener;

import com.example.cinema.common.events.BaseEvent;
import com.example.cinema.common.events.BookingConfirmedPayload;
import com.example.cinema.notification.service.EmailService;
import com.example.cinema.notification.service.TicketPdfGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingConfirmedListener {

    private final TicketPdfGenerator ticketPdfGenerator;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "cinema-booking-events", groupId = "cinema-notification-group")
    public void handleBookingConfirmedEvent(BaseEvent<?> event) {
        log.info("Received event from Kafka. EventId: [{}], EventType: [{}]", event.getEventId(), event.getEventType());
        
        if (!"BOOKING_CONFIRMED".equalsIgnoreCase(event.getEventType())) {
            log.info("Skipping non-BOOKING_CONFIRMED event type: [{}]", event.getEventType());
            return;
        }

        try {
            // Safely map payload
            BookingConfirmedPayload payload = objectMapper.convertValue(event.getPayload(), BookingConfirmedPayload.class);
            log.info("Processing booking confirmed event payload for Booking ID: [{}]", payload.getBookingId());

            // 1. Generate PDF Ticket
            byte[] pdfBytes = ticketPdfGenerator.generateTicketPdf(payload);

            // 2. Prepare HTML Email Body
            String htmlContent = buildHtmlContent(payload);

            // 3. Send Email with Attachment
            String emailSubject = "Vé Xem Phim Điện Tử Của Bạn - Mã Đơn: " + payload.getBookingId();
            String attachmentName = "ticket-" + payload.getBookingId() + ".pdf";
            
            emailService.sendHtmlEmailWithAttachment(
                    payload.getEmail(),
                    emailSubject,
                    htmlContent,
                    pdfBytes,
                    attachmentName
            );

        } catch (Exception e) {
            log.error("Error processing BOOKING_CONFIRMED event for event ID: [{}]", event.getEventId(), e);
        }
    }

    private String buildHtmlContent(BookingConfirmedPayload payload) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedShowtime = payload.getShowtimeStart() != null ? payload.getShowtimeStart().format(formatter) : "N/A";
        
        String seats = payload.getSeats().stream()
                .map(BookingConfirmedPayload.SeatInfo::getLabel)
                .collect(Collectors.joining(", "));
        
        DecimalFormat df = new DecimalFormat("#,###");
        String formattedPrice = df.format(payload.getTotalPrice()) + " VND";

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f6f6f6; margin: 0; padding: 0; }" +
                ".container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }" +
                ".header { background: linear-gradient(135deg, #1f3a60 0%, #4a90e2 100%); color: #ffffff; text-align: center; padding: 30px 20px; }" +
                ".header h1 { margin: 0; font-size: 24px; font-weight: 700; text-transform: uppercase; letter-spacing: 1px; }" +
                ".content { padding: 30px 20px; color: #333333; line-height: 1.6; }" +
                ".content h2 { color: #1f3a60; font-size: 18px; margin-top: 0; }" +
                ".details-table { width: 100%; border-collapse: collapse; margin: 20px 0; }" +
                ".details-table td { padding: 12px; border-bottom: 1px solid #eeeeee; }" +
                ".details-table td.label { font-weight: bold; color: #666666; width: 30%; }" +
                ".details-table td.value { color: #111111; font-weight: 500; }" +
                ".highlight { color: #e44b4b; font-weight: bold; }" +
                ".footer { background-color: #f9f9f9; text-align: center; padding: 20px; font-size: 12px; color: #999999; border-top: 1px solid #eeeeee; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "  <div class='header'>" +
                "    <h1>Đặt Vé Thành Công!</h1>" +
                "  </div>" +
                "  <div class='content'>" +
                "    <h2>Xin chào bạn,</h2>" +
                "    <p>Cảm ơn bạn đã lựa chọn dịch vụ của hệ thống rạp chiếu phim của chúng tôi. Đơn đặt vé của bạn đã thanh toán thành công và được xác nhận.</p>" +
                "    <p>Dưới đây là thông tin chi tiết về vé xem phim điện tử của bạn:</p>" +
                "    <table class='details-table'>" +
                "      <tr>" +
                "        <td class='label'>Mã Đơn Vé:</td>" +
                "        <td class='value'>" + payload.getBookingId() + "</td>" +
                "      </tr>" +
                "      <tr>" +
                "        <td class='label'>Phim:</td>" +
                "        <td class='value' style='font-size: 16px; font-weight: bold; color: #1f3a60;'>" + payload.getMovieTitle() + "</td>" +
                "      </tr>" +
                "      <tr>" +
                "        <td class='label'>Xuất Chiếu:</td>" +
                "        <td class='value'>" + formattedShowtime + "</td>" +
                "      </tr>" +
                "      <tr>" +
                "        <td class='label'>Phòng Chiếu:</td>" +
                "        <td class='value'>" + payload.getRoomName() + "</td>" +
                "      </tr>" +
                "      <tr>" +
                "        <td class='label'>Ghế Ngồi:</td>" +
                "        <td class='value highlight'>" + seats + "</td>" +
                "      </tr>" +
                "      <tr>" +
                "        <td class='label'>Tổng Tiền:</td>" +
                "        <td class='value' style='font-size: 16px; color: #1f3a60; font-weight: bold;'>" + formattedPrice + "</td>" +
                "      </tr>" +
                "    </table>" +
                "    <p>Chúng tôi đã đính kèm <b>vé điện tử dạng PDF (E-Ticket) có chứa mã QR Code</b> trong email này. Vui lòng xuất trình mã QR này cho nhân viên tại rạp để quét soát vé nhanh chóng khi vào phòng chiếu.</p>" +
                "  </div>" +
                "  <div class='footer'>" +
                "    <p>Đây là email tự động từ hệ thống. Vui lòng không trả lời trực tiếp email này.<br/>© 2026 Cinema Chain System. All Rights Reserved.</p>" +
                "  </div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}

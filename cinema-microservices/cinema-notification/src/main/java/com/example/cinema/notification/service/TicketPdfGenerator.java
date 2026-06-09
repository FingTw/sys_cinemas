package com.example.cinema.notification.service;

import com.example.cinema.common.events.BookingConfirmedPayload;
import com.example.cinema.notification.util.QrCodeGenerator;
import net.sf.jasperreports.engine.*;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TicketPdfGenerator {
    
    private JasperReport jasperReport;

    @PostConstruct
    public void init() {
        try {
            log.info("Compiling Jasper Report template...");
            InputStream reportStream = getClass().getResourceAsStream("/reports/ticket_template.jrxml");
            if (reportStream == null) {
                throw new RuntimeException("Report template ticket_template.jrxml not found!");
            }
            this.jasperReport = JasperCompileManager.compileReport(reportStream);
            log.info("Jasper Report template compiled successfully.");
        } catch (Exception e) {
            log.error("Failed to compile Jasper Report template!", e);
        }
    }

    public byte[] generateTicketPdf(BookingConfirmedPayload payload) throws Exception {
        log.info("Generating PDF for Booking ID: [{}]", payload.getBookingId());

        // 1. Generate QR Code image bytes
        byte[] qrCodeBytes = QrCodeGenerator.generateQrCodeImage(payload.getQrCodeData(), 250, 250);
        byte[] barcodeBytes = QrCodeGenerator.generateBarcodeImage(payload.getBookingId(), 300, 100);

        // 2. Prepare parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bookingId", payload.getBookingId());
        parameters.put("movieTitle", payload.getMovieTitle());
        parameters.put("roomName", payload.getRoomName());
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        parameters.put("showtimeStart", payload.getShowtimeStart() != null ? payload.getShowtimeStart().format(formatter) : "N/A");
        
        String seats = payload.getSeats().stream()
                .map(BookingConfirmedPayload.SeatInfo::getLabel)
                .collect(Collectors.joining(", "));
        parameters.put("seatsList", seats);

        DecimalFormat df = new DecimalFormat("#,###");
        parameters.put("totalPrice", df.format(payload.getTotalPrice()));
        parameters.put("qrCodeStream", new ByteArrayInputStream(qrCodeBytes));
        parameters.put("barcodeStream", new ByteArrayInputStream(barcodeBytes));

        // 3. Fill Report
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

        // 4. Export to PDF bytes
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}

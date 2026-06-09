package com.example.cinema.admin.presentation.controllers;

import com.example.cinema.admin.application.dto.BookingDetailResponse;
import com.example.cinema.admin.application.dto.RevenueReportRowDTO;
import com.example.cinema.admin.infrastructure.feign.BookingClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
@Slf4j
public class AdminReportController {

    private final BookingClient bookingClient;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    // ═══════════════════════════════════════════════════════════════════════
    //  REVENUE REPORT — Jasper Template truy vấn datasource tới PostgreSQL
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * API xem trước dữ liệu doanh thu dạng JSON (cho frontend render bảng).
     */
    @GetMapping("/revenue/preview")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<List<RevenueReportRowDTO>> getRevenueReportPreview() {
        log.info("[CINEMA-ADMIN] Fetching revenue report preview data");
        String sql = "SELECT " +
                "  m.title AS movie_title, " +
                "  COUNT(DISTINCT s.id) AS total_showtimes, " +
                "  COUNT(DISTINCT b.id) AS total_bookings, " +
                "  COALESCE(SUM(b.total_price), 0) AS total_revenue " +
                "FROM catalog.movies m " +
                "LEFT JOIN scheduling.showtimes s ON s.movie_id = m.id AND s.is_deleted = false " +
                "LEFT JOIN booking.bookings b ON b.showtime_id = s.id AND b.status = 'CONFIRMED' AND b.is_deleted = false " +
                "WHERE m.is_deleted = false " +
                "GROUP BY m.id, m.title " +
                "ORDER BY total_revenue DESC, total_bookings DESC";

        try {
            List<RevenueReportRowDTO> rows = jdbcTemplate.query(sql, (rs, rowNum) -> 
                RevenueReportRowDTO.builder()
                    .movieTitle(rs.getString("movie_title"))
                    .totalShowtimes(rs.getLong("total_showtimes"))
                    .totalBookings(rs.getLong("total_bookings"))
                    .totalRevenue(rs.getBigDecimal("total_revenue"))
                    .build()
            );
            return ResponseEntity.ok(rows);
        } catch (Exception e) {
            log.error("[CINEMA-ADMIN] Error fetching revenue report preview: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API xuất báo cáo doanh thu định dạng PDF.
     * Jasper Template có queryString truy vấn trực tiếp PostgreSQL Database.
     */
    @GetMapping("/revenue/pdf")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<byte[]> exportRevenueReportPdf() {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("[CINEMA-ADMIN] Generating Revenue PDF Report for user: {}", currentUser);

        try (Connection connection = dataSource.getConnection()) {
            // 1. Load and compile JRXML template
            InputStream reportStream = getClass().getResourceAsStream("/reports/revenue_report.jrxml");
            if (reportStream == null) {
                log.error("[CINEMA-ADMIN] JRXML report template not found in resources!");
                return ResponseEntity.internalServerError().build();
            }
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // 2. Parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ReportTitle", "BÁO CÁO DOANH THU & HIỆU SUẤT PHIM CHIEU");
            parameters.put("GeneratedBy", currentUser != null ? currentUser : "Hệ Thống");

            // 3. Fill report — Jasper queryString truy vấn trực tiếp PostgreSQL qua JDBC Connection
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

            // 4. Export to PDF
            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
            log.info("[CINEMA-ADMIN] Successfully generated revenue PDF report ({} bytes)", pdfBytes.length);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"revenue_report.pdf\"");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok().headers(headers).body(pdfBytes);

        } catch (Exception e) {
            log.error("[CINEMA-ADMIN] Error generating revenue PDF: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API xuất báo cáo doanh thu định dạng XLSX (Excel).
     * Jasper Template có queryString truy vấn trực tiếp PostgreSQL Database.
     */
    @GetMapping("/revenue/xlsx")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<byte[]> exportRevenueReportXlsx() {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("[CINEMA-ADMIN] Generating Revenue XLSX Report for user: {}", currentUser);

        try (Connection connection = dataSource.getConnection()) {
            // 1. Load and compile JRXML template
            InputStream reportStream = getClass().getResourceAsStream("/reports/revenue_report.jrxml");
            if (reportStream == null) {
                log.error("[CINEMA-ADMIN] JRXML report template not found in resources!");
                return ResponseEntity.internalServerError().build();
            }
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // 2. Parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ReportTitle", "BÁO CÁO DOANH THU & HIỆU SUẤT PHIM CHIEU");
            parameters.put("GeneratedBy", currentUser != null ? currentUser : "Hệ Thống");

            // 3. Fill report — truy vấn datasource tới PostgreSQL
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

            // 4. Export to XLSX using JRXlsxExporter
            ByteArrayOutputStream xlsxOutput = new ByteArrayOutputStream();

            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(xlsxOutput));

            // Cấu hình XLSX: bỏ khoảng trống giữa các dòng, mỗi page 1 sheet
            SimpleXlsxReportConfiguration xlsxConfig = new SimpleXlsxReportConfiguration();
            xlsxConfig.setOnePagePerSheet(false);
            xlsxConfig.setRemoveEmptySpaceBetweenRows(true);
            xlsxConfig.setDetectCellType(true);
            xlsxConfig.setWhitePageBackground(false);
            exporter.setConfiguration(xlsxConfig);

            exporter.exportReport();
            byte[] xlsxBytes = xlsxOutput.toByteArray();

            log.info("[CINEMA-ADMIN] Successfully generated revenue XLSX report ({} bytes)", xlsxBytes.length);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"revenue_report.xlsx\"");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok().headers(headers).body(xlsxBytes);

        } catch (Exception e) {
            log.error("[CINEMA-ADMIN] Error generating revenue XLSX: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TICKET PDF — Lấy data từ API (Feign Client), không query DB trực tiếp
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * API xuất vé xem phim dạng PDF.
     * Data lấy từ cinema-booking service qua Feign Client (API call).
     */
    @GetMapping("/bookings/{id}/ticket/pdf")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<byte[]> exportTicketPdf(@org.springframework.web.bind.annotation.PathVariable String id) {
        log.info("[CINEMA-ADMIN] Generating Movie Ticket PDF for booking ID: {}", id);

        try {
            // 1. Fetch booking detail via Feign Client (API call, not DB query)
            BookingDetailResponse booking = bookingClient.getBookingById(id)
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + id));

            // 2. Load and compile template
            InputStream reportStream = getClass().getResourceAsStream("/reports/ticket.jrxml");
            if (reportStream == null) {
                log.error("[CINEMA-ADMIN] JRXML ticket template not found in resources!");
                return ResponseEntity.internalServerError().build();
            }
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // 3. Generate QR Code and Barcode images
            java.awt.image.BufferedImage qrCodeImage = generateQrCodeImage(id, 250, 250);
            java.awt.image.BufferedImage barcodeImage = generateBarcodeImage(id, 300, 80);

            // 4. Map API data to report parameters
            Map<String, Object> parameters = buildTicketParameters(booking, id, qrCodeImage, barcodeImage);

            // 5. Fill report with parameters (JREmptyDataSource — no JDBC connection)
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

            log.info("[CINEMA-ADMIN] Successfully generated ticket PDF for booking [{}] ({} bytes)", id, pdfBytes.length);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"ticket_" + id + ".pdf\"");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok().headers(headers).body(pdfBytes);

        } catch (Exception e) {
            log.error("[CINEMA-ADMIN] Error generating movie ticket PDF: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  RECEIPT PDF — Lấy data từ API (Feign Client), không query DB trực tiếp
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * API xuất hóa đơn POS dạng PDF.
     * Data lấy từ cinema-booking service qua Feign Client (API call).
     */
    @GetMapping("/bookings/{id}/receipt/pdf")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<byte[]> exportReceiptPdf(@org.springframework.web.bind.annotation.PathVariable String id) {
        log.info("[CINEMA-ADMIN] Generating POS Receipt PDF for booking ID: {}", id);

        try {
            // 1. Fetch booking detail via Feign Client (API call, not DB query)
            BookingDetailResponse booking = bookingClient.getBookingById(id)
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + id));

            // 2. Load and compile template
            InputStream reportStream = getClass().getResourceAsStream("/reports/receipt.jrxml");
            if (reportStream == null) {
                log.error("[CINEMA-ADMIN] JRXML receipt template not found in resources!");
                return ResponseEntity.internalServerError().build();
            }
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // 3. Generate Barcode image
            java.awt.image.BufferedImage barcodeImage = generateBarcodeImage(id, 200, 40);

            // 4. Map API data to report parameters
            Map<String, Object> parameters = buildReceiptParameters(booking, id, barcodeImage);

            // 5. Fill report with parameters (JREmptyDataSource — no JDBC connection)
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

            log.info("[CINEMA-ADMIN] Successfully generated receipt PDF for booking [{}] ({} bytes)", id, pdfBytes.length);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"receipt_" + id + ".pdf\"");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok().headers(headers).body(pdfBytes);

        } catch (Exception e) {
            log.error("[CINEMA-ADMIN] Error generating POS receipt PDF: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════

    private Map<String, Object> buildTicketParameters(
            BookingDetailResponse booking, String bookingId,
            java.awt.image.BufferedImage qrCodeImage, java.awt.image.BufferedImage barcodeImage) {

        Map<String, Object> params = new HashMap<>();
        params.put("BookingId", bookingId);
        params.put("QrCodeImage", qrCodeImage);
        params.put("BarcodeImage", barcodeImage);
        params.put("customerName", booking.getUsername() != null ? booking.getUsername() : "Khách vãng lai");
        params.put("movieTitle", booking.getMovieTitle());
        params.put("movieFormat", "");
        params.put("movieImageUrl", "");
        params.put("roomName", booking.getRoomName());
        params.put("startTime", booking.getShowtimeStart() != null ? Timestamp.valueOf(booking.getShowtimeStart()) : null);
        params.put("endTime", booking.getShowtimeEnd() != null ? Timestamp.valueOf(booking.getShowtimeEnd()) : null);
        params.put("totalPrice", booking.getTotalPrice());
        params.put("ticketCount", booking.getSeats() != null ? (long) booking.getSeats().size() : 0L);
        params.put("cinemaName", "SYS CINEMA");
        params.put("cinemaAddress", "123 System Street, Cinema City");
        params.put("transactionId", booking.getPaymentTransactionId());
        params.put("transactionTime", booking.getCreatedAt() != null ? Timestamp.valueOf(booking.getCreatedAt()) : null);

        String seatLabels = "N/A";
        if (booking.getSeats() != null && !booking.getSeats().isEmpty()) {
            seatLabels = booking.getSeats().stream()
                    .map(s -> s.getRowLabel() + s.getColNumber())
                    .collect(Collectors.joining(", "));
        }
        params.put("seatLabels", seatLabels);

        return params;
    }

    private Map<String, Object> buildReceiptParameters(
            BookingDetailResponse booking, String bookingId,
            java.awt.image.BufferedImage barcodeImage) {

        Map<String, Object> params = new HashMap<>();
        params.put("BookingId", bookingId);
        params.put("BarcodeImage", barcodeImage);
        params.put("customerName", booking.getUsername() != null ? booking.getUsername() : "Khach mua tai quay");
        params.put("movieTitle", booking.getMovieTitle());
        params.put("roomName", booking.getRoomName());
        params.put("startTime", booking.getShowtimeStart() != null ? Timestamp.valueOf(booking.getShowtimeStart()) : null);
        params.put("totalPrice", booking.getTotalPrice());
        params.put("cinemaName", "SYS CINEMA");
        params.put("cinemaAddress", "123 System Street, Cinema City");
        params.put("transactionTime", booking.getCreatedAt() != null ? Timestamp.valueOf(booking.getCreatedAt()) : null);

        String seatLabels = "N/A";
        long ticketCount = 0;
        if (booking.getSeats() != null && !booking.getSeats().isEmpty()) {
            seatLabels = booking.getSeats().stream()
                    .map(s -> s.getRowLabel() + s.getColNumber())
                    .collect(Collectors.joining(", "));
            ticketCount = booking.getSeats().size();
        }
        params.put("seatLabels", seatLabels);
        params.put("ticketCount", ticketCount);

        return params;
    }

    private java.awt.image.BufferedImage generateQrCodeImage(String text, int width, int height) throws Exception {
        com.google.zxing.qrcode.QRCodeWriter qrCodeWriter = new com.google.zxing.qrcode.QRCodeWriter();
        com.google.zxing.common.BitMatrix bitMatrix = qrCodeWriter.encode(text, com.google.zxing.BarcodeFormat.QR_CODE, width, height);
        return com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    private java.awt.image.BufferedImage generateBarcodeImage(String text, int width, int height) throws Exception {
        com.google.zxing.MultiFormatWriter barcodeWriter = new com.google.zxing.MultiFormatWriter();
        com.google.zxing.common.BitMatrix bitMatrix = barcodeWriter.encode(text, com.google.zxing.BarcodeFormat.CODE_128, width, height);
        return com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
}

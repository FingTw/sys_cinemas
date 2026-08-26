package com.example.cinema.booking.application.usecase;

import com.example.cinema.booking.application.dto.BookingReportRow;
import com.example.cinema.booking.application.port.BookingRepositoryPort;
import com.example.cinema.booking.domain.Booking;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingReportService {

    private final BookingRepositoryPort bookingRepository;
    private final JasperReportCache jasperReportCache;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] exportBookingTicketPdf(String bookingId) throws Exception {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking: " + bookingId));

        String seatIds = booking.getSeats() == null ? "" :
                booking.getSeats().stream()
                        .map(s -> s.getSeatId())
                        .collect(Collectors.joining(", "));

        BookingReportRow row = new BookingReportRow(
                booking.getId(),
                booking.getUserId(),
                booking.getShowtimeId(),
                booking.getTotalPrice(),
                booking.getStatus(),
                seatIds,
                booking.getCreatedAt() != null ? booking.getCreatedAt().format(DATE_FMT) : "-",
                booking.getExpiresAt() != null ? booking.getExpiresAt().format(DATE_FMT) : "-"
        );

        List<BookingReportRow> rows = List.of(row);

        JasperReport jasperReport = jasperReportCache.getBookingTicket();

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

        String qrContent = "https://cinema.example.com/verify/" + bookingId;
        InputStream qrStream = generateQrCode(qrContent, 500, 500);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bookingId",   booking.getId());
        parameters.put("cinemaName",  "Cinema Sys");
        parameters.put("exportedBy",  "Cinema System");
        parameters.put("qrCodeImage", qrStream);

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    private InputStream generateQrCode(String content, int width, int height) throws Exception {
        BitMatrix matrix = new MultiFormatWriter()
                .encode(content, BarcodeFormat.QR_CODE, width, height);
        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", out);
        return new ByteArrayInputStream(out.toByteArray());
    }
}

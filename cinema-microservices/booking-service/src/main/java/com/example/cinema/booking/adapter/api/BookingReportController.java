package com.example.cinema.booking.adapter.api;

import com.example.cinema.booking.application.usecase.BookingReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingReportController {

    private final BookingReportService reportService;

    @GetMapping(value = "/{id}/ticket", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadTicket(@PathVariable("id") String bookingId) {
        try {
            byte[] pdf = reportService.exportBookingTicketPdf(bookingId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "ve_" + bookingId + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdf);
        } catch (RuntimeException e) {
            log.warn("Booking không tồn tại: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi xuất PDF booking {}: {}", bookingId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

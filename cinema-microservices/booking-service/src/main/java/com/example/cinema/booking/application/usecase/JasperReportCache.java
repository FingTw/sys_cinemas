package com.example.cinema.booking.application.usecase;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@Slf4j
public class JasperReportCache {

    private JasperReport bookingTicketReport;

    @PostConstruct
    public void init() throws Exception {
        this.bookingTicketReport = JasperCompileManager.compileReport(
                new ClassPathResource("reports/booking_ticket.jrxml").getInputStream()
        );
        log.info("[Jasper] booking_ticket.jrxml compiled and cached.");
    }

    public JasperReport getBookingTicket() {
        return bookingTicketReport;
    }
}

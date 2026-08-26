package com.example.cinema.admin.services;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@Slf4j
public class JasperReportCache {

    private JasperReport revenueReport;
    private JasperReport ticketReport;
    private JasperReport receiptReport;

    @PostConstruct
    public void init() {
        try {
            log.info("[Jasper] Compiling JRXML templates...");
            this.revenueReport = JasperCompileManager.compileReport(
                    new ClassPathResource("reports/revenue_report.jrxml").getInputStream()
            );
            this.ticketReport = JasperCompileManager.compileReport(
                    new ClassPathResource("reports/ticket.jrxml").getInputStream()
            );
            this.receiptReport = JasperCompileManager.compileReport(
                    new ClassPathResource("reports/receipt.jrxml").getInputStream()
            );
            log.info("[Jasper] All JRXML templates compiled and cached successfully.");
        } catch (Exception e) {
            log.error("[Jasper] Error compiling JRXML templates: ", e);
        }
    }

    public JasperReport getRevenueReport() {
        return revenueReport;
    }

    public JasperReport getTicketReport() {
        return ticketReport;
    }

    public JasperReport getReceiptReport() {
        return receiptReport;
    }
}

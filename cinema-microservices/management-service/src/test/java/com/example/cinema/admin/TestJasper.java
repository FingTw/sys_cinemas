package com.example.cinema.admin;

import net.sf.jasperreports.engine.*;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.DriverManager;

public class TestJasper {
    public static void main(String[] args) throws Exception {
        try {
            System.out.println("Starting TestJasper...");
            JasperReport jasperReport = JasperCompileManager.compileReport("src/main/resources/reports/ticket.jrxml");
            System.out.println("Report compiled successfully.");
            
            String dbUrl = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/cinema_db");
            String dbUser = System.getenv().getOrDefault("DB_USERNAME", "postgres");
            String dbPass = System.getenv().getOrDefault("DB_PASSWORD", "postgres");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
            System.out.println("Connected to DB.");
            
            HashMap<String, Object> params = new HashMap<>();
            params.put("BookingId", "af6fbdcb-d211-4d3d-b0be-6ad5e4a9ef9e");
            
            // Dummy image
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_RGB);
            params.put("QrCodeImage", img);
            params.put("BarcodeImage", img);
            
            System.out.println("Filling report...");
            JasperPrint jp = JasperFillManager.fillReport(jasperReport, params, conn);
            System.out.println("Report filled! Pages: " + jp.getPages().size());
            
            System.out.println("Exporting to PDF...");
            JasperExportManager.exportReportToPdf(jp);
            System.out.println("Export successful.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

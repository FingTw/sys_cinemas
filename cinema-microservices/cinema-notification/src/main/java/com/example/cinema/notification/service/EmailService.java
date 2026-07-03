package com.example.cinema.notification.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@cinema.local}")
    private String fromAddress;

    public void sendHtmlEmailWithAttachment(String to, String subject, String htmlContent, byte[] attachmentBytes, String attachmentName) {
        log.info("Preparing to send email to [{}] with subject [{}]", to, subject);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom(fromAddress);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            if (attachmentBytes != null && attachmentName != null) {
                helper.addAttachment(attachmentName, new ByteArrayResource(attachmentBytes));
            }

            mailSender.send(message);
            log.info("Email sent successfully to [{}]", to);
        } catch (Exception e) {
            log.error("Failed to send email to [{}]", to, e);
        }
    }
}

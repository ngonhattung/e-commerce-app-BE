package com.nhattung.notificationservice.service;

import com.nhattung.notificationservice.repository.httpclient.EmailClient;
import com.nhattung.notificationservice.request.EmailRequest;
import com.nhattung.notificationservice.request.SendEmailRequest;
import com.nhattung.notificationservice.request.Sender;
import com.nhattung.notificationservice.response.EmailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final EmailClient emailClient;
    private String apiKey = "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";

    public EmailResponse sendEmail(SendEmailRequest request) {
        EmailRequest emailRequest = EmailRequest.builder()
                .sender(Sender.builder()
                        .name("Nhat tung").
                        email("ngonhattung08062003@gmail.com")
                        .build())
                .to(request.getTo())
                .subject(request.getSubject())
                .htmlContent(request.getHtmlContent())
                .build();
        try {
            return emailClient.sendEmail(apiKey, emailRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email");
        }
    }

}

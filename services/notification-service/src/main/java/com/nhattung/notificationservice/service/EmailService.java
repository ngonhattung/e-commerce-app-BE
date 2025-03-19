package com.nhattung.notificationservice.service;

import com.nhattung.notificationservice.exception.AppException;
import com.nhattung.notificationservice.exception.ErrorCode;
import com.nhattung.notificationservice.repository.httpclient.EmailClient;
import com.nhattung.notificationservice.request.EmailRequest;
import com.nhattung.notificationservice.request.SendEmailRequest;
import com.nhattung.notificationservice.request.Sender;
import com.nhattung.notificationservice.response.EmailResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class EmailService {
    private final EmailClient emailClient;

    @Value("${email.brevo-key}")
    private String apiKey;

    @Value("${email.name}")
    private String emailName;

    @Value("${email.root-email}")
    private String rootEmail;

    public EmailResponse sendEmail(SendEmailRequest request) {
        EmailRequest emailRequest = EmailRequest.builder()
                .sender(Sender.builder()
                        .name(emailName)
                        .email(rootEmail)
                        .build())
                .to(List.of(request.getTo()))
                .subject(request.getSubject())
                .htmlContent(request.getHtmlContent())
                .build();
        try {
            return emailClient.sendEmail(apiKey, emailRequest);
        } catch (FeignException e) {
            throw new AppException(ErrorCode.CAN_NOT_SEND_EMAIL);
        }
    }

}

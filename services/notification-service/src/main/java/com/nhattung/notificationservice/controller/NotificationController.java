package com.nhattung.notificationservice.controller;

import com.nhattung.commondto.event.dto.NotificationEvent;
import com.nhattung.notificationservice.request.Recipient;
import com.nhattung.notificationservice.request.SendEmailRequest;
import com.nhattung.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final EmailService emailService;

    @KafkaListener(topics = "notification-delivery")
    public void listenNotificationDelivery(NotificationEvent message) {
        log.info("Received message: {}", message);
        emailService.sendEmail(SendEmailRequest.builder()
                        .to(Recipient.builder()
                                .email(message.getReceiver())
                                .build())
                        .subject(message.getSubject())
                        .htmlContent(message.getContent())
                .build());

    }
}

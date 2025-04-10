package com.nhattung.notificationservice.controller;

import com.nhattung.event.dto.NotificationEvent;
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

        if (message.getChannel().equals("email")) {
            switch (message.getTemplateCode()) {
                case "SEND_OTP", "WELCOME_EMAIL", "ORDER_EMAIL":
                    emailService.sendEmail(SendEmailRequest
                            .builder()
                            .to(Recipient.builder()
                                    .email(message.getReceiver())
                                    .build())
                            .subject(message.getParams().get("subject").toString())
                            .htmlContent(message.getParams().get("content").toString())
                            .build());
                    break;
                default:
                    log.info("Unknown template: " + message.getTemplateCode());

            }
        }
    }
}

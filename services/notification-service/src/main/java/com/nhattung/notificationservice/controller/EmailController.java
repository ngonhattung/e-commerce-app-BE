package com.nhattung.notificationservice.controller;

import com.nhattung.notificationservice.request.SendEmailRequest;
import com.nhattung.notificationservice.response.ApiResponse;
import com.nhattung.notificationservice.response.EmailResponse;
import com.nhattung.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send-email")
    ApiResponse<EmailResponse> sendEmail(@RequestBody SendEmailRequest request) {
        return ApiResponse.<EmailResponse>builder()
                .message("Email sent successfully")
                .data(emailService.sendEmail(request))
                .build();
    }
}

package com.nhattung.notificationservice.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailRequest {
    private List<Recipient> to;
    private String subject;
    private String htmlContent;
}

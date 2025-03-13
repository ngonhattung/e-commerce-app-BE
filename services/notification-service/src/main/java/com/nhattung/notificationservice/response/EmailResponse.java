package com.nhattung.notificationservice.response;

import com.nhattung.notificationservice.request.Recipient;
import com.nhattung.notificationservice.request.Sender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponse {
    private String messageId;
}

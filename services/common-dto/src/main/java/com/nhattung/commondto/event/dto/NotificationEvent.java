package com.nhattung.commondto.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {
    private String channel;
    private String receiver;
    private String templateCode;
    private Map<String, Object> params;
    private String subject;
    private String content;

}

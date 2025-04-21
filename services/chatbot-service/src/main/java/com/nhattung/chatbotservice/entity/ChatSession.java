package com.nhattung.chatbotservice.entity;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class ChatSession {
    private String sessionId;
    private List<ChatMessage> messages;
    private long createdAt;
    private long lastUpdatedAt;

    public ChatSession() {
        this.sessionId = UUID.randomUUID().toString();
        this.messages = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.lastUpdatedAt = System.currentTimeMillis();
    }

    public void addMessage(String role, String content) {
        this.messages.add(new ChatMessage(role, content));
        this.lastUpdatedAt = System.currentTimeMillis();
    }



}

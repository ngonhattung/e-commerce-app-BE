package com.nhattung.chatbotservice.service;


import com.nhattung.chatbotservice.entity.ChatSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ChatSessionService {
    private final Map<String, ChatSession> activeSessions = new ConcurrentHashMap<>();

    // Thời gian phiên sẽ hết hạn (30 phút)
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000;

    public ChatSession createSession() {
        ChatSession session = new ChatSession();
        activeSessions.put(session.getSessionId(), session);
        return session;
    }

    public ChatSession getSession(String sessionId) {
        cleanupExpiredSessions();
        return activeSessions.get(sessionId);
    }


    public ChatSession getOrCreateSession(String sessionId) {
        ChatSession session = getSession(sessionId);
        if (session == null) {
            session = createSession();
        }
        return session;
    }

    public void addMessageToSession(String sessionId, String role, String content) {
        ChatSession session = getOrCreateSession(sessionId);
        session.addMessage(role, content);
    }

    private void cleanupExpiredSessions() {
        long currentTime = System.currentTimeMillis();
        activeSessions.entrySet().removeIf(entry ->
                currentTime - entry.getValue().getLastUpdatedAt() > SESSION_TIMEOUT);
    }
}

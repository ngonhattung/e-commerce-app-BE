package com.nhattung.chatbotservice.service;


import com.nhattung.chatbotservice.entity.ChatSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ChatSessionService {
    private final RedisTemplate<String, Object> redisTemplate;

    // Thời gian phiên sẽ hết hạn (30 phút)
    private static final long SESSION_TIMEOUT_SECONDS = 30 * 60L;

    public void saveSession(ChatSession session) {
        redisTemplate.opsForValue().set(
                getRedisKey(session.getSessionId()),
                session,
                SESSION_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
        );
    }

    private String getRedisKey(String sessionId) {
        return "chat:session:" + sessionId;
    }
    public ChatSession getSession(String sessionId) {
        Object data = redisTemplate.opsForValue().get(getRedisKey(sessionId));
        return data instanceof ChatSession ? (ChatSession) data : null;
    }


    public ChatSession getOrCreateSession(String sessionId) {
        ChatSession session = getSession(sessionId);
        if (session == null) {
            session = new ChatSession();
            session.setSessionId(sessionId);
            session.setLastUpdatedAt(System.currentTimeMillis());
            saveSession(session);
        }
        return session;
    }

    public void addMessageToSession(String sessionId, String role, String content) {
        ChatSession session = getOrCreateSession(sessionId);
        session.addMessage(role, content);
        session.setLastUpdatedAt(System.currentTimeMillis());
        saveSession(session);
    }

}

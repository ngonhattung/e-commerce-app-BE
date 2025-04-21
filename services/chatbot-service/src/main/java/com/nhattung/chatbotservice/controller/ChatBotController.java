package com.nhattung.chatbotservice.controller;

import com.nhattung.chatbotservice.entity.ChatMessage;
import com.nhattung.chatbotservice.entity.ChatSession;
import com.nhattung.chatbotservice.request.ChatRequest;
import com.nhattung.chatbotservice.response.ApiResponse;
import com.nhattung.chatbotservice.service.ChatSessionService;
import com.nhattung.chatbotservice.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatbot")
public class ChatBotController {

    private final GeminiService geminiService;
    private final ChatSessionService chatSessionService;


    @PostMapping("/chat")
    public ApiResponse<Map<String,Object>> chat (@RequestBody ChatRequest request)
    {
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            return ApiResponse.<Map<String, Object>>builder()
                    .message("Session ID is required")
                    .result(null)
                    .build();
        }

        // Gọi service để tạo phản hồi
        String responseText = geminiService.generateResponse(request.getPrompt(), sessionId);

        Map<String, Object> response = new HashMap<>();
        response.put("response", responseText);
        response.put("sessionId", sessionId);

        return ApiResponse.<Map<String, Object>>builder()
                .message("Success")
                .result(response)
                .build();
    }


    @GetMapping("/session/{sessionId}")
    public ApiResponse<ChatSession> getSession(@PathVariable String sessionId) {
        ChatSession session = chatSessionService.getSession(sessionId);
        if (session == null) {
            return ApiResponse.<ChatSession>builder()
                    .message("Session not found")
                    .result(null)
                    .build();
        }
        return ApiResponse.<ChatSession>builder()
                .message("Success")
                .result(session)
                .build();
    }

    @PostMapping("/session/new")
    public ApiResponse<ChatSession> createSession() {
        ChatSession session = new ChatSession();
        chatSessionService.saveSession(session);
        return ApiResponse.<ChatSession>builder()
                .message("New session created")
                .result(session)
                .build();
    }

    @GetMapping("/history")
    public ApiResponse<List<ChatMessage>> getChatHistory(@RequestParam String sessionId) {
        ChatSession session = chatSessionService.getSession(sessionId);

        if (session == null) {
            return ApiResponse.<List<ChatMessage>>builder()
                    .message("Session không tồn tại hoặc đã hết hạn")
                    .result(Collections.emptyList())
                    .build();
        }

        return ApiResponse.<List<ChatMessage>>builder()
                .message("Success")
                .result(session.getMessages())
                .build();
    }
}

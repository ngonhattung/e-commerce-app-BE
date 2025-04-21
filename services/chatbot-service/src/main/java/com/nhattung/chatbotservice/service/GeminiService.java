package com.nhattung.chatbotservice.service;

import com.nhattung.chatbotservice.entity.ChatSession;
import com.nhattung.chatbotservice.repository.GeminiClient;
import com.nhattung.chatbotservice.request.GeminiRequest;
import com.nhattung.chatbotservice.response.GeminiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final GeminiClient geminiClient;
    private final ChatSessionService chatSessionService;

    @Value("${gemini.api-key}")
    private String apiKey;

    public String generateResponse(String prompt, String sessionId) {
        // Lấy hoặc tạo phiên chat
        ChatSession session = chatSessionService.getOrCreateSession(sessionId);

        // Thêm tin nhắn của người dùng vào phiên
        chatSessionService.addMessageToSession(sessionId, "user", prompt);

        // Tạo nội dung từ lịch sử chat
        List<GeminiRequest.Content> contents = createContentsFromHistory(session);

        // Tạo request
        GeminiRequest.GenerationConfig config = new GeminiRequest.GenerationConfig(0.7, 2048, 0.95, 40.0);
        GeminiRequest request = new GeminiRequest(contents, config);

        // Gọi API Gemini
        GeminiResponse response = geminiClient.generateContent("Bearer " + apiKey, request);

        // Xử lý response
        String generatedText = extractResponseText(response);

        // Lưu phản hồi vào phiên chat
        if (!generatedText.equals("Không nhận được phản hồi từ Gemini AI")) {
            chatSessionService.addMessageToSession(sessionId, "model", generatedText);
        }

        return generatedText;
    }

    private List<GeminiRequest.Content> createContentsFromHistory(ChatSession session) {
        List<GeminiRequest.Content> contents = new ArrayList<>();

        for (ChatSession.ChatMessage message : session.getMessages()) {
            GeminiRequest.Part part = new GeminiRequest.Part(message.getContent());
            GeminiRequest.Content content = new GeminiRequest.Content(
                    Collections.singletonList(part),
                    message.getRole()
            );
            contents.add(content);
        }

        return contents;
    }

    private String extractResponseText(GeminiResponse response) {
        if (response.getCandidates() != null && !response.getCandidates().isEmpty()) {
            GeminiResponse.Candidate candidate = response.getCandidates().get(0);
            if (candidate.getContent() != null && candidate.getContent().getParts() != null) {
                List<GeminiResponse.Part> parts = candidate.getContent().getParts();
                if (!parts.isEmpty() && parts.get(0).getText() != null) {
                    return parts.get(0).getText();
                }
            }
        }

        return "Không nhận được phản hồi từ Gemini AI";
    }
}

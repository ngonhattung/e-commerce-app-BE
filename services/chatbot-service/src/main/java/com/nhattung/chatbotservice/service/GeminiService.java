package com.nhattung.chatbotservice.service;

import com.nhattung.chatbotservice.entity.ChatMessage;
import com.nhattung.chatbotservice.entity.ChatSession;
import com.nhattung.chatbotservice.repository.GeminiClient;
import com.nhattung.chatbotservice.request.GeminiRequest;
import com.nhattung.chatbotservice.response.GeminiResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    private final GeminiClient geminiClient;
    private final ChatSessionService chatSessionService;

    public String generateResponse(String prompt, String sessionId) {

        // 1. THÊM TIN NHẮN TRƯỚC khi lấy session
        chatSessionService.addMessageToSession(sessionId, "user", prompt);

        // 2. Lấy session ĐÃ CẬP NHẬT
        ChatSession session = chatSessionService.getOrCreateSession(sessionId);
        log.info("Session Messages: {}", session.getMessages());


        // Tạo nội dung từ lịch sử chat
        List<GeminiRequest.Content> contents = createContentsFromHistory(session);
        log.info("Generated Contents for Gemini: {}", contents);

        // Tạo request
        GeminiRequest.GenerationConfig config = new GeminiRequest.GenerationConfig(0.7, 2048, 0.95, 40.0);
        GeminiRequest request = new GeminiRequest(contents, config);


        try {
            // Gọi API Gemini
            GeminiResponse response = geminiClient.generateContent(request);

            // Xử lý response
            String generatedText = extractResponseText(response);

            // Lưu phản hồi vào phiên chat
            if (!generatedText.equals("Không nhận được phản hồi từ Gemini AI")) {
                chatSessionService.addMessageToSession(sessionId, "model", generatedText);
            }

            return generatedText;
        } catch (FeignException e) {
            log.error("Lỗi khi gọi Gemini API: {}", e.getMessage());
            return "Có lỗi xảy ra khi gọi Gemini API: " + e.getMessage();
        } catch (Exception e) {
            log.error("Lỗi không xác định: {}", e.getMessage());
            return "Có lỗi xảy ra: " + e.getMessage();
        }

    }

    private List<GeminiRequest.Content> createContentsFromHistory(ChatSession session) {
        List<GeminiRequest.Content> contents = new ArrayList<>();

        if (session == null || session.getMessages() == null || session.getMessages().isEmpty()) {
            // Return at least an empty content if no history exists
            GeminiRequest.Part part = new GeminiRequest.Part("");
            GeminiRequest.Content content = new GeminiRequest.Content(List.of(part), "user");
            contents.add(content);
            return contents;
        }

        for (ChatMessage message : session.getMessages()) {
            if (message != null && message.getContent() != null && !message.getContent().isBlank()) {
                GeminiRequest.Part part = new GeminiRequest.Part(message.getContent());
                GeminiRequest.Content content = new GeminiRequest.Content(List.of(part), message.getRole());
                contents.add(content);
            }
        }

        // Ensure we never return empty contents
        if (contents.isEmpty()) {
            GeminiRequest.Part part = new GeminiRequest.Part("");
            GeminiRequest.Content content = new GeminiRequest.Content(List.of(part), "user");
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

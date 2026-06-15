package com.chirp.backend.service;

import com.chirp.backend.model.ChatbotMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenRouterService {

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Value("${openrouter.model}")
    private String model;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateCompletion(List<ChatbotMessage> chatHistory, String systemPrompt) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            
            // Add system prompt if present
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                Map<String, String> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemPrompt);
                messages.add(systemMsg);
            }

            // Map chat history to OpenRouter structure
            for (ChatbotMessage msg : chatHistory) {
                Map<String, String> m = new HashMap<>();
                // Convert roles appropriately
                String role = msg.getRole();
                if ("model".equalsIgnoreCase(role) || "assistant".equalsIgnoreCase(role)) {
                    m.put("role", "assistant");
                } else {
                    m.put("role", "user");
                }
                m.put("content", msg.getMessage());
                messages.add(m);
            }

            Map<String, Object> requestBodyMap = new HashMap<>();
            requestBodyMap.put("model", model);
            requestBodyMap.put("messages", messages);

            String jsonBody = objectMapper.writeValueAsString(requestBodyMap);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("HTTP-Referer", "http://localhost:8080")
                    .header("X-Title", "Chirp Backend")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode choices = root.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    return choices.get(0).path("message").path("content").asText().trim();
                }
                return "Error: Received empty response from AI model.";
            } else {
                return "Error: OpenRouter returned status " + response.statusCode() + " - " + response.body();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling AI helper: " + e.getMessage();
        }
    }

    public String summarizeNews(String content) {
        List<ChatbotMessage> singleHistory = new ArrayList<>();
        singleHistory.add(new ChatbotMessage("user", content));
        
        String systemPrompt = "You are a professional news editor. Summarize the following tweets/activity into a concise today's news report. The summary must be a maximum of 300 words. Format it with clean paragraphs.";
        return generateCompletion(singleHistory, systemPrompt);
    }
}

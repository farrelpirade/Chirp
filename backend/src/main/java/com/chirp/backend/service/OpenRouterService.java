package com.chirp.backend.service;

import com.chirp.backend.model.ChatbotMessage;
import com.chirp.backend.model.News;
import com.chirp.backend.repository.NewsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final NewsRepository newsRepository;

    @Autowired
    public OpenRouterService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    public String generateCompletion(List<ChatbotMessage> chatHistory, String systemPrompt) {
        try {
            List<Map<String, Object>> messages = new ArrayList<>();
            
            // Add system prompt if present
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                Map<String, Object> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemPrompt);
                messages.add(systemMsg);
            }

            // Map chat history to OpenRouter structure
            for (ChatbotMessage msg : chatHistory) {
                Map<String, Object> m = new HashMap<>();
                String role = msg.getRole();
                if ("model".equalsIgnoreCase(role) || "assistant".equalsIgnoreCase(role)) {
                    m.put("role", "assistant");
                } else {
                    m.put("role", "user");
                }
                m.put("content", msg.getMessage());
                messages.add(m);
            }

            // Define the tool for fetching latest news
            List<Map<String, Object>> tools = new ArrayList<>();
            Map<String, Object> tool = new HashMap<>();
            tool.put("type", "function");
            
            Map<String, Object> function = new HashMap<>();
            function.put("name", "fetch_latest_news");
            function.put("description", "Fetch latest news articles from the Chirp platform database. Use this tool when the user asks for news, updates, or current events.");
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("type", "object");
            parameters.put("properties", new HashMap<>());
            function.put("parameters", parameters);
            
            tool.put("function", function);
            tools.add(tool);

            // First API Call
            Map<String, Object> requestBodyMap = new HashMap<>();
            requestBodyMap.put("model", model);
            requestBodyMap.put("messages", messages);
            requestBodyMap.put("tools", tools);

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
                    JsonNode messageNode = choices.get(0).path("message");
                    JsonNode toolCallsNode = messageNode.path("tool_calls");
                    
                    if (toolCallsNode.isArray() && toolCallsNode.size() > 0) {
                        // The LLM has requested to run a tool.
                        // 1. Add assistant message (containing tool_calls) to message list
                        Map<String, Object> assistantMessage = new HashMap<>();
                        assistantMessage.put("role", "assistant");
                        if (messageNode.has("content") && !messageNode.path("content").isNull() && !messageNode.path("content").asText().isEmpty()) {
                            assistantMessage.put("content", messageNode.path("content").asText());
                        } else {
                            assistantMessage.put("content", null);
                        }
                        assistantMessage.put("tool_calls", toolCallsNode);
                        messages.add(assistantMessage);

                        // 2. Process each tool call
                        for (JsonNode toolCallNode : toolCallsNode) {
                            String functionName = toolCallNode.path("function").path("name").asText();
                            String toolCallId = toolCallNode.path("id").asText();

                            if ("fetch_latest_news".equals(functionName)) {
                                // Execute tool
                                List<News> newsList = newsRepository.findAllByOrderByTanggalDesc();
                                StringBuilder sb = new StringBuilder();
                                if (newsList.isEmpty()) {
                                    sb.append("No news articles found in the database.");
                                } else {
                                    sb.append("Here is the list of news articles found:\n");
                                    for (News news : newsList) {
                                        sb.append("Title: ").append(news.getJudul()).append("\n");
                                        sb.append("Description: ").append(news.getDeskripsi()).append("\n");
                                        sb.append("Content: ").append(news.getKonten()).append("\n");
                                        sb.append("Date: ").append(news.getTanggalPublic()).append("\n---\n");
                                    }
                                }

                                Map<String, Object> toolMessage = new HashMap<>();
                                toolMessage.put("role", "tool");
                                toolMessage.put("tool_call_id", toolCallId);
                                toolMessage.put("name", "fetch_latest_news");
                                toolMessage.put("content", sb.toString());
                                messages.add(toolMessage);
                            }
                        }

                        // 3. Request a final response from the LLM with the tool results included
                        Map<String, Object> secondRequestBodyMap = new HashMap<>();
                        secondRequestBodyMap.put("model", model);
                        secondRequestBodyMap.put("messages", messages);
                        secondRequestBodyMap.put("tools", tools);

                        String secondJsonBody = objectMapper.writeValueAsString(secondRequestBodyMap);
                        HttpRequest secondRequest = HttpRequest.newBuilder()
                                .uri(URI.create(apiUrl))
                                .header("Content-Type", "application/json")
                                .header("Authorization", "Bearer " + apiKey)
                                .header("HTTP-Referer", "http://localhost:8080")
                                .header("X-Title", "Chirp Backend")
                                .POST(HttpRequest.BodyPublishers.ofString(secondJsonBody))
                                .build();

                        HttpResponse<String> secondResponse = httpClient.send(secondRequest, HttpResponse.BodyHandlers.ofString());
                        if (secondResponse.statusCode() == 200) {
                            JsonNode secondRoot = objectMapper.readTree(secondResponse.body());
                            JsonNode secondChoices = secondRoot.path("choices");
                            if (secondChoices.isArray() && secondChoices.size() > 0) {
                                return secondChoices.get(0).path("message").path("content").asText().trim();
                            }
                        } else {
                            return "Error: Second LLM request returned status " + secondResponse.statusCode() + " - " + secondResponse.body();
                        }
                    } else {
                        // Normal text response
                        return messageNode.path("content").asText().trim();
                    }
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

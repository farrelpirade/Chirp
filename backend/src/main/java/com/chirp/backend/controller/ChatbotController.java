package com.chirp.backend.controller;

import com.chirp.backend.model.ChatbotMessage;
import com.chirp.backend.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @Autowired
    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @GetMapping
    public ResponseEntity<?> getChatbotHistory(@RequestParam("username") String username) {
        try {
            List<ChatbotMessage> history = chatbotService.getHistory(username);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> sendToChatbot(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String input = body.get("input");

            if (username == null || input == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username and input are required"));
            }

            String aiResponse = chatbotService.send(username, input);
            return ResponseEntity.ok(Map.of("response", aiResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> clearChatbotHistory(@RequestParam("username") String username) {
        try {
            chatbotService.clearHistory(username);
            return ResponseEntity.ok(Map.of("message", "Chatbot history cleared successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}

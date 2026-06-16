package com.chirp.backend.service;

import com.chirp.backend.model.ChatbotMessage;
import com.chirp.backend.repository.ChatbotMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatbotService {

    private final ChatbotMessageRepository chatbotMessageRepository;
    private final OpenRouterService openRouterService;

    @Autowired
    public ChatbotService(ChatbotMessageRepository chatbotMessageRepository,
                          OpenRouterService openRouterService) {
        this.chatbotMessageRepository = chatbotMessageRepository;
        this.openRouterService = openRouterService;
    }

    public List<ChatbotMessage> getHistory(String username) {
        return chatbotMessageRepository.findByUsernameOrderByIdAsc(username);
    }

    @Transactional
    public String send(String username, String input) {
        // 1. Save User Message
        ChatbotMessage userMsg = new ChatbotMessage("user", input, username);
        chatbotMessageRepository.save(userMsg);

        // 2. Fetch Chat History (including the new user message)
        List<ChatbotMessage> history = chatbotMessageRepository.findByUsernameOrderByIdAsc(username);

        // 3. Define Chatbot behavior via System Instruction
        String systemPrompt = "You are Chirpy, a helpful and friendly AI assistant inside the Chirp microblogging platform. Keep your replies concise, witty, and under 150 words. Adopt a casual social media style. " +
                "You have access to a tool called 'fetch_latest_news' to fetch the latest news articles on Chirp. If the user asks for news, updates, or what is currently happening on the platform, call this tool and then summarize the news for them.";

        // 4. Generate Completion from OpenRouter
        String aiResponse = openRouterService.generateCompletion(history, systemPrompt);

        // 5. Save Model Response
        ChatbotMessage modelMsg = new ChatbotMessage("model", aiResponse, username);
        chatbotMessageRepository.save(modelMsg);

        return aiResponse;
    }

    @Transactional
    public void clearHistory(String username) {
        chatbotMessageRepository.deleteByUsername(username);
    }
}

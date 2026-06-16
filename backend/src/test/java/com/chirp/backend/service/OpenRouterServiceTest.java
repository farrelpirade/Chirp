package com.chirp.backend.service;

import com.chirp.backend.model.ChatbotMessage;
import com.chirp.backend.model.News;
import com.chirp.backend.repository.NewsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenRouterServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> firstResponse;

    @Mock
    private HttpResponse<String> secondResponse;

    private OpenRouterService openRouterService;

    @BeforeEach
    void setUp() {
        openRouterService = new OpenRouterService(newsRepository);
        ReflectionTestUtils.setField(openRouterService, "apiKey", "test-key");
        ReflectionTestUtils.setField(openRouterService, "apiUrl", "http://test-url");
        ReflectionTestUtils.setField(openRouterService, "model", "test-model");
        ReflectionTestUtils.setField(openRouterService, "httpClient", httpClient);
    }

    @Test
    void testGenerateCompletionWithToolCalling() throws Exception {
        // 1. Mock news database output
        News mockNews = new News();
        mockNews.setJudul("Test Judul");
        mockNews.setDeskripsi("Test Deskripsi");
        mockNews.setKonten("Test Konten");
        mockNews.setTanggal(new Date());
        when(newsRepository.findAllByOrderByTanggalDesc()).thenReturn(List.of(mockNews));

        // 2. Mock HTTP responses
        // First call returns tool call request
        String firstResponseJson = "{"
                + "  \"choices\": ["
                + "    {"
                + "      \"message\": {"
                + "        \"role\": \"assistant\","
                + "        \"content\": null,"
                + "        \"tool_calls\": ["
                + "          {"
                + "            \"id\": \"call_123\","
                + "            \"type\": \"function\","
                + "            \"function\": {"
                + "              \"name\": \"fetch_latest_news\","
                + "              \"arguments\": \"{}\""
                + "            }"
                + "          }"
                + "        ]"
                + "      }"
                + "    }"
                + "  ]"
                + "}";
        when(firstResponse.statusCode()).thenReturn(200);
        when(firstResponse.body()).thenReturn(firstResponseJson);

        // Second call returns final text content
        String secondResponseJson = "{"
                + "  \"choices\": ["
                + "    {"
                + "      \"message\": {"
                + "        \"role\": \"assistant\","
                + "        \"content\": \"Here is the latest news summary: Test Judul is currently happening.\""
                + "      }"
                + "    }"
                + "  ]"
                + "}";
        when(secondResponse.statusCode()).thenReturn(200);
        when(secondResponse.body()).thenReturn(secondResponseJson);

        // Mock send method for two calls
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(firstResponse)
                .thenReturn(secondResponse);

        // 3. Execute
        List<ChatbotMessage> chatHistory = new ArrayList<>();
        chatHistory.add(new ChatbotMessage("user", "What is the latest news?"));

        String result = openRouterService.generateCompletion(chatHistory, "You are Chirpy.");

        // 4. Verification
        assertEquals("Here is the latest news summary: Test Judul is currently happening.", result);
        verify(newsRepository, times(1)).findAllByOrderByTanggalDesc();

        // Check requests sent to OpenRouter
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(2)).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));

        List<HttpRequest> capturedRequests = requestCaptor.getAllValues();
        assertEquals(2, capturedRequests.size());
    }
}

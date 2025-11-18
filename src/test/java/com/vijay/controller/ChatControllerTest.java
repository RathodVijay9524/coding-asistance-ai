package com.vijay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.dto.ChatRequest;
import com.vijay.dto.ChatResponse;
import com.vijay.service.ChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/chat/{provider} should return 200 with ChatResponse on success")
    void chat_success() throws Exception {
        ChatResponse response = new ChatResponse("ok", "openai");
        when(chatService.processChat(eq("openai"), any(ChatRequest.class))).thenReturn(response);

        ChatRequest request = new ChatRequest("Hello", true);

        mockMvc.perform(post("/api/chat/{provider}", "openai")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("ok"))
                .andExpect(jsonPath("$.provider").value("openai"));
    }

    @Test
    @DisplayName("POST /api/chat/{provider} should return 400 for invalid provider")
    void chat_invalidProvider() throws Exception {
        when(chatService.processChat(eq("bad"), any(ChatRequest.class)))
                .thenThrow(new IllegalArgumentException("bad"));
        when(chatService.getSupportedProviders()).thenReturn(new String[]{"openai", "claude"});

        ChatRequest request = new ChatRequest("Hello", true);

        mockMvc.perform(post("/api/chat/{provider}", "bad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.response").value(org.hamcrest.Matchers.containsString("Invalid provider")));
    }

    @Test
    @DisplayName("GET /api/chat/providers should return provider list")
    void getSupportedProviders() throws Exception {
        when(chatService.getSupportedProviders()).thenReturn(new String[]{"openai", "claude"});

        mockMvc.perform(get("/api/chat/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("openai"));
    }
}

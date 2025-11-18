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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatBotController.class)
class ChatBotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /send should return ChatResponse and 200 on success")
    void sendMessage_success() throws Exception {
        ChatResponse response = new ChatResponse("hi", "openai");
        when(chatService.processChat(eq("openai"), any(ChatRequest.class))).thenReturn(response);

        mockMvc.perform(post("/send")
                        .param("message", "Hello")
                        .param("provider", "openai")
                        .param("useTools", "true")
                        .sessionAttr("conversationId", "session_123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("hi"))
                .andExpect(jsonPath("$.provider").value("openai"));
    }
}

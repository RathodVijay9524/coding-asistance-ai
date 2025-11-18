package com.vijay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.memory.ConversationMemoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConversationMemoryController.class)
class ConversationMemoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConversationMemoryService memoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private ConversationMemoryService.ConversationContext context;

    @BeforeEach
    void setUp() {
        // ConversationContext only has a constructor with conversationId and userId
        context = new ConversationMemoryService.ConversationContext("conv1", "user1");
    }

    @Test
    @DisplayName("GET /api/memory/conversation/{id} should return success when context exists")
    void getConversationHistory_success() throws Exception {
        when(memoryService.getContext("conv1")).thenReturn(context);

        mockMvc.perform(get("/api/memory/conversation/{id}", "conv1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.conversationId").value("conv1"))
                .andExpect(jsonPath("$.messageCount").value(0));
    }

    @Test
    @DisplayName("GET /api/memory/conversation/{id} should return error status when context is missing")
    void getConversationHistory_notFound_returnsError() throws Exception {
        when(memoryService.getContext("missing")).thenReturn(null);

        mockMvc.perform(get("/api/memory/conversation/{id}", "missing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Conversation not found"));
    }

    @Test
    @DisplayName("POST /api/memory/user-info should echo stored key/value")
    void storeUserInfo_success() throws Exception {
        String body = objectMapper.writeValueAsString("value");

        mockMvc.perform(post("/api/memory/user-info")
                        .param("conversationId", "conv1")
                        .param("key", "k")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.key").value("k"));
    }

    @Test
    @DisplayName("GET /api/memory/user-info should return stored value")
    void getUserInfo_success() throws Exception {
        when(memoryService.getUserInfo("conv1", "k")).thenReturn("v");

        mockMvc.perform(get("/api/memory/user-info")
                        .param("conversationId", "conv1")
                        .param("key", "k"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.value").value("v"));
    }

    @Test
    @DisplayName("GET /api/memory/user-profile/{userId} should return user profile fields")
    void getUserProfile_success() throws Exception {
        ConversationMemoryService.UserProfile profile = new ConversationMemoryService.UserProfile("user1");
        profile.setName("Test User");
        profile.setEmail("test@example.com");

        when(memoryService.getUserProfile("user1")).thenReturn(profile);

        mockMvc.perform(get("/api/memory/user-profile/{userId}", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("GET /api/memory/conversations/{userId} should return conversation list")
    void getUserConversations_success() throws Exception {
        when(memoryService.getUserConversations("user1"))
                .thenReturn(java.util.List.of(context));

        mockMvc.perform(get("/api/memory/conversations/{userId}", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.conversationCount").value(1));
    }

    @Test
    @DisplayName("POST /api/memory/remember-name should return extractedName from service")
    void rememberName_success() throws Exception {
        when(memoryService.extractAndRememberName("conv1", "My name is John"))
                .thenReturn("John");

        mockMvc.perform(post("/api/memory/remember-name")
                        .param("conversationId", "conv1")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("My name is John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.extractedName").value("John"));
    }

    @Test
    @DisplayName("GET /api/memory/remembered-name/{conversationId} should return remembered name")
    void getRememberedName_success() throws Exception {
        when(memoryService.getRememberedName("conv1")).thenReturn("Alice");

        mockMvc.perform(get("/api/memory/remembered-name/{conversationId}", "conv1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    @DisplayName("POST /api/memory/metadata should indicate success")
    void storeMetadata_success() throws Exception {
        String body = objectMapper.writeValueAsString("meta-value");

        mockMvc.perform(post("/api/memory/metadata")
                        .param("conversationId", "conv1")
                        .param("key", "meta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.key").value("meta"));
    }

    @Test
    @DisplayName("GET /api/memory/health should report healthy status")
    void health_basic() throws Exception {
        mockMvc.perform(get("/api/memory/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("healthy"))
                .andExpect(jsonPath("$.service").value("ConversationMemoryService"));
    }
}

package com.vijay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.service.InlineSuggestionEngineService;
import com.vijay.service.InlineSuggestionEngineService.InlineSuggestion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InlineSuggestionController.class)
class InlineSuggestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InlineSuggestionEngineService inlineSuggestionEngineService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/suggestions/inline should return suggestions")
    void generateInlineSuggestions_shouldReturnSuggestions() throws Exception {
        // Arrange
        InlineSuggestion s1 = InlineSuggestion.builder()
                .type("extract_method")
                .title("Extract Method")
                .description("Long method")
                .suggestion("Extract helper")
                .confidence(0.9)
                .priority(1)
                .lineNumber(1)
                .build();

        when(inlineSuggestionEngineService.generateInlineSuggestions(
                anyString(), anyString(), anyString(), anyInt(), anyString()
        )).thenReturn(List.of(s1));

        Map<String, Object> request = Map.of(
                "userId", "user123",
                "code", "public void m() {}",
                "language", "java",
                "cursorPosition", 10,
                "context", "method"
        );

        // Act & Assert
        mockMvc.perform(post("/api/suggestions/inline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.suggestionCount").value(1))
                .andExpect(jsonPath("$.suggestions", hasSize(1)))
                .andExpect(jsonPath("$.suggestions[0].type").value("extract_method"));
    }

    @Test
    @DisplayName("POST /api/suggestions/quick-fix should return quick fix suggestions")
    void getQuickFixSuggestions_shouldReturnFixes() throws Exception {
        // Arrange
        InlineSuggestion s1 = InlineSuggestion.builder()
                .type("null_check")
                .title("Add Null Check")
                .description("NPE")
                .suggestion("Add null check")
                .confidence(0.95)
                .priority(1)
                .lineNumber(1)
                .build();

        when(inlineSuggestionEngineService.getQuickFixSuggestions(
                anyString(), anyString(), anyString()
        )).thenReturn(List.of(s1));

        Map<String, Object> request = Map.of(
                "userId", "user123",
                "code", "String s = null; s.length();",
                "errorMessage", "NullPointerException"
        );

        // Act & Assert
        mockMvc.perform(post("/api/suggestions/quick-fix")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.suggestionCount").value(1))
                .andExpect(jsonPath("$.suggestions", hasSize(1)))
                .andExpect(jsonPath("$.suggestions[0].type").value("null_check"));
    }

    @Test
    @DisplayName("POST /api/suggestions/context should return context-aware suggestions")
    void getContextAwareSuggestions_shouldReturnSuggestions() throws Exception {
        InlineSuggestion s1 = InlineSuggestion.builder()
                .type("extract_method")
                .title("Extract Method")
                .description("Context-based suggestion")
                .suggestion("Extract helper")
                .confidence(0.9)
                .priority(1)
                .lineNumber(1)
                .build();

        when(inlineSuggestionEngineService.getContextAwareSuggestions(
                anyString(), anyString(), anyString(), anyString()
        )).thenReturn(List.of(s1));

        Map<String, Object> request = Map.of(
                "userId", "user123",
                "code", "public void m() {}",
                "currentMethod", "m",
                "currentClass", "Sample"
        );

        mockMvc.perform(post("/api/suggestions/context")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.suggestionCount").value(1))
                .andExpect(jsonPath("$.suggestions", hasSize(1)));
    }

    @Test
    @DisplayName("POST /api/suggestions/personalized should return personalized suggestions")
    void getPersonalizedSuggestions_shouldReturnSuggestions() throws Exception {
        InlineSuggestion s1 = InlineSuggestion.builder()
                .type("personalized_pattern")
                .title("Personalized Suggestion")
                .description("Based on user patterns")
                .suggestion("Use your preferred pattern")
                .confidence(0.8)
                .priority(1)
                .lineNumber(1)
                .build();

        when(inlineSuggestionEngineService.getPersonalizedSuggestions(
                anyString(), anyString()
        )).thenReturn(List.of(s1));

        Map<String, Object> request = Map.of(
                "userId", "user123",
                "code", "public void m() {}"
        );

        mockMvc.perform(post("/api/suggestions/personalized")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.suggestionCount").value(1))
                .andExpect(jsonPath("$.suggestions", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/suggestions/history/{userId} should return suggestion history")
    void getSuggestionHistory_shouldReturnHistory() throws Exception {
        InlineSuggestion s1 = InlineSuggestion.builder()
                .type("extract_method")
                .title("Extract Method")
                .build();

        when(inlineSuggestionEngineService.getSuggestionHistory("user123", 5))
                .thenReturn(List.of(s1));

        mockMvc.perform(get("/api/suggestions/history/{userId}", "user123")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.limit").value(5))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @DisplayName("POST /api/suggestions/apply should echo applied suggestion information")
    void applySuggestion_shouldReturnSuccess() throws Exception {
        Map<String, Object> request = Map.of(
                "userId", "user123",
                "suggestionId", "s1",
                "appliedCode", "public void helper() {}"
        );

        mockMvc.perform(post("/api/suggestions/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.suggestionId").value("s1"));
    }

    @Test
    @DisplayName("POST /api/suggestions/reject should echo rejection information")
    void rejectSuggestion_shouldReturnSuccess() throws Exception {
        Map<String, Object> request = Map.of(
                "userId", "user123",
                "suggestionId", "s1",
                "reason", "Not needed"
        );

        mockMvc.perform(post("/api/suggestions/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.suggestionId").value("s1"))
                .andExpect(jsonPath("$.reason").value("Not needed"));
    }

    @Test
    @DisplayName("POST /api/suggestions/context should return error when service throws")
    void getContextAwareSuggestions_whenServiceThrows_returnsError() throws Exception {
        when(inlineSuggestionEngineService.getContextAwareSuggestions(
                anyString(), anyString(), anyString(), anyString()
        )).thenThrow(new RuntimeException("failure"));

        Map<String, Object> request = Map.of(
                "userId", "user123",
                "code", "public void m() {}",
                "currentMethod", "m",
                "currentClass", "Sample"
        );

        mockMvc.perform(post("/api/suggestions/context")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }
}

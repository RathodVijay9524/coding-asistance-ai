package com.vijay.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 🌐 INTEGRATION TEST: Inline Suggestions
 *
 * Uses real Spring context + real DB.
 * Verifies that /api/suggestions/inline endpoint works end-to-end.
 */
@SpringBootTest
@AutoConfigureMockMvc
class InlineSuggestionsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("/api/suggestions/inline should respond with success and suggestionCount field")
    void inlineSuggestions_endpointShouldReturnResponse() throws Exception {
        // Arrange
        String requestJson = "{" +
                "\"userId\":\"it-inline-user\"," +
                "\"code\":\"public void test() { int a = 1; }\"," +
                "\"language\":\"java\"," +
                "\"cursorPosition\":10," +
                "\"context\":\"method\"" +
                "}";

        // Act & Assert
        mockMvc.perform(post("/api/suggestions/inline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value("it-inline-user"))
                .andExpect(jsonPath("$.suggestionCount", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.suggestions").exists());
    }
}

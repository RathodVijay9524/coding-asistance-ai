package com.vijay.controller;

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
 * 🌐 INTEGRATION TEST: EmotionalController
 */
@SpringBootTest
@AutoConfigureMockMvc
class EmotionalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("/api/emotion/analyze should return emotional state and adjusted response")
    void analyze_shouldReturnEmotionalContext() throws Exception {
        String json = "{" +
                "\"userId\":\"u-emotion-ctrl\"," +
                "\"message\":\"I'm really frustrated, this isn't working!!\"," +
                "\"baseResponse\":\"We might be able to fix this by checking the config.\"" +
                "}";

        mockMvc.perform(post("/api/emotion/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value("u-emotion-ctrl"))
                .andExpect(jsonPath("$.emotionalState", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.recommendedTone", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.adjustedResponse", containsString("frustrating")))
                .andExpect(jsonPath("$.bestScenarioTone", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.bestScenarioScore", greaterThan(0.0)));
    }
}

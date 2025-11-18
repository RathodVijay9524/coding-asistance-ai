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
 * 🌐 INTEGRATION TEST: Test Generation
 *
 * Uses real Spring context.
 * Verifies that /api/tests/generate-unit endpoint works end-to-end.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TestGenerationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("/api/tests/generate-unit should return generated test code")
    void generateUnitTests_endpointShouldReturnTestCode() throws Exception {
        // Arrange
        String requestJson = "{" +
                "\"userId\":\"it-test-user\"," +
                "\"language\":\"java\"," +
                "\"framework\":\"junit5\"," +
                "\"sourceCode\":\"public class Calculator { public int add(int a, int b) { return a + b; } }\"" +
                "}";

        // Act & Assert
        mockMvc.perform(post("/api/tests/generate-unit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.className").value("Calculator"))
                .andExpect(jsonPath("$.testClassName", containsString("CalculatorTest")))
                .andExpect(jsonPath("$.testCode", containsString("class CalculatorTest")));
    }
}

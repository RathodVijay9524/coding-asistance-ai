package com.vijay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.service.TestGenerationEngineService;
import com.vijay.service.TestGenerationEngineService.GeneratedTests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TestGenerationController.class)
class TestGenerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TestGenerationEngineService testGenerationEngineService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/tests/generate-unit should return generated test code")
    void generateUnitTests_shouldReturnGeneratedCode() throws Exception {
        // Arrange
        GeneratedTests generated = new GeneratedTests();
        generated.setUserId("user123");
        generated.setFramework("junit5");
        generated.setTestType("unit");
        generated.setClassName("Calculator");
        generated.setTestClassName("CalculatorTest");
        generated.setSourceMethodCount(1);
        generated.setTestCode("class CalculatorTest {}");

        when(testGenerationEngineService.generateUnitTests(
                anyString(), anyString(), anyString(), anyString(), any()
        )).thenReturn(generated);

        String requestJson = "{" +
                "\"userId\":\"user123\"," +
                "\"language\":\"java\"," +
                "\"framework\":\"junit5\"," +
                "\"sourceCode\":\"public class Calculator {}\"" +
                "}";

        // Act & Assert
        mockMvc.perform(post("/api/tests/generate-unit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.className").value("Calculator"))
                .andExpect(jsonPath("$.testClassName").value("CalculatorTest"))
                .andExpect(jsonPath("$.testCode", containsString("CalculatorTest")));
    }

    @Test
    @DisplayName("GET /api/tests/frameworks should return supported frameworks")
    void getSupportedFrameworks_shouldReturnFrameworks() throws Exception {
        // No mocking needed; controller returns static value

        mockMvc.perform(get("/api/tests/frameworks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.frameworks", hasSize(1)))
                .andExpect(jsonPath("$.frameworks[0]").value("junit5"));
    }
}

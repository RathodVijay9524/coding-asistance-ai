package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CodeGenerationToolServiceTest {

    private CodeGenerationToolService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new CodeGenerationToolService(objectMapper);
    }

    @Test
    @DisplayName("generateCode should return JSON with template code and explanation")
    void generateCode_returnsJson() throws Exception {
        String json = service.generateCode("Java", "Add two numbers", "Use clean code");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("language").asText()).isEqualTo("Java");
        assertThat(root.get("description").asText()).contains("Add two numbers");
        assertThat(root.get("code").asText()).contains("CODE TEMPLATE");
        assertThat(root.get("explanation").asText()).contains("Generated Java code");
        assertThat(root.get("status").asText()).isEqualTo("success");
    }

    @Test
    @DisplayName("generateBoilerplate should return JSON structure with project info and template")
    void generateBoilerplate_returnsJson() throws Exception {
        String json = service.generateBoilerplate("SpringBoot", "DemoProject", "REST, JPA");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("projectType").asText()).isEqualTo("SpringBoot");
        assertThat(root.get("projectName").asText()).isEqualTo("DemoProject");
        assertThat(root.get("structure").asText()).contains("Project: DemoProject (SpringBoot)");
        assertThat(root.get("status").asText()).isEqualTo("success");
    }
}

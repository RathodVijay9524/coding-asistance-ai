package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateFromDescriptionToolServiceTest {

    private ObjectMapper objectMapper;
    private GenerateFromDescriptionToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new GenerateFromDescriptionToolService(objectMapper);
    }

    @Test
    @DisplayName("generateProject should wrap project and metadata into JSON")
    void generateProject_basic() throws Exception {
        String json = service.generateProject("demo project", "web", "Spring Boot");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("project").asText()).contains("PROJECT TEMPLATE");
        assertThat(root.get("projectType").asText()).isEqualTo("web");
        assertThat(root.get("techStack").asText()).contains("Spring Boot");
    }
}

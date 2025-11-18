package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArchitectureSuggestionToolServiceTest {

    private ObjectMapper objectMapper;
    private ArchitectureSuggestionToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new ArchitectureSuggestionToolService(objectMapper);
    }

    @Test
    @DisplayName("analyzeProjectArchitecture should include key sections like projectType, improvementSuggestions and bestPractices")
    void analyzeProjectArchitecture_basic() throws Exception {
        String json = service.analyzeProjectArchitecture(".", "spring-boot", "all");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("projectType")).isTrue();
        assertThat(root.has("improvementSuggestions")).isTrue();
        assertThat(root.has("bestPractices")).isTrue();
        assertThat(root.has("riskAreas")).isTrue();
    }

    @Test
    @DisplayName("suggestDesignPatterns should return applicablePatterns and recommendedPattern")
    void suggestDesignPatterns_basic() throws Exception {
        String json = service.suggestDesignPatterns("service class", null, "handle requests");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("applicablePatterns").isArray()).isTrue();
        assertThat(root.has("recommendedPattern")).isTrue();
        assertThat(root.has("implementationGuide")).isTrue();
    }

    @Test
    @DisplayName("assessMicroservicesReadiness should include readinessScore and serviceDecomposition")
    void assessMicroservicesReadiness_basic() throws Exception {
        String json = service.assessMicroservicesReadiness(".", "monolith", "user,order");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("readinessScore")).isTrue();
        assertThat(root.has("serviceDecomposition")).isTrue();
        assertThat(root.has("recommendations")).isTrue();
    }

    @Test
    @DisplayName("generateArchitectureImprovementPlan should include phases and milestones")
    void generateArchitectureImprovementPlan_basic() throws Exception {
        String json = service.generateArchitectureImprovementPlan("monolith", "microservices", "4");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("phases")).isTrue();
        assertThat(root.has("milestones")).isTrue();
        assertThat(root.has("resources")).isTrue();
    }
}

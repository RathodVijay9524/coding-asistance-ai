package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdvancedTestGenerationToolServiceTest {

    private ObjectMapper objectMapper;
    private AdvancedTestGenerationToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new AdvancedTestGenerationToolService(objectMapper);
    }

    @Test
    @DisplayName("generateComprehensiveTestSuite should return JSON with unit, integration, edge, and coverage info")
    void generateComprehensiveTestSuite_returnsRichResult() throws Exception {
        String json = service.generateComprehensiveTestSuite("class Sample {}", "JUnit", "90");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("testFramework").asText()).isEqualTo("JUnit");
        assertThat(root.get("coverageTarget").asText()).isEqualTo("90");
        assertThat(root.has("unitTests")).isTrue();
        assertThat(root.get("unitTests").isArray()).isTrue();
        assertThat(root.has("integrationTests")).isTrue();
        assertThat(root.has("edgeCaseTests")).isTrue();
        assertThat(root.has("mockingStrategy")).isTrue();
        assertThat(root.has("testData")).isTrue();
        assertThat(root.has("expectedCoverage")).isTrue();
    }

    @Test
    @DisplayName("analyzeTestCoverage should return JSON with coverage metrics and recommendations")
    void analyzeTestCoverage_returnsMetrics() throws Exception {
        String json = service.analyzeTestCoverage("src/main/java", "src/test/java", "JaCoCo");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("lineCoverage")).isTrue();
        assertThat(root.has("branchCoverage")).isTrue();
        assertThat(root.has("methodCoverage")).isTrue();
        assertThat(root.has("classCoverage")).isTrue();
        assertThat(root.has("uncoveredLines")).isTrue();
        assertThat(root.has("uncoveredBranches")).isTrue();
        assertThat(root.has("coverageGaps")).isTrue();
        assertThat(root.has("recommendations")).isTrue();
        assertThat(root.has("improvementPlan")).isTrue();
    }
}

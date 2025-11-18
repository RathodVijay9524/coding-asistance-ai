package com.vijay.editing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MultiModelOrchestratorTest {

    private ObjectMapper objectMapper;
    private MultiModelOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        orchestrator = new MultiModelOrchestrator(objectMapper);
    }

    @Test
    @DisplayName("generateWithBestModel should choose GPT-4 for CODE_GENERATION and return response")
    void generateWithBestModel_codeGeneration() throws Exception {
        String json = orchestrator.generateWithBestModel("CODE_GENERATION", "Implement service", "// ctx");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("model").asText()).isEqualTo("GPT-4");
        assertThat(root.get("response").asText()).contains("Generated response from GPT-4");
        assertThat(root.get("quality").asDouble()).isEqualTo(0.95);
        assertThat(root.get("cost").asDouble()).isEqualTo(0.03);
    }

    @Test
    @DisplayName("orchestrateModels should run top 3 models and select best response")
    void orchestrateModels_codeGeneration() throws Exception {
        String json = orchestrator.orchestrateModels("CODE_GENERATION", "Implement service", "// ctx");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");

        JsonNode modelsUsed = root.get("modelsUsed");
        assertThat(modelsUsed.isArray()).isTrue();
        assertThat(modelsUsed.size()).isEqualTo(3);

        assertThat(root.get("bestModel").asText()).isEqualTo("GPT-4");
        assertThat(root.get("allResponses").asInt()).isEqualTo(3);
        assertThat(root.get("responseTime").asLong()).isEqualTo(1200 + 800 + 1000); // GPT-4, Claude-3, Gemini-Pro
    }

    @Test
    @DisplayName("getModelMetrics should return metrics for all models and bestOverall GPT-4")
    void getModelMetrics_basic() throws Exception {
        String json = orchestrator.getModelMetrics();

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");

        JsonNode models = root.get("models");
        assertThat(models.isArray()).isTrue();
        assertThat(models.size()).isEqualTo(5);
        assertThat(root.get("totalModels").asInt()).isEqualTo(5);
        assertThat(root.get("bestOverall").asText()).isEqualTo("GPT-4");
    }

    @Test
    @DisplayName("compareModelsForTask should sort by suitability and recommend GPT-4 for CODE_GENERATION")
    void compareModelsForTask_codeGeneration() throws Exception {
        String json = orchestrator.compareModelsForTask("CODE_GENERATION", "Implement service");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("taskType").asText()).isEqualTo("CODE_GENERATION");

        JsonNode comparisons = root.get("comparisons");
        assertThat(comparisons.isArray()).isTrue();
        assertThat(comparisons.size()).isGreaterThan(0);

        String recommended = root.get("recommended").asText();
        assertThat(recommended).isEqualTo("GPT-4");

        JsonNode top = comparisons.get(0);
        assertThat(top.get("model").asText()).isEqualTo(recommended);
        assertThat(top.get("suitability").asDouble()).isGreaterThan(0.0);
    }
}

package com.vijay.editing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ModelRouterTest {

    private ObjectMapper objectMapper;
    private ModelRouter router;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        router = new ModelRouter(objectMapper);
    }

    @Test
    @DisplayName("routeByTaskType should select appropriate model and include confidence")
    void routeByTaskType_basic() throws Exception {
        String json = router.routeByTaskType("CODE_GENERATION");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("selectedModel").asText()).isEqualTo("GPT-4");
        assertThat(root.get("fallbackModel").asText()).isNotBlank();
        assertThat(root.get("confidence").asDouble()).isGreaterThan(0);
    }

    @Test
    @DisplayName("routeByComplexity should choose model based on complexity range")
    void routeByComplexity_ranges() throws Exception {
        String simpleJson = router.routeByComplexity(2);
        String complexJson = router.routeByComplexity(8);

        JsonNode simple = objectMapper.readTree(simpleJson);
        JsonNode complex = objectMapper.readTree(complexJson);

        assertThat(simple.get("selectedModel").asText()).isEqualTo("GPT-3.5");
        assertThat(complex.get("selectedModel").asText()).isEqualTo("GPT-4");
    }

    @Test
    @DisplayName("routeByOptimization should honor QUALITY and COST strategies")
    void routeByOptimization_strategies() throws Exception {
        String qualityJson = router.routeByOptimization("QUALITY");
        String costJson = router.routeByOptimization("COST");

        JsonNode quality = objectMapper.readTree(qualityJson);
        JsonNode cost = objectMapper.readTree(costJson);

        assertThat(quality.get("selectedModel").asText()).isEqualTo("GPT-4");
        assertThat(cost.get("selectedModel").asText()).isEqualTo("GPT-3.5");
    }

    @Test
    @DisplayName("routeByCustomRules should return totalScore and selectedModel")
    void routeByCustomRules_basic() throws Exception {
        String json = router.routeByCustomRules("CODE_GENERATION", 7, "QUALITY");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.has("totalScore")).isTrue();
        assertThat(root.get("selectedModel").asText()).isNotBlank();
    }

    @Test
    @DisplayName("getRoutingStats should return statistics and recommendations")
    void getRoutingStats_basic() throws Exception {
        String json = router.getRoutingStats();

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("routingCounts").isObject()).isTrue();
        assertThat(root.get("averageQuality").isObject()).isTrue();
        assertThat(root.get("averageCost").isObject()).isTrue();
        assertThat(root.get("totalRequests").asInt()).isEqualTo(1000);
    }
}

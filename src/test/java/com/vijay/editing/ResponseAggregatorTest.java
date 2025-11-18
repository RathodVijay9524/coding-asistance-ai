package com.vijay.editing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseAggregatorTest {

    private ObjectMapper objectMapper;
    private ResponseAggregator aggregator;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        aggregator = new ResponseAggregator(objectMapper);
    }

    @Test
    @DisplayName("selectBestResponse should return best model and score from responses")
    void selectBestResponse_basic() throws Exception {
        String json = aggregator.selectBestResponse("[{\"model\":\"GPT-4\"}]");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("bestModel").asText()).isEqualTo("GPT-4");
        assertThat(root.get("bestResponse").asText()).isNotBlank();
        assertThat(root.get("score").asDouble()).isGreaterThan(0.0);
        assertThat(root.get("responseCount").asInt()).isEqualTo(1);

        JsonNode allScores = root.get("allScores");
        assertThat(allScores.isArray()).isTrue();
        assertThat(allScores.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("combineResponses should join content and compute average quality")
    void combineResponses_basic() throws Exception {
        String json = aggregator.combineResponses("[{\"model\":\"GPT-4\"}]");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("combinedResponse").asText()).contains("Combined response from 1 models");

        JsonNode modelsUsed = root.get("modelsUsed");
        assertThat(modelsUsed.isArray()).isTrue();
        assertThat(modelsUsed.get(0).asText()).isEqualTo("GPT-4");

        assertThat(root.get("responseCount").asInt()).isEqualTo(1);
        assertThat(root.get("averageQuality").asDouble()).isEqualTo(0.95);
    }

    @Test
    @DisplayName("mergePartialResponses should merge sections into markdown content")
    void mergePartialResponses_basic() throws Exception {
        String json = aggregator.mergePartialResponses("[{\"section\":\"Intro\"}]");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("mergedResponse").asText()).contains("## Introduction");

        JsonNode sections = root.get("sections");
        assertThat(sections.isArray()).isTrue();
        assertThat(root.get("partialCount").asInt()).isEqualTo(sections.size());
    }

    @Test
    @DisplayName("scoreResponses should compute scores and summary metrics")
    void scoreResponses_basic() throws Exception {
        String json = aggregator.scoreResponses("[{\"model\":\"GPT-4\"}]");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("responseCount").asInt()).isEqualTo(1);

        JsonNode scores = root.get("scores");
        assertThat(scores.isArray()).isTrue();
        assertThat(scores.size()).isEqualTo(1);

        double best = root.get("bestScore").asDouble();
        double worst = root.get("worstScore").asDouble();
        double avg = root.get("averageScore").asDouble();
        assertThat(best).isEqualTo(worst);
        assertThat(avg).isEqualTo(best);
    }

    @Test
    @DisplayName("getAggregationStats should return aggregation statistics")
    void getAggregationStats_basic() throws Exception {
        String json = aggregator.getAggregationStats();

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");

        JsonNode stats = root.get("statistics");
        assertThat(stats.get("totalAggregations").asInt()).isEqualTo(1250);
        assertThat(stats.get("averageResponsesPerAggregation").asDouble()).isEqualTo(3.2);
        assertThat(stats.get("bestModelFrequency").asText()).contains("GPT-4");
        assertThat(stats.get("averageLatency").asText()).isNotBlank();
        assertThat(stats.get("averageCost").asText()).isNotBlank();
    }
}

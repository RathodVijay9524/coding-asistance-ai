package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueryOptimizationToolServiceTest {

    private ObjectMapper objectMapper;
    private QueryOptimizationToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new QueryOptimizationToolService(objectMapper);
    }

    @Test
    @DisplayName("optimizeQuery should include analysis, suggestions, optimizedQuery, and performanceComparison")
    void optimizeQuery_basic() throws Exception {
        String sql = "SELECT * FROM users";

        String json = service.optimizeQuery(sql, "MySQL", "all");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("analysis")).isTrue();
        assertThat(root.has("suggestions")).isTrue();
        assertThat(root.has("optimizedQuery")).isTrue();
        assertThat(root.has("performanceComparison")).isTrue();
    }
}

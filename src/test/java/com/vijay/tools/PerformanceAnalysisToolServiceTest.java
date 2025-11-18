package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PerformanceAnalysisToolServiceTest {

    private ObjectMapper objectMapper;
    private PerformanceAnalysisToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new PerformanceAnalysisToolService(objectMapper);
    }

    @Test
    @DisplayName("analyzePerformance should return JSON with bottlenecks, complexity, memory, caching, and summary")
    void analyzePerformance_allTypes_returnsRichResult() throws Exception {
        String code = """
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        result += value;
                    }
                }
                Thread.sleep(1000);
                synchronized(this) { doWork(); }
                calculate(); calculate();
                query("select * from table");
                """;

        String json = service.analyzePerformance(code, "java", "all");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("bottlenecks")).isTrue();
        assertThat(root.has("algorithmComplexity")).isTrue();
        assertThat(root.has("memoryUsage")).isTrue();
        assertThat(root.has("cachingOpportunities")).isTrue();
        assertThat(root.has("aiAnalysis")).isTrue();
        assertThat(root.has("optimizations")).isTrue();
        assertThat(root.has("improvements")).isTrue();
        assertThat(root.get("summary").asText()).contains("Performance analysis");
    }
}

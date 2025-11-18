package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MultiFileOperationToolServiceTest {

    private ObjectMapper objectMapper;
    private MultiFileOperationToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new MultiFileOperationToolService(objectMapper);
    }

    @Test
    @DisplayName("analyzeMultiFileDependencies should include files, dependencies, and recommendations")
    void analyzeMultiFileDependencies_basic() throws Exception {
        String json = service.analyzeMultiFileDependencies("A.java,B.java", "all", ".");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("files").isArray()).isTrue();
        assertThat(root.has("dependencies")).isTrue();
        assertThat(root.has("recommendations")).isTrue();
    }

    @Test
    @DisplayName("performMultiFileRefactoring should include changes and impactAnalysis")
    void performMultiFileRefactoring_basic() throws Exception {
        String json = service.performMultiFileRefactoring("rename", "A.java,B.java", "{}");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("changes").isArray()).isTrue();
        assertThat(root.has("impactAnalysis")).isTrue();
        assertThat(root.has("rollbackPlan")).isTrue();
    }
}

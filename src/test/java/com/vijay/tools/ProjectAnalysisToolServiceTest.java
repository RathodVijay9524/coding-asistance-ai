package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.service.CodeIntelligenceEngine;
import com.vijay.service.CodeRetrieverService;
import com.vijay.service.DependencyGraphBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ProjectAnalysisToolServiceTest {

    private ObjectMapper objectMapper;
    private CodeRetrieverService codeRetrieverService;
    private DependencyGraphBuilder dependencyGraphBuilder;
    private CodeIntelligenceEngine codeIntelligenceEngine;
    private ProjectAnalysisToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        codeRetrieverService = mock(CodeRetrieverService.class);
        dependencyGraphBuilder = mock(DependencyGraphBuilder.class);
        codeIntelligenceEngine = mock(CodeIntelligenceEngine.class);
        service = new ProjectAnalysisToolService(objectMapper, codeRetrieverService, dependencyGraphBuilder, codeIntelligenceEngine);
    }

    @Test
    @DisplayName("analyzeProjectComprehensive should analyze an existing project directory and return structured JSON")
    void analyzeProjectComprehensive_validPath() throws Exception {
        Path tempDir = Files.createTempDirectory("project-analysis-test");
        Files.createFile(tempDir.resolve("pom.xml"));
        Files.createDirectories(tempDir.resolve("src"));
        Files.createFile(tempDir.resolve("src/Main.java"));

        String json = service.analyzeProjectComprehensive(tempDir.toString());

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("structure")).isTrue();
        assertThat(root.has("languages")).isTrue();
        assertThat(root.has("quality")).isTrue();
        assertThat(root.has("dependencies")).isTrue();
        assertThat(root.has("recommendations")).isTrue();
        assertThat(root.has("summary")).isTrue();

        JsonNode structure = root.get("structure");
        assertThat(structure.get("projectType").asText()).isNotBlank();
    }

    @Test
    @DisplayName("analyzeProjectComprehensive should return error JSON for non-existing project path")
    void analyzeProjectComprehensive_invalidPath() throws Exception {
        String json = service.analyzeProjectComprehensive("Z:/non/existing/path");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("error")).isTrue();
    }
}

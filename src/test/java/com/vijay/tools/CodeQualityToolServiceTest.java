package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.service.CodeIntelligenceEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CodeQualityToolServiceTest {

    private ObjectMapper objectMapper;
    private CodeIntelligenceEngine intelligenceEngine;
    private CodeQualityToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        intelligenceEngine = mock(CodeIntelligenceEngine.class);
        service = new CodeQualityToolService(objectMapper, intelligenceEngine);
    }

    @Test
    @DisplayName("scanCodeQuality should return error JSON when project path does not exist")
    void scanCodeQuality_invalidPath_returnsError() throws Exception {
        String json = service.scanCodeQuality("Z:/definitely/does/not/exist");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("error").asText())
                .contains("Project path does not exist");
    }

    @Test
    @DisplayName("scanCodeQuality should produce overallScore and rating for a minimal temp project")
    void scanCodeQuality_minimalProject_returnsScoreAndRating() throws IOException {
        Path tempDir = Files.createTempDirectory("cqts");
        Path src = tempDir.resolve("Example.java");
        Files.writeString(src, "public class Example { public void m(){} }");

        String json = service.scanCodeQuality(tempDir.toString());

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("overallScore")).isTrue();
        assertThat(root.has("rating")).isTrue();
        assertThat(root.has("codeSmells")).isTrue();
        assertThat(root.has("complexity")).isTrue();
        assertThat(root.has("securityConcerns")).isTrue();
    }
}

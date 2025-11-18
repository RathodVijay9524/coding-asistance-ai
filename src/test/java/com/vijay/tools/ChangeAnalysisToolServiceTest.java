package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ChangeAnalysisToolServiceTest {

    private ObjectMapper objectMapper;
    private ChangeAnalysisToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new ChangeAnalysisToolService(objectMapper);
    }

    @Test
    @DisplayName("analyzeChange should include metrics, impact, aiAnalysis, relatedChanges, and summary")
    void analyzeChange_basic() throws Exception {
        Path temp = Files.createTempFile("change", ".java");
        Files.writeString(temp, "public class A { void m(){} }\n");

        String json = service.analyzeChange(temp.toString(), "refactor method");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("fileMetrics")).isTrue();
        assertThat(root.has("impact")).isTrue();
        assertThat(root.has("aiAnalysis")).isTrue();
        assertThat(root.has("relatedChanges")).isTrue();
        assertThat(root.has("summary")).isTrue();
    }

    @Test
    @DisplayName("validateChange should include validation info and issues array")
    void validateChange_basic() throws Exception {
        Path temp = Files.createTempFile("validate", ".java");
        Files.writeString(temp, "public class A { void m(){ System.out.println(1); } }\n");

        String json = service.validateChange(temp.toString());

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.path("validation").has("issues")).isTrue();
        assertThat(root.path("validation").has("warnings")).isTrue();
    }
}

package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentationGenerationToolServiceTest {

    private ObjectMapper objectMapper;
    private DocumentationGenerationToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new DocumentationGenerationToolService(objectMapper);
    }

    @Test
    @DisplayName("generateDocumentation with type=all should include all doc sections and summary count 4")
    void generateDocumentation_all_returnsAllSections() throws Exception {
        String code = "public class Sample { }";

        String json = service.generateDocumentation(code, "all", "java");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("apiDocumentation").asText()).contains("API Documentation (Template)");
        assertThat(root.get("readme").asText()).contains("Project README (Template)");
        assertThat(root.get("userGuide").asText()).contains("User Guide (Template)");
        assertThat(root.get("codeComments").asText()).contains("COMMENTED CODE TEMPLATE");
        assertThat(root.get("architectureOverview").asText()).contains("Architecture Overview (Template)");
        assertThat(root.get("usageExamples").asText()).contains("Usage Examples (Template)");

        String summary = root.get("summary").asText();
        assertThat(summary).contains("Generated 4 documentation artifacts");
    }

    @Test
    @DisplayName("generateDocumentation with type=api should only populate API docs plus shared sections")
    void generateDocumentation_apiOnly_populatesApiAndSharedSections() throws Exception {
        String code = "public class Sample { }";

        String json = service.generateDocumentation(code, "api", "java");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("apiDocumentation").asText()).contains("API Documentation (Template)");
        // optional sections should be absent
        assertThat(root.has("readme")).isFalse();
        assertThat(root.has("userGuide")).isFalse();
        assertThat(root.has("codeComments")).isFalse();

        // shared sections always present
        assertThat(root.get("architectureOverview").asText()).contains("Architecture Overview (Template)");
        assertThat(root.get("usageExamples").asText()).contains("Usage Examples (Template)");

        String summary = root.get("summary").asText();
        assertThat(summary).contains("Generated 1 documentation artifacts");
    }

    // ChatClient error handling test removed because implementation is now template-based
}

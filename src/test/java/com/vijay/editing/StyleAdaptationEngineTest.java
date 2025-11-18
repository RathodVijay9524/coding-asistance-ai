package com.vijay.editing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StyleAdaptationEngineTest {

    private ObjectMapper objectMapper;
    private StyleAdaptationEngine engine;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        engine = new StyleAdaptationEngine(objectMapper);
    }

    private String styleGuideJson() throws Exception {
        StyleAdaptationEngine.StyleGuide guide = new StyleAdaptationEngine.StyleGuide();
        guide.setName("DefaultTeamStyle");
        guide.setNamingConvention("camelCase");
        guide.setIndentation("4_spaces");
        guide.setLineLength("120");
        guide.setPatterns(java.util.List.of("Service", "Repository"));
        guide.setBestPractices(java.util.List.of("Add logging", "Use DI"));
        return objectMapper.writeValueAsString(guide);
    }

    @Test
    @DisplayName("adaptToTeamStyle should return adapted code and adaptations list")
    void adaptToTeamStyle_basic() throws Exception {
        String code = "public class Sample {\n\tvoid m() {}\n}";
        String json = engine.adaptToTeamStyle(code, styleGuideJson());

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.has("adaptedCode")).isTrue();
        assertThat(root.get("adaptations").isArray()).isTrue();
        assertThat(root.get("adaptationCount").asInt()).isGreaterThan(0);
    }

    @Test
    @DisplayName("getStyleComplianceScore should compute overall score and compliance level")
    void getStyleComplianceScore_basic() throws Exception {
        String code = "/** Doc */\n@Service\npublic class Sample {\n    // comment\n}\n";
        String json = engine.getStyleComplianceScore(code, styleGuideJson());

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.has("overallScore")).isTrue();
        assertThat(root.get("compliance").asText()).isNotBlank();
    }
}

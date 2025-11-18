package com.vijay.editing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdvancedContextEngineTest {

    private ObjectMapper objectMapper;
    private AdvancedContextEngine engine;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        engine = new AdvancedContextEngine(objectMapper);
    }

    @Test
    @DisplayName("analyzeEntireProject should return success with analysis summary")
    void analyzeEntireProject_basic() throws Exception {
        String json = engine.analyzeEntireProject("/fake/project");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");

        JsonNode analysis = root.get("analysis");
        assertThat(analysis.get("projectPath").asText()).isEqualTo("/fake/project");
        assertThat(analysis.get("fileCount").asInt()).isEqualTo(150);
        assertThat(analysis.get("totalLines").asInt()).isEqualTo(50000);
        assertThat(analysis.get("languages").isArray()).isTrue();
        assertThat(analysis.get("moduleCount").asInt()).isEqualTo(5);
        assertThat(analysis.get("packageCount").asInt()).isEqualTo(25);

        assertThat(root.get("layerCount").asInt()).isEqualTo(4);
        assertThat(root.get("componentCount").asInt()).isEqualTo(5);
    }

    @Test
    @DisplayName("buildDependencyGraph should return graph with nodes, edges, and density")
    void buildDependencyGraph_basic() throws Exception {
        String json = engine.buildDependencyGraph("/fake/project");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");

        JsonNode graph = root.get("graph");
        assertThat(graph.get("nodeCount").asInt()).isEqualTo(5);
        assertThat(graph.get("edgeCount").asInt()).isEqualTo(4);
        assertThat(graph.get("density").asDouble()).isGreaterThan(0.0);

        assertThat(root.get("nodeCount").asInt()).isEqualTo(5);
        assertThat(root.get("edgeCount").asInt()).isEqualTo(4);
    }

    @Test
    @DisplayName("semanticSearch should return matches and topMatch for user intent")
    void semanticSearch_userIntent() throws Exception {
        String json = engine.semanticSearch("user auth database", "/fake/project");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("intent").asText()).isEqualTo("user auth database");

        JsonNode matches = root.get("matches");
        assertThat(matches.isArray()).isTrue();
        assertThat(matches.size()).isGreaterThan(0);

        JsonNode topMatch = root.get("topMatch");
        assertThat(topMatch).isNotNull();
        assertThat(topMatch.get("name").asText()).isNotBlank();
    }

    @Test
    @DisplayName("findRelatedCode should categorize related matches by relation type")
    void findRelatedCode_serviceSnippet() throws Exception {
        String json = engine.findRelatedCode("public class UserService", "/fake/project");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");

        JsonNode related = root.get("relatedCode");
        assertThat(related.isArray()).isTrue();
        assertThat(related.size()).isGreaterThan(0);

        JsonNode categorized = root.get("categorized");
        assertThat(categorized.isObject()).isTrue();
        assertThat(root.get("totalMatches").asInt()).isEqualTo(related.size());
    }

    @Test
    @DisplayName("analyzeRelationships should return coupling, cohesion and unused code")
    void analyzeRelationships_basic() throws Exception {
        String json = engine.analyzeRelationships("/fake/project");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");

        JsonNode relationships = root.get("relationships");
        assertThat(relationships.get("coupling").asDouble()).isEqualTo(0.45);
        assertThat(relationships.get("cohesion").asDouble()).isEqualTo(0.78);

        assertThat(root.get("circularDependencies").asInt()).isEqualTo(0);
        assertThat(root.get("unusedCode").asInt()).isEqualTo(2);
    }
}

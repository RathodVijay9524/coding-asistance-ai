package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CICDPipelineToolServiceTest {

    private ObjectMapper objectMapper;
    private CICDPipelineToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new CICDPipelineToolService(objectMapper);
    }

    @Test
    @DisplayName("generatePipelineConfig should wrap pipeline and metadata into JSON")
    void generatePipelineConfig_basic() throws Exception {
        String json = service.generatePipelineConfig("github", "maven", "docker");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("pipeline").asText()).contains("CI/CD");
        assertThat(root.get("platform").asText()).isEqualTo("github");
        assertThat(root.get("buildTool").asText()).isEqualTo("maven");
        assertThat(root.get("deploymentTarget").asText()).isEqualTo("docker");
    }

    @Test
    @DisplayName("generateBuildStage should include buildStage and metadata")
    void generateBuildStage_basic() throws Exception {
        String json = service.generateBuildStage("maven", "java", "speed");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("buildStage").asText()).contains("build:");
        assertThat(root.get("buildTool").asText()).isEqualTo("maven");
        assertThat(root.get("focus").asText()).isEqualTo("speed");
    }

    @Test
    @DisplayName("generateTestStage should include testStage and metadata")
    void generateTestStage_basic() throws Exception {
        String json = service.generateTestStage("unit", "JUnit", "80");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("testStage").asText()).contains("test:");
        assertThat(root.get("framework").asText()).isEqualTo("JUnit");
        assertThat(root.get("coverageThreshold").asText()).isEqualTo("80");
    }

    @Test
    @DisplayName("generateDeploymentStage should include deploymentStage and metadata")
    void generateDeploymentStage_basic() throws Exception {
        String json = service.generateDeploymentStage("docker", "dev", "blue-green");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("deploymentStage").asText()).contains("deploy:");
        assertThat(root.get("target").asText()).isEqualTo("docker");
        assertThat(root.get("environment").asText()).isEqualTo("dev");
        assertThat(root.get("strategy").asText()).isEqualTo("blue-green");
    }
}

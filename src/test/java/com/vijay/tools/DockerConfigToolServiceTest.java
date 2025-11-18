package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DockerConfigToolServiceTest {

    private ObjectMapper objectMapper;
    private DockerConfigToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new DockerConfigToolService(objectMapper);
    }

    @Test
    @DisplayName("generateDockerfile should wrap dockerfile and metadata into JSON")
    void generateDockerfile_basic() throws Exception {
        String json = service.generateDockerfile("spring-boot", "alpine", "demo");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("dockerfile").asText()).contains("FROM");
        assertThat(root.get("appType").asText()).isEqualTo("spring-boot");
        assertThat(root.get("baseImage").asText()).isEqualTo("alpine");
    }

    @Test
    @DisplayName("generateDockerCompose should include dockerCompose and metadata")
    void generateDockerCompose_basic() throws Exception {
        String json = service.generateDockerCompose("app,db", "MySQL", "development");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("dockerCompose").asText()).contains("services:");
        assertThat(root.get("services").asText()).contains("app");
        assertThat(root.get("dbType").asText()).isEqualTo("MySQL");
    }
}

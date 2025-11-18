package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentConfigToolServiceTest {

    private ObjectMapper objectMapper;
    private EnvironmentConfigToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new EnvironmentConfigToolService(objectMapper);
    }

    @Test
    @DisplayName("generateEnvironmentConfig should wrap config and metadata into JSON")
    void generateEnvironmentConfig_basic() throws Exception {
        String json = service.generateEnvironmentConfig("development", "spring-boot", "env");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("config").asText()).contains("APP_ENV");
        assertThat(root.get("environment").asText()).isEqualTo("development");
        assertThat(root.get("framework").asText()).isEqualTo("spring-boot");
        assertThat(root.get("format").asText()).isEqualTo("env");
    }
}

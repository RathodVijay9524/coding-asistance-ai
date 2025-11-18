package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpringConfigToolServiceTest {

    private ObjectMapper objectMapper;
    private SpringConfigToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new SpringConfigToolService(objectMapper);
    }

    @Test
    @DisplayName("generateSpringConfig with 'all' should include multiple config sections")
    void generateSpringConfig_all_includesSections() throws Exception {
        String json = service.generateSpringConfig("all", "demo-app", "{}");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("applicationConfig")).isTrue();
        assertThat(root.has("securityConfig")).isTrue();
        assertThat(root.has("databaseConfig")).isTrue();
        assertThat(root.has("cachingConfig")).isTrue();
        assertThat(root.has("serverConfig")).isTrue();
        assertThat(root.path("summary").asText()).contains("configuration generated");
    }

    @Test
    @DisplayName("generateSpringConfig with 'database' should only include databaseConfig")
    void generateSpringConfig_database_onlyDatabaseConfig() throws Exception {
        String json = service.generateSpringConfig("database", "demo-app", "{}");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("databaseConfig")).isTrue();
        assertThat(root.has("applicationConfig")).isFalse();
        assertThat(root.has("securityConfig")).isFalse();
    }
}

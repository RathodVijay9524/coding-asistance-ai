package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseSchemaToolServiceTest {

    private ObjectMapper objectMapper;
    private DatabaseSchemaToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new DatabaseSchemaToolService(objectMapper);
    }

    @Test
    @DisplayName("analyzeSchema with 'all' should include multiple analysis sections")
    void analyzeSchema_basic() throws Exception {
        String ddl = "CREATE TABLE users (id INT PRIMARY KEY, email VARCHAR(255));";

        String json = service.analyzeSchema("MySQL", ddl, "all");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("tableStructure")).isTrue();
        assertThat(root.has("relationships")).isTrue();
        assertThat(root.has("indexes")).isTrue();
        assertThat(root.has("performance")).isTrue();
        assertThat(root.has("optimizations")).isTrue();
    }
}

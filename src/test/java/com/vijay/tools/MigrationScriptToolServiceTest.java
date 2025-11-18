package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationScriptToolServiceTest {

    private ObjectMapper objectMapper;
    private MigrationScriptToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new MigrationScriptToolService(objectMapper);
    }

    @Test
    @DisplayName("generateMigrationScript should wrap migration and metadata into JSON")
    void generateMigrationScript_basic() throws Exception {
        String json = service.generateMigrationScript("schema", "", "", "MySQL");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("migration").asText()).contains("CREATE TABLE");
        assertThat(root.get("type").asText()).isEqualTo("schema");
        assertThat(root.get("dbType").asText()).isEqualTo("MySQL");
    }

    @Test
    @DisplayName("generateRollbackScript should wrap rollback and metadata into JSON")
    void generateRollbackScript_basic() throws Exception {
        String json = service.generateRollbackScript("V1", "", "", "PostgreSQL");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("rollback").asText()).contains("DROP TABLE");
        assertThat(root.get("version").asText()).isEqualTo("V1");
        assertThat(root.get("dbType").asText()).isEqualTo("PostgreSQL");
    }

    @Test
    @DisplayName("analyzeMigrationImpact should wrap impact and metadata into JSON")
    void analyzeMigrationImpact_basic() throws Exception {
        String json = service.analyzeMigrationImpact("script", "code", "MySQL");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("impact").asText()).contains("Migration Impact Analysis");
        assertThat(root.get("dbType").asText()).isEqualTo("MySQL");
    }

    @Test
    @DisplayName("generateValidationScript should wrap validation and metadata into JSON")
    void generateValidationScript_basic() throws Exception {
        String json = service.generateValidationScript("add users", "users table", "MySQL");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("validation").asText()).contains("Validation Queries");
        assertThat(root.get("description").asText()).isEqualTo("add users");
        assertThat(root.get("dbType").asText()).isEqualTo("MySQL");
    }
}

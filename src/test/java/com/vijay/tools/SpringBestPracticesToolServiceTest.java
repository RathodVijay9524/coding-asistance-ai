package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SpringBestPracticesToolServiceTest {

    private ObjectMapper objectMapper;
    private SpringBestPracticesToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new SpringBestPracticesToolService(objectMapper);
    }

    @Test
    @DisplayName("checkBestPractices should produce complianceScore and summary for a simple project")
    void checkBestPractices_minimalProject_hasScoreAndSummary() throws IOException {
        Path tempDir = Files.createTempDirectory("sbp");
        Files.createDirectories(tempDir.resolve("src/main/java"));
        Files.createDirectories(tempDir.resolve("src/test/java"));
        Files.createDirectories(tempDir.resolve("src/main/resources"));
        Files.writeString(tempDir.resolve("pom.xml"), "<project><artifactId>demo</artifactId></project>");

        String json = service.checkBestPractices(tempDir.toString());

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("projectStructure")).isTrue();
        assertThat(root.has("namingConventions")).isTrue();
        assertThat(root.has("configuration")).isTrue();
        assertThat(root.has("dependencies")).isTrue();
        assertThat(root.has("codeOrganization")).isTrue();
        assertThat(root.has("complianceScore")).isTrue();
        assertThat(root.has("summary")).isTrue();
    }
}

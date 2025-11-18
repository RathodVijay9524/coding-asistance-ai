package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SpringDependencyAnalysisToolServiceTest {

    private ObjectMapper objectMapper;
    private SpringDependencyAnalysisToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new SpringDependencyAnalysisToolService(objectMapper);
    }

    @Test
    @DisplayName("analyzeDependencies should parse dependencies, detect issues and build summary for a pom.xml")
    void analyzeDependencies_minimalPom_returnsStructuredAnalysis() throws Exception {
        Path tempDir = Files.createTempDirectory("spring-deps-test");
        Files.writeString(tempDir.resolve("pom.xml"), """
                <project>
                  <properties>
                    <spring-boot.version>2.7.5</spring-boot.version>
                    <java.version>8</java.version>
                  </properties>
                  <dependencies>
                    <dependency>
                      <groupId>org.springframework.boot</groupId>
                      <artifactId>spring-boot-starter-web</artifactId>
                    </dependency>
                    <dependency>
                      <groupId>org.springframework.boot</groupId>
                      <artifactId>spring-boot-starter-webflux</artifactId>
                    </dependency>
                    <dependency>
                      <groupId>junit</groupId>
                      <artifactId>junit</artifactId>
                    </dependency>
                  </dependencies>
                </project>
                """);

        String json = service.analyzeDependencies(tempDir.toString());

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("dependencies")).isTrue();
        assertThat(root.has("compatibility")).isTrue();
        assertThat(root.has("outdated")).isTrue();
        assertThat(root.has("conflicts")).isTrue();
        assertThat(root.has("upgradeSuggestions")).isTrue();
        assertThat(root.has("summary")).isTrue();

        JsonNode deps = root.get("dependencies");
        assertThat(deps.get("springBootVersion").asText()).isEqualTo("2.7.5");
        assertThat(deps.get("count").asInt()).isGreaterThanOrEqualTo(3);

        JsonNode compatibility = root.get("compatibility");
        assertThat(compatibility.get("compatible").asBoolean()).isFalse();

        JsonNode outdated = root.get("outdated");
        assertThat(outdated.get("count").asInt()).isGreaterThanOrEqualTo(0);
        assertThat(outdated.get("dependencies").isArray()).isTrue();

        JsonNode conflicts = root.get("conflicts");
        assertThat(conflicts.get("count").asInt()).isGreaterThanOrEqualTo(1);

        JsonNode suggestions = root.get("upgradeSuggestions");
        assertThat(suggestions.get("suggestions").isArray()).isTrue();
    }

    @Test
    @DisplayName("analyzeDependencies should still return structured JSON when path does not exist")
    void analyzeDependencies_invalidPath_returnsStructuredResult() throws Exception {
        String json = service.analyzeDependencies("Z:/definitely/does/not/exist");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("dependencies")).isTrue();
        assertThat(root.has("compatibility")).isTrue();
        assertThat(root.has("outdated")).isTrue();
        assertThat(root.has("conflicts")).isTrue();
        assertThat(root.has("upgradeSuggestions")).isTrue();
        assertThat(root.has("summary")).isTrue();
        assertThat(root.has("error")).isFalse();
    }
}

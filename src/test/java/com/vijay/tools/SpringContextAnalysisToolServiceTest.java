package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SpringContextAnalysisToolServiceTest {

    private ObjectMapper objectMapper;
    private SpringContextAnalysisToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new SpringContextAnalysisToolService(objectMapper);
    }

    @Test
    @DisplayName("analyzeSpringContext should analyze components, beans, autoConfig, scanning and starters for a minimal project")
    void analyzeSpringContext_minimalProject_returnsStructuredAnalysis() throws Exception {
        Path tempDir = Files.createTempDirectory("spring-context-test");
        // minimal pom
        Files.writeString(tempDir.resolve("pom.xml"), """
                <project>
                  <dependencies>
                    <dependency>
                      <groupId>org.springframework.boot</groupId>
                      <artifactId>spring-boot-starter-web</artifactId>
                    </dependency>
                  </dependencies>
                </project>
                """);
        // minimal application class
        Path srcDir = Files.createDirectories(tempDir.resolve("src"));
        Files.writeString(srcDir.resolve("App.java"), """
                import org.springframework.boot.autoconfigure.SpringBootApplication;
                import org.springframework.context.annotation.Bean;

                @SpringBootApplication
                public class App {
                    @Bean
                    public String bean() { return "x"; }
                }
                """);

        String json = service.analyzeSpringContext(tempDir.toString());

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("components")).isTrue();
        assertThat(root.has("beans")).isTrue();
        assertThat(root.has("autoConfiguration")).isTrue();
        assertThat(root.has("componentScanning")).isTrue();
        assertThat(root.has("starters")).isTrue();
        assertThat(root.has("summary")).isTrue();

        JsonNode components = root.get("components");
        assertThat(components.get("count").asInt()).isGreaterThanOrEqualTo(0);

        JsonNode beans = root.get("beans");
        assertThat(beans.get("beanCount").asInt()).isEqualTo(1);

        JsonNode starters = root.get("starters");
        assertThat(starters.get("count").asInt()).isEqualTo(1);
    }

    @Test
    @DisplayName("analyzeSpringContext should still return structured JSON when path does not exist")
    void analyzeSpringContext_invalidPath_returnsStructuredResult() throws Exception {
        String json = service.analyzeSpringContext("Z:/definitely/does/not/exist");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("components")).isTrue();
        assertThat(root.has("beans")).isTrue();
        assertThat(root.has("autoConfiguration")).isTrue();
        assertThat(root.has("componentScanning")).isTrue();
        assertThat(root.has("starters")).isTrue();
        assertThat(root.has("summary")).isTrue();
        assertThat(root.has("error")).isFalse();
    }
}

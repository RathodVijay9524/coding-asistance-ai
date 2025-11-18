package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestGenerationToolServiceTest {

    private ObjectMapper objectMapper;
    private TestGenerationToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new TestGenerationToolService(objectMapper);
    }

    @Test
    @DisplayName("generateTests with type=all should include unit, integration, edge tests, coverage and best practices")
    void generateTests_all_returnsRichResult() throws Exception {
        String code = "public class Sample { public void m() {} }";

        String json = service.generateTests(code, "all", "Spring Boot + JUnit");

        JsonNode root = objectMapper.readTree(json);

        // testability
        JsonNode testability = root.get("testability");
        assertThat(testability).isNotNull();
        assertThat(testability.get("score").asInt()).isBetween(0, 10);
        assertThat(testability.get("rating").asText()).isNotBlank();

        // templates
        assertThat(root.has("unitTests")).isTrue();
        assertThat(root.get("unitTests").asText()).contains("UNIT TEST TEMPLATE");

        assertThat(root.has("integrationTests")).isTrue();
        assertThat(root.get("integrationTests").asText()).contains("INTEGRATION TEST TEMPLATE");

        assertThat(root.has("edgeCaseTests")).isTrue();
        assertThat(root.get("edgeCaseTests").asText()).contains("EDGE CASE TEST TEMPLATE");

        // coverage
        JsonNode coverage = root.get("coverage");
        assertThat(coverage).isNotNull();
        assertThat(coverage.get("estimatedCoverage").asText()).endsWith("%");
        assertThat(coverage.get("methods").asInt()).isGreaterThanOrEqualTo(0);
        assertThat(coverage.get("target").asText()).isEqualTo("80%");
        assertThat(coverage.get("status").asText()).isNotBlank();

        // best practices
        JsonNode best = root.get("bestPractices");
        assertThat(best).isNotNull();
        assertThat(best.get("naming").asText()).isNotBlank();
        assertThat(best.get("structure").asText()).isNotBlank();
        assertThat(best.get("isolation").asText()).isNotBlank();
        assertThat(best.get("coverage").asText()).contains("80%");
        assertThat(best.get("speed").asText()).isNotBlank();
        assertThat(best.get("mocking").asText()).isNotBlank();
        assertThat(best.get("assertions").asText()).isNotBlank();
        // framework-specific advice for Spring
        assertThat(best.get("framework").asText()).contains("@SpringBootTest");
    }

    @Test
    @DisplayName("generateTests with type=unit should only include unit tests and omit integration/edge")
    void generateTests_unitOnly_omitsOtherTypes() throws Exception {
        String code = "public class Sample { public void m() {} }";

        String json = service.generateTests(code, "unit", "JUnit 5");

        JsonNode root = objectMapper.readTree(json);

        assertThat(root.has("unitTests")).isTrue();
        assertThat(root.get("unitTests").asText()).contains("UNIT TEST TEMPLATE");

        assertThat(root.has("integrationTests")).isFalse();
        assertThat(root.has("edgeCaseTests")).isFalse();

        JsonNode best = root.get("bestPractices");
        assertThat(best).isNotNull();
        // framework-specific advice for JUnit
        assertThat(best.get("framework").asText()).contains("@Test");
    }

    @Test
    @DisplayName("generateTests should still return structured JSON when code is null")
    void generateTests_nullCode_returnsStructuredResult() throws Exception {
        String json = service.generateTests(null, "all", "JUnit");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("testability")).isTrue();
        assertThat(root.has("coverage")).isTrue();
        assertThat(root.has("bestPractices")).isTrue();
        assertThat(root.has("error")).isFalse();
    }
}

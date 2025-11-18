package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CodeReviewToolServiceTest {

    private ObjectMapper objectMapper;
    private CodeReviewToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new CodeReviewToolService(objectMapper);
    }

    @Test
    @DisplayName("reviewCode should return structured review with issues, score and rating for Java code")
    void reviewCode_basicJavaCode_returnsStructuredReview() throws Exception {
        String code = """
                public class Example {
                    public void m(int a, int b, int c, int d, int e, int f, int g, int h, int i, int j, int k) {
                        // TODO: refactor this method
                        System.out.println(\"debug\");
                        if (a > 0) {}
                        if (b > 0) {}
                        if (c > 0) {}
                        if (d > 0) {}
                        if (e > 0) {}
                        if (f > 0) {}
                        if (g > 0) {}
                        if (h > 0) {}
                        if (i > 0) {}
                        if (j > 0) {}
                        if (k > 0) {}
                        try {
                            doWork();
                        } catch (Exception e) {
                            // generic
                        }
                    }
                }
                """;

        String json = service.reviewCode(code, "java", "Example.java");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("structure")).isTrue();
        assertThat(root.has("issues")).isTrue();
        assertThat(root.has("bestPractices")).isTrue();
        assertThat(root.has("performance")).isTrue();
        assertThat(root.has("security")).isTrue();
        assertThat(root.has("suggestions")).isTrue();
        assertThat(root.has("score")).isTrue();
        assertThat(root.has("rating")).isTrue();
        assertThat(root.has("summary")).isTrue();

        JsonNode structure = root.get("structure");
        assertThat(structure.get("totalLines").asInt()).isGreaterThan(0);

        JsonNode issues = root.get("issues");
        assertThat(issues.isArray()).isTrue();

        JsonNode suggestions = root.get("suggestions");
        assertThat(suggestions.isArray()).isTrue();
        assertThat(suggestions.size()).isGreaterThan(0);

        assertThat(root.get("rating").asText()).isNotBlank();
        assertThat(root.get("summary").asText()).isNotBlank();
    }

    @Test
    @DisplayName("reviewCode should flag security vulnerabilities and set severity HIGH when dangerous patterns present")
    void reviewCode_securityIssues_setsHighSeverity() throws Exception {
        String code = """
                public class Dangerous {
                    public void hack(String userInput) {
                        String password = \"secret\"; // hardcoded
                        eval(\"doSomething\");
                        String sql = \"SELECT * FROM users WHERE name='\" + userInput + \"'\";
                    }
                }
                """;

        String json = service.reviewCode(code, "java", "Dangerous.java");

        JsonNode root = objectMapper.readTree(json);
        JsonNode security = root.get("security");
        assertThat(security).isNotNull();

        JsonNode vulnerabilities = security.get("vulnerabilities");
        assertThat(vulnerabilities.isArray()).isTrue();
        assertThat(vulnerabilities.size()).isGreaterThan(0);

        assertThat(security.get("severity").asText()).isEqualTo("HIGH");
    }

    @Test
    @DisplayName("reviewCode should return error JSON when code is null and analysis fails")
    void reviewCode_nullCode_returnsError() throws Exception {
        String json = service.reviewCode(null, "java", "Example.java");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("structure")).isTrue();
        JsonNode structure = root.get("structure");
        assertThat(structure.has("error")).isTrue();
        assertThat(structure.get("error").asText()).isNotBlank();
    }
}

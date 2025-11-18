package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BugDetectionToolServiceTest {

    private ObjectMapper objectMapper;
    private BugDetectionToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new BugDetectionToolService(objectMapper);
    }

    @Test
    @DisplayName("detectBugs should detect null pointer and resource leak issues and return JSON summary")
    void detectBugs_allTypes_detectsIssues() throws Exception {
        String code = """
                public void test() {
                    FileInputStream in = new FileInputStream(path);
                    Object value = repo.find(id).getName();
                    if (flag = true) {
                        // logic error: assignment in condition
                    }
                    List list = new ArrayList();
                }
                """;

        String json = service.detectBugs(code, "java", "all");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("nullPointerBugs")).isTrue();
        assertThat(root.get("nullPointerBugs").size()).isGreaterThanOrEqualTo(1);
        assertThat(root.has("resourceLeaks")).isTrue();
        assertThat(root.has("logicErrors")).isTrue();
        assertThat(root.has("typeIssues")).isTrue();
        assertThat(root.get("summary").asText()).contains("Found");
    }

    @Test
    @DisplayName("detectBugs should handle specific bugType filter")
    void detectBugs_specificType() throws Exception {
        String code = "FileInputStream in = new FileInputStream(path);";

        String json = service.detectBugs(code, "java", "resource");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("resourceLeaks")).isTrue();
        assertThat(root.has("nullPointerBugs")).isFalse();
    }

    @Test
    @DisplayName("detectBugs should return an error JSON when exception occurs")
    void detectBugs_errorPath() throws Exception {
        // Use a service with a broken ObjectMapper to force serialization error
        BugDetectionToolService brokenService = new BugDetectionToolService(new ObjectMapper() {
            @Override
            public String writeValueAsString(Object value) throws com.fasterxml.jackson.core.JsonProcessingException {
                throw new com.fasterxml.jackson.core.JsonProcessingException("boom") {};
            }
        });

        String json = brokenService.detectBugs("code", "java", "all");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("error")).isTrue();
    }
}

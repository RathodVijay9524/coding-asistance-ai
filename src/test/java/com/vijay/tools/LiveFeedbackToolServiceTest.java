package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LiveFeedbackToolServiceTest {

    private ObjectMapper objectMapper;
    private LiveFeedbackToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new LiveFeedbackToolService(objectMapper);
    }

    @Test
    @DisplayName("getLiveFeedback should include analysis, aiFeedback, suggestions, and summary")
    void getLiveFeedback_basic() throws Exception {
        Path temp = Files.createTempFile("livefb", ".java");
        Files.writeString(temp, "// TODO test file\npublic class A {}\n");

        String json = service.getLiveFeedback(temp.toString(), "modified");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("fileAnalysis")).isTrue();
        assertThat(root.has("aiFeedback")).isTrue();
        assertThat(root.has("suggestions")).isTrue();
        assertThat(root.has("summary")).isTrue();
    }

    @Test
    @DisplayName("getChangeHistory should return history array")
    void getChangeHistory_basic() throws Exception {
        String json = service.getChangeHistory("/tmp/file.java", 3);

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("history").isArray()).isTrue();
        assertThat(root.get("totalChanges").asInt()).isGreaterThan(0);
    }
}

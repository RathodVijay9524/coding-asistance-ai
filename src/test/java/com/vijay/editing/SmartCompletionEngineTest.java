package com.vijay.editing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SmartCompletionEngineTest {

    private ObjectMapper objectMapper;
    private SmartCompletionEngine engine;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        engine = new SmartCompletionEngine(objectMapper);
    }

    @Test
    @DisplayName("getCompletions should return method completions and metadata for method partial input")
    void getCompletions_methodPartial() throws Exception {
        String code = "public class Sample {\n public String toString() { return \"x\"; }\n}";

        String json = engine.getCompletions(code, 2, 5, "to");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("completions").isArray()).isTrue();
    }

    @Test
    @DisplayName("getContextAwareCompletions should return context-specific completions")
    void getContextAwareCompletions_usesContext() throws Exception {
        String context = "public class Sample {\n String s = new String();\n }";

        String json = engine.getContextAwareCompletions(context, "Str");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("completions").isArray()).isTrue();
        assertThat(root.get("count").asInt()).isGreaterThanOrEqualTo(1);
    }
}

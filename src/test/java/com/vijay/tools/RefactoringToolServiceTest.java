package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RefactoringToolServiceTest {

    private ObjectMapper objectMapper;
    private RefactoringToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new RefactoringToolService(objectMapper);
    }

    @Test
    @DisplayName("suggestRefactoring should return JSON with structure, patterns, duplication, and summary")
    void suggestRefactoring_allFocus_returnsRichResult() throws Exception {
        String code = """
                public class Sample {
                    public void method() {
                        if (a) { }
                        else if (b) { }
                        else if (c) { }
                        if (a) { }
                        if (a) { }
                        new Foo(); new Foo(); new Foo(); new Foo(); new Foo(); new Foo();
                        for (int i = 0; i < 10; i++) {
                            for (int j = 0; j < 10; j++) { }
                        }
                        int tmp = 0; String data = "x";
                    }
                }
                """;

        String json = service.suggestRefactoring(code, "all", "java");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("structure")).isTrue();
        assertThat(root.has("designPatterns")).isTrue();
        assertThat(root.has("duplication")).isTrue();
        assertThat(root.has("methodExtraction")).isTrue();
        assertThat(root.has("namingImprovements")).isTrue();
        assertThat(root.has("aiSuggestions")).isTrue();
        assertThat(root.has("refactoredCode")).isTrue();
        assertThat(root.get("summary").asText()).isNotBlank();
    }
}

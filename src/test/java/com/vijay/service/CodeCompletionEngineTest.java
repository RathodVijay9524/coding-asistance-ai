package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CodeCompletionEngineTest {

    private CodeCompletionEngine engine;

    @BeforeEach
    void setUp() {
        engine = new CodeCompletionEngine();
    }

    @Test
    @DisplayName("suggestNextLine should return default comment for unknown pattern")
    void suggestNextLine_default() {
        List<String> suggestions = engine.suggestNextLine("int a = 1;", "", "java");
        assertThat(suggestions).isNotEmpty();
        assertThat(suggestions.get(0)).contains("Continue implementation");
    }

    @Test
    @DisplayName("suggestNextLine should respond to if and for patterns")
    void suggestNextLine_ifAndFor() {
        List<String> ifSuggestions = engine.suggestNextLine("if (a > 0) {", "", "java");
        assertThat(ifSuggestions).anyMatch(s -> s.contains("Handle condition"));

        List<String> forSuggestions = engine.suggestNextLine("for (int i = 0; i < n; i++) {", "", "java");
        assertThat(forSuggestions).anyMatch(s -> s.contains("Loop body"));
    }

    @Test
    @DisplayName("suggestMethodNames should include get/fetch for retrieval context")
    void suggestMethodNames_retrieval() {
        List<String> names = engine.suggestMethodNames("get all users", "java");
        assertThat(names).contains("get", "fetch", "retrieve", "load");
    }

    @Test
    @DisplayName("suggestVariableNames should respond to list and string contexts")
    void suggestVariableNames_contexts() {
        List<String> listNames = engine.suggestVariableNames("list of users", "java");
        assertThat(listNames).contains("items", "elements");

        List<String> stringNames = engine.suggestVariableNames("string message", "java");
        assertThat(stringNames).contains("text", "message");
    }

    @Test
    @DisplayName("getCompletions should include keywords, variables and methods for java prefix")
    void getCompletions_java() {
        List<CodeCompletionEngine.Completion> completions =
                engine.getCompletions("pub", "", "java");

        assertThat(completions)
                .extracting(c -> c.text)
                .anyMatch(t -> t.startsWith("public"));
    }
}

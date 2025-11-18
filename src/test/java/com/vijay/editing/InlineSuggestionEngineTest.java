package com.vijay.editing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class InlineSuggestionEngineTest {

    private ObjectMapper objectMapper;
    private CodeSelectionAnalyzer codeSelectionAnalyzer;
    private ChatClient chatClient;
    private InlineSuggestionEngine engine;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        codeSelectionAnalyzer = mock(CodeSelectionAnalyzer.class);
        chatClient = Mockito.mock(ChatClient.class, Mockito.RETURNS_DEEP_STUBS);
        engine = new InlineSuggestionEngine(objectMapper, codeSelectionAnalyzer, chatClient);
    }

    @Test
    @DisplayName("suggestMethodExtractions should suggest extraction when many statements present")
    void suggestMethodExtractions_suggestsWhenComplex() {
        String context = "int a = 1; int b = 2; int c = 3; int d = 4;";
        List<InlineSuggestionEngine.InlineSuggestion> suggestions =
                engine.suggestMethodExtractions("full", context);

        assertThat(suggestions).isNotEmpty();
        assertThat(suggestions.get(0).getType()).isEqualTo("Extract Method");
    }

    @Test
    @DisplayName("suggestVariableRenamings should suggest renaming single-letter variables")
    void suggestVariableRenamings_detectsSingleLetterVars() {
        String context = "int a = 1; int b = 2;";
        List<InlineSuggestionEngine.InlineSuggestion> suggestions =
                engine.suggestVariableRenamings("full", context);

        assertThat(suggestions).isNotEmpty();
        assertThat(suggestions.get(0).getType()).isEqualTo("Rename Variable");
    }

    @Test
    @DisplayName("getSuggestionsAtPosition should return JSON with suggestions array")
    void getSuggestionsAtPosition_returnsJson() throws Exception {
        String code = "public void test() { int a = 1; int b = 2; int c = 3; int d = 4; }";

        String json = engine.getSuggestionsAtPosition(code, 1, 10);

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("suggestions").isArray()).isTrue();
        assertThat(root.get("count").asInt()).isGreaterThanOrEqualTo(1);
    }
}

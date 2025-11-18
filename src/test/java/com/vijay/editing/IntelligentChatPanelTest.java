package com.vijay.editing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class IntelligentChatPanelTest {

    private ObjectMapper objectMapper;
    private CodeSelectionAnalyzer codeSelectionAnalyzer;
    private EditSuggestionGenerator editSuggestionGenerator;
    private IntelligentChatPanel panel;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        codeSelectionAnalyzer = mock(CodeSelectionAnalyzer.class);
        editSuggestionGenerator = mock(EditSuggestionGenerator.class);
        panel = new IntelligentChatPanel(objectMapper, codeSelectionAnalyzer, editSuggestionGenerator);
    }

    @Test
    @DisplayName("processInlineChat should return structured response with intent and suggestions")
    void processInlineChat_basic() throws Exception {
        String message = "Can you extract this into a method?";
        String selectedCode = "public void m() { int a=1; int b=2; }";

        String json = panel.processInlineChat(message, selectedCode, "// file context");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        JsonNode response = root.get("response");
        assertThat(response.get("intent").asText()).isNotBlank();
    }

    @Test
    @DisplayName("getQuickSuggestions should return suggestions based on selection analysis")
    void getQuickSuggestions_basic() throws Exception {
        String code = "public void m() { int a=1; int b=2; int c=3; int d=4; int e=5; int f=6; }";
        CodeSelectionAnalyzer.SelectionAnalysis analysis = new CodeSelectionAnalyzer.SelectionAnalysis();
        analysis.setLineCount(20);
        analysis.setComplexity(10);
        analysis.setVariables(java.util.List.of("a", "b", "c", "d", "e", "f"));
        org.mockito.Mockito.when(codeSelectionAnalyzer.analyzeSelection(code)).thenReturn(analysis);

        String json = panel.getQuickSuggestions(code);

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("suggestions").isArray()).isTrue();
    }
}

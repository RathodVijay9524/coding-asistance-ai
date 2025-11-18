package com.vijay.editing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class LiveCodeEditorTest {

    private ObjectMapper objectMapper;
    private CodeSelectionAnalyzer codeSelectionAnalyzer;
    private EditSuggestionGenerator editSuggestionGenerator;
    private LiveCodeEditor editor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        codeSelectionAnalyzer = mock(CodeSelectionAnalyzer.class);
        editSuggestionGenerator = mock(EditSuggestionGenerator.class);
        editor = new LiveCodeEditor(objectMapper, codeSelectionAnalyzer, editSuggestionGenerator);
    }

    @Test
    @DisplayName("suggestEdits should include analysis summary and suggestions list")
    void suggestEdits_basic() throws Exception {
        String selectedCode = "public void m() { int a = 1; }";
        String instruction = "Refactor method";
        String context = "// file context";

        CodeSelectionAnalyzer.SelectionAnalysis analysis = new CodeSelectionAnalyzer.SelectionAnalysis();
        analysis.setLineCount(3);
        analysis.setComplexity(5);
        analysis.setMaintainability(80);
        analysis.setMethods(List.of());
        analysis.setIssues(List.of());

        when(codeSelectionAnalyzer.analyzeSelection(selectedCode)).thenReturn(analysis);

        EditSuggestionGenerator.EditSuggestion suggestion = new EditSuggestionGenerator.EditSuggestion();
        suggestion.setType("REFACTOR");
        suggestion.setDescription("Refactor method body");
        suggestion.setOriginalCode(selectedCode);
        suggestion.setSuggestedCode("public void m() { int a = 2; }");
        suggestion.setConfidence(0.9);

        when(editSuggestionGenerator.generateSuggestions(selectedCode, instruction))
                .thenReturn(List.of(suggestion));

        String json = editor.suggestEdits(selectedCode, instruction, context);

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("selectedCode").asText()).isEqualTo(selectedCode);
        assertThat(root.get("instruction").asText()).isEqualTo(instruction);

        JsonNode analysisNode = root.get("analysis");
        assertThat(analysisNode.get("lineCount").asInt()).isEqualTo(3);
        assertThat(analysisNode.get("complexity").asInt()).isEqualTo(5);
        assertThat(analysisNode.get("maintainability").asInt()).isEqualTo(80);

        JsonNode suggestions = root.get("suggestions");
        assertThat(suggestions.isArray()).isTrue();
        assertThat(root.get("suggestionsCount").asInt()).isEqualTo(1);
    }

    @Test
    @DisplayName("previewEdit should return diff, impact and safe=true when edit is safe")
    void previewEdit_safe() throws Exception {
        String original = "public void m() {\n  int a = 1;\n}";
        String suggested = "public void m() {\n  int a = 2;\n}";

        String json = editor.previewEdit(original, suggested, "REFACTOR");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("editType").asText()).isEqualTo("REFACTOR");
        assertThat(root.get("originalCode").asText()).contains("int a = 1");
        assertThat(root.get("suggestedCode").asText()).contains("int a = 2");

        JsonNode diff = root.get("diff");
        assertThat(diff.isArray()).isTrue();
        assertThat(diff.size()).isGreaterThan(0);

        JsonNode impact = root.get("impact");
        assertThat(impact.get("linesChanged").asInt()).isGreaterThanOrEqualTo(1);
        assertThat(impact.get("riskLevel").asText()).isIn("Low", "Medium", "High");

        assertThat(root.get("safe").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("applyEdit should return success and include report and rollback info when validation passes")
    void applyEdit_success() throws Exception {
        String original = "public void m() {\n  int a = 1;\n}";
        String suggested = "public void m() {\n  int a = 2;\n}";

        String json = editor.applyEdit(original, suggested, "REFACTOR");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("editType").asText()).isEqualTo("REFACTOR");
        assertThat(root.get("editedCode").asText()).contains("int a = 2");

        JsonNode report = root.get("report");
        assertThat(report.get("editType").asText()).isEqualTo("REFACTOR");
        assertThat(report.get("linesChanged").asInt()).isGreaterThanOrEqualTo(1);

        JsonNode rollback = root.get("rollbackInfo");
        assertThat(rollback.get("originalCode").asText()).contains("int a = 1");
        assertThat(rollback.get("canRollback").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("applyEdit should return error status when validation fails")
    void applyEdit_validationFails() throws Exception {
        String original = "public void m() {\n  int a = 1;\n}";
        // Missing 'public' and unmatched brace to trigger breaking change and syntax error
        String suggested = "void m() {\n  int a = 2;";

        String json = editor.applyEdit(original, suggested, "REFACTOR");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("error");
        assertThat(root.get("message").asText()).isEqualTo("Edit validation failed");

        JsonNode errors = root.get("errors");
        assertThat(errors.isArray()).isTrue();
        assertThat(errors.size()).isGreaterThan(0);
    }
}

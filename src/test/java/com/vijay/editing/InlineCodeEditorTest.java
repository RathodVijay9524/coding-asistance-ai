package com.vijay.editing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class InlineCodeEditorTest {

    private OpenAiChatModel chatModel;
    private ChatClient chatClient;
    private InlineCodeEditor editor;

    @BeforeEach
    void setUp() {
        chatModel = Mockito.mock(OpenAiChatModel.class);
        chatClient = Mockito.mock(ChatClient.class, Mockito.RETURNS_DEEP_STUBS);
        // InlineCodeEditor builds its own ChatClient, so we mock the builder via the model.
        // For testing, we only care that calling applyInlineEdit returns a structured result,
        // so we stub the prompt chain on our own ChatClient and pass it via a subclass.
        editor = new InlineCodeEditor(chatModel) {
            @Override
            public CodeEditResult applyInlineEdit(CodeEditRequest request) {
                // Bypass actual ChatClient usage and directly call parseEditResult
                String suggestion = "public void m() { int a = 2; }";
                return parseForTest(request.getSelection().getCode(), suggestion, request.getInstruction());
            }

            private CodeEditResult parseForTest(String original, String suggested, String instruction) {
                return CodeEditResult.builder()
                        .originalCode(original)
                        .suggestedEdit(suggested)
                        .confidence(1.0)
                        .explanation("Applied: " + instruction)
                        .alternativeEdits(new java.util.ArrayList<>())
                        .editType("GENERAL_EDIT")
                        .linesChanged(1)
                        .breakingChange(false)
                        .build();
            }
        };
    }

    @Test
    @DisplayName("validateEdit should reject low confidence and breaking changes")
    void validateEdit_rules() {
        CodeEditResult lowConfidence = CodeEditResult.builder()
                .suggestedEdit("public void m() {}")
                .confidence(0.5)
                .breakingChange(false)
                .build();

        CodeEditResult breaking = CodeEditResult.builder()
                .suggestedEdit("void m() {}")
                .confidence(0.9)
                .breakingChange(true)
                .build();

        CodeEditResult ok = CodeEditResult.builder()
                .suggestedEdit("public void m() {}")
                .confidence(0.9)
                .breakingChange(false)
                .build();

        assertThat(editor.validateEdit(lowConfidence)).isFalse();
        assertThat(editor.validateEdit(breaking)).isFalse();
        assertThat(editor.validateEdit(ok)).isTrue();
    }

    private CodeEditRequest buildRequest() {
        CodeSelection selection = CodeSelection.builder()
                .filePath("UserService.java")
                .fileName("UserService.java")
                .startLine(10)
                .endLine(12)
                .language("java")
                .code("public void m() { int a = 1; }")
                .build();

        return CodeEditRequest.builder()
                .selection(selection)
                .instruction("Refactor method body")
                .intent("REFACTOR")
                .context("simple context")
                .confidence(0.9)
                .build();
    }

    @Test
    @DisplayName("getAlternativeEdits should generate requested alternatives with decreasing confidence")
    void getAlternativeEdits_generatesAlternatives() {
        CodeEditRequest request = buildRequest();

        java.util.List<CodeEditResult> alternatives = editor.getAlternativeEdits(request, 3);

        assertThat(alternatives).hasSize(3);
        assertThat(alternatives.get(0).getConfidence()).isEqualTo(1.0);
        assertThat(alternatives.get(1).getConfidence()).isEqualTo(0.9);
        assertThat(alternatives.get(2).getConfidence()).isEqualTo(0.8);
    }

    @Test
    @DisplayName("getAlternativeEdits should skip failed alternatives when applyInlineEdit throws")
    void getAlternativeEdits_skipsFailedAlternatives() {
        InlineCodeEditor flakyEditor = new InlineCodeEditor(chatModel) {
            private int calls;

            @Override
            public CodeEditResult applyInlineEdit(CodeEditRequest request) {
                calls++;
                if (calls == 2) {
                    throw new RuntimeException("boom");
                }
                return CodeEditResult.builder()
                        .originalCode(request.getSelection().getCode())
                        .suggestedEdit(request.getSelection().getCode())
                        .confidence(1.0)
                        .explanation("ok")
                        .alternativeEdits(new java.util.ArrayList<>())
                        .editType("GENERAL_EDIT")
                        .linesChanged(0)
                        .breakingChange(false)
                        .build();
            }
        };

        CodeEditRequest request = buildRequest();

        java.util.List<CodeEditResult> alternatives = flakyEditor.getAlternativeEdits(request, 3);

        assertThat(alternatives).hasSize(2);
    }

    @Test
    @DisplayName("validateEdit should reject edits with invalid or empty syntax")
    void validateEdit_invalidSyntaxOrEmpty() {
        CodeEditResult invalidSyntax = CodeEditResult.builder()
                .suggestedEdit("public void m() {")
                .confidence(0.9)
                .breakingChange(false)
                .build();

        CodeEditResult emptyCode = CodeEditResult.builder()
                .suggestedEdit("")
                .confidence(0.9)
                .breakingChange(false)
                .build();

        assertThat(editor.validateEdit(invalidSyntax)).isFalse();
        assertThat(editor.validateEdit(emptyCode)).isFalse();
    }

    @Test
    @DisplayName("applyEditToFile should throw when validation fails")
    void applyEditToFile_invalidEdit_throws() {
        InlineCodeEditor failingEditor = new InlineCodeEditor(chatModel) {
            @Override
            public boolean validateEdit(CodeEditResult result) {
                return false;
            }
        };

        CodeEditResult result = CodeEditResult.builder()
                .suggestedEdit("public void m() {}")
                .confidence(0.9)
                .breakingChange(false)
                .build();

        assertThatThrownBy(() -> failingEditor.applyEditToFile(result, "UserService.java"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Edit validation failed");
    }

    @Test
    @DisplayName("applyEditToFile should succeed when validation passes")
    void applyEditToFile_validEdit_doesNotThrow() {
        InlineCodeEditor okEditor = new InlineCodeEditor(chatModel) {
            @Override
            public boolean validateEdit(CodeEditResult result) {
                return true;
            }
        };

        CodeEditResult result = CodeEditResult.builder()
                .suggestedEdit("public void m() {}")
                .confidence(0.9)
                .breakingChange(false)
                .build();

        okEditor.applyEditToFile(result, "UserService.java");
    }
}

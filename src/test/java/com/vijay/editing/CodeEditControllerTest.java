package com.vijay.editing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CodeEditControllerTest {

    private InlineCodeEditor inlineCodeEditor;
    private CodeEditController controller;

    @BeforeEach
    void setUp() {
        inlineCodeEditor = mock(InlineCodeEditor.class);
        controller = new CodeEditController(inlineCodeEditor);
    }

    private CodeEditRequest buildRequest() {
        CodeSelection selection = CodeSelection.builder()
                .filePath("/path/UserService.java")
                .fileName("UserService.java")
                .startLine(10)
                .endLine(20)
                .language("java")
                .code("public void processUser(User user) { }")
                .build();

        return CodeEditRequest.builder()
                .selection(selection)
                .instruction("Extract validation logic")
                .intent("REFACTOR")
                .context("User wants more readable code")
                .confidence(0.9)
                .build();
    }

    private CodeEditResult buildResult() {
        return CodeEditResult.builder()
                .originalCode("public void processUser(User user) { }")
                .suggestedEdit("public void processUser(User user) { validateUser(user); }")
                .confidence(0.9)
                .explanation("Extracted validation to separate method")
                .alternativeEdits(List.of())
                .editType("REFACTOR")
                .linesChanged(1)
                .breakingChange(false)
                .build();
    }

    @Test
    @DisplayName("applyInlineEdit should delegate to InlineCodeEditor and wrap result in success response")
    void applyInlineEdit_success() {
        CodeEditRequest request = buildRequest();
        CodeEditResult result = buildResult();

        when(inlineCodeEditor.applyInlineEdit(any(CodeEditRequest.class))).thenReturn(result);

        ResponseEntity<?> responseEntity = controller.applyInlineEdit(request);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        Map<?,?> body = (Map<?,?>) responseEntity.getBody();
        assertThat(body.get("status")).isEqualTo("success");
        assertThat(body.get("original")).isEqualTo(result.getOriginalCode());
        assertThat(body.get("suggested")).isEqualTo(result.getSuggestedEdit());
        assertThat(body.get("confidence")).isEqualTo(result.getConfidence());
        assertThat(body.get("editType")).isEqualTo(result.getEditType());
        assertThat(body.get("linesChanged")).isEqualTo(result.getLinesChanged());
        assertThat(body.get("breakingChange")).isEqualTo(result.isBreakingChange());

        verify(inlineCodeEditor).applyInlineEdit(eq(request));
    }

    @Test
    @DisplayName("applyInlineEdit should return error response when InlineCodeEditor throws")
    void applyInlineEdit_error() {
        CodeEditRequest request = buildRequest();

        when(inlineCodeEditor.applyInlineEdit(any(CodeEditRequest.class)))
                .thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> responseEntity = controller.applyInlineEdit(request);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(500);
        Map<?,?> body = (Map<?,?>) responseEntity.getBody();
        assertThat(body.get("status")).isEqualTo("error");
        assertThat(body.get("message").toString()).contains("boom");
    }

    @Test
    @DisplayName("getAlternativeEdits should return alternatives count and list")
    void getAlternativeEdits_success() {
        CodeEditRequest request = buildRequest();
        CodeEditResult result = buildResult();

        when(inlineCodeEditor.getAlternativeEdits(eq(request), eq(3))).thenReturn(List.of(result, result));

        ResponseEntity<?> responseEntity = controller.getAlternativeEdits(request, 3);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        Map<?,?> body = (Map<?,?>) responseEntity.getBody();
        assertThat(body.get("status")).isEqualTo("success");
        assertThat(body.get("count")).isEqualTo(2);
        assertThat((List<?>) body.get("alternatives")).hasSize(2);

        verify(inlineCodeEditor).getAlternativeEdits(eq(request), eq(3));
    }

    @Test
    @DisplayName("getAlternativeEdits should return error response when InlineCodeEditor throws")
    void getAlternativeEdits_error() {
        CodeEditRequest request = buildRequest();

        when(inlineCodeEditor.getAlternativeEdits(eq(request), eq(3)))
                .thenThrow(new RuntimeException("fail"));

        ResponseEntity<?> responseEntity = controller.getAlternativeEdits(request, 3);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(500);
        Map<?,?> body = (Map<?,?>) responseEntity.getBody();
        assertThat(body.get("status")).isEqualTo("error");
        assertThat(body.get("message").toString()).contains("fail");
    }

    @Test
    @DisplayName("validateEdit should delegate to InlineCodeEditor and expose validation result")
    void validateEdit_success() {
        CodeEditResult result = buildResult();

        when(inlineCodeEditor.validateEdit(result)).thenReturn(true);

        ResponseEntity<?> responseEntity = controller.validateEdit(result);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        Map<?,?> body = (Map<?,?>) responseEntity.getBody();
        assertThat(body.get("status")).isEqualTo("success");
        assertThat(body.get("valid")).isEqualTo(true);
        assertThat(body.get("confidence")).isEqualTo(result.getConfidence());
        assertThat(body.get("breakingChange")).isEqualTo(result.isBreakingChange());

        verify(inlineCodeEditor).validateEdit(result);
    }

    @Test
    @DisplayName("validateEdit should return error response when InlineCodeEditor throws")
    void validateEdit_error() {
        CodeEditResult result = buildResult();

        when(inlineCodeEditor.validateEdit(result)).thenThrow(new RuntimeException("oops"));

        ResponseEntity<?> responseEntity = controller.validateEdit(result);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(500);
        Map<?,?> body = (Map<?,?>) responseEntity.getBody();
        assertThat(body.get("status")).isEqualTo("error");
        assertThat(body.get("message").toString()).contains("oops");
    }

    @Test
    @DisplayName("applyEditToFile should call InlineCodeEditor and return success message")
    void applyEditToFile_success() {
        CodeEditResult result = buildResult();
        String filePath = "/path/UserService.java";

        ResponseEntity<?> responseEntity = controller.applyEditToFile(filePath, result);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        Map<?,?> body = (Map<?,?>) responseEntity.getBody();
        assertThat(body.get("status")).isEqualTo("success");
        assertThat(body.get("message")).isEqualTo("Edit applied successfully");
        assertThat(body.get("filePath")).isEqualTo(filePath);
        assertThat(body.get("linesChanged")).isEqualTo(result.getLinesChanged());

        verify(inlineCodeEditor).applyEditToFile(result, filePath);
    }

    @Test
    @DisplayName("applyEditToFile should return error response when InlineCodeEditor throws")
    void applyEditToFile_error() {
        CodeEditResult result = buildResult();
        String filePath = "/path/UserService.java";

        doThrow(new RuntimeException("file error"))
                .when(inlineCodeEditor).applyEditToFile(result, filePath);

        ResponseEntity<?> responseEntity = controller.applyEditToFile(filePath, result);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(500);
        Map<?,?> body = (Map<?,?>) responseEntity.getBody();
        assertThat(body.get("status")).isEqualTo("error");
        assertThat(body.get("message").toString()).contains("file error");
    }

    @Test
    @DisplayName("health should return healthy status for InlineCodeEditor component")
    void health_basic() {
        ResponseEntity<?> responseEntity = controller.health();

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        Map<?,?> body = (Map<?,?>) responseEntity.getBody();
        assertThat(body.get("status")).isEqualTo("healthy");
        assertThat(body.get("component")).isEqualTo("InlineCodeEditor");
    }
}

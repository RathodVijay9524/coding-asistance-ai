package com.vijay.editing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CodeEditRequestTest {

    @Test
    @DisplayName("getFullContext should include selected code, instruction, intent, and context")
    void getFullContext_includesAllFields() {
        CodeSelection selection = CodeSelection.builder()
                .filePath("/path/Demo.java")
                .fileName("Demo.java")
                .startLine(10)
                .endLine(20)
                .language("java")
                .code("public void demo() { int x = 1; }")
                .build();

        CodeEditRequest request = CodeEditRequest.builder()
                .selection(selection)
                .instruction("Extract method")
                .intent("REFACTOR")
                .context("User wants to improve readability")
                .confidence(0.9)
                .build();

        String full = request.getFullContext();

        assertThat(full).contains("SELECTED CODE:");
        assertThat(full).contains(selection.getCode());
        assertThat(full).contains("INSTRUCTION: Extract method");
        assertThat(full).contains("INTENT: REFACTOR");
        assertThat(full).contains("CONTEXT: User wants to improve readability");
    }
}

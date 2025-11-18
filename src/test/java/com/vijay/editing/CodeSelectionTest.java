package com.vijay.editing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CodeSelectionTest {

    @Test
    @DisplayName("getContext should format selection information for display")
    void getContext_formatsSelection() {
        CodeSelection selection = CodeSelection.builder()
                .filePath("/path/Demo.java")
                .fileName("Demo.java")
                .startLine(5)
                .endLine(10)
                .language("java")
                .code("public void demo() { int x = 1; }")
                .build();

        String context = selection.getContext(2);

        assertThat(context).contains("File: Demo.java (lines 5-10)");
        assertThat(context).contains("Language: java");
        assertThat(context).contains("public void demo()");
    }
}

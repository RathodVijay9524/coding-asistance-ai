package com.vijay.editing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CodeEditResultTest {

    @Test
    @DisplayName("getFormattedResult should include key fields and show breaking change as YES")
    void getFormattedResult_breakingChangeYes() {
        CodeEditResult result = CodeEditResult.builder()
                .originalCode("int x = 1;")
                .suggestedEdit("int x = 2;")
                .confidence(0.9)
                .explanation("Changed initial value")
                .linesChanged(1)
                .editType("REFACTOR")
                .breakingChange(true)
                .build();

        String formatted = result.getFormattedResult();

        assertThat(formatted).contains("ORIGINAL:\nint x = 1;");
        assertThat(formatted).contains("SUGGESTED:\nint x = 2;");
        assertThat(formatted).contains("CONFIDENCE: 90.00%");
        assertThat(formatted).contains("EXPLANATION: Changed initial value");
        assertThat(formatted).contains("LINES CHANGED: 1");
        assertThat(formatted).contains("BREAKING CHANGE: ⚠️ YES");
    }

    @Test
    @DisplayName("getFormattedResult should show breaking change as NO when not breaking")
    void getFormattedResult_breakingChangeNo() {
        CodeEditResult result = CodeEditResult.builder()
                .originalCode("int x = 1;")
                .suggestedEdit("int x = 2;")
                .confidence(0.9)
                .explanation("Changed initial value")
                .linesChanged(1)
                .editType("REFACTOR")
                .breakingChange(false)
                .build();

        String formatted = result.getFormattedResult();

        assertThat(formatted).contains("BREAKING CHANGE: ✅ NO");
    }
}

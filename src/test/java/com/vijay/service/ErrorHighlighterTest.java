package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorHighlighterTest {

    private ErrorHighlighter highlighter;

    @BeforeEach
    void setUp() {
        highlighter = new ErrorHighlighter();
    }

    @Test
    @DisplayName("highlightErrors should return empty list for empty code")
    void highlightErrors_emptyCode() {
        List<ErrorHighlighter.ErrorHighlight> errors = highlighter.highlightErrors("", "Java");
        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("highlightErrors should detect syntax, logic, and style issues")
    void highlightErrors_detectsMultipleIssueTypes() {
        String code = "int a = 5\n" +
                "{\n" +
                "if(a > 0\n" +
                "Optional v = opt.get()\n" +
                "while(true){}\n" +
                "return\n" +
                "var x = 1;\n" +
                "  badIndent();\n" +
                "if(true) {}\n";

        List<ErrorHighlighter.ErrorHighlight> errors = highlighter.highlightErrors(code, "Java");

        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> "Missing semicolon".equals(e.title));
        assertThat(errors).anyMatch(e -> "Potential null pointer".equals(e.title));
        assertThat(errors).anyMatch(e -> "STYLE".equals(e.type));
    }

    @Test
    @DisplayName("explainError and suggestFix should use ErrorHighlight data")
    void explainError_and_suggestFix() {
        ErrorHighlighter.ErrorHighlight error = new ErrorHighlighter.ErrorHighlight(
                10,
                "Missing semicolon",
                "Statement should end with semicolon",
                "Add ; at end of line",
                "SYNTAX"
        );

        String explanation = highlighter.explainError(error);
        String fix = highlighter.suggestFix(error);

        assertThat(explanation).contains("line 10");
        assertThat(explanation).contains("Statement should end with semicolon");
        assertThat(fix).isEqualTo("Add ; at end of line");
    }
}

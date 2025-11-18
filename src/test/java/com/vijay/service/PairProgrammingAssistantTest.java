package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class PairProgrammingAssistantTest {

    @Mock
    private CodeIntelligenceEngine codeIntelligenceEngine;

    @InjectMocks
    private PairProgrammingAssistant assistant;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("provideSuggestions should map intelligence into issues, alternatives, steps and reasoning")
    void provideSuggestions_mapsIntelligence() {
        // Include 'new FileInputStream' so offerAlternatives detects resource management alternative
        String code = "@Component class Demo { void test() { while(true){} FileInputStream fis = new FileInputStream(\"f\"); } }";

        CodeIntelligenceEngine.CodeIntelligence intelligence = new CodeIntelligenceEngine.CodeIntelligence();
        intelligence.setBugs(Collections.singletonList(
                new CodeIntelligenceEngine.Bug("Potential null pointer", "Missing null check", "HIGH")
        ));
        intelligence.setRefactorings(Collections.singletonList(
                new CodeIntelligenceEngine.Refactoring("Extract method", "Method too long", "MEDIUM")
        ));
        intelligence.setPerformanceIssues(Collections.singletonList(
                new CodeIntelligenceEngine.PerformanceIssue("String concatenation", "Use StringBuilder", "MEDIUM")
        ));
        intelligence.setPatterns(Collections.singletonList("Spring Component"));
        intelligence.setQualityScore(40.0);

        when(codeIntelligenceEngine.analyzeCode(code, "java")).thenReturn(intelligence);

        PairProgrammingAssistant.ProgrammingSuggestions suggestions =
                assistant.provideSuggestions(code, "java", "controller");

        assertThat(suggestions.getIssues()).isNotEmpty();
        assertThat(suggestions.getAlternatives()).isNotEmpty();
        assertThat(suggestions.getNextSteps()).isNotEmpty();
        assertThat(suggestions.getReasoning()).contains("Code Analysis Summary");
        assertThat(suggestions.getConfidence()).isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("provideSuggestions should return empty object for empty code")
    void provideSuggestions_emptyCode() {
        PairProgrammingAssistant.ProgrammingSuggestions suggestions =
                assistant.provideSuggestions("", "java", "");

        assertThat(suggestions.getIssues()).isEmpty();
        assertThat(suggestions.getAlternatives()).isEmpty();
        assertThat(suggestions.getNextSteps()).isEmpty();
        assertThat(suggestions.getReasoning()).isEmpty();
    }
}

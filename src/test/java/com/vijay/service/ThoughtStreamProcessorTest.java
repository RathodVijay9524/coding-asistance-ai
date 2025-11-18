package com.vijay.service;

import com.vijay.dto.ThoughtStreamCursor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThoughtStreamProcessorTest {

    private ThoughtStreamProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new ThoughtStreamProcessor();
    }

    @Test
    @DisplayName("processQuery should set focus area, strategy and brains for code-focused query")
    void processQuery_codeFocus() {
        String query = "Explain this code and how to implement it";

        ThoughtStreamCursor cursor = processor.processQuery("q1", query);

        assertThat(cursor.getFocusArea()).isEqualTo("code");
        assertThat(cursor.getIgnoreArea()).isEqualTo("none");
        assertThat(cursor.getStrategy()).isNotNull();
        assertThat(cursor.getRelevantBrains()).contains("CodeRetriever");
        assertThat(cursor.getRelevantBrains()).contains("ErrorPrediction");
        assertThat(cursor.getRelevantBrains()).contains("EmotionalContext", "Personality");
        assertThat(cursor.getComplexity()).isBetween(0.0, 1.0);
        assertThat(cursor.getAmbiguity()).isBetween(0.0, 1.0);
        assertThat(cursor.getConfidence()).isBetween(0.5, 1.0);
    }

    @Test
    @DisplayName("processQuery should mark debugging focus and use debugging brains")
    void processQuery_debuggingFocus() {
        String query = "How to debug this error in my service?";

        ThoughtStreamCursor cursor = processor.processQuery("q2", query);

        assertThat(cursor.getFocusArea()).isEqualTo("debugging");
        assertThat(cursor.getRelevantBrains()).contains("ErrorPrediction", "CodeRetriever", "KnowledgeGraph");
        assertThat(cursor.isNeedsSlowReasoning()).isFalse();
    }

    @Test
    @DisplayName("processQuery should set ignore area when user says not to focus on something")
    void processQuery_ignoreArea() {
        String query = "Explain the design but do not focus on performance";

        ThoughtStreamCursor cursor = processor.processQuery("q3", query);

        assertThat(cursor.getFocusArea()).isEqualTo("explanation");
        assertThat(cursor.getIgnoreArea()).isEqualTo("performance");
    }

    @Test
    @DisplayName("getRoutingSummary should include cursor details")
    void getRoutingSummary_includesDetails() {
        ThoughtStreamCursor cursor = processor.processQuery("q4", "Explain this architecture and optimize it");

        String summary = processor.getRoutingSummary(cursor);

        assertThat(summary).contains("THOUGHT STREAM ROUTING", "Focus:");
        assertThat(summary).contains("Strategy:");
        assertThat(summary).contains("Relevant Brains:");
        assertThat(summary).contains("Confidence:");
    }
}

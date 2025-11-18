package com.vijay.service;

import com.vijay.dto.ThoughtStreamCursor;
import com.vijay.dto.VisualAttentionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VisualAttentionEngineTest {

    private VisualAttentionEngine engine;

    @BeforeEach
    void setUp() {
        engine = new VisualAttentionEngine();
    }

    private ThoughtStreamCursor newCursor(String focusArea, double complexity, double ambiguity) {
        ThoughtStreamCursor cursor = new ThoughtStreamCursor("q1");
        cursor.setFocusArea(focusArea);
        cursor.setComplexity(complexity);
        cursor.setAmbiguity(ambiguity);
        cursor.setStrategy(ThoughtStreamCursor.ReasoningStrategy.BALANCED);
        return cursor;
    }

    @Test
    @DisplayName("calculateAttention should derive primary focus, context window and scores from query and cursor")
    void calculateAttention_basic() {
        String query = "How to fix this error in my Spring backend API?";
        ThoughtStreamCursor cursor = newCursor("DEBUG", 0.7, 0.4);

        VisualAttentionState state = engine.calculateAttention(query, cursor);

        assertThat(state.getPrimaryFocus()).isEqualTo("Error Handling");
        assertThat(state.getPrimaryFocusScore()).isBetween(50.0, 100.0);
        assertThat(state.getContextWindow()).isNotEmpty();
        assertThat(state.getContextRelevanceScore()).isBetween(30.0, 100.0);
        assertThat(state.getFocusDepth()).isBetween(1, 10);
        assertThat(state.getFocusType()).isIn("PROCESS", "SOLUTION", "ISSUE", "GENERAL");
    }

    @Test
    @DisplayName("calculateAttention should return default attention when input invalid")
    void calculateAttention_invalidInput() {
        VisualAttentionState state = engine.calculateAttention("", null);

        assertThat(state.getPrimaryFocus()).isEqualTo("General Query");
        assertThat(state.getPrimaryFocusScore()).isEqualTo(50.0);
        assertThat(state.getContextRelevanceScore()).isEqualTo(30.0);
        assertThat(state.getFocusDepth()).isEqualTo(5);
        assertThat(state.getFocusType()).isEqualTo("GENERAL");
    }

    @Test
    @DisplayName("scoreRelevance should increase when element matches query and focus area")
    void scoreRelevance_basic() {
        double score1 = engine.scoreRelevance("Spring", "Explain Spring Boot architecture", "architecture");
        double score2 = engine.scoreRelevance("Random", "Explain Spring Boot architecture", "architecture");

        assertThat(score1).isGreaterThan(score2);
    }
}

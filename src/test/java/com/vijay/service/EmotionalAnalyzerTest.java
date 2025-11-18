package com.vijay.service;

import com.vijay.dto.EmotionalContext;
import com.vijay.dto.EmotionalState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmotionalAnalyzerTest {

    private EmotionalAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new EmotionalAnalyzer();
    }

    @Test
    @DisplayName("analyzeEmotion should detect positive and excited states")
    void analyzeEmotion_positiveAndExcited() {
        EmotionalContext positive = analyzer.analyzeEmotion("This is great, I love it! Thanks a lot.");
        assertThat(positive.getCurrentState()).isIn(EmotionalState.POSITIVE, EmotionalState.EXCITED);
        assertThat(positive.getEmotionalIntensity()).isGreaterThan(0);
        assertThat(positive.getRecommendedTone()).isEqualTo("enthusiastic");
        assertThat(positive.getConfidence()).isBetween(0.5, 1.0);

        EmotionalContext excited = analyzer.analyzeEmotion("This is AMAZING!! Fantastic work!!!");
        assertThat(excited.getCurrentState()).isIn(EmotionalState.POSITIVE, EmotionalState.EXCITED);
        assertThat(excited.getEmotionalIntensity()).isGreaterThan(0);
    }

    @Test
    @DisplayName("analyzeEmotion should detect negative and frustrated/urgent states")
    void analyzeEmotion_negativeAndUrgent() {
        EmotionalContext negative = analyzer.analyzeEmotion("This is terrible and I hate these error.");
        assertThat(negative.getCurrentState()).isNotEqualTo(EmotionalState.POSITIVE);
        assertThat(negative.getTriggerKeywords()).contains("terrible", "hate", "error");

        EmotionalContext urgent = analyzer.analyzeEmotion("URGENT: I'm stuck and need help ASAP!!!");
        assertThat(urgent.getCurrentState()).isIn(EmotionalState.URGENT, EmotionalState.FRUSTRATED);
        assertThat(urgent.getRecommendedTone()).isIn("empathetic_and_urgent", "clear_and_detailed", "supportive");
        assertThat(urgent.getEmotionalIntensity()).isGreaterThan(0);
    }

    @Test
    @DisplayName("analyzeEmotion should return neutral context for empty message")
    void analyzeEmotion_empty() {
        EmotionalContext ctx = analyzer.analyzeEmotion("");
        assertThat(ctx.getCurrentState()).isEqualTo(EmotionalState.NEUTRAL);
        assertThat(ctx.getEmotionalIntensity()).isEqualTo(0);
    }
}

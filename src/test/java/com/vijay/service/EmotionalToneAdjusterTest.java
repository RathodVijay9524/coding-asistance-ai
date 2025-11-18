package com.vijay.service;

import com.vijay.dto.EmotionalContext;
import com.vijay.dto.EmotionalState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmotionalToneAdjusterTest {

    private EmotionalToneAdjuster adjuster;

    @BeforeEach
    void setUp() {
        adjuster = new EmotionalToneAdjuster();
    }

    private EmotionalContext ctx(EmotionalState state) {
        EmotionalContext c = new EmotionalContext();
        c.setCurrentState(state);
        return c;
    }

    @Test
    @DisplayName("adjustTone should add empathy and encouragement for frustrated users")
    void adjustTone_frustrated() {
        String base = "We might be able to fix this by checking the logs.";
        EmotionalContext context = ctx(EmotionalState.FRUSTRATED);

        String adjusted = adjuster.adjustTone(base, context);

        assertThat(adjusted).contains("I understand this is frustrating");
        assertThat(adjusted).doesNotContain("might be able to"); // made more direct
        assertThat(adjusted).contains("You've got this");

        int recommendedLength = adjuster.getRecommendedResponseLength(context);
        assertThat(recommendedLength).isEqualTo(200);
        assertThat(adjuster.shouldIncludeStepByStep(context)).isTrue();
    }

    @Test
    @DisplayName("adjustTone should add clarity and reassurance for confused users")
    void adjustTone_confused() {
        String base = "It's not working because you don't handle nulls.";
        EmotionalContext context = ctx(EmotionalState.CONFUSED);

        String adjusted = adjuster.adjustTone(base, context);

        assertThat(adjusted).contains("I can see why this might be confusing");
        assertThat(adjusted).contains("This is a common question");
        assertThat(adjuster.shouldIncludeExamples(context)).isTrue();
        assertThat(adjuster.shouldSimplify(context)).isTrue();
    }

    @Test
    @DisplayName("getEmotionEmoji should delegate to context emoji")
    void getEmotionEmoji_delegates() {
        EmotionalContext context = new EmotionalContext();
        context.setCurrentState(EmotionalState.POSITIVE);

        String emoji = adjuster.getEmotionEmoji(context);
        assertThat(emoji).isNotNull();
    }
}

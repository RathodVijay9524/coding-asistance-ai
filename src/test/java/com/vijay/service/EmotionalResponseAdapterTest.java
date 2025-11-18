package com.vijay.service;

import com.vijay.dto.EmotionalContext;
import com.vijay.dto.EmotionalState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class EmotionalResponseAdapterTest {

    private EmotionalToneAdjuster toneAdjuster;
    private EmotionalResponseAdapter adapter;

    @BeforeEach
    void setUp() {
        toneAdjuster = Mockito.mock(EmotionalToneAdjuster.class);
        adapter = new EmotionalResponseAdapter(toneAdjuster);
    }

    private EmotionalContext ctx(EmotionalState state) {
        EmotionalContext c = new EmotionalContext();
        c.setCurrentState(state);
        return c;
    }

    @Test
    @DisplayName("adaptResponse should use tone adjuster, examples, steps and simplification")
    void adaptResponse_fullFlow() {
        EmotionalContext context = ctx(EmotionalState.CONFUSED);
        String base = "- First\n- Second\nUse to implement and utilize subsequently.";

        when(toneAdjuster.adjustTone(base, context)).thenReturn(base);
        when(toneAdjuster.getRecommendedResponseLength(context)).thenReturn(50);
        when(toneAdjuster.shouldIncludeExamples(context)).thenReturn(true);
        when(toneAdjuster.shouldIncludeStepByStep(context)).thenReturn(true);
        when(toneAdjuster.shouldSimplify(context)).thenReturn(true);

        String adapted = adapter.adaptResponse(base, context);

        // Bullet points should be marked as examples
        assertThat(adapted).contains("📝 - First");
        assertThat(adapted).contains("use");
        assertThat(adapted).doesNotContain("utilize");

        String summary = adapter.getEmotionalSummary(context);
        assertThat(summary).contains("Intensity");
    }

    @Test
    @DisplayName("adaptResponse should return input when response is null or empty")
    void adaptResponse_nullOrEmpty() {
        EmotionalContext context = ctx(EmotionalState.NEUTRAL);

        String adaptedNull = adapter.adaptResponse(null, context);
        String adaptedEmpty = adapter.adaptResponse("", context);

        assertThat(adaptedNull).isNull();
        assertThat(adaptedEmpty).isEmpty();
    }
}

package com.vijay.service;

import com.vijay.dto.EmotionalContext;
import com.vijay.dto.EmotionalState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmotionalMemoryStoreTest {

    private EmotionalMemoryStore store;

    @BeforeEach
    void setUp() {
        store = new EmotionalMemoryStore();
    }

    private EmotionalContext ctx(EmotionalState state, int intensity) {
        EmotionalContext c = new EmotionalContext();
        c.setCurrentState(state);
        c.setEmotionalIntensity(intensity);
        return c;
    }

    @Test
    @DisplayName("storeEmotionalContext should keep bounded history and compute trend")
    void storeEmotionalContext_andTrend() {
        String userId = "u1";
        for (int i = 0; i < 60; i++) {
            store.storeEmotionalContext(userId, ctx(EmotionalState.FRUSTRATED, 80));
        }

        assertThat(store.getEmotionalHistory(userId)).hasSizeLessThanOrEqualTo(50);
        String trend = store.getAverageEmotionalTrend(userId);
        assertThat(trend).isEqualToIgnoringCase("FRUSTRATED");

        int avgIntensity = store.getAverageEmotionalIntensity(userId);
        assertThat(avgIntensity).isGreaterThan(0);
        assertThat(store.isUserEmotionallyFatigued(userId)).isTrue();
    }

    @Test
    @DisplayName("empty history should return neutral and zero intensity")
    void emptyHistory_defaults() {
        String userId = "unknown";
        assertThat(store.getAverageEmotionalTrend(userId)).isEqualTo("NEUTRAL");
        assertThat(store.getAverageEmotionalIntensity(userId)).isEqualTo(0);
        assertThat(store.isUserEmotionallyFatigued(userId)).isFalse();
    }

    @Test
    @DisplayName("clearEmotionalHistory should remove stored entries")
    void clearEmotionalHistory_clears() {
        String userId = "u2";
        store.storeEmotionalContext(userId, ctx(EmotionalState.POSITIVE, 50));
        assertThat(store.getEmotionalHistory(userId)).isNotEmpty();

        store.clearEmotionalHistory(userId);
        assertThat(store.getEmotionalHistory(userId)).isEmpty();
    }
}

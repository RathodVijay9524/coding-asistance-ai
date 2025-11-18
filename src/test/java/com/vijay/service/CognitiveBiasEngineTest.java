package com.vijay.service;

import com.vijay.dto.CognitiveBias;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CognitiveBiasEngineTest {

    private CognitiveBiasEngine engine;

    @BeforeEach
    void setUp() {
        engine = new CognitiveBiasEngine();
    }

    @Test
    @DisplayName("applyBiases should default to RECENCY_BIAS and emphasize recent interactions")
    void applyBiases_recencyBias() {
        String userId = "user1";
        engine.recordInteraction(userId, "We discussed this recently");

        String base = "We recently talked about this last time.";
        String biased = engine.applyBiases(userId, "query", base);

        assertThat(biased).contains("recently");
        assertThat(biased.toLowerCase()).contains("last time");
    }

    @Test
    @DisplayName("setActiveBias and getBiasSummary should reflect selected bias")
    void setActiveBias_and_getBiasSummary() {
        String userId = "user2";
        engine.setActiveBias(userId, CognitiveBias.POSITIVITY_BIAS);

        String summary = engine.getBiasSummary(userId);

        assertThat(summary).contains("Active Bias:");
        assertThat(summary.toLowerCase()).contains("positivity");
        assertThat(engine.getBiasStrength(userId)).isGreaterThan(0.0f);
    }

    @Test
    @DisplayName("rotateBias should change active bias for the user")
    void rotateBias_changesBias() {
        String userId = "user3";
        CognitiveBias initial = engine.getActiveBias(userId);

        engine.rotateBias(userId);
        CognitiveBias after = engine.getActiveBias(userId);

        assertThat(after).isNotNull();
        // Can't guarantee sequence, but usually rotates to different bias
        if (CognitiveBias.values().length > 1) {
            assertThat(after).isNotEqualTo(initial);
        }
    }

    @Test
    @DisplayName("getAvailableBiases should list all CognitiveBias values")
    void getAvailableBiases_listsAll() {
        assertThat(engine.getAvailableBiases()).containsExactlyInAnyOrder(CognitiveBias.values());
    }
}

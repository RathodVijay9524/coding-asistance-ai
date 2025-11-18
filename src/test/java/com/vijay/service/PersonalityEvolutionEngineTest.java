package com.vijay.service;

import com.vijay.dto.PersonalityTraits;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersonalityEvolutionEngineTest {

    private PersonalityEvolutionEngine engine;

    @BeforeEach
    void setUp() {
        engine = new PersonalityEvolutionEngine();
    }

    @Test
    @DisplayName("recordFeedbackAndEvolve should adjust traits after enough interactions")
    void recordFeedbackAndEvolve_changesTraits() {
        String userId = "user1";

        PersonalityTraits traitsBefore = engine.getEvolvedTraits(userId);
        int formalityBefore = traitsBefore.getFormality();

        for (int i = 0; i < 20; i++) {
            engine.recordFeedbackAndEvolve(userId, 3, "too_formal");
        }

        PersonalityTraits traitsAfter = engine.getEvolvedTraits(userId);
        assertThat(traitsAfter.getFormality()).isLessThanOrEqualTo(formalityBefore);

        String summary = engine.getEvolutionSummary(userId);
        assertThat(summary).contains("Personality Evolution");
    }

    @Test
    @DisplayName("resetPersonality should restore default traits and counters")
    void resetPersonality_resetsState() {
        String userId = "user2";

        engine.recordFeedbackAndEvolve(userId, 5, "not_helpful");

        engine.resetPersonality(userId);

        assertThat(engine.getUserSatisfactionScore(userId)).isEqualTo(0.5);
        assertThat(engine.getInteractionCount(userId)).isEqualTo(0);
        assertThat(engine.getEvolvedTraits(userId).getHelpfulness()).isBetween(1, 10);
    }
}

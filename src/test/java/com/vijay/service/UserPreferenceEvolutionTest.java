package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UserPreferenceEvolutionTest {

    private UserPreferenceEvolution evolution;

    @BeforeEach
    void setUp() {
        evolution = new UserPreferenceEvolution();
    }

    @Test
    @DisplayName("recordPreferenceFeedback should update scores and interaction counts")
    void recordPreferenceFeedback_updatesScores() {
        String user = "u1";

        evolution.recordPreferenceFeedback(user, "dark_mode", 0.9);
        evolution.recordPreferenceFeedback(user, "dark_mode", 0.8);

        double score = evolution.getPreferenceScore(user, "dark_mode");
        int interactions = evolution.getInteractionCount(user);

        assertThat(score).isGreaterThan(0.5);
        assertThat(interactions).isEqualTo(2);
    }

    @Test
    @DisplayName("getTopPreferences and getMostFrequentPreferences should reflect history")
    void topAndFrequentPreferences() {
        String user = "u2";

        evolution.recordPreferenceFeedback(user, "dark_mode", 0.9);
        evolution.recordPreferenceFeedback(user, "light_mode", 0.3);
        evolution.recordPreferenceFeedback(user, "dark_mode", 0.9);

        List<String> top = evolution.getTopPreferences(user, 1);
        List<String> frequent = evolution.getMostFrequentPreferences(user, 1);

        assertThat(top).contains("dark_mode");
        assertThat(frequent).contains("dark_mode");
    }

    @Test
    @DisplayName("hasPreferenceEvolved and getPreferenceTrend should respond after threshold interactions")
    void evolutionAndTrend() {
        String user = "u3";

        for (int i = 0; i < 5; i++) {
            evolution.recordPreferenceFeedback(user, "ai_tips", 0.9);
        }

        boolean evolved = evolution.hasPreferenceEvolved(user, "ai_tips");
        String trend = evolution.getPreferenceTrend(user, "ai_tips");

        assertThat(evolved).isTrue();
        assertThat(trend).containsAnyOf("Strong", "Moderate", "Weak", "Disliked");
    }

    @Test
    @DisplayName("predictNextPreference and resetPreferences should behave correctly")
    void predictAndReset() {
        String user = "u4";

        evolution.recordPreferenceFeedback(user, "short_suggestions", 0.8);
        evolution.recordPreferenceFeedback(user, "long_explanations", 0.4);

        String predicted = evolution.predictNextPreference(user);
        Map<String, Double> allBefore = evolution.getAllPreferences(user);

        assertThat(predicted).isEqualTo("short_suggestions");
        assertThat(allBefore).isNotEmpty();

        evolution.resetPreferences(user);

        Map<String, Double> allAfter = evolution.getAllPreferences(user);
        assertThat(allAfter).isEmpty();
        assertThat(evolution.getInteractionCount(user)).isZero();
    }
}

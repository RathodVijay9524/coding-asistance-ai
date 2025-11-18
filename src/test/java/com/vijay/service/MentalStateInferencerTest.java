package com.vijay.service;

import com.vijay.dto.UserMentalModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MentalStateInferencerTest {

    private MentalStateInferencer inferencer;

    @BeforeEach
    void setUp() {
        inferencer = new MentalStateInferencer();
    }

    @Test
    @DisplayName("inferFromQuery should set knowledge level, confusion, frustration and expertise")
    void inferFromQuery_setsModelFields() {
        String userId = "u-mental";
        String query = "I am new to Spring and don't understand how to configure security. " +
                "Can you explain best practice architecture?";

        inferencer.inferFromQuery(userId, query);

        UserMentalModel model = inferencer.getMentalModel(userId);

        assertThat(model.getKnowledgeLevel()).isGreaterThanOrEqualTo(2); // beginner or more
        assertThat(model.getConfusionLevel()).isGreaterThan(0);
        assertThat(model.getFrustrationLevel()).isGreaterThanOrEqualTo(0);
        assertThat(model.getExpertiseAreas()).contains("backend", "security", "architecture");
        assertThat(model.getKnowledgeGaps()).containsAnyOf("spring", "api", "database");
        assertThat(model.getLearningStyle()).isIn("code-heavy", "step-by-step", "textual", "visual");
        assertThat(model.getConfidence()).isGreaterThan(0.0);

        String summary = inferencer.getMentalModelSummary(userId);
        assertThat(summary).isNotEmpty();
    }
}

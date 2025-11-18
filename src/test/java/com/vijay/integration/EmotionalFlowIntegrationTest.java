package com.vijay.integration;

import com.vijay.dto.EmotionalContext;
import com.vijay.dto.EmotionalState;
import com.vijay.dto.ResponseScenario;
import com.vijay.dto.UserMentalModel;
import com.vijay.service.EmotionalAnalyzer;
import com.vijay.service.EmotionalToneAdjuster;
import com.vijay.service.MentalSimulator;
import com.vijay.service.MentalStateInferencer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 🌐 INTEGRATION TEST: Emotional & Mental Flow
 *
 * Wires together EmotionalAnalyzer, EmotionalToneAdjuster, MentalStateInferencer,
 * and MentalSimulator to validate an end-to-end flow for an emotional user query.
 */
@SpringBootTest
class EmotionalFlowIntegrationTest {

    @Autowired
    private EmotionalAnalyzer emotionalAnalyzer;

    @Autowired
    private EmotionalToneAdjuster emotionalToneAdjuster;

    @Autowired
    private MentalStateInferencer mentalStateInferencer;

    @Autowired
    private MentalSimulator mentalSimulator;

    @Test
    @DisplayName("End-to-end emotional flow: frustrated user gets empathetic, detailed response")
    void emotionalFlow_frustratedUser() {
        String userId = "emotional-user-1";
        String query = "I'm really frustrated, this Spring security config is not working and I'm stuck!!";

        // 1) Analyze emotion
        EmotionalContext context = emotionalAnalyzer.analyzeEmotion(query);
        assertThat(context.getCurrentState()).isIn(EmotionalState.FRUSTRATED, EmotionalState.NEGATIVE, EmotionalState.URGENT);
        assertThat(context.getEmotionalIntensity()).isGreaterThan(0);

        // 2) Infer mental state
        mentalStateInferencer.inferFromQuery(userId, query);
        UserMentalModel model = mentalStateInferencer.getMentalModel(userId);
        assertThat(model.getConfusionLevel()).isGreaterThanOrEqualTo(0);
        assertThat(model.getFrustrationLevel()).isGreaterThanOrEqualTo(0);

        // 3) Base response (e.g. from some service)
        String baseResponse = "We might be able to fix this by checking your Spring Security configuration and logs.";

        // 4) Adjust tone based on emotional context
        String adjusted = emotionalToneAdjuster.adjustTone(baseResponse, context);
        assertThat(adjusted).contains("frustrating");
        assertThat(adjusted).contains("You've got this");

        // 5) Simulate scenarios and pick best response style
        List<ResponseScenario> scenarios = mentalSimulator.simulateScenarios(query, adjusted);
        assertThat(scenarios).isNotEmpty();

        ResponseScenario best = mentalSimulator.evaluateAndSelectBest(scenarios);
        assertThat(best).isNotNull();
        assertThat(best.getOverallScore()).isGreaterThan(0.0);

        String reaction = mentalSimulator.predictUserReaction(best);
        assertThat(reaction).contains("User");
    }

    @Test
    @DisplayName("End-to-end emotional flow: confused user gets clear, detailed response with examples and steps")
    void emotionalFlow_confusedUser() {
        String userId = "emotional-user-2";
        String query = "I'm confused about how this caching works. Can you explain step by step with examples?";

        // 1) Analyze emotion
        EmotionalContext context = emotionalAnalyzer.analyzeEmotion(query);
        assertThat(context.getCurrentState()).isIn(EmotionalState.CONFUSED, EmotionalState.NEUTRAL, EmotionalState.POSITIVE);

        // 2) Infer mental state
        mentalStateInferencer.inferFromQuery(userId, query);
        UserMentalModel model = mentalStateInferencer.getMentalModel(userId);
        assertThat(model.getConfusionLevel()).isGreaterThan(0);

        // 3) Base response
        String baseResponse = "Caching improves performance by storing frequently used data in memory.";

        // 4) Adjust tone based on emotional context
        String adjusted = emotionalToneAdjuster.adjustTone(baseResponse, context);
        assertThat(adjusted).contains("confusing");
        assertThat(adjusted).contains("This is a common question");

        // For confused users, tone adjuster suggests examples and step-by-step explanations
        assertThat(emotionalToneAdjuster.shouldIncludeExamples(context)).isTrue();
        assertThat(emotionalToneAdjuster.shouldIncludeStepByStep(context)).isTrue();

        // 5) Simulate scenarios and pick best response style
        List<ResponseScenario> scenarios = mentalSimulator.simulateScenarios(query, adjusted);
        assertThat(scenarios).isNotEmpty();

        ResponseScenario best = mentalSimulator.evaluateAndSelectBest(scenarios);
        assertThat(best.getOverallScore()).isGreaterThan(0.0);
    }
}

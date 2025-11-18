package com.vijay.service;

import com.vijay.dto.ResponseScenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MentalSimulatorTest {

    private MentalSimulator simulator;

    @BeforeEach
    void setUp() {
        simulator = new MentalSimulator();
    }

    @Test
    @DisplayName("simulateScenarios should create multiple response scenarios with diverse tones")
    void simulateScenarios_createsScenarios() {
        String query = "Explain caching strategies";
        String baseResponse = "Caching helps improve performance by storing frequently used data.";

        List<ResponseScenario> scenarios = simulator.simulateScenarios(query, baseResponse);

        assertThat(scenarios).hasSizeGreaterThanOrEqualTo(5);
        assertThat(scenarios)
                .extracting(ResponseScenario::getTone)
                .contains("concise", "detailed", "example-heavy", "explanation-focused", "balanced");
    }

    @Test
    @DisplayName("evaluateAndSelectBest should pick scenario with highest score and predict reaction")
    void evaluateAndSelectBest_andPredictReaction() {
        String baseResponse = "Response";
        List<ResponseScenario> scenarios = simulator.simulateScenarios("q", baseResponse);

        ResponseScenario best = simulator.evaluateAndSelectBest(scenarios);
        String reaction = simulator.predictUserReaction(best);

        assertThat(best.getOverallScore()).isGreaterThan(0.0);
        assertThat(reaction).contains("User");

        String comparison = simulator.compareScenarios(scenarios);
        assertThat(comparison).contains("Scenario Comparison");
    }
}

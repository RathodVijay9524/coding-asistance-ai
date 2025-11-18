package com.vijay.service;

import com.vijay.dto.LearningMetric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EnhancedLearningSystemTest {

    private EnhancedLearningSystem system;

    @BeforeEach
    void setUp() {
        system = new EnhancedLearningSystem();
    }

    @Test
    @DisplayName("recordStrategyOutcome should update metrics and best strategy")
    void recordStrategyOutcome_updatesMetrics() {
        String type = "search";

        system.recordStrategyOutcome(type, "similarity", true, 0.9, 0.8, 100);
        system.recordStrategyOutcome(type, "similarity", true, 0.8, 0.9, 110);
        system.recordStrategyOutcome(type, "hybrid", false, 0.5, 0.5, 200);

        String best = system.getBestStrategy(type);
        List<String> ranked = system.getRankedStrategies(type);
        LearningMetric metric = system.getMetric(type, "similarity");

        assertThat(best).isEqualTo("similarity");
        assertThat(ranked).isNotEmpty();
        assertThat(ranked.get(0)).isEqualTo("similarity");
        assertThat(metric.getSuccessRate()).isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("getStrategyTrend should reflect effectiveness buckets")
    void getStrategyTrend_ranges() {
        String type = "qa";

        // Effectiveness depends on success rate, quality, satisfaction, response time; we just ensure a non-empty message
        system.recordStrategyOutcome(type, "balanced", true, 0.9, 0.9, 50);
        String trend = system.getStrategyTrend(type, "balanced");

        assertThat(trend).containsAnyOf("Excellent", "Good", "Fair", "Poor");
    }

    @Test
    @DisplayName("getLearningsSummary and getOverallStatistics should include counts and effectiveness")
    void summaries_includeMetrics() {
        String type = "cmd";
        system.recordStrategyOutcome(type, "balanced", true, 0.8, 0.8, 80);
        system.recordStrategyOutcome(type, "fast", false, 0.4, 0.4, 30);

        String summary = system.getLearningsSummary(type);
        String overall = system.getOverallStatistics();

        assertThat(summary).contains("Learnings for");
        assertThat(overall).contains("Overall Learning Statistics");

        List<LearningMetric> metricsForType = system.getMetricsForQueryType(type);
        assertThat(metricsForType).isNotEmpty();
    }

    @Test
    @DisplayName("resetLearnings should clear metrics for query type")
    void resetLearnings_clearsMetrics() {
        String type = "clear-test";
        system.recordStrategyOutcome(type, "balanced", true, 0.8, 0.8, 80);

        assertThat(system.getMetricsForQueryType(type)).isNotEmpty();

        system.resetLearnings(type);

        assertThat(system.getMetricsForQueryType(type)).isEmpty();
    }
}

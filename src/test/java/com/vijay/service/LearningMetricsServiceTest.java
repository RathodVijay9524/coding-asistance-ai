package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LearningMetricsServiceTest {

    private LearningMetricsService service;

    @BeforeEach
    void setUp() {
        service = new LearningMetricsService();
    }

    @Test
    @DisplayName("recordQuerySuccess and recordQueryFailure should update metrics and best strategy")
    void recordQuery_updatesMetrics() {
        String type = "search";

        // 3 successes, 1 failure for two strategies
        service.recordQuerySuccess(type, "similarity", 4.5, 100);
        service.recordQuerySuccess(type, "similarity", 4.0, 120);
        service.recordQueryFailure(type, "similarity", "timeout");

        service.recordQuerySuccess(type, "hybrid", 5.0, 90);

        double avgQuality = service.getAverageQuality(type);
        double rateSimilarity = service.getStrategySuccessRate(type, "similarity");
        double rateHybrid = service.getStrategySuccessRate(type, "hybrid");
        String best = service.getBestStrategy(type);

        assertThat(avgQuality).isGreaterThan(0.0);
        assertThat(rateSimilarity).isBetween(0.0, 1.0);
        assertThat(rateHybrid).isBetween(0.0, 1.0);
        assertThat(best).isIn("similarity", "hybrid");
    }

    @Test
    @DisplayName("detectPatterns and getInsights should produce patterns and non-zero totals")
    void detectPatterns_andInsights() {
        String type = "analytics";

        // Create >=5 total queries so patterns are detected
        for (int i = 0; i < 4; i++) {
            service.recordQuerySuccess(type, "similarity", 4.0, 100);
        }
        service.recordQueryFailure(type, "similarity", "error");

        List<LearningMetricsService.QueryPattern> patterns = service.detectPatterns();
        LearningMetricsService.LearningInsights insights = service.getInsights();
        String summary = service.getMetricsSummary();

        assertThat(patterns).isNotEmpty();
        assertThat(insights.totalQueries).isGreaterThanOrEqualTo(5);
        assertThat(insights.detectedPatterns).isNotEmpty();
        assertThat(summary).contains("Learning Metrics");
    }

    @Test
    @DisplayName("recordUserFeedback should adjust strategy user rating")
    void recordUserFeedback_updatesRating() {
        String type = "chat";

        service.recordQuerySuccess(type, "similarity", 4.0, 100);
        service.recordUserFeedback(type, "similarity", 5);

        double rate = service.getStrategySuccessRate(type, "similarity");
        assertThat(rate).isGreaterThan(0.0);
    }
}

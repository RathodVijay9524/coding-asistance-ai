package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SuggestionRankerTest {

    private SuggestionRanker ranker;

    @BeforeEach
    void setUp() {
        ranker = new SuggestionRanker();
    }

    private PairProgrammingAssistant.Suggestion bugCritical(String title) {
        return new PairProgrammingAssistant.Suggestion(title, "desc", "BUG", "CRITICAL");
    }

    private PairProgrammingAssistant.Suggestion refactorMedium(String title) {
        return new PairProgrammingAssistant.Suggestion(title, "desc", "REFACTOR", "MEDIUM");
    }

    private PairProgrammingAssistant.Suggestion perfHigh(String title) {
        return new PairProgrammingAssistant.Suggestion(title, "desc", "PERFORMANCE", "HIGH");
    }

    @Test
    @DisplayName("rankSuggestions should order by type+severity score")
    void rankSuggestions_ordersByScore() {
        List<PairProgrammingAssistant.Suggestion> list = Arrays.asList(
                refactorMedium("refactor"),
                perfHigh("perf"),
                bugCritical("bug")
        );

        List<PairProgrammingAssistant.Suggestion> ranked = ranker.rankSuggestions(list);

        assertThat(ranked.get(0).type).isEqualTo("BUG");
        assertThat(ranked.get(0).severity).isEqualTo("CRITICAL");
    }

    @Test
    @DisplayName("filterLowConfidence should keep only suggestions above threshold")
    void filterLowConfidence_filters() {
        List<PairProgrammingAssistant.Suggestion> list = Arrays.asList(
                bugCritical("bug"),
                refactorMedium("refactor")
        );

        List<PairProgrammingAssistant.Suggestion> filtered = ranker.filterLowConfidence(list, 0.9);

        // BUG+CRITICAL has highest confidence, refactor lower
        assertThat(filtered)
                .extracting(s -> s.type)
                .contains("BUG")
                .doesNotContain("REFACTOR");
    }

    @Test
    @DisplayName("getTopSuggestions should limit to top N ranked suggestions")
    void getTopSuggestions_limitsToN() {
        List<PairProgrammingAssistant.Suggestion> list = Arrays.asList(
                bugCritical("bug1"),
                perfHigh("perf1"),
                refactorMedium("refactor1")
        );

        List<PairProgrammingAssistant.Suggestion> top2 = ranker.getTopSuggestions(list, 2);

        assertThat(top2).hasSize(2);
    }

    @Test
    @DisplayName("groupByType and groupBySeverity should aggregate counts correctly")
    void groupAndStats() {
        List<PairProgrammingAssistant.Suggestion> list = Arrays.asList(
                bugCritical("bug1"),
                bugCritical("bug2"),
                perfHigh("perf1"),
                refactorMedium("refactor1")
        );

        Map<String, List<PairProgrammingAssistant.Suggestion>> byType = ranker.groupByType(list);
        Map<String, List<PairProgrammingAssistant.Suggestion>> bySeverity = ranker.groupBySeverity(list);
        SuggestionRanker.SuggestionStats stats = ranker.getStats(list);

        assertThat(byType.get("BUG")).hasSize(2);
        assertThat(byType.get("PERFORMANCE")).hasSize(1);
        // Only BUG suggestions are CRITICAL; PERFORMANCE here is HIGH
        assertThat(bySeverity.get("CRITICAL")).hasSize(2);

        assertThat(stats.totalSuggestions).isEqualTo(4);
        assertThat(stats.bugCount).isEqualTo(2);
        assertThat(stats.performanceCount).isEqualTo(1);
        assertThat(stats.refactoringCount).isEqualTo(1);
        assertThat(stats.criticalCount).isEqualTo(2);
        assertThat(stats.mediumCount).isEqualTo(1);
    }
}

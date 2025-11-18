package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueryPlannerTest {

    private QueryPlanner planner;

    @BeforeEach
    void setUp() {
        planner = new QueryPlanner();
    }

    @Test
    @DisplayName("createSearchPlan should classify debugging query and choose error_trace strategy")
    void createSearchPlan_debugQuery() {
        String query = "I get this error when running tests, how to debug this issue?";

        QueryPlanner.SearchPlan plan = planner.createSearchPlan(query);

        assertThat(plan.intent).isEqualTo("DEBUG");
        assertThat(plan.searchStrategy).isIn("error_trace", "similarity_search");
        assertThat(plan.topK).isGreaterThanOrEqualTo(4);
        assertThat(plan.maxHops).isGreaterThanOrEqualTo(2);
        assertThat(plan.includeReverseDeps).isTrue();
        assertThat(plan.tokenBudget).isBetween(5000, 7000);
        assertThat(plan.isHighConfidence()).isTrue();
    }

    @Test
    @DisplayName("createSearchPlan should detect architecture intent and use dependency_graph strategy")
    void createSearchPlan_architectureQuery() {
        String query = "Explain the architecture and structure of this Spring Boot service layer";

        QueryPlanner.SearchPlan plan = planner.createSearchPlan(query);

        assertThat(plan.intent).isEqualTo("ARCHITECTURE");
        assertThat(plan.searchStrategy).isIn("dependency_graph", "similarity_search");
        assertThat(plan.topK).isGreaterThanOrEqualTo(4);
        assertThat(plan.maxHops).isGreaterThanOrEqualTo(2);
        assertThat(plan.includeReverseDeps).isTrue();
        assertThat(plan.tokenBudget).isLessThanOrEqualTo(6500);
    }

    @Test
    @DisplayName("createSearchPlan should infer entity-centered strategy and starting files from class names")
    void createSearchPlan_entityCentered() {
        String query = "How does ChatService interact with AIProviderConfig?";

        QueryPlanner.SearchPlan plan = planner.createSearchPlan(query);

        assertThat(plan.searchStrategy).isIn("entity_centered", "configuration_chain");
        assertThat(plan.hasSpecificEntities()).isTrue();
        assertThat(plan.startingFiles).contains("ChatService.java");
        assertThat(plan.startingFiles).anyMatch(f -> f.endsWith("AIProviderConfig.java"));
    }
}

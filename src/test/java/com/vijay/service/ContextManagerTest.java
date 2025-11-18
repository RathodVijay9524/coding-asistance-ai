package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ContextManagerTest {

    private ContextManager contextManager;

    @BeforeEach
    void setUp() {
        contextManager = new ContextManager();
    }

    @Test
    @DisplayName("createBudget should initialize tokens based on query size")
    void createBudget_basic() {
        String query = "simple query";
        ContextManager.ContextBudget budget = contextManager.createBudget(query);

        assertThat(budget.maxTokens).isGreaterThan(0);
        assertThat(budget.usedTokens).isGreaterThan(0);
        assertThat(budget.remainingTokens).isEqualTo(budget.maxTokens - budget.usedTokens);
        assertThat(budget.isNearLimit()).isFalse();
        assertThat(budget.isOverLimit()).isFalse();
    }

    @Test
    @DisplayName("pruneContent should return all when under budget and subset when over budget")
    void pruneContent_underAndOverBudget() {
        String query = "service config";
        ContextManager.ContextBudget budget = contextManager.createBudget(query);

        List<String> content = Arrays.asList(
                "short text",
                "another short text",
                "some content about service and config" // more relevant
        );

        // Under budget: artificially lower usedTokens to almost zero
        budget.usedTokens = 0;
        budget.remainingTokens = budget.maxTokens;
        List<String> all = contextManager.pruneContent(content, budget, query);
        assertThat(all).hasSize(3);

        // Over budget: simulate nearly full budget
        budget.usedTokens = budget.maxTokens - 10;
        budget.remainingTokens = 10;
        List<String> pruned = contextManager.pruneContent(content, budget, query);

        assertThat(pruned.size()).isLessThanOrEqualTo(3);
        assertThat(pruned).contains("some content about service and config");
    }

    @Test
    @DisplayName("canAddContent and addContent should respect budget limits")
    void canAddContent_andAddContent() {
        ContextManager.ContextBudget budget = contextManager.createBudget("q");

        String small = "small";
        boolean canAddSmall = contextManager.canAddContent(small, budget);
        assertThat(canAddSmall).isTrue();
        contextManager.addContent(small, budget);

        // Force budget close to max
        budget.usedTokens = budget.maxTokens - 1;
        budget.remainingTokens = 1;

        String large = "this is a very large content block that likely exceeds remaining tokens";
        boolean canAddLarge = contextManager.canAddContent(large, budget);
        assertThat(canAddLarge).isFalse();
    }

    @Test
    @DisplayName("prioritizeFiles should filter low relevance files")
    void prioritizeFiles_filters() {
        ContextManager.ContextBudget budget = contextManager.createBudget("service config");

        List<String> files = Arrays.asList(
                "ChatService.java",
                "AppConfig.java",
                "README.md",
                "unrelated.txt",
                "UserController.java"
        );

        List<String> prioritized = contextManager.prioritizeFiles(files, "service config", budget);

        assertThat(prioritized).contains("ChatService.java", "AppConfig.java");
    }
}

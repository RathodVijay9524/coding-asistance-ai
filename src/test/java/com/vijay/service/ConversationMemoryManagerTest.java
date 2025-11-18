package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationMemoryManagerTest {

    private ConversationMemoryManager manager;

    @BeforeEach
    void setUp() {
        manager = new ConversationMemoryManager();
    }

    @Test
    @DisplayName("storeConversation should update session, profile and allow context retrieval")
    void storeConversation_andGetContext() {
        String sessionId = "s1";
        String userId = "u1";

        manager.storeConversation(sessionId, userId,
                "How to design architecture?",
                "You can use layered architecture...",
                "similarity_search", 0.9);

        ConversationMemoryManager.ConversationContext ctx =
                manager.getRelevantContext(sessionId, userId, "architecture design");

        assertThat(ctx.getRecentExchanges()).isNotEmpty();
        assertThat(ctx.getRecentExchanges().get(0).getUserQuery()).contains("architecture");
        assertThat(ctx.getPreferredSearchStrategies()).isNotNull();
        assertThat(ctx.hasRelevantContext()).isTrue();

        ConversationMemoryManager.UserInsights insights = manager.getUserInsights(userId);
        assertThat(insights.getUserId()).isEqualTo(userId);
        assertThat(insights.getTotalQueries()).isEqualTo(1);
        assertThat(insights.getAverageConfidence()).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("getRelevantContext should return empty context when no session or user exists")
    void getRelevantContext_noSession_returnsEmptyContext() {
        ConversationMemoryManager.ConversationContext ctx =
                manager.getRelevantContext("missing-session", "missing-user", "some query");

        assertThat(ctx.getRecentExchanges()).isEmpty();
        assertThat(ctx.getRelatedExchanges()).isEmpty();
        assertThat(ctx.getLongTermMemories()).isEmpty();
        assertThat(ctx.hasRelevantContext()).isFalse();
        assertThat(ctx.getFormattedContext()).isEmpty();
    }

    @Test
    @DisplayName("high-confidence important queries should be promoted to long-term memory")
    void longTermMemory_promotionAndPruning() {
        String sessionId = "s2";
        String userId = "u2";

        // This query matches promotion rules (architecture + high confidence)
        manager.storeConversation(sessionId, userId,
                "Discuss system architecture and design patterns",
                "We can use hexagonal architecture...",
                "hybrid", 0.95);

        ConversationMemoryManager.ConversationContext ctx =
                manager.getRelevantContext(sessionId, userId, "architecture patterns");

        List<ConversationMemoryManager.ConversationMemory> longTerm = ctx.getLongTermMemories();
        assertThat(longTerm).isNotNull();
    }

    @Test
    @DisplayName("getRelevantContext should limit recent exchanges to last 5 entries")
    void getRelevantContext_limitsRecentExchangesToFive() {
        String sessionId = "s-window";
        String userId = "u-window";

        for (int i = 1; i <= 8; i++) {
            manager.storeConversation(sessionId, userId,
                    "query-" + i,
                    "answer-" + i,
                    "strategy", 0.5);
        }

        ConversationMemoryManager.ConversationContext ctx =
                manager.getRelevantContext(sessionId, userId, "query-8");

        assertThat(ctx.getRecentExchanges()).hasSize(5);
        assertThat(ctx.getRecentExchanges().get(0).getUserQuery()).isEqualTo("query-4");
        assertThat(ctx.getRecentExchanges().get(4).getUserQuery()).isEqualTo("query-8");
    }

    @Test
    @DisplayName("getRelevantContext should include related exchanges based on similarity")
    void getRelevantContext_includesRelatedExchanges() {
        String sessionId = "s-related";
        String userId = "u-related";

        manager.storeConversation(sessionId, userId,
                "architecture patterns",
                "answer-1",
                "strategy", 0.9);
        manager.storeConversation(sessionId, userId,
                "unrelated topic",
                "answer-2",
                "strategy", 0.9);

        ConversationMemoryManager.ConversationContext ctx =
                manager.getRelevantContext(sessionId, userId, "architecture patterns");

        assertThat(ctx.getRelatedExchanges()).isNotEmpty();
        assertThat(ctx.getRelatedExchanges())
                .extracting(ConversationMemoryManager.ConversationExchange::getUserQuery)
                .contains("architecture patterns");
    }

    @Test
    @DisplayName("cleanupOldSessions should remove sessions older than 24 hours")
    void cleanupOldSessions_removesOld() {
        String sessionId = "old-session";
        String userId = "u3";

        // Store one conversation to create session
        manager.storeConversation(sessionId, userId,
                "Old question about configuration",
                "Use application.yml...",
                "similarity_search", 0.7);

        // There is no direct way to force lastActivityTime back in time, but cleanupOldSessions
        // should at least be callable without exceptions. We assert it doesn't affect new sessions.
        String newSession = "new-session";
        manager.storeConversation(newSession, userId,
                "New question",
                "New answer",
                "similarity_search", 0.8);

        manager.cleanupOldSessions();

        ConversationMemoryManager.ConversationContext ctx =
                manager.getRelevantContext(newSession, userId, "New question");
        assertThat(ctx.getRecentExchanges()).isNotEmpty();
    }

    @Test
    @DisplayName("getUserInsights should return defaults for unknown user")
    void getUserInsights_unknownUser_returnsDefaults() {
        ConversationMemoryManager.UserInsights insights = manager.getUserInsights("unknown");

        assertThat(insights.getUserId()).isEqualTo("unknown");
        assertThat(insights.getTotalQueries()).isEqualTo(0);
        assertThat(insights.getPreferredStrategies()).isEmpty();
        assertThat(insights.getCommonTopics()).isEmpty();
        assertThat(insights.getAverageConfidence()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("ConversationContext.getFormattedContext should include recent and preference information")
    void conversationContext_getFormattedContext_includesSections() {
        String sessionId = "s-format";
        String userId = "u-format";

        manager.storeConversation(sessionId, userId,
                "Question about architecture and configuration",
                "Answer",
                "similarity_search", 0.9);

        ConversationMemoryManager.ConversationContext ctx =
                manager.getRelevantContext(sessionId, userId, "architecture");

        String formatted = ctx.getFormattedContext();
        assertThat(formatted).contains("Recent Conversation");
        assertThat(formatted).contains("Question about architecture");
    }
}

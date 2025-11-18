package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenCountingServiceTest {

    private TokenCountingService service;

    @BeforeEach
    void setUp() {
        service = new TokenCountingService();
    }

    @Test
    @DisplayName("countTokens should return 0 for null or empty text")
    void countTokens_nullOrEmpty() {
        assertThat(service.countTokens(null)).isZero();
        assertThat(service.countTokens("")).isZero();
    }

    @Test
    @DisplayName("countTokens should count words separated by whitespace")
    void countTokens_basic() {
        assertThat(service.countTokens("one")).isEqualTo(1);
        assertThat(service.countTokens("one two three")).isEqualTo(3);
        assertThat(service.countTokens("  many   spaces here ")).isEqualTo(3);
    }

    @Test
    @DisplayName("recordTokenUsage should initialize budget and update used/remaining tokens")
    void recordTokenUsage_initializesAndUpdatesBudget() {
        String userId = "user-token-1";

        TokenCountingService.TokenUsageRecord record = service.recordTokenUsage(
                userId,
                "request text here",      // 3 tokens
                "response text here more" // 4 tokens
        );

        assertThat(record.userId).isEqualTo(userId);
        assertThat(record.requestTokens).isEqualTo(3);
        assertThat(record.responseTokens).isEqualTo(4);
        assertThat(record.totalTokens).isEqualTo(7);
        assertThat(record.usagePercentage).isGreaterThan(0.0);

        // Verify budget state
        int used = service.getUsedTokens(userId);
        int remaining = service.getRemainingTokens(userId);
        assertThat(used).isEqualTo(7);
        assertThat(remaining).isGreaterThan(0);
    }

    @Test
    @DisplayName("hasEnoughTokens should respect remaining budget")
    void hasEnoughTokens_respectsBudget() {
        String userId = "user-token-2";
        service.initializeUserBudget(userId, 10);

        // Use 7 tokens
        service.recordTokenUsage(userId, "one two three", "one two"); // 3 + 2 = 5
        service.recordTokenUsage(userId, "one two", "");              // 2 + 0 = 2 (total 7)

        assertThat(service.hasEnoughTokens(userId, 3)).isTrue();  // remaining 3
        assertThat(service.hasEnoughTokens(userId, 4)).isFalse(); // need more than remaining
    }

    @Test
    @DisplayName("resetMonthlyQuota should reset used and remaining tokens")
    void resetMonthlyQuota_resetsUsage() {
        String userId = "user-token-3";
        service.initializeUserBudget(userId, 20);

        service.recordTokenUsage(userId, "one two three", "four five six"); // 3 + 3 = 6
        assertThat(service.getUsedTokens(userId)).isEqualTo(6);

        service.resetMonthlyQuota(userId);

        assertThat(service.getUsedTokens(userId)).isEqualTo(0);
        assertThat(service.getRemainingTokens(userId)).isEqualTo(20);
    }

    @Test
    @DisplayName("getTokenStatistics should return default stats for unknown user and real stats for known user")
    void getTokenStatistics_behaviour() {
        String unknownUser = "unknown";
        TokenCountingService.TokenStatistics statsUnknown = service.getTokenStatistics(unknownUser);
        assertThat(statsUnknown.usedTokens).isZero();
        assertThat(statsUnknown.remainingTokens).isZero();
        assertThat(statsUnknown.monthlyQuota).isZero();

        String userId = "user-token-4";
        service.initializeUserBudget(userId, 50);
        service.recordTokenUsage(userId, "one two", "three four five"); // 2 + 3 = 5

        TokenCountingService.TokenStatistics stats = service.getTokenStatistics(userId);
        assertThat(stats.userId).isEqualTo(userId);
        assertThat(stats.usedTokens).isEqualTo(5);
        assertThat(stats.remainingTokens).isEqualTo(45);
        assertThat(stats.monthlyQuota).isEqualTo(50);
        assertThat(stats.usagePercentage).isGreaterThan(0.0);
    }
}

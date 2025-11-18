package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 🧠 TOKEN COUNTING SERVICE - Phase 7
 * 
 * Purpose: Counts tokens in requests/responses, tracks token usage per user,
 * alerts on token limits, and manages token budget.
 * 
 * Responsibilities:
 * - Count tokens in text
 * - Track token usage per user
 * - Monitor token budget
 * - Alert on token limit exceeded
 * - Provide token statistics
 * - Manage token quotas
 */
@Service
public class TokenCountingService {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenCountingService.class);
    
    // Token tracking
    private final Map<String, UserTokenBudget> userBudgets = new ConcurrentHashMap<>();
    private final Map<String, TokenUsageHistory> usageHistory = new ConcurrentHashMap<>();
    
    // Configuration
    private static final int TOKENS_PER_WORD = 1; // Simplified: 1 token per word
    private static final int DEFAULT_MONTHLY_QUOTA = 100000; // 100K tokens per month
    private static final int WARNING_THRESHOLD = 80; // Warn at 80% usage
    
    /**
     * Initialize token budget for a user
     */
    public void initializeUserBudget(String userId, int monthlyQuota) {
        UserTokenBudget budget = new UserTokenBudget(userId, monthlyQuota);
        userBudgets.put(userId, budget);
        usageHistory.put(userId, new TokenUsageHistory(userId));
        logger.info("🧠 Token Service: Initialized budget for user {} (quota: {})", userId, monthlyQuota);
    }
    
    /**
     * Initialize user with default quota
     */
    public void initializeUserBudget(String userId) {
        initializeUserBudget(userId, DEFAULT_MONTHLY_QUOTA);
    }
    
    /**
     * Count tokens in text
     */
    public int countTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        // Simple token counting: split by whitespace
        String[] words = text.trim().split("\\s+");
        return words.length * TOKENS_PER_WORD;
    }
    
    /**
     * Record token usage for a user
     */
    public TokenUsageRecord recordTokenUsage(String userId, String requestText, String responseText) {
        int requestTokens = countTokens(requestText);
        int responseTokens = countTokens(responseText);
        int totalTokens = requestTokens + responseTokens;
        
        // Update user budget
        UserTokenBudget budget = userBudgets.get(userId);
        if (budget == null) {
            initializeUserBudget(userId);
            budget = userBudgets.get(userId);
        }
        
        budget.recordUsage(totalTokens);
        
        // Record in history
        TokenUsageHistory history = usageHistory.get(userId);
        if (history != null) {
            history.recordUsage(requestTokens, responseTokens);
        }
        
        // Check if warning needed
        double usagePercentage = budget.getUsagePercentage();
        if (usagePercentage > WARNING_THRESHOLD) {
            logger.warn("⚠️ Token Service: User {} at {:.1f}% of monthly quota", userId, usagePercentage);
        }
        
        logger.debug("🧠 Token Service: Recorded {} tokens for user {} (request: {}, response: {})", 
            totalTokens, userId, requestTokens, responseTokens);
        
        return new TokenUsageRecord(userId, requestTokens, responseTokens, totalTokens, usagePercentage);
    }
    
    /**
     * Check if user has enough tokens
     */
    public boolean hasEnoughTokens(String userId, int requiredTokens) {
        UserTokenBudget budget = userBudgets.get(userId);
        if (budget == null) {
            return true; // No budget set, allow
        }
        
        return budget.getRemainingTokens() >= requiredTokens;
    }
    
    /**
     * Get remaining tokens for user
     */
    public int getRemainingTokens(String userId) {
        UserTokenBudget budget = userBudgets.get(userId);
        if (budget == null) {
            return DEFAULT_MONTHLY_QUOTA;
        }
        
        return budget.getRemainingTokens();
    }
    
    /**
     * Get token usage for user
     */
    public int getUsedTokens(String userId) {
        UserTokenBudget budget = userBudgets.get(userId);
        if (budget == null) {
            return 0;
        }
        
        return budget.getUsedTokens();
    }
    
    /**
     * Get usage percentage for user
     */
    public double getUsagePercentage(String userId) {
        UserTokenBudget budget = userBudgets.get(userId);
        if (budget == null) {
            return 0.0;
        }
        
        return budget.getUsagePercentage();
    }
    
    /**
     * Get token statistics for user
     */
    public TokenStatistics getTokenStatistics(String userId) {
        UserTokenBudget budget = userBudgets.get(userId);
        TokenUsageHistory history = usageHistory.get(userId);
        
        if (budget == null || history == null) {
            return new TokenStatistics(userId, 0, 0, 0, 0.0);
        }
        
        return new TokenStatistics(
            userId,
            budget.getUsedTokens(),
            budget.getRemainingTokens(),
            budget.getMonthlyQuota(),
            budget.getUsagePercentage()
        );
    }
    
    /**
     * Get usage history for user
     */
    public TokenUsageHistory getUsageHistory(String userId) {
        return usageHistory.get(userId);
    }
    
    /**
     * Reset monthly quota for user
     */
    public void resetMonthlyQuota(String userId) {
        UserTokenBudget budget = userBudgets.get(userId);
        if (budget != null) {
            budget.reset();
            logger.info("🧠 Token Service: Reset monthly quota for user {}", userId);
        }
    }
    
    /**
     * Reset all monthly quotas
     */
    public void resetAllMonthlyQuotas() {
        userBudgets.values().forEach(UserTokenBudget::reset);
        logger.info("🧠 Token Service: Reset all monthly quotas");
    }
    
    /**
     * Log token statistics
     */
    public void logTokenStatistics(String userId) {
        TokenStatistics stats = getTokenStatistics(userId);
        logger.info("🧠 Token Service Statistics for user {}:", userId);
        logger.info("   📊 Used: {} tokens", stats.usedTokens);
        logger.info("   ⏳ Remaining: {} tokens", stats.remainingTokens);
        logger.info("   📈 Total Quota: {} tokens", stats.monthlyQuota);
        logger.info("   📉 Usage: {:.1f}%", stats.usagePercentage);
    }
    
    // ============ Inner Classes ============
    
    /**
     * Tracks token budget for a user
     */
    public static class UserTokenBudget {
        private final String userId;
        private final int monthlyQuota;
        private final AtomicLong usedTokens = new AtomicLong(0);
        private final long createdTime;
        
        public UserTokenBudget(String userId, int monthlyQuota) {
            this.userId = userId;
            this.monthlyQuota = monthlyQuota;
            this.createdTime = System.currentTimeMillis();
        }
        
        public void recordUsage(int tokens) {
            usedTokens.addAndGet(tokens);
        }
        
        public int getUsedTokens() {
            return (int) usedTokens.get();
        }
        
        public int getRemainingTokens() {
            return Math.max(0, monthlyQuota - getUsedTokens());
        }
        
        public double getUsagePercentage() {
            return (double) getUsedTokens() / monthlyQuota * 100;
        }
        
        public int getMonthlyQuota() {
            return monthlyQuota;
        }
        
        public void reset() {
            usedTokens.set(0);
        }
    }
    
    /**
     * Tracks token usage history
     */
    public static class TokenUsageHistory {
        private final String userId;
        private final List<TokenUsageEntry> entries = Collections.synchronizedList(new ArrayList<>());
        
        public TokenUsageHistory(String userId) {
            this.userId = userId;
        }
        
        public void recordUsage(int requestTokens, int responseTokens) {
            entries.add(new TokenUsageEntry(requestTokens, responseTokens));
        }
        
        public List<TokenUsageEntry> getEntries() {
            return new ArrayList<>(entries);
        }
        
        public int getTotalRequestTokens() {
            return entries.stream().mapToInt(e -> e.requestTokens).sum();
        }
        
        public int getTotalResponseTokens() {
            return entries.stream().mapToInt(e -> e.responseTokens).sum();
        }
        
        public double getAverageRequestTokens() {
            return entries.isEmpty() ? 0 : (double) getTotalRequestTokens() / entries.size();
        }
        
        public double getAverageResponseTokens() {
            return entries.isEmpty() ? 0 : (double) getTotalResponseTokens() / entries.size();
        }
    }
    
    /**
     * Single token usage entry
     */
    public static class TokenUsageEntry {
        public final int requestTokens;
        public final int responseTokens;
        public final long timestamp;
        
        public TokenUsageEntry(int requestTokens, int responseTokens) {
            this.requestTokens = requestTokens;
            this.responseTokens = responseTokens;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Token usage record
     */
    public static class TokenUsageRecord {
        public final String userId;
        public final int requestTokens;
        public final int responseTokens;
        public final int totalTokens;
        public final double usagePercentage;
        
        public TokenUsageRecord(String userId, int requestTokens, int responseTokens, 
                               int totalTokens, double usagePercentage) {
            this.userId = userId;
            this.requestTokens = requestTokens;
            this.responseTokens = responseTokens;
            this.totalTokens = totalTokens;
            this.usagePercentage = usagePercentage;
        }
    }
    
    /**
     * Token statistics for a user
     */
    public static class TokenStatistics {
        public final String userId;
        public final int usedTokens;
        public final int remainingTokens;
        public final int monthlyQuota;
        public final double usagePercentage;
        
        public TokenStatistics(String userId, int usedTokens, int remainingTokens, 
                              int monthlyQuota, double usagePercentage) {
            this.userId = userId;
            this.usedTokens = usedTokens;
            this.remainingTokens = remainingTokens;
            this.monthlyQuota = monthlyQuota;
            this.usagePercentage = usagePercentage;
        }
    }
}

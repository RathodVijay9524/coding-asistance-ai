package com.vijay.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 💰 Token Budget Manager - Intelligent token usage
 * 
 * Purpose: Manage token quota and prevent overages
 * 
 * Features:
 * ✅ Track token usage
 * ✅ Dynamic output limits (small request → small output)
 * ✅ Hard stop protection (> 1K tokens = warning)
 * ✅ Budget alerts
 * ✅ Usage statistics
 * 
 * Benefits:
 * - Prevents token overages
 * - Reduces costs
 * - Better resource management
 * - Predictable billing
 */
@Service
public class TokenBudgetManager {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenBudgetManager.class);
    
    // Token limits
    private static final long TOTAL_QUOTA = 100_000L;
    private static final int SMALL_REQUEST_TOKENS = 100;
    private static final int MEDIUM_REQUEST_TOKENS = 500;
    private static final int LARGE_REQUEST_TOKENS = 1000;
    private static final int WARNING_THRESHOLD = 1000;
    
    // Tracking
    private long totalTokensUsed = 0;
    private long requestCount = 0;
    private long warningCount = 0;
    
    /**
     * Estimate input tokens from text
     */
    public int estimateInputTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        // Rough estimate: ~4 characters per token
        return Math.max(1, text.length() / 4);
    }
    
    /**
     * Calculate dynamic output limit based on input
     */
    public int calculateOutputLimit(String input) {
        int inputTokens = estimateInputTokens(input);
        
        // Dynamic scaling
        if (inputTokens < SMALL_REQUEST_TOKENS) {
            return 100;  // Small request → small output
        } else if (inputTokens < MEDIUM_REQUEST_TOKENS) {
            return 300;  // Medium request → medium output
        } else if (inputTokens < LARGE_REQUEST_TOKENS) {
            return 500;  // Large request → large output
        } else {
            return 200;  // Very large request → limited output (prevent overages)
        }
    }
    
    /**
     * Record token usage
     */
    public void recordUsage(String userId, int inputTokens, int outputTokens) {
        int totalTokens = inputTokens + outputTokens;
        totalTokensUsed += totalTokens;
        requestCount++;
        
        // Check for warnings
        if (totalTokens > WARNING_THRESHOLD) {
            warningCount++;
            logger.warn("⚠️ HIGH TOKEN USAGE: {} tokens (input: {}, output: {})", 
                totalTokens, inputTokens, outputTokens);
        }
        
        logger.info("💰 Token Usage: {} tokens (total: {}/{}, requests: {})", 
            totalTokens, totalTokensUsed, TOTAL_QUOTA, requestCount);
    }
    
    /**
     * Check if budget exceeded
     */
    public boolean isBudgetExceeded() {
        return totalTokensUsed >= TOTAL_QUOTA;
    }
    
    /**
     * Get remaining budget
     */
    public long getRemainingBudget() {
        return Math.max(0, TOTAL_QUOTA - totalTokensUsed);
    }
    
    /**
     * Get budget percentage used
     */
    public double getBudgetUsagePercent() {
        return (double) totalTokensUsed / TOTAL_QUOTA * 100;
    }
    
    /**
     * Get budget status
     */
    public BudgetStatus getStatus() {
        return new BudgetStatus(
            totalTokensUsed,
            TOTAL_QUOTA,
            getRemainingBudget(),
            getBudgetUsagePercent(),
            requestCount,
            warningCount,
            isBudgetExceeded()
        );
    }
    
    /**
     * Reset budget (for testing)
     */
    public void reset() {
        totalTokensUsed = 0;
        requestCount = 0;
        warningCount = 0;
        logger.info("💰 Token budget reset");
    }
    
    /**
     * Budget Status DTO
     */
    public static class BudgetStatus {
        public long used;
        public long total;
        public long remaining;
        public double usagePercent;
        public long requestCount;
        public long warningCount;
        public boolean exceeded;
        
        public BudgetStatus(long used, long total, long remaining, double usagePercent,
                          long requestCount, long warningCount, boolean exceeded) {
            this.used = used;
            this.total = total;
            this.remaining = remaining;
            this.usagePercent = usagePercent;
            this.requestCount = requestCount;
            this.warningCount = warningCount;
            this.exceeded = exceeded;
        }
        
        @Override
        public String toString() {
            return String.format(
                "BudgetStatus{used=%d, total=%d, remaining=%d, usage=%.1f%%, requests=%d, warnings=%d, exceeded=%s}",
                used, total, remaining, usagePercent, requestCount, warningCount, exceeded
            );
        }
    }
}

package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🧠 Brain 4: Learning System - Tracks query patterns and response quality
 * 
 * Responsibilities:
 * - Track successful vs failed queries
 * - Learn from user feedback
 * - Identify patterns in query types
 * - Optimize search strategies based on performance
 * - Store learning metrics for future queries
 */
@Service
public class LearningMetricsService {
    
    private static final Logger logger = LoggerFactory.getLogger(LearningMetricsService.class);
    
    // In-memory storage for metrics (replace with database in production)
    private final Map<String, QueryMetric> queryMetrics = new ConcurrentHashMap<>();
    private final Map<String, StrategyMetric> strategyMetrics = new ConcurrentHashMap<>();
    private final Map<String, QueryPattern> queryPatterns = new ConcurrentHashMap<>();
    
    /**
     * Record a successful query execution
     */
    public void recordQuerySuccess(String queryType, String strategy, double responseQuality, long responseTimeMs) {
        logger.info("📊 Learning: Recording successful query - Type: {}, Strategy: {}, Quality: {:.2f}", 
            queryType, strategy, responseQuality);
        
        // Update query metrics
        QueryMetric metric = queryMetrics.computeIfAbsent(queryType, k -> new QueryMetric(queryType));
        metric.recordSuccess(responseQuality, responseTimeMs);
        
        // Update strategy metrics
        String strategyKey = queryType + "_" + strategy;
        StrategyMetric strategyMetric = strategyMetrics.computeIfAbsent(strategyKey, 
            k -> new StrategyMetric(queryType, strategy));
        strategyMetric.recordSuccess(responseQuality);
    }
    
    /**
     * Record a failed query execution
     */
    public void recordQueryFailure(String queryType, String strategy, String reason) {
        logger.warn("📊 Learning: Recording failed query - Type: {}, Strategy: {}, Reason: {}", 
            queryType, strategy, reason);
        
        // Update query metrics
        QueryMetric metric = queryMetrics.computeIfAbsent(queryType, k -> new QueryMetric(queryType));
        metric.recordFailure();
        
        // Update strategy metrics
        String strategyKey = queryType + "_" + strategy;
        StrategyMetric strategyMetric = strategyMetrics.computeIfAbsent(strategyKey, 
            k -> new StrategyMetric(queryType, strategy));
        strategyMetric.recordFailure();
    }
    
    /**
     * Record user feedback on response quality
     */
    public void recordUserFeedback(String queryType, String strategy, int rating) {
        logger.info("📊 Learning: Recording user feedback - Type: {}, Strategy: {}, Rating: {}/5", 
            queryType, strategy, rating);
        
        String strategyKey = queryType + "_" + strategy;
        StrategyMetric strategyMetric = strategyMetrics.get(strategyKey);
        if (strategyMetric != null) {
            strategyMetric.recordUserFeedback(rating);
        }
    }
    
    /**
     * Get the best strategy for a query type
     */
    public String getBestStrategy(String queryType) {
        return strategyMetrics.entrySet().stream()
            .filter(e -> e.getKey().startsWith(queryType + "_"))
            .max(Comparator.comparingDouble(e -> e.getValue().getSuccessRate()))
            .map(e -> e.getValue().strategy)
            .orElse("similarity_search"); // Default fallback
    }
    
    /**
     * Get average response quality for a query type
     */
    public double getAverageQuality(String queryType) {
        QueryMetric metric = queryMetrics.get(queryType);
        return metric != null ? metric.getAverageQuality() : 0.5;
    }
    
    /**
     * Get success rate for a strategy
     */
    public double getStrategySuccessRate(String queryType, String strategy) {
        String key = queryType + "_" + strategy;
        StrategyMetric metric = strategyMetrics.get(key);
        return metric != null ? metric.getSuccessRate() : 0.0;
    }
    
    /**
     * Detect emerging query patterns
     */
    public List<QueryPattern> detectPatterns() {
        logger.info("🔍 Learning: Analyzing query patterns...");
        
        List<QueryPattern> patterns = new ArrayList<>();
        
        // Analyze query frequency and success rates
        queryMetrics.forEach((queryType, metric) -> {
            if (metric.getTotalQueries() >= 5) { // Minimum threshold
                QueryPattern pattern = new QueryPattern(
                    queryType,
                    metric.getTotalQueries(),
                    metric.getSuccessRate(),
                    getBestStrategy(queryType)
                );
                patterns.add(pattern);
                queryPatterns.put(queryType, pattern);
            }
        });
        
        logger.info("📈 Learning: Detected {} patterns", patterns.size());
        return patterns;
    }
    
    /**
     * Get learning insights
     */
    public LearningInsights getInsights() {
        LearningInsights insights = new LearningInsights();
        
        // Calculate overall metrics
        insights.totalQueries = queryMetrics.values().stream()
            .mapToInt(QueryMetric::getTotalQueries)
            .sum();
        
        insights.overallSuccessRate = queryMetrics.values().stream()
            .mapToDouble(QueryMetric::getSuccessRate)
            .average()
            .orElse(0.0);
        
        insights.averageQuality = queryMetrics.values().stream()
            .mapToDouble(QueryMetric::getAverageQuality)
            .average()
            .orElse(0.5);
        
        insights.bestPerformingStrategy = strategyMetrics.values().stream()
            .max(Comparator.comparingDouble(StrategyMetric::getSuccessRate))
            .map(s -> s.strategy)
            .orElse("unknown");
        
        insights.detectedPatterns = detectPatterns();
        
        return insights;
    }
    
    /**
     * Get metrics summary for logging
     */
    public String getMetricsSummary() {
        LearningInsights insights = getInsights();
        return String.format(
            "📊 Learning Metrics - Total: %d | Success Rate: %.1f%% | Avg Quality: %.2f/5.0 | Best Strategy: %s",
            insights.totalQueries,
            insights.overallSuccessRate * 100,
            insights.averageQuality,
            insights.bestPerformingStrategy
        );
    }
    
    // Inner classes for metrics tracking
    
    public static class QueryMetric {
        private final String queryType;
        private int successCount = 0;
        private int failureCount = 0;
        private double totalQuality = 0;
        private long totalResponseTime = 0;
        private LocalDateTime lastUpdated;
        
        public QueryMetric(String queryType) {
            this.queryType = queryType;
            this.lastUpdated = LocalDateTime.now();
        }
        
        public void recordSuccess(double quality, long responseTimeMs) {
            this.successCount++;
            this.totalQuality += quality;
            this.totalResponseTime += responseTimeMs;
            this.lastUpdated = LocalDateTime.now();
        }
        
        public void recordFailure() {
            this.failureCount++;
            this.lastUpdated = LocalDateTime.now();
        }
        
        public double getSuccessRate() {
            int total = successCount + failureCount;
            return total > 0 ? (double) successCount / total : 0.0;
        }
        
        public double getAverageQuality() {
            return successCount > 0 ? totalQuality / successCount : 0.0;
        }
        
        public long getAverageResponseTime() {
            return successCount > 0 ? totalResponseTime / successCount : 0;
        }
        
        public int getTotalQueries() {
            return successCount + failureCount;
        }
    }
    
    public static class StrategyMetric {
        private final String queryType;
        private final String strategy;
        private int successCount = 0;
        private int failureCount = 0;
        private int userFeedbackSum = 0;
        private int feedbackCount = 0;
        
        public StrategyMetric(String queryType, String strategy) {
            this.queryType = queryType;
            this.strategy = strategy;
        }
        
        public void recordSuccess(double quality) {
            this.successCount++;
        }
        
        public void recordFailure() {
            this.failureCount++;
        }
        
        public void recordUserFeedback(int rating) {
            this.userFeedbackSum += rating;
            this.feedbackCount++;
        }
        
        public double getSuccessRate() {
            int total = successCount + failureCount;
            return total > 0 ? (double) successCount / total : 0.0;
        }
        
        public double getAverageUserRating() {
            return feedbackCount > 0 ? (double) userFeedbackSum / feedbackCount : 0.0;
        }
    }
    
    public static class QueryPattern {
        public final String patternName;
        public final int frequency;
        public final double successRate;
        public final String recommendedStrategy;
        public final LocalDateTime detectedAt;
        
        public QueryPattern(String patternName, int frequency, double successRate, String recommendedStrategy) {
            this.patternName = patternName;
            this.frequency = frequency;
            this.successRate = successRate;
            this.recommendedStrategy = recommendedStrategy;
            this.detectedAt = LocalDateTime.now();
        }
    }
    
    public static class LearningInsights {
        public int totalQueries;
        public double overallSuccessRate;
        public double averageQuality;
        public String bestPerformingStrategy;
        public List<QueryPattern> detectedPatterns;
    }
}

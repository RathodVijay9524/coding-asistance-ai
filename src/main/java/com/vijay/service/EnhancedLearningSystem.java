package com.vijay.service;

import com.vijay.dto.LearningMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 🧠 Enhanced Learning System
 * 
 * Tracks and optimizes strategies based on continuous learning from interactions.
 * Learns which strategies work best for different query types and user profiles.
 */
@Service
public class EnhancedLearningSystem {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedLearningSystem.class);
    
    private final Map<String, LearningMetric> metrics = new ConcurrentHashMap<>();
    private final Map<String, List<String>> strategyHistory = new ConcurrentHashMap<>();
    private final Map<String, Double> queryTypeSuccessRates = new ConcurrentHashMap<>();
    
    private static final int HISTORY_SIZE = 100;
    
    public EnhancedLearningSystem() {
        logger.info("🧠 Enhanced Learning System initialized - Continuous improvement engine");
    }
    
    /**
     * Record strategy usage and outcome
     */
    public void recordStrategyOutcome(String queryType, String strategy, 
                                     boolean success, double quality, double satisfaction, double responseTime) {
        logger.debug("📊 Recording strategy outcome - Type: {}, Strategy: {}, Success: {}", 
            queryType, strategy, success);
        
        String metricKey = queryType + "_" + strategy;
        LearningMetric metric = metrics.computeIfAbsent(metricKey, 
            k -> new LearningMetric(queryType, strategy));
        
        if (success) {
            metric.incrementSuccess();
        } else {
            metric.incrementFailure();
        }
        
        metric.updateQuality(quality);
        metric.updateSatisfaction(satisfaction);
        metric.updateResponseTime(responseTime);
        
        // Update query type success rate
        updateQueryTypeSuccessRate(queryType);
        
        // Record in history
        recordInHistory(queryType, strategy);
        
        logger.debug("✅ Strategy outcome recorded - {}", metric.getSummary());
    }
    
    /**
     * Get best strategy for query type
     */
    public String getBestStrategy(String queryType) {
        logger.debug("🎯 Finding best strategy for query type: {}", queryType);
        
        return metrics.values().stream()
            .filter(m -> m.getQueryType().equals(queryType))
            .max(Comparator.comparingDouble(LearningMetric::getEffectivenessScore))
            .map(LearningMetric::getStrategy)
            .orElse("balanced");
    }
    
    /**
     * Get all strategies ranked by effectiveness
     */
    public List<String> getRankedStrategies(String queryType) {
        logger.debug("📊 Ranking strategies for query type: {}", queryType);
        
        return metrics.values().stream()
            .filter(m -> m.getQueryType().equals(queryType))
            .sorted(Comparator.comparingDouble(LearningMetric::getEffectivenessScore).reversed())
            .map(LearningMetric::getStrategy)
            .collect(Collectors.toList());
    }
    
    /**
     * Get learning metric for strategy
     */
    public LearningMetric getMetric(String queryType, String strategy) {
        String key = queryType + "_" + strategy;
        return metrics.get(key);
    }
    
    /**
     * Update query type success rate
     */
    private void updateQueryTypeSuccessRate(String queryType) {
        double avgSuccessRate = metrics.values().stream()
            .filter(m -> m.getQueryType().equals(queryType))
            .mapToDouble(LearningMetric::getSuccessRate)
            .average()
            .orElse(0.5);
        
        queryTypeSuccessRates.put(queryType, avgSuccessRate);
        logger.debug("📈 Query type success rate updated - {}: {:.1f}%", 
            queryType, avgSuccessRate * 100);
    }
    
    /**
     * Record strategy in history
     */
    private void recordInHistory(String queryType, String strategy) {
        List<String> history = strategyHistory.computeIfAbsent(queryType, k -> new ArrayList<>());
        history.add(strategy);
        
        // Keep only recent history
        if (history.size() > HISTORY_SIZE) {
            history.remove(0);
        }
    }
    
    /**
     * Get strategy effectiveness trend
     */
    public String getStrategyTrend(String queryType, String strategy) {
        LearningMetric metric = getMetric(queryType, strategy);
        if (metric == null) {
            return "No data available";
        }
        
        double effectiveness = metric.getEffectivenessScore();
        if (effectiveness >= 0.85) {
            return "📈 Excellent - High effectiveness";
        } else if (effectiveness >= 0.75) {
            return "📊 Good - Solid performance";
        } else if (effectiveness >= 0.65) {
            return "⚠️ Fair - Room for improvement";
        } else {
            return "📉 Poor - Consider alternatives";
        }
    }
    
    /**
     * Get learning summary for query type
     */
    public String getLearningsSummary(String queryType) {
        List<LearningMetric> queryMetrics = metrics.values().stream()
            .filter(m -> m.getQueryType().equals(queryType))
            .collect(Collectors.toList());
        
        if (queryMetrics.isEmpty()) {
            return "No learnings yet for query type: " + queryType;
        }
        
        double avgEffectiveness = queryMetrics.stream()
            .mapToDouble(LearningMetric::getEffectivenessScore)
            .average()
            .orElse(0);
        
        String bestStrategy = queryMetrics.stream()
            .max(Comparator.comparingDouble(LearningMetric::getEffectivenessScore))
            .map(LearningMetric::getStrategy)
            .orElse("unknown");
        
        return String.format(
            "📚 Learnings for %s: %d strategies tested | Avg Effectiveness: %.2f | Best: %s",
            queryType, queryMetrics.size(), avgEffectiveness, bestStrategy
        );
    }
    
    /**
     * Get all metrics
     */
    public Collection<LearningMetric> getAllMetrics() {
        return metrics.values();
    }
    
    /**
     * Get metrics for query type
     */
    public List<LearningMetric> getMetricsForQueryType(String queryType) {
        return metrics.values().stream()
            .filter(m -> m.getQueryType().equals(queryType))
            .sorted(Comparator.comparingDouble(LearningMetric::getEffectivenessScore).reversed())
            .collect(Collectors.toList());
    }
    
    /**
     * Get overall learning statistics
     */
    public String getOverallStatistics() {
        int totalMetrics = metrics.size();
        double avgEffectiveness = metrics.values().stream()
            .mapToDouble(LearningMetric::getEffectivenessScore)
            .average()
            .orElse(0);
        
        int totalAttempts = metrics.values().stream()
            .mapToInt(LearningMetric::getTotalAttempts)
            .sum();
        
        int totalSuccesses = metrics.values().stream()
            .mapToInt(LearningMetric::getSuccessCount)
            .sum();
        
        double overallSuccessRate = totalAttempts > 0 ? 
            (double) totalSuccesses / totalAttempts : 0;
        
        return String.format(
            "📊 Overall Learning Statistics: %d strategies | Avg Effectiveness: %.2f | " +
            "Total Attempts: %d | Success Rate: %.1f%%",
            totalMetrics, avgEffectiveness, totalAttempts, overallSuccessRate * 100
        );
    }
    
    /**
     * Reset learning for specific query type
     */
    public void resetLearnings(String queryType) {
        metrics.entrySet().removeIf(e -> e.getKey().startsWith(queryType + "_"));
        strategyHistory.remove(queryType);
        queryTypeSuccessRates.remove(queryType);
        
        logger.info("🔄 Learning reset for query type: {}", queryType);
    }
}

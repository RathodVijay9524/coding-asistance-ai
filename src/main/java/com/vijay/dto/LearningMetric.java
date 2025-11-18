package com.vijay.dto;

import java.time.LocalDateTime;

/**
 * 🧠 Learning Metric
 * 
 * Tracks individual learning metrics for continuous improvement.
 * Records success rates, quality scores, and effectiveness of different strategies.
 */
public class LearningMetric {
    
    private String metricId;
    private String queryType;              // Type of query (e.g., "code_explanation", "debugging")
    private String strategy;               // Strategy used (e.g., "concise", "detailed")
    private int successCount;              // Number of successful responses
    private int failureCount;              // Number of failed responses
    private double avgResponseQuality;     // Average quality score (0-1)
    private double avgUserSatisfaction;    // Average user satisfaction (0-1)
    private double avgResponseTime;        // Average response time in ms
    private double effectivenessScore;     // Overall effectiveness (0-1)
    private LocalDateTime lastUpdated;
    private int totalAttempts;             // Total attempts with this strategy
    
    // Constructor
    public LearningMetric(String queryType, String strategy) {
        this.metricId = queryType + "_" + strategy;
        this.queryType = queryType;
        this.strategy = strategy;
        this.successCount = 0;
        this.failureCount = 0;
        this.avgResponseQuality = 0.5;
        this.avgUserSatisfaction = 0.5;
        this.avgResponseTime = 0;
        this.effectivenessScore = 0.5;
        this.lastUpdated = LocalDateTime.now();
        this.totalAttempts = 0;
    }
    
    // Getters and Setters
    public String getMetricId() {
        return metricId;
    }
    
    public String getQueryType() {
        return queryType;
    }
    
    public String getStrategy() {
        return strategy;
    }
    
    public int getSuccessCount() {
        return successCount;
    }
    
    public void incrementSuccess() {
        this.successCount++;
        this.totalAttempts++;
        updateEffectivenessScore();
    }
    
    public int getFailureCount() {
        return failureCount;
    }
    
    public void incrementFailure() {
        this.failureCount++;
        this.totalAttempts++;
        updateEffectivenessScore();
    }
    
    public double getAvgResponseQuality() {
        return avgResponseQuality;
    }
    
    public void updateQuality(double quality) {
        this.avgResponseQuality = (avgResponseQuality * (totalAttempts - 1) + quality) / totalAttempts;
        updateEffectivenessScore();
    }
    
    public double getAvgUserSatisfaction() {
        return avgUserSatisfaction;
    }
    
    public void updateSatisfaction(double satisfaction) {
        this.avgUserSatisfaction = (avgUserSatisfaction * (totalAttempts - 1) + satisfaction) / totalAttempts;
        updateEffectivenessScore();
    }
    
    public double getAvgResponseTime() {
        return avgResponseTime;
    }
    
    public void updateResponseTime(double responseTime) {
        this.avgResponseTime = (avgResponseTime * (totalAttempts - 1) + responseTime) / totalAttempts;
    }
    
    public double getEffectivenessScore() {
        return effectivenessScore;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public int getTotalAttempts() {
        return totalAttempts;
    }
    
    /**
     * Calculate success rate
     */
    public double getSuccessRate() {
        if (totalAttempts == 0) return 0;
        return (double) successCount / totalAttempts;
    }
    
    /**
     * Update effectiveness score based on all factors
     */
    private void updateEffectivenessScore() {
        // Weighted: success rate 40%, quality 30%, satisfaction 20%, response time 10%
        double successRateScore = getSuccessRate();
        double qualityScore = avgResponseQuality;
        double satisfactionScore = avgUserSatisfaction;
        double timeScore = Math.max(0, 1 - (avgResponseTime / 5000)); // Penalize slow responses
        
        this.effectivenessScore = (successRateScore * 0.4) + 
                                 (qualityScore * 0.3) + 
                                 (satisfactionScore * 0.2) + 
                                 (timeScore * 0.1);
        
        this.lastUpdated = LocalDateTime.now();
    }
    
    /**
     * Get metric summary
     */
    public String getSummary() {
        return String.format(
            "Strategy: %s | Success Rate: %.1f%% | Quality: %.2f | Satisfaction: %.2f | Effectiveness: %.2f",
            strategy, getSuccessRate() * 100, avgResponseQuality, avgUserSatisfaction, effectivenessScore
        );
    }
    
    @Override
    public String toString() {
        return "LearningMetric{" +
                "strategy='" + strategy + '\'' +
                ", successRate=" + String.format("%.1f%%", getSuccessRate() * 100) +
                ", effectiveness=" + String.format("%.2f", effectivenessScore) +
                ", attempts=" + totalAttempts +
                '}';
    }
}

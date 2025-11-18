package com.vijay.dto;

/**
 * 🧠 Response Scenario
 * 
 * Represents a potential response scenario for mental simulation.
 * Used to evaluate multiple possible responses before choosing the best one.
 */
public class ResponseScenario {
    
    private String responseText;
    private double qualityScore;        // 0-1 scale
    private double userSatisfactionScore; // 0-1 scale
    private double relevanceScore;      // 0-1 scale
    private double clarityScore;        // 0-1 scale
    private String tone;                // Response tone
    private int estimatedLength;        // Estimated response length
    private boolean includesExamples;
    private boolean includesExplanation;
    private double overallScore;        // Weighted average of all scores
    
    // Constructor
    public ResponseScenario(String responseText) {
        this.responseText = responseText;
        this.qualityScore = 0.5;
        this.userSatisfactionScore = 0.5;
        this.relevanceScore = 0.5;
        this.clarityScore = 0.5;
        this.tone = "neutral";
        this.estimatedLength = responseText.length();
        this.includesExamples = false;
        this.includesExplanation = false;
        this.overallScore = 0.5;
    }
    
    // Getters and Setters
    public String getResponseText() {
        return responseText;
    }
    
    public double getQualityScore() {
        return qualityScore;
    }
    
    public void setQualityScore(double qualityScore) {
        this.qualityScore = Math.max(0, Math.min(1, qualityScore));
        updateOverallScore();
    }
    
    public double getUserSatisfactionScore() {
        return userSatisfactionScore;
    }
    
    public void setUserSatisfactionScore(double userSatisfactionScore) {
        this.userSatisfactionScore = Math.max(0, Math.min(1, userSatisfactionScore));
        updateOverallScore();
    }
    
    public double getRelevanceScore() {
        return relevanceScore;
    }
    
    public void setRelevanceScore(double relevanceScore) {
        this.relevanceScore = Math.max(0, Math.min(1, relevanceScore));
        updateOverallScore();
    }
    
    public double getClarityScore() {
        return clarityScore;
    }
    
    public void setClarityScore(double clarityScore) {
        this.clarityScore = Math.max(0, Math.min(1, clarityScore));
        updateOverallScore();
    }
    
    public String getTone() {
        return tone;
    }
    
    public void setTone(String tone) {
        this.tone = tone;
    }
    
    public int getEstimatedLength() {
        return estimatedLength;
    }
    
    public void setEstimatedLength(int estimatedLength) {
        this.estimatedLength = estimatedLength;
    }
    
    public boolean isIncludesExamples() {
        return includesExamples;
    }
    
    public void setIncludesExamples(boolean includesExamples) {
        this.includesExamples = includesExamples;
    }
    
    public boolean isIncludesExplanation() {
        return includesExplanation;
    }
    
    public void setIncludesExplanation(boolean includesExplanation) {
        this.includesExplanation = includesExplanation;
    }
    
    public double getOverallScore() {
        return overallScore;
    }
    
    /**
     * Update overall score based on component scores
     */
    private void updateOverallScore() {
        // Weighted average: quality 40%, satisfaction 30%, relevance 20%, clarity 10%
        this.overallScore = (qualityScore * 0.4) + 
                           (userSatisfactionScore * 0.3) + 
                           (relevanceScore * 0.2) + 
                           (clarityScore * 0.1);
    }
    
    /**
     * Get scenario summary
     */
    public String getSummary() {
        return String.format(
            "Scenario: %s | Quality: %.2f | Satisfaction: %.2f | Relevance: %.2f | Clarity: %.2f | Overall: %.2f",
            tone, qualityScore, userSatisfactionScore, relevanceScore, clarityScore, overallScore
        );
    }
    
    @Override
    public String toString() {
        return "ResponseScenario{" +
                "tone='" + tone + '\'' +
                ", overallScore=" + String.format("%.2f", overallScore) +
                ", length=" + estimatedLength +
                ", examples=" + includesExamples +
                ", explanation=" + includesExplanation +
                '}';
    }
}

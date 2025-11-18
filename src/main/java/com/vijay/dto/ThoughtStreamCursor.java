package com.vijay.dto;

/**
 * 🧠 Thought Stream Cursor
 * 
 * Represents the "cursor" that guides attention in the thought stream.
 * Determines what to focus on, what to ignore, and routing strategy.
 */
public class ThoughtStreamCursor {
    
    private String queryId;
    private String focusArea;              // What to focus on
    private String ignoreArea;             // What to ignore
    private ReasoningStrategy strategy;    // Slow vs fast reasoning
    private double complexity;             // 0-1 scale
    private double ambiguity;              // 0-1 scale
    private boolean needsSlowReasoning;    // Requires deep thinking
    private boolean needsFastRecall;       // Requires quick lookup
    private String[] relevantBrains;       // Which brains to activate
    private double confidence;             // Confidence in routing decision
    
    public enum ReasoningStrategy {
        FAST_RECALL("Fast Recall", 0),           // Quick lookup, no reasoning
        FAST_REASONING("Fast Reasoning", 1),     // Quick analysis
        BALANCED("Balanced", 2),                 // Mix of fast and slow
        SLOW_REASONING("Slow Reasoning", 3),     // Deep analysis
        VERY_SLOW_REASONING("Very Slow", 4);    // Maximum depth
        
        private final String displayName;
        private final int level;
        
        ReasoningStrategy(String displayName, int level) {
            this.displayName = displayName;
            this.level = level;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getLevel() {
            return level;
        }
    }
    
    // Constructor
    public ThoughtStreamCursor(String queryId) {
        this.queryId = queryId;
        this.focusArea = "general";
        this.ignoreArea = "none";
        this.strategy = ReasoningStrategy.BALANCED;
        this.complexity = 0.5;
        this.ambiguity = 0.5;
        this.needsSlowReasoning = false;
        this.needsFastRecall = false;
        this.relevantBrains = new String[0];
        this.confidence = 0.5;
    }
    
    // Getters and Setters
    public String getQueryId() {
        return queryId;
    }
    
    public String getFocusArea() {
        return focusArea;
    }
    
    public void setFocusArea(String focusArea) {
        this.focusArea = focusArea;
    }
    
    public String getIgnoreArea() {
        return ignoreArea;
    }
    
    public void setIgnoreArea(String ignoreArea) {
        this.ignoreArea = ignoreArea;
    }
    
    public ReasoningStrategy getStrategy() {
        return strategy;
    }
    
    public void setStrategy(ReasoningStrategy strategy) {
        this.strategy = strategy;
    }
    
    public double getComplexity() {
        return complexity;
    }
    
    public void setComplexity(double complexity) {
        this.complexity = Math.max(0, Math.min(1, complexity));
    }
    
    public double getAmbiguity() {
        return ambiguity;
    }
    
    public void setAmbiguity(double ambiguity) {
        this.ambiguity = Math.max(0, Math.min(1, ambiguity));
    }
    
    public boolean isNeedsSlowReasoning() {
        return needsSlowReasoning;
    }
    
    public void setNeedsSlowReasoning(boolean needsSlowReasoning) {
        this.needsSlowReasoning = needsSlowReasoning;
    }
    
    public boolean isNeedsFastRecall() {
        return needsFastRecall;
    }
    
    public void setNeedsFastRecall(boolean needsFastRecall) {
        this.needsFastRecall = needsFastRecall;
    }
    
    public String[] getRelevantBrains() {
        return relevantBrains;
    }
    
    public void setRelevantBrains(String[] relevantBrains) {
        this.relevantBrains = relevantBrains;
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(double confidence) {
        this.confidence = Math.max(0, Math.min(1, confidence));
    }
    
    /**
     * Get cursor summary
     */
    public String getSummary() {
        return String.format(
            "Cursor: Focus=%s | Ignore=%s | Strategy=%s | Complexity=%.2f | Ambiguity=%.2f | Confidence=%.2f",
            focusArea, ignoreArea, strategy.getDisplayName(), complexity, ambiguity, confidence
        );
    }
    
    @Override
    public String toString() {
        return "ThoughtStreamCursor{" +
                "queryId='" + queryId + '\'' +
                ", focusArea='" + focusArea + '\'' +
                ", strategy=" + strategy.getDisplayName() +
                ", complexity=" + String.format("%.2f", complexity) +
                ", ambiguity=" + String.format("%.2f", ambiguity) +
                ", slowReasoning=" + needsSlowReasoning +
                ", fastRecall=" + needsFastRecall +
                '}';
    }
}

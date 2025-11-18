package com.vijay.dto;

import java.util.List;

/**
 * 📋 AgentPlan DTO
 * 
 * The master plan created by ConductorAdvisor (Brain 0)
 * 
 * This plan is stored in the request context and read by all downstream brains:
 * - Brain 1 (DynamicContextAdvisor): Uses intent to fetch context
 * - Brain 2 (ToolCallAdvisor): Uses requiredTools to execute tools
 * - Brain 13 (Judge): Uses complexity to adjust quality checks
 * - Brain 14 (Voice): Uses intent to adjust personality
 * 
 * This ensures ONE unified thought process (no split brain)
 */
public class AgentPlan {
    
    // Core planning information
    private String intent;                    // CALCULATION, DEBUG, REFACTOR, IMPLEMENTATION, EXPLANATION, TESTING, GENERAL
    private int complexity;                   // 1-10: How complex is this query?
    private int ambiguity;                    // 1-10: How ambiguous is this query?
    private String focusArea;                 // What to focus on: DEBUG, REFACTOR, TESTING, ARCHITECTURE, PERFORMANCE, SECURITY, IMPLEMENTATION, GENERAL
    private String ignoreArea;                // What to skip: CONSTRAINTS, NONE
    private String strategy;                  // FAST_RECALL, BALANCED, SLOW_REASONING
    
    // Execution information
    private List<String> requiredTools;       // Tools needed: add, subtract, multiply, divide, etc.
    private List<String> selectedBrains;      // Specialist brains to activate
    
    // Confidence and metadata
    private double confidence;                // 0.0-1.0: How confident are we in this plan?
    private String userQuery;                 // Original user query
    private long createdAt;                   // When was this plan created?
    
    // Constructors
    public AgentPlan() {
    }
    
    public AgentPlan(String intent, int complexity, int ambiguity, String focusArea, String strategy) {
        this.intent = intent;
        this.complexity = complexity;
        this.ambiguity = ambiguity;
        this.focusArea = focusArea;
        this.strategy = strategy;
        this.createdAt = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getIntent() {
        return intent;
    }
    
    public AgentPlan setIntent(String intent) {
        this.intent = intent;
        return this;
    }
    
    public int getComplexity() {
        return complexity;
    }
    
    public AgentPlan setComplexity(int complexity) {
        this.complexity = complexity;
        return this;
    }
    
    public int getAmbiguity() {
        return ambiguity;
    }
    
    public AgentPlan setAmbiguity(int ambiguity) {
        this.ambiguity = ambiguity;
        return this;
    }
    
    public String getFocusArea() {
        return focusArea;
    }
    
    public AgentPlan setFocusArea(String focusArea) {
        this.focusArea = focusArea;
        return this;
    }
    
    public String getIgnoreArea() {
        return ignoreArea;
    }
    
    public AgentPlan setIgnoreArea(String ignoreArea) {
        this.ignoreArea = ignoreArea;
        return this;
    }
    
    public String getStrategy() {
        return strategy;
    }
    
    public AgentPlan setStrategy(String strategy) {
        this.strategy = strategy;
        return this;
    }
    
    public List<String> getRequiredTools() {
        return requiredTools;
    }
    
    public AgentPlan setRequiredTools(List<String> requiredTools) {
        this.requiredTools = requiredTools;
        return this;
    }
    
    public List<String> getSelectedBrains() {
        return selectedBrains;
    }
    
    public AgentPlan setSelectedBrains(List<String> selectedBrains) {
        this.selectedBrains = selectedBrains;
        return this;
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    public AgentPlan setConfidence(double confidence) {
        this.confidence = confidence;
        return this;
    }
    
    public String getUserQuery() {
        return userQuery;
    }
    
    public AgentPlan setUserQuery(String userQuery) {
        this.userQuery = userQuery;
        return this;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public AgentPlan setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    
    // Utility methods
    public boolean isHighComplexity() {
        return complexity > 7;
    }
    
    public boolean isHighAmbiguity() {
        return ambiguity > 7;
    }
    
    public boolean isHighConfidence() {
        return confidence > 0.8;
    }
    
    public boolean requiresTools() {
        return requiredTools != null && !requiredTools.isEmpty();
    }
    
    public boolean hasSpecialistBrains() {
        return selectedBrains != null && !selectedBrains.isEmpty();
    }
    
    @Override
    public String toString() {
        return "AgentPlan{" +
                "intent='" + intent + '\'' +
                ", complexity=" + complexity +
                ", ambiguity=" + ambiguity +
                ", focusArea='" + focusArea + '\'' +
                ", strategy='" + strategy + '\'' +
                ", requiredTools=" + requiredTools +
                ", selectedBrains=" + selectedBrains +
                ", confidence=" + confidence +
                '}';
    }
}

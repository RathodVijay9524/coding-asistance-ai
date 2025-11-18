package com.vijay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 🗳️ BrainVote - Voting System for Brain Decisions
 * 
 * Purpose: Enable confidence-based decision making instead of deterministic
 * 
 * Each brain votes on whether tools are needed:
 * - SmartFinder: 85% (found relevant tools)
 * - NLPClassifier: 60% (intent suggests tools)
 * - MemoryBrain: 20% (similar queries in history)
 * 
 * Conductor combines votes:
 * - Average: (85 + 60 + 20) / 3 = 55%
 * - Decision: Enable tools (> 50%)
 * 
 * Benefits:
 * ✅ Confidence-based decisions
 * ✅ Multiple perspectives
 * ✅ Explainable AI (why was decision made)
 * ✅ Easy to debug
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrainVote {
    
    // Voting brain
    private String brainName;              // e.g., "smartFinder", "nlpClassifier"
    
    // Vote value (0.0 - 1.0)
    private double toolRequiredScore;      // Confidence that tools are needed
    
    // Reasoning
    private String reasoning;              // Why this brain voted this way
    
    // Metadata
    private long timestamp;                // When vote was cast
    private String category;               // e.g., "TOOL_REQUIRED", "COMPLEXITY", "SAFETY"
    
    /**
     * Constructor with basic info
     */
    public BrainVote(String brainName, double score, String reasoning) {
        this.brainName = brainName;
        this.toolRequiredScore = score;
        this.reasoning = reasoning;
        this.timestamp = System.currentTimeMillis();
        this.category = "TOOL_REQUIRED";
    }
    
    /**
     * Constructor with category
     */
    public BrainVote(String brainName, double score, String reasoning, String category) {
        this.brainName = brainName;
        this.toolRequiredScore = score;
        this.reasoning = reasoning;
        this.timestamp = System.currentTimeMillis();
        this.category = category;
    }
    
    /**
     * Get vote as percentage (0-100)
     */
    public double getScoreAsPercentage() {
        return toolRequiredScore * 100;
    }
    
    /**
     * Get vote strength (WEAK, MEDIUM, STRONG)
     */
    public String getStrength() {
        if (toolRequiredScore < 0.33) {
            return "WEAK";
        } else if (toolRequiredScore < 0.67) {
            return "MEDIUM";
        } else {
            return "STRONG";
        }
    }
    
    /**
     * String representation for logging
     */
    @Override
    public String toString() {
        return String.format(
            "%s: %.0f%% (%s) - %s",
            brainName,
            getScoreAsPercentage(),
            getStrength(),
            reasoning
        );
    }
}

package com.vijay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 🧠 ReasoningState - Unified State Object for All Brains
 * 
 * This is the SINGLE SOURCE OF TRUTH for all brain decisions.
 * 
 * Flow:
 * 1. SmartFinder populates: suggestedTools, vectorMatches
 * 2. ConductorAdvisor sets: approvedTools (FINAL DECISION)
 * 3. ToolCallAdvisor checks: isToolApproved() before execution
 * 4. All brains read: metadata, intent, strategy
 * 
 * Benefits:
 * ✅ No dual decision-making
 * ✅ Single source of truth
 * ✅ Easy to debug
 * ✅ Shared context
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReasoningState {
    
    // Tracking
    private String traceId;                    // UUID for request tracking
    private long timestamp;                    // When created
    
    // User Input
    private String userQuery;                  // Original user input
    
    // Tool Decisions
    private List<String> suggestedTools;       // From SmartFinder (suggestions)
    private List<String> approvedTools;        // From ConductorAdvisor (FINAL DECISION)
    
    // Analysis Results
    private String intent;                     // User's intent (e.g., CALCULATION, QUERY)
    private String strategy;                   // Reasoning strategy
    private double confidence;                 // Overall confidence (0.0 - 1.0)
    
    // Context
    private Map<String, Object> metadata;      // Additional context from all brains
    
    // Voting System (Phase 2)
    private List<BrainVote> votes;             // Votes from all brains
    
    /**
     * Constructor with trace ID
     */
    public ReasoningState(String userQuery) {
        this.traceId = UUID.randomUUID().toString();
        this.userQuery = userQuery;
        this.timestamp = System.currentTimeMillis();
        this.votes = new ArrayList<>();
    }
    
    /**
     * Conductor's final decision: approve tools
     */
    public void approveTools(List<String> tools) {
        this.approvedTools = tools;
    }
    
    /**
     * Check if a specific tool is approved
     */
    public boolean isToolApproved(String toolName) {
        return approvedTools != null && approvedTools.contains(toolName);
    }
    
    /**
     * Get all approved tools
     */
    public List<String> getApprovedTools() {
        return approvedTools;
    }
    
    /**
     * Check if any tools are approved
     */
    public boolean hasApprovedTools() {
        return approvedTools != null && !approvedTools.isEmpty();
    }
    
    /**
     * Add metadata
     */
    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new java.util.HashMap<>();
        }
        this.metadata.put(key, value);
    }
    
    /**
     * Get metadata
     */
    public Object getMetadata(String key) {
        if (this.metadata == null) {
            return null;
        }
        return this.metadata.get(key);
    }
    
    /**
     * Add a vote from a brain (Phase 2)
     */
    public void addVote(BrainVote vote) {
        if (this.votes == null) {
            this.votes = new ArrayList<>();
        }
        this.votes.add(vote);
    }
    
    /**
     * Get average vote score (Phase 2)
     */
    public double getAverageVoteScore() {
        if (votes == null || votes.isEmpty()) {
            return 0.0;
        }
        return votes.stream()
            .mapToDouble(BrainVote::getToolRequiredScore)
            .average()
            .orElse(0.0);
    }
    
    /**
     * Get vote count (Phase 2)
     */
    public int getVoteCount() {
        return votes != null ? votes.size() : 0;
    }
    
    /**
     * Get votes as string for logging (Phase 2)
     */
    public String getVotesAsString() {
        if (votes == null || votes.isEmpty()) {
            return "No votes";
        }
        StringBuilder sb = new StringBuilder();
        for (BrainVote vote : votes) {
            sb.append(vote.toString()).append("; ");
        }
        return sb.toString();
    }
    
    /**
     * String representation for logging
     */
    @Override
    public String toString() {
        return String.format(
            "ReasoningState{traceId='%s', query='%s', suggested=%s, approved=%s, intent='%s', confidence=%.2f, votes=%d}",
            traceId,
            userQuery.length() > 50 ? userQuery.substring(0, 50) + "..." : userQuery,
            suggestedTools,
            approvedTools,
            intent,
            confidence,
            getVoteCount()
        );
    }
}

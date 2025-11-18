package com.vijay.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the visual attention state for a query
 * Tracks primary focus, secondary focus, and context window
 */
public class VisualAttentionState {
    private String primaryFocus; // Main entity to focus on
    private List<String> secondaryFocus = new ArrayList<>(); // Related entities
    private List<String> contextWindow = new ArrayList<>(); // Relevant code blocks
    private double primaryFocusScore; // 0-100
    private double contextRelevanceScore; // 0-100
    private int focusDepth; // How deep to analyze
    private String focusType; // ENTITY, PATTERN, ISSUE, SOLUTION, etc.
    
    // ===== Getters and Setters =====
    
    public String getPrimaryFocus() {
        return primaryFocus;
    }
    
    public VisualAttentionState setPrimaryFocus(String primaryFocus) {
        this.primaryFocus = primaryFocus;
        return this;
    }
    
    public List<String> getSecondaryFocus() {
        return secondaryFocus;
    }
    
    public VisualAttentionState setSecondaryFocus(List<String> secondaryFocus) {
        this.secondaryFocus = secondaryFocus;
        return this;
    }
    
    public VisualAttentionState addSecondaryFocus(String focus) {
        this.secondaryFocus.add(focus);
        return this;
    }
    
    public List<String> getContextWindow() {
        return contextWindow;
    }
    
    public VisualAttentionState setContextWindow(List<String> contextWindow) {
        this.contextWindow = contextWindow;
        return this;
    }
    
    public VisualAttentionState addContextWindow(String context) {
        this.contextWindow.add(context);
        return this;
    }
    
    public double getPrimaryFocusScore() {
        return primaryFocusScore;
    }
    
    public VisualAttentionState setPrimaryFocusScore(double score) {
        this.primaryFocusScore = Math.min(100, Math.max(0, score));
        return this;
    }
    
    public double getContextRelevanceScore() {
        return contextRelevanceScore;
    }
    
    public VisualAttentionState setContextRelevanceScore(double score) {
        this.contextRelevanceScore = Math.min(100, Math.max(0, score));
        return this;
    }
    
    public int getFocusDepth() {
        return focusDepth;
    }
    
    public VisualAttentionState setFocusDepth(int depth) {
        this.focusDepth = Math.min(10, Math.max(1, depth));
        return this;
    }
    
    public String getFocusType() {
        return focusType;
    }
    
    public VisualAttentionState setFocusType(String focusType) {
        this.focusType = focusType;
        return this;
    }
    
    @Override
    public String toString() {
        return String.format(
            "VisualAttentionState{primary=%s, secondary=%d, context=%d, score=%.1f}",
            primaryFocus, secondaryFocus.size(), contextWindow.size(), primaryFocusScore
        );
    }
}

package com.vijay.dto;

import java.time.LocalDateTime;

/**
 * Represents a single thought in working memory
 * Used by WorkingMemoryManager to track recent thoughts
 */
public class ThoughtNode {
    private String thought;
    private String context;
    private int importance; // 1-10
    private LocalDateTime timestamp;
    private String source; // "user_query", "brain_output", "system"
    
    /**
     * Constructor with basic parameters
     */
    public ThoughtNode(String thought, String context, int importance) {
        this.thought = thought;
        this.context = context;
        this.importance = Math.min(10, Math.max(1, importance));
        this.timestamp = LocalDateTime.now();
        this.source = "system";
    }
    
    /**
     * Constructor with source
     */
    public ThoughtNode(String thought, String context, int importance, String source) {
        this.thought = thought;
        this.context = context;
        this.importance = Math.min(10, Math.max(1, importance));
        this.timestamp = LocalDateTime.now();
        this.source = source != null ? source : "system";
    }
    
    // ===== Getters =====
    
    public String getThought() {
        return thought;
    }
    
    public String getContext() {
        return context;
    }
    
    public int getImportance() {
        return importance;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getSource() {
        return source;
    }
    
    // ===== Setters =====
    
    public void setThought(String thought) {
        this.thought = thought;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
    
    public void setImportance(int importance) {
        this.importance = Math.min(10, Math.max(1, importance));
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s (importance: %d, source: %s)", 
            timestamp, thought, importance, source);
    }
}

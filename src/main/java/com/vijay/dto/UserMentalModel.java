package com.vijay.dto;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 🧠 User Mental Model
 * 
 * Represents the inferred mental state of the user based on conversation patterns.
 * Includes knowledge level, emotional state, confusion indicators, and expertise areas.
 */
public class UserMentalModel {
    
    private String userId;
    private int knowledgeLevel;              // 1-5 scale (beginner to expert)
    private int confusionLevel;              // 0-100 scale
    private int frustrationLevel;            // 0-100 scale
    private Set<String> expertiseAreas;      // e.g., "frontend", "backend", "devops"
    private Set<String> knowledgeGaps;       // Topics user struggles with
    private String learningStyle;            // "visual", "textual", "code-heavy", "step-by-step"
    private int queryComplexity;             // Average complexity of user queries
    private LocalDateTime lastUpdated;
    private double confidence;               // How confident we are in this model
    
    // Constructor
    public UserMentalModel(String userId) {
        this.userId = userId;
        this.knowledgeLevel = 2; // Default: intermediate
        this.confusionLevel = 0;
        this.frustrationLevel = 0;
        this.expertiseAreas = new HashSet<>();
        this.knowledgeGaps = new HashSet<>();
        this.learningStyle = "balanced";
        this.queryComplexity = 3;
        this.lastUpdated = LocalDateTime.now();
        this.confidence = 0.5;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public int getKnowledgeLevel() {
        return knowledgeLevel;
    }
    
    public void setKnowledgeLevel(int knowledgeLevel) {
        this.knowledgeLevel = Math.max(1, Math.min(5, knowledgeLevel));
        this.lastUpdated = LocalDateTime.now();
    }
    
    public int getConfusionLevel() {
        return confusionLevel;
    }
    
    public void setConfusionLevel(int confusionLevel) {
        this.confusionLevel = Math.max(0, Math.min(100, confusionLevel));
        this.lastUpdated = LocalDateTime.now();
    }
    
    public int getFrustrationLevel() {
        return frustrationLevel;
    }
    
    public void setFrustrationLevel(int frustrationLevel) {
        this.frustrationLevel = Math.max(0, Math.min(100, frustrationLevel));
        this.lastUpdated = LocalDateTime.now();
    }
    
    public Set<String> getExpertiseAreas() {
        return new HashSet<>(expertiseAreas);
    }
    
    public void addExpertiseArea(String area) {
        this.expertiseAreas.add(area.toLowerCase());
        this.lastUpdated = LocalDateTime.now();
    }
    
    public void removeExpertiseArea(String area) {
        this.expertiseAreas.remove(area.toLowerCase());
        this.lastUpdated = LocalDateTime.now();
    }
    
    public Set<String> getKnowledgeGaps() {
        return new HashSet<>(knowledgeGaps);
    }
    
    public void addKnowledgeGap(String gap) {
        this.knowledgeGaps.add(gap.toLowerCase());
        this.lastUpdated = LocalDateTime.now();
    }
    
    public void removeKnowledgeGap(String gap) {
        this.knowledgeGaps.remove(gap.toLowerCase());
        this.lastUpdated = LocalDateTime.now();
    }
    
    public String getLearningStyle() {
        return learningStyle;
    }
    
    public void setLearningStyle(String learningStyle) {
        this.learningStyle = learningStyle;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public int getQueryComplexity() {
        return queryComplexity;
    }
    
    public void setQueryComplexity(int queryComplexity) {
        this.queryComplexity = Math.max(1, Math.min(5, queryComplexity));
        this.lastUpdated = LocalDateTime.now();
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(double confidence) {
        this.confidence = Math.max(0, Math.min(1, confidence));
    }
    
    /**
     * Check if user is confused
     */
    public boolean isConfused() {
        return confusionLevel > 60;
    }
    
    /**
     * Check if user is frustrated
     */
    public boolean isFrustrated() {
        return frustrationLevel > 60;
    }
    
    /**
     * Check if user is a beginner
     */
    public boolean isBeginner() {
        return knowledgeLevel <= 2;
    }
    
    /**
     * Check if user is an expert
     */
    public boolean isExpert() {
        return knowledgeLevel >= 4;
    }
    
    /**
     * Get mental state summary
     */
    public String getMentalStateSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Knowledge: ").append(knowledgeLevel).append("/5 | ");
        summary.append("Confusion: ").append(confusionLevel).append("% | ");
        summary.append("Frustration: ").append(frustrationLevel).append("% | ");
        summary.append("Style: ").append(learningStyle);
        
        if (!expertiseAreas.isEmpty()) {
            summary.append(" | Expertise: ").append(String.join(", ", expertiseAreas));
        }
        
        if (!knowledgeGaps.isEmpty()) {
            summary.append(" | Gaps: ").append(String.join(", ", knowledgeGaps));
        }
        
        return summary.toString();
    }
    
    @Override
    public String toString() {
        return "UserMentalModel{" +
                "userId='" + userId + '\'' +
                ", knowledgeLevel=" + knowledgeLevel +
                ", confusionLevel=" + confusionLevel +
                ", frustrationLevel=" + frustrationLevel +
                ", learningStyle='" + learningStyle + '\'' +
                ", confidence=" + String.format("%.2f", confidence) +
                '}';
    }
}

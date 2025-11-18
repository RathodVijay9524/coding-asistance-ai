package com.vijay.dto;

import java.time.LocalDateTime;

/**
 * 🧠 Emotional Context Model
 * 
 * Stores emotional information about a user's message and interaction.
 * Used to track and respond to user emotions appropriately.
 */
public class EmotionalContext {
    
    private EmotionalState currentState;
    private int emotionalIntensity;        // 0-100 scale
    private String triggerKeywords;        // What caused this emotion
    private String recommendedTone;        // How to respond
    private LocalDateTime detectedAt;
    private double confidence;             // 0-1 scale, how confident in detection
    
    // Constructor
    public EmotionalContext(EmotionalState state, int intensity, String keywords, 
                           String tone, double confidence) {
        this.currentState = state;
        this.emotionalIntensity = Math.max(0, Math.min(100, intensity));
        this.triggerKeywords = keywords;
        this.recommendedTone = tone;
        this.confidence = Math.max(0, Math.min(1, confidence));
        this.detectedAt = LocalDateTime.now();
    }
    
    // Default constructor
    public EmotionalContext() {
        this.currentState = EmotionalState.NEUTRAL;
        this.emotionalIntensity = 0;
        this.triggerKeywords = "";
        this.recommendedTone = "neutral";
        this.confidence = 0.5;
        this.detectedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public EmotionalState getCurrentState() {
        return currentState;
    }
    
    public void setCurrentState(EmotionalState currentState) {
        this.currentState = currentState;
    }
    
    public int getEmotionalIntensity() {
        return emotionalIntensity;
    }
    
    public void setEmotionalIntensity(int emotionalIntensity) {
        this.emotionalIntensity = Math.max(0, Math.min(100, emotionalIntensity));
    }
    
    public String getTriggerKeywords() {
        return triggerKeywords;
    }
    
    public void setTriggerKeywords(String triggerKeywords) {
        this.triggerKeywords = triggerKeywords;
    }
    
    public String getRecommendedTone() {
        return recommendedTone;
    }
    
    public void setRecommendedTone(String recommendedTone) {
        this.recommendedTone = recommendedTone;
    }
    
    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(double confidence) {
        this.confidence = Math.max(0, Math.min(1, confidence));
    }
    
    /**
     * Check if emotion is strong (intensity > 70)
     */
    public boolean isStrongEmotion() {
        return emotionalIntensity > 70;
    }
    
    /**
     * Check if we're confident in this detection
     */
    public boolean isConfidentDetection() {
        return confidence > 0.7;
    }
    
    /**
     * Get emoji representation of emotional state
     */
    public String getEmoji() {
        switch (currentState) {
            case POSITIVE:
                return "😊";
            case EXCITED:
                return "🤩";
            case CALM:
                return "😌";
            case NEUTRAL:
                return "😐";
            case NEGATIVE:
                return "😞";
            case FRUSTRATED:
                return "😤";
            case CONFUSED:
                return "😕";
            case URGENT:
                return "⚠️";
            default:
                return "❓";
        }
    }
    
    @Override
    public String toString() {
        return "EmotionalContext{" +
                "state=" + currentState +
                ", intensity=" + emotionalIntensity +
                ", confidence=" + String.format("%.2f", confidence) +
                ", tone='" + recommendedTone + '\'' +
                ", emoji=" + getEmoji() +
                '}';
    }
}

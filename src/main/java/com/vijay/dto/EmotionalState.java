package com.vijay.dto;

/**
 * 🧠 Emotional State Enum
 * 
 * Represents the emotional state of the user based on their message analysis.
 * Used by EmotionalContextAdvisor to detect and respond to user emotions.
 */
public enum EmotionalState {
    
    // Positive states
    POSITIVE("Positive", 1.2f),      // Happy, satisfied, excited
    EXCITED("Excited", 1.3f),         // Very enthusiastic
    CALM("Calm", 1.0f),               // Relaxed, peaceful
    
    // Neutral state
    NEUTRAL("Neutral", 1.0f),         // No strong emotion
    
    // Negative states
    NEGATIVE("Negative", 0.8f),       // Unhappy, dissatisfied
    FRUSTRATED("Frustrated", 0.7f),   // Annoyed, impatient
    CONFUSED("Confused", 0.9f),       // Lost, uncertain
    URGENT("Urgent", 1.1f),           // Time-sensitive, critical
    
    // Unknown state
    UNKNOWN("Unknown", 1.0f);         // Cannot determine emotion
    
    private final String displayName;
    private final float responseMultiplier; // How to adjust response (1.0 = normal)
    
    EmotionalState(String displayName, float responseMultiplier) {
        this.displayName = displayName;
        this.responseMultiplier = responseMultiplier;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public float getResponseMultiplier() {
        return responseMultiplier;
    }
    
    /**
     * Check if this is a positive emotional state
     */
    public boolean isPositive() {
        return this == POSITIVE || this == EXCITED || this == CALM;
    }
    
    /**
     * Check if this is a negative emotional state
     */
    public boolean isNegative() {
        return this == NEGATIVE || this == FRUSTRATED || this == CONFUSED;
    }
    
    /**
     * Check if this requires urgent attention
     */
    public boolean isUrgent() {
        return this == URGENT || this == FRUSTRATED;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}

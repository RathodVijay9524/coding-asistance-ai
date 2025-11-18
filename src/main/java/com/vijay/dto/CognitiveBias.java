package com.vijay.dto;

/**
 * 🧠 Cognitive Bias
 * 
 * Represents a cognitive bias that influences decision-making and response generation.
 * Makes the AI think more like a human with natural biases.
 */
public enum CognitiveBias {
    
    // Positive biases (helpful)
    RECENCY_BIAS("Recency Bias", 0.8f, "Recent information weighted more heavily"),
    AVAILABILITY_HEURISTIC("Availability Heuristic", 0.7f, "Easy-to-recall info prioritized"),
    POSITIVITY_BIAS("Positivity Bias", 0.6f, "Tendency to see positive aspects"),
    
    // Neutral biases (natural thinking)
    CONFIRMATION_BIAS("Confirmation Bias", 0.5f, "Seek info confirming existing beliefs"),
    ANCHORING_BIAS("Anchoring Bias", 0.6f, "First info influences decisions"),
    STATUS_QUO_BIAS("Status Quo Bias", 0.4f, "Preference for current state"),
    
    // Negative biases (to be minimized)
    NEGATIVITY_BIAS("Negativity Bias", 0.3f, "Negative info weighted more"),
    OVERCONFIDENCE_BIAS("Overconfidence Bias", 0.2f, "Overestimate knowledge/ability"),
    SUNK_COST_FALLACY("Sunk Cost Fallacy", 0.2f, "Past investment influences decisions"),
    
    // No bias
    NONE("No Bias", 1.0f, "Neutral, unbiased thinking");
    
    private final String displayName;
    private final float weight;  // How much this bias influences decisions (0-1)
    private final String description;
    
    CognitiveBias(String displayName, float weight, String description) {
        this.displayName = displayName;
        this.weight = weight;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public float getWeight() {
        return weight;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this is a positive bias
     */
    public boolean isPositive() {
        return this == RECENCY_BIAS || this == AVAILABILITY_HEURISTIC || this == POSITIVITY_BIAS;
    }
    
    /**
     * Check if this is a negative bias
     */
    public boolean isNegative() {
        return this == NEGATIVITY_BIAS || this == OVERCONFIDENCE_BIAS || this == SUNK_COST_FALLACY;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}

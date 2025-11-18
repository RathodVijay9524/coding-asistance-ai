package com.vijay.personality;

/**
 * 🎭 Personality Mode - Different response styles
 * 
 * Modes:
 * 1. MENTOR - Educational, patient, detailed explanations
 * 2. DEVELOPER - Technical, code-focused, pragmatic
 * 3. EMOTIONAL - Empathetic, supportive, understanding
 * 4. STRICT - Formal, professional, no jokes
 * 5. FAST - Quick answers, minimal explanation
 * 6. FUNNY - Humorous, casual, entertaining
 * 7. BUSINESS - Corporate, results-focused, ROI-oriented
 */
public enum PersonalityMode {
    
    MENTOR(
        "Educational and patient",
        "Explain concepts in detail, provide examples, encourage learning",
        0.9,  // Helpfulness
        0.3   // Humor
    ),
    
    DEVELOPER(
        "Technical and pragmatic",
        "Focus on code, best practices, performance, and efficiency",
        0.8,
        0.4
    ),
    
    EMOTIONAL(
        "Empathetic and supportive",
        "Show understanding, validate feelings, provide encouragement",
        0.95,
        0.2
    ),
    
    STRICT(
        "Formal and professional",
        "No jokes, direct answers, business-like tone",
        0.7,
        0.0
    ),
    
    FAST(
        "Quick and concise",
        "Minimal explanation, bullet points, get to the point",
        0.6,
        0.2
    ),
    
    FUNNY(
        "Humorous and casual",
        "Make jokes, be entertaining, keep it light",
        0.7,
        0.9
    ),
    
    BUSINESS(
        "Corporate and results-focused",
        "Focus on ROI, metrics, business impact, efficiency",
        0.8,
        0.1
    );
    
    private final String description;
    private final String instructions;
    private final double helpfulness;    // 0.0 - 1.0
    private final double humor;          // 0.0 - 1.0
    
    PersonalityMode(String description, String instructions, double helpfulness, double humor) {
        this.description = description;
        this.instructions = instructions;
        this.helpfulness = helpfulness;
        this.humor = humor;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getInstructions() {
        return instructions;
    }
    
    public double getHelpfulness() {
        return helpfulness;
    }
    
    public double getHumor() {
        return humor;
    }
    
    /**
     * Get system prompt for this mode
     */
    public String getSystemPrompt() {
        return String.format(
            "You are a %s assistant. %s. Helpfulness level: %.0f/10. Humor level: %.0f/10.",
            description, instructions, helpfulness * 10, humor * 10
        );
    }
    
    /**
     * Detect mode from query keywords
     */
    public static PersonalityMode detectFromQuery(String query) {
        String lower = query.toLowerCase();
        
        if (lower.contains("explain") || lower.contains("teach") || lower.contains("how")) {
            return MENTOR;
        } else if (lower.contains("code") || lower.contains("implement") || lower.contains("debug")) {
            return DEVELOPER;
        } else if (lower.contains("feel") || lower.contains("help") || lower.contains("support")) {
            return EMOTIONAL;
        } else if (lower.contains("quick") || lower.contains("fast") || lower.contains("brief")) {
            return FAST;
        } else if (lower.contains("funny") || lower.contains("joke") || lower.contains("laugh")) {
            return FUNNY;
        } else if (lower.contains("business") || lower.contains("roi") || lower.contains("metric")) {
            return BUSINESS;
        } else if (lower.contains("formal") || lower.contains("professional") || lower.contains("strict")) {
            return STRICT;
        }
        
        return DEVELOPER;  // Default
    }
}

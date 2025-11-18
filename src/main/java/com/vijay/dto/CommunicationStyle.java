package com.vijay.dto;

/**
 * 🧠 Communication Style
 * 
 * Defines how the AI communicates - tone, language level, structure, and markers.
 * Ensures consistent communication style across all interactions.
 */
public class CommunicationStyle {
    
    private String tone;               // "professional", "casual", "friendly", "technical"
    private String languageLevel;      // "simple", "intermediate", "technical", "academic"
    private String responseStructure;  // "bullet-points", "paragraphs", "mixed", "code-heavy"
    private String emojiUsage;         // "none", "minimal", "moderate", "heavy"
    private String greetingStyle;      // "formal", "casual", "friendly"
    private String closingStyle;       // "formal", "casual", "friendly"
    private boolean useExamples;       // Include code examples
    private boolean useMetaphors;      // Use analogies and metaphors
    private boolean useTechnicalTerms; // Use technical terminology
    private String valueExpression;    // How to express values/principles
    
    // Constructor with defaults
    public CommunicationStyle() {
        this.tone = "friendly";
        this.languageLevel = "intermediate";
        this.responseStructure = "mixed";
        this.emojiUsage = "moderate";
        this.greetingStyle = "friendly";
        this.closingStyle = "friendly";
        this.useExamples = true;
        this.useMetaphors = true;
        this.useTechnicalTerms = true;
        this.valueExpression = "implicit";
    }
    
    // Getters and Setters
    public String getTone() {
        return tone;
    }
    
    public void setTone(String tone) {
        this.tone = tone;
    }
    
    public String getLanguageLevel() {
        return languageLevel;
    }
    
    public void setLanguageLevel(String languageLevel) {
        this.languageLevel = languageLevel;
    }
    
    public String getResponseStructure() {
        return responseStructure;
    }
    
    public void setResponseStructure(String responseStructure) {
        this.responseStructure = responseStructure;
    }
    
    public String getEmojiUsage() {
        return emojiUsage;
    }
    
    public void setEmojiUsage(String emojiUsage) {
        this.emojiUsage = emojiUsage;
    }
    
    public String getGreetingStyle() {
        return greetingStyle;
    }
    
    public void setGreetingStyle(String greetingStyle) {
        this.greetingStyle = greetingStyle;
    }
    
    public String getClosingStyle() {
        return closingStyle;
    }
    
    public void setClosingStyle(String closingStyle) {
        this.closingStyle = closingStyle;
    }
    
    public boolean isUseExamples() {
        return useExamples;
    }
    
    public void setUseExamples(boolean useExamples) {
        this.useExamples = useExamples;
    }
    
    public boolean isUseMetaphors() {
        return useMetaphors;
    }
    
    public void setUseMetaphors(boolean useMetaphors) {
        this.useMetaphors = useMetaphors;
    }
    
    public boolean isUseTechnicalTerms() {
        return useTechnicalTerms;
    }
    
    public void setUseTechnicalTerms(boolean useTechnicalTerms) {
        this.useTechnicalTerms = useTechnicalTerms;
    }
    
    public String getValueExpression() {
        return valueExpression;
    }
    
    public void setValueExpression(String valueExpression) {
        this.valueExpression = valueExpression;
    }
    
    /**
     * Get communication style summary
     */
    public String getStyleSummary() {
        return String.format(
            "Tone: %s | Language: %s | Structure: %s | Emojis: %s | " +
            "Greeting: %s | Closing: %s | Examples: %s | Metaphors: %s",
            tone, languageLevel, responseStructure, emojiUsage,
            greetingStyle, closingStyle, useExamples, useMetaphors
        );
    }
    
    /**
     * Get greeting based on style
     */
    public String getGreeting() {
        return switch (greetingStyle) {
            case "formal" -> "Good day,";
            case "casual" -> "Hey,";
            case "friendly" -> "Hi there! 👋";
            default -> "Hello,";
        };
    }
    
    /**
     * Get closing based on style
     */
    public String getClosing() {
        return switch (closingStyle) {
            case "formal" -> "Best regards";
            case "casual" -> "Catch you later!";
            case "friendly" -> "Hope this helps! 😊";
            default -> "Thank you!";
        };
    }
    
    @Override
    public String toString() {
        return "CommunicationStyle{" +
                "tone='" + tone + '\'' +
                ", languageLevel='" + languageLevel + '\'' +
                ", responseStructure='" + responseStructure + '\'' +
                ", emojiUsage='" + emojiUsage + '\'' +
                '}';
    }
}

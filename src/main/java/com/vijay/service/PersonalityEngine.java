package com.vijay.service;

import com.vijay.dto.CommunicationStyle;
import com.vijay.dto.PersonalityTraits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 🧠 Personality Engine
 * 
 * Applies consistent personality traits and communication style to responses.
 * Ensures the AI maintains a cohesive, recognizable personality across all interactions.
 */
@Service
public class PersonalityEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(PersonalityEngine.class);
    
    private final PersonalityTraits traits;
    private final CommunicationStyle style;
    
    public PersonalityEngine() {
        this.traits = new PersonalityTraits();
        this.style = new CommunicationStyle();
        logger.info("🧠 Personality Engine initialized - Archetype: {}", traits.getArchetype());
    }
    
    /**
     * Apply personality to response
     */
    public String applyPersonality(String response) {
        logger.debug("🎭 Applying personality to response...");
        
        if (response == null || response.isEmpty()) {
            return response;
        }
        
        String personalizedResponse = response;
        
        // Apply tone markers
        personalizedResponse = applyToneMarkers(personalizedResponse);
        
        // Apply language level adjustments
        personalizedResponse = adjustLanguageLevel(personalizedResponse);
        
        // Apply emoji usage
        personalizedResponse = applyEmojiUsage(personalizedResponse);
        
        // Apply humor if appropriate
        if (traits.getHumor() >= 6) {
            personalizedResponse = addHumorMarkers(personalizedResponse);
        }
        
        // Apply enthusiasm markers
        personalizedResponse = applyEnthusiasm(personalizedResponse);
        
        logger.debug("✅ Personality applied successfully");
        return personalizedResponse;
    }
    
    /**
     * Apply tone markers based on personality
     */
    private String applyToneMarkers(String response) {
        if (traits.getFormality() <= 3) {
            // Casual tone - use contractions
            response = response.replaceAll("(?i)cannot", "can't");
            response = response.replaceAll("(?i)do not", "don't");
            response = response.replaceAll("(?i)will not", "won't");
        } else if (traits.getFormality() >= 8) {
            // Formal tone - avoid contractions
            response = response.replaceAll("(?i)can't", "cannot");
            response = response.replaceAll("(?i)don't", "do not");
            response = response.replaceAll("(?i)won't", "will not");
        }
        
        return response;
    }
    
    /**
     * Adjust language level
     */
    private String adjustLanguageLevel(String response) {
        if (style.getLanguageLevel().equals("simple")) {
            // Simplify complex words
            response = response.replaceAll("(?i)utilize", "use");
            response = response.replaceAll("(?i)implement", "create");
            response = response.replaceAll("(?i)facilitate", "help");
        } else if (style.getLanguageLevel().equals("technical")) {
            // Use more technical terms
            response = response.replaceAll("(?i)\\buse\\b", "utilize");
            response = response.replaceAll("(?i)\\bcreate\\b", "implement");
        }
        
        return response;
    }
    
    /**
     * Apply emoji usage based on personality
     */
    private String applyEmojiUsage(String response) {
        if (style.getEmojiUsage().equals("none")) {
            // Remove emojis
            response = response.replaceAll("[😊😞😤😕🤩😌⚠️✅❌🚀💪✨🔍📝👣🎯]", "");
        } else if (style.getEmojiUsage().equals("heavy")) {
            // Add more emojis
            if (response.contains("success") || response.contains("great")) {
                response = response.replaceAll("(?i)success", "success ✅");
                response = response.replaceAll("(?i)great", "great 🎉");
            }
        }
        
        return response;
    }
    
    /**
     * Add humor markers if appropriate
     */
    private String addHumorMarkers(String response) {
        if (traits.getHumor() >= 8) {
            // High humor - add witty comments
            if (response.contains("error")) {
                response = response.replaceAll("(?i)error", "error (oops! 😅)");
            }
        }
        
        return response;
    }
    
    /**
     * Apply enthusiasm markers
     */
    private String applyEnthusiasm(String response) {
        if (traits.getEnthusiasm() >= 8) {
            // High enthusiasm
            response = response.replaceAll("(?i)\\bgreat\\b", "amazing");
            response = response.replaceAll("(?i)\\bgood\\b", "excellent");
        } else if (traits.getEnthusiasm() <= 3) {
            // Low enthusiasm
            response = response.replaceAll("(?i)amazing", "good");
            response = response.replaceAll("(?i)excellent", "fine");
        }
        
        return response;
    }
    
    /**
     * Get personality-appropriate greeting
     */
    public String getGreeting() {
        return style.getGreeting();
    }
    
    /**
     * Get personality-appropriate closing
     */
    public String getClosing() {
        return style.getClosing();
    }
    
    /**
     * Check if personality is empathetic
     */
    public boolean isEmpathetic() {
        return traits.getEmpathy() >= 7;
    }
    
    /**
     * Check if personality is patient
     */
    public boolean isPatient() {
        return traits.getPatience() >= 7;
    }
    
    /**
     * Check if personality is helpful
     */
    public boolean isHelpful() {
        return traits.getHelpfulness() >= 7;
    }
    
    /**
     * Get personality summary
     */
    public String getPersonalitySummary() {
        return String.format(
            "🎭 Personality: %s | %s",
            traits.getArchetype(),
            traits.getPersonalitySummary()
        );
    }
    
    /**
     * Get communication style summary
     */
    public String getStyleSummary() {
        return String.format(
            "💬 Communication: %s",
            style.getStyleSummary()
        );
    }
    
    /**
     * Get traits
     */
    public PersonalityTraits getTraits() {
        return traits;
    }
    
    /**
     * Get communication style
     */
    public CommunicationStyle getStyle() {
        return style;
    }
}

package com.vijay.personality;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 🎭 Personality Engine v2 - Dynamic personality switching
 * 
 * Features:
 * ✅ 7 personality modes
 * ✅ Auto-detect from query
 * ✅ User preferences
 * ✅ Dynamic switching
 * ✅ Personality metrics
 * 
 * Benefits:
 * - More natural responses
 * - Better user experience
 * - Adaptive to context
 * - Consistent personality
 */
@Service
public class PersonalityEngineV2 {
    
    private static final Logger logger = LoggerFactory.getLogger(PersonalityEngineV2.class);
    
    // User personality preferences: userId -> PersonalityMode
    private final Map<String, PersonalityMode> userPreferences = new ConcurrentHashMap<>();
    
    // Default personality
    private PersonalityMode defaultMode = PersonalityMode.DEVELOPER;
    
    /**
     * Get personality for user and query
     */
    public PersonalityMode getPersonality(String userId, String query) {
        // Check user preference first
        PersonalityMode userMode = userPreferences.get(userId);
        if (userMode != null) {
            logger.info("🎭 Using user preference: {}", userMode);
            return userMode;
        }
        
        // Auto-detect from query
        PersonalityMode detectedMode = PersonalityMode.detectFromQuery(query);
        logger.info("🎭 Auto-detected personality: {}", detectedMode);
        
        return detectedMode;
    }
    
    /**
     * Set user personality preference
     */
    public void setUserPreference(String userId, PersonalityMode mode) {
        userPreferences.put(userId, mode);
        logger.info("🎭 Set personality preference for {}: {}", userId, mode);
    }
    
    /**
     * Get system prompt for personality
     */
    public String getSystemPrompt(String userId, String query) {
        PersonalityMode mode = getPersonality(userId, query);
        return mode.getSystemPrompt();
    }
    
    /**
     * Get personality metrics
     */
    public PersonalityMetrics getMetrics(String userId, String query) {
        PersonalityMode mode = getPersonality(userId, query);
        
        return new PersonalityMetrics(
            mode.name(),
            mode.getDescription(),
            mode.getHelpfulness(),
            mode.getHumor()
        );
    }
    
    /**
     * Get all available modes
     */
    public PersonalityMode[] getAllModes() {
        return PersonalityMode.values();
    }
    
    /**
     * Personality Metrics DTO
     */
    public static class PersonalityMetrics {
        public String mode;
        public String description;
        public double helpfulness;
        public double humor;
        
        public PersonalityMetrics(String mode, String description, double helpfulness, double humor) {
            this.mode = mode;
            this.description = description;
            this.helpfulness = helpfulness;
            this.humor = humor;
        }
        
        @Override
        public String toString() {
            return String.format(
                "PersonalityMetrics{mode=%s, helpfulness=%.1f/10, humor=%.1f/10}",
                mode, helpfulness * 10, humor * 10
            );
        }
    }
}

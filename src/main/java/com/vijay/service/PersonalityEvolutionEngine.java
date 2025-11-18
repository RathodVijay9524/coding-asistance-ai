package com.vijay.service;

import com.vijay.dto.PersonalityTraits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🧠 Personality Evolution Engine
 * 
 * Evolves personality traits over time based on user interactions and feedback.
 * Enables the AI to develop and adapt its personality dynamically.
 */
@Service
public class PersonalityEvolutionEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(PersonalityEvolutionEngine.class);
    
    private final Map<String, PersonalityTraits> evolvedTraits = new ConcurrentHashMap<>();
    private final Map<String, Integer> interactionCounts = new ConcurrentHashMap<>();
    private final Map<String, Double> userSatisfactionScores = new ConcurrentHashMap<>();
    
    private static final double EVOLUTION_RATE = 0.05; // 5% change per interaction
    private static final int EVOLUTION_THRESHOLD = 10; // Evolve after 10 interactions
    
    public PersonalityEvolutionEngine() {
        logger.info("🧠 Personality Evolution Engine initialized - Dynamic personality development");
    }
    
    /**
     * Get evolved personality traits for user
     */
    public PersonalityTraits getEvolvedTraits(String userId) {
        return evolvedTraits.computeIfAbsent(userId, k -> {
            logger.info("👤 Creating evolved personality traits for user: {}", userId);
            return new PersonalityTraits();
        });
    }
    
    /**
     * Record user feedback and evolve personality
     */
    public void recordFeedbackAndEvolve(String userId, int satisfactionScore, String feedbackType) {
        logger.debug("📊 Recording feedback for user: {} - Score: {}", userId, satisfactionScore);
        
        // Update satisfaction score
        int currentCount = interactionCounts.getOrDefault(userId, 0);
        double currentSatisfaction = userSatisfactionScores.getOrDefault(userId, 0.5);
        double newSatisfaction = (currentSatisfaction * currentCount + satisfactionScore / 5.0) / (currentCount + 1);
        
        userSatisfactionScores.put(userId, newSatisfaction);
        interactionCounts.put(userId, currentCount + 1);
        
        // Evolve personality if threshold reached
        if (currentCount > 0 && currentCount % EVOLUTION_THRESHOLD == 0) {
            evolvePersonality(userId, feedbackType, newSatisfaction);
        }
    }
    
    /**
     * Evolve personality based on feedback
     */
    private void evolvePersonality(String userId, String feedbackType, double satisfaction) {
        logger.info("🔄 Evolving personality for user: {} - Feedback: {}", userId, feedbackType);
        
        PersonalityTraits traits = getEvolvedTraits(userId);
        
        switch (feedbackType.toLowerCase()) {
            case "too_formal":
                // Decrease formality
                traits.setFormality((int) (traits.getFormality() * (1 - EVOLUTION_RATE)));
                logger.info("📉 Decreased formality - becoming more casual");
                break;
                
            case "too_casual":
                // Increase formality
                traits.setFormality((int) (traits.getFormality() * (1 + EVOLUTION_RATE)));
                logger.info("📈 Increased formality - becoming more professional");
                break;
                
            case "not_helpful":
                // Increase helpfulness
                traits.setHelpfulness((int) (traits.getHelpfulness() * (1 + EVOLUTION_RATE)));
                logger.info("📈 Increased helpfulness - being more supportive");
                break;
                
            case "too_verbose":
                // Decrease verbosity
                traits.setVerbosity((int) (traits.getVerbosity() * (1 - EVOLUTION_RATE)));
                logger.info("📉 Decreased verbosity - being more concise");
                break;
                
            case "too_concise":
                // Increase verbosity
                traits.setVerbosity((int) (traits.getVerbosity() * (1 + EVOLUTION_RATE)));
                logger.info("📈 Increased verbosity - providing more detail");
                break;
                
            case "not_empathetic":
                // Increase empathy
                traits.setEmpathy((int) (traits.getEmpathy() * (1 + EVOLUTION_RATE)));
                logger.info("❤️ Increased empathy - being more understanding");
                break;
                
            case "too_humorous":
                // Decrease humor
                traits.setHumor((int) (traits.getHumor() * (1 - EVOLUTION_RATE)));
                logger.info("😐 Decreased humor - being more serious");
                break;
                
            case "not_humorous":
                // Increase humor
                traits.setHumor((int) (traits.getHumor() * (1 + EVOLUTION_RATE)));
                logger.info("😄 Increased humor - being more witty");
                break;
                
            case "impatient":
                // Increase patience
                traits.setPatience((int) (traits.getPatience() * (1 + EVOLUTION_RATE)));
                logger.info("😌 Increased patience - being more tolerant");
                break;
                
            case "not_direct":
                // Increase directness
                traits.setDirectness((int) (traits.getDirectness() * (1 + EVOLUTION_RATE)));
                logger.info("➡️ Increased directness - being more straightforward");
                break;
                
            default:
                // General satisfaction-based evolution
                if (satisfaction > 0.8) {
                    // User is very satisfied - maintain current traits
                    logger.info("✅ High satisfaction - maintaining current personality");
                } else if (satisfaction < 0.5) {
                    // User is dissatisfied - increase helpfulness and empathy
                    traits.setHelpfulness((int) (traits.getHelpfulness() * (1 + EVOLUTION_RATE)));
                    traits.setEmpathy((int) (traits.getEmpathy() * (1 + EVOLUTION_RATE)));
                    logger.info("⚠️ Low satisfaction - increasing helpfulness and empathy");
                }
        }
        
        logger.info("🎭 Personality evolved - New archetype: {}", traits.getArchetype());
    }
    
    /**
     * Get personality evolution summary
     */
    public String getEvolutionSummary(String userId) {
        PersonalityTraits traits = getEvolvedTraits(userId);
        int interactionCount = interactionCounts.getOrDefault(userId, 0);
        double satisfaction = userSatisfactionScores.getOrDefault(userId, 0.5);
        
        return String.format(
            "👤 Personality Evolution: %s | Interactions: %d | Satisfaction: %.2f | Archetype: %s",
            traits.getPersonalitySummary(), interactionCount, satisfaction, traits.getArchetype()
        );
    }
    
    /**
     * Get personality change history
     */
    public String getPersonalityChangeHistory(String userId) {
        PersonalityTraits traits = getEvolvedTraits(userId);
        int interactionCount = interactionCounts.getOrDefault(userId, 0);
        
        return String.format(
            "📊 After %d interactions, personality has evolved to: %s",
            interactionCount, traits.getArchetype()
        );
    }
    
    /**
     * Reset personality to default
     */
    public void resetPersonality(String userId) {
        evolvedTraits.put(userId, new PersonalityTraits());
        interactionCounts.put(userId, 0);
        userSatisfactionScores.put(userId, 0.5);
        
        logger.info("🔄 Personality reset for user: {}", userId);
    }
    
    /**
     * Get user satisfaction score
     */
    public double getUserSatisfactionScore(String userId) {
        return userSatisfactionScores.getOrDefault(userId, 0.5);
    }
    
    /**
     * Get interaction count
     */
    public int getInteractionCount(String userId) {
        return interactionCounts.getOrDefault(userId, 0);
    }
}

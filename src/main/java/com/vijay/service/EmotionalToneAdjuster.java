package com.vijay.service;

import com.vijay.dto.EmotionalContext;
import com.vijay.dto.EmotionalState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 🧠 Emotional Tone Adjuster Service
 * 
 * Adjusts response tone and content based on detected emotional context.
 * Adds empathy, adjusts formality, and modifies response style.
 */
@Service
public class EmotionalToneAdjuster {
    
    private static final Logger logger = LoggerFactory.getLogger(EmotionalToneAdjuster.class);
    
    /**
     * Adjust response based on emotional context
     */
    public String adjustTone(String response, EmotionalContext emotionalContext) {
        logger.debug("🎨 Adjusting tone for emotional state: {}", emotionalContext.getCurrentState());
        
        if (response == null || response.isEmpty()) {
            logger.warn("⚠️ Empty response, returning as-is");
            return response;
        }
        
        String adjustedResponse = response;
        
        // Add empathy prefix if needed
        adjustedResponse = addEmpathyPrefix(adjustedResponse, emotionalContext);
        
        // Adjust formality
        adjustedResponse = adjustFormality(adjustedResponse, emotionalContext);
        
        // Add encouragement if needed
        adjustedResponse = addEncouragement(adjustedResponse, emotionalContext);
        
        // Add urgency markers if needed
        adjustedResponse = addUrgencyMarkers(adjustedResponse, emotionalContext);
        
        logger.debug("✅ Tone adjusted successfully");
        return adjustedResponse;
    }
    
    /**
     * Add empathy prefix based on emotional state
     */
    private String addEmpathyPrefix(String response, EmotionalContext context) {
        EmotionalState state = context.getCurrentState();
        String prefix = "";
        
        switch (state) {
            case FRUSTRATED:
                prefix = "I understand this is frustrating. ";
                break;
            case CONFUSED:
                prefix = "I can see why this might be confusing. Let me clarify: ";
                break;
            case NEGATIVE:
                prefix = "I'm sorry you're experiencing this issue. ";
                break;
            case URGENT:
                prefix = "I understand this is urgent. ";
                break;
            case EXCITED:
                prefix = "That's great! ";
                break;
            case POSITIVE:
                prefix = "I'm glad you're pleased! ";
                break;
            default:
                return response; // No prefix for neutral/calm
        }
        
        logger.debug("💬 Adding empathy prefix: {}", prefix);
        return prefix + response;
    }
    
    /**
     * Adjust formality based on emotional state
     */
    private String adjustFormality(String response, EmotionalContext context) {
        EmotionalState state = context.getCurrentState();
        
        // For frustrated/urgent: more direct and action-oriented
        if (state == EmotionalState.FRUSTRATED || state == EmotionalState.URGENT) {
            response = response.replaceAll("(?i)might be able to", "can");
            response = response.replaceAll("(?i)perhaps", "");
            response = response.replaceAll("(?i)possibly", "");
            logger.debug("📝 Adjusted to more direct tone");
        }
        
        // For confused: more detailed and explanatory
        if (state == EmotionalState.CONFUSED) {
            response = response.replaceAll("(?i)it's", "it is");
            response = response.replaceAll("(?i)don't", "do not");
            logger.debug("📝 Adjusted to more formal/detailed tone");
        }
        
        // For excited/positive: more casual and enthusiastic
        if (state == EmotionalState.EXCITED || state == EmotionalState.POSITIVE) {
            response = response.replaceAll("(?i)certainly", "definitely");
            response = response.replaceAll("(?i)affirmative", "yes");
            logger.debug("📝 Adjusted to more casual tone");
        }
        
        return response;
    }
    
    /**
     * Add encouragement markers if user is discouraged
     */
    private String addEncouragement(String response, EmotionalContext context) {
        EmotionalState state = context.getCurrentState();
        
        if (state == EmotionalState.NEGATIVE || state == EmotionalState.FRUSTRATED) {
            // Add encouragement at the end
            String encouragement = "\n\n💪 You've got this! Feel free to ask if you need more help.";
            response = response + encouragement;
            logger.debug("💪 Added encouragement");
        }
        
        if (state == EmotionalState.CONFUSED) {
            // Add reassurance
            String reassurance = "\n\n✨ This is a common question - you're not alone in finding this tricky!";
            response = response + reassurance;
            logger.debug("✨ Added reassurance");
        }
        
        return response;
    }
    
    /**
     * Add urgency markers if needed
     */
    private String addUrgencyMarkers(String response, EmotionalContext context) {
        if (context.getCurrentState() == EmotionalState.URGENT) {
            // Prioritize critical information
            String urgencyMarker = "\n\n🚨 **PRIORITY**: ";
            
            // Find the most important sentence and mark it
            String[] sentences = response.split("\\. ");
            if (sentences.length > 0) {
                String firstSentence = sentences[0];
                response = urgencyMarker + firstSentence + ". " + 
                          String.join(". ", java.util.Arrays.copyOfRange(sentences, 1, sentences.length));
                logger.debug("🚨 Added urgency marker");
            }
        }
        
        return response;
    }
    
    /**
     * Get response length adjustment based on emotional state
     */
    public int getRecommendedResponseLength(EmotionalContext context) {
        EmotionalState state = context.getCurrentState();
        
        switch (state) {
            case FRUSTRATED:
            case URGENT:
                return 200; // Shorter for frustrated users
            case CONFUSED:
                return 500; // Longer for confused users (more detail)
            case EXCITED:
                return 300; // Medium for excited users
            case POSITIVE:
                return 300; // Medium for positive users
            case CALM:
                return 400; // Normal length
            default:
                return 400; // Default length
        }
    }
    
    /**
     * Check if response should include examples
     */
    public boolean shouldIncludeExamples(EmotionalContext context) {
        // Include examples for confused users
        return context.getCurrentState() == EmotionalState.CONFUSED;
    }
    
    /**
     * Check if response should include step-by-step instructions
     */
    public boolean shouldIncludeStepByStep(EmotionalContext context) {
        // Include steps for frustrated/confused users
        EmotionalState state = context.getCurrentState();
        return state == EmotionalState.FRUSTRATED || state == EmotionalState.CONFUSED;
    }
    
    /**
     * Get emoji for emotional state
     */
    public String getEmotionEmoji(EmotionalContext context) {
        return context.getEmoji();
    }
    
    /**
     * Check if response needs simplification
     */
    public boolean shouldSimplify(EmotionalContext context) {
        // Simplify for confused users
        return context.getCurrentState() == EmotionalState.CONFUSED;
    }
}

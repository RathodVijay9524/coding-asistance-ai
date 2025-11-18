package com.vijay.service;

import com.vijay.dto.EmotionalContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 🧠 Emotional Response Adapter
 * 
 * Adapts AI responses based on emotional context.
 * Modifies response to be more empathetic, encouraging, or urgent as needed.
 */
@Service
public class EmotionalResponseAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(EmotionalResponseAdapter.class);
    
    private final EmotionalToneAdjuster toneAdjuster;
    
    public EmotionalResponseAdapter(EmotionalToneAdjuster toneAdjuster) {
        this.toneAdjuster = toneAdjuster;
    }
    
    /**
     * Adapt response based on emotional context
     */
    public String adaptResponse(String response, EmotionalContext emotionalContext) {
        logger.debug("🎨 Adapting response for emotional state: {}", emotionalContext.getCurrentState());
        
        if (response == null || response.isEmpty()) {
            return response;
        }
        
        // Apply tone adjustment
        String adaptedResponse = toneAdjuster.adjustTone(response, emotionalContext);
        
        // Adjust response length if needed
        int recommendedLength = toneAdjuster.getRecommendedResponseLength(emotionalContext);
        adaptedResponse = adjustResponseLength(adaptedResponse, recommendedLength);
        
        // Add examples if needed
        if (toneAdjuster.shouldIncludeExamples(emotionalContext)) {
            adaptedResponse = addExampleMarkers(adaptedResponse);
        }
        
        // Add step-by-step if needed
        if (toneAdjuster.shouldIncludeStepByStep(emotionalContext)) {
            adaptedResponse = addStepByStepMarkers(adaptedResponse);
        }
        
        // Simplify if needed
        if (toneAdjuster.shouldSimplify(emotionalContext)) {
            adaptedResponse = simplifyLanguage(adaptedResponse);
        }
        
        logger.debug("✅ Response adapted successfully");
        return adaptedResponse;
    }
    
    /**
     * Adjust response length if it exceeds recommended
     */
    private String adjustResponseLength(String response, int recommendedLength) {
        if (response.length() <= recommendedLength) {
            return response;
        }
        
        logger.debug("📏 Trimming response from {} to ~{} chars", response.length(), recommendedLength);
        
        // Find a good breaking point (end of sentence)
        int breakPoint = response.lastIndexOf(".", recommendedLength);
        if (breakPoint < recommendedLength * 0.7) {
            breakPoint = response.lastIndexOf(" ", recommendedLength);
        }
        
        if (breakPoint > 0) {
            return response.substring(0, breakPoint + 1) + "\n\n[Response truncated for brevity]";
        }
        
        return response;
    }
    
    /**
     * Add markers for examples in response
     */
    private String addExampleMarkers(String response) {
        // Mark code blocks as examples
        response = response.replaceAll("```", "📝 **Example:**\n```");
        
        // Mark bullet points as examples
        response = response.replaceAll("(?m)^- ", "📝 - ");
        
        logger.debug("📝 Added example markers");
        return response;
    }
    
    /**
     * Add step-by-step markers
     */
    private String addStepByStepMarkers(String response) {
        String[] lines = response.split("\n");
        StringBuilder result = new StringBuilder();
        int stepNumber = 1;
        
        for (String line : lines) {
            if (line.trim().startsWith("-") || line.trim().startsWith("•")) {
                result.append("**Step ").append(stepNumber).append(":** ").append(line).append("\n");
                stepNumber++;
            } else {
                result.append(line).append("\n");
            }
        }
        
        logger.debug("👣 Added step-by-step markers");
        return result.toString();
    }
    
    /**
     * Simplify language in response
     */
    private String simplifyLanguage(String response) {
        // Replace complex words with simpler alternatives
        response = response.replaceAll("(?i)utilize", "use");
        response = response.replaceAll("(?i)implement", "create");
        response = response.replaceAll("(?i)facilitate", "help");
        response = response.replaceAll("(?i)subsequently", "then");
        response = response.replaceAll("(?i)furthermore", "also");
        response = response.replaceAll("(?i)notwithstanding", "despite");
        response = response.replaceAll("(?i)aforementioned", "mentioned");
        response = response.replaceAll("(?i)consequently", "so");
        
        logger.debug("🔤 Simplified language");
        return response;
    }
    
    /**
     * Get emotional context summary for logging
     */
    public String getEmotionalSummary(EmotionalContext context) {
        return String.format(
            "%s State: %s | Intensity: %d | Confidence: %.2f | Tone: %s",
            context.getEmoji(),
            context.getCurrentState(),
            context.getEmotionalIntensity(),
            context.getConfidence(),
            context.getRecommendedTone()
        );
    }
}

package com.vijay.manager;

import com.vijay.dto.EmotionalContext;
import com.vijay.service.EmotionalAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

/**
 * 🧠 Brain 7: Emotional Context Advisor
 * 
 * Purpose: Detect and track user emotional state
 * 
 * Responsibilities:
 * - Analyze user message for emotional cues
 * - Detect emotional state (frustrated, confused, excited, etc.)
 * - Store emotional context for response adaptation
 * - Prepare emotional metadata for other advisors
 * 
 * Execution Order: 1 (Early, right after query planning)
 */
@Component
public class EmotionalContextAdvisor implements CallAdvisor, IAgentBrain {
    
    private static final Logger logger = LoggerFactory.getLogger(EmotionalContextAdvisor.class);
    
    private final EmotionalAnalyzer emotionalAnalyzer;
    
    public EmotionalContextAdvisor(EmotionalAnalyzer emotionalAnalyzer) {
        this.emotionalAnalyzer = emotionalAnalyzer;
    }
    
    @Override
    public String getName() {
        return "EmotionalContextAdvisor";
    }
    
    @Override
    public int getOrder() {
        return 1; // Execute early, right after LocalQueryPlanner
    }
    
    // ===== IAgentBrain Implementation =====
    @Override
    public String getBrainName() {
        return "emotionalContextAdvisor";  // ← Spring bean name (lowercase first letter)
    }
    
    @Override
    public String getBrainDescription() {
        return "Analyzes user emotions and sentiment, detects emotional state and prepares emotional context for response adaptation";
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("🧠 Brain 7 (Emotional Context): Analyzing user emotional state...");
        
        try {
            // Extract user message
            String userQuery = extractUserMessage(request);
            if (userQuery.isEmpty()) {
                logger.info("⚠️ Brain 7: Empty query, proceeding");
                return chain.nextCall(request);
            }
            
            // Analyze emotional context
            EmotionalContext emotionalContext = emotionalAnalyzer.analyzeEmotion(userQuery);
            
            // Log emotional analysis
            logEmotionalAnalysis(emotionalContext);
            
            logger.info("✅ Brain 7: Emotional context detected - State: {} | Intensity: {} | Confidence: {:.2f}",
                emotionalContext.getCurrentState(),
                emotionalContext.getEmotionalIntensity(),
                emotionalContext.getConfidence());
            
            // Continue to next advisor
            return chain.nextCall(request);
            
        } catch (Exception e) {
            logger.error("❌ Brain 7: Error analyzing emotional context - {}", e.getMessage(), e);
            // Continue chain even if emotion analysis fails
            return chain.nextCall(request);
        }
    }
    
    /**
     * Extract user message from request
     */
    private String extractUserMessage(ChatClientRequest request) {
        try {
            if (request.prompt() != null && request.prompt().getInstructions() != null) {
                StringBuilder query = new StringBuilder();
                for (var message : request.prompt().getInstructions()) {
                    if (message instanceof UserMessage) {
                        UserMessage userMsg = (UserMessage) message;
                        query.append(userMsg.getText()).append(" ");
                    }
                }
                return query.toString().trim();
            }
        } catch (Exception e) {
            logger.debug("Could not extract user message: {}", e.getMessage());
        }
        return "";
    }
    
    /**
     * Log detailed emotional analysis
     */
    private void logEmotionalAnalysis(EmotionalContext context) {
        logger.info("😊 Emotional Analysis Results:");
        logger.info("   {} State: {}", context.getEmoji(), context.getCurrentState());
        logger.info("   📊 Intensity: {}/100", context.getEmotionalIntensity());
        logger.info("   🎯 Confidence: {:.2f}", context.getConfidence());
        logger.info("   💬 Recommended Tone: {}", context.getRecommendedTone());
        
        if (!context.getTriggerKeywords().isEmpty()) {
            logger.info("   🔑 Trigger Keywords: {}", context.getTriggerKeywords());
        }
        
        if (context.isStrongEmotion()) {
            logger.info("   ⚡ STRONG EMOTION DETECTED - Response should be carefully adapted");
        }
        
        if (!context.isConfidentDetection()) {
            logger.info("   ⚠️ Low confidence in detection - Treat as neutral");
        }
    }
}

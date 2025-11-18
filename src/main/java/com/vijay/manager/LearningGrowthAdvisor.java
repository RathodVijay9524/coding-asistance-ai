package com.vijay.manager;

import com.vijay.service.EnhancedLearningSystem;
import com.vijay.service.UserPreferenceEvolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

/**
 * 🧠 Brain 12: Learning & Growth Advisor
 * 
 * Purpose: Continuous learning and optimization from interactions
 * 
 * Responsibilities:
 * - Track strategy effectiveness
 * - Learn from successful/failed interactions
 * - Evolve user preferences
 * - Optimize response strategies
 * - Improve over time based on feedback
 * 
 * Execution Order: 950 (Very late, after all processing)
 */
@Component
public class LearningGrowthAdvisor implements CallAdvisor, IAgentBrain {
    
    private static final Logger logger = LoggerFactory.getLogger(LearningGrowthAdvisor.class);
    
    private final EnhancedLearningSystem enhancedLearningSystem;
    private final UserPreferenceEvolution userPreferenceEvolution;
    
    public LearningGrowthAdvisor(EnhancedLearningSystem enhancedLearningSystem,
                                UserPreferenceEvolution userPreferenceEvolution) {
        this.enhancedLearningSystem = enhancedLearningSystem;
        this.userPreferenceEvolution = userPreferenceEvolution;
    }
    
    @Override
    public String getName() {
        return "LearningGrowthAdvisor";
    }
    
    @Override
    public int getOrder() {
        return 950; // Execute very late, after all other processing
    }
    
    // ===== IAgentBrain Implementation =====
    @Override
    public String getBrainName() {
        return "learningGrowthAdvisor";  // ← Spring bean name (lowercase first letter)
    }
    
    @Override
    public String getBrainDescription() {
        return "Learns from interactions and improves over time, adapts to user preferences and patterns, evolves strategies based on feedback";
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("🧠 Brain 12 (Learning & Growth): Recording learnings and optimizing...");
        
        try {
            // Extract user ID and query
            String userId = extractUserId(request);
            String userQuery = extractUserMessage(request);
            
            if (userQuery.isEmpty()) {
                logger.info("⚠️ Brain 12: Empty query, proceeding");
                return chain.nextCall(request);
            }
            
            // Get response from chain
            ChatClientResponse response = chain.nextCall(request);
            
            // Extract response text
            String responseText = response.chatResponse().getResult().getOutput().getText();
            
            // Record learning metrics
            recordLearningMetrics(userId, userQuery, responseText);
            
            // Evolve user preferences
            evolveUserPreferences(userId, userQuery);
            
            // Log learning and growth
            logLearningAndGrowth(userId);
            
            logger.info("✅ Brain 12: Learning recorded - {}", enhancedLearningSystem.getOverallStatistics());
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Brain 12: Error in learning and growth - {}", e.getMessage(), e);
            // Continue chain even if learning fails
            return chain.nextCall(request);
        }
    }
    
    /**
     * Record learning metrics
     */
    private void recordLearningMetrics(String userId, String userQuery, String responseText) {
        logger.debug("📊 Recording learning metrics for user: {}", userId);
        
        // Detect query type
        String queryType = detectQueryType(userQuery);
        
        // Detect strategy used
        String strategy = detectStrategy(responseText);
        
        // Estimate success (simple heuristic)
        boolean success = responseText.length() > 50 && !responseText.contains("error");
        
        // Calculate quality (simple heuristic)
        double quality = calculateQuality(responseText);
        
        // Default satisfaction (would be from user feedback in real system)
        double satisfaction = success ? 0.8 : 0.4;
        
        // Estimate response time (default)
        double responseTime = 500;
        
        // Record in learning system
        enhancedLearningSystem.recordStrategyOutcome(queryType, strategy, success, quality, satisfaction, responseTime);
    }
    
    /**
     * Evolve user preferences
     */
    private void evolveUserPreferences(String userId, String userQuery) {
        logger.debug("📈 Evolving user preferences for user: {}", userId);
        
        // Detect preferences from query
        if (userQuery.toLowerCase().contains("concise") || userQuery.toLowerCase().contains("short")) {
            userPreferenceEvolution.recordPreferenceFeedback(userId, "concise_responses", 0.9);
        }
        
        if (userQuery.toLowerCase().contains("detailed") || userQuery.toLowerCase().contains("explain")) {
            userPreferenceEvolution.recordPreferenceFeedback(userId, "detailed_responses", 0.9);
        }
        
        if (userQuery.toLowerCase().contains("example") || userQuery.toLowerCase().contains("code")) {
            userPreferenceEvolution.recordPreferenceFeedback(userId, "code_examples", 0.9);
        }
        
        if (userQuery.toLowerCase().contains("step") || userQuery.toLowerCase().contains("how")) {
            userPreferenceEvolution.recordPreferenceFeedback(userId, "step_by_step", 0.9);
        }
    }
    
    /**
     * Detect query type
     */
    private String detectQueryType(String query) {
        String lower = query.toLowerCase();
        
        if (lower.contains("debug") || lower.contains("error") || lower.contains("fix")) {
            return "debugging";
        } else if (lower.contains("explain") || lower.contains("how") || lower.contains("what")) {
            return "explanation";
        } else if (lower.contains("code") || lower.contains("implement")) {
            return "code_generation";
        } else if (lower.contains("optimize") || lower.contains("improve")) {
            return "optimization";
        } else {
            return "general";
        }
    }
    
    /**
     * Detect strategy used
     */
    private String detectStrategy(String response) {
        if (response.length() < 200) {
            return "concise";
        } else if (response.length() > 1000) {
            return "detailed";
        } else if (response.contains("```")) {
            return "code_heavy";
        } else if (response.contains("Step") || response.contains("1.") || response.contains("2.")) {
            return "step_by_step";
        } else {
            return "balanced";
        }
    }
    
    /**
     * Calculate quality score
     */
    private double calculateQuality(String response) {
        double score = 0.5;
        
        if (response.length() > 100) score += 0.1;
        if (response.length() > 500) score += 0.1;
        if (response.contains("```")) score += 0.1;
        if (response.contains("\n")) score += 0.1;
        if (!response.contains("error")) score += 0.1;
        
        return Math.min(score, 1.0);
    }
    
    /**
     * Extract user ID from request
     */
    private String extractUserId(ChatClientRequest request) {
        try {
            if (request.prompt() != null && request.prompt().getInstructions() != null) {
                for (var message : request.prompt().getInstructions()) {
                    if (message instanceof UserMessage) {
                        UserMessage userMsg = (UserMessage) message;
                        String text = userMsg.getText();
                        if (text.contains("user_id:")) {
                            return text.substring(text.indexOf("user_id:") + 8).split(" ")[0];
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Could not extract user ID: {}", e.getMessage());
        }
        return "default_user";
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
     * Log learning and growth details
     */
    private void logLearningAndGrowth(String userId) {
        logger.info("🧠 Learning & Growth Profile:");
        logger.info("   📊 {}", enhancedLearningSystem.getOverallStatistics());
        logger.info("   👤 {}", userPreferenceEvolution.getEvolutionSummary(userId));
        
        String topPreference = userPreferenceEvolution.predictNextPreference(userId);
        if (!topPreference.equals("unknown")) {
            logger.info("   🎯 Predicted next preference: {}", topPreference);
        }
    }
}

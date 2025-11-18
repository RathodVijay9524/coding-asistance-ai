package com.vijay.manager;

import com.vijay.service.LearningMetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.stereotype.Component;

/**
 * 🧠 Brain 4: Learning System Advisor
 * 
 * Tracks query patterns, response quality, and learns from interactions
 * to improve future query handling and strategy selection.
 * 
 * Execution Order: 7 (After most processing, before final evaluation)
 */
@Component
public class LearningSystemAdvisor implements CallAdvisor {
    
    private static final Logger logger = LoggerFactory.getLogger(LearningSystemAdvisor.class);
    
    private final LearningMetricsService learningMetricsService;
    
    public LearningSystemAdvisor(LearningMetricsService learningMetricsService) {
        this.learningMetricsService = learningMetricsService;
    }
    
    @Override
    public String getName() {
        return "LearningSystemAdvisor";
    }
    
    @Override
    public int getOrder() {
        return 7; // Execute after most processing, before final evaluation
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("📚 Brain 4 (Learning System): Starting pattern analysis and learning...");
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Extract query information
            String userQuery = extractUserQuery(request);
            String queryType = extractQueryType(request);
            String strategy = extractStrategy(request);
            
            logger.info("📚 Brain 4: Analyzing - Type: {}, Strategy: {}", queryType, strategy);
            
            // Call the chain to get response
            ChatClientResponse response = chain.nextCall(request);
            
            long responseTimeMs = System.currentTimeMillis() - startTime;
            
            // Record the interaction for learning
            try {
                recordInteractionMetrics(response, queryType, strategy, responseTimeMs);
            } catch (Exception e) {
                logger.debug("Could not record metrics: {}", e.getMessage());
            }
            
            // Analyze patterns periodically
            try {
                analyzePatterns();
            } catch (Exception e) {
                logger.debug("Could not analyze patterns: {}", e.getMessage());
            }
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Brain 4: Error in learning system - {}", e.getMessage());
            return chain.nextCall(request);
        }
    }
    
    /**
     * Record metrics about the interaction
     */
    private void recordInteractionMetrics(ChatClientResponse response, String queryType, 
                                         String strategy, long responseTimeMs) {
        try {
            // Extract response quality indicators
            String responseText = response.chatResponse().getResult().getOutput().getText();
            double responseQuality = calculateResponseQuality(responseText);
            
            // Record success
            learningMetricsService.recordQuerySuccess(queryType, strategy, responseQuality, responseTimeMs);
            
            logger.info("📊 Brain 4: Metrics recorded - Quality: {:.2f}/5.0, Time: {}ms", 
                responseQuality, responseTimeMs);
            
        } catch (Exception e) {
            logger.debug("Failed to record metrics: {}", e.getMessage());
        }
    }
    
    /**
     * Calculate response quality based on content characteristics
     */
    private double calculateResponseQuality(String responseText) {
        double quality = 2.5; // Base score
        
        // Length indicates effort (but not too long)
        if (responseText.length() > 200 && responseText.length() < 3000) {
            quality += 1.0;
        } else if (responseText.length() > 100) {
            quality += 0.5;
        }
        
        // Code examples indicate technical depth
        if (responseText.contains("```") || responseText.contains("code")) {
            quality += 0.8;
        }
        
        // Structure indicators
        if (responseText.contains("\n") && responseText.split("\n").length > 5) {
            quality += 0.5;
        }
        
        // Clarity indicators
        if (responseText.contains("1.") || responseText.contains("•") || responseText.contains("-")) {
            quality += 0.3;
        }
        
        return Math.min(5.0, quality);
    }
    
    /**
     * Analyze detected patterns and log insights
     */
    private void analyzePatterns() {
        LearningMetricsService.LearningInsights insights = learningMetricsService.getInsights();
        
        if (insights.totalQueries > 0) {
            logger.info("📈 Brain 4: Learning Insights:");
            logger.info("   📊 Total Queries Analyzed: {}", insights.totalQueries);
            logger.info("   ✅ Overall Success Rate: {:.1f}%", insights.overallSuccessRate * 100);
            logger.info("   ⭐ Average Quality: {:.2f}/5.0", insights.averageQuality);
            logger.info("   🏆 Best Strategy: {}", insights.bestPerformingStrategy);
            
            // Log detected patterns
            if (!insights.detectedPatterns.isEmpty()) {
                logger.info("   🔍 Detected {} patterns:", insights.detectedPatterns.size());
                for (LearningMetricsService.QueryPattern pattern : insights.detectedPatterns) {
                    logger.info("      - {}: {} queries, {:.1f}% success, recommend: {}", 
                        pattern.patternName,
                        pattern.frequency,
                        pattern.successRate * 100,
                        pattern.recommendedStrategy);
                }
            }
        }
    }
    
    /**
     * Extract query type from request
     */
    private String extractQueryType(ChatClientRequest request) {
        try {
            // Try to get from request metadata if available
            String userQuery = extractUserQuery(request);
            
            // Simple classification
            String lower = userQuery.toLowerCase();
            if (lower.contains("how") || lower.contains("explain") || lower.contains("architecture")) {
                return "ARCHITECTURE";
            } else if (lower.contains("show") || lower.contains("example") || lower.contains("code")) {
                return "CODE";
            } else if (lower.contains("error") || lower.contains("bug") || lower.contains("fix")) {
                return "DEBUGGING";
            } else if (lower.contains("what") || lower.contains("define") || lower.contains("mean")) {
                return "DEFINITION";
            } else {
                return "GENERAL";
            }
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
    
    /**
     * Extract search strategy from request if available
     */
    private String extractStrategy(ChatClientRequest request) {
        try {
            // Default strategies based on query type
            String queryType = extractQueryType(request);
            
            // Get best strategy from learning metrics
            String bestStrategy = learningMetricsService.getBestStrategy(queryType);
            if (!bestStrategy.equals("similarity_search")) {
                return bestStrategy;
            }
            
            // Fallback to default
            return switch (queryType) {
                case "ARCHITECTURE" -> "dependency_graph";
                case "CODE" -> "method_focused";
                case "DEBUGGING" -> "error_trace";
                case "DEFINITION" -> "entity_centered";
                default -> "similarity_search";
            };
        } catch (Exception e) {
            return "similarity_search";
        }
    }
    
    /**
     * Extract user query from request
     */
    private String extractUserQuery(ChatClientRequest request) {
        try {
            if (request.prompt() != null && request.prompt().getInstructions() != null) {
                StringBuilder query = new StringBuilder();
                for (var message : request.prompt().getInstructions()) {
                    if (message instanceof org.springframework.ai.chat.messages.UserMessage) {
                        org.springframework.ai.chat.messages.UserMessage userMsg = 
                            (org.springframework.ai.chat.messages.UserMessage) message;
                        query.append(userMsg.getText()).append(" ");
                    }
                }
                return query.toString().trim();
            }
        } catch (Exception e) {
            logger.debug("Failed to extract user query: {}", e.getMessage());
        }
        return "";
    }
}

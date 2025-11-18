package com.vijay.manager;

import com.vijay.dto.UserMentalModel;
import com.vijay.service.MentalStateInferencer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

/**
 * 🧠 Brain 8: Theory of Mind Advisor
 * 
 * Purpose: Build and maintain mental model of user
 * 
 * Responsibilities:
 * - Infer user's knowledge level
 * - Detect confusion and frustration patterns
 * - Identify expertise areas
 * - Understand learning style preferences
 * - Predict user needs based on mental state
 * 
 * Execution Order: 3 (Early, after emotional context, to understand user's mind)
 */
@Component
public class TheoryOfMindAdvisor implements CallAdvisor, IAgentBrain {
    
    private static final Logger logger = LoggerFactory.getLogger(TheoryOfMindAdvisor.class);
    
    private final MentalStateInferencer mentalStateInferencer;
    
    public TheoryOfMindAdvisor(MentalStateInferencer mentalStateInferencer) {
        this.mentalStateInferencer = mentalStateInferencer;
    }
    
    @Override
    public String getName() {
        return "TheoryOfMindAdvisor";
    }
    
    @Override
    public int getOrder() {
        return 3; // Execute early, after emotional context
    }
    
    // ===== IAgentBrain Implementation =====
    @Override
    public String getBrainName() {
        return "theoryOfMindAdvisor";  // ← Spring bean name (lowercase first letter)
    }
    
    @Override
    public String getBrainDescription() {
        return "Infers user mental state and knowledge level, detects confusion and expertise areas, predicts user needs and learning style";
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("🧠 Brain 8 (Theory of Mind): Building mental model of user...");
        
        try {
            // Extract user ID and query
            String userId = extractUserId(request);
            String userQuery = extractUserMessage(request);
            
            if (userQuery.isEmpty()) {
                logger.info("⚠️ Brain 8: Empty query, proceeding");
                return chain.nextCall(request);
            }
            
            // Infer mental state from query
            mentalStateInferencer.inferFromQuery(userId, userQuery);
            
            // Get mental model
            UserMentalModel mentalModel = mentalStateInferencer.getMentalModel(userId);
            
            // Log mental model analysis
            logMentalModelAnalysis(mentalModel);
            
            // Augment request with mental model guidance
            ChatClientRequest augmentedRequest = augmentRequestWithMentalModel(request, mentalModel);
            
            logger.info("✅ Brain 8: Mental model updated - {}", mentalModel.getMentalStateSummary());
            
            // Continue to next advisor with augmented request
            return chain.nextCall(augmentedRequest);
            
        } catch (Exception e) {
            logger.error("❌ Brain 8: Error in theory of mind - {}", e.getMessage(), e);
            // Continue chain even if mental model inference fails
            return chain.nextCall(request);
        }
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
     * Augment request with mental model guidance
     */
    private ChatClientRequest augmentRequestWithMentalModel(ChatClientRequest request, UserMentalModel mentalModel) {
        StringBuilder guidance = new StringBuilder();
        
        // Add knowledge level guidance
        guidance.append(getKnowledgeLevelGuidance(mentalModel));
        
        // Add confusion handling
        if (mentalModel.isConfused()) {
            guidance.append("\n\n⚠️ User appears confused. Provide clear, step-by-step explanations.");
        }
        
        // Add frustration handling
        if (mentalModel.isFrustrated()) {
            guidance.append("\n\n💪 User appears frustrated. Be empathetic and supportive.");
        }
        
        // Add learning style guidance
        guidance.append("\n\nUser's preferred learning style: ").append(mentalModel.getLearningStyle());
        
        // Add expertise context
        if (!mentalModel.getExpertiseAreas().isEmpty()) {
            guidance.append("\nUser's expertise areas: ").append(String.join(", ", mentalModel.getExpertiseAreas()));
        }
        
        // Add knowledge gaps
        if (!mentalModel.getKnowledgeGaps().isEmpty()) {
            guidance.append("\nUser's knowledge gaps: ").append(String.join(", ", mentalModel.getKnowledgeGaps()));
        }
        
        // Augment the request
        String userQuery = extractUserMessage(request);
        String augmentedQuery = guidance.toString() + "\n\nUser Query: " + userQuery;
        
        return request.mutate()
            .prompt(request.prompt().augmentUserMessage(augmentedQuery))
            .build();
    }
    
    /**
     * Get knowledge level guidance
     */
    private String getKnowledgeLevelGuidance(UserMentalModel mentalModel) {
        return switch (mentalModel.getKnowledgeLevel()) {
            case 1 -> "🟢 User is a COMPLETE BEGINNER. Use very simple language and basic examples.";
            case 2 -> "🟡 User is a BEGINNER. Explain concepts clearly with examples.";
            case 3 -> "🟠 User is INTERMEDIATE. Balance depth with clarity. Include some advanced concepts.";
            case 4 -> "🔴 User is ADVANCED. Assume good foundational knowledge. Focus on details and edge cases.";
            case 5 -> "⚫ User is an EXPERT. Provide in-depth technical analysis. Discuss trade-offs and optimizations.";
            default -> "User expertise level unknown. Provide balanced explanation.";
        };
    }
    
    /**
     * Log detailed mental model analysis
     */
    private void logMentalModelAnalysis(UserMentalModel mentalModel) {
        logger.info("🧠 Mental Model Analysis:");
        logger.info("   📚 Knowledge Level: {}/5", mentalModel.getKnowledgeLevel());
        logger.info("   😕 Confusion Level: {}%", mentalModel.getConfusionLevel());
        logger.info("   😤 Frustration Level: {}%", mentalModel.getFrustrationLevel());
        logger.info("   🎓 Learning Style: {}", mentalModel.getLearningStyle());
        logger.info("   📊 Model Confidence: {:.2f}", mentalModel.getConfidence());
        
        if (!mentalModel.getExpertiseAreas().isEmpty()) {
            logger.info("   ⭐ Expertise Areas: {}", String.join(", ", mentalModel.getExpertiseAreas()));
        }
        
        if (!mentalModel.getKnowledgeGaps().isEmpty()) {
            logger.info("   ❓ Knowledge Gaps: {}", String.join(", ", mentalModel.getKnowledgeGaps()));
        }
        
        if (mentalModel.isConfused()) {
            logger.info("   ⚠️ USER IS CONFUSED - Needs clear, detailed explanations");
        }
        
        if (mentalModel.isFrustrated()) {
            logger.info("   ⚠️ USER IS FRUSTRATED - Needs empathetic, supportive response");
        }
    }
}

package com.vijay.manager;

import com.vijay.dto.ResponseScenario;
import com.vijay.service.MentalSimulator;
import com.vijay.service.PersonalityEvolutionEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 🧠 Brain 11: Advanced Capabilities Advisor
 * 
 * Purpose: Orchestrate advanced mental simulation and personality evolution
 * 
 * Responsibilities:
 * - Simulate multiple response scenarios
 * - Evaluate and select best response
 * - Predict user reactions
 * - Evolve personality based on feedback
 * - Integrate mental simulation with personality evolution
 * 
 * Execution Order: 900 (Very late, after all other processing)
 */
@Component
public class AdvancedCapabilitiesAdvisor implements CallAdvisor, IAgentBrain {
    
    private static final Logger logger = LoggerFactory.getLogger(AdvancedCapabilitiesAdvisor.class);
    
    private final MentalSimulator mentalSimulator;
    private final PersonalityEvolutionEngine personalityEvolutionEngine;
    
    public AdvancedCapabilitiesAdvisor(MentalSimulator mentalSimulator,
                                      PersonalityEvolutionEngine personalityEvolutionEngine) {
        this.mentalSimulator = mentalSimulator;
        this.personalityEvolutionEngine = personalityEvolutionEngine;
    }
    
    @Override
    public String getName() {
        return "AdvancedCapabilitiesAdvisor";
    }
    
    @Override
    public int getOrder() {
        return 900; // Execute very late, after all other processing
    }
    
    // ===== IAgentBrain Implementation =====
    @Override
    public String getBrainName() {
        return "advancedCapabilitiesAdvisor";  // ← Spring bean name (lowercase first letter)
    }
    
    @Override
    public String getBrainDescription() {
        return "Handles complex reasoning and multi-step problem solving, simulates multiple scenarios, evaluates and selects best responses";
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("🧠 Brain 11 (Advanced Capabilities): Simulating scenarios and evolving personality...");
        
        try {
            // Extract user ID and query
            String userId = extractUserId(request);
            String userQuery = extractUserMessage(request);
            
            if (userQuery.isEmpty()) {
                logger.info("⚠️ Brain 11: Empty query, proceeding");
                return chain.nextCall(request);
            }
            
            // Get response from chain
            ChatClientResponse response = chain.nextCall(request);
            
            // Extract response text
            String responseText = response.chatResponse().getResult().getOutput().getText();
            
            // Simulate multiple scenarios
            List<ResponseScenario> scenarios = mentalSimulator.simulateScenarios(userQuery, responseText);
            
            // Evaluate and select best scenario
            ResponseScenario bestScenario = mentalSimulator.evaluateAndSelectBest(scenarios);
            
            // Predict user reaction
            String userReaction = mentalSimulator.predictUserReaction(bestScenario);
            
            // Log advanced capabilities
            logAdvancedCapabilities(userId, scenarios, bestScenario, userReaction);
            
            logger.info("✅ Brain 11: Advanced capabilities applied - Best scenario: {}", bestScenario.getTone());
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Brain 11: Error in advanced capabilities - {}", e.getMessage(), e);
            // Continue chain even if advanced capabilities fail
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
     * Log advanced capabilities details
     */
    private void logAdvancedCapabilities(String userId, List<ResponseScenario> scenarios, 
                                        ResponseScenario bestScenario, String userReaction) {
        logger.info("🧠 Advanced Capabilities Profile:");
        logger.info("   🎭 Mental Simulation: {} scenarios evaluated", scenarios.size());
        logger.info("   🏆 Best Scenario: {} (Score: {:.2f})", bestScenario.getTone(), bestScenario.getOverallScore());
        logger.info("   👤 Predicted Reaction: {}", userReaction);
        
        logger.debug("📊 Scenario Comparison:");
        scenarios.forEach(scenario -> {
            logger.debug("   - {}: {:.2f}", scenario.getTone(), scenario.getOverallScore());
        });
        
        // Log personality evolution info
        logger.info("   🔄 Personality Evolution: {}", personalityEvolutionEngine.getEvolutionSummary(userId));
    }
}

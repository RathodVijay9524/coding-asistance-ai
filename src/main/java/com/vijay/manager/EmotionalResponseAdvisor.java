package com.vijay.manager;

import com.vijay.dto.EmotionalContext;
import com.vijay.service.EmotionalMemoryStore;
import com.vijay.service.EmotionalResponseAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

/**
 * 🧠 Emotional Response Advisor
 * 
 * Purpose: Apply emotional tone adjustments to responses
 * 
 * Responsibilities:
 * - Retrieve emotional context from earlier advisors
 * - Adapt response based on emotional state
 * - Store emotional context in memory
 * - Ensure empathetic and appropriate responses
 * 
 * Execution Order: 750 (Late, after response generation but before final evaluation)
 */
@Component
public class EmotionalResponseAdvisor implements CallAdvisor, IAgentBrain {
    
    private static final Logger logger = LoggerFactory.getLogger(EmotionalResponseAdvisor.class);
    
    private final EmotionalResponseAdapter responseAdapter;
    private final EmotionalMemoryStore emotionalMemoryStore;
    
    public EmotionalResponseAdvisor(EmotionalResponseAdapter responseAdapter,
                                    EmotionalMemoryStore emotionalMemoryStore) {
        this.responseAdapter = responseAdapter;
        this.emotionalMemoryStore = emotionalMemoryStore;
    }
    
    @Override
    public String getName() {
        return "EmotionalResponseAdvisor";
    }
    
    @Override
    public int getOrder() {
        return 750; // Execute late, after response generation
    }
    
    // ===== IAgentBrain Implementation =====
    @Override
    public String getBrainName() {
        return "emotionalResponseAdvisor";  // ← Spring bean name (lowercase first letter)
    }
    
    @Override
    public String getBrainDescription() {
        return "Generates emotionally appropriate responses, adds empathy and human touch, adapts tone based on user emotional state";
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("🧠 Emotional Response Advisor: Processing response with emotional awareness...");
        
        try {
            // Get response from chain
            ChatClientResponse response = chain.nextCall(request);
            
            // Extract user ID
            String userId = extractUserId(request);
            
            // Try to get emotional context from request (if available from EmotionalContextAdvisor)
            EmotionalContext emotionalContext = getEmotionalContextFromRequest(request);
            
            if (emotionalContext != null && !emotionalContext.getCurrentState().toString().equals("NEUTRAL")) {
                logger.info("😊 Applying emotional tone adjustment...");
                
                // Get response text
                String responseText = response.chatResponse().getResult().getOutput().getText();
                
                // Adapt response based on emotional context
                String adaptedResponse = responseAdapter.adaptResponse(responseText, emotionalContext);
                
                // Store emotional context for future reference
                emotionalMemoryStore.storeEmotionalContext(userId, emotionalContext);
                
                logger.info("✅ Emotional Response: {}", responseAdapter.getEmotionalSummary(emotionalContext));
                logger.debug("📝 Original length: {}, Adapted length: {}", responseText.length(), adaptedResponse.length());
                
            } else {
                logger.debug("😐 Neutral emotional state, no tone adjustment needed");
            }
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error in emotional response advisor: {}", e.getMessage(), e);
            // Continue chain even if emotion adaptation fails
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
                        // Extract user ID from message metadata if available
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
     * Get emotional context from request (if stored by EmotionalContextAdvisor)
     */
    private EmotionalContext getEmotionalContextFromRequest(ChatClientRequest request) {
        try {
            // In a real implementation, this would retrieve from request context
            // For now, return null to indicate no emotional context available
            // This will be enhanced when we implement request-scoped storage
            return null;
        } catch (Exception e) {
            logger.debug("Could not retrieve emotional context: {}", e.getMessage());
            return null;
        }
    }
}

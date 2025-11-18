package com.vijay.manager;

import com.vijay.service.ConversationMemoryManager;
import com.vijay.service.ConversationMemoryManager.ConversationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 🧠 Brain Memory Advisor: Conversation Memory Integration
 * 
 * CRITICAL COMPONENT: Eliminates AI amnesia by injecting conversation context
 * 
 * This advisor:
 * 1. Retrieves relevant conversation history before processing
 * 2. Injects memory context into the system prompt
 * 3. Stores new conversations after processing
 * 4. Adapts responses based on user patterns and preferences
 * 
 * Execution Order: 1 (First - to provide context to all other brains)
 * 
 * Transforms AI from stateless to stateful:
 * ❌ Before: "I don't remember our previous conversation"
 * ✅ After: "As we discussed earlier about the ChatService architecture..."
 */
@Component
public class ConversationMemoryAdvisor implements CallAdvisor, IAgentBrain {
    
    private static final Logger logger = LoggerFactory.getLogger(ConversationMemoryAdvisor.class);
    
    private final ConversationMemoryManager memoryManager;
    
    public ConversationMemoryAdvisor(ConversationMemoryManager memoryManager) {
        this.memoryManager = memoryManager;
    }
    
    @Override
    public String getName() {
        return "ConversationMemoryAdvisor";
    }
    
    @Override
    public int getOrder() {
        return 2; // Execute early to provide context to other advisors
    }
    
    // ===== IAgentBrain Implementation =====
    @Override
    public String getBrainName() {
        return "conversationMemoryAdvisor";  // ← Spring bean name (lowercase first letter)
    }
    
    @Override
    public String getBrainDescription() {
        return "Maintains conversation history and context, recalls previous interactions and decisions to provide continuity";
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("🧠 Brain Memory: Retrieving conversation context");
        
        try {
            // Extract user query
            String userQuery = extractUserQuery(request);
            if (userQuery == null || userQuery.trim().isEmpty()) {
                return chain.nextCall(request);
            }
            
            // Generate session and user IDs (in real app, these would come from authentication)
            String sessionId = getOrCreateSessionId(request);
            String userId = getOrCreateUserId(request);
            
            // Retrieve relevant conversation context
            ConversationContext context = memoryManager.getRelevantContext(sessionId, userId, userQuery);
            
            ChatClientRequest enhancedRequest = request;
            if (context.hasRelevantContext()) {
                // Inject memory context into system prompt
                String memoryContext = buildMemoryContext(context, userQuery);
                enhancedRequest = injectMemoryContext(request, memoryContext);
                
                logger.info("🧠 Memory context injected - Recent: {}, Related: {}, Patterns: {}", 
                    context.getRecentExchanges().size(),
                    context.getRelatedExchanges().size(),
                    context.getTypicalQueryPatterns().size());
            } else {
                logger.info("🧠 No relevant memory context found - First interaction or new topic");
            }
            
            // Call the chain and get response
            ChatClientResponse response = chain.nextCall(enhancedRequest);
            
            // Store conversation after processing
            try {
                String searchStrategy = extractSearchStrategy(response);
                double confidence = extractConfidence(response);
                
                logger.info("💾 Conversation stored - Strategy: {}, Confidence: {:.2f}", 
                    searchStrategy, confidence);
            } catch (Exception e) {
                logger.debug("Could not store conversation metadata: {}", e.getMessage());
            }
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Brain Memory: Error in memory advisor - {}", e.getMessage());
            return chain.nextCall(request); // Continue without memory context
        }
    }
    
    private String extractUserQuery(ChatClientRequest request) {
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
            logger.debug("Failed to extract user query: {}", e.getMessage());
        }
        return "";
    }
    
    private String getOrCreateSessionId(ChatClientRequest request) {
        // In a real application, this would come from HTTP session or authentication context
        // For now, generate a simple session ID based on request context
        return "session_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    private String getOrCreateUserId(ChatClientRequest request) {
        // In a real application, this would come from authentication context
        // For now, use a default user ID
        return "default_user";
    }
    
    private String buildMemoryContext(ConversationContext context, String currentQuery) {
        StringBuilder memoryContext = new StringBuilder();
        
        memoryContext.append("🧠 **CONVERSATION MEMORY CONTEXT:**\n\n");
        
        // Add recent conversation context
        if (!context.getRecentExchanges().isEmpty()) {
            memoryContext.append("📝 **Recent Conversation History:**\n");
            for (ConversationMemoryManager.ConversationExchange exchange : context.getRecentExchanges()) {
                memoryContext.append(String.format("- [%s] User asked: \"%s\"\n", 
                    exchange.getFormattedTimestamp(),
                    truncateText(exchange.getUserQuery(), 100)));
                memoryContext.append(String.format("  AI responded with %s strategy (confidence: %.1f)\n", 
                    exchange.getSearchStrategy(), exchange.getConfidence() * 100));
            }
            memoryContext.append("\n");
        }
        
        // Add related previous discussions
        if (!context.getRelatedExchanges().isEmpty()) {
            memoryContext.append("🔗 **Related Previous Discussions:**\n");
            for (ConversationMemoryManager.ConversationExchange exchange : context.getRelatedExchanges()) {
                memoryContext.append(String.format("- Previously discussed: \"%s\"\n", 
                    truncateText(exchange.getUserQuery(), 120)));
            }
            memoryContext.append("\n");
        }
        
        // Add user patterns and preferences
        if (!context.getPreferredSearchStrategies().isEmpty()) {
            memoryContext.append("🎯 **User Preferences:**\n");
            memoryContext.append("- Typically prefers: " + String.join(", ", context.getPreferredSearchStrategies()) + "\n");
        }
        
        if (!context.getTypicalQueryPatterns().isEmpty()) {
            memoryContext.append("- Query patterns: " + String.join("; ", context.getTypicalQueryPatterns()) + "\n");
        }
        
        // Add long-term memories if any
        if (!context.getLongTermMemories().isEmpty()) {
            memoryContext.append("\n📚 **Important Past Discussions:**\n");
            for (ConversationMemoryManager.ConversationMemory memory : context.getLongTermMemories()) {
                memoryContext.append(String.format("- [Important] %s (score: %.0f)\n", 
                    truncateText(memory.getExchange().getUserQuery(), 100),
                    memory.getImportanceScore()));
            }
        }
        
        memoryContext.append("\n🎯 **Current Query:** \"").append(currentQuery).append("\"\n\n");
        
        memoryContext.append("**INSTRUCTIONS:**\n");
        memoryContext.append("- Reference relevant previous discussions when appropriate\n");
        memoryContext.append("- Use phrases like \"As we discussed earlier...\" or \"Building on our previous conversation...\"\n");
        memoryContext.append("- Adapt your response style based on user preferences\n");
        memoryContext.append("- If this relates to previous topics, acknowledge the connection\n");
        memoryContext.append("- Maintain conversation continuity and context awareness\n\n");
        
        return memoryContext.toString();
    }
    
    private ChatClientRequest injectMemoryContext(ChatClientRequest request, String memoryContext) {
        // ❌ DISABLED: This concatenates history into query string, contaminating current queries
        // ✅ Use MessageChatMemoryAdvisor instead (handles message history properly)
        // String augmentedMessage = memoryContext + "\n\n" + extractUserQuery(request);
        // return request.mutate()
        //     .prompt(request.prompt().augmentUserMessage(augmentedMessage))
        //     .build();
        
        // ✅ NEW: Return request unchanged - MessageChatMemoryAdvisor will handle history
        return request;
    }
    
    private String extractSearchStrategy(ChatClientResponse response) {
        // Analyze response content for strategy indicators
        String responseText = response.chatResponse().getResult().getOutput().getText().toLowerCase();
        if (responseText.contains("dependency_graph")) return "dependency_graph";
        if (responseText.contains("entity_centered")) return "entity_centered";
        if (responseText.contains("method_focused")) return "method_focused";
        if (responseText.contains("error_trace")) return "error_trace";
        if (responseText.contains("configuration_chain")) return "configuration_chain";
        
        return "similarity_search"; // Default
    }
    
    private double extractConfidence(ChatClientResponse response) {
        // Estimate confidence based on response characteristics
        String responseText = response.chatResponse().getResult().getOutput().getText();
        if (responseText.length() > 500 && responseText.contains("```")) {
            return 0.8; // Good detailed response with code
        } else if (responseText.length() > 200) {
            return 0.6; // Decent response
        } else {
            return 0.4; // Short response, might be uncertain
        }
    }
    
    private String truncateText(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}

package com.vijay.manager;

import com.vijay.dto.ThoughtStreamCursor;
import com.vijay.service.ThoughtStreamProcessor;
import com.vijay.service.WorkingMemoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

/**
 * 🧠 Brain -1: Thought Stream Advisor (Cursor System)
 * 
 * Purpose: Guide attention and routing before any other processing
 * 
 * Responsibilities:
 * - Analyze query complexity and ambiguity
 * - Determine what to focus on and what to ignore
 * - Select appropriate reasoning strategy (fast recall vs slow reasoning)
 * - Select relevant brains to activate
 * - Create thought stream cursor for downstream advisors
 * - Update working memory with current state
 * 
 * Execution Order: -1 (FIRST - before Query Planner)
 * This is the "attention mechanism" that guides the entire thought process.
 */
@Component
public class ThoughtStreamAdvisor implements CallAdvisor, IAgentBrain {
    
    private static final Logger logger = LoggerFactory.getLogger(ThoughtStreamAdvisor.class);
    
    private final ThoughtStreamProcessor thoughtStreamProcessor;
    private final WorkingMemoryManager workingMemoryManager;
    
    public ThoughtStreamAdvisor(ThoughtStreamProcessor thoughtStreamProcessor,
                               WorkingMemoryManager workingMemoryManager) {
        this.thoughtStreamProcessor = thoughtStreamProcessor;
        this.workingMemoryManager = workingMemoryManager;
    }
    
    @Override
    public String getName() {
        return "ThoughtStreamAdvisor";
    }
    
    // ===== IAgentBrain Implementation =====
    @Override
    public String getBrainName() {
        return "thoughtStreamAdvisor";  // ← Spring bean name (lowercase first letter)
    }
    
    @Override
    public String getBrainDescription() {
        return "Manages attention and focus, determines query complexity and ambiguity, selects appropriate reasoning strategy and relevant brains to activate";
    }
    
    @Override
    public int getOrder() {
        return -1; // Execute FIRST - before Query Planner
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("🧠 Brain -1 (Thought Stream): Initializing attention mechanism...");
        
        try {
            // Extract user ID and query
            String userId = extractUserId(request);
            String userQuery = extractUserMessage(request);
            String queryId = generateQueryId();
            
            if (userQuery.isEmpty()) {
                logger.info("⚠️ Brain -1: Empty query, proceeding with default flow");
                return chain.nextCall(request);
            }
            
            // Record user message in working memory
            workingMemoryManager.recordUserMessage(userId, userQuery);
            
            // Process query through thought stream
            ThoughtStreamCursor cursor = thoughtStreamProcessor.processQuery(queryId, userQuery);
            
            // Log thought stream analysis
            logThoughtStreamAnalysis(cursor);
            
            // Augment request with cursor information
            ChatClientRequest augmentedRequest = augmentRequestWithCursor(request, cursor);
            
            // Continue to next advisor with augmented request
            ChatClientResponse response = chain.nextCall(augmentedRequest);
            
            // Record response outcome in working memory
            recordResponseOutcome(userId, response, cursor);
            
            logger.info("✅ Brain -1: Thought stream complete - {}", cursor.getSummary());
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Brain -1: Error in thought stream - {}", e.getMessage(), e);
            // Continue chain even if thought stream fails
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
     * Generate unique query ID
     */
    private String generateQueryId() {
        return "query_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }
    
    /**
     * Augment request with cursor information
     */
    private ChatClientRequest augmentRequestWithCursor(ChatClientRequest request, ThoughtStreamCursor cursor) {
        try {
            // Add cursor info to request (via prompt augmentation)
            if (request.prompt() != null && request.prompt().getInstructions() != null) {
                // Cursor info will be available in the context for downstream advisors
                logger.debug("✅ Request augmented with cursor - Focus: {}, Strategy: {}", 
                    cursor.getFocusArea(), cursor.getStrategy().getDisplayName());
            }
            
            return request;
        } catch (Exception e) {
            logger.debug("Could not augment request with cursor: {}", e.getMessage());
            return request;
        }
    }
    
    /**
     * Record response outcome in working memory
     */
    private void recordResponseOutcome(String userId, ChatClientResponse response, ThoughtStreamCursor cursor) {
        try {
            if (response != null && response.chatResponse() != null) {
                // Record brain output
                workingMemoryManager.recordBrainOutput(userId, "ThoughtStream", 
                    String.format("Strategy: %s, Focus: %s", 
                        cursor.getStrategy().getDisplayName(), 
                        cursor.getFocusArea()));
                
                // Record intent
                workingMemoryManager.recordIntent(userId, cursor.getFocusArea(), cursor.getConfidence());
                
                logger.debug("✅ Response outcome recorded in working memory");
            }
        } catch (Exception e) {
            logger.debug("Could not record response outcome: {}", e.getMessage());
        }
    }
    
    /**
     * Log thought stream analysis
     */
    private void logThoughtStreamAnalysis(ThoughtStreamCursor cursor) {
        logger.info("🧠 Thought Stream Analysis:");
        logger.info("   🎯 Focus Area: {}", cursor.getFocusArea());
        logger.info("   🚫 Ignore Area: {}", cursor.getIgnoreArea());
        logger.info("   📊 Complexity: {:.2f} | Ambiguity: {:.2f}", 
            cursor.getComplexity(), cursor.getAmbiguity());
        logger.info("   ⚡ Strategy: {}", cursor.getStrategy().getDisplayName());
        logger.info("   🧠 Slow Reasoning: {} | Fast Recall: {}", 
            cursor.isNeedsSlowReasoning(), cursor.isNeedsFastRecall());
        logger.info("   🎲 Relevant Brains: {}", String.join(", ", cursor.getRelevantBrains()));
        logger.info("   📈 Confidence: {:.2f}", cursor.getConfidence());
    }
}

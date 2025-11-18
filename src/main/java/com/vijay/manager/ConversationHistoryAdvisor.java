package com.vijay.manager;

import com.vijay.context.GlobalBrainContext;
import com.vijay.context.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

/**
 * 📚 ConversationHistoryAdvisor - Logs conversation history loading
 * 
 * This advisor:
 * 1. Logs when conversation history is being loaded
 * 2. Logs the conversation ID being used
 * 3. Logs the current user message
 * 
 * Order: -2 (runs BEFORE MessageChatMemoryAdvisor at -1)
 * 
 * Note: MessageChatMemoryAdvisor (at order -1) will actually load the history
 * This advisor just provides visibility into what's happening
 */
@Component
public class ConversationHistoryAdvisor implements CallAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(ConversationHistoryAdvisor.class);

    @Override
    public int getOrder() {
        return -2;  // Run BEFORE MessageChatMemoryAdvisor (-1)
    }

    @Override
    public String getName() {
        return "ConversationHistoryAdvisor";
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        String traceId = TraceContext.getTraceId();
        
        try {
            // Extract conversation ID from GlobalBrainContext (set by ChatService)
            String conversationId = (String) GlobalBrainContext.get("conversationId");
            
            // Extract current user message
            String userMessage = extractUserMessage(request);
            
            logger.info("[{}] 📚 ConversationHistoryAdvisor: Preparing to load history", traceId);
            if (conversationId != null && !conversationId.isEmpty()) {
                logger.info("[{}]    💾 Conversation ID: {}", traceId, conversationId);
            } else {
                logger.info("[{}]    ⚠️ No conversation ID in context (first message?)", traceId);
            }
            logger.info("[{}]    📝 Current message: {}", traceId, userMessage);
            logger.info("[{}]    ℹ️ MessageChatMemoryAdvisor (order -1) will load history next", traceId);

        } catch (Exception e) {
            logger.error("[{}]    ❌ Error in ConversationHistoryAdvisor: {}", traceId, e.getMessage());
        }

        return chain.nextCall(request);
    }

    /**
     * Extract user message from the request
     */
    private String extractUserMessage(ChatClientRequest request) {
        try {
            if (request.prompt() != null && request.prompt().getInstructions() != null) {
                StringBuilder messageText = new StringBuilder();
                for (Message message : request.prompt().getInstructions()) {
                    if (message instanceof UserMessage) {
                        UserMessage userMsg = (UserMessage) message;
                        messageText.append(userMsg.getText()).append(" ");
                    }
                }
                String result = messageText.toString().trim();
                return result.isEmpty() ? "(empty)" : result;
            }
        } catch (Exception e) {
            logger.debug("Could not extract user message: {}", e.getMessage());
        }
        return "(could not extract)";
    }
}

package com.vijay.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

@Component
public class QueryPlannerAdvisor implements CallAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(QueryPlannerAdvisor.class);

    @Override
    public String getName() {
        return "QueryPlannerAdvisor";
    }

    @Override
    public int getOrder() {
        return 0; // Run FIRST - this is the "front door"
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("🧠 Brain 0 (Query Planner): Analyzing query intent...");
        
        try {
            String userQuery = extractUserMessage(request);
            if (userQuery.isEmpty()) {
                logger.info("⚠️ Brain 0: Empty query, proceeding with default flow");
                return chain.nextCall(request);
            }

            String intent = analyzeQueryIntent(userQuery);
            logger.info("🎯 Brain 0: Query intent detected - {}", intent);

            return chain.nextCall(request);
            
        } catch (Exception e) {
            logger.error("❌ Brain 0: Error in query planning - {}", e.getMessage());
            return chain.nextCall(request);
        }
    }

    private String analyzeQueryIntent(String query) {
        String lowerQuery = query.toLowerCase();
        
        // Code-related patterns
        if (lowerQuery.contains("chatservice") || lowerQuery.contains("aiproviderconfig") ||
            lowerQuery.contains("advisor") || lowerQuery.contains("how does") ||
            lowerQuery.contains("show me") || lowerQuery.contains("explain") ||
            lowerQuery.contains("architecture") || lowerQuery.contains("code") ||
            lowerQuery.contains("class") || lowerQuery.contains("method") ||
            lowerQuery.contains("service") || lowerQuery.contains("config")) {
            return "CODE";
        }
        
        // Tool-related patterns
        if (lowerQuery.contains("weather") || lowerQuery.contains("calendar") ||
            lowerQuery.contains("meeting") || lowerQuery.contains("schedule") ||
            lowerQuery.contains("search") || lowerQuery.contains("email") ||
            lowerQuery.contains("time") || lowerQuery.contains("date") ||
            lowerQuery.contains("forecast") || lowerQuery.contains("temperature")) {
            return "TOOLS";
        }
        
        // General conversation patterns
        if (lowerQuery.contains("hello") || lowerQuery.contains("hi") ||
            lowerQuery.contains("hey") || lowerQuery.contains("how are you") ||
            lowerQuery.contains("what can you do") || lowerQuery.contains("help")) {
            return "GENERAL";
        }
        
        return "UNKNOWN";
    }

    private String extractUserMessage(ChatClientRequest request) {
        try {
            if (request.prompt() != null && request.prompt().getInstructions() != null) {
                StringBuilder messageText = new StringBuilder();
                for (var message : request.prompt().getInstructions()) {
                    if (message instanceof UserMessage) {
                        UserMessage userMsg = (UserMessage) message;
                        messageText.append(userMsg.getText()).append(" ");
                    }
                }
                return messageText.toString().trim();
            }
            return "";
        } catch (Exception e) {
            logger.debug("Failed to extract user message: {}", e.getMessage());
            return "";
        }
    }
}

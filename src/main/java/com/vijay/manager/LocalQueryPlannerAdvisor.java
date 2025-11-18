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
public class LocalQueryPlannerAdvisor implements CallAdvisor, IAgentBrain {

    private static final Logger logger = LoggerFactory.getLogger(LocalQueryPlannerAdvisor.class);

    @Override
    public String getName() {
        return "LocalQueryPlannerAdvisor";
    }

    @Override
    public int getOrder() {
        return 0; // Run FIRST - local planning without token usage
    }
    
    // ===== IAgentBrain Implementation =====
    @Override
    public String getBrainName() {
        return "localQueryPlannerAdvisor";  // ← Spring bean name (lowercase first letter)
    }
    
    @Override
    public String getBrainDescription() {
        return "Plans query execution locally, analyzes intent and decides what cognitive functions to use without token consumption";
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("🚀 Brain 0 (Local Planner): Analyzing query intent locally (NO TOKENS!)...");
        
        try {
            String userQuery = extractUserMessage(request);
            if (userQuery.isEmpty()) {
                logger.info("⚠️ Brain 0: Empty query, proceeding with default flow");
                return chain.nextCall(request);
            }

            // Local intent analysis (no API calls)
            QueryAnalysis analysis = analyzeQueryLocally(userQuery);
            
            logger.info("🎯 Brain 0: Local Analysis:");
            logger.info("   📝 Query: {}", userQuery.length() > 50 ? userQuery.substring(0, 50) + "..." : userQuery);
            logger.info("   🎯 Intent: {} (Confidence: {:.2f})", analysis.intent, analysis.confidence);
            logger.info("   🔍 Complexity: {} | Keywords: {}", analysis.complexity, analysis.keywordCount);
            
            if (analysis.isCorrection) {
                logger.info("🔄 Brain 0: CORRECTION detected - user providing feedback");
            }
            
            if (analysis.isComplex) {
                logger.info("🧩 Brain 0: COMPLEX query - multiple intents detected");
            }

            // Store planning results for memory tracking
            logger.info("📊 Brain 0: Planning results - Strategy: {}, Confidence: {}, Intent: {}, Complexity: {}",
                "local_" + analysis.intent.toLowerCase(), analysis.confidence, analysis.intent, analysis.complexity);

            return chain.nextCall(request);
            
        } catch (Exception e) {
            logger.error("❌ Brain 0: Error in local planning - {}", e.getMessage());
            return chain.nextCall(request);
        }
    }

    private QueryAnalysis analyzeQueryLocally(String query) {
        QueryAnalysis analysis = new QueryAnalysis();
        analysis.originalQuery = query;
        
        String lowerQuery = query.toLowerCase();
        String[] words = query.split("\\s+");
        analysis.keywordCount = words.length;
        
        // Detect corrections/feedback
        analysis.isCorrection = lowerQuery.contains("wrong") || lowerQuery.contains("incorrect") || 
                               lowerQuery.contains("you said") || lowerQuery.contains("but i") ||
                               lowerQuery.contains("actually") || lowerQuery.contains("correct") ||
                               lowerQuery.contains("no, ") || lowerQuery.contains("not ");
        
        // Detect complexity
        analysis.isComplex = words.length > 15 || 
                           lowerQuery.contains(" and ") || 
                           lowerQuery.contains(" also ") || 
                           lowerQuery.contains(" plus ") ||
                           lowerQuery.contains(" then ");
        
        // Intent classification with confidence scoring
        int codeScore = 0;
        int toolScore = 0;
        int generalScore = 0;
        
        // Code-related keywords
        String[] codeKeywords = {
            "chatservice", "aiproviderconfig", "advisor", "service", "config", "class", "method",
            "how does", "show me", "explain", "architecture", "implementation", "dependency",
            "brain", "code", "function", "java", "spring", "component", "controller", "repository"
        };
        
        // Tool-related keywords
        String[] toolKeywords = {
            "weather", "temperature", "calendar", "meeting", "schedule", "search", "email",
            "time", "date", "forecast", "event", "appointment", "google", "find", "version",
            "latest", "current", "today", "now", "when", "what time"
        };
        
        // General keywords
        String[] generalKeywords = {
            "hello", "hi", "hey", "how are you", "what can you do", "help", "thanks", 
            "thank you", "please", "can you", "would you", "could you"
        };
        
        // Score each category
        for (String keyword : codeKeywords) {
            if (lowerQuery.contains(keyword)) codeScore++;
        }
        for (String keyword : toolKeywords) {
            if (lowerQuery.contains(keyword)) toolScore++;
        }
        for (String keyword : generalKeywords) {
            if (lowerQuery.contains(keyword)) generalScore++;
        }
        
        // Determine intent and confidence
        if (analysis.isCorrection) {
            analysis.intent = "CORRECTION";
            analysis.confidence = 0.9;
        } else if (analysis.isComplex) {
            analysis.intent = "COMPLEX";
            analysis.confidence = 0.8;
        } else if (codeScore > toolScore && codeScore > generalScore) {
            analysis.intent = "CODE";
            analysis.confidence = Math.min(0.95, 0.6 + (codeScore * 0.1));
        } else if (toolScore > codeScore && toolScore > generalScore) {
            analysis.intent = "TOOLS";
            analysis.confidence = Math.min(0.95, 0.6 + (toolScore * 0.1));
        } else if (generalScore > 0) {
            analysis.intent = "GENERAL";
            analysis.confidence = Math.min(0.9, 0.5 + (generalScore * 0.1));
        } else {
            analysis.intent = "UNKNOWN";
            analysis.confidence = 0.3;
        }
        
        // Determine complexity level
        if (words.length < 5) {
            analysis.complexity = "LOW";
        } else if (words.length < 15) {
            analysis.complexity = "MEDIUM";
        } else {
            analysis.complexity = "HIGH";
        }
        
        return analysis;
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

    private static class QueryAnalysis {
        String originalQuery;
        String intent;
        double confidence;
        String complexity;
        int keywordCount;
        boolean isCorrection;
        boolean isComplex;
    }
}

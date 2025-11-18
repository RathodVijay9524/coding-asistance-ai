package com.vijay.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SmartQualityAdvisor implements CallAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(SmartQualityAdvisor.class);
    
    private final ChatClient qualityClient;
    private static final int MAX_RESPONSE_LENGTH = 1500;
    private static final double MIN_QUALITY_SCORE = 3.0;

    public SmartQualityAdvisor(OpenAiChatModel chatModel) {
        this.qualityClient = ChatClient.builder(chatModel).build();
    }

    @Override
    public String getName() {
        return "SmartQualityAdvisor";
    }

    @Override
    public int getOrder() {
        return 800; // Run after Knowledge Graph but before final processing
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("🎯 Smart Quality Advisor: Analyzing response quality...");
        
        try {
            // Get the response from the chain
            ChatClientResponse response = chain.nextCall(request);
            
            if (response == null || response.chatResponse() == null) {
                logger.warn("⚠️ Smart Quality: No response to analyze");
                return response;
            }

            String content = response.chatResponse().getResult().getOutput().getText();
            if (content == null || content.trim().isEmpty()) {
                logger.warn("⚠️ Smart Quality: Empty response content");
                return response;
            }

            // Analyze response quality
            String userQuery = extractUserQuery(request);
            QualityMetrics metrics = analyzeQuality(content, userQuery);
            
            logger.info("📊 Smart Quality: Length: {}, Entities: {}, Quality: {:.1f}/5.0", 
                metrics.length, metrics.entities.size(), metrics.qualityScore);
            
            // Log quality insights
            if (!metrics.entities.isEmpty()) {
                logger.info("🏷️ Key Components: {}", String.join(", ", metrics.entities));
            }
            
            if (metrics.length > MAX_RESPONSE_LENGTH) {
                logger.info("📏 Response length exceeds recommended limit ({} > {})", 
                    metrics.length, MAX_RESPONSE_LENGTH);
            }
            
            if (metrics.qualityScore < MIN_QUALITY_SCORE) {
                logger.warn("⚠️ Response quality below threshold ({:.1f} < {:.1f})", 
                    metrics.qualityScore, MIN_QUALITY_SCORE);
                logQualityIssues(content, userQuery);
            } else {
                logger.info("✅ Response quality acceptable ({:.1f}/5.0)", metrics.qualityScore);
            }
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Smart Quality: Error in quality analysis - {}", e.getMessage());
            return chain.nextCall(request);
        }
    }

    private QualityMetrics analyzeQuality(String content, String userQuery) {
        QualityMetrics metrics = new QualityMetrics();
        metrics.length = content.length();
        metrics.entities = extractTechnicalEntities(content);
        metrics.qualityScore = calculateQualityScore(content, userQuery);
        return metrics;
    }

    private Set<String> extractTechnicalEntities(String content) {
        Set<String> entities = new HashSet<>();
        
        // Technical entity patterns
        Pattern[] patterns = {
            Pattern.compile("\\b([A-Z][a-zA-Z]*Service)\\b"),           // Services
            Pattern.compile("\\b([A-Z][a-zA-Z]*Advisor)\\b"),           // Advisors  
            Pattern.compile("\\b([A-Z][a-zA-Z]*Config)\\b"),            // Configurations
            Pattern.compile("\\b([A-Z][a-zA-Z]*Controller)\\b"),        // Controllers
            Pattern.compile("\\b(Brain \\d+)\\b"),                      // Brains
            Pattern.compile("\\b(OpenAI|Claude|Gemini|Ollama)\\b"),     // AI Models
            Pattern.compile("\\b([a-zA-Z]+\\.java)\\b"),                // Java files
            Pattern.compile("\\b(@[A-Z][a-zA-Z]*)\\b")                  // Annotations
        };
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                entities.add(matcher.group(1));
            }
        }
        
        return entities;
    }

    private double calculateQualityScore(String content, String userQuery) {
        double score = 2.0; // Base score
        
        // Length appropriateness
        if (content.length() > 100 && content.length() < 2000) {
            score += 0.5;
        }
        
        // Query relevance
        if (userQuery != null && !userQuery.isEmpty()) {
            String[] queryWords = userQuery.toLowerCase().split("\\s+");
            String lowerContent = content.toLowerCase();
            int matches = 0;
            for (String word : queryWords) {
                if (word.length() > 3 && lowerContent.contains(word)) {
                    matches++;
                }
            }
            score += Math.min(1.0, matches * 0.1);
        }
        
        // Structure quality
        if (content.contains("##") || content.contains("###")) score += 0.3;
        if (content.contains("- ") || content.contains("* ")) score += 0.2;
        if (content.contains("```")) score += 0.3;
        
        // Technical accuracy
        if (content.contains("Service") || content.contains("Advisor")) score += 0.2;
        if (content.contains("Brain") && content.contains("Architecture")) score += 0.3;
        
        // Completeness indicators
        if (content.contains("Example:") || content.contains("For example")) score += 0.2;
        if (content.contains("Steps:") || content.contains("1.") || content.contains("2.")) score += 0.2;
        
        return Math.min(5.0, score);
    }

    private void logQualityIssues(String content, String userQuery) {
        // Check for common quality issues
        if (content.length() < 100) {
            logger.warn("📏 Issue: Response too short (< 100 chars)");
        }
        
        if (!content.contains(".") && !content.contains("!") && !content.contains("?")) {
            logger.warn("📝 Issue: No proper sentence structure");
        }
        
        if (userQuery != null && !userQuery.isEmpty()) {
            String[] queryWords = userQuery.toLowerCase().split("\\s+");
            String lowerContent = content.toLowerCase();
            boolean hasRelevantKeywords = false;
            for (String word : queryWords) {
                if (word.length() > 3 && lowerContent.contains(word)) {
                    hasRelevantKeywords = true;
                    break;
                }
            }
            if (!hasRelevantKeywords) {
                logger.warn("🎯 Issue: Response may not be relevant to query");
            }
        }
        
        if (!content.contains("Service") && !content.contains("Config") && 
            !content.contains("Advisor") && userQuery != null && 
            userQuery.toLowerCase().contains("how does")) {
            logger.warn("🔧 Issue: Technical explanation may lack specific components");
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

    private static class QualityMetrics {
        int length;
        double qualityScore;
        Set<String> entities = new HashSet<>();
    }
}

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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EnhancedContextBuilderAdvisor implements CallAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedContextBuilderAdvisor.class);
    
    private final ChatClient contextBuilderClient;
    private static final int MAX_RESPONSE_LENGTH = 1500; // Token budget
    private static final double MIN_QUALITY_SCORE = 0.7;

    public EnhancedContextBuilderAdvisor(OpenAiChatModel chatModel) {
        this.contextBuilderClient = ChatClient.builder(chatModel).build();
    }

    @Override
    public String getName() {
        return "EnhancedContextBuilderAdvisor";
    }

    @Override
    public int getOrder() {
        return 500; // Run after Knowledge Graph (100) but before Self-Refine (1000)
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("📝 Brain 2 (Enhanced Context Builder): Processing response...");
        
        try {
            // Get the response from the chain
            ChatClientResponse response = chain.nextCall(request);
            
            if (response == null || response.chatResponse() == null) {
                logger.warn("⚠️ Brain 2: No response to process");
                return response;
            }

            String originalContent = response.chatResponse().getResult().getOutput().getText();
            if (originalContent == null || originalContent.trim().isEmpty()) {
                logger.warn("⚠️ Brain 2: Empty response content");
                return response;
            }

            // Analyze and enhance the response
            ResponseAnalysis analysis = analyzeResponse(originalContent, extractUserQuery(request));
            logger.info("📊 Brain 2: Analysis - Quality: {:.2f}, Length: {}, Entities: {}", 
                analysis.qualityScore, analysis.length, analysis.entities.size());

            String enhancedContent = enhanceResponse(originalContent, analysis);
            
            // For now, we'll return the original response and log the enhancement
            // The actual response modification would require deeper API integration
            logger.info("📝 Brain 2: Enhanced content ready (length: {})", enhancedContent.length());
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Brain 2: Error in context building - {}", e.getMessage());
            return chain.nextCall(request);
        }
    }

    private ResponseAnalysis analyzeResponse(String content, String userQuery) {
        ResponseAnalysis analysis = new ResponseAnalysis();
        analysis.originalContent = content;
        analysis.length = content.length();
        analysis.userQuery = userQuery;
        
        // Extract entities
        analysis.entities = extractEntities(content);
        
        // Extract relationships
        analysis.relationships = extractRelationships(content);
        
        // Calculate quality score
        analysis.qualityScore = calculateQualityScore(content, userQuery);
        
        // Determine if summarization is needed
        analysis.needsSummarization = analysis.length > MAX_RESPONSE_LENGTH;
        
        // Determine if quality improvement is needed
        analysis.needsImprovement = analysis.qualityScore < MIN_QUALITY_SCORE;
        
        return analysis;
    }

    private Set<String> extractEntities(String content) {
        Set<String> entities = new HashSet<>();
        
        // Technical entities
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

    private Set<String> extractRelationships(String content) {
        Set<String> relationships = new HashSet<>();
        
        // Relationship patterns
        String[] relationshipKeywords = {
            "depends on", "uses", "extends", "implements", "calls", "invokes",
            "configured by", "managed by", "processes", "handles", "triggers"
        };
        
        String lowerContent = content.toLowerCase();
        for (String keyword : relationshipKeywords) {
            if (lowerContent.contains(keyword)) {
                relationships.add(keyword);
            }
        }
        
        return relationships;
    }

    private double calculateQualityScore(String content, String userQuery) {
        double score = 0.5; // Base score
        
        // Length appropriateness (not too short, not too long)
        if (content.length() > 100 && content.length() < 2000) {
            score += 0.1;
        }
        
        // Query relevance (contains keywords from user query)
        if (userQuery != null && !userQuery.isEmpty()) {
            String[] queryWords = userQuery.toLowerCase().split("\\s+");
            String lowerContent = content.toLowerCase();
            int matches = 0;
            for (String word : queryWords) {
                if (word.length() > 3 && lowerContent.contains(word)) {
                    matches++;
                }
            }
            score += Math.min(0.3, matches * 0.05);
        }
        
        // Structure quality (has headings, bullet points, code blocks)
        if (content.contains("##") || content.contains("###")) score += 0.1;
        if (content.contains("- ") || content.contains("* ")) score += 0.05;
        if (content.contains("```")) score += 0.1;
        
        // Technical accuracy (mentions specific components)
        if (content.contains("Service") || content.contains("Advisor")) score += 0.05;
        if (content.contains("Brain") && content.contains("Architecture")) score += 0.1;
        
        return Math.min(1.0, score);
    }

    private String enhanceResponse(String originalContent, ResponseAnalysis analysis) {
        StringBuilder enhanced = new StringBuilder();
        
        // If quality is too low, try to improve it
        if (analysis.needsImprovement) {
            logger.info("🔧 Brain 2: Improving response quality (score: {:.2f})", analysis.qualityScore);
            String improvedContent = improveResponseQuality(originalContent, analysis);
            if (improvedContent != null && !improvedContent.equals(originalContent)) {
                originalContent = improvedContent;
                analysis.qualityScore = calculateQualityScore(originalContent, analysis.userQuery);
                logger.info("✅ Brain 2: Quality improved to {:.2f}", analysis.qualityScore);
            }
        }
        
        // If too long, summarize
        if (analysis.needsSummarization) {
            logger.info("📝 Brain 2: Summarizing response ({} chars → target: {})", 
                analysis.length, MAX_RESPONSE_LENGTH);
            String summary = createStructuredSummary(originalContent, analysis);
            if (summary != null && summary.length() < originalContent.length()) {
                enhanced.append(summary);
                logger.info("✅ Brain 2: Summarized to {} chars", summary.length());
            } else {
                enhanced.append(originalContent);
            }
        } else {
            enhanced.append(originalContent);
        }
        
        // Add context metadata if entities or relationships found
        if (!analysis.entities.isEmpty() || !analysis.relationships.isEmpty()) {
            enhanced.append(buildContextMetadata(analysis));
        }
        
        return enhanced.toString();
    }

    private String improveResponseQuality(String content, ResponseAnalysis analysis) {
        try {
            String improvementPrompt = String.format("""
                Improve this response to make it more accurate, structured, and helpful.
                
                Original Query: %s
                Current Response: %s
                
                Issues to fix:
                - Quality score is low (%.2f)
                - Make it more structured with headings and bullet points
                - Add more specific technical details
                - Ensure it directly answers the user's question
                
                Return only the improved response, no explanations.
                """, analysis.userQuery, content, analysis.qualityScore);

            return contextBuilderClient.prompt(improvementPrompt).call().content();
            
        } catch (Exception e) {
            logger.warn("⚠️ Brain 2: Failed to improve response quality - {}", e.getMessage());
            return content;
        }
    }

    private String createStructuredSummary(String content, ResponseAnalysis analysis) {
        try {
            String summaryPrompt = String.format("""
                Create a structured summary of this response. Keep it under %d characters.
                
                Original Query: %s
                Content to summarize: %s
                
                Key entities found: %s
                Key relationships: %s
                
                Create a summary that:
                1. Directly answers the user's question
                2. Includes the most important technical details
                3. Uses bullet points and headings for clarity
                4. Preserves code examples if present
                
                Return only the summary, no explanations.
                """, MAX_RESPONSE_LENGTH, analysis.userQuery, content, 
                String.join(", ", analysis.entities), 
                String.join(", ", analysis.relationships));

            return contextBuilderClient.prompt(summaryPrompt).call().content();
            
        } catch (Exception e) {
            logger.warn("⚠️ Brain 2: Failed to create summary - {}", e.getMessage());
            return content;
        }
    }

    private String buildContextMetadata(ResponseAnalysis analysis) {
        StringBuilder metadata = new StringBuilder();
        
        if (!analysis.entities.isEmpty()) {
            metadata.append("\n\n**🏷️ Key Components:** ");
            metadata.append(String.join(", ", analysis.entities));
        }
        
        if (!analysis.relationships.isEmpty()) {
            metadata.append("\n\n**🔗 Relationships:** ");
            metadata.append(String.join(", ", analysis.relationships));
        }
        
        return metadata.toString();
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

    private static class ResponseAnalysis {
        String originalContent;
        String userQuery;
        int length;
        double qualityScore;
        Set<String> entities = new HashSet<>();
        Set<String> relationships = new HashSet<>();
        boolean needsSummarization;
        boolean needsImprovement;
    }
}

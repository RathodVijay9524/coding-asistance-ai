package com.vijay.manager;

import com.vijay.context.GlobalBrainContext;
import com.vijay.service.UserProfilingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

/**
 * 🧠 Brain 5: User Profiling Advisor
 * 
 * Builds and maintains user profiles including:
 * - Expertise level detection
 * - Preferred response formats
 * - Specialization areas
 * - Interaction patterns
 * 
 * Adapts responses based on user profile to improve relevance and satisfaction.
 * 
 * Execution Order: 2 (Early, after memory, to adapt behavior for this user)
 */
@Component
public class UserProfilingAdvisor implements CallAdvisor, IAgentBrain {
    
    private static final Logger logger = LoggerFactory.getLogger(UserProfilingAdvisor.class);
    
    private final UserProfilingService userProfilingService;
    
    public UserProfilingAdvisor(UserProfilingService userProfilingService) {
        this.userProfilingService = userProfilingService;
    }
    
    @Override
    public String getName() {
        return "UserProfilingAdvisor";
    }
    
    @Override
    public int getOrder() {
        return 5; // Execute early to adapt behavior for this user
    }
    
    // ===== IAgentBrain Implementation =====
    @Override
    public String getBrainName() {
        return "userProfilingAdvisor";  // ← Spring bean name (lowercase first letter)
    }
    
    @Override
    public String getBrainDescription() {
        return "Builds and maintains user profile, remembers preferences and communication style, adapts responses based on user expertise and history";
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("👤 Brain 5 (User Profiling): Analyzing user profile and preferences...");
        
        try {
            // Extract user ID
            String userId = extractUserId(request);
            
            // ✨ NEW: Extract user name from query (e.g., "my name is Vijay")
            String userQuery = extractUserQuery(request);
            String userName = extractUserName(userQuery);
            if (userName != null && !userName.isEmpty()) {
                logger.info("👤 Brain 5: User identified as: {}", userName);
                // Store in GlobalBrainContext for all brains to use
                GlobalBrainContext.put("userName", userName);
                // TODO: Store in UserProfilingService for persistence when method is available
            }
            
            // Get user profile
            UserProfilingService.UserProfileSummary profile = userProfilingService.getProfileSummary(userId);
            
            logger.info("👤 Brain 5: User Profile - {}", profile);
            
            // Adapt request based on user profile
            ChatClientRequest adaptedRequest = adaptRequestForUser(request, profile);
            
            // Call the chain with adapted request
            ChatClientResponse response = chain.nextCall(adaptedRequest);
            
            // Record interaction for learning
            try {
                recordUserInteraction(userId, response, profile);
            } catch (Exception e) {
                logger.debug("Could not record user interaction: {}", e.getMessage());
            }
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Brain 5: Error in user profiling - {}", e.getMessage());
            return chain.nextCall(request);
        }
    }
    
    /**
     * Adapt the request based on user profile
     */
    private ChatClientRequest adaptRequestForUser(ChatClientRequest request, 
                                                   UserProfilingService.UserProfileSummary profile) {
        StringBuilder adaptationPrompt = new StringBuilder();
        
        // Add expertise level guidance
        adaptationPrompt.append(getExpertiseGuidance(profile.expertiseLevel));
        
        // Add format preference
        adaptationPrompt.append(getFormatGuidance(profile.preferredResponseFormat));
        
        // Add specialization context
        if (!profile.specializations.isEmpty()) {
            adaptationPrompt.append("\n\nUser Specializations: ").append(String.join(", ", profile.specializations));
            adaptationPrompt.append("\nConsider user's expertise in these areas when responding.");
        }
        
        // Augment the user message with profile-based guidance
        String userQuery = extractUserQuery(request);
        String augmentedQuery = adaptationPrompt.toString() + "\n\nUser Query: " + userQuery;
        
        return request.mutate()
            .prompt(request.prompt().augmentUserMessage(augmentedQuery))
            .build();
    }
    
    /**
     * Get guidance based on expertise level
     */
    private String getExpertiseGuidance(int expertiseLevel) {
        return switch (expertiseLevel) {
            case 1 -> "🟢 User is a BEGINNER. Provide clear, simple explanations with basic examples.";
            case 2 -> "🟡 User is INTERMEDIATE. Balance depth with clarity. Include some advanced concepts.";
            case 3 -> "🟠 User is ADVANCED. Assume good foundational knowledge. Focus on details and edge cases.";
            case 4 -> "🔴 User is EXPERT. Provide in-depth technical analysis. Discuss trade-offs and optimizations.";
            case 5 -> "⚫ User is MASTER. Assume deep expertise. Focus on cutting-edge approaches and research.";
            default -> "User expertise level unknown. Provide balanced explanation.";
        };
    }
    
    /**
     * Get guidance based on preferred response format
     */
    private String getFormatGuidance(String format) {
        return switch (format) {
            case "concise" -> "Response Style: Keep responses brief and to the point.";
            case "detailed" -> "Response Style: Provide comprehensive, detailed explanations.";
            case "code-heavy" -> "Response Style: Focus on code examples and implementation details.";
            case "balanced" -> "Response Style: Balance theory with practical examples.";
            default -> "Response Style: Use a balanced approach.";
        };
    }
    
    /**
     * Record user interaction for profiling
     */
    private void recordUserInteraction(String userId, ChatClientResponse response, 
                                      UserProfilingService.UserProfileSummary profile) {
        try {
            // Extract response quality
            String responseText = response.chatResponse().getResult().getOutput().getText();
            int quality = calculateQuality(responseText);
            
            // Determine specialization from response
            String specialization = detectSpecialization(responseText);
            
            // Record interaction
            userProfilingService.recordInteraction(userId, "GENERAL", quality, specialization);
            
            logger.debug("👤 Brain 5: Recorded interaction - Quality: {}, Specialization: {}", 
                quality, specialization);
            
        } catch (Exception e) {
            logger.debug("Could not record interaction: {}", e.getMessage());
        }
    }
    
    /**
     * Calculate response quality
     */
    private int calculateQuality(String responseText) {
        int quality = 3; // Base score
        
        if (responseText.length() > 500) quality++;
        if (responseText.contains("```")) quality++;
        if (responseText.contains("\n") && responseText.split("\n").length > 5) quality++;
        
        return Math.min(5, quality);
    }
    
    /**
     * Detect specialization from response content
     */
    private String detectSpecialization(String responseText) {
        String lower = responseText.toLowerCase();
        
        if (lower.contains("frontend") || lower.contains("react") || lower.contains("vue")) {
            return "frontend";
        } else if (lower.contains("backend") || lower.contains("database") || lower.contains("api")) {
            return "backend";
        } else if (lower.contains("devops") || lower.contains("docker") || lower.contains("kubernetes")) {
            return "devops";
        } else if (lower.contains("security") || lower.contains("encryption") || lower.contains("auth")) {
            return "security";
        } else if (lower.contains("performance") || lower.contains("optimization") || lower.contains("cache")) {
            return "performance";
        } else if (lower.contains("architecture") || lower.contains("design pattern")) {
            return "architecture";
        }
        
        return "general";
    }
    
    /**
     * Extract user ID from request
     */
    private String extractUserId(ChatClientRequest request) {
        // In a real application, this would come from authentication context
        // For now, use a default or session-based ID
        return "default_user";
    }
    
    /**
     * Extract user query from request
     */
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
    
    /**
     * ✨ NEW: Extract user name from query
     * Patterns: "my name is Vijay", "I'm Vijay", "call me Vijay"
     */
    private String extractUserName(String query) {
        if (query == null || query.isEmpty()) return null;
        
        String lowerQuery = query.toLowerCase();
        
        // Pattern 1: "my name is <name>"
        if (lowerQuery.contains("my name is")) {
            int startIdx = lowerQuery.indexOf("my name is") + 10;
            String remainder = query.substring(startIdx).trim();
            String name = remainder.split("[,.]")[0].trim();
            name = sanitizeName(name);
            if (!name.isEmpty() && name.length() < 50) {
                return name;
            }
        }
        
        // Pattern 2: "I'm <name>"
        if (lowerQuery.contains("i'm ")) {
            int startIdx = lowerQuery.indexOf("i'm ") + 4;
            String remainder = query.substring(startIdx).trim();
            String name = remainder.split("[,.]")[0].trim();
            name = sanitizeName(name);
            if (!name.isEmpty() && name.length() < 50) {
                return name;
            }
        }
        
        // Pattern 3: "call me <name>"
        if (lowerQuery.contains("call me ")) {
            int startIdx = lowerQuery.indexOf("call me ") + 8;
            String remainder = query.substring(startIdx).trim();
            String name = remainder.split("[,.]")[0].trim();
            name = sanitizeName(name);
            if (!name.isEmpty() && name.length() < 50) {
                return name;
            }
        }
        
        return null;
    }

    private String sanitizeName(String name) {
        String lower = name.toLowerCase();
        int andIndex = lower.indexOf(" and ");
        if (andIndex > 0) {
            return name.substring(0, andIndex).trim();
        }
        return name;
    }
}

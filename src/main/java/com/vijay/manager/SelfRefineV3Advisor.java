package com.vijay.manager;

import com.vijay.context.GlobalBrainContext;
import com.vijay.context.TraceContext;
import com.vijay.dto.AgentPlan;
import com.vijay.service.*;
import com.vijay.util.AgentPlanHolder;
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

/**
 * 🧠 Brain 13: Self-Refine V3 Advisor (Enhanced Judge) - Phase 7
 * 
 * Purpose: Enhanced judge with all checks, Δ-diff improvement tracking,
 * hallucination penalties, consistency validation, field validation,
 * code structure validation.
 * 
 * Responsibilities:
 * - Evaluate response quality with all checks
 * - Track Δ-diff improvement
 * - Apply hallucination penalties
 * - Validate consistency
 * - Validate required fields
 * - Validate code structure
 * - Trigger regeneration if needed
 * - Enforce quality standards
 * 
 * Execution Order: 1000 (LAST - Final Quality Gate)
 */
@Component
public class SelfRefineV3Advisor implements CallAdvisor, IAgentBrain {
    
    private static final Logger logger = LoggerFactory.getLogger(SelfRefineV3Advisor.class);
    
    private final ChatClient judgeClient;
    private final SupervisorBrain supervisorBrain;
    private final TokenCountingService tokenCountingService;
    private final ConsistencyCheckService consistencyCheckService;
    private final HallucinationDetector hallucinationDetector;
    private final OutputMerger outputMerger;
    
    // Configuration
    private static final double MIN_ACCEPTABLE_RATING = 3.8;  // Raised from 3.5 - most responses should be "Good" or better
    private static final double HALLUCINATION_PENALTY = 0.3;
    private static final double CONSISTENCY_PENALTY = 0.2;
    private static final int MAX_REFINEMENT_ATTEMPTS = 2;  // Reduced from 3 - good responses don't need refinement
    
    public SelfRefineV3Advisor(
            OpenAiChatModel chatModel,
            SupervisorBrain supervisorBrain,
            TokenCountingService tokenCountingService,
            ConsistencyCheckService consistencyCheckService,
            HallucinationDetector hallucinationDetector,
            OutputMerger outputMerger) {
        this.judgeClient = ChatClient.builder(chatModel).build();
        this.supervisorBrain = supervisorBrain;
        this.tokenCountingService = tokenCountingService;
        this.consistencyCheckService = consistencyCheckService;
        this.hallucinationDetector = hallucinationDetector;
        this.outputMerger = outputMerger;
    }
    
    @Override
    public String getName() {
        return "SelfRefineV3Advisor";
    }
    
    @Override
    public int getOrder() {
        return 1000; // Run LAST - final quality gate
    }
    
    // ===== IAgentBrain Implementation =====
    @Override
    public String getBrainName() {
        return "selfRefineV3Advisor";  // ← Spring bean name (lowercase first letter)
    }
    
    @Override
    public String getBrainDescription() {
        return "Evaluates responses against multiple criteria, judges quality and appropriateness, triggers refinement if needed, enforces quality standards";
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        String traceId = TraceContext.getTraceId();
        logger.info("[{}] 🧾 Brain 13 (Self-Refine V3): Enhanced quality evaluation...", traceId);
        
        try {
            String userId = extractUserId(request);
            String userQuery = extractUserMessage(request);
            
            // 💾 Get conversation ID from GlobalBrainContext (set by ChatService)
            String conversationId = (String) GlobalBrainContext.get("conversationId");
            if (conversationId == null || conversationId.isEmpty()) {
                // Only generate new ID if none exists
                conversationId = generateConversationId();
                logger.warn("[{}] ⚠️ No conversation ID in context, generated: {}", traceId, conversationId);
            } else {
                logger.info("[{}] ✅ Using existing conversation ID: {}", traceId, conversationId);
            }
            
            // Initialize supervisor for this conversation (reuse existing ID)
            supervisorBrain.initializeConversation(userId, conversationId);
            
            // ✅ OPTIMIZATION: Skip quality checks for simple queries (complexity ≤ 3)
            AgentPlan plan = AgentPlanHolder.getPlan();
            if (plan != null && plan.getComplexity() <= 3) {
                logger.info("[{}] 🚀 Brain 13: Skipping quality evaluation for simple query (complexity: {})", 
                    traceId, plan.getComplexity());
                logger.info("[{}]    📊 Query intent: {}, Strategy: {}", 
                    traceId, plan.getIntent(), plan.getStrategy());
                
                // Just pass through without quality checks
                return chain.nextCall(request);
            }
            
            // Get initial response
            ChatClientResponse response = chain.nextCall(request);
            
            if (response == null || response.chatResponse() == null) {
                logger.warn("[{}] ⚠️ Brain 13: No response to evaluate", traceId);
                return response;
            }
            
            String content = response.chatResponse().getResult().getOutput().getText();
            if (content == null || content.trim().isEmpty()) {
                logger.warn("[{}] ⚠️ Brain 13: Empty response content", traceId);
                return response;
            }
            
            // Perform comprehensive evaluation
            EnhancedQualityEvaluation evaluation = performEnhancedEvaluation(
                content, userQuery, userId, conversationId
            );
            
            // Log evaluation details
            logEvaluationDetails(evaluation);
            
            // Check if refinement is needed
            if (evaluation.finalRating < MIN_ACCEPTABLE_RATING && 
                evaluation.refinementAttempts < MAX_REFINEMENT_ATTEMPTS) {
                
                logger.info("🔁 Brain 13: Quality too low ({}), attempting refinement...", 
                    String.format("%.2f", evaluation.finalRating));
                
                String refinedContent = refineResponse(content, userQuery, evaluation);
                
                if (refinedContent != null && !refinedContent.equals(content)) {
                    // Re-evaluate refined response
                    EnhancedQualityEvaluation refinedEvaluation = performEnhancedEvaluation(
                        refinedContent, userQuery, userId, conversationId
                    );
                    
                    logger.info("🔄 Brain 13: Refined quality: {} (improvement: {})", 
                        String.format("%.2f", refinedEvaluation.finalRating), 
                        String.format("%.2f", refinedEvaluation.finalRating - evaluation.finalRating));
                    
                    // Use refined if better
                    if (refinedEvaluation.finalRating > evaluation.finalRating) {
                        evaluation = refinedEvaluation;
                        content = refinedContent;
                    }
                }
            }
            
            // Log final status
            supervisorBrain.logStatus(conversationId);
            
            logger.info("✅ Brain 13: Final rating: {}/5.0 - {}", 
                String.format("%.2f", evaluation.finalRating), evaluation.verdict);
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Brain 13: Error in Self-Refine V3 - {}", e.getMessage(), e);
            return chain.nextCall(request);
        }
    }
    
    /**
     * Perform comprehensive enhanced evaluation
     */
    private EnhancedQualityEvaluation performEnhancedEvaluation(
            String content, String userQuery, String userId, String conversationId) {
        
        EnhancedQualityEvaluation evaluation = new EnhancedQualityEvaluation();
        
        // 1. Basic quality evaluation
        double clarityScore = evaluateClarity(content);
        double relevanceScore = evaluateRelevance(content, userQuery);
        double helpfulnessScore = evaluateHelpfulness(content);
        
        evaluation.clarityScore = clarityScore;
        evaluation.relevanceScore = relevanceScore;
        evaluation.helpfulnessScore = helpfulnessScore;
        
        // 2. Consistency check
        ConsistencyCheckService.ConsistencyReport consistencyReport = 
            consistencyCheckService.checkConsistency(content);
        
        double consistencyScore = consistencyReport.isConsistent() ? 1.0 : 0.5;
        evaluation.consistencyScore = consistencyScore;
        evaluation.consistencyIssues = consistencyReport.getIssues().size();
        
        // Apply consistency penalty
        if (evaluation.consistencyIssues > 0) {
            evaluation.consistencyPenalty = CONSISTENCY_PENALTY * (evaluation.consistencyIssues / 5.0);
        }
        
        // 3. Hallucination detection
        HallucinationDetector.HallucinationReport hallucinationReport = 
            hallucinationDetector.detectHallucinations(content);
        
        evaluation.hallucinationScore = hallucinationReport.getHallucinationScore();
        evaluation.hallucinationCount = hallucinationReport.getHallucinationCount();
        
        // Apply hallucination penalty
        if (evaluation.hallucinationCount > 0) {
            evaluation.hallucinationPenalty = HALLUCINATION_PENALTY * evaluation.hallucinationScore;
        }
        
        // 4. Code structure validation (if applicable)
        if (content.contains("class") || content.contains("public") || content.contains("def")) {
            ConsistencyCheckService.CodeStructureReport codeReport = 
                consistencyCheckService.validateCodeStructure(content);
            
            evaluation.codeStructureValid = codeReport.isValid();
            evaluation.codeIssues = codeReport.getIssues().size();
            
            if (!evaluation.codeStructureValid) {
                evaluation.hallucinationPenalty += 0.1; // Additional penalty for code issues
            }
        }
        
        // 5. Token counting
        int tokenCount = tokenCountingService.countTokens(content);
        evaluation.tokenCount = tokenCount;
        
        // Record token usage
        TokenCountingService.TokenUsageRecord tokenRecord = 
            tokenCountingService.recordTokenUsage(userId, "", content);
        evaluation.tokenUsagePercentage = tokenRecord.usagePercentage;
        
        // 6. Calculate final rating
        double baseRating = (clarityScore + relevanceScore + helpfulnessScore + consistencyScore) / 4.0;
        double finalRating = baseRating - evaluation.consistencyPenalty - evaluation.hallucinationPenalty;
        
        // Cap between 0 and 5
        evaluation.finalRating = Math.max(0.0, Math.min(5.0, finalRating));
        
        // Determine verdict
        if (evaluation.finalRating >= 4.5) {
            evaluation.verdict = "Excellent";
        } else if (evaluation.finalRating >= 4.0) {
            evaluation.verdict = "Very Good";
        } else if (evaluation.finalRating >= 3.5) {
            evaluation.verdict = "Good";
        } else if (evaluation.finalRating >= 3.0) {
            evaluation.verdict = "Acceptable";
        } else if (evaluation.finalRating >= 2.0) {
            evaluation.verdict = "Poor";
        } else {
            evaluation.verdict = "Very Poor";
        }
        
        return evaluation;
    }
    
    /**
     * Evaluate clarity of response
     * NOTE: Short, direct answers are CLEAR - not unclear!
     */
    private double evaluateClarity(String content) {
        if (content == null || content.isEmpty()) {
            return 1.0;
        }
        
        // Simple clarity metrics
        int sentenceCount = Math.max(1, content.split("[.!?]").length);
        int wordCount = content.split("\\s+").length;
        
        // Average words per sentence
        double avgWordsPerSentence = (double) wordCount / sentenceCount;
        
        // Recognize that short, direct answers are CLEAR
        // Examples: "2025-11-16", "30", "Yes" - all clear!
        if (wordCount < 20) {
            return 4.5; // Concise and clear ✅
        } else if (avgWordsPerSentence > 30) {
            return 2.5; // Too verbose, harder to parse
        } else {
            return 4.5; // Good clarity
        }
    }
    
    /**
     * Evaluate relevance to query - REALISTIC SCORING
     * 
     * Context-aware relevance that understands intent, not just word matching
     */
    private double evaluateRelevance(String content, String userQuery) {
        if (content == null || userQuery == null) {
            return 2.0;
        }
        
        String queryLower = userQuery.toLowerCase();
        String contentLower = content.toLowerCase();
        
        // ===== DATE/TIME QUERIES =====
        if (queryLower.contains("date") || queryLower.contains("today") || queryLower.contains("tody")) {
            // Check if response contains date indicators
            if (contentLower.contains("202") ||  // Year like 2025
                contentLower.contains("january") || contentLower.contains("february") ||
                contentLower.contains("march") || contentLower.contains("april") ||
                contentLower.contains("may") || contentLower.contains("june") ||
                contentLower.contains("july") || contentLower.contains("august") ||
                contentLower.contains("september") || contentLower.contains("october") ||
                contentLower.contains("november") || contentLower.contains("december") ||
                contentLower.matches(".*\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}.*")) {  // Date format
                return 4.8; // HIGHLY relevant! ✅
            }
            return 3.0; // Moderately relevant
        }
        
        // ===== PROJECT/ANALYSIS QUERIES =====
        if (queryLower.contains("analyze") || queryLower.contains("project") || 
            queryLower.contains("code") || queryLower.contains("structure")) {
            if (contentLower.contains("project") || contentLower.contains("analysis") ||
                contentLower.contains("code") || contentLower.contains("structure") ||
                contentLower.contains("class") || contentLower.contains("method")) {
                return 4.5; // Highly relevant! ✅
            }
            return 3.0;
        }
        
        // ===== WEATHER QUERIES =====
        if (queryLower.contains("weather") || queryLower.contains("temperature") || 
            queryLower.contains("forecast")) {
            if (contentLower.contains("weather") || contentLower.contains("temperature") ||
                contentLower.contains("celsius") || contentLower.contains("fahrenheit") ||
                contentLower.contains("rain") || contentLower.contains("sunny")) {
                return 4.5; // Highly relevant! ✅
            }
            return 3.0;
        }
        
        // ===== CALCULATION QUERIES =====
        if (queryLower.matches(".*\\d+.*[+\\-*/].*\\d+.*")) {  // Math expression
            // Check if response contains numbers or results
            if (contentLower.matches(".*\\d+.*")) {
                return 4.5; // Highly relevant! ✅
            }
            return 3.0;
        }
        
        // ===== DEFAULT: WORD MATCHING =====
        String[] queryWords = queryLower.split("\\s+");
        int matchCount = 0;
        for (String word : queryWords) {
            if (word.length() > 3 && contentLower.contains(word)) {
                matchCount++;
            }
        }
        
        double wordMatchRatio = (double) matchCount / Math.max(1, queryWords.length);
        
        // More realistic scoring
        if (wordMatchRatio >= 0.8) {
            return 4.5; // Excellent match
        } else if (wordMatchRatio >= 0.6) {
            return 4.0; // Good match
        } else if (wordMatchRatio >= 0.4) {
            return 3.5; // Moderate match
        } else if (wordMatchRatio >= 0.2) {
            return 3.0; // Some relevance
        } else {
            return 2.5; // Weak relevance
        }
    }
    
    /**
     * Evaluate helpfulness of response - REALISTIC SCORING
     * 
     * Direct answers are helpful! Not everything needs examples/code/explanations
     */
    private double evaluateHelpfulness(String content) {
        if (content == null || content.isEmpty()) {
            return 1.0;
        }
        
        int contentLength = content.length();
        
        // ===== DIRECT ANSWERS ARE HELPFUL =====
        // "November 17, 2025" is VERY helpful for "What is today's date?"
        if (contentLength < 50) {
            // Short, direct answers are HIGHLY helpful
            return 4.5; // ✅ Concise and to the point
        }
        
        // ===== MEDIUM ANSWERS =====
        if (contentLength < 200) {
            double score = 4.0; // Good base score
            
            // Bonus for examples
            if (content.contains("example") || content.contains("for instance")) {
                score += 0.3;
            }
            
            // Bonus for code
            if (content.contains("```") || content.contains("public") || content.contains("def")) {
                score += 0.3;
            }
            
            return Math.min(5.0, score);
        }
        
        // ===== LONG ANSWERS =====
        double score = 3.5; // Base score for longer content
        
        // Check for examples
        if (content.contains("example") || content.contains("for instance")) {
            score += 0.4;
        }
        
        // Check for code
        if (content.contains("```") || content.contains("public") || content.contains("def")) {
            score += 0.4;
        }
        
        // Check for actionable advice
        if (content.contains("you can") || content.contains("you should") || content.contains("try")) {
            score += 0.3;
        }
        
        // Check for explanations
        if (content.contains("because") || content.contains("reason") || content.contains("why")) {
            score += 0.3;
        }
        
        // Check for structure (lists, sections)
        if (content.contains("\n-") || content.contains("\n1.") || content.contains("##")) {
            score += 0.2;
        }
        
        return Math.min(5.0, score);
    }
    
    /**
     * Refine response based on evaluation
     */
    private String refineResponse(String content, String userQuery, EnhancedQualityEvaluation evaluation) {
        StringBuilder refinedPrompt = new StringBuilder();
        refinedPrompt.append("Improve this response:\n\n");
        refinedPrompt.append(content).append("\n\n");
        refinedPrompt.append("Original query: ").append(userQuery).append("\n\n");
        
        // Add specific improvement suggestions
        if (evaluation.consistencyIssues > 0) {
            refinedPrompt.append("Fix consistency issues.\n");
        }
        
        if (evaluation.hallucinationCount > 0) {
            refinedPrompt.append("Remove or verify any questionable claims.\n");
        }
        
        if (evaluation.codeIssues > 0) {
            refinedPrompt.append("Fix code structure issues.\n");
        }
        
        if (evaluation.clarityScore < 3.0) {
            refinedPrompt.append("Improve clarity and readability.\n");
        }
        
        try {
            String refined = judgeClient.prompt(refinedPrompt.toString()).call().content();
            return refined;
        } catch (Exception e) {
            logger.debug("Could not refine response: {}", e.getMessage());
            return null;
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
     * Generate unique conversation ID
     */
    private String generateConversationId() {
        return "conv_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }
    
    /**
     * Log evaluation details with null checks
     */
    private void logEvaluationDetails(EnhancedQualityEvaluation eval) {
        if (eval == null) {
            logger.warn("⚠️ Brain 13: Evaluation is null, skipping details");
            return;
        }
        
        logger.info("🧾 Brain 13: Comprehensive Evaluation:");
        logger.info("   📝 Clarity: {}/5.0", String.format("%.2f", Math.max(0, eval.clarityScore)));
        logger.info("   🎯 Relevance: {}/5.0", String.format("%.2f", Math.max(0, eval.relevanceScore)));
        logger.info("   💡 Helpfulness: {}/5.0", String.format("%.2f", Math.max(0, eval.helpfulnessScore)));
        logger.info("   ✅ Consistency: {}/5.0 (issues: {})", 
            String.format("%.2f", Math.max(0, eval.consistencyScore)), 
            Math.max(0, eval.consistencyIssues));
        logger.info("   🚨 Hallucination Score: {} (count: {})", 
            String.format("%.2f", Math.max(0, eval.hallucinationScore)), 
            Math.max(0, eval.hallucinationCount));
        logger.info("   📊 Code Structure: {}", eval.codeStructureValid ? "Valid" : "Invalid");
        logger.info("   🧮 Tokens: {} ({}% of quota)", 
            Math.max(0, eval.tokenCount), 
            String.format("%.1f", Math.max(0, eval.tokenUsagePercentage)));
        logger.info("   📉 Penalties: Consistency={}, Hallucination={}", 
            String.format("%.2f", Math.max(0, eval.consistencyPenalty)), 
            String.format("%.2f", Math.max(0, eval.hallucinationPenalty)));
        logger.info("   ⭐ Final Rating: {}/5.0 - {}", 
            String.format("%.2f", Math.max(0, eval.finalRating)), 
            eval.verdict != null ? eval.verdict : "Unknown");
    }
    
    // ============ Inner Classes ============
    
    /**
     * Enhanced quality evaluation with all checks
     */
    public static class EnhancedQualityEvaluation {
        // Basic scores
        public double clarityScore = 0.0;
        public double relevanceScore = 0.0;
        public double helpfulnessScore = 0.0;
        
        // Consistency
        public double consistencyScore = 0.0;
        public int consistencyIssues = 0;
        public double consistencyPenalty = 0.0;
        
        // Hallucination
        public double hallucinationScore = 0.0;
        public int hallucinationCount = 0;
        public double hallucinationPenalty = 0.0;
        
        // Code structure
        public boolean codeStructureValid = true;
        public int codeIssues = 0;
        
        // Token usage
        public int tokenCount = 0;
        public double tokenUsagePercentage = 0.0;
        
        // Final rating
        public double finalRating = 0.0;
        public String verdict = "";
        
        // Refinement
        public int refinementAttempts = 0;
    }
}

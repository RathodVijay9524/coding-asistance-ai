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

@Component
public class MultiCriteriaJudgeAdvisor implements CallAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(MultiCriteriaJudgeAdvisor.class);
    
    private final ChatClient judgeClient;
    private static final double MIN_OVERALL_SCORE = 3.5; // Higher threshold
    private static final double MIN_FACTUAL_SCORE = 3.0; // Factual accuracy threshold

    public MultiCriteriaJudgeAdvisor(OpenAiChatModel chatModel) {
        this.judgeClient = ChatClient.builder(chatModel).build();
    }

    @Override
    public String getName() {
        return "MultiCriteriaJudgeAdvisor";
    }

    @Override
    public int getOrder() {
        return 1000; // Run LAST - final quality gate
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("🧾 Brain 3 (Multi-Criteria Judge): Comprehensive evaluation...");
        
        try {
            // Get the response from previous advisors
            ChatClientResponse response = chain.nextCall(request);
            
            if (response == null || response.chatResponse() == null) {
                logger.warn("⚠️ Brain 3: No response to evaluate");
                return response;
            }

            String content = response.chatResponse().getResult().getOutput().getText();
            if (content == null || content.trim().isEmpty()) {
                logger.warn("⚠️ Brain 3: Empty response content");
                return response;
            }

            String userQuery = extractUserQuery(request);
            
            // Multi-criteria evaluation
            QualityAssessment assessment = evaluateMultipleCriteria(content, userQuery);
            
            logger.info("🧾 Brain 3: Multi-Criteria Assessment:");
            logger.info("   📝 Clarity: {:.1f}/5.0", assessment.clarityScore);
            logger.info("   🎯 Relevance: {:.1f}/5.0", assessment.relevanceScore);
            logger.info("   ✅ Factual Accuracy: {:.1f}/5.0", assessment.factualScore);
            logger.info("   💡 Helpfulness: {:.1f}/5.0", assessment.helpfulnessScore);
            logger.info("   📊 Overall Score: {:.1f}/5.0", assessment.overallScore);
            
            // Check if response meets quality thresholds
            if (assessment.overallScore < MIN_OVERALL_SCORE) {
                logger.warn("⚠️ Brain 3: Overall quality below threshold ({:.1f} < {:.1f})", 
                    assessment.overallScore, MIN_OVERALL_SCORE);
                logQualityIssues(assessment);
            }
            
            if (assessment.factualScore < MIN_FACTUAL_SCORE) {
                logger.error("❌ Brain 3: FACTUAL ACCURACY CONCERN ({:.1f} < {:.1f})", 
                    assessment.factualScore, MIN_FACTUAL_SCORE);
                logger.error("🚨 Brain 3: Response may contain incorrect information!");
                
                // For factual issues, we should flag this prominently
                if (assessment.factualIssues != null && !assessment.factualIssues.isEmpty()) {
                    logger.error("🔍 Brain 3: Identified Issues: {}", assessment.factualIssues);
                }
            }
            
            if (assessment.overallScore >= MIN_OVERALL_SCORE && assessment.factualScore >= MIN_FACTUAL_SCORE) {
                logger.info("✅ Brain 3: Response meets all quality criteria");
            }
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Brain 3: Error in multi-criteria evaluation - {}", e.getMessage());
            return chain.nextCall(request);
        }
    }

    private QualityAssessment evaluateMultipleCriteria(String content, String userQuery) {
        try {
            String evaluationPrompt = String.format("""
                You are an expert quality judge. Evaluate this AI response across multiple criteria.
                
                User Query: "%s"
                AI Response: "%s"
                
                Please evaluate the response on these 4 criteria (1-5 scale each):
                
                1. CLARITY (1-5):
                   - Is the response well-structured and easy to understand?
                   - Are explanations clear and logical?
                   - Is the language appropriate?
                
                2. RELEVANCE (1-5):
                   - Does it directly address the user's question?
                   - Is the information on-topic?
                   - Does it provide what the user was looking for?
                
                3. FACTUAL ACCURACY (1-5):
                   - Are the facts, numbers, and technical details correct?
                   - Are version numbers, dates, and specifications accurate?
                   - Is there any obviously incorrect information?
                   - CRITICAL: If you detect any factual errors, rate this LOW (1-2)
                
                4. HELPFULNESS (1-5):
                   - Would this response actually help the user?
                   - Is it actionable and practical?
                   - Does it provide sufficient detail?
                
                Also identify any specific issues:
                - Factual errors (version numbers, dates, technical specs)
                - Outdated information
                - Contradictions or inconsistencies
                - Missing important details
                
                Respond in this exact format:
                CLARITY: [1-5]
                RELEVANCE: [1-5]
                FACTUAL: [1-5]
                HELPFULNESS: [1-5]
                OVERALL: [1-5]
                ISSUES: [list any specific problems, especially factual errors]
                """, userQuery, content);

            String evaluation = judgeClient.prompt(evaluationPrompt).call().chatResponse()
                .getResult().getOutput().getText();
            
            return parseQualityAssessment(evaluation);
            
        } catch (Exception e) {
            logger.warn("⚠️ Brain 3: Multi-criteria evaluation failed - {}", e.getMessage());
            return createFallbackAssessment();
        }
    }

    private QualityAssessment parseQualityAssessment(String evaluation) {
        QualityAssessment assessment = new QualityAssessment();
        
        try {
            String[] lines = evaluation.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("CLARITY:")) {
                    assessment.clarityScore = parseScore(line.substring(8).trim());
                } else if (line.startsWith("RELEVANCE:")) {
                    assessment.relevanceScore = parseScore(line.substring(10).trim());
                } else if (line.startsWith("FACTUAL:")) {
                    assessment.factualScore = parseScore(line.substring(8).trim());
                } else if (line.startsWith("HELPFULNESS:")) {
                    assessment.helpfulnessScore = parseScore(line.substring(12).trim());
                } else if (line.startsWith("OVERALL:")) {
                    assessment.overallScore = parseScore(line.substring(8).trim());
                } else if (line.startsWith("ISSUES:")) {
                    assessment.factualIssues = line.substring(7).trim();
                }
            }
            
            // Calculate overall score if not provided
            if (assessment.overallScore == 0.0) {
                assessment.overallScore = (assessment.clarityScore + assessment.relevanceScore + 
                                        assessment.factualScore + assessment.helpfulnessScore) / 4.0;
            }
            
        } catch (Exception e) {
            logger.warn("⚠️ Brain 3: Failed to parse quality assessment - {}", e.getMessage());
            return createFallbackAssessment();
        }
        
        return assessment;
    }

    private double parseScore(String scoreStr) {
        try {
            // Extract just the number from strings like "4.5" or "4/5" or "4 out of 5"
            String cleanScore = scoreStr.replaceAll("[^0-9.]", "");
            if (cleanScore.isEmpty()) return 3.0; // Default
            
            double score = Double.parseDouble(cleanScore);
            return Math.max(1.0, Math.min(5.0, score)); // Clamp to 1-5 range
        } catch (Exception e) {
            return 3.0; // Default middle score
        }
    }

    private QualityAssessment createFallbackAssessment() {
        QualityAssessment assessment = new QualityAssessment();
        assessment.clarityScore = 3.0;
        assessment.relevanceScore = 3.0;
        assessment.factualScore = 3.0; // Conservative - assume unknown factual accuracy
        assessment.helpfulnessScore = 3.0;
        assessment.overallScore = 3.0;
        assessment.factualIssues = "Unable to verify - evaluation failed";
        return assessment;
    }

    private void logQualityIssues(QualityAssessment assessment) {
        if (assessment.clarityScore < 3.0) {
            logger.warn("📝 Issue: Poor clarity/structure (score: {:.1f})", assessment.clarityScore);
        }
        if (assessment.relevanceScore < 3.0) {
            logger.warn("🎯 Issue: Low relevance to query (score: {:.1f})", assessment.relevanceScore);
        }
        if (assessment.factualScore < 3.0) {
            logger.error("❌ Issue: Factual accuracy concerns (score: {:.1f})", assessment.factualScore);
        }
        if (assessment.helpfulnessScore < 3.0) {
            logger.warn("💡 Issue: Limited helpfulness (score: {:.1f})", assessment.helpfulnessScore);
        }
        
        if (assessment.factualIssues != null && !assessment.factualIssues.isEmpty() && 
            !assessment.factualIssues.equals("Unable to verify - evaluation failed")) {
            logger.warn("🔍 Specific Issues: {}", assessment.factualIssues);
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

    private static class QualityAssessment {
        double clarityScore = 0.0;
        double relevanceScore = 0.0;
        double factualScore = 0.0;
        double helpfulnessScore = 0.0;
        double overallScore = 0.0;
        String factualIssues = "";
    }
}

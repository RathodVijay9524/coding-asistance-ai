package com.vijay.service;

import com.vijay.dto.UserMentalModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 🧠 Mental State Inferencer
 * 
 * Infers user's mental state from conversation patterns:
 * - Knowledge level detection
 * - Confusion signal detection
 * - Frustration pattern recognition
 * - Expertise area identification
 * - Learning style preferences
 */
@Service
public class MentalStateInferencer {
    
    private static final Logger logger = LoggerFactory.getLogger(MentalStateInferencer.class);
    
    private final Map<String, UserMentalModel> mentalModels = new ConcurrentHashMap<>();
    
    // Knowledge level indicators
    private static final Map<String, Integer> BEGINNER_KEYWORDS = new HashMap<>();
    private static final Map<String, Integer> INTERMEDIATE_KEYWORDS = new HashMap<>();
    private static final Map<String, Integer> EXPERT_KEYWORDS = new HashMap<>();
    
    static {
        // Beginner indicators
        BEGINNER_KEYWORDS.put("what is", 1);
        BEGINNER_KEYWORDS.put("how do i", 1);
        BEGINNER_KEYWORDS.put("can you explain", 1);
        BEGINNER_KEYWORDS.put("i don't understand", 2);
        BEGINNER_KEYWORDS.put("basic", 1);
        BEGINNER_KEYWORDS.put("simple", 1);
        BEGINNER_KEYWORDS.put("beginner", 2);
        BEGINNER_KEYWORDS.put("newbie", 2);
        BEGINNER_KEYWORDS.put("new to", 1);
        
        // Intermediate indicators
        INTERMEDIATE_KEYWORDS.put("how can i", 1);
        INTERMEDIATE_KEYWORDS.put("best practice", 1);
        INTERMEDIATE_KEYWORDS.put("optimize", 1);
        INTERMEDIATE_KEYWORDS.put("refactor", 1);
        INTERMEDIATE_KEYWORDS.put("pattern", 1);
        INTERMEDIATE_KEYWORDS.put("architecture", 1);
        INTERMEDIATE_KEYWORDS.put("intermediate", 2);
        
        // Expert indicators
        EXPERT_KEYWORDS.put("edge case", 1);
        EXPERT_KEYWORDS.put("performance", 1);
        EXPERT_KEYWORDS.put("scalability", 1);
        EXPERT_KEYWORDS.put("trade-off", 1);
        EXPERT_KEYWORDS.put("algorithm", 1);
        EXPERT_KEYWORDS.put("implementation detail", 1);
        EXPERT_KEYWORDS.put("expert", 2);
        EXPERT_KEYWORDS.put("advanced", 1);
    }
    
    /**
     * Get or create mental model for user
     */
    public UserMentalModel getMentalModel(String userId) {
        return mentalModels.computeIfAbsent(userId, k -> {
            logger.info("🧠 Creating new mental model for user: {}", userId);
            return new UserMentalModel(userId);
        });
    }
    
    /**
     * Infer mental state from user query
     */
    public void inferFromQuery(String userId, String query) {
        logger.debug("🔍 Inferring mental state from query: {}", query);
        
        UserMentalModel model = getMentalModel(userId);
        String lowerQuery = query.toLowerCase();
        
        // Detect knowledge level
        int detectedLevel = detectKnowledgeLevel(lowerQuery);
        if (detectedLevel > 0) {
            model.setKnowledgeLevel(detectedLevel);
        }
        
        // Detect confusion signals
        int confusionScore = detectConfusion(lowerQuery);
        model.setConfusionLevel(confusionScore);
        
        // Detect frustration signals
        int frustrationScore = detectFrustration(lowerQuery);
        model.setFrustrationLevel(frustrationScore);
        
        // Detect expertise areas
        detectExpertiseAreas(lowerQuery, model);
        
        // Detect knowledge gaps
        detectKnowledgeGaps(lowerQuery, model);
        
        // Detect learning style
        detectLearningStyle(lowerQuery, model);
        
        // Update confidence
        updateConfidence(model);
        
        logger.debug("🧠 Mental state inferred: {}", model.getMentalStateSummary());
    }
    
    /**
     * Detect knowledge level from query
     */
    private int detectKnowledgeLevel(String lowerQuery) {
        int beginnerScore = 0;
        int intermediateScore = 0;
        int expertScore = 0;
        
        for (Map.Entry<String, Integer> entry : BEGINNER_KEYWORDS.entrySet()) {
            if (lowerQuery.contains(entry.getKey())) {
                beginnerScore += entry.getValue();
            }
        }
        
        for (Map.Entry<String, Integer> entry : INTERMEDIATE_KEYWORDS.entrySet()) {
            if (lowerQuery.contains(entry.getKey())) {
                intermediateScore += entry.getValue();
            }
        }
        
        for (Map.Entry<String, Integer> entry : EXPERT_KEYWORDS.entrySet()) {
            if (lowerQuery.contains(entry.getKey())) {
                expertScore += entry.getValue();
            }
        }
        
        if (expertScore > intermediateScore && expertScore > beginnerScore) {
            return 4; // Expert
        } else if (intermediateScore > beginnerScore) {
            return 3; // Intermediate
        } else if (beginnerScore > 0) {
            return 2; // Beginner
        }
        
        return 0; // Cannot determine
    }
    
    /**
     * Detect confusion signals
     */
    private int detectConfusion(String lowerQuery) {
        int confusionScore = 0;
        
        // Direct confusion indicators
        if (lowerQuery.contains("confused")) confusionScore += 30;
        if (lowerQuery.contains("don't understand")) confusionScore += 40;
        if (lowerQuery.contains("unclear")) confusionScore += 25;
        if (lowerQuery.contains("lost")) confusionScore += 30;
        if (lowerQuery.contains("??")) confusionScore += 20;
        if (Pattern.compile("\\?{2,}").matcher(lowerQuery).find()) confusionScore += 25;
        
        // Indirect confusion indicators
        if (lowerQuery.contains("why")) confusionScore += 10;
        if (lowerQuery.contains("what does")) confusionScore += 10;
        if (lowerQuery.contains("how does")) confusionScore += 10;
        if (lowerQuery.contains("can you explain")) confusionScore += 15;
        
        // Very short query might indicate confusion
        if (lowerQuery.length() < 20) confusionScore += 5;
        
        return Math.min(confusionScore, 100);
    }
    
    /**
     * Detect frustration signals
     */
    private int detectFrustration(String lowerQuery) {
        int frustrationScore = 0;
        
        // Direct frustration indicators
        if (lowerQuery.contains("frustrated")) frustrationScore += 40;
        if (lowerQuery.contains("annoyed")) frustrationScore += 35;
        if (lowerQuery.contains("stuck")) frustrationScore += 30;
        if (lowerQuery.contains("blocked")) frustrationScore += 30;
        if (lowerQuery.contains("not working")) frustrationScore += 25;
        if (lowerQuery.contains("error")) frustrationScore += 15;
        if (lowerQuery.contains("problem")) frustrationScore += 15;
        if (lowerQuery.contains("issue")) frustrationScore += 10;
        
        // Punctuation indicators
        if (Pattern.compile("!{2,}").matcher(lowerQuery).find()) frustrationScore += 20;
        if (lowerQuery.equals(lowerQuery.toUpperCase()) && lowerQuery.length() > 3) frustrationScore += 25;
        
        return Math.min(frustrationScore, 100);
    }
    
    /**
     * Detect expertise areas
     */
    private void detectExpertiseAreas(String lowerQuery, UserMentalModel model) {
        // Frontend technologies
        if (lowerQuery.contains("react") || lowerQuery.contains("vue") || lowerQuery.contains("angular")) {
            model.addExpertiseArea("frontend");
        }
        
        // Backend technologies
        if (lowerQuery.contains("spring") || lowerQuery.contains("api") || lowerQuery.contains("database")) {
            model.addExpertiseArea("backend");
        }
        
        // DevOps
        if (lowerQuery.contains("docker") || lowerQuery.contains("kubernetes") || lowerQuery.contains("devops")) {
            model.addExpertiseArea("devops");
        }
        
        // Security
        if (lowerQuery.contains("security") || lowerQuery.contains("encryption") || lowerQuery.contains("auth")) {
            model.addExpertiseArea("security");
        }
        
        // Performance
        if (lowerQuery.contains("performance") || lowerQuery.contains("optimization") || lowerQuery.contains("cache")) {
            model.addExpertiseArea("performance");
        }
        
        // Architecture
        if (lowerQuery.contains("architecture") || lowerQuery.contains("design pattern")) {
            model.addExpertiseArea("architecture");
        }
    }
    
    /**
     * Detect knowledge gaps
     */
    private void detectKnowledgeGaps(String lowerQuery, UserMentalModel model) {
        if (lowerQuery.contains("don't know") || lowerQuery.contains("don't understand")) {
            // Extract what they don't understand
            if (lowerQuery.contains("react")) model.addKnowledgeGap("react");
            if (lowerQuery.contains("spring")) model.addKnowledgeGap("spring");
            if (lowerQuery.contains("database")) model.addKnowledgeGap("database");
            if (lowerQuery.contains("api")) model.addKnowledgeGap("api");
        }
    }
    
    /**
     * Detect learning style preferences
     */
    private void detectLearningStyle(String lowerQuery, UserMentalModel model) {
        if (lowerQuery.contains("show me code") || lowerQuery.contains("example")) {
            model.setLearningStyle("code-heavy");
        } else if (lowerQuery.contains("step by step") || lowerQuery.contains("steps")) {
            model.setLearningStyle("step-by-step");
        } else if (lowerQuery.contains("explain") || lowerQuery.contains("why")) {
            model.setLearningStyle("textual");
        } else if (lowerQuery.contains("diagram") || lowerQuery.contains("visual")) {
            model.setLearningStyle("visual");
        }
    }
    
    /**
     * Update confidence in mental model
     */
    private void updateConfidence(UserMentalModel model) {
        // Confidence increases with more interactions
        double currentConfidence = model.getConfidence();
        double newConfidence = Math.min(currentConfidence + 0.05, 0.95);
        model.setConfidence(newConfidence);
    }
    
    /**
     * Get mental model summary for logging
     */
    public String getMentalModelSummary(String userId) {
        UserMentalModel model = getMentalModel(userId);
        return model.getMentalStateSummary();
    }
}

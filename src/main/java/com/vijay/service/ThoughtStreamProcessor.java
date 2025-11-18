package com.vijay.service;

import com.vijay.dto.ThoughtStreamCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 🧠 Thought Stream Processor
 * 
 * The "cursor system" that guides attention in the thought stream.
 * Processes incoming queries to determine:
 * - What to focus on
 * - What to ignore
 * - Whether to use slow reasoning or fast recall
 * - Which brains to activate
 * 
 * Sits BEFORE the Query Planner (Order: -1)
 */
@Service
public class ThoughtStreamProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(ThoughtStreamProcessor.class);
    
    public ThoughtStreamProcessor() {
        logger.info("🧠 Thought Stream Processor initialized - Cursor system for attention guidance");
    }
    
    /**
     * Process query and create thought stream cursor
     */
    public ThoughtStreamCursor processQuery(String queryId, String query) {
        logger.debug("🎯 Processing query through thought stream - Query: {}", query);
        
        ThoughtStreamCursor cursor = new ThoughtStreamCursor(queryId);
        
        // Analyze query characteristics
        double complexity = analyzeComplexity(query);
        double ambiguity = analyzeAmbiguity(query);
        
        cursor.setComplexity(complexity);
        cursor.setAmbiguity(ambiguity);
        
        // Determine focus area
        String focusArea = determineFocusArea(query);
        cursor.setFocusArea(focusArea);
        
        // Determine ignore area
        String ignoreArea = determineIgnoreArea(query);
        cursor.setIgnoreArea(ignoreArea);
        
        // Determine reasoning strategy
        ThoughtStreamCursor.ReasoningStrategy strategy = determineReasoningStrategy(complexity, ambiguity);
        cursor.setStrategy(strategy);
        
        // Determine if slow reasoning needed
        boolean needsSlowReasoning = complexity > 0.7 || ambiguity > 0.6;
        cursor.setNeedsSlowReasoning(needsSlowReasoning);
        
        // Determine if fast recall needed
        boolean needsFastRecall = complexity < 0.3 && ambiguity < 0.3;
        cursor.setNeedsFastRecall(needsFastRecall);
        
        // Select relevant brains
        String[] relevantBrains = selectRelevantBrains(query, focusArea, complexity);
        cursor.setRelevantBrains(relevantBrains);
        
        // Set confidence
        double confidence = calculateConfidence(complexity, ambiguity);
        cursor.setConfidence(confidence);
        
        logger.info("✅ Thought stream cursor created - {}", cursor.getSummary());
        
        return cursor;
    }
    
    /**
     * Analyze query complexity (0-1 scale)
     */
    private double analyzeComplexity(String query) {
        double complexity = 0.5;
        
        // Length-based complexity
        if (query.length() < 50) complexity -= 0.2;
        if (query.length() > 200) complexity += 0.2;
        
        // Word count
        int wordCount = query.split("\\s+").length;
        if (wordCount < 5) complexity -= 0.15;
        if (wordCount > 20) complexity += 0.15;
        
        // Keyword-based complexity
        if (query.toLowerCase().contains("why") || query.toLowerCase().contains("how")) {
            complexity += 0.2;
        }
        if (query.toLowerCase().contains("debug") || query.toLowerCase().contains("error")) {
            complexity += 0.15;
        }
        if (query.toLowerCase().contains("optimize") || query.toLowerCase().contains("design")) {
            complexity += 0.25;
        }
        
        return Math.max(0, Math.min(1, complexity));
    }
    
    /**
     * Analyze query ambiguity (0-1 scale)
     */
    private double analyzeAmbiguity(String query) {
        double ambiguity = 0.5;
        
        // Punctuation-based ambiguity
        if (!query.endsWith("?") && !query.endsWith(".")) {
            ambiguity += 0.1;
        }
        
        // Keyword-based ambiguity
        if (query.toLowerCase().contains("maybe") || query.toLowerCase().contains("possibly")) {
            ambiguity += 0.2;
        }
        if (query.toLowerCase().contains("or") && !query.toLowerCase().contains("error")) {
            ambiguity += 0.15;
        }
        
        // Specificity-based ambiguity
        if (query.toLowerCase().contains("something") || query.toLowerCase().contains("anything")) {
            ambiguity += 0.25;
        }
        
        // Clear keywords reduce ambiguity
        if (query.toLowerCase().contains("specifically") || query.toLowerCase().contains("exactly")) {
            ambiguity -= 0.2;
        }
        
        return Math.max(0, Math.min(1, ambiguity));
    }
    
    /**
     * Determine focus area
     */
    private String determineFocusArea(String query) {
        String lower = query.toLowerCase();
        
        if (lower.contains("code") || lower.contains("implement") || lower.contains("function")) {
            return "code";
        } else if (lower.contains("debug") || lower.contains("error") || lower.contains("fix")) {
            return "debugging";
        } else if (lower.contains("explain") || lower.contains("understand")) {
            return "explanation";
        } else if (lower.contains("optimize") || lower.contains("improve")) {
            return "optimization";
        } else if (lower.contains("design") || lower.contains("architecture")) {
            return "design";
        } else if (lower.contains("test") || lower.contains("test case")) {
            return "testing";
        } else {
            return "general";
        }
    }
    
    /**
     * Determine ignore area
     */
    private String determineIgnoreArea(String query) {
        String lower = query.toLowerCase();
        
        if (lower.contains("not") || lower.contains("don't")) {
            if (lower.contains("performance")) return "performance";
            if (lower.contains("security")) return "security";
            if (lower.contains("documentation")) return "documentation";
        }
        
        return "none";
    }
    
    /**
     * Determine reasoning strategy
     */
    private ThoughtStreamCursor.ReasoningStrategy determineReasoningStrategy(double complexity, double ambiguity) {
        if (complexity < 0.3 && ambiguity < 0.3) {
            return ThoughtStreamCursor.ReasoningStrategy.FAST_RECALL;
        } else if (complexity < 0.5 && ambiguity < 0.5) {
            return ThoughtStreamCursor.ReasoningStrategy.FAST_REASONING;
        } else if (complexity < 0.7 && ambiguity < 0.7) {
            return ThoughtStreamCursor.ReasoningStrategy.BALANCED;
        } else if (complexity < 0.85 || ambiguity < 0.85) {
            return ThoughtStreamCursor.ReasoningStrategy.SLOW_REASONING;
        } else {
            return ThoughtStreamCursor.ReasoningStrategy.VERY_SLOW_REASONING;
        }
    }
    
    /**
     * Select relevant brains based on query characteristics
     */
    private String[] selectRelevantBrains(String query, String focusArea, double complexity) {
        Set<String> brains = new HashSet<>();
        
        // Always include these
        brains.add("LocalQueryPlanner");
        brains.add("ConversationMemory");
        
        // Based on focus area
        switch (focusArea) {
            case "code":
                brains.add("CodeRetriever");
                brains.add("ErrorPrediction");
                break;
            case "debugging":
                brains.add("ErrorPrediction");
                brains.add("CodeRetriever");
                brains.add("KnowledgeGraph");
                break;
            case "optimization":
                brains.add("ChainOfThought");
                brains.add("KnowledgeGraph");
                break;
            case "design":
                brains.add("ChainOfThought");
                brains.add("KnowledgeGraph");
                break;
        }
        
        // Based on complexity
        if (complexity > 0.7) {
            brains.add("ChainOfThought");
            brains.add("MentalSimulator");
        }
        
        // Emotional and personality brains always active
        brains.add("EmotionalContext");
        brains.add("Personality");
        
        return brains.toArray(new String[0]);
    }
    
    /**
     * Calculate confidence in routing decision
     */
    private double calculateConfidence(double complexity, double ambiguity) {
        // Higher confidence when complexity and ambiguity are not extreme
        double confidence = 1.0 - (Math.abs(complexity - 0.5) * 0.2) - (Math.abs(ambiguity - 0.5) * 0.2);
        return Math.max(0.5, Math.min(1.0, confidence));
    }
    
    /**
     * Get cursor routing summary
     */
    public String getRoutingSummary(ThoughtStreamCursor cursor) {
        StringBuilder summary = new StringBuilder();
        summary.append("🧠 THOUGHT STREAM ROUTING:\n");
        summary.append("==========================\n");
        summary.append(String.format("Focus: %s\n", cursor.getFocusArea()));
        summary.append(String.format("Ignore: %s\n", cursor.getIgnoreArea()));
        summary.append(String.format("Strategy: %s\n", cursor.getStrategy().getDisplayName()));
        summary.append(String.format("Complexity: %.2f | Ambiguity: %.2f\n", cursor.getComplexity(), cursor.getAmbiguity()));
        summary.append(String.format("Slow Reasoning: %s | Fast Recall: %s\n", 
            cursor.isNeedsSlowReasoning(), cursor.isNeedsFastRecall()));
        summary.append(String.format("Relevant Brains: %s\n", String.join(", ", cursor.getRelevantBrains())));
        summary.append(String.format("Confidence: %.2f\n", cursor.getConfidence()));
        
        return summary.toString();
    }
}

package com.vijay.service;

import com.vijay.dto.ThoughtStreamCursor;
import com.vijay.dto.VisualAttentionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 👁️ Visual Attention Engine
 * 
 * Calculates visual attention for queries
 * - Primary focus (main entity)
 * - Secondary focus (related entities)
 * - Context window (relevant code blocks)
 * - Relevance scoring
 */
@Component
public class VisualAttentionEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(VisualAttentionEngine.class);
    
    /**
     * Calculate visual attention state for a query
     */
    public VisualAttentionState calculateAttention(String query, ThoughtStreamCursor cursor) {
        if (query == null || query.isEmpty() || cursor == null) {
            logger.warn("⚠️ Invalid input to VisualAttentionEngine");
            return createDefaultAttention();
        }
        
        logger.debug("👁️ Calculating visual attention for: {}", query);
        
        VisualAttentionState attention = new VisualAttentionState();
        
        // 1. Calculate primary focus
        String primaryFocus = calculatePrimaryFocus(query, cursor);
        attention.setPrimaryFocus(primaryFocus);
        
        // 2. Calculate primary focus score
        double primaryScore = calculatePrimaryFocusScore(query, primaryFocus);
        attention.setPrimaryFocusScore(primaryScore);
        
        // 3. Find secondary focus
        List<String> secondaryFocus = findSecondaryFocus(query, primaryFocus);
        attention.setSecondaryFocus(secondaryFocus);
        
        // 4. Identify context window
        List<String> contextWindow = identifyContextWindow(query, cursor);
        attention.setContextWindow(contextWindow);
        
        // 5. Calculate context relevance score
        double contextScore = calculateContextRelevance(query, contextWindow);
        attention.setContextRelevanceScore(contextScore);
        
        // 6. Determine focus depth
        int focusDepth = determineFocusDepth((int)cursor.getComplexity(), (int)cursor.getAmbiguity());
        attention.setFocusDepth(focusDepth);
        
        // 7. Determine focus type
        String focusType = determineFocusType(query, cursor.getFocusArea());
        attention.setFocusType(focusType);
        
        logger.debug("✅ Visual attention calculated: {}", attention);
        
        return attention;
    }
    
    /**
     * Calculate primary focus (main entity to focus on)
     */
    private String calculatePrimaryFocus(String query, ThoughtStreamCursor cursor) {
        String lowerQuery = query.toLowerCase();
        
        // Based on focus area
        switch (cursor.getFocusArea()) {
            case "DEBUG":
                if (lowerQuery.contains("null")) return "NullPointerException";
                if (lowerQuery.contains("error")) return "Error Handling";
                if (lowerQuery.contains("exception")) return "Exception";
                return "Bug";
                
            case "REFACTOR":
                if (lowerQuery.contains("duplicate")) return "Code Duplication";
                if (lowerQuery.contains("method")) return "Method Extraction";
                if (lowerQuery.contains("class")) return "Class Design";
                return "Code Quality";
                
            case "TESTING":
                if (lowerQuery.contains("unit")) return "Unit Tests";
                if (lowerQuery.contains("integration")) return "Integration Tests";
                if (lowerQuery.contains("mock")) return "Mocking";
                return "Testing";
                
            case "ARCHITECTURE":
                if (lowerQuery.contains("microservice")) return "Microservices";
                if (lowerQuery.contains("pattern")) return "Design Pattern";
                if (lowerQuery.contains("layer")) return "Layered Architecture";
                return "Architecture";
                
            case "PERFORMANCE":
                if (lowerQuery.contains("cache")) return "Caching";
                if (lowerQuery.contains("database")) return "Database Query";
                if (lowerQuery.contains("memory")) return "Memory Optimization";
                return "Performance";
                
            case "SECURITY":
                if (lowerQuery.contains("encrypt")) return "Encryption";
                if (lowerQuery.contains("auth")) return "Authentication";
                if (lowerQuery.contains("sql")) return "SQL Injection";
                return "Security";
                
            default:
                return "General Query";
        }
    }
    
    /**
     * Calculate primary focus score (0-100)
     */
    private double calculatePrimaryFocusScore(String query, String primaryFocus) {
        double score = 50.0; // Base score
        
        // Increase score if focus is mentioned in query
        if (query.toLowerCase().contains(primaryFocus.toLowerCase())) {
            score += 25;
        }
        
        // Increase score based on query specificity
        int wordCount = query.split("\\s+").length;
        if (wordCount > 10) score += 15;
        if (wordCount > 20) score += 10;
        
        // Cap at 100
        return Math.min(100, score);
    }
    
    /**
     * Find secondary focus (related entities)
     */
    private List<String> findSecondaryFocus(String query, String primaryFocus) {
        List<String> secondary = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        // Common related entities
        Map<String, List<String>> relatedEntities = new HashMap<>();
        relatedEntities.put("NullPointerException", Arrays.asList("null check", "defensive programming", "optional"));
        relatedEntities.put("Error Handling", Arrays.asList("try-catch", "exception", "logging"));
        relatedEntities.put("Code Duplication", Arrays.asList("extract method", "inheritance", "composition"));
        relatedEntities.put("Unit Tests", Arrays.asList("JUnit", "mocking", "assertions"));
        relatedEntities.put("Microservices", Arrays.asList("API Gateway", "service discovery", "load balancing"));
        relatedEntities.put("Caching", Arrays.asList("Redis", "Memcached", "cache invalidation"));
        relatedEntities.put("Encryption", Arrays.asList("SSL/TLS", "hashing", "key management"));
        
        List<String> related = relatedEntities.getOrDefault(primaryFocus, new ArrayList<>());
        for (String entity : related) {
            if (lowerQuery.contains(entity.toLowerCase())) {
                secondary.add(entity);
            }
        }
        
        // Limit to 3 secondary focuses
        return secondary.stream().limit(3).collect(Collectors.toList());
    }
    
    /**
     * Identify context window (relevant code blocks)
     */
    private List<String> identifyContextWindow(String query, ThoughtStreamCursor cursor) {
        List<String> contextWindow = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        // Add focus area context
        if (cursor.getFocusArea() != null) {
            contextWindow.add(cursor.getFocusArea());
        }
        
        // Add strategy context
        if (cursor.getStrategy() != null) {
            contextWindow.add(cursor.getStrategy().toString());
        }
        
        // Add keyword-based context
        if (lowerQuery.contains("spring")) contextWindow.add("Spring Framework");
        if (lowerQuery.contains("database")) contextWindow.add("Database");
        if (lowerQuery.contains("api")) contextWindow.add("REST API");
        if (lowerQuery.contains("ui")) contextWindow.add("User Interface");
        if (lowerQuery.contains("backend")) contextWindow.add("Backend");
        if (lowerQuery.contains("frontend")) contextWindow.add("Frontend");
        
        // Remove duplicates and limit
        return contextWindow.stream()
            .distinct()
            .limit(5)
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate context relevance score (0-100)
     */
    private double calculateContextRelevance(String query, List<String> contextWindow) {
        if (contextWindow.isEmpty()) {
            return 30.0; // Low relevance if no context
        }
        
        double score = 50.0; // Base score
        
        // Increase score based on context window size
        score += (contextWindow.size() * 10);
        
        // Cap at 100
        return Math.min(100, score);
    }
    
    /**
     * Determine focus depth (1-10)
     */
    private int determineFocusDepth(int complexity, int ambiguity) {
        int depth = 5; // Base depth
        
        // Increase depth for complex queries
        depth += (complexity / 2);
        
        // Increase depth for ambiguous queries
        depth += (ambiguity / 3);
        
        // Cap at 10
        return Math.min(10, depth);
    }
    
    /**
     * Determine focus type
     */
    private String determineFocusType(String query, String focusArea) {
        String lowerQuery = query.toLowerCase();
        
        if (lowerQuery.contains("what")) return "ENTITY";
        if (lowerQuery.contains("how")) return "PROCESS";
        if (lowerQuery.contains("why")) return "REASONING";
        if (lowerQuery.contains("fix") || lowerQuery.contains("solve")) return "SOLUTION";
        if (lowerQuery.contains("pattern") || lowerQuery.contains("design")) return "PATTERN";
        if (lowerQuery.contains("issue") || lowerQuery.contains("problem")) return "ISSUE";
        
        return "GENERAL";
    }
    
    /**
     * Create default attention for error cases
     */
    private VisualAttentionState createDefaultAttention() {
        return new VisualAttentionState()
            .setPrimaryFocus("General Query")
            .setPrimaryFocusScore(50.0)
            .setContextRelevanceScore(30.0)
            .setFocusDepth(5)
            .setFocusType("GENERAL");
    }
    
    /**
     * Score relevance of an element (0-100)
     */
    public double scoreRelevance(String element, String query, String focusArea) {
        if (element == null || element.isEmpty()) {
            return 0.0;
        }
        
        double score = 0.0;
        String lowerElement = element.toLowerCase();
        String lowerQuery = query.toLowerCase();
        String lowerFocus = focusArea.toLowerCase();
        
        // Exact match
        if (lowerElement.equals(lowerQuery)) {
            score += 50;
        }
        
        // Contains match
        if (lowerQuery.contains(lowerElement)) {
            score += 30;
        }
        
        // Focus area match
        if (lowerElement.contains(lowerFocus) || lowerFocus.contains(lowerElement)) {
            score += 20;
        }
        
        // Cap at 100
        return Math.min(100, score);
    }
}

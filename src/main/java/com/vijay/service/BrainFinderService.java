package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 🧠 PHASE 8: Brain RAG - Brain Finder Service
 * 
 * Semantic search for brains, just like ToolFinderService for tools.
 * 
 * Instead of running all 13 brains for every query, we:
 * 1. Embed the user's query
 * 2. Search the brainVectorStore for Top 3-4 similar brains
 * 3. Return only the relevant brains
 * 
 * This saves ~98% of tokens and prevents HTTP 413 errors.
 */
@Service
public class BrainFinderService {

    private static final Logger logger = LoggerFactory.getLogger(BrainFinderService.class);

    // 🧠 Core brains that MUST ALWAYS be included
    private static final List<String> CORE_BRAINS = Arrays.asList(
            "conductorAdvisor",           // Brain 0: Planner
            "toolCallAdvisor",            // Brain 2: Hands
            "selfRefineV3Advisor",        // Brain 13: Judge
            "personalityAdvisor"          // Brain 14: Voice
    );

    private final VectorStore brainVectorStore;

    public BrainFinderService(@Qualifier("brainVectorStore") VectorStore brainVectorStore) {
        this.brainVectorStore = brainVectorStore;
    }

    /**
     * Find the most relevant brains for a given query
     * 
     * IMPORTANT: ALWAYS includes core brains (Planner, Hands, Judge, Voice)
     * Then adds specialist brains found via semantic search
     * 
     * Example:
     * Query: "what is 10 + 20"
     * Returns: [conductorAdvisor, toolCallAdvisor, selfRefineV3Advisor, personalityAdvisor, advancedCapabilities]
     */
    public List<String> findBrainsFor(String query) {
        try {
            // STEP 1: Always include core brains
            List<String> brains = new ArrayList<>(CORE_BRAINS);
            
            // STEP 2: Find specialist brains via semantic search
            SearchRequest request = SearchRequest.builder()
                    .query(query)
                    .topK(4)  // Get top 4 specialist brains
                    .build();

            List<Document> similarDocuments = brainVectorStore.similaritySearch(request);

            List<String> specialistBrains = similarDocuments.stream()
                    .map(doc -> (String) doc.getMetadata().get("brainName"))
                    .collect(Collectors.toList());
            
            // STEP 3: Add specialist brains (avoid duplicates with core brains)
            for (String brain : specialistBrains) {
                if (!brains.contains(brain)) {
                    brains.add(brain);
                }
            }
            
            // STEP 4: Sort by execution order
            brains.sort(this::compareByOrder);

            logger.info("🧠 BrainFinder: Core({}) + Specialist({}) = Total({})", 
                    CORE_BRAINS.size(), 
                    specialistBrains.size(), 
                    brains.size());
            logger.info("   Selected brains: {}", brains);

            return brains;
        } catch (Exception e) {
            logger.error("❌ Error finding brains: {}", e.getMessage());
            // Fallback: return core brains only
            return new ArrayList<>(CORE_BRAINS);
        }
    }
    
    /**
     * Compare brains by their execution order
     */
    private int compareByOrder(String brain1, String brain2) {
        Map<String, Integer> orderMap = new HashMap<>();
        orderMap.put("conductorAdvisor", 0);
        orderMap.put("toolCallAdvisor", 2);
        orderMap.put("personalityAdvisor", 800);
        orderMap.put("selfRefineV3Advisor", 1000);
        
        return Integer.compare(
                orderMap.getOrDefault(brain1, 500),
                orderMap.getOrDefault(brain2, 500)
        );
    }

    /**
     * Find top N brains with multi-dimensional scoring
     * 
     * Scores:
     * - Relevance (40%): semantic similarity to query
     * - Complexity match (30%): brain handles this complexity level
     * - User history (20%): user has used this brain before
     * - Performance (10%): brain response time
     */
    public List<String> findTopBrains(String query, int complexity, String userId, int topN) {
        try {
            logger.info("🧠 BrainFinder: Finding top {} brains (complexity={}, user={})", topN, complexity, userId);
            
            // Get all brains
            List<String> allBrains = getAllBrains();
            
            // Score each brain
            List<BrainScore> scoredBrains = allBrains.stream()
                    .map(brain -> new BrainScore(
                            brain,
                            calculateTotalScore(brain, query, complexity, userId)
                    ))
                    .sorted((a, b) -> Double.compare(b.score, a.score))
                    .collect(Collectors.toList());
            
            // Get top N
            List<String> topBrains = scoredBrains.stream()
                    .limit(topN)
                    .map(bs -> bs.brainName)
                    .collect(Collectors.toList());
            
            // Always include core brains
            for (String coreBrain : CORE_BRAINS) {
                if (!topBrains.contains(coreBrain)) {
                    topBrains.add(coreBrain);
                }
            }
            
            // Sort by execution order
            topBrains.sort(this::compareByOrder);
            
            logger.info("✅ Top brains selected: {}", topBrains);
            
            return topBrains;
        } catch (Exception e) {
            logger.error("❌ Error finding top brains: {}", e.getMessage());
            return new ArrayList<>(CORE_BRAINS);
        }
    }
    
    /**
     * Calculate total score for a brain (0-100)
     */
    private double calculateTotalScore(String brain, String query, int complexity, String userId) {
        double relevanceScore = scoreRelevance(brain, query);           // 40%
        double complexityScore = scoreComplexityMatch(brain, complexity); // 30%
        double historyScore = scoreUserHistory(brain, userId);          // 20%
        double performanceScore = scorePerformance(brain);              // 10%
        
        double totalScore = relevanceScore + complexityScore + historyScore + performanceScore;
        
        logger.debug("   Brain {}: relevance={}, complexity={}, history={}, performance={}, total={}",
                brain, relevanceScore, complexityScore, historyScore, performanceScore, totalScore);
        
        return totalScore;
    }
    
    /**
     * Score relevance (40%) - semantic similarity
     */
    private double scoreRelevance(String brain, String query) {
        try {
            SearchRequest request = SearchRequest.builder()
                    .query(query)
                    .topK(1)
                    .build();
            
            List<Document> results = brainVectorStore.similaritySearch(request);
            
            if (results.isEmpty()) {
                return 0;
            }
            
            Document doc = results.get(0);
            String docBrainName = (String) doc.getMetadata().get("brainName");
            
            // If this brain matches, give it high score
            if (brain.equals(docBrainName)) {
                return 40.0; // 40% weight
            }
            
            return 10.0; // Lower score for non-matching
        } catch (Exception e) {
            logger.debug("Error scoring relevance for {}: {}", brain, e.getMessage());
            return 0;
        }
    }
    
    /**
     * Score complexity match (30%) - does brain handle this complexity?
     */
    private double scoreComplexityMatch(String brain, int complexity) {
        // Map brains to their complexity handling
        Map<String, Integer> complexityMap = new HashMap<>();
        complexityMap.put("conductorAdvisor", 10);           // Handles all
        complexityMap.put("toolCallAdvisor", 10);            // Handles all
        complexityMap.put("selfRefineV3Advisor", 10);        // Handles all
        complexityMap.put("personalityAdvisor", 5);          // Low complexity
        complexityMap.put("errorPredictionAdvisor", 8);      // High complexity
        complexityMap.put("knowledgeGraphAdvisor", 9);       // Very high
        complexityMap.put("advancedCapabilitiesAdvisor", 8); // High
        complexityMap.put("theoryOfMindAdvisor", 7);         // Medium-high
        
        Integer brainComplexity = complexityMap.getOrDefault(brain, 5);
        
        // Score: how well does this brain match the query complexity?
        double match = 1.0 - Math.abs(brainComplexity - complexity) / 10.0;
        return Math.max(0, match * 30.0); // 30% weight
    }
    
    /**
     * Score user history (20%) - has user used this brain before?
     */
    private double scoreUserHistory(String brain, String userId) {
        // TODO: Implement user history tracking
        // For now, return neutral score
        return 20.0 * 0.5; // 50% of 20% weight
    }
    
    /**
     * Score performance (10%) - how fast is this brain?
     */
    private double scorePerformance(String brain) {
        // Map brains to their typical response time (ms)
        Map<String, Integer> performanceMap = new HashMap<>();
        performanceMap.put("conductorAdvisor", 10);           // Very fast
        performanceMap.put("toolCallAdvisor", 15);            // Very fast
        performanceMap.put("personalityAdvisor", 50);         // Fast
        performanceMap.put("selfRefineV3Advisor", 100);       // Medium
        performanceMap.put("errorPredictionAdvisor", 80);     // Medium
        performanceMap.put("knowledgeGraphAdvisor", 150);     // Slow
        performanceMap.put("advancedCapabilitiesAdvisor", 120); // Slow
        
        Integer responseTime = performanceMap.getOrDefault(brain, 100);
        
        // Score: faster brains get higher score
        double score = Math.max(0, 1.0 - (responseTime / 200.0));
        return score * 10.0; // 10% weight
    }
    
    /**
     * Get all brains (for debugging/monitoring)
     */
    public List<String> getAllBrains() {
        try {
            SearchRequest request = SearchRequest.builder()
                    .query("*")  // Match all
                    .topK(100)
                    .build();

            List<Document> allDocuments = brainVectorStore.similaritySearch(request);

            return allDocuments.stream()
                    .map(doc -> (String) doc.getMetadata().get("brainName"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("❌ Error getting all brains: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Inner class for brain scoring
     */
    private static class BrainScore {
        String brainName;
        double score;
        
        BrainScore(String brainName, double score) {
            this.brainName = brainName;
            this.score = score;
        }
    }
}

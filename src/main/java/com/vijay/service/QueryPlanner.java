package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class QueryPlanner {

    private static final Logger logger = LoggerFactory.getLogger(QueryPlanner.class);

    public SearchPlan createSearchPlan(String query) {
        logger.info("🎯 Query Planner: Creating intelligent search plan for: '{}'", query);
        
        SearchPlan plan = new SearchPlan();
        plan.originalQuery = query;
        
        // Step 1: Analyze query structure and intent
        QueryAnalysis analysis = analyzeQuery(query);
        plan.intent = analysis.intent;
        plan.confidence = analysis.confidence;
        plan.complexity = analysis.complexity;
        
        // Step 2: Extract key entities and concepts
        plan.targetEntities = extractEntities(query);
        plan.searchKeywords = extractSearchKeywords(query);
        
        // Step 3: Determine search strategy based on query type
        plan.searchStrategy = determineSearchStrategy(analysis, plan.targetEntities);
        
        // Step 4: Set search parameters
        configureSearchParameters(plan, analysis);
        
        // Step 5: Plan dependency expansion
        planDependencyExpansion(plan, analysis);
        
        // Step 6: Set token budget allocation
        allocateTokenBudget(plan, analysis);
        
        logger.info("📋 Search Plan Created:");
        logger.info("   🎯 Intent: {} (confidence: {:.2f})", plan.intent, plan.confidence);
        logger.info("   🔍 Strategy: {}", plan.searchStrategy);
        logger.info("   📂 Starting Files: {}", plan.startingFiles);
        logger.info("   🔗 Max Hops: {} | Token Budget: {}", plan.maxHops, plan.tokenBudget);
        logger.info("   🎯 Target Entities: {}", plan.targetEntities);
        
        return plan;
    }

    private QueryAnalysis analyzeQuery(String query) {
        QueryAnalysis analysis = new QueryAnalysis();
        String lowerQuery = query.toLowerCase();
        String[] words = query.split("\\s+");
        
        // Detect query patterns
        analysis.isHowQuestion = lowerQuery.startsWith("how") || lowerQuery.contains("how does") || lowerQuery.contains("how to");
        analysis.isWhatQuestion = lowerQuery.startsWith("what") || lowerQuery.contains("what is");
        analysis.isWhereQuestion = lowerQuery.startsWith("where") || lowerQuery.contains("where is");
        analysis.isExplainRequest = lowerQuery.contains("explain") || lowerQuery.contains("show me");
        analysis.isArchitectureQuery = lowerQuery.contains("architecture") || lowerQuery.contains("structure") || lowerQuery.contains("design");
        analysis.isImplementationQuery = lowerQuery.contains("implement") || lowerQuery.contains("code") || lowerQuery.contains("method");
        analysis.isConfigurationQuery = lowerQuery.contains("config") || lowerQuery.contains("setup") || lowerQuery.contains("configure");
        analysis.isDebuggingQuery = lowerQuery.contains("error") || lowerQuery.contains("bug") || lowerQuery.contains("issue") || lowerQuery.contains("problem");
        
        // Determine intent with higher precision
        if (analysis.isDebuggingQuery) {
            analysis.intent = "DEBUG";
            analysis.confidence = 0.9;
        } else if (analysis.isConfigurationQuery) {
            analysis.intent = "CONFIG";
            analysis.confidence = 0.85;
        } else if (analysis.isArchitectureQuery) {
            analysis.intent = "ARCHITECTURE";
            analysis.confidence = 0.9;
        } else if (analysis.isImplementationQuery) {
            analysis.intent = "IMPLEMENTATION";
            analysis.confidence = 0.8;
        } else if (containsCodeKeywords(lowerQuery)) {
            analysis.intent = "CODE";
            analysis.confidence = 0.75;
        } else if (containsToolKeywords(lowerQuery)) {
            analysis.intent = "TOOLS";
            analysis.confidence = 0.8;
        } else {
            analysis.intent = "GENERAL";
            analysis.confidence = 0.6;
        }
        
        // Determine complexity
        if (words.length > 20 || lowerQuery.contains(" and ") || lowerQuery.contains(" also ")) {
            analysis.complexity = "HIGH";
        } else if (words.length > 8 || analysis.isArchitectureQuery) {
            analysis.complexity = "MEDIUM";
        } else {
            analysis.complexity = "LOW";
        }
        
        return analysis;
    }

    private List<String> extractEntities(String query) {
        List<String> entities = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        // Extract class/service names (CamelCase or specific patterns)
        Pattern classPattern = Pattern.compile("\\b[A-Z][a-zA-Z]*(?:Service|Controller|Config|Manager|Advisor|Builder)\\b");
        var classMatcher = classPattern.matcher(query);
        while (classMatcher.find()) {
            entities.add(classMatcher.group());
        }
        
        // Extract method names (common patterns)
        Pattern methodPattern = Pattern.compile("\\b(?:get|set|process|handle|create|build|configure|manage)[A-Z][a-zA-Z]*\\b");
        var methodMatcher = methodPattern.matcher(query);
        while (methodMatcher.find()) {
            entities.add(methodMatcher.group());
        }
        
        // Extract technical keywords
        String[] techKeywords = {
            "chatservice", "aiproviderconfig", "advisor", "retriever", "planner",
            "vectorstore", "embedding", "dependency", "graph", "context", "budget",
            "token", "chunk", "summary", "brain", "query", "intent", "planning"
        };
        
        for (String keyword : techKeywords) {
            if (lowerQuery.contains(keyword)) {
                entities.add(keyword);
            }
        }
        
        return entities.stream().distinct().limit(8).toList();
    }

    private List<String> extractSearchKeywords(String query) {
        List<String> keywords = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        // Remove common stop words and extract meaningful terms
        String[] stopWords = {"how", "does", "what", "is", "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by"};
        Set<String> stopWordSet = Set.of(stopWords);
        
        String[] words = query.split("\\s+");
        for (String word : words) {
            String cleanWord = word.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
            if (cleanWord.length() > 2 && !stopWordSet.contains(cleanWord)) {
                keywords.add(cleanWord);
            }
        }
        
        return keywords.stream().distinct().limit(6).toList();
    }

    private String determineSearchStrategy(QueryAnalysis analysis, List<String> entities) {
        if (analysis.intent.equals("DEBUG")) {
            return "error_trace"; // Follow error paths and exception handling
        } else if (analysis.intent.equals("ARCHITECTURE")) {
            return "dependency_graph"; // Wide exploration of relationships
        } else if (analysis.intent.equals("IMPLEMENTATION")) {
            return "method_focused"; // Focus on specific implementations
        } else if (analysis.intent.equals("CONFIG")) {
            return "configuration_chain"; // Follow configuration dependencies
        } else if (!entities.isEmpty()) {
            return "entity_centered"; // Start from specific entities
        } else {
            return "similarity_search"; // Broad similarity-based search
        }
    }

    private void configureSearchParameters(SearchPlan plan, QueryAnalysis analysis) {
        switch (plan.searchStrategy) {
            case "dependency_graph":
                plan.topK = 5;
                plan.maxHops = 3;
                plan.includeReverseDeps = true;
                break;
            case "entity_centered":
                plan.topK = 3;
                plan.maxHops = 2;
                plan.includeReverseDeps = false;
                break;
            case "method_focused":
                plan.topK = 4;
                plan.maxHops = 1;
                plan.includeReverseDeps = false;
                break;
            case "error_trace":
                plan.topK = 6;
                plan.maxHops = 2;
                plan.includeReverseDeps = true;
                break;
            case "configuration_chain":
                plan.topK = 4;
                plan.maxHops = 2;
                plan.includeReverseDeps = false;
                break;
            default: // similarity_search
                plan.topK = 3;
                plan.maxHops = 1;
                plan.includeReverseDeps = false;
        }
        
        // Adjust based on complexity
        if (analysis.complexity.equals("HIGH")) {
            plan.topK += 2;
            plan.maxHops = Math.min(plan.maxHops + 1, 3);
        } else if (analysis.complexity.equals("LOW")) {
            plan.topK = Math.max(plan.topK - 1, 2);
        }
    }

    private void planDependencyExpansion(SearchPlan plan, QueryAnalysis analysis) {
        // Determine starting files based on entities
        plan.startingFiles = new ArrayList<>();
        
        for (String entity : plan.targetEntities) {
            if (entity.toLowerCase().contains("service")) {
                plan.startingFiles.add(entity + ".java");
            } else if (entity.toLowerCase().contains("config")) {
                plan.startingFiles.add(entity + ".java");
            } else if (entity.toLowerCase().contains("controller")) {
                plan.startingFiles.add(entity + ".java");
            }
        }
        
        // If no specific files identified, use query-based guessing
        if (plan.startingFiles.isEmpty()) {
            String lowerQuery = plan.originalQuery.toLowerCase();
            if (lowerQuery.contains("chat")) {
                plan.startingFiles.add("ChatService.java");
            }
            if (lowerQuery.contains("config") || lowerQuery.contains("provider")) {
                plan.startingFiles.add("AIProviderConfig.java");
            }
            if (lowerQuery.contains("advisor") || lowerQuery.contains("brain")) {
                plan.startingFiles.add("QueryPlannerAdvisor.java");
            }
        }
        
        // Fallback to similarity search if no starting files
        if (plan.startingFiles.isEmpty()) {
            plan.searchStrategy = "similarity_search";
        }
    }

    private void allocateTokenBudget(SearchPlan plan, QueryAnalysis analysis) {
        // Base budget allocation
        plan.tokenBudget = 7000;
        
        // Adjust based on complexity and strategy
        if (analysis.complexity.equals("HIGH")) {
            plan.tokenBudget = 6000; // More conservative for complex queries
        } else if (analysis.complexity.equals("LOW")) {
            plan.tokenBudget = 5000; // Even more conservative for simple queries
        }
        
        // Strategy-specific adjustments
        switch (plan.searchStrategy) {
            case "dependency_graph":
                plan.tokenBudget = Math.min(plan.tokenBudget, 6500); // Wide search needs control
                break;
            case "method_focused":
                plan.tokenBudget = Math.min(plan.tokenBudget, 4000); // Focused search
                break;
            case "error_trace":
                plan.tokenBudget = Math.min(plan.tokenBudget, 5500); // Moderate for debugging
                break;
        }
    }

    private boolean containsCodeKeywords(String query) {
        String[] codeKeywords = {
            "chatservice", "aiproviderconfig", "advisor", "service", "config", "class", "method",
            "how does", "show me", "explain", "architecture", "implementation", "dependency",
            "brain", "code", "function", "java", "spring", "component", "controller", "repository"
        };
        
        for (String keyword : codeKeywords) {
            if (query.contains(keyword)) return true;
        }
        return false;
    }

    private boolean containsToolKeywords(String query) {
        String[] toolKeywords = {
            "weather", "temperature", "calendar", "meeting", "schedule", "search", "email",
            "time", "date", "forecast", "event", "appointment", "google", "find", "version",
            "latest", "current", "today", "now", "when", "what time"
        };
        
        for (String keyword : toolKeywords) {
            if (query.contains(keyword)) return true;
        }
        return false;
    }

    public static class SearchPlan {
        public String originalQuery;
        public String intent;
        public double confidence;
        public String complexity;
        public String searchStrategy;
        public List<String> targetEntities = new ArrayList<>();
        public List<String> searchKeywords = new ArrayList<>();
        public List<String> startingFiles = new ArrayList<>();
        public int topK = 3;
        public int maxHops = 2;
        public boolean includeReverseDeps = false;
        public int tokenBudget = 7000;
        
        public boolean isHighConfidence() {
            return confidence >= 0.8;
        }
        
        public boolean isComplexQuery() {
            return complexity.equals("HIGH");
        }
        
        public boolean hasSpecificEntities() {
            return !targetEntities.isEmpty();
        }
    }

    private static class QueryAnalysis {
        String intent;
        double confidence;
        String complexity;
        boolean isHowQuestion;
        boolean isWhatQuestion;
        boolean isWhereQuestion;
        boolean isExplainRequest;
        boolean isArchitectureQuery;
        boolean isImplementationQuery;
        boolean isConfigurationQuery;
        boolean isDebuggingQuery;
    }
}

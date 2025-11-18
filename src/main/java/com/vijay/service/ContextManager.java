package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContextManager {

    private static final Logger logger = LoggerFactory.getLogger(ContextManager.class);
    
    // Token budget configuration
    private static final int MAX_CONTEXT_TOKENS = 8000;
    private static final int RESERVED_TOKENS = 1000; // For response generation
    private static final int AVAILABLE_TOKENS = MAX_CONTEXT_TOKENS - RESERVED_TOKENS;
    
    // Rough token estimation (1 token ≈ 4 characters for code)
    private static final double CHARS_PER_TOKEN = 4.0;

    public ContextBudget createBudget(String query) {
        ContextBudget budget = new ContextBudget();
        budget.maxTokens = AVAILABLE_TOKENS;
        budget.usedTokens = estimateTokens(query);
        budget.remainingTokens = budget.maxTokens - budget.usedTokens;
        
        logger.info("💰 Context Budget: {}/{} tokens available (Query: {} tokens)", 
            budget.remainingTokens, budget.maxTokens, budget.usedTokens);
        
        return budget;
    }

    public List<String> pruneContent(List<String> content, ContextBudget budget, String query) {
        if (content.isEmpty()) return content;
        
        // Calculate current content size
        int totalTokens = content.stream()
            .mapToInt(this::estimateTokens)
            .sum();
        
        logger.info("📊 Content Analysis: {} items, {} tokens total", content.size(), totalTokens);
        
        if (budget.usedTokens + totalTokens <= budget.maxTokens) {
            logger.info("✅ All content fits within budget");
            budget.usedTokens += totalTokens;
            budget.remainingTokens = budget.maxTokens - budget.usedTokens;
            return content;
        }
        
        // Need to prune - score and rank content by relevance
        logger.info("⚠️ Content exceeds budget, pruning required");
        
        List<ContentItem> scoredContent = content.stream()
            .map(item -> new ContentItem(item, calculateRelevanceScore(item, query)))
            .sorted((a, b) -> Double.compare(b.relevanceScore, a.relevanceScore))
            .collect(Collectors.toList());
        
        // Select content within budget, prioritizing by relevance
        List<String> prunedContent = new ArrayList<>();
        int budgetUsed = budget.usedTokens;
        int itemsKept = 0;
        
        for (ContentItem item : scoredContent) {
            int itemTokens = estimateTokens(item.content);
            if (budgetUsed + itemTokens <= budget.maxTokens) {
                prunedContent.add(item.content);
                budgetUsed += itemTokens;
                itemsKept++;
            }
        }
        
        budget.usedTokens = budgetUsed;
        budget.remainingTokens = budget.maxTokens - budget.usedTokens;
        
        logger.info("✂️ Pruned: {}/{} items kept, {}/{} tokens used", 
            itemsKept, content.size(), budget.usedTokens, budget.maxTokens);
        
        return prunedContent;
    }

    public boolean canAddContent(String content, ContextBudget budget) {
        int contentTokens = estimateTokens(content);
        boolean canAdd = budget.usedTokens + contentTokens <= budget.maxTokens;
        
        if (!canAdd) {
            logger.warn("🚫 Cannot add content: {} tokens would exceed budget ({}/{})", 
                contentTokens, budget.usedTokens + contentTokens, budget.maxTokens);
        }
        
        return canAdd;
    }

    public void addContent(String content, ContextBudget budget) {
        int contentTokens = estimateTokens(content);
        budget.usedTokens += contentTokens;
        budget.remainingTokens = budget.maxTokens - budget.usedTokens;
        
        logger.debug("➕ Added content: {} tokens, budget: {}/{}", 
            contentTokens, budget.usedTokens, budget.maxTokens);
    }

    public List<String> prioritizeFiles(List<String> files, String query, ContextBudget budget) {
        if (files.size() <= 5) return files; // Small lists don't need prioritization
        
        logger.info("🎯 Prioritizing {} files for relevance", files.size());
        
        List<FileItem> scoredFiles = files.stream()
            .map(file -> new FileItem(file, calculateFileRelevanceScore(file, query)))
            .sorted((a, b) -> Double.compare(b.relevanceScore, a.relevanceScore))
            .collect(Collectors.toList());
        
        // Log top and bottom files for debugging
        logger.info("🏆 Top relevant: {} (score: {:.2f})", 
            scoredFiles.get(0).fileName, scoredFiles.get(0).relevanceScore);
        logger.info("🔻 Least relevant: {} (score: {:.2f})", 
            scoredFiles.get(scoredFiles.size()-1).fileName, 
            scoredFiles.get(scoredFiles.size()-1).relevanceScore);
        
        // Filter out very low relevance files (score < 0.3)
        List<String> filteredFiles = scoredFiles.stream()
            .filter(item -> item.relevanceScore >= 0.3)
            .map(item -> item.fileName)
            .collect(Collectors.toList());
        
        if (filteredFiles.size() < files.size()) {
            logger.info("🗑️ Filtered out {} low-relevance files", files.size() - filteredFiles.size());
        }
        
        return filteredFiles;
    }

    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        return (int) Math.ceil(text.length() / CHARS_PER_TOKEN);
    }

    private double calculateRelevanceScore(String content, String query) {
        if (content == null || content.isEmpty() || query == null || query.isEmpty()) {
            return 0.0;
        }
        
        String lowerContent = content.toLowerCase();
        String lowerQuery = query.toLowerCase();
        String[] queryWords = lowerQuery.split("\\s+");
        
        double score = 0.0;
        int matches = 0;
        
        // Direct word matches
        for (String word : queryWords) {
            if (word.length() > 2 && lowerContent.contains(word)) {
                matches++;
                score += 0.2;
            }
        }
        
        // Technical keyword bonuses
        if (lowerQuery.contains("service") && lowerContent.contains("service")) score += 0.3;
        if (lowerQuery.contains("config") && lowerContent.contains("config")) score += 0.3;
        if (lowerQuery.contains("advisor") && lowerContent.contains("advisor")) score += 0.3;
        
        // Code structure bonuses
        if (lowerContent.contains("public class") || lowerContent.contains("@service")) score += 0.2;
        if (lowerContent.contains("@component") || lowerContent.contains("@configuration")) score += 0.2;
        
        // Length penalty for very long content (likely less focused)
        if (content.length() > 5000) score *= 0.8;
        
        return Math.min(1.0, score);
    }

    private double calculateFileRelevanceScore(String fileName, String query) {
        if (fileName == null || fileName.isEmpty()) return 0.0;
        
        String lowerFile = fileName.toLowerCase();
        String lowerQuery = query.toLowerCase();
        String[] queryWords = lowerQuery.split("\\s+");
        
        double score = 0.0;
        
        // Direct filename matches
        for (String word : queryWords) {
            if (word.length() > 2 && lowerFile.contains(word.toLowerCase())) {
                score += 0.4;
            }
        }
        
        // File type relevance
        if (lowerQuery.contains("service") && lowerFile.contains("service")) score += 0.5;
        if (lowerQuery.contains("config") && lowerFile.contains("config")) score += 0.5;
        if (lowerQuery.contains("advisor") && lowerFile.contains("advisor")) score += 0.5;
        if (lowerQuery.contains("controller") && lowerFile.contains("controller")) score += 0.5;
        
        // Core files get bonus
        if (lowerFile.contains("chatservice") || lowerFile.contains("aiproviderconfig")) {
            score += 0.3;
        }
        
        return Math.min(1.0, score);
    }

    public static class ContextBudget {
        public int maxTokens;
        public int usedTokens;
        public int remainingTokens;
        
        public double getUsagePercentage() {
            return (double) usedTokens / maxTokens * 100;
        }
        
        public boolean isNearLimit() {
            return getUsagePercentage() > 80;
        }
        
        public boolean isOverLimit() {
            return usedTokens > maxTokens;
        }
    }

    private static class ContentItem {
        final String content;
        final double relevanceScore;
        
        ContentItem(String content, double relevanceScore) {
            this.content = content;
            this.relevanceScore = relevanceScore;
        }
    }

    private static class FileItem {
        final String fileName;
        final double relevanceScore;
        
        FileItem(String fileName, double relevanceScore) {
            this.fileName = fileName;
            this.relevanceScore = relevanceScore;
        }
    }
}

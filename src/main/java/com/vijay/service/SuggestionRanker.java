package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 🎯 Suggestion Ranker
 * 
 * Ranks and filters suggestions:
 * - Rank by importance
 * - Filter low-confidence suggestions
 * - Prioritize actionable suggestions
 */
@Component
public class SuggestionRanker {
    
    private static final Logger logger = LoggerFactory.getLogger(SuggestionRanker.class);
    
    /**
     * Rank suggestions by importance
     */
    public List<PairProgrammingAssistant.Suggestion> rankSuggestions(
            List<PairProgrammingAssistant.Suggestion> suggestions) {
        
        if (suggestions == null || suggestions.isEmpty()) {
            logger.debug("⚠️ No suggestions to rank");
            return new ArrayList<>();
        }
        
        logger.debug("🎯 Ranking {} suggestions", suggestions.size());
        
        // Score each suggestion
        List<SuggestionScore> scored = suggestions.stream()
                .map(this::scoreSuggestion)
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .collect(Collectors.toList());
        
        // Extract ranked suggestions
        List<PairProgrammingAssistant.Suggestion> ranked = scored.stream()
                .map(ss -> ss.suggestion)
                .collect(Collectors.toList());
        
        logger.debug("✅ Ranked {} suggestions", ranked.size());
        return ranked;
    }
    
    /**
     * Filter out low-confidence suggestions
     */
    public List<PairProgrammingAssistant.Suggestion> filterLowConfidence(
            List<PairProgrammingAssistant.Suggestion> suggestions,
            double confidenceThreshold) {
        
        if (suggestions == null || suggestions.isEmpty()) {
            return new ArrayList<>();
        }
        
        logger.debug("🔍 Filtering suggestions (threshold={})", confidenceThreshold);
        
        List<PairProgrammingAssistant.Suggestion> filtered = suggestions.stream()
                .filter(s -> calculateConfidence(s) >= confidenceThreshold)
                .collect(Collectors.toList());
        
        logger.debug("✅ Filtered: {} → {} suggestions", suggestions.size(), filtered.size());
        return filtered;
    }
    
    /**
     * Prioritize actionable suggestions
     */
    public List<PairProgrammingAssistant.Suggestion> prioritizeActionable(
            List<PairProgrammingAssistant.Suggestion> suggestions) {
        
        if (suggestions == null || suggestions.isEmpty()) {
            return new ArrayList<>();
        }
        
        logger.debug("⚡ Prioritizing actionable suggestions");
        
        // Score by actionability
        List<SuggestionScore> scored = suggestions.stream()
                .map(s -> new SuggestionScore(s, scoreActionability(s)))
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .collect(Collectors.toList());
        
        List<PairProgrammingAssistant.Suggestion> prioritized = scored.stream()
                .map(ss -> ss.suggestion)
                .collect(Collectors.toList());
        
        logger.debug("✅ Prioritized {} suggestions", prioritized.size());
        return prioritized;
    }
    
    /**
     * Score a suggestion (0-100)
     */
    private SuggestionScore scoreSuggestion(PairProgrammingAssistant.Suggestion suggestion) {
        double score = 0;
        
        // Score by type
        switch (suggestion.type) {
            case "BUG":
                score += 50; // Bugs are high priority
                break;
            case "REFACTOR":
                score += 30; // Refactoring is medium priority
                break;
            case "PERFORMANCE":
                score += 40; // Performance is high priority
                break;
            default:
                score += 20;
        }
        
        // Score by severity
        switch (suggestion.severity) {
            case "CRITICAL":
                score += 40;
                break;
            case "HIGH":
                score += 30;
                break;
            case "MEDIUM":
                score += 20;
                break;
            case "LOW":
                score += 10;
                break;
        }
        
        // Actionability bonus
        score += scoreActionability(suggestion) * 10;
        
        return new SuggestionScore(suggestion, Math.min(100, score));
    }
    
    /**
     * Score actionability (0-1)
     */
    private double scoreActionability(PairProgrammingAssistant.Suggestion suggestion) {
        double actionability = 0.5; // Base actionability
        
        // Bugs are highly actionable
        if ("BUG".equals(suggestion.type)) {
            actionability += 0.3;
        }
        
        // High severity is more actionable
        if ("CRITICAL".equals(suggestion.severity) || "HIGH".equals(suggestion.severity)) {
            actionability += 0.2;
        }
        
        return Math.min(1.0, actionability);
    }
    
    /**
     * Calculate confidence in suggestion (0-1)
     */
    private double calculateConfidence(PairProgrammingAssistant.Suggestion suggestion) {
        double confidence = 0.7; // Base confidence
        
        // Increase confidence for bugs
        if ("BUG".equals(suggestion.type)) {
            confidence += 0.2;
        }
        
        // Increase confidence for high severity
        if ("CRITICAL".equals(suggestion.severity) || "HIGH".equals(suggestion.severity)) {
            confidence += 0.1;
        }
        
        return Math.min(1.0, confidence);
    }
    
    /**
     * Get top N suggestions
     */
    public List<PairProgrammingAssistant.Suggestion> getTopSuggestions(
            List<PairProgrammingAssistant.Suggestion> suggestions,
            int topN) {
        
        if (suggestions == null || suggestions.isEmpty()) {
            return new ArrayList<>();
        }
        
        logger.debug("🏆 Getting top {} suggestions", topN);
        
        List<PairProgrammingAssistant.Suggestion> ranked = rankSuggestions(suggestions);
        
        List<PairProgrammingAssistant.Suggestion> top = ranked.stream()
                .limit(topN)
                .collect(Collectors.toList());
        
        logger.debug("✅ Returned {} top suggestions", top.size());
        return top;
    }
    
    /**
     * Group suggestions by type
     */
    public Map<String, List<PairProgrammingAssistant.Suggestion>> groupByType(
            List<PairProgrammingAssistant.Suggestion> suggestions) {
        
        if (suggestions == null || suggestions.isEmpty()) {
            return new HashMap<>();
        }
        
        logger.debug("📊 Grouping {} suggestions by type", suggestions.size());
        
        return suggestions.stream()
                .collect(Collectors.groupingBy(s -> s.type));
    }
    
    /**
     * Group suggestions by severity
     */
    public Map<String, List<PairProgrammingAssistant.Suggestion>> groupBySeverity(
            List<PairProgrammingAssistant.Suggestion> suggestions) {
        
        if (suggestions == null || suggestions.isEmpty()) {
            return new HashMap<>();
        }
        
        logger.debug("📊 Grouping {} suggestions by severity", suggestions.size());
        
        return suggestions.stream()
                .collect(Collectors.groupingBy(s -> s.severity));
    }
    
    /**
     * Get summary statistics
     */
    public SuggestionStats getStats(List<PairProgrammingAssistant.Suggestion> suggestions) {
        SuggestionStats stats = new SuggestionStats();
        
        if (suggestions == null || suggestions.isEmpty()) {
            return stats;
        }
        
        stats.totalSuggestions = suggestions.size();
        stats.bugCount = (int) suggestions.stream().filter(s -> "BUG".equals(s.type)).count();
        stats.refactoringCount = (int) suggestions.stream().filter(s -> "REFACTOR".equals(s.type)).count();
        stats.performanceCount = (int) suggestions.stream().filter(s -> "PERFORMANCE".equals(s.type)).count();
        
        stats.criticalCount = (int) suggestions.stream().filter(s -> "CRITICAL".equals(s.severity)).count();
        stats.highCount = (int) suggestions.stream().filter(s -> "HIGH".equals(s.severity)).count();
        stats.mediumCount = (int) suggestions.stream().filter(s -> "MEDIUM".equals(s.severity)).count();
        stats.lowCount = (int) suggestions.stream().filter(s -> "LOW".equals(s.severity)).count();
        
        logger.debug("📈 Stats: {} total, {} bugs, {} refactor, {} perf",
                stats.totalSuggestions, stats.bugCount, stats.refactoringCount, stats.performanceCount);
        
        return stats;
    }
    
    /**
     * Suggestion Score DTO
     */
    private static class SuggestionScore {
        PairProgrammingAssistant.Suggestion suggestion;
        double score;
        
        SuggestionScore(PairProgrammingAssistant.Suggestion suggestion, double score) {
            this.suggestion = suggestion;
            this.score = score;
        }
    }
    
    /**
     * Suggestion Stats DTO
     */
    public static class SuggestionStats {
        public int totalSuggestions = 0;
        public int bugCount = 0;
        public int refactoringCount = 0;
        public int performanceCount = 0;
        public int criticalCount = 0;
        public int highCount = 0;
        public int mediumCount = 0;
        public int lowCount = 0;
        
        @Override
        public String toString() {
            return String.format("SuggestionStats{total=%d, bugs=%d, refactor=%d, perf=%d, critical=%d, high=%d}",
                    totalSuggestions, bugCount, refactoringCount, performanceCount, criticalCount, highCount);
        }
    }
}

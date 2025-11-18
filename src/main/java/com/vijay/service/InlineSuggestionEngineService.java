package com.vijay.service;

import com.vijay.model.EditHistory;
import com.vijay.model.UserPattern;
import com.vijay.repository.EditHistoryRepository;
import com.vijay.repository.UserPatternRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 💡 INLINE SUGGESTION ENGINE SERVICE
 * 
 * Generates real-time inline suggestions as user types.
 * Provides context-aware suggestions based on code analysis and user patterns.
 * Learns from user feedback to improve suggestion quality.
 * 
 * ✅ PHASE 3.3: InlineSuggestionEngine - Week 13
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InlineSuggestionEngineService {
    
    private static final Logger logger = LoggerFactory.getLogger(InlineSuggestionEngineService.class);
    private final EditHistoryRepository editHistoryRepository;
    private final UserPatternRepository userPatternRepository;
    
    /**
     * Generate inline suggestions for code
     */
    public List<InlineSuggestion> generateInlineSuggestions(
            String userId,
            String code,
            String language,
            int cursorPosition,
            String context) {
        
        logger.info("💡 Generating inline suggestions for user: {}", userId);
        
        List<InlineSuggestion> suggestions = new ArrayList<>();
        
        // Get user's preferred patterns
        List<UserPattern> userPatterns = userPatternRepository.findByUserIdAndActiveTrue(userId);
        
        // Analyze code and generate suggestions
        suggestions.addAll(suggestMethodExtraction(code, userId, userPatterns));
        suggestions.addAll(suggestVariableRenaming(code, userId, userPatterns));
        suggestions.addAll(suggestCodeSimplification(code, userId, userPatterns));
        suggestions.addAll(suggestComments(code, userId, userPatterns));
        suggestions.addAll(suggestPatternApplication(code, userId, userPatterns));
        
        // Rank suggestions by user preference
        suggestions = rankSuggestions(suggestions, userId, userPatterns);
        
        logger.info("✅ Generated {} suggestions for user: {}", suggestions.size(), userId);
        
        return suggestions;
    }
    
    /**
     * Suggest method extraction
     */
    private List<InlineSuggestion> suggestMethodExtraction(
            String code,
            String userId,
            List<UserPattern> userPatterns) {
        
        logger.debug("🔍 Analyzing for method extraction opportunities");
        
        List<InlineSuggestion> suggestions = new ArrayList<>();
        
        // Check if user has high acceptance rate for method extraction
        UserPattern extractPattern = userPatterns.stream()
                .filter(p -> "extract_method".equalsIgnoreCase(p.getPatternType()))
                .findFirst()
                .orElse(null);
        
        if (extractPattern != null && extractPattern.getAcceptanceRate() > 0.7) {
            // Look for long methods (more than 10 lines)
            String[] lines = code.split("\n");
            if (lines.length > 10) {
                InlineSuggestion suggestion = InlineSuggestion.builder()
                        .type("extract_method")
                        .title("Extract Method")
                        .description("This method is long and could be split into smaller methods")
                        .suggestion("Consider extracting a helper method for better readability")
                        .confidence(0.85)
                        .priority(1)
                        .lineNumber(1)
                        .build();
                suggestions.add(suggestion);
            }
        }
        
        return suggestions;
    }
    
    /**
     * Suggest variable renaming
     */
    private List<InlineSuggestion> suggestVariableRenaming(
            String code,
            String userId,
            List<UserPattern> userPatterns) {
        
        logger.debug("🔍 Analyzing for variable renaming opportunities");
        
        List<InlineSuggestion> suggestions = new ArrayList<>();
        
        // Check if user has high acceptance rate for renaming
        UserPattern renamePattern = userPatterns.stream()
                .filter(p -> "rename_variable".equalsIgnoreCase(p.getPatternType()))
                .findFirst()
                .orElse(null);
        
        if (renamePattern != null && renamePattern.getAcceptanceRate() > 0.7) {
            // Look for single-letter variable names
            Pattern pattern = Pattern.compile("\\b[a-z]\\s*=");
            Matcher matcher = pattern.matcher(code);
            
            int count = 0;
            while (matcher.find() && count < 3) {
                String variable = matcher.group().trim();
                InlineSuggestion suggestion = InlineSuggestion.builder()
                        .type("rename_variable")
                        .title("Rename Variable")
                        .description("Variable '" + variable + "' has a non-descriptive name")
                        .suggestion("Consider renaming to a more descriptive name")
                        .confidence(0.80)
                        .priority(2)
                        .lineNumber(count + 1)
                        .build();
                suggestions.add(suggestion);
                count++;
            }
        }
        
        return suggestions;
    }
    
    /**
     * Suggest code simplification
     */
    private List<InlineSuggestion> suggestCodeSimplification(
            String code,
            String userId,
            List<UserPattern> userPatterns) {
        
        logger.debug("🔍 Analyzing for code simplification opportunities");
        
        List<InlineSuggestion> suggestions = new ArrayList<>();
        
        // Check if user has high acceptance rate for simplification
        UserPattern simplifyPattern = userPatterns.stream()
                .filter(p -> "simplify_logic".equalsIgnoreCase(p.getPatternType()))
                .findFirst()
                .orElse(null);
        
        if (simplifyPattern != null && simplifyPattern.getAcceptanceRate() > 0.7) {
            // Look for nested if statements
            if (code.contains("if") && code.contains("if")) {
                int ifCount = code.split("if").length - 1;
                if (ifCount > 2) {
                    InlineSuggestion suggestion = InlineSuggestion.builder()
                            .type("simplify_logic")
                            .title("Simplify Logic")
                            .description("Code has nested conditionals that could be simplified")
                            .suggestion("Consider using early returns or combining conditions")
                            .confidence(0.75)
                            .priority(2)
                            .lineNumber(1)
                            .build();
                    suggestions.add(suggestion);
                }
            }
        }
        
        return suggestions;
    }
    
    /**
     * Suggest adding comments
     */
    private List<InlineSuggestion> suggestComments(
            String code,
            String userId,
            List<UserPattern> userPatterns) {
        
        logger.debug("🔍 Analyzing for comment opportunities");
        
        List<InlineSuggestion> suggestions = new ArrayList<>();
        
        // Check if user has high acceptance rate for comments
        UserPattern commentPattern = userPatterns.stream()
                .filter(p -> "add_comments".equalsIgnoreCase(p.getPatternType()))
                .findFirst()
                .orElse(null);
        
        if (commentPattern != null && commentPattern.getAcceptanceRate() > 0.7) {
            // Look for complex logic without comments
            if (code.contains("for") || code.contains("while")) {
                if (!code.contains("//") && !code.contains("/*")) {
                    InlineSuggestion suggestion = InlineSuggestion.builder()
                            .type("add_comments")
                            .title("Add Comments")
                            .description("Complex logic without explanatory comments")
                            .suggestion("Consider adding comments to explain the logic")
                            .confidence(0.70)
                            .priority(3)
                            .lineNumber(1)
                            .build();
                    suggestions.add(suggestion);
                }
            }
        }
        
        return suggestions;
    }
    
    /**
     * Suggest pattern application
     */
    private List<InlineSuggestion> suggestPatternApplication(
            String code,
            String userId,
            List<UserPattern> userPatterns) {
        
        logger.debug("🔍 Analyzing for design pattern opportunities");
        
        List<InlineSuggestion> suggestions = new ArrayList<>();
        
        // Check for singleton pattern opportunity
        if (code.contains("static") && code.contains("getInstance")) {
            InlineSuggestion suggestion = InlineSuggestion.builder()
                    .type("apply_pattern")
                    .title("Apply Singleton Pattern")
                    .description("Code looks like it could use Singleton pattern")
                    .suggestion("Consider implementing Singleton pattern for better control")
                    .confidence(0.65)
                    .priority(3)
                    .lineNumber(1)
                    .build();
            suggestions.add(suggestion);
        }
        
        return suggestions;
    }
    
    /**
     * Rank suggestions by user preference
     */
    private List<InlineSuggestion> rankSuggestions(
            List<InlineSuggestion> suggestions,
            String userId,
            List<UserPattern> userPatterns) {
        
        logger.debug("📊 Ranking suggestions by user preference");
        
        // Sort by priority and confidence
        return suggestions.stream()
                .sorted((a, b) -> {
                    // First by priority (lower is better)
                    int priorityCompare = Integer.compare(a.getPriority(), b.getPriority());
                    if (priorityCompare != 0) return priorityCompare;
                    
                    // Then by confidence (higher is better)
                    return Double.compare(b.getConfidence(), a.getConfidence());
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get context-aware suggestions
     */
    public List<InlineSuggestion> getContextAwareSuggestions(
            String userId,
            String code,
            String currentMethod,
            String currentClass) {
        
        logger.info("🎯 Getting context-aware suggestions for user: {}", userId);
        
        List<InlineSuggestion> suggestions = new ArrayList<>();
        
        // Get user's recent edits for context
        List<EditHistory> recentEdits = editHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (recentEdits.size() > 10) {
            recentEdits = recentEdits.subList(0, 10);
        }
        
        // Analyze recent patterns
        Map<String, Long> editTypeFrequency = recentEdits.stream()
                .collect(Collectors.groupingBy(EditHistory::getEditType, Collectors.counting()));
        
        // Generate suggestions based on context
        if (editTypeFrequency.getOrDefault("extract_method", 0L) > 3) {
            InlineSuggestion suggestion = InlineSuggestion.builder()
                    .type("extract_method")
                    .title("Extract Method")
                    .description("You frequently extract methods - this code might benefit from it")
                    .suggestion("Consider extracting a helper method")
                    .confidence(0.90)
                    .priority(1)
                    .lineNumber(1)
                    .build();
            suggestions.add(suggestion);
        }
        
        logger.info("✅ Generated {} context-aware suggestions", suggestions.size());
        
        return suggestions;
    }
    
    /**
     * Get personalized suggestions based on user patterns
     */
    public List<InlineSuggestion> getPersonalizedSuggestions(
            String userId,
            String code) {
        
        logger.info("👤 Getting personalized suggestions for user: {}", userId);
        
        List<InlineSuggestion> suggestions = new ArrayList<>();
        
        // Get user's most effective patterns
        List<UserPattern> effectivePatterns = userPatternRepository.findByUserIdAndAcceptanceRateGreaterThan(
                userId, 0.75);
        
        // Generate suggestions based on effective patterns
        for (UserPattern pattern : effectivePatterns) {
            InlineSuggestion suggestion = InlineSuggestion.builder()
                    .type(pattern.getPatternType())
                    .title("Suggested: " + pattern.getPatternType())
                    .description("Based on your preferences, you might like this suggestion")
                    .suggestion(pattern.getDescription())
                    .confidence(pattern.getAcceptanceRate())
                    .priority(1)
                    .lineNumber(1)
                    .build();
            suggestions.add(suggestion);
        }
        
        logger.info("✅ Generated {} personalized suggestions", suggestions.size());
        
        return suggestions;
    }
    
    /**
     * Get quick fix suggestions
     */
    public List<InlineSuggestion> getQuickFixSuggestions(
            String userId,
            String code,
            String errorMessage) {
        
        logger.info("🔧 Getting quick fix suggestions for user: {}", userId);
        
        List<InlineSuggestion> suggestions = new ArrayList<>();
        
        // Analyze error and suggest fixes
        if (errorMessage.contains("NullPointerException")) {
            InlineSuggestion suggestion = InlineSuggestion.builder()
                    .type("null_check")
                    .title("Add Null Check")
                    .description("NullPointerException detected")
                    .suggestion("Add null check before using the variable")
                    .confidence(0.95)
                    .priority(1)
                    .lineNumber(1)
                    .build();
            suggestions.add(suggestion);
        }
        
        if (errorMessage.contains("ArrayIndexOutOfBoundsException")) {
            InlineSuggestion suggestion = InlineSuggestion.builder()
                    .type("bounds_check")
                    .title("Add Bounds Check")
                    .description("ArrayIndexOutOfBoundsException detected")
                    .suggestion("Add bounds check before accessing array")
                    .confidence(0.95)
                    .priority(1)
                    .lineNumber(1)
                    .build();
            suggestions.add(suggestion);
        }
        
        logger.info("✅ Generated {} quick fix suggestions", suggestions.size());
        
        return suggestions;
    }
    
    /**
     * Get suggestion by ID
     */
    public InlineSuggestion getSuggestionById(String suggestionId) {
        logger.info("🔍 Fetching suggestion: {}", suggestionId);
        // In a real implementation, this would fetch from database
        return null;
    }
    
    /**
     * Get suggestion history for user
     */
    public List<InlineSuggestion> getSuggestionHistory(String userId, int limit) {
        logger.info("📜 Fetching suggestion history for user: {}", userId);
        // In a real implementation, this would fetch from database
        return new ArrayList<>();
    }
    
    /**
     * Inner class for inline suggestions
     */
    @lombok.Data
    @lombok.Builder
    public static class InlineSuggestion {
        private String type;
        private String title;
        private String description;
        private String suggestion;
        private Double confidence;
        private Integer priority;
        private Integer lineNumber;
        private String code;
        private String replacement;
        private List<String> keywords;
    }
}

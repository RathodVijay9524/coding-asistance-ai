package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 👥 Pair Programming Assistant
 * 
 * Provides real-time coding suggestions:
 * - Detect issues while coding
 * - Offer alternative solutions
 * - Suggest next steps
 * - Explain reasoning
 */
@Component
public class PairProgrammingAssistant {
    
    private static final Logger logger = LoggerFactory.getLogger(PairProgrammingAssistant.class);
    
    private final CodeIntelligenceEngine codeIntelligence;
    
    public PairProgrammingAssistant(CodeIntelligenceEngine codeIntelligence) {
        this.codeIntelligence = codeIntelligence;
    }
    
    /**
     * Provide real-time suggestions for code
     */
    public ProgrammingSuggestions provideSuggestions(String code, String language, String context) {
        if (code == null || code.isEmpty()) {
            logger.warn("⚠️ Empty code provided to PairProgrammingAssistant");
            return new ProgrammingSuggestions();
        }
        
        logger.debug("👥 Analyzing code for suggestions (language={})", language);
        
        ProgrammingSuggestions suggestions = new ProgrammingSuggestions();
        
        // 1. Analyze code using CodeIntelligenceEngine
        CodeIntelligenceEngine.CodeIntelligence intelligence = codeIntelligence.analyzeCode(code, language);
        
        // 2. Detect issues
        List<Suggestion> issues = detectIssues(intelligence);
        suggestions.setIssues(issues);
        
        // 3. Offer alternatives
        List<Alternative> alternatives = offerAlternatives(code, language, intelligence);
        suggestions.setAlternatives(alternatives);
        
        // 4. Suggest next steps
        List<String> nextSteps = suggestNextSteps(code, language, intelligence);
        suggestions.setNextSteps(nextSteps);
        
        // 5. Explain reasoning
        String reasoning = explainReasoning(intelligence);
        suggestions.setReasoning(reasoning);
        
        // 6. Calculate confidence
        double confidence = calculateConfidence(intelligence);
        suggestions.setConfidence(confidence);
        
        logger.debug("✅ Suggestions generated: {} issues, {} alternatives, {} next steps, confidence={}",
                issues.size(), alternatives.size(), nextSteps.size(), confidence);
        
        return suggestions;
    }
    
    /**
     * Detect issues in code
     */
    private List<Suggestion> detectIssues(CodeIntelligenceEngine.CodeIntelligence intelligence) {
        List<Suggestion> issues = new ArrayList<>();
        
        // Add bugs as issues
        for (CodeIntelligenceEngine.Bug bug : intelligence.getBugs()) {
            issues.add(new Suggestion(
                    bug.type,
                    bug.description,
                    "BUG",
                    bug.severity
            ));
        }
        
        // Add refactoring suggestions as issues
        for (CodeIntelligenceEngine.Refactoring refactor : intelligence.getRefactorings()) {
            issues.add(new Suggestion(
                    refactor.type,
                    refactor.description,
                    "REFACTOR",
                    refactor.priority
            ));
        }
        
        // Add performance issues
        for (CodeIntelligenceEngine.PerformanceIssue perf : intelligence.getPerformanceIssues()) {
            issues.add(new Suggestion(
                    perf.type,
                    perf.suggestion,
                    "PERFORMANCE",
                    perf.severity
            ));
        }
        
        logger.debug("   Detected {} issues", issues.size());
        return issues;
    }
    
    /**
     * Offer alternative solutions
     */
    private List<Alternative> offerAlternatives(String code, String language, 
                                               CodeIntelligenceEngine.CodeIntelligence intelligence) {
        List<Alternative> alternatives = new ArrayList<>();
        
        // Alternative 1: Use try-with-resources for resource management
        if (code.contains("new FileInputStream") || code.contains("new FileReader")) {
            alternatives.add(new Alternative(
                    "Use try-with-resources",
                    "try (FileInputStream fis = new FileInputStream(file)) { ... }",
                    "Automatically closes resources",
                    "HIGH"
            ));
        }
        
        // Alternative 2: Use Optional instead of null checks
        if (code.contains(".get(") && code.contains("null")) {
            alternatives.add(new Alternative(
                    "Use Optional",
                    "optional.orElseThrow(() -> new Exception(...))",
                    "Safer null handling",
                    "HIGH"
            ));
        }
        
        // Alternative 3: Use StringBuilder for string concatenation
        if (code.contains("for (") && code.contains("+= \"")) {
            alternatives.add(new Alternative(
                    "Use StringBuilder",
                    "StringBuilder sb = new StringBuilder(); sb.append(...)",
                    "Better performance in loops",
                    "MEDIUM"
            ));
        }
        
        // Alternative 4: Use streams instead of loops
        if (code.contains("for (") && code.contains("if (")) {
            alternatives.add(new Alternative(
                    "Use streams",
                    "list.stream().filter(...).map(...).collect(...)",
                    "More functional and readable",
                    "MEDIUM"
            ));
        }
        
        // Alternative 5: Use constants instead of magic numbers
        if (code.matches(".*\\b(0|1|2|3|4|5|10|100|1000)\\b.*")) {
            alternatives.add(new Alternative(
                    "Extract constants",
                    "private static final int MAX_SIZE = 100;",
                    "More maintainable code",
                    "LOW"
            ));
        }
        
        // Alternative 6: Use prepared statements for SQL
        if (code.contains("\"SELECT") && code.contains("+ ")) {
            alternatives.add(new Alternative(
                    "Use PreparedStatement",
                    "PreparedStatement ps = conn.prepareStatement(sql); ps.setString(...)",
                    "Prevents SQL injection",
                    "CRITICAL"
            ));
        }
        
        logger.debug("   Offered {} alternatives", alternatives.size());
        return alternatives;
    }
    
    /**
     * Suggest next steps
     */
    private List<String> suggestNextSteps(String code, String language,
                                         CodeIntelligenceEngine.CodeIntelligence intelligence) {
        List<String> nextSteps = new ArrayList<>();
        
        // Based on quality score
        if (intelligence.getQualityScore() < 50) {
            nextSteps.add("Fix critical bugs first");
            nextSteps.add("Add error handling");
            nextSteps.add("Add null checks");
        }
        
        if (intelligence.getQualityScore() < 70) {
            nextSteps.add("Refactor long methods");
            nextSteps.add("Extract duplicated code");
            nextSteps.add("Add documentation");
        }
        
        if (intelligence.getQualityScore() < 85) {
            nextSteps.add("Optimize performance");
            nextSteps.add("Add unit tests");
            nextSteps.add("Review code style");
        }
        
        // Based on patterns
        if (intelligence.getPatterns().contains("Spring Component")) {
            nextSteps.add("Add @Transactional if needed");
            nextSteps.add("Add logging");
        }
        
        if (!intelligence.getPatterns().contains("Functional Programming") && code.contains("for (")) {
            nextSteps.add("Consider using streams");
        }
        
        // Default suggestions
        if (nextSteps.isEmpty()) {
            nextSteps.add("Add unit tests");
            nextSteps.add("Review error handling");
            nextSteps.add("Add documentation");
        }
        
        logger.debug("   Suggested {} next steps", nextSteps.size());
        return nextSteps;
    }
    
    /**
     * Explain reasoning for suggestions
     */
    private String explainReasoning(CodeIntelligenceEngine.CodeIntelligence intelligence) {
        StringBuilder reasoning = new StringBuilder();
        
        reasoning.append("Code Analysis Summary:\n");
        reasoning.append(String.format("- Quality Score: %.1f/100\n", intelligence.getQualityScore()));
        reasoning.append(String.format("- Bugs Found: %d\n", intelligence.getBugs().size()));
        reasoning.append(String.format("- Refactoring Opportunities: %d\n", intelligence.getRefactorings().size()));
        reasoning.append(String.format("- Performance Issues: %d\n", intelligence.getPerformanceIssues().size()));
        reasoning.append(String.format("- Patterns Recognized: %d\n", intelligence.getPatterns().size()));
        
        if (!intelligence.getBugs().isEmpty()) {
            reasoning.append("\nTop Bugs:\n");
            intelligence.getBugs().stream()
                    .limit(3)
                    .forEach(b -> reasoning.append(String.format("  - %s: %s\n", b.type, b.description)));
        }
        
        if (!intelligence.getPatterns().isEmpty()) {
            reasoning.append("\nPatterns Detected:\n");
            intelligence.getPatterns().stream()
                    .limit(3)
                    .forEach(p -> reasoning.append(String.format("  - %s\n", p)));
        }
        
        return reasoning.toString();
    }
    
    /**
     * Calculate confidence in suggestions (0-1)
     */
    private double calculateConfidence(CodeIntelligenceEngine.CodeIntelligence intelligence) {
        double confidence = 0.8; // Base confidence
        
        // Reduce confidence if many bugs
        if (intelligence.getBugs().size() > 5) {
            confidence -= 0.1;
        }
        
        // Increase confidence if patterns recognized
        if (!intelligence.getPatterns().isEmpty()) {
            confidence += 0.1;
        }
        
        // Reduce confidence if low quality
        if (intelligence.getQualityScore() < 50) {
            confidence -= 0.2;
        }
        
        return Math.min(1.0, Math.max(0, confidence));
    }
    
    /**
     * Programming Suggestions DTO
     */
    public static class ProgrammingSuggestions {
        private List<Suggestion> issues = new ArrayList<>();
        private List<Alternative> alternatives = new ArrayList<>();
        private List<String> nextSteps = new ArrayList<>();
        private String reasoning = "";
        private double confidence = 0.0;
        
        public List<Suggestion> getIssues() { return issues; }
        public void setIssues(List<Suggestion> issues) { this.issues = issues; }
        
        public List<Alternative> getAlternatives() { return alternatives; }
        public void setAlternatives(List<Alternative> alternatives) { this.alternatives = alternatives; }
        
        public List<String> getNextSteps() { return nextSteps; }
        public void setNextSteps(List<String> nextSteps) { this.nextSteps = nextSteps; }
        
        public String getReasoning() { return reasoning; }
        public void setReasoning(String reasoning) { this.reasoning = reasoning; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        
        @Override
        public String toString() {
            return String.format("ProgrammingSuggestions{issues=%d, alternatives=%d, nextSteps=%d, confidence=%.2f}",
                    issues.size(), alternatives.size(), nextSteps.size(), confidence);
        }
    }
    
    /**
     * Suggestion DTO
     */
    public static class Suggestion {
        public String title;
        public String description;
        public String type; // BUG, REFACTOR, PERFORMANCE
        public String severity; // CRITICAL, HIGH, MEDIUM, LOW
        
        public Suggestion(String title, String description, String type, String severity) {
            this.title = title;
            this.description = description;
            this.type = type;
            this.severity = severity;
        }
        
        @Override
        public String toString() {
            return String.format("[%s-%s] %s: %s", type, severity, title, description);
        }
    }
    
    /**
     * Alternative Solution DTO
     */
    public static class Alternative {
        public String title;
        public String code;
        public String benefit;
        public String priority; // CRITICAL, HIGH, MEDIUM, LOW
        
        public Alternative(String title, String code, String benefit, String priority) {
            this.title = title;
            this.code = code;
            this.benefit = benefit;
            this.priority = priority;
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s: %s", priority, title, benefit);
        }
    }
}

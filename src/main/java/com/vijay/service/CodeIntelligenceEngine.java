package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 🧠 Code Intelligence Engine
 * 
 * Analyzes code for:
 * - Bug detection (null pointers, resource leaks)
 * - Refactoring suggestions (long methods, duplicates)
 * - Pattern recognition (Spring, Interfaces)
 * - Performance issues
 */
@Component
public class CodeIntelligenceEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(CodeIntelligenceEngine.class);
    
    /**
     * Analyze code for bugs, refactorings, patterns, and performance issues
     */
    public CodeIntelligence analyzeCode(String code, String language) {
        if (code == null || code.isEmpty()) {
            logger.warn("⚠️ Empty code provided to CodeIntelligenceEngine");
            return new CodeIntelligence();
        }
        
        logger.debug("🔍 Analyzing code ({} lines, language={})", code.split("\n").length, language);
        
        CodeIntelligence intelligence = new CodeIntelligence();
        
        // 1. Detect bugs
        List<Bug> bugs = detectBugs(code, language);
        intelligence.setBugs(bugs);
        
        // 2. Suggest refactorings
        List<Refactoring> refactorings = suggestRefactorings(code, language);
        intelligence.setRefactorings(refactorings);
        
        // 3. Recognize patterns
        List<String> patterns = recognizePatterns(code, language);
        intelligence.setPatterns(patterns);
        
        // 4. Detect performance issues
        List<PerformanceIssue> performanceIssues = detectPerformanceIssues(code, language);
        intelligence.setPerformanceIssues(performanceIssues);
        
        // 5. Calculate overall code quality score
        double qualityScore = calculateQualityScore(bugs, refactorings, performanceIssues);
        intelligence.setQualityScore(qualityScore);
        
        logger.debug("✅ Code analysis complete: {} bugs, {} refactorings, {} patterns, {} perf issues, quality={}",
                bugs.size(), refactorings.size(), patterns.size(), performanceIssues.size(), qualityScore);
        
        return intelligence;
    }
    
    /**
     * Detect bugs in code
     */
    private List<Bug> detectBugs(String code, String language) {
        List<Bug> bugs = new ArrayList<>();
        
        // Null pointer detection
        if (code.contains(".get(") && !code.contains("isPresent()") && !code.contains("orElse")) {
            bugs.add(new Bug("Potential null pointer", "Missing null check after .get()", "HIGH"));
        }
        
        // Resource leak detection
        if (code.contains("new FileInputStream") && !code.contains("try-with-resources")) {
            bugs.add(new Bug("Resource leak", "FileInputStream not closed properly", "HIGH"));
        }
        
        if (code.contains("new FileReader") && !code.contains("try-with-resources")) {
            bugs.add(new Bug("Resource leak", "FileReader not closed properly", "HIGH"));
        }
        
        // SQL injection detection
        if (code.contains("\"SELECT") && code.contains("+ ") && !code.contains("PreparedStatement")) {
            bugs.add(new Bug("SQL injection", "String concatenation in SQL query", "CRITICAL"));
        }
        
        // Unhandled exception
        if (code.contains("throw new") && !code.contains("catch")) {
            bugs.add(new Bug("Unhandled exception", "Exception thrown but not caught", "MEDIUM"));
        }
        
        // Infinite loop detection
        if (code.contains("while(true)") || code.contains("while (true)")) {
            bugs.add(new Bug("Infinite loop", "Infinite loop detected", "HIGH"));
        }
        
        logger.debug("   Detected {} bugs", bugs.size());
        return bugs;
    }
    
    /**
     * Suggest refactorings
     */
    private List<Refactoring> suggestRefactorings(String code, String language) {
        List<Refactoring> refactorings = new ArrayList<>();
        
        // Long method detection
        int lineCount = code.split("\n").length;
        if (lineCount > 50) {
            refactorings.add(new Refactoring("Extract method", "Method is too long (" + lineCount + " lines)", "MEDIUM"));
        }
        
        // Code duplication detection
        if (code.contains("if (") && code.split("if \\(").length > 5) {
            refactorings.add(new Refactoring("Extract condition", "Multiple similar if statements", "MEDIUM"));
        }
        
        // Magic numbers
        if (code.matches(".*\\b(0|1|2|3|4|5|10|100|1000)\\b.*")) {
            refactorings.add(new Refactoring("Extract constant", "Magic numbers should be constants", "LOW"));
        }
        
        // Nested loops
        if (code.contains("for (") && code.split("for \\(").length > 2) {
            refactorings.add(new Refactoring("Reduce nesting", "Deeply nested loops", "MEDIUM"));
        }
        
        // Large class detection
        if (code.split("public ").length > 10) {
            refactorings.add(new Refactoring("Split class", "Class has too many methods", "MEDIUM"));
        }
        
        // Missing documentation
        if (!code.contains("/**") && lineCount > 20) {
            refactorings.add(new Refactoring("Add documentation", "Missing JavaDoc comments", "LOW"));
        }
        
        logger.debug("   Suggested {} refactorings", refactorings.size());
        return refactorings;
    }
    
    /**
     * Recognize design patterns
     */
    private List<String> recognizePatterns(String code, String language) {
        List<String> patterns = new ArrayList<>();
        
        // Spring patterns
        if (code.contains("@Component") || code.contains("@Service")) {
            patterns.add("Spring Component");
        }
        
        if (code.contains("@Autowired") || code.contains("@Inject")) {
            patterns.add("Dependency Injection");
        }
        
        if (code.contains("@RestController") || code.contains("@RequestMapping")) {
            patterns.add("Spring REST Controller");
        }
        
        // Design patterns
        if (code.contains("public static") && code.contains("getInstance")) {
            patterns.add("Singleton Pattern");
        }
        
        if (code.contains("implements Runnable") || code.contains("extends Thread")) {
            patterns.add("Thread Pattern");
        }
        
        if (code.contains("interface ") && code.split("interface ").length > 2) {
            patterns.add("Interface Segregation");
        }
        
        if (code.contains("abstract class")) {
            patterns.add("Abstract Base Class");
        }
        
        // Functional patterns
        if (code.contains(".stream()") || code.contains(".map(") || code.contains(".filter(")) {
            patterns.add("Functional Programming");
        }
        
        if (code.contains("lambda") || code.contains("->")) {
            patterns.add("Lambda Expression");
        }
        
        logger.debug("   Recognized {} patterns", patterns.size());
        return patterns;
    }
    
    /**
     * Detect performance issues
     */
    private List<PerformanceIssue> detectPerformanceIssues(String code, String language) {
        List<PerformanceIssue> issues = new ArrayList<>();
        
        // String concatenation in loop
        if (code.contains("for (") && code.contains("+= \"")) {
            issues.add(new PerformanceIssue("String concatenation", "Use StringBuilder in loops", "MEDIUM"));
        }
        
        // Database query in loop
        if (code.contains("for (") && code.contains("query(") || code.contains("select(")) {
            issues.add(new PerformanceIssue("N+1 query", "Database query in loop", "HIGH"));
        }
        
        // Missing index
        if (code.contains(".indexOf(") || code.contains(".contains(")) {
            issues.add(new PerformanceIssue("Linear search", "Consider using indexed data structure", "MEDIUM"));
        }
        
        // Inefficient collection
        if (code.contains("new ArrayList()") && code.contains(".get(")) {
            issues.add(new PerformanceIssue("Inefficient access", "ArrayList.get() is O(n) in loop", "LOW"));
        }
        
        // Synchronization overhead
        if (code.contains("synchronized")) {
            issues.add(new PerformanceIssue("Synchronization", "Consider using concurrent collections", "MEDIUM"));
        }
        
        // Reflection usage
        if (code.contains(".getClass()") || code.contains("Class.forName")) {
            issues.add(new PerformanceIssue("Reflection", "Reflection is slow, cache if possible", "LOW"));
        }
        
        logger.debug("   Detected {} performance issues", issues.size());
        return issues;
    }
    
    /**
     * Calculate overall code quality score (0-100)
     */
    private double calculateQualityScore(List<Bug> bugs, List<Refactoring> refactorings, 
                                        List<PerformanceIssue> performanceIssues) {
        double score = 100.0;
        
        // Deduct for bugs
        score -= bugs.stream()
                .mapToDouble(b -> b.severity.equals("CRITICAL") ? 20 : 
                           b.severity.equals("HIGH") ? 10 : 5)
                .sum();
        
        // Deduct for refactorings
        score -= refactorings.stream()
                .mapToDouble(r -> r.priority.equals("MEDIUM") ? 5 : 2)
                .sum();
        
        // Deduct for performance issues
        score -= performanceIssues.stream()
                .mapToDouble(p -> p.severity.equals("HIGH") ? 8 : 3)
                .sum();
        
        return Math.max(0, Math.min(100, score));
    }
    
    /**
     * Code Intelligence DTO
     */
    public static class CodeIntelligence {
        private List<Bug> bugs = new ArrayList<>();
        private List<Refactoring> refactorings = new ArrayList<>();
        private List<String> patterns = new ArrayList<>();
        private List<PerformanceIssue> performanceIssues = new ArrayList<>();
        private double qualityScore = 100.0;
        
        public List<Bug> getBugs() { return bugs; }
        public void setBugs(List<Bug> bugs) { this.bugs = bugs; }
        
        public List<Refactoring> getRefactorings() { return refactorings; }
        public void setRefactorings(List<Refactoring> refactorings) { this.refactorings = refactorings; }
        
        public List<String> getPatterns() { return patterns; }
        public void setPatterns(List<String> patterns) { this.patterns = patterns; }
        
        public List<PerformanceIssue> getPerformanceIssues() { return performanceIssues; }
        public void setPerformanceIssues(List<PerformanceIssue> performanceIssues) { this.performanceIssues = performanceIssues; }
        
        public double getQualityScore() { return qualityScore; }
        public void setQualityScore(double qualityScore) { this.qualityScore = qualityScore; }
        
        @Override
        public String toString() {
            return String.format("CodeIntelligence{bugs=%d, refactorings=%d, patterns=%d, perf=%d, quality=%.1f}",
                    bugs.size(), refactorings.size(), patterns.size(), performanceIssues.size(), qualityScore);
        }
    }
    
    /**
     * Bug DTO
     */
    public static class Bug {
        public String type;
        public String description;
        public String severity; // CRITICAL, HIGH, MEDIUM, LOW
        
        public Bug(String type, String description, String severity) {
            this.type = type;
            this.description = description;
            this.severity = severity;
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s: %s", severity, type, description);
        }
    }
    
    /**
     * Refactoring DTO
     */
    public static class Refactoring {
        public String type;
        public String description;
        public String priority; // HIGH, MEDIUM, LOW
        
        public Refactoring(String type, String description, String priority) {
            this.type = type;
            this.description = description;
            this.priority = priority;
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s: %s", priority, type, description);
        }
    }
    
    /**
     * Performance Issue DTO
     */
    public static class PerformanceIssue {
        public String type;
        public String suggestion;
        public String severity; // HIGH, MEDIUM, LOW
        
        public PerformanceIssue(String type, String suggestion, String severity) {
            this.type = type;
            this.suggestion = suggestion;
            this.severity = severity;
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s: %s", severity, type, suggestion);
        }
    }
}

package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 🤖 ML Pattern Detection Service
 * 
 * Uses machine learning techniques for code pattern detection including:
 * - Design pattern recognition
 * - Anti-pattern detection
 * - Code clone detection
 * - Anomaly detection
 * - Pattern frequency analysis
 * - Predictive refactoring suggestions
 * 
 * ✅ PHASE 3.2: Advanced Features
 * Uses statistical analysis and pattern matching for ML-like behavior
 */
@Service
public class MLPatternDetectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(MLPatternDetectionService.class);
    
    // Pattern signatures for design patterns
    private static final Map<String, PatternSignature> DESIGN_PATTERNS = new HashMap<>();
    
    static {
        // Singleton pattern
        DESIGN_PATTERNS.put("Singleton", new PatternSignature(
            "Singleton",
            Arrays.asList("private static", "getInstance", "private constructor"),
            "Ensures single instance of a class"
        ));
        
        // Factory pattern
        DESIGN_PATTERNS.put("Factory", new PatternSignature(
            "Factory",
            Arrays.asList("create", "new", "interface", "abstract"),
            "Creates objects without specifying exact classes"
        ));
        
        // Observer pattern
        DESIGN_PATTERNS.put("Observer", new PatternSignature(
            "Observer",
            Arrays.asList("notify", "update", "listener", "event"),
            "Defines one-to-many dependency between objects"
        ));
        
        // Strategy pattern
        DESIGN_PATTERNS.put("Strategy", new PatternSignature(
            "Strategy",
            Arrays.asList("interface", "algorithm", "context", "execute"),
            "Encapsulates interchangeable algorithms"
        ));
        
        // Decorator pattern
        DESIGN_PATTERNS.put("Decorator", new PatternSignature(
            "Decorator",
            Arrays.asList("wrap", "component", "delegate", "add"),
            "Adds new functionality to objects dynamically"
        ));
    }
    
    /**
     * Detect design patterns in code
     */
    public List<PatternMatch> detectDesignPatterns(String sourceCode) {
        List<PatternMatch> matches = new ArrayList<>();
        
        try {
            String codeLower = sourceCode.toLowerCase();
            
            for (Map.Entry<String, PatternSignature> entry : DESIGN_PATTERNS.entrySet()) {
                PatternSignature signature = entry.getValue();
                int matchScore = 0;
                
                // Calculate match score based on signature keywords
                for (String keyword : signature.getKeywords()) {
                    if (codeLower.contains(keyword.toLowerCase())) {
                        matchScore++;
                    }
                }
                
                // If enough keywords match, it's likely this pattern
                if (matchScore >= signature.getKeywords().size() * 0.6) {
                    matches.add(new PatternMatch(
                        signature.getName(),
                        "Design Pattern",
                        matchScore / (double) signature.getKeywords().size(),
                        signature.getDescription()
                    ));
                }
            }
            
            logger.info("✅ Detected {} design patterns", matches.size());
            
        } catch (Exception e) {
            logger.error("❌ Design pattern detection failed: {}", e.getMessage());
        }
        
        return matches;
    }
    
    /**
     * Detect anti-patterns in code
     */
    public List<AntiPattern> detectAntiPatterns(String sourceCode) {
        List<AntiPattern> antiPatterns = new ArrayList<>();
        
        try {
            String codeLower = sourceCode.toLowerCase();
            
            // God Object detection
            if (countOccurrences(sourceCode, "public") > 20 && 
                countOccurrences(sourceCode, "private") < 5) {
                antiPatterns.add(new AntiPattern(
                    "God Object",
                    "Class has too many public methods and low encapsulation",
                    "High",
                    "Break into smaller, focused classes"
                ));
            }
            
            // Spaghetti Code detection
            if (countOccurrences(sourceCode, "goto|continue|break") > 5) {
                antiPatterns.add(new AntiPattern(
                    "Spaghetti Code",
                    "Complex control flow with multiple jumps",
                    "High",
                    "Refactor to use structured programming"
                ));
            }
            
            // Magic Numbers detection
            if (countOccurrences(sourceCode, "\\d{3,}") > 10) {
                antiPatterns.add(new AntiPattern(
                    "Magic Numbers",
                    "Hard-coded numeric values without explanation",
                    "Medium",
                    "Extract to named constants"
                ));
            }
            
            // Null checking hell
            if (countOccurrences(sourceCode, "!= null|== null") > 8) {
                antiPatterns.add(new AntiPattern(
                    "Null Checking Hell",
                    "Excessive null checks throughout code",
                    "Medium",
                    "Use Optional or null object pattern"
                ));
            }
            
            // Duplicate code
            if (hasDuplicateCode(sourceCode)) {
                antiPatterns.add(new AntiPattern(
                    "Duplicate Code",
                    "Similar code blocks repeated multiple times",
                    "Medium",
                    "Extract to shared method"
                ));
            }
            
            logger.info("✅ Detected {} anti-patterns", antiPatterns.size());
            
        } catch (Exception e) {
            logger.error("❌ Anti-pattern detection failed: {}", e.getMessage());
        }
        
        return antiPatterns;
    }
    
    /**
     * Detect code clones
     */
    public List<CodeClone> detectCodeClones(String sourceCode) {
        List<CodeClone> clones = new ArrayList<>();
        
        try {
            String[] lines = sourceCode.split("\n");
            Map<String, List<Integer>> lineSignatures = new HashMap<>();
            
            // Create signatures for each line
            for (int i = 0; i < lines.length; i++) {
                String normalized = normalizeCodeLine(lines[i]);
                if (!normalized.isEmpty()) {
                    lineSignatures.computeIfAbsent(normalized, k -> new ArrayList<>()).add(i);
                }
            }
            
            // Find duplicate lines
            for (Map.Entry<String, List<Integer>> entry : lineSignatures.entrySet()) {
                if (entry.getValue().size() > 2) {
                    clones.add(new CodeClone(
                        "Type 1 Clone",
                        entry.getValue().size(),
                        entry.getValue(),
                        "Identical code blocks"
                    ));
                }
            }
            
            logger.info("✅ Detected {} code clones", clones.size());
            
        } catch (Exception e) {
            logger.error("❌ Code clone detection failed: {}", e.getMessage());
        }
        
        return clones;
    }
    
    /**
     * Detect anomalies in code
     */
    public List<CodeAnomaly> detectAnomalies(String sourceCode) {
        List<CodeAnomaly> anomalies = new ArrayList<>();
        
        try {
            // Method length anomaly
            int avgMethodLength = estimateAverageMethodLength(sourceCode);
            if (avgMethodLength > 100) {
                anomalies.add(new CodeAnomaly(
                    "Long Methods",
                    "Average method length: " + avgMethodLength + " lines",
                    "High",
                    0.85
                ));
            }
            
            // Cyclomatic complexity anomaly
            int complexity = calculateComplexity(sourceCode);
            if (complexity > 15) {
                anomalies.add(new CodeAnomaly(
                    "High Complexity",
                    "Cyclomatic complexity: " + complexity,
                    "High",
                    0.90
                ));
            }
            
            // Naming convention anomaly
            if (hasWeakNaming(sourceCode)) {
                anomalies.add(new CodeAnomaly(
                    "Weak Naming",
                    "Variables with single letters or unclear names",
                    "Medium",
                    0.70
                ));
            }
            
            // Comment density anomaly
            int commentDensity = calculateCommentDensity(sourceCode);
            if (commentDensity < 5) {
                anomalies.add(new CodeAnomaly(
                    "Low Documentation",
                    "Comment density: " + commentDensity + "%",
                    "Low",
                    0.60
                ));
            }
            
            logger.info("✅ Detected {} anomalies", anomalies.size());
            
        } catch (Exception e) {
            logger.error("❌ Anomaly detection failed: {}", e.getMessage());
        }
        
        return anomalies;
    }
    
    /**
     * Predict refactoring opportunities
     */
    public List<RefactoringOpportunity> predictRefactoringOpportunities(String sourceCode) {
        List<RefactoringOpportunity> opportunities = new ArrayList<>();
        
        try {
            // Based on detected patterns and anomalies
            List<AntiPattern> antiPatterns = detectAntiPatterns(sourceCode);
            List<CodeAnomaly> anomalies = detectAnomalies(sourceCode);
            
            // Generate refactoring suggestions
            for (AntiPattern antiPattern : antiPatterns) {
                opportunities.add(new RefactoringOpportunity(
                    antiPattern.getName(),
                    antiPattern.getSuggestion(),
                    calculatePriority(antiPattern.getSeverity()),
                    estimateEffort(antiPattern.getName()),
                    calculateImpact(antiPattern.getName())
                ));
            }
            
            for (CodeAnomaly anomaly : anomalies) {
                opportunities.add(new RefactoringOpportunity(
                    anomaly.getType(),
                    "Refactor to improve: " + anomaly.getDescription(),
                    calculatePriority(anomaly.getSeverity()),
                    estimateEffort(anomaly.getType()),
                    anomaly.getConfidence()
                ));
            }
            
            logger.info("✅ Predicted {} refactoring opportunities", opportunities.size());
            
        } catch (Exception e) {
            logger.error("❌ Refactoring prediction failed: {}", e.getMessage());
        }
        
        return opportunities;
    }
    
    // Helper methods
    
    private int countOccurrences(String text, String pattern) {
        try {
            return text.split(pattern, -1).length - 1;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private boolean hasDuplicateCode(String sourceCode) {
        String[] lines = sourceCode.split("\n");
        Set<String> seen = new HashSet<>();
        
        for (String line : lines) {
            String normalized = normalizeCodeLine(line);
            if (!normalized.isEmpty()) {
                if (seen.contains(normalized)) {
                    return true;
                }
                seen.add(normalized);
            }
        }
        
        return false;
    }
    
    private String normalizeCodeLine(String line) {
        return line.trim()
            .replaceAll("\\s+", " ")
            .replaceAll("//.*", "")
            .trim();
    }
    
    private int estimateAverageMethodLength(String sourceCode) {
        int methodCount = countOccurrences(sourceCode, "\\{");
        int lineCount = sourceCode.split("\n").length;
        return methodCount > 0 ? lineCount / methodCount : 0;
    }
    
    private int calculateComplexity(String sourceCode) {
        int complexity = 1;
        complexity += countOccurrences(sourceCode, "if\\s*\\(");
        complexity += countOccurrences(sourceCode, "for\\s*\\(");
        complexity += countOccurrences(sourceCode, "while\\s*\\(");
        complexity += countOccurrences(sourceCode, "catch\\s*\\(");
        complexity += countOccurrences(sourceCode, "case\\s+");
        return complexity;
    }
    
    private boolean hasWeakNaming(String sourceCode) {
        return countOccurrences(sourceCode, "\\b[a-z]\\b|\\b[a-z]{1,2}\\b") > 10;
    }
    
    private int calculateCommentDensity(String sourceCode) {
        int commentLines = countOccurrences(sourceCode, "//|/\\*|\\*/");
        int totalLines = sourceCode.split("\n").length;
        return totalLines > 0 ? (commentLines * 100) / totalLines : 0;
    }
    
    private int calculatePriority(String severity) {
        return "High".equals(severity) ? 1 : "Medium".equals(severity) ? 2 : 3;
    }
    
    private int estimateEffort(String type) {
        if (type.contains("Duplicate") || type.contains("Clone")) return 2;
        if (type.contains("Long") || type.contains("Complex")) return 3;
        if (type.contains("Naming")) return 1;
        return 2;
    }
    
    private double calculateImpact(String type) {
        if (type.contains("Duplicate") || type.contains("Clone")) return 0.85;
        if (type.contains("Long") || type.contains("Complex")) return 0.90;
        if (type.contains("Naming")) return 0.60;
        return 0.70;
    }
    
    // Inner classes
    
    public static class PatternSignature {
        private String name;
        private List<String> keywords;
        private String description;
        
        public PatternSignature(String name, List<String> keywords, String description) {
            this.name = name;
            this.keywords = keywords;
            this.description = description;
        }
        
        public String getName() { return name; }
        public List<String> getKeywords() { return keywords; }
        public String getDescription() { return description; }
    }
    
    public static class PatternMatch {
        private String name;
        private String type;
        private double confidence;
        private String description;
        
        public PatternMatch(String name, String type, double confidence, String description) {
            this.name = name;
            this.type = type;
            this.confidence = confidence;
            this.description = description;
        }
        
        public String getName() { return name; }
        public String getType() { return type; }
        public double getConfidence() { return confidence; }
        public String getDescription() { return description; }
    }
    
    public static class AntiPattern {
        private String name;
        private String description;
        private String severity;
        private String suggestion;
        
        public AntiPattern(String name, String description, String severity, String suggestion) {
            this.name = name;
            this.description = description;
            this.severity = severity;
            this.suggestion = suggestion;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getSeverity() { return severity; }
        public String getSuggestion() { return suggestion; }
    }
    
    public static class CodeClone {
        private String type;
        private int occurrences;
        private List<Integer> locations;
        private String description;
        
        public CodeClone(String type, int occurrences, List<Integer> locations, String description) {
            this.type = type;
            this.occurrences = occurrences;
            this.locations = locations;
            this.description = description;
        }
        
        public String getType() { return type; }
        public int getOccurrences() { return occurrences; }
        public List<Integer> getLocations() { return locations; }
        public String getDescription() { return description; }
    }
    
    public static class CodeAnomaly {
        private String type;
        private String description;
        private String severity;
        private double confidence;
        
        public CodeAnomaly(String type, String description, String severity, double confidence) {
            this.type = type;
            this.description = description;
            this.severity = severity;
            this.confidence = confidence;
        }
        
        public String getType() { return type; }
        public String getDescription() { return description; }
        public String getSeverity() { return severity; }
        public double getConfidence() { return confidence; }
    }
    
    public static class RefactoringOpportunity {
        private String name;
        private String suggestion;
        private int priority;
        private int estimatedEffort;
        private double expectedImpact;
        
        public RefactoringOpportunity(String name, String suggestion, int priority, int effort, double impact) {
            this.name = name;
            this.suggestion = suggestion;
            this.priority = priority;
            this.estimatedEffort = effort;
            this.expectedImpact = impact;
        }
        
        public String getName() { return name; }
        public String getSuggestion() { return suggestion; }
        public int getPriority() { return priority; }
        public int getEstimatedEffort() { return estimatedEffort; }
        public double getExpectedImpact() { return expectedImpact; }
    }
}

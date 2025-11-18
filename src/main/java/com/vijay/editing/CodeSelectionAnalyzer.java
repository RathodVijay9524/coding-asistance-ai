package com.vijay.editing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.service.ASTAnalysisService;
import com.vijay.service.MLPatternDetectionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 🔍 CODE SELECTION ANALYZER
 * 
 * Analyzes selected code to understand its structure, patterns, and issues.
 * Provides detailed information about what the user has selected.
 * 
 * ✅ PHASE 2: Intelligent Editing - Week 1
 */
@Service
@RequiredArgsConstructor
public class CodeSelectionAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(CodeSelectionAnalyzer.class);
    private final ObjectMapper objectMapper;
    private final ASTAnalysisService astAnalysisService;
    private final MLPatternDetectionService mlPatternDetectionService;
    
    /**
     * Analyze selected code and provide detailed information
     */
    public SelectionAnalysis analyzeSelection(String selectedCode) {
        logger.info("🔍 Analyzing selected code ({} chars)", selectedCode.length());
        
        try {
            SelectionAnalysis analysis = new SelectionAnalysis();
            
            // Basic metrics
            analysis.setCodeLength(selectedCode.length());
            analysis.setLineCount(countLines(selectedCode));
            analysis.setLanguage(detectLanguage(selectedCode));
            
            // Structure analysis
            analysis.setMethods(extractMethods(selectedCode));
            analysis.setClasses(extractClasses(selectedCode));
            analysis.setVariables(extractVariables(selectedCode));
            
            // Pattern analysis
            analysis.setPatterns(detectPatterns(selectedCode));
            analysis.setAntiPatterns(detectAntiPatterns(selectedCode));
            
            // Issue detection
            analysis.setCodeSmells(detectCodeSmells(selectedCode));
            analysis.setIssues(detectIssues(selectedCode));
            
            // Complexity metrics
            analysis.setComplexity(calculateComplexity(selectedCode));
            analysis.setMaintainability(calculateMaintainability(selectedCode));
            
            // Refactoring opportunities
            analysis.setRefactoringOpportunities(identifyRefactoringOpportunities(selectedCode));
            
            logger.info("✅ Analysis complete: {} lines, {} methods, {} issues", 
                analysis.getLineCount(), analysis.getMethods().size(), analysis.getIssues().size());
            
            return analysis;
            
        } catch (Exception e) {
            logger.error("❌ Analysis failed: {}", e.getMessage());
            return new SelectionAnalysis();
        }
    }
    
    /**
     * Detect patterns in selected code
     */
    public List<PatternInfo> detectPatterns(String code) {
        logger.info("🔍 Detecting patterns");
        
        List<PatternInfo> patterns = new ArrayList<>();
        
        // Singleton pattern
        if (code.contains("private static") && code.contains("getInstance")) {
            patterns.add(new PatternInfo("Singleton", "Single instance management", 0.9));
        }
        
        // Factory pattern
        if (code.contains("create") && code.contains("new") && code.contains("return")) {
            patterns.add(new PatternInfo("Factory", "Object creation abstraction", 0.8));
        }
        
        // Observer pattern
        if (code.contains("addListener") || code.contains("addEventListener")) {
            patterns.add(new PatternInfo("Observer", "Event handling", 0.85));
        }
        
        // Strategy pattern
        if (code.contains("interface") && code.contains("implement")) {
            patterns.add(new PatternInfo("Strategy", "Algorithm encapsulation", 0.75));
        }
        
        // Decorator pattern
        if (code.contains("extends") && code.contains("wrap")) {
            patterns.add(new PatternInfo("Decorator", "Behavior extension", 0.7));
        }
        
        return patterns;
    }
    
    /**
     * Detect anti-patterns in selected code
     */
    public List<AntiPatternInfo> detectAntiPatterns(String code) {
        logger.info("🔍 Detecting anti-patterns");
        
        List<AntiPatternInfo> antiPatterns = new ArrayList<>();
        
        // God Object
        int methodCount = countOccurrences(code, "public ");
        if (methodCount > 20) {
            antiPatterns.add(new AntiPatternInfo("God Object", "Too many responsibilities", methodCount, "High"));
        }
        
        // Spaghetti Code
        int ifCount = countOccurrences(code, "if");
        int nestedLevel = calculateNestingLevel(code);
        if (ifCount > 10 && nestedLevel > 4) {
            antiPatterns.add(new AntiPatternInfo("Spaghetti Code", "Complex control flow", ifCount, "High"));
        }
        
        // Magic Numbers
        Pattern magicPattern = Pattern.compile("\\b\\d{3,}\\b");
        Matcher matcher = magicPattern.matcher(code);
        int magicCount = 0;
        while (matcher.find()) magicCount++;
        if (magicCount > 5) {
            antiPatterns.add(new AntiPatternInfo("Magic Numbers", "Hard-coded values", magicCount, "Medium"));
        }
        
        // Null Checking Hell
        int nullChecks = countOccurrences(code, "null");
        if (nullChecks > 10) {
            antiPatterns.add(new AntiPatternInfo("Null Checking Hell", "Excessive null checks", nullChecks, "Medium"));
        }
        
        // Duplicate Code
        if (code.length() > 500) {
            antiPatterns.add(new AntiPatternInfo("Potential Duplication", "Code may be duplicated", 1, "Low"));
        }
        
        return antiPatterns;
    }
    
    /**
     * Detect code smells
     */
    public List<CodeSmellInfo> detectCodeSmells(String code) {
        logger.info("🔍 Detecting code smells");
        
        List<CodeSmellInfo> smells = new ArrayList<>();
        
        // Long method
        int lineCount = countLines(code);
        if (lineCount > 50) {
            smells.add(new CodeSmellInfo("Long Method", "Method exceeds 50 lines", lineCount, "Medium"));
        }
        
        // High complexity
        int complexity = calculateComplexity(code);
        if (complexity > 10) {
            smells.add(new CodeSmellInfo("High Complexity", "Cyclomatic complexity > 10", complexity, "High"));
        }
        
        // Long parameter list
        Pattern paramPattern = Pattern.compile("\\((.*?)\\)");
        Matcher matcher = paramPattern.matcher(code);
        if (matcher.find()) {
            String params = matcher.group(1);
            int paramCount = params.isEmpty() ? 0 : params.split(",").length;
            if (paramCount > 5) {
                smells.add(new CodeSmellInfo("Long Parameter List", "More than 5 parameters", paramCount, "Medium"));
            }
        }
        
        // Duplicate code blocks
        if (code.length() > 300) {
            smells.add(new CodeSmellInfo("Potential Duplication", "Code block may be duplicated", 1, "Low"));
        }
        
        return smells;
    }
    
    /**
     * Detect issues in selected code
     */
    public List<IssueInfo> detectIssues(String code) {
        logger.info("🔍 Detecting issues");
        
        List<IssueInfo> issues = new ArrayList<>();
        
        // TODO/FIXME comments
        if (code.contains("TODO")) {
            issues.add(new IssueInfo("TODO Found", "Unfinished work", "Info"));
        }
        if (code.contains("FIXME")) {
            issues.add(new IssueInfo("FIXME Found", "Known bug", "Warning"));
        }
        
        // Potential null pointer
        if (code.contains(".") && !code.contains("null check")) {
            issues.add(new IssueInfo("Potential NPE", "Method call without null check", "Warning"));
        }
        
        // Resource leak
        if (code.contains("new File") || code.contains("new Stream")) {
            issues.add(new IssueInfo("Resource Leak", "Resource not closed", "Warning"));
        }
        
        // Security issue
        if (code.contains("eval") || code.contains("Runtime.exec")) {
            issues.add(new IssueInfo("Security Risk", "Dangerous operation", "Error"));
        }
        
        return issues;
    }
    
    /**
     * Identify refactoring opportunities
     */
    public List<RefactoringOpportunity> identifyRefactoringOpportunities(String code) {
        logger.info("🔍 Identifying refactoring opportunities");
        
        List<RefactoringOpportunity> opportunities = new ArrayList<>();
        
        // Extract method
        if (countLines(code) > 30) {
            opportunities.add(new RefactoringOpportunity(
                "Extract Method",
                "Code block is too long, consider extracting methods",
                "High",
                "Improves readability and reusability"
            ));
        }
        
        // Rename variable
        if (code.contains("i ") || code.contains("x ") || code.contains("temp")) {
            opportunities.add(new RefactoringOpportunity(
                "Rename Variables",
                "Use more descriptive variable names",
                "Medium",
                "Improves code clarity"
            ));
        }
        
        // Consolidate duplicates
        if (code.length() > 500) {
            opportunities.add(new RefactoringOpportunity(
                "Consolidate Duplicates",
                "Check for duplicate code blocks",
                "Medium",
                "Reduces code duplication"
            ));
        }
        
        // Simplify expressions
        if (code.contains("&&") && code.contains("||")) {
            opportunities.add(new RefactoringOpportunity(
                "Simplify Expressions",
                "Complex boolean expressions can be simplified",
                "Low",
                "Improves readability"
            ));
        }
        
        // Apply design patterns
        if (code.contains("if") && code.contains("else if")) {
            opportunities.add(new RefactoringOpportunity(
                "Apply Strategy Pattern",
                "Multiple if-else can use strategy pattern",
                "Medium",
                "Improves extensibility"
            ));
        }
        
        return opportunities;
    }
    
    // Helper methods
    
    private int countLines(String code) {
        return code.split("\n").length;
    }
    
    private String detectLanguage(String code) {
        if (code.contains("public class") || code.contains("public interface")) return "Java";
        if (code.contains("function") || code.contains("const ")) return "JavaScript";
        if (code.contains("def ") || code.contains("class ")) return "Python";
        if (code.contains("func ") || code.contains("struct ")) return "Go";
        return "Unknown";
    }
    
    private List<MethodInfo> extractMethods(String code) {
        List<MethodInfo> methods = new ArrayList<>();
        Pattern pattern = Pattern.compile("(public|private|protected)?\\s+(\\w+)\\s+(\\w+)\\s*\\(([^)]*)\\)");
        Matcher matcher = pattern.matcher(code);
        
        while (matcher.find()) {
            methods.add(new MethodInfo(
                matcher.group(3),
                matcher.group(2),
                matcher.group(4).split(",").length
            ));
        }
        
        return methods;
    }
    
    private List<ClassInfo> extractClasses(String code) {
        List<ClassInfo> classes = new ArrayList<>();
        Pattern pattern = Pattern.compile("(public|private)?\\s+class\\s+(\\w+)");
        Matcher matcher = pattern.matcher(code);
        
        while (matcher.find()) {
            classes.add(new ClassInfo(matcher.group(2)));
        }
        
        return classes;
    }
    
    private List<String> extractVariables(String code) {
        List<String> variables = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\b(\\w+)\\s*=");
        Matcher matcher = pattern.matcher(code);
        
        while (matcher.find()) {
            String var = matcher.group(1);
            if (!isKeyword(var) && !variables.contains(var)) {
                variables.add(var);
            }
        }
        
        return variables;
    }
    
    private int calculateComplexity(String code) {
        int complexity = 1;
        complexity += countOccurrences(code, "if");
        complexity += countOccurrences(code, "else");
        complexity += countOccurrences(code, "for");
        complexity += countOccurrences(code, "while");
        complexity += countOccurrences(code, "case");
        complexity += countOccurrences(code, "catch");
        return complexity;
    }
    
    private int calculateMaintainability(String code) {
        int score = 100;
        score -= countLines(code) / 5;
        score -= calculateComplexity(code) * 2;
        score -= detectCodeSmells(code).size() * 5;
        return Math.max(0, Math.min(100, score));
    }
    
    private int countOccurrences(String code, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = code.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }
    
    private int calculateNestingLevel(String code) {
        int maxNesting = 0;
        int currentNesting = 0;
        
        for (char c : code.toCharArray()) {
            if (c == '{') {
                currentNesting++;
                maxNesting = Math.max(maxNesting, currentNesting);
            } else if (c == '}') {
                currentNesting--;
            }
        }
        
        return maxNesting;
    }
    
    private boolean isKeyword(String word) {
        String[] keywords = {"if", "else", "for", "while", "return", "new", "class", "public", "private", "protected"};
        for (String kw : keywords) {
            if (kw.equals(word)) return true;
        }
        return false;
    }
    
    // Inner classes
    
    public static class SelectionAnalysis {
        private int codeLength;
        private int lineCount;
        private String language;
        private List<MethodInfo> methods = new ArrayList<>();
        private List<ClassInfo> classes = new ArrayList<>();
        private List<String> variables = new ArrayList<>();
        private List<PatternInfo> patterns = new ArrayList<>();
        private List<AntiPatternInfo> antiPatterns = new ArrayList<>();
        private List<CodeSmellInfo> codeSmells = new ArrayList<>();
        private List<IssueInfo> issues = new ArrayList<>();
        private int complexity;
        private int maintainability;
        private List<RefactoringOpportunity> refactoringOpportunities = new ArrayList<>();
        
        // Getters and setters
        public int getCodeLength() { return codeLength; }
        public void setCodeLength(int codeLength) { this.codeLength = codeLength; }
        
        public int getLineCount() { return lineCount; }
        public void setLineCount(int lineCount) { this.lineCount = lineCount; }
        
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        
        public List<MethodInfo> getMethods() { return methods; }
        public void setMethods(List<MethodInfo> methods) { this.methods = methods; }
        
        public List<ClassInfo> getClasses() { return classes; }
        public void setClasses(List<ClassInfo> classes) { this.classes = classes; }
        
        public List<String> getVariables() { return variables; }
        public void setVariables(List<String> variables) { this.variables = variables; }
        
        public List<PatternInfo> getPatterns() { return patterns; }
        public void setPatterns(List<PatternInfo> patterns) { this.patterns = patterns; }
        
        public List<AntiPatternInfo> getAntiPatterns() { return antiPatterns; }
        public void setAntiPatterns(List<AntiPatternInfo> antiPatterns) { this.antiPatterns = antiPatterns; }
        
        public List<CodeSmellInfo> getCodeSmells() { return codeSmells; }
        public void setCodeSmells(List<CodeSmellInfo> codeSmells) { this.codeSmells = codeSmells; }
        
        public List<IssueInfo> getIssues() { return issues; }
        public void setIssues(List<IssueInfo> issues) { this.issues = issues; }
        
        public int getComplexity() { return complexity; }
        public void setComplexity(int complexity) { this.complexity = complexity; }
        
        public int getMaintainability() { return maintainability; }
        public void setMaintainability(int maintainability) { this.maintainability = maintainability; }
        
        public List<RefactoringOpportunity> getRefactoringOpportunities() { return refactoringOpportunities; }
        public void setRefactoringOpportunities(List<RefactoringOpportunity> refactoringOpportunities) { 
            this.refactoringOpportunities = refactoringOpportunities; 
        }
    }
    
    public static class MethodInfo {
        private String name;
        private String returnType;
        private int parameterCount;
        
        public MethodInfo(String name, String returnType, int parameterCount) {
            this.name = name;
            this.returnType = returnType;
            this.parameterCount = parameterCount;
        }
        
        public String getName() { return name; }
        public String getReturnType() { return returnType; }
        public int getParameterCount() { return parameterCount; }
    }
    
    public static class ClassInfo {
        private String name;
        
        public ClassInfo(String name) { this.name = name; }
        public String getName() { return name; }
    }
    
    public static class PatternInfo {
        private String name;
        private String description;
        private double confidence;
        
        public PatternInfo(String name, String description, double confidence) {
            this.name = name;
            this.description = description;
            this.confidence = confidence;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public double getConfidence() { return confidence; }
    }
    
    public static class AntiPatternInfo {
        private String name;
        private String description;
        private int severity;
        private String level;
        
        public AntiPatternInfo(String name, String description, int severity, String level) {
            this.name = name;
            this.description = description;
            this.severity = severity;
            this.level = level;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getSeverity() { return severity; }
        public String getLevel() { return level; }
    }
    
    public static class CodeSmellInfo {
        private String name;
        private String description;
        private int count;
        private String severity;
        
        public CodeSmellInfo(String name, String description, int count, String severity) {
            this.name = name;
            this.description = description;
            this.count = count;
            this.severity = severity;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getCount() { return count; }
        public String getSeverity() { return severity; }
    }
    
    public static class IssueInfo {
        private String type;
        private String description;
        private String severity;
        
        public IssueInfo(String type, String description, String severity) {
            this.type = type;
            this.description = description;
            this.severity = severity;
        }
        
        public String getType() { return type; }
        public String getDescription() { return description; }
        public String getSeverity() { return severity; }
    }
    
    public static class RefactoringOpportunity {
        private String type;
        private String description;
        private String priority;
        private String benefit;
        
        public RefactoringOpportunity(String type, String description, String priority, String benefit) {
            this.type = type;
            this.description = description;
            this.priority = priority;
            this.benefit = benefit;
        }
        
        public String getType() { return type; }
        public String getDescription() { return description; }
        public String getPriority() { return priority; }
        public String getBenefit() { return benefit; }
    }
}

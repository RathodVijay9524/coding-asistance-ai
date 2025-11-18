package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 🧠 CONSISTENCY CHECK SERVICE - Phase 7
 * 
 * Purpose: Validates response consistency, checks for contradictions,
 * validates field requirements, and validates code structure.
 * 
 * Responsibilities:
 * - Check response consistency
 * - Detect contradictions
 * - Validate required fields
 * - Validate code structure
 * - Provide consistency report
 */
@Service
public class ConsistencyCheckService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConsistencyCheckService.class);
    
    // Patterns for code validation
    private static final Pattern CLASS_PATTERN = Pattern.compile("\\bclass\\s+\\w+");
    private static final Pattern METHOD_PATTERN = Pattern.compile("\\b(public|private|protected)?\\s+(static\\s+)?(\\w+)\\s+\\w+\\s*\\(");
    private static final Pattern FIELD_PATTERN = Pattern.compile("\\b(public|private|protected)?\\s+(static\\s+)?(final\\s+)?(\\w+)\\s+\\w+");
    
    /**
     * Check overall consistency of response
     */
    public ConsistencyReport checkConsistency(String response) {
        ConsistencyReport report = new ConsistencyReport();
        
        if (response == null || response.isEmpty()) {
            report.addIssue("Response is empty", IssueSeverity.HIGH);
            return report;
        }
        
        // Run all checks
        checkForContradictions(response, report);
        checkForIncompleteStatements(response, report);
        checkForLogicalFlow(response, report);
        checkForCodeConsistency(response, report);
        
        logger.info("🧠 Consistency Check: {} issues found (severity: {})", 
            report.getIssueCount(), report.getHighestSeverity());
        
        return report;
    }
    
    /**
     * Check for contradictions in response
     */
    private void checkForContradictions(String response, ConsistencyReport report) {
        String lower = response.toLowerCase();
        
        // Check for common contradictions
        if (lower.contains("yes") && lower.contains("no")) {
            if (lower.indexOf("yes") < lower.indexOf("no")) {
                // Check if they're in same context
                int distance = lower.indexOf("no") - lower.indexOf("yes");
                if (distance < 500) { // Within 500 chars
                    report.addIssue("Potential contradiction: contains both 'yes' and 'no' in close proximity", 
                        IssueSeverity.MEDIUM);
                }
            }
        }
        
        if (lower.contains("always") && lower.contains("never")) {
            report.addIssue("Potential contradiction: contains both 'always' and 'never'", IssueSeverity.MEDIUM);
        }
        
        if (lower.contains("must") && lower.contains("optional")) {
            report.addIssue("Potential contradiction: contains both 'must' and 'optional'", IssueSeverity.MEDIUM);
        }
    }
    
    /**
     * Check for incomplete statements
     */
    private void checkForIncompleteStatements(String response, ConsistencyReport report) {
        String[] sentences = response.split("[.!?]");
        
        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            
            // Check for incomplete sentences
            if (trimmed.startsWith("For example") || trimmed.startsWith("Such as")) {
                if (trimmed.endsWith(",") || trimmed.endsWith(":")) {
                    report.addIssue("Incomplete statement: '" + trimmed.substring(0, Math.min(50, trimmed.length())) + "...'", 
                        IssueSeverity.LOW);
                }
            }
            
            // Check for dangling references
            if (trimmed.contains("this") || trimmed.contains("that")) {
                if (trimmed.length() < 20) {
                    report.addIssue("Incomplete reference in short sentence", IssueSeverity.LOW);
                }
            }
        }
    }
    
    /**
     * Check for logical flow
     */
    private void checkForLogicalFlow(String response, ConsistencyReport report) {
        String lower = response.toLowerCase();
        
        // Check for proper transitions
        int transitionCount = 0;
        String[] transitions = {"however", "therefore", "thus", "moreover", "furthermore", "meanwhile", "subsequently"};
        
        for (String transition : transitions) {
            if (lower.contains(transition)) {
                transitionCount++;
            }
        }
        
        // If response is long but has no transitions, might lack flow
        if (response.length() > 500 && transitionCount == 0) {
            report.addIssue("Response may lack logical flow (no transition words found)", IssueSeverity.LOW);
        }
    }
    
    /**
     * Check for code consistency
     */
    private void checkForCodeConsistency(String response, ConsistencyReport report) {
        // Check for code blocks
        if (!response.contains("```") && !response.contains("public") && !response.contains("class")) {
            return; // Not a code response
        }
        
        // Check for matching braces
        int openBraces = countOccurrences(response, "{");
        int closeBraces = countOccurrences(response, "}");
        
        if (openBraces != closeBraces) {
            report.addIssue(String.format("Unmatched braces: %d open, %d close", openBraces, closeBraces), 
                IssueSeverity.HIGH);
        }
        
        // Check for matching parentheses
        int openParens = countOccurrences(response, "(");
        int closeParens = countOccurrences(response, ")");
        
        if (openParens != closeParens) {
            report.addIssue(String.format("Unmatched parentheses: %d open, %d close", openParens, closeParens), 
                IssueSeverity.HIGH);
        }
        
        // Check for matching brackets
        int openBrackets = countOccurrences(response, "[");
        int closeBrackets = countOccurrences(response, "]");
        
        if (openBrackets != closeBrackets) {
            report.addIssue(String.format("Unmatched brackets: %d open, %d close", openBrackets, closeBrackets), 
                IssueSeverity.HIGH);
        }
    }
    
    /**
     * Validate required fields in response
     */
    public FieldValidationReport validateRequiredFields(String response, List<String> requiredFields) {
        FieldValidationReport report = new FieldValidationReport();
        
        if (response == null || response.isEmpty()) {
            report.addMissingField("Response is empty");
            return report;
        }
        
        String lower = response.toLowerCase();
        
        for (String field : requiredFields) {
            if (!lower.contains(field.toLowerCase())) {
                report.addMissingField(field);
            }
        }
        
        logger.info("🧠 Field Validation: {} required fields, {} missing", 
            requiredFields.size(), report.getMissingFieldCount());
        
        return report;
    }
    
    /**
     * Validate code structure
     */
    public CodeStructureReport validateCodeStructure(String response) {
        CodeStructureReport report = new CodeStructureReport();
        
        if (!response.contains("class") && !response.contains("function") && !response.contains("def")) {
            return report; // Not code
        }
        
        // Check for classes
        Matcher classMatcher = CLASS_PATTERN.matcher(response);
        int classCount = 0;
        while (classMatcher.find()) {
            classCount++;
            report.addFoundClass(classMatcher.group());
        }
        
        // Check for methods
        Matcher methodMatcher = METHOD_PATTERN.matcher(response);
        int methodCount = 0;
        while (methodMatcher.find()) {
            methodCount++;
            report.addFoundMethod(methodMatcher.group());
        }
        
        // Check for fields
        Matcher fieldMatcher = FIELD_PATTERN.matcher(response);
        int fieldCount = 0;
        while (fieldMatcher.find()) {
            fieldCount++;
            report.addFoundField(fieldMatcher.group());
        }
        
        // Validate structure
        if (classCount > 0 && methodCount == 0) {
            report.addIssue("Class found but no methods defined", IssueSeverity.MEDIUM);
        }
        
        if (methodCount > 0 && classCount == 0) {
            report.addIssue("Methods found but no class definition", IssueSeverity.MEDIUM);
        }
        
        logger.info("🧠 Code Structure: {} classes, {} methods, {} fields", classCount, methodCount, fieldCount);
        
        return report;
    }
    
    /**
     * Count occurrences of a string
     */
    private int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }
    
    // ============ Inner Classes ============
    
    /**
     * Severity levels for issues
     */
    public enum IssueSeverity {
        LOW, MEDIUM, HIGH
    }
    
    /**
     * Consistency report
     */
    public static class ConsistencyReport {
        private final List<ConsistencyIssue> issues = new ArrayList<>();
        
        public void addIssue(String message, IssueSeverity severity) {
            issues.add(new ConsistencyIssue(message, severity));
        }
        
        public List<ConsistencyIssue> getIssues() {
            return new ArrayList<>(issues);
        }
        
        public int getIssueCount() {
            return issues.size();
        }
        
        public IssueSeverity getHighestSeverity() {
            return issues.stream()
                .map(i -> i.severity)
                .max(Comparator.comparingInt(Enum::ordinal))
                .orElse(IssueSeverity.LOW);
        }
        
        public boolean isConsistent() {
            return issues.stream().noneMatch(i -> i.severity == IssueSeverity.HIGH);
        }
    }
    
    /**
     * Single consistency issue
     */
    public static class ConsistencyIssue {
        public final String message;
        public final IssueSeverity severity;
        
        public ConsistencyIssue(String message, IssueSeverity severity) {
            this.message = message;
            this.severity = severity;
        }
    }
    
    /**
     * Field validation report
     */
    public static class FieldValidationReport {
        private final List<String> missingFields = new ArrayList<>();
        
        public void addMissingField(String field) {
            missingFields.add(field);
        }
        
        public List<String> getMissingFields() {
            return new ArrayList<>(missingFields);
        }
        
        public int getMissingFieldCount() {
            return missingFields.size();
        }
        
        public boolean isValid() {
            return missingFields.isEmpty();
        }
    }
    
    /**
     * Code structure report
     */
    public static class CodeStructureReport {
        private final List<String> foundClasses = new ArrayList<>();
        private final List<String> foundMethods = new ArrayList<>();
        private final List<String> foundFields = new ArrayList<>();
        private final List<CodeStructureIssue> issues = new ArrayList<>();
        
        public void addFoundClass(String className) {
            foundClasses.add(className);
        }
        
        public void addFoundMethod(String methodName) {
            foundMethods.add(methodName);
        }
        
        public void addFoundField(String fieldName) {
            foundFields.add(fieldName);
        }
        
        public void addIssue(String message, IssueSeverity severity) {
            issues.add(new CodeStructureIssue(message, severity));
        }
        
        public List<String> getFoundClasses() {
            return new ArrayList<>(foundClasses);
        }
        
        public List<String> getFoundMethods() {
            return new ArrayList<>(foundMethods);
        }
        
        public List<String> getFoundFields() {
            return new ArrayList<>(foundFields);
        }
        
        public List<CodeStructureIssue> getIssues() {
            return new ArrayList<>(issues);
        }
        
        public boolean isValid() {
            return issues.isEmpty();
        }
    }
    
    /**
     * Code structure issue
     */
    public static class CodeStructureIssue {
        public final String message;
        public final IssueSeverity severity;
        
        public CodeStructureIssue(String message, IssueSeverity severity) {
            this.message = message;
            this.severity = severity;
        }
    }
}

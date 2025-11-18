package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 🚨 Error Highlighter
 * 
 * Highlights and explains errors:
 * - Highlight potential errors
 * - Show error explanations
 * - Suggest fixes
 */
@Component
public class ErrorHighlighter {
    
    private static final Logger logger = LoggerFactory.getLogger(ErrorHighlighter.class);
    
    /**
     * Highlight errors in code
     */
    public List<ErrorHighlight> highlightErrors(String code, String language) {
        if (code == null || code.isEmpty()) {
            logger.warn("⚠️ Empty code provided");
            return new ArrayList<>();
        }
        
        logger.debug("🚨 Highlighting errors (language={})", language);
        
        List<ErrorHighlight> errors = new ArrayList<>();
        String[] lines = code.split("\n");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            // Check for syntax errors
            checkSyntaxErrors(line, i + 1, language, errors);
            
            // Check for logic errors
            checkLogicErrors(line, i + 1, language, errors);
            
            // Check for style errors
            checkStyleErrors(line, i + 1, language, errors);
        }
        
        logger.debug("✅ Highlighted {} errors", errors.size());
        return errors;
    }
    
    /**
     * Check for syntax errors
     */
    private void checkSyntaxErrors(String line, int lineNumber, String language, 
                                   List<ErrorHighlight> errors) {
        // Missing semicolon
        if (line.trim().matches(".*[a-zA-Z0-9\\)\\}\\]]$") && 
            !line.trim().startsWith("//") && 
            !line.trim().isEmpty() &&
            !line.contains("{") && !line.contains("}")) {
            errors.add(new ErrorHighlight(
                    lineNumber,
                    "Missing semicolon",
                    "Statement should end with semicolon",
                    "Add ; at end of line",
                    "SYNTAX"
            ));
        }
        
        // Unmatched braces
        if (line.contains("{") && !line.contains("}")) {
            int openBraces = countChar(line, '{');
            int closeBraces = countChar(line, '}');
            if (openBraces > closeBraces) {
                errors.add(new ErrorHighlight(
                        lineNumber,
                        "Unmatched opening brace",
                        "Missing closing brace",
                        "Add } to match {",
                        "SYNTAX"
                ));
            }
        }
        
        // Unmatched parentheses
        if (line.contains("(") && !line.contains(")")) {
            int openParens = countChar(line, '(');
            int closeParens = countChar(line, ')');
            if (openParens > closeParens) {
                errors.add(new ErrorHighlight(
                        lineNumber,
                        "Unmatched opening parenthesis",
                        "Missing closing parenthesis",
                        "Add ) to match (",
                        "SYNTAX"
                ));
            }
        }
    }
    
    /**
     * Check for logic errors
     */
    private void checkLogicErrors(String line, int lineNumber, String language,
                                  List<ErrorHighlight> errors) {
        // Null pointer risk
        if (line.contains(".get(") && !line.contains("isPresent()") && 
            !line.contains("orElse") && !line.contains("orElseThrow")) {
            errors.add(new ErrorHighlight(
                    lineNumber,
                    "Potential null pointer",
                    "Missing null check after .get()",
                    "Use isPresent() or orElse()",
                    "LOGIC"
            ));
        }
        
        // Infinite loop
        if (line.contains("while(true)") || line.contains("while (true)")) {
            errors.add(new ErrorHighlight(
                    lineNumber,
                    "Infinite loop",
                    "Loop will never terminate",
                    "Add break condition",
                    "LOGIC"
            ));
        }
        
        // Unreachable code
        if (line.contains("return") && line.trim().endsWith("return")) {
            errors.add(new ErrorHighlight(
                    lineNumber,
                    "Unreachable code",
                    "Code after return will not execute",
                    "Move code before return",
                    "LOGIC"
            ));
        }
        
        // Unused variable
        if (line.matches(".*\\bvar\\s+\\w+\\s*=.*") && !line.contains("return")) {
            errors.add(new ErrorHighlight(
                    lineNumber,
                    "Possibly unused variable",
                    "Variable declared but may not be used",
                    "Remove or use variable",
                    "LOGIC"
            ));
        }
    }
    
    /**
     * Check for style errors
     */
    private void checkStyleErrors(String line, int lineNumber, String language,
                                  List<ErrorHighlight> errors) {
        // Inconsistent indentation
        if (line.startsWith(" ") && !line.startsWith("    ") && 
            !line.startsWith("\t") && line.length() > 0) {
            errors.add(new ErrorHighlight(
                    lineNumber,
                    "Inconsistent indentation",
                    "Use 4 spaces or tabs consistently",
                    "Fix indentation",
                    "STYLE"
            ));
        }
        
        // Line too long
        if (line.length() > 120) {
            errors.add(new ErrorHighlight(
                    lineNumber,
                    "Line too long",
                    "Line exceeds 120 characters",
                    "Break into multiple lines",
                    "STYLE"
            ));
        }
        
        // Missing space after keyword
        if (line.matches(".*\\b(if|for|while|catch)\\(.*")) {
            errors.add(new ErrorHighlight(
                    lineNumber,
                    "Missing space after keyword",
                    "Add space between keyword and parenthesis",
                    "Change 'if(' to 'if ('",
                    "STYLE"
            ));
        }
    }
    
    /**
     * Explain error
     */
    public String explainError(ErrorHighlight error) {
        StringBuilder explanation = new StringBuilder();
        
        explanation.append("Error at line ").append(error.lineNumber).append(":\n");
        explanation.append("Type: ").append(error.type).append("\n");
        explanation.append("Issue: ").append(error.description).append("\n");
        explanation.append("Details: ").append(error.details).append("\n");
        explanation.append("Fix: ").append(error.suggestion).append("\n");
        
        return explanation.toString();
    }
    
    /**
     * Suggest fix for error
     */
    public String suggestFix(ErrorHighlight error) {
        return error.suggestion;
    }
    
    /**
     * Count character occurrences
     */
    private int countChar(String str, char ch) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == ch) count++;
        }
        return count;
    }
    
    /**
     * Error Highlight DTO
     */
    public static class ErrorHighlight {
        public int lineNumber;
        public String title;
        public String description;
        public String details;
        public String suggestion;
        public String type; // SYNTAX, LOGIC, STYLE
        
        public ErrorHighlight(int lineNumber, String title, String description,
                            String suggestion, String type) {
            this.lineNumber = lineNumber;
            this.title = title;
            this.description = description;
            this.details = description;
            this.suggestion = suggestion;
            this.type = type;
        }
        
        @Override
        public String toString() {
            return String.format("[%s] Line %d: %s - %s", type, lineNumber, title, description);
        }
    }
}

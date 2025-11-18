package com.vijay.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.manager.AiToolProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 🔍 Code Review Tool Service
 * 
 * Performs automated code review including:
 * - Code quality issues
 * - Best practices violations
 * - Performance concerns
 * - Security vulnerabilities
 * - Suggestions for improvement
 * 
 * ✅ FIXED: Uses static analysis instead of ChatClient calls to prevent infinite recursion
 * 
 * Implements AiToolProvider to be accessible from chatbot
 */
@Service
@RequiredArgsConstructor
public class CodeReviewToolService implements AiToolProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(CodeReviewToolService.class);
    private final ObjectMapper objectMapper;
    
    /**
     * Review code and provide feedback
     */
    @Tool(description = "Perform automated code review with issues and suggestions")
    public String reviewCode(
            @ToolParam(description = "Code to review") String code,
            @ToolParam(description = "Programming language") String language,
            @ToolParam(description = "File path or context") String context) {
        
        logger.info("🔍 Starting code review for: {}", context);
        
        try {
            Map<String, Object> review = new HashMap<>();
            
            // 1. Analyze code structure
            review.put("structure", analyzeCodeStructure(code, language));
            
            // 2. Find issues
            review.put("issues", findCodeIssues(code, language));
            
            // 3. Check best practices
            review.put("bestPractices", checkBestPractices(code, language));
            
            // 4. Performance analysis
            review.put("performance", analyzePerformance(code, language));
            
            // 5. Security analysis
            review.put("security", analyzeSecurityIssues(code, language));
            
            // 6. Get AI suggestions
            review.put("suggestions", getAISuggestions(code, language));
            
            // 7. Calculate review score
            double score = calculateReviewScore(review);
            review.put("score", score);
            review.put("rating", getRating(score));
            
            // 8. Summary
            review.put("summary", generateReviewSummary(review));
            
            logger.info("✅ Code review complete");
            return toJson(review);
            
        } catch (Exception e) {
            logger.error("❌ Code review failed: {}", e.getMessage(), e);
            return errorResponse("Code review failed: " + e.getMessage());
        }
    }
    
    /**
     * Analyze code structure
     */
    private Map<String, Object> analyzeCodeStructure(String code, String language) {
        Map<String, Object> structure = new HashMap<>();
        
        try {
            String[] lines = code.split("\n");
            structure.put("totalLines", lines.length);
            structure.put("emptyLines", (int) java.util.Arrays.stream(lines).filter(String::isBlank).count());
            structure.put("commentLines", (int) java.util.Arrays.stream(lines).filter(l -> l.trim().startsWith("//") || l.trim().startsWith("#")).count());
            structure.put("codeLines", lines.length - ((Number) structure.get("emptyLines")).intValue() - ((Number) structure.get("commentLines")).intValue());
            
            // Language-specific analysis
            if ("java".equalsIgnoreCase(language)) {
                structure.put("classes", countOccurrences(code, "class "));
                structure.put("methods", countOccurrences(code, "public ") + countOccurrences(code, "private "));
            } else if ("python".equalsIgnoreCase(language)) {
                structure.put("functions", countOccurrences(code, "def "));
                structure.put("classes", countOccurrences(code, "class "));
            }
            
        } catch (Exception e) {
            logger.debug("Could not analyze structure: {}", e.getMessage());
            structure.put("error", e.getMessage());
        }
        
        return structure;
    }
    
    /**
     * Find code issues
     */
    private List<Map<String, Object>> findCodeIssues(String code, String language) {
        List<Map<String, Object>> issues = new ArrayList<>();
        
        try {
            // Check for common issues
            if (code.contains("TODO") || code.contains("FIXME")) {
                issues.add(createIssue("TODO/FIXME Comments", "Found unresolved TODO or FIXME comments", "MEDIUM"));
            }
            
            if (countOccurrences(code, "if") > 10) {
                issues.add(createIssue("High Cyclomatic Complexity", "Too many conditional statements", "MEDIUM"));
            }
            
            if (code.length() > 5000) {
                issues.add(createIssue("Large Code Block", "Code block is too large, consider breaking it down", "LOW"));
            }
            
            if (code.contains("System.out.println") || code.contains("console.log")) {
                issues.add(createIssue("Debug Logging", "Debug logging should be removed or use proper logger", "LOW"));
            }
            
            if (code.contains("catch (Exception e)")) {
                issues.add(createIssue("Generic Exception Handling", "Catching generic Exception is not recommended", "MEDIUM"));
            }
            
        } catch (Exception e) {
            logger.debug("Could not find issues: {}", e.getMessage());
        }
        
        return issues;
    }
    
    /**
     * Check best practices
     */
    private List<String> checkBestPractices(String code, String language) {
        List<String> practices = new ArrayList<>();
        
        try {
            if ("java".equalsIgnoreCase(language)) {
                if (!code.contains("@Override") && code.contains("public")) {
                    practices.add("✅ Use @Override annotation for overridden methods");
                }
                if (!code.contains("final") && code.contains("class")) {
                    practices.add("✅ Consider using 'final' for classes not meant to be extended");
                }
            }
            
            if (!code.contains("//") && !code.contains("#") && code.length() > 500) {
                practices.add("✅ Add comments to explain complex logic");
            }
            
            if (code.contains("public static")) {
                practices.add("✅ Minimize use of static methods");
            }
            
        } catch (Exception e) {
            logger.debug("Could not check practices: {}", e.getMessage());
        }
        
        return practices;
    }
    
    /**
     * Analyze performance
     */
    private Map<String, Object> analyzePerformance(String code, String language) {
        Map<String, Object> performance = new HashMap<>();
        List<String> concerns = new ArrayList<>();
        
        try {
            if (code.contains("Thread.sleep")) {
                concerns.add("⚠️ Thread.sleep() blocks execution");
            }
            
            if (code.contains("synchronized")) {
                concerns.add("⚠️ Synchronization may cause performance issues");
            }
            
            if (countOccurrences(code, "new ") > 20) {
                concerns.add("⚠️ Excessive object creation");
            }
            
            performance.put("concerns", concerns);
            performance.put("score", Math.max(0, 10 - concerns.size()));
            
        } catch (Exception e) {
            logger.debug("Could not analyze performance: {}", e.getMessage());
        }
        
        return performance;
    }
    
    /**
     * Analyze security issues
     */
    private Map<String, Object> analyzeSecurityIssues(String code, String language) {
        Map<String, Object> security = new HashMap<>();
        List<String> vulnerabilities = new ArrayList<>();
        
        try {
            if (code.contains("eval(") || code.contains("exec(")) {
                vulnerabilities.add("🔴 CRITICAL: eval() or exec() detected");
            }
            
            if (code.contains("password") && !code.contains("encrypted")) {
                vulnerabilities.add("🔴 CRITICAL: Potential hardcoded password");
            }
            
            if (code.contains("SELECT") && code.contains("\"") && code.contains("+")) {
                vulnerabilities.add("🟡 WARNING: Potential SQL injection");
            }
            
            security.put("vulnerabilities", vulnerabilities);
            security.put("severity", vulnerabilities.isEmpty() ? "LOW" : "HIGH");
            
        } catch (Exception e) {
            logger.debug("Could not analyze security: {}", e.getMessage());
        }
        
        return security;
    }
    
    /**
     * Get AI suggestions (STATIC - no ChatClient calls to prevent recursion)
     */
    private List<String> getAISuggestions(String code, String language) {
        List<String> suggestions = new ArrayList<>();
        
        try {
            // ✅ STATIC: Return predefined suggestions instead of AI-generated
            suggestions.add("1. Follow SOLID principles for better design");
            suggestions.add("2. Add comprehensive error handling");
            suggestions.add("3. Improve code documentation and comments");
            suggestions.add("4. Reduce code complexity and cyclomatic complexity");
            suggestions.add("5. Add unit tests for critical logic");
            
        } catch (Exception e) {
            logger.debug("Could not get suggestions: {}", e.getMessage());
            suggestions.add("Unable to generate suggestions at this time");
        }
        
        return suggestions;
    }
    
    /**
     * Generate review summary
     */
    private String generateReviewSummary(Map<String, Object> review) {
        try {
            List<Map<String, Object>> issues = (List<Map<String, Object>>) review.get("issues");
            Map<String, Object> security = (Map<String, Object>) review.get("security");
            double score = ((Number) review.get("score")).doubleValue();
            
            int issueCount = issues != null ? issues.size() : 0;
            
            if (score >= 8) {
                return "✅ Code quality is good. " + issueCount + " minor issues found.";
            } else if (score >= 6) {
                return "🟡 Code quality is acceptable. " + issueCount + " issues found. Consider improvements.";
            } else {
                return "🔴 Code quality needs improvement. " + issueCount + " issues found. Refactoring recommended.";
            }
            
        } catch (Exception e) {
            return "Review completed";
        }
    }
    
    private double calculateReviewScore(Map<String, Object> review) {
        double score = 10.0;
        
        try {
            List<Map<String, Object>> issues = (List<Map<String, Object>>) review.get("issues");
            if (issues != null) {
                score -= issues.size() * 0.5;
            }
            
            Map<String, Object> security = (Map<String, Object>) review.get("security");
            if (security != null) {
                List<String> vulns = (List<String>) security.get("vulnerabilities");
                if (vulns != null) {
                    score -= vulns.size() * 2.0;
                }
            }
            
        } catch (Exception e) {
            logger.debug("Could not calculate score: {}", e.getMessage());
        }
        
        return Math.max(0, Math.min(10, score));
    }
    
    private String getRating(double score) {
        if (score >= 8) return "Excellent";
        if (score >= 6) return "Good";
        if (score >= 4) return "Fair";
        return "Poor";
    }
    
    private Map<String, Object> createIssue(String title, String description, String severity) {
        Map<String, Object> issue = new HashMap<>();
        issue.put("title", title);
        issue.put("description", description);
        issue.put("severity", severity);
        return issue;
    }
    
    private int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }
    
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("❌ JSON serialization failed: {}", e.getMessage());
            return "{\"error\": \"JSON serialization failed\"}";
        }
    }
    
    private String errorResponse(String message) {
        return "{\"error\": \"" + message.replace("\"", "\\\"") + "\"}";
    }
}

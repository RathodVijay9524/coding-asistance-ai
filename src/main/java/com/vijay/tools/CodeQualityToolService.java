package com.vijay.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.manager.AiToolProvider;
import com.vijay.service.CodeIntelligenceEngine;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 📊 Code Quality Tool Service
 * 
 * Scans code quality issues including:
 * - Code smells
 * - Complexity metrics
 * - Best practices violations
 * - Performance issues
 * - Security concerns
 * 
 * Implements AiToolProvider to be accessible from chatbot
 */
@Service
@RequiredArgsConstructor
public class CodeQualityToolService implements AiToolProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(CodeQualityToolService.class);
    private final ObjectMapper objectMapper;
    private final CodeIntelligenceEngine intelligenceEngine;
    
    /**
     * Scan code quality in project
     */
    @Tool(description = "Scan code quality: detect issues, metrics, and recommendations")
    public String scanCodeQuality(
            @ToolParam(description = "Project path to scan") String projectPath) {
        
        logger.info("📊 Starting code quality scan for: {}", projectPath);
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Validate path
            Path path = Paths.get(projectPath);
            if (!Files.exists(path)) {
                return errorResponse("Project path does not exist: " + projectPath);
            }
            
            // 1. Detect code smells
            logger.debug("🐛 Detecting code smells...");
            result.put("codeSmells", detectCodeSmells(projectPath));
            
            // 2. Calculate complexity metrics
            logger.debug("📈 Calculating complexity metrics...");
            result.put("complexity", calculateComplexityMetrics(projectPath));
            
            // 3. Check best practices
            logger.debug("✅ Checking best practices...");
            result.put("bestPractices", checkBestPractices(projectPath));
            
            // 4. Detect performance issues
            logger.debug("⚡ Detecting performance issues...");
            result.put("performanceIssues", detectPerformanceIssues(projectPath));
            
            // 5. Security analysis
            logger.debug("🔒 Analyzing security...");
            result.put("securityConcerns", analyzeSecurityConcerns(projectPath));
            
            // 6. Calculate overall score
            double overallScore = calculateOverallScore(result);
            result.put("overallScore", overallScore);
            result.put("rating", getRating(overallScore));
            
            // 7. Generate recommendations
            result.put("recommendations", generateQualityRecommendations(result));
            
            logger.info("✅ Code quality scan complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Code quality scan failed: {}", e.getMessage(), e);
            return errorResponse("Code quality scan failed: " + e.getMessage());
        }
    }
    
    /**
     * Detect code smells
     */
    private Map<String, Object> detectCodeSmells(String projectPath) {
        Map<String, Object> smells = new HashMap<>();
        List<String> issues = new ArrayList<>();
        
        try {
            Path path = Paths.get(projectPath);
            
            try (Stream<Path> paths = Files.walk(path)) {
                paths.filter(Files::isRegularFile)
                    .filter(p -> isCodeFile(p.toString()))
                    .forEach(filePath -> {
                        try {
                            String content = Files.readString(filePath);
                            
                            // Check for common code smells
                            if (content.contains("TODO") || content.contains("FIXME")) {
                                issues.add("⚠️ TODO/FIXME comments found in: " + filePath.getFileName());
                            }
                            
                            if (content.split("\n").length > 500) {
                                issues.add("📏 Large file detected: " + filePath.getFileName() + " (" + content.split("\n").length + " lines)");
                            }
                            
                            if (countOccurrences(content, "if") > 10) {
                                issues.add("🔀 High cyclomatic complexity in: " + filePath.getFileName());
                            }
                            
                            if (content.contains("System.out.println") || content.contains("console.log")) {
                                issues.add("🖨️ Debug logging found in: " + filePath.getFileName());
                            }
                            
                        } catch (Exception e) {
                            logger.debug("Could not analyze file: {}", filePath);
                        }
                    });
            }
            
            smells.put("count", issues.size());
            smells.put("issues", issues);
            smells.put("severity", issues.isEmpty() ? "LOW" : "MEDIUM");
            
        } catch (Exception e) {
            logger.error("❌ Code smell detection failed: {}", e.getMessage());
            smells.put("error", e.getMessage());
        }
        
        return smells;
    }
    
    /**
     * Calculate complexity metrics
     */
    private Map<String, Object> calculateComplexityMetrics(String projectPath) {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            Path path = Paths.get(projectPath);
            
            int[] stats = new int[]{0, 0, 0, 0}; // files, lines, methods, classes
            
            try (Stream<Path> paths = Files.walk(path)) {
                paths.filter(Files::isRegularFile)
                    .filter(p -> isCodeFile(p.toString()))
                    .forEach(filePath -> {
                        try {
                            String content = Files.readString(filePath);
                            stats[0]++; // file count
                            stats[1] += content.split("\n").length; // line count
                            stats[2] += countOccurrences(content, "def ") + countOccurrences(content, "public "); // methods
                            stats[3] += countOccurrences(content, "class "); // classes
                        } catch (Exception e) {
                            logger.debug("Could not analyze file: {}", filePath);
                        }
                    });
            }
            
            metrics.put("totalFiles", stats[0]);
            metrics.put("totalLines", stats[1]);
            metrics.put("totalMethods", stats[2]);
            metrics.put("totalClasses", stats[3]);
            metrics.put("avgLinesPerFile", stats[0] > 0 ? stats[1] / stats[0] : 0);
            metrics.put("avgMethodsPerClass", stats[3] > 0 ? stats[2] / stats[3] : 0);
            
            double cyclomatic = (stats[2] * 1.5) / Math.max(stats[3], 1);
            metrics.put("cyclomaticComplexity", Math.min(cyclomatic, 10.0));
            
        } catch (Exception e) {
            logger.error("❌ Complexity calculation failed: {}", e.getMessage());
            metrics.put("error", e.getMessage());
        }
        
        return metrics;
    }
    
    /**
     * Check best practices
     */
    private Map<String, Object> checkBestPractices(String projectPath) {
        Map<String, Object> practices = new HashMap<>();
        List<String> violations = new ArrayList<>();
        
        try {
            // Check for common best practice violations
            File dir = new File(projectPath);
            File[] files = dir.listFiles();
            
            if (files != null) {
                boolean hasTests = Arrays.stream(files).anyMatch(f -> f.getName().contains("test"));
                boolean hasReadme = Arrays.stream(files).anyMatch(f -> f.getName().equalsIgnoreCase("README.md"));
                boolean hasGitignore = Arrays.stream(files).anyMatch(f -> f.getName().equals(".gitignore"));
                boolean hasLicense = Arrays.stream(files).anyMatch(f -> f.getName().equalsIgnoreCase("LICENSE"));
                
                if (!hasTests) violations.add("❌ No test directory found");
                if (!hasReadme) violations.add("❌ No README.md found");
                if (!hasGitignore) violations.add("❌ No .gitignore found");
                if (!hasLicense) violations.add("⚠️ No LICENSE file found");
            }
            
            practices.put("violations", violations);
            practices.put("score", Math.max(0, 10 - violations.size()));
            
        } catch (Exception e) {
            logger.error("❌ Best practices check failed: {}", e.getMessage());
            practices.put("error", e.getMessage());
        }
        
        return practices;
    }
    
    /**
     * Detect performance issues
     */
    private Map<String, Object> detectPerformanceIssues(String projectPath) {
        Map<String, Object> issues = new HashMap<>();
        List<String> problems = new ArrayList<>();
        
        try {
            Path path = Paths.get(projectPath);
            
            try (Stream<Path> paths = Files.walk(path)) {
                paths.filter(Files::isRegularFile)
                    .filter(p -> isCodeFile(p.toString()))
                    .forEach(filePath -> {
                        try {
                            String content = Files.readString(filePath);
                            
                            if (content.contains("Thread.sleep")) {
                                problems.add("⏱️ Thread.sleep() detected in: " + filePath.getFileName());
                            }
                            
                            if (content.contains("synchronized") && countOccurrences(content, "synchronized") > 5) {
                                problems.add("🔒 Excessive synchronization in: " + filePath.getFileName());
                            }
                            
                            if (content.contains("new ArrayList") && countOccurrences(content, "new ArrayList") > 10) {
                                problems.add("💾 Excessive object creation in: " + filePath.getFileName());
                            }
                            
                        } catch (Exception e) {
                            logger.debug("Could not analyze file: {}", filePath);
                        }
                    });
            }
            
            issues.put("count", problems.size());
            issues.put("problems", problems);
            
        } catch (Exception e) {
            logger.error("❌ Performance analysis failed: {}", e.getMessage());
            issues.put("error", e.getMessage());
        }
        
        return issues;
    }
    
    /**
     * Analyze security concerns
     */
    private Map<String, Object> analyzeSecurityConcerns(String projectPath) {
        Map<String, Object> security = new HashMap<>();
        List<String> concerns = new ArrayList<>();
        
        try {
            Path path = Paths.get(projectPath);
            
            try (Stream<Path> paths = Files.walk(path)) {
                paths.filter(Files::isRegularFile)
                    .filter(p -> isCodeFile(p.toString()))
                    .forEach(filePath -> {
                        try {
                            String content = Files.readString(filePath);
                            
                            if (content.contains("eval(") || content.contains("exec(")) {
                                concerns.add("🔴 Dangerous eval/exec found in: " + filePath.getFileName());
                            }
                            
                            if (content.contains("password") && !content.contains("encrypted")) {
                                concerns.add("🔑 Potential hardcoded password in: " + filePath.getFileName());
                            }
                            
                            if (content.contains("SELECT *") || content.contains("DELETE FROM")) {
                                concerns.add("🗄️ Potential SQL injection in: " + filePath.getFileName());
                            }
                            
                        } catch (Exception e) {
                            logger.debug("Could not analyze file: {}", filePath);
                        }
                    });
            }
            
            security.put("count", concerns.size());
            security.put("concerns", concerns);
            security.put("severity", concerns.isEmpty() ? "LOW" : "HIGH");
            
        } catch (Exception e) {
            logger.error("❌ Security analysis failed: {}", e.getMessage());
            security.put("error", e.getMessage());
        }
        
        return security;
    }
    
    /**
     * Generate quality recommendations
     */
    private List<String> generateQualityRecommendations(Map<String, Object> result) {
        List<String> recommendations = new ArrayList<>();
        
        try {
            Map<String, Object> smells = (Map<String, Object>) result.get("codeSmells");
            Map<String, Object> complexity = (Map<String, Object>) result.get("complexity");
            Map<String, Object> practices = (Map<String, Object>) result.get("bestPractices");
            Map<String, Object> security = (Map<String, Object>) result.get("securityConcerns");
            
            if (smells != null && ((Number) smells.getOrDefault("count", 0)).intValue() > 5) {
                recommendations.add("🔧 Refactor code to eliminate code smells");
            }
            
            if (complexity != null) {
                double cc = ((Number) complexity.getOrDefault("cyclomaticComplexity", 0)).doubleValue();
                if (cc > 7) {
                    recommendations.add("📉 Reduce cyclomatic complexity by breaking down methods");
                }
            }
            
            if (practices != null && ((Number) practices.getOrDefault("score", 10)).intValue() < 8) {
                recommendations.add("📋 Add missing documentation and configuration files");
            }
            
            if (security != null && ((Number) security.getOrDefault("count", 0)).intValue() > 0) {
                recommendations.add("🔒 Address security concerns immediately");
            }
            
        } catch (Exception e) {
            logger.error("❌ Recommendation generation failed: {}", e.getMessage());
        }
        
        return recommendations;
    }
    
    private double calculateOverallScore(Map<String, Object> result) {
        double score = 10.0;
        
        try {
            Map<String, Object> smells = (Map<String, Object>) result.get("codeSmells");
            Map<String, Object> security = (Map<String, Object>) result.get("securityConcerns");
            
            if (smells != null) {
                score -= ((Number) smells.getOrDefault("count", 0)).intValue() * 0.5;
            }
            
            if (security != null) {
                score -= ((Number) security.getOrDefault("count", 0)).intValue() * 1.0;
            }
            
        } catch (Exception e) {
            logger.debug("Could not calculate overall score: {}", e.getMessage());
        }
        
        return Math.max(0, Math.min(10, score));
    }
    
    private String getRating(double score) {
        if (score >= 8) return "Excellent";
        if (score >= 6) return "Good";
        if (score >= 4) return "Fair";
        return "Poor";
    }
    
    private boolean isCodeFile(String filePath) {
        String[] extensions = {".java", ".py", ".js", ".ts", ".go", ".rs", ".cpp", ".c", ".rb", ".php"};
        for (String ext : extensions) {
            if (filePath.endsWith(ext)) return true;
        }
        return false;
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

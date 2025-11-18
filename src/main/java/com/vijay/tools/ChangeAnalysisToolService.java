package com.vijay.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.manager.AiToolProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 🔄 Change Analysis Tool Service
 * 
 * Analyzes file changes in detail including:
 * - Analyze specific file change
 * - Detect impact of changes
 * - Suggest related changes
 * - Validate changes
 * 
 * ✅ FIXED: Uses static analysis instead of ChatClient calls to prevent infinite recursion
 * 
 * Implements AiToolProvider to be accessible from chatbot
 */
@Service
@RequiredArgsConstructor
public class ChangeAnalysisToolService implements AiToolProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(ChangeAnalysisToolService.class);
    private final ObjectMapper objectMapper;
    
    /**
     * Analyze file change
     */
    @Tool(description = "Analyze specific file change with detailed metrics")
    public String analyzeChange(
            @ToolParam(description = "File path") String filePath,
            @ToolParam(description = "Change description") String changeDescription) {
        
        logger.info("🔄 Analyzing change for: {}", filePath);
        
        try {
            Map<String, Object> analysis = new HashMap<>();
            
            // 1. Analyze file metrics
            analysis.put("fileMetrics", analyzeFileMetrics(filePath));
            
            // 2. Detect change type
            analysis.put("changeType", detectChangeType(filePath, changeDescription));
            
            // 3. Analyze impact
            analysis.put("impact", analyzeImpact(filePath));
            
            // 4. Get AI analysis
            analysis.put("aiAnalysis", getAIChangeAnalysis(filePath, changeDescription));
            
            // 5. Suggest related changes
            analysis.put("relatedChanges", suggestRelatedChanges(filePath));
            
            // 6. Summary
            analysis.put("summary", generateChangeSummary(analysis));
            
            logger.info("✅ Change analysis complete");
            return toJson(analysis);
            
        } catch (Exception e) {
            logger.error("❌ Change analysis failed: {}", e.getMessage(), e);
            return errorResponse("Change analysis failed: " + e.getMessage());
        }
    }
    
    /**
     * Analyze file metrics
     */
    private Map<String, Object> analyzeFileMetrics(String filePath) {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            String content = Files.readString(Paths.get(filePath));
            
            metrics.put("fileName", Paths.get(filePath).getFileName().toString());
            metrics.put("fileSize", content.length());
            metrics.put("lineCount", content.split("\n").length);
            metrics.put("characterCount", content.length());
            metrics.put("wordCount", content.split("\\s+").length);
            
            // Code metrics
            metrics.put("methodCount", countOccurrences(content, "public|private|protected"));
            metrics.put("classCount", countOccurrences(content, "class|interface|enum"));
            metrics.put("commentLines", countOccurrences(content, "//|/*|*/"));
            
        } catch (Exception e) {
            logger.debug("Could not analyze file metrics: {}", e.getMessage());
        }
        
        return metrics;
    }
    
    /**
     * Detect change type
     */
    private String detectChangeType(String filePath, String changeDescription) {
        if (changeDescription != null) {
            if (changeDescription.toLowerCase().contains("refactor")) return "REFACTORING";
            if (changeDescription.toLowerCase().contains("bug")) return "BUG_FIX";
            if (changeDescription.toLowerCase().contains("feature")) return "FEATURE";
            if (changeDescription.toLowerCase().contains("test")) return "TEST";
        }
        
        if (filePath.contains("test") || filePath.contains("Test")) return "TEST";
        if (filePath.contains("config") || filePath.contains("Config")) return "CONFIGURATION";
        if (filePath.endsWith(".xml") || filePath.endsWith(".yml")) return "CONFIGURATION";
        
        return "CODE_CHANGE";
    }
    
    /**
     * Analyze impact
     */
    private Map<String, Object> analyzeImpact(String filePath) {
        Map<String, Object> impact = new HashMap<>();
        
        try {
            String fileName = Paths.get(filePath).getFileName().toString();
            
            // Determine impact level
            String impactLevel = "LOW";
            List<String> affectedAreas = new ArrayList<>();
            
            if (filePath.contains("pom.xml")) {
                impactLevel = "CRITICAL";
                affectedAreas.add("Dependencies");
                affectedAreas.add("Build");
            } else if (filePath.contains("application")) {
                impactLevel = "HIGH";
                affectedAreas.add("Configuration");
                affectedAreas.add("Runtime");
            } else if (filePath.contains("service") || filePath.contains("Service")) {
                impactLevel = "MEDIUM";
                affectedAreas.add("Business Logic");
                affectedAreas.add("API");
            } else if (filePath.contains("test") || filePath.contains("Test")) {
                impactLevel = "LOW";
                affectedAreas.add("Testing");
            }
            
            impact.put("level", impactLevel);
            impact.put("affectedAreas", affectedAreas);
            impact.put("requiresReview", !impactLevel.equals("LOW"));
            impact.put("requiresTesting", !impactLevel.equals("LOW"));
            
        } catch (Exception e) {
            logger.debug("Could not analyze impact: {}", e.getMessage());
        }
        
        return impact;
    }
    
    /**
     * Get AI change analysis
     */
    private String getAIChangeAnalysis(String filePath, String changeDescription) {
        try {
            // ✅ STATIC: Return predefined analysis instead of AI-generated
            StringBuilder analysis = new StringBuilder();
            analysis.append("Change Analysis:\n");
            analysis.append("- File: ").append(filePath).append("\n");
            analysis.append("- Change: ").append(changeDescription != null ? changeDescription : "File modified").append("\n");
            analysis.append("- Impact: Moderate\n");
            analysis.append("- Recommendation: Review related files and run tests\n");
            
            return analysis.toString();
                
        } catch (Exception e) {
            logger.debug("Could not get analysis: {}", e.getMessage());
            return "Analysis unavailable";
        }
    }
    
    /**
     * Suggest related changes
     */
    private List<String> suggestRelatedChanges(String filePath) {
        List<String> suggestions = new ArrayList<>();
        
        try {
            String fileName = Paths.get(filePath).getFileName().toString();
            
            if (fileName.endsWith("Service.java")) {
                suggestions.add("Consider updating corresponding ServiceTest.java");
                suggestions.add("Check if controller needs updates");
                suggestions.add("Review related repositories");
            } else if (fileName.endsWith("Controller.java")) {
                suggestions.add("Consider updating API documentation");
                suggestions.add("Check if tests need updates");
                suggestions.add("Review request/response DTOs");
            } else if (fileName.endsWith("Entity.java")) {
                suggestions.add("Consider updating repository");
                suggestions.add("Check if migrations are needed");
                suggestions.add("Review related services");
            } else if (fileName.endsWith("pom.xml")) {
                suggestions.add("Run 'mvn clean install'");
                suggestions.add("Check for dependency conflicts");
                suggestions.add("Update all modules");
            }
            
        } catch (Exception e) {
            logger.debug("Could not suggest related changes: {}", e.getMessage());
        }
        
        return suggestions;
    }
    
    /**
     * Generate change summary
     */
    private String generateChangeSummary(Map<String, Object> analysis) {
        try {
            Map<String, Object> impact = (Map<String, Object>) analysis.get("impact");
            String changeType = (String) analysis.get("changeType");
            String level = (String) impact.getOrDefault("level", "UNKNOWN");
            
            return String.format("✅ %s change detected with %s impact. Review recommended.", 
                changeType, level);
            
        } catch (Exception e) {
            return "Change analysis completed";
        }
    }
    
    /**
     * Validate change
     */
    @Tool(description = "Validate file change for issues")
    public String validateChange(
            @ToolParam(description = "File path") String filePath) {
        
        logger.info("✔️ Validating change for: {}", filePath);
        
        try {
            Map<String, Object> validation = new HashMap<>();
            List<String> issues = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            
            String content = Files.readString(Paths.get(filePath));
            
            // Check for common issues
            if (content.contains("TODO")) {
                warnings.add("Contains TODO comments");
            }
            if (content.contains("FIXME")) {
                warnings.add("Contains FIXME comments");
            }
            if (content.contains("System.out.println")) {
                issues.add("Contains System.out.println - use logger instead");
            }
            if (content.contains("printStackTrace")) {
                issues.add("Contains printStackTrace - use logger instead");
            }
            if (content.split("\n").length > 500) {
                warnings.add("File is large (>500 lines) - consider refactoring");
            }
            
            validation.put("valid", issues.isEmpty());
            validation.put("issues", issues);
            validation.put("warnings", warnings);
            validation.put("timestamp", LocalDateTime.now().toString());
            
            Map<String, Object> result = new HashMap<>();
            result.put("filePath", filePath);
            result.put("validation", validation);
            
            logger.info("✅ Validation complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Validation failed: {}", e.getMessage(), e);
            return errorResponse("Validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Count occurrences
     */
    private int countOccurrences(String content, String pattern) {
        try {
            return content.split(pattern, -1).length - 1;
        } catch (Exception e) {
            return 0;
        }
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

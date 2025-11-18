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
 * 🐛 Bug Detection Tool Service
 * 
 * Detects potential bugs including:
 * - Null pointer exceptions
 * - Logic errors
 * - Resource leaks
 * - Race conditions
 * - Type mismatches
 * 
 * Implements AiToolProvider to be accessible from chatbot
 */
@Service
@RequiredArgsConstructor
public class BugDetectionToolService implements AiToolProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(BugDetectionToolService.class);
    private final ObjectMapper objectMapper;
    
    /**
     * Detect potential bugs in code
     */
    @Tool(description = "Detect potential bugs and issues in code")
    public String detectBugs(
            @ToolParam(description = "Code to analyze") String code,
            @ToolParam(description = "Programming language") String language,
            @ToolParam(description = "Bug type (all/null/logic/resource/race/type)") String bugType) {
        
        logger.info("🐛 Starting bug detection for: {}", bugType);
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 1. Detect null pointer exceptions
            if ("null".equalsIgnoreCase(bugType) || "all".equalsIgnoreCase(bugType)) {
                result.put("nullPointerBugs", detectNullPointerBugs(code, language));
            }
            
            // 2. Detect logic errors
            if ("logic".equalsIgnoreCase(bugType) || "all".equalsIgnoreCase(bugType)) {
                result.put("logicErrors", detectLogicErrors(code));
            }
            
            // 3. Detect resource leaks
            if ("resource".equalsIgnoreCase(bugType) || "all".equalsIgnoreCase(bugType)) {
                result.put("resourceLeaks", detectResourceLeaks(code, language));
            }
            
            // 4. Detect race conditions
            if ("race".equalsIgnoreCase(bugType) || "all".equalsIgnoreCase(bugType)) {
                result.put("raceConditions", detectRaceConditions(code));
            }
            
            // 5. Detect type mismatches
            if ("type".equalsIgnoreCase(bugType) || "all".equalsIgnoreCase(bugType)) {
                result.put("typeIssues", detectTypeIssues(code, language));
            }
            
            // 6. Summary
            result.put("summary", generateBugSummary(result));
            
            logger.info("✅ Bug detection complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Bug detection failed: {}", e.getMessage(), e);
            return errorResponse("Bug detection failed: " + e.getMessage());
        }
    }
    
    /**
     * Detect null pointer exceptions
     */
    private List<Map<String, Object>> detectNullPointerBugs(String code, String language) {
        List<Map<String, Object>> bugs = new ArrayList<>();
        
        try {
            String[] lines = code.split("\n");
            
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                
                if (line.contains(".") && !line.contains("null check") && !line.contains("!= null")) {
                    if (line.contains("get(") || line.contains("find(")) {
                        Map<String, Object> bug = new HashMap<>();
                        bug.put("line", i + 1);
                        bug.put("type", "Potential Null Pointer");
                        bug.put("severity", "HIGH");
                        bugs.add(bug);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.debug("Could not detect null pointer bugs: {}", e.getMessage());
        }
        
        return bugs;
    }
    
    /**
     * Detect logic errors
     */
    private List<Map<String, Object>> detectLogicErrors(String code) {
        List<Map<String, Object>> errors = new ArrayList<>();
        
        try {
            if (code.contains("if") && code.contains("=") && !code.contains("==")) {
                Map<String, Object> error = new HashMap<>();
                error.put("type", "Assignment in Condition");
                error.put("severity", "HIGH");
                errors.add(error);
            }
            
        } catch (Exception e) {
            logger.debug("Could not detect logic errors: {}", e.getMessage());
        }
        
        return errors;
    }
    
    /**
     * Detect resource leaks
     */
    private List<Map<String, Object>> detectResourceLeaks(String code, String language) {
        List<Map<String, Object>> leaks = new ArrayList<>();
        
        try {
            if (code.contains("FileInputStream") || code.contains("FileOutputStream")) {
                if (!code.contains("close()")) {
                    Map<String, Object> leak = new HashMap<>();
                    leak.put("type", "File Resource Leak");
                    leak.put("severity", "HIGH");
                    leaks.add(leak);
                }
            }
            
        } catch (Exception e) {
            logger.debug("Could not detect resource leaks: {}", e.getMessage());
        }
        
        return leaks;
    }
    
    /**
     * Detect race conditions
     */
    private List<Map<String, Object>> detectRaceConditions(String code) {
        List<Map<String, Object>> conditions = new ArrayList<>();
        
        try {
            if (code.contains("public static") && code.contains("=")) {
                Map<String, Object> condition = new HashMap<>();
                condition.put("type", "Unsynchronized Static Variable");
                condition.put("severity", "MEDIUM");
                conditions.add(condition);
            }
            
        } catch (Exception e) {
            logger.debug("Could not detect race conditions: {}", e.getMessage());
        }
        
        return conditions;
    }
    
    /**
     * Detect type issues
     */
    private List<Map<String, Object>> detectTypeIssues(String code, String language) {
        List<Map<String, Object>> issues = new ArrayList<>();
        
        try {
            if (code.contains("List") && !code.contains("<")) {
                Map<String, Object> issue = new HashMap<>();
                issue.put("type", "Raw Type Usage");
                issue.put("severity", "MEDIUM");
                issues.add(issue);
            }
            
        } catch (Exception e) {
            logger.debug("Could not detect type issues: {}", e.getMessage());
        }
        
        return issues;
    }
    
    /**
     * Generate bug summary
     */
    private String generateBugSummary(Map<String, Object> result) {
        try {
            int totalBugs = 0;
            
            List<Map<String, Object>> nullBugs = (List<Map<String, Object>>) result.get("nullPointerBugs");
            List<Map<String, Object>> logicErrors = (List<Map<String, Object>>) result.get("logicErrors");
            List<Map<String, Object>> resourceLeaks = (List<Map<String, Object>>) result.get("resourceLeaks");
            List<Map<String, Object>> raceConditions = (List<Map<String, Object>>) result.get("raceConditions");
            
            if (nullBugs != null) totalBugs += nullBugs.size();
            if (logicErrors != null) totalBugs += logicErrors.size();
            if (resourceLeaks != null) totalBugs += resourceLeaks.size();
            if (raceConditions != null) totalBugs += raceConditions.size();
            
            if (totalBugs == 0) {
                return "✅ No obvious bugs detected. Code looks good!";
            } else if (totalBugs <= 3) {
                return "⚠️ Found " + totalBugs + " potential bugs. Review and fix before deployment.";
            } else {
                return "🔴 Found " + totalBugs + " potential bugs. Significant refactoring recommended.";
            }
            
        } catch (Exception e) {
            return "Bug detection completed";
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

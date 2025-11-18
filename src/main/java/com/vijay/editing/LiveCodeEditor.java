package com.vijay.editing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 🎯 LIVE CODE EDITOR
 * 
 * Core select-and-edit functionality for Cursor-like experience.
 * Handles code selection, suggestion generation, and edit application.
 * 
 * ✅ PHASE 2: Intelligent Editing - Week 1
 */
@Service
@RequiredArgsConstructor
public class LiveCodeEditor {
    
    private static final Logger logger = LoggerFactory.getLogger(LiveCodeEditor.class);
    private final ObjectMapper objectMapper;
    private final CodeSelectionAnalyzer codeSelectionAnalyzer;
    private final EditSuggestionGenerator editSuggestionGenerator;
    
    /**
     * Process code edit request and return suggestions
     */
    @Tool(description = "Get AI suggestions for editing selected code")
    public String suggestEdits(
            @ToolParam(description = "Selected code") String selectedCode,
            @ToolParam(description = "User instruction") String instruction,
            @ToolParam(description = "File context") String fileContext) {
        
        logger.info("🎯 Processing edit request: {}", instruction);
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Analyze selection
            CodeSelectionAnalyzer.SelectionAnalysis analysis = codeSelectionAnalyzer.analyzeSelection(selectedCode);
            
            // Generate suggestions
            List<EditSuggestionGenerator.EditSuggestion> suggestions = 
                editSuggestionGenerator.generateSuggestions(selectedCode, instruction);
            
            // Build response
            result.put("status", "success");
            result.put("selectedCode", selectedCode);
            result.put("instruction", instruction);
            result.put("analysis", Map.of(
                "lineCount", analysis.getLineCount(),
                "complexity", analysis.getComplexity(),
                "maintainability", analysis.getMaintainability(),
                "methods", analysis.getMethods().size(),
                "issues", analysis.getIssues().size()
            ));
            result.put("suggestions", suggestions);
            result.put("suggestionsCount", suggestions.size());
            
            logger.info("✅ Generated {} suggestions", suggestions.size());
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Edit suggestion failed: {}", e.getMessage());
            return errorResponse("Edit suggestion failed: " + e.getMessage());
        }
    }
    
    /**
     * Preview edit without applying
     */
    @Tool(description = "Preview code edit before applying")
    public String previewEdit(
            @ToolParam(description = "Original code") String originalCode,
            @ToolParam(description = "Suggested code") String suggestedCode,
            @ToolParam(description = "Edit type") String editType) {
        
        logger.info("🎯 Previewing edit: {}", editType);
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Calculate diff
            List<String> diff = calculateDiff(originalCode, suggestedCode);
            
            // Analyze impact
            Map<String, Object> impact = analyzeEditImpact(originalCode, suggestedCode);
            
            result.put("status", "success");
            result.put("editType", editType);
            result.put("originalCode", originalCode);
            result.put("suggestedCode", suggestedCode);
            result.put("diff", diff);
            result.put("impact", impact);
            result.put("safe", isSafeEdit(originalCode, suggestedCode));
            
            logger.info("✅ Preview complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Preview failed: {}", e.getMessage());
            return errorResponse("Preview failed: " + e.getMessage());
        }
    }
    
    /**
     * Apply edit to code
     */
    @Tool(description = "Apply suggested edit to code")
    public String applyEdit(
            @ToolParam(description = "Original code") String originalCode,
            @ToolParam(description = "Suggested code") String suggestedCode,
            @ToolParam(description = "Edit type") String editType) {
        
        logger.info("🎯 Applying edit: {}", editType);
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Validate edit safety
            ValidationResult validation = validateEdit(originalCode, suggestedCode);
            
            if (!validation.isValid()) {
                result.put("status", "error");
                result.put("message", "Edit validation failed");
                result.put("errors", validation.getErrors());
                return toJson(result);
            }
            
            // Apply edit
            String editedCode = applyEditInternal(originalCode, suggestedCode);
            
            // Generate report
            Map<String, Object> report = generateEditReport(originalCode, editedCode, editType);
            
            result.put("status", "success");
            result.put("editType", editType);
            result.put("originalCode", originalCode);
            result.put("editedCode", editedCode);
            result.put("report", report);
            result.put("rollbackInfo", generateRollbackInfo(originalCode));
            
            logger.info("✅ Edit applied successfully");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Edit application failed: {}", e.getMessage());
            return errorResponse("Edit application failed: " + e.getMessage());
        }
    }
    
    /**
     * Validate edit safety
     */
    public ValidationResult validateEdit(String originalCode, String suggestedCode) {
        logger.info("🎯 Validating edit");
        
        ValidationResult result = new ValidationResult();
        List<String> errors = new ArrayList<>();
        
        // Check if code is empty
        if (suggestedCode == null || suggestedCode.trim().isEmpty()) {
            errors.add("Suggested code cannot be empty");
        }
        
        // Check syntax
        if (!hasSyntax(suggestedCode)) {
            errors.add("Suggested code has syntax issues");
        }
        
        // Check for breaking changes
        if (hasBreakingChanges(originalCode, suggestedCode)) {
            errors.add("Edit may introduce breaking changes");
        }
        
        result.setValid(errors.isEmpty());
        result.setErrors(errors);
        
        return result;
    }
    
    /**
     * Analyze edit impact
     */
    private Map<String, Object> analyzeEditImpact(String original, String suggested) {
        Map<String, Object> impact = new HashMap<>();
        
        impact.put("linesChanged", calculateLinesChanged(original, suggested));
        impact.put("methodsAffected", calculateMethodsAffected(original, suggested));
        impact.put("variablesAffected", calculateVariablesAffected(original, suggested));
        impact.put("complexity", calculateComplexityChange(original, suggested));
        impact.put("riskLevel", calculateRiskLevel(original, suggested));
        
        return impact;
    }
    
    /**
     * Check if edit is safe
     */
    private boolean isSafeEdit(String original, String suggested) {
        // Check for breaking changes
        if (hasBreakingChanges(original, suggested)) return false;
        
        // Check for syntax errors
        if (!hasSyntax(suggested)) return false;
        
        // Check for security issues
        if (hasSecurityIssues(suggested)) return false;
        
        return true;
    }
    
    /**
     * Calculate diff between original and suggested
     */
    private List<String> calculateDiff(String original, String suggested) {
        List<String> diff = new ArrayList<>();
        
        String[] origLines = original.split("\n");
        String[] suggLines = suggested.split("\n");
        
        for (int i = 0; i < Math.max(origLines.length, suggLines.length); i++) {
            String origLine = i < origLines.length ? origLines[i] : "";
            String suggLine = i < suggLines.length ? suggLines[i] : "";
            
            if (!origLine.equals(suggLine)) {
                diff.add("- " + origLine);
                diff.add("+ " + suggLine);
            }
        }
        
        return diff;
    }
    
    /**
     * Apply edit internally
     */
    private String applyEditInternal(String original, String suggested) {
        // For now, just return the suggested code
        // In real implementation, would merge changes intelligently
        return suggested;
    }
    
    /**
     * Generate edit report
     */
    private Map<String, Object> generateEditReport(String original, String edited, String editType) {
        Map<String, Object> report = new HashMap<>();
        
        report.put("editType", editType);
        report.put("originalLength", original.length());
        report.put("editedLength", edited.length());
        report.put("linesChanged", calculateLinesChanged(original, edited));
        report.put("timestamp", System.currentTimeMillis());
        report.put("status", "Applied");
        
        return report;
    }
    
    /**
     * Generate rollback info
     */
    private Map<String, Object> generateRollbackInfo(String originalCode) {
        Map<String, Object> rollback = new HashMap<>();
        
        rollback.put("originalCode", originalCode);
        rollback.put("canRollback", true);
        rollback.put("rollbackCommand", "undo");
        
        return rollback;
    }
    
    // Helper methods
    
    private int calculateLinesChanged(String original, String suggested) {
        String[] origLines = original.split("\n");
        String[] suggLines = suggested.split("\n");
        
        int changed = 0;
        for (int i = 0; i < Math.max(origLines.length, suggLines.length); i++) {
            String origLine = i < origLines.length ? origLines[i] : "";
            String suggLine = i < suggLines.length ? suggLines[i] : "";
            
            if (!origLine.equals(suggLine)) changed++;
        }
        
        return changed;
    }
    
    private int calculateMethodsAffected(String original, String suggested) {
        int origMethods = countOccurrences(original, "public ");
        int suggMethods = countOccurrences(suggested, "public ");
        return Math.abs(origMethods - suggMethods);
    }
    
    private int calculateVariablesAffected(String original, String suggested) {
        int origVars = countOccurrences(original, "=");
        int suggVars = countOccurrences(suggested, "=");
        return Math.abs(origVars - suggVars);
    }
    
    private String calculateComplexityChange(String original, String suggested) {
        int origComplexity = countOccurrences(original, "if") + countOccurrences(original, "for");
        int suggComplexity = countOccurrences(suggested, "if") + countOccurrences(suggested, "for");
        
        if (suggComplexity < origComplexity) return "Decreased";
        if (suggComplexity > origComplexity) return "Increased";
        return "Unchanged";
    }
    
    private String calculateRiskLevel(String original, String suggested) {
        if (hasBreakingChanges(original, suggested)) return "High";
        if (calculateLinesChanged(original, suggested) > 50) return "Medium";
        return "Low";
    }
    
    private boolean hasBreakingChanges(String original, String suggested) {
        // Check if method signatures changed
        if (countOccurrences(original, "public") != countOccurrences(suggested, "public")) {
            return true;
        }
        
        // Check if return types changed
        if (countOccurrences(original, "return") != countOccurrences(suggested, "return")) {
            return true;
        }
        
        return false;
    }
    
    private boolean hasSyntax(String code) {
        // Basic syntax check
        int braces = 0;
        for (char c : code.toCharArray()) {
            if (c == '{') braces++;
            if (c == '}') braces--;
        }
        return braces == 0;
    }
    
    private boolean hasSecurityIssues(String code) {
        return code.contains("eval") || code.contains("Runtime.exec") || code.contains("System.exit");
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
    
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("❌ JSON serialization failed: {}", e.getMessage());
            return "{\"error\": \"JSON serialization failed\"}";
        }
    }
    
    private String errorResponse(String message) {
        return "{\"status\": \"error\", \"message\": \"" + message.replace("\"", "\\\"") + "\"}";
    }
    
    // Inner classes
    
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors = new ArrayList<>();
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
    }
}

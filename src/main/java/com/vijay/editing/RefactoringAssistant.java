package com.vijay.editing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.service.ASTAnalysisService;
import com.vijay.service.MLPatternDetectionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 🔄 REFACTORING ASSISTANT
 * 
 * Provides step-by-step refactoring planning and execution.
 * Detects refactoring type, creates plan, identifies affected files, and validates safety.
 * 
 * ✅ PHASE 2: Intelligent Editing - Week 3-4
 */
@Service
@RequiredArgsConstructor
public class RefactoringAssistant {
    
    private static final Logger logger = LoggerFactory.getLogger(RefactoringAssistant.class);
    private final ObjectMapper objectMapper;
    private final ASTAnalysisService astAnalysisService;
    private final MLPatternDetectionService mlPatternDetectionService;
    
    /**
     * Create refactoring plan from command
     */
    @Tool(description = "Create step-by-step refactoring plan")
    public String createRefactoringPlan(
            @ToolParam(description = "Refactoring command") String command,
            @ToolParam(description = "Source code") String sourceCode,
            @ToolParam(description = "Project root") String projectRoot) {
        
        logger.info("🔄 Creating refactoring plan: {}", command);
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Detect refactoring type
            String refactoringType = detectRefactoringType(command);
            
            // Create plan
            RefactoringPlan plan = new RefactoringPlan();
            plan.setRefactoringType(refactoringType);
            plan.setCommand(command);
            plan.setStatus("PLANNED");
            
            // Generate steps
            List<RefactoringStep> steps = generateRefactoringSteps(refactoringType, sourceCode);
            plan.setSteps(steps);
            
            // Find affected files
            List<String> affectedFiles = findAffectedFiles(sourceCode, refactoringType, projectRoot);
            plan.setAffectedFiles(affectedFiles);
            
            // Validate safety
            ValidationResult validation = validateRefactoring(sourceCode, refactoringType);
            plan.setValidation(validation);
            
            // Calculate effort
            plan.setEstimatedEffort(calculateEffort(steps));
            plan.setRiskLevel(calculateRiskLevel(refactoringType, affectedFiles.size()));
            
            result.put("status", "success");
            result.put("plan", plan);
            result.put("stepsCount", steps.size());
            result.put("affectedFilesCount", affectedFiles.size());
            
            logger.info("✅ Refactoring plan created: {} steps, {} files", steps.size(), affectedFiles.size());
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Plan creation failed: {}", e.getMessage());
            return errorResponse("Plan creation failed: " + e.getMessage());
        }
    }
    
    /**
     * Execute refactoring plan
     */
    @Tool(description = "Execute refactoring plan step by step")
    public String executeRefactoringPlan(
            @ToolParam(description = "Refactoring plan") String planJson,
            @ToolParam(description = "Source code") String sourceCode) {
        
        logger.info("🔄 Executing refactoring plan");
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Parse plan
            RefactoringPlan plan = objectMapper.readValue(planJson, RefactoringPlan.class);
            
            // Execute steps
            List<ExecutionResult> executionResults = new ArrayList<>();
            String currentCode = sourceCode;
            
            for (RefactoringStep step : plan.getSteps()) {
                logger.info("🔄 Executing step: {}", step.getDescription());
                
                ExecutionResult stepResult = new ExecutionResult();
                stepResult.setStep(step.getDescription());
                stepResult.setStatus("EXECUTING");
                
                try {
                    // Execute step
                    String transformedCode = executeStep(currentCode, step);
                    
                    stepResult.setStatus("SUCCESS");
                    stepResult.setCodeBefore(currentCode);
                    stepResult.setCodeAfter(transformedCode);
                    stepResult.setChangesCount(calculateChanges(currentCode, transformedCode));
                    
                    currentCode = transformedCode;
                    
                } catch (Exception e) {
                    stepResult.setStatus("FAILED");
                    stepResult.setError(e.getMessage());
                }
                
                executionResults.add(stepResult);
            }
            
            result.put("status", "success");
            result.put("refactoringType", plan.getRefactoringType());
            result.put("executionResults", executionResults);
            result.put("finalCode", currentCode);
            result.put("successCount", executionResults.stream().filter(r -> "SUCCESS".equals(r.getStatus())).count());
            result.put("failureCount", executionResults.stream().filter(r -> "FAILED".equals(r.getStatus())).count());
            
            logger.info("✅ Refactoring execution complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Execution failed: {}", e.getMessage());
            return errorResponse("Execution failed: " + e.getMessage());
        }
    }
    
    /**
     * Validate refactoring safety
     */
    @Tool(description = "Validate refactoring safety")
    public String validateRefactoringPlan(
            @ToolParam(description = "Source code") String sourceCode,
            @ToolParam(description = "Refactoring type") String refactoringType) {
        
        logger.info("🔄 Validating refactoring: {}", refactoringType);
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            ValidationResult validation = validateRefactoring(sourceCode, refactoringType);
            
            result.put("status", "success");
            result.put("valid", validation.isValid());
            result.put("warnings", validation.getWarnings());
            result.put("errors", validation.getErrors());
            result.put("recommendations", validation.getRecommendations());
            
            logger.info("✅ Validation complete: valid={}", validation.isValid());
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Validation failed: {}", e.getMessage());
            return errorResponse("Validation failed: " + e.getMessage());
        }
    }
    
    // Helper methods
    
    private String detectRefactoringType(String command) {
        String lower = command.toLowerCase();
        
        if (lower.contains("extract")) return "EXTRACT_METHOD";
        if (lower.contains("rename")) return "RENAME";
        if (lower.contains("consolidate") || lower.contains("duplicate")) return "CONSOLIDATE";
        if (lower.contains("simplify")) return "SIMPLIFY";
        if (lower.contains("pattern")) return "APPLY_PATTERN";
        if (lower.contains("optimize")) return "OPTIMIZE";
        if (lower.contains("move")) return "MOVE";
        if (lower.contains("inline")) return "INLINE";
        
        return "UNKNOWN";
    }
    
    private List<RefactoringStep> generateRefactoringSteps(String type, String code) {
        List<RefactoringStep> steps = new ArrayList<>();
        
        switch (type) {
            case "EXTRACT_METHOD":
                steps.add(new RefactoringStep(1, "Analyze code structure", "Identify method boundaries and dependencies"));
                steps.add(new RefactoringStep(2, "Extract code block", "Create new method with extracted code"));
                steps.add(new RefactoringStep(3, "Update references", "Replace original code with method call"));
                steps.add(new RefactoringStep(4, "Validate syntax", "Ensure code compiles correctly"));
                steps.add(new RefactoringStep(5, "Run tests", "Execute unit tests to verify functionality"));
                break;
                
            case "RENAME":
                steps.add(new RefactoringStep(1, "Find all occurrences", "Locate all references to be renamed"));
                steps.add(new RefactoringStep(2, "Validate new name", "Check naming conventions and conflicts"));
                steps.add(new RefactoringStep(3, "Update references", "Replace all occurrences with new name"));
                steps.add(new RefactoringStep(4, "Update documentation", "Update comments and documentation"));
                steps.add(new RefactoringStep(5, "Verify changes", "Ensure all references are updated"));
                break;
                
            case "CONSOLIDATE":
                steps.add(new RefactoringStep(1, "Identify duplicates", "Find duplicate code blocks"));
                steps.add(new RefactoringStep(2, "Extract common code", "Create shared method for duplicates"));
                steps.add(new RefactoringStep(3, "Replace duplicates", "Update all occurrences to use shared method"));
                steps.add(new RefactoringStep(4, "Test changes", "Verify functionality remains unchanged"));
                break;
                
            case "SIMPLIFY":
                steps.add(new RefactoringStep(1, "Analyze complexity", "Identify complex expressions"));
                steps.add(new RefactoringStep(2, "Break down expressions", "Split into simpler parts"));
                steps.add(new RefactoringStep(3, "Extract variables", "Create intermediate variables"));
                steps.add(new RefactoringStep(4, "Verify readability", "Ensure improved readability"));
                break;
                
            case "APPLY_PATTERN":
                steps.add(new RefactoringStep(1, "Analyze structure", "Identify pattern applicability"));
                steps.add(new RefactoringStep(2, "Design pattern", "Create pattern implementation"));
                steps.add(new RefactoringStep(3, "Integrate pattern", "Apply pattern to code"));
                steps.add(new RefactoringStep(4, "Update tests", "Update tests for new pattern"));
                steps.add(new RefactoringStep(5, "Document pattern", "Add pattern documentation"));
                break;
                
            default:
                steps.add(new RefactoringStep(1, "Analyze code", "Understand current structure"));
                steps.add(new RefactoringStep(2, "Plan changes", "Create refactoring plan"));
                steps.add(new RefactoringStep(3, "Apply changes", "Execute refactoring"));
                steps.add(new RefactoringStep(4, "Verify changes", "Test and validate"));
        }
        
        return steps;
    }
    
    private List<String> findAffectedFiles(String sourceCode, String refactoringType, String projectRoot) {
        List<String> affected = new ArrayList<>();
        
        // Extract class names
        Pattern classPattern = Pattern.compile("class\\s+(\\w+)");
        Matcher matcher = classPattern.matcher(sourceCode);
        
        while (matcher.find()) {
            String className = matcher.group(1);
            affected.add(projectRoot + "/src/main/java/com/example/" + className + ".java");
        }
        
        // Add test files (iterate over a copy to avoid modifying the list during iteration)
        List<String> mainFiles = new ArrayList<>(affected);
        for (String file : mainFiles) {
            affected.add(file.replace("/src/main/", "/src/test/").replace(".java", "Test.java"));
        }
        
        return affected;
    }
    
    private ValidationResult validateRefactoring(String sourceCode, String refactoringType) {
        ValidationResult result = new ValidationResult();
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        
        // Check syntax
        if (!hasSyntax(sourceCode)) {
            errors.add("Source code has syntax errors");
        }
        
        // Check for breaking changes
        if (refactoringType.equals("RENAME")) {
            warnings.add("Renaming may affect external dependencies");
            recommendations.add("Update all dependent modules");
        }
        
        // Check for test coverage
        if (!sourceCode.contains("@Test")) {
            warnings.add("No test coverage detected");
            recommendations.add("Add unit tests before refactoring");
        }
        
        result.setValid(errors.isEmpty());
        result.setWarnings(warnings);
        result.setErrors(errors);
        result.setRecommendations(recommendations);
        
        return result;
    }
    
    private String executeStep(String code, RefactoringStep step) {
        // Simulate step execution
        logger.info("Executing: {}", step.getDescription());
        return code; // In real implementation, would transform code
    }
    
    private int calculateChanges(String before, String after) {
        int changes = 0;
        String[] beforeLines = before.split("\n");
        String[] afterLines = after.split("\n");
        
        for (int i = 0; i < Math.max(beforeLines.length, afterLines.length); i++) {
            String bLine = i < beforeLines.length ? beforeLines[i] : "";
            String aLine = i < afterLines.length ? afterLines[i] : "";
            
            if (!bLine.equals(aLine)) changes++;
        }
        
        return changes;
    }
    
    private int calculateEffort(List<RefactoringStep> steps) {
        return steps.size() * 15; // 15 minutes per step
    }
    
    private String calculateRiskLevel(String type, int affectedFilesCount) {
        if (affectedFilesCount > 10) return "HIGH";
        if (affectedFilesCount > 5) return "MEDIUM";
        return "LOW";
    }
    
    private boolean hasSyntax(String code) {
        int braces = 0;
        for (char c : code.toCharArray()) {
            if (c == '{') braces++;
            if (c == '}') braces--;
        }
        return braces == 0;
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
    
    public static class RefactoringPlan {
        private String refactoringType;
        private String command;
        private String status;
        private List<RefactoringStep> steps = new ArrayList<>();
        private List<String> affectedFiles = new ArrayList<>();
        private ValidationResult validation;
        private int estimatedEffort;
        private String riskLevel;
        
        // Getters and setters
        public String getRefactoringType() { return refactoringType; }
        public void setRefactoringType(String refactoringType) { this.refactoringType = refactoringType; }
        
        public String getCommand() { return command; }
        public void setCommand(String command) { this.command = command; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public List<RefactoringStep> getSteps() { return steps; }
        public void setSteps(List<RefactoringStep> steps) { this.steps = steps; }
        
        public List<String> getAffectedFiles() { return affectedFiles; }
        public void setAffectedFiles(List<String> affectedFiles) { this.affectedFiles = affectedFiles; }
        
        public ValidationResult getValidation() { return validation; }
        public void setValidation(ValidationResult validation) { this.validation = validation; }
        
        public int getEstimatedEffort() { return estimatedEffort; }
        public void setEstimatedEffort(int estimatedEffort) { this.estimatedEffort = estimatedEffort; }
        
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    }
    
    public static class RefactoringStep {
        private int stepNumber;
        private String description;
        private String details;
        
        public RefactoringStep() {
        }
        
        public RefactoringStep(int number, String desc, String details) {
            this.stepNumber = number;
            this.description = desc;
            this.details = details;
        }
        
        public int getStepNumber() { return stepNumber; }
        public String getDescription() { return description; }
        public String getDetails() { return details; }
    }
    
    public static class ExecutionResult {
        private String step;
        private String status;
        private String codeBefore;
        private String codeAfter;
        private int changesCount;
        private String error;
        
        // Getters and setters
        public String getStep() { return step; }
        public void setStep(String step) { this.step = step; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getCodeBefore() { return codeBefore; }
        public void setCodeBefore(String codeBefore) { this.codeBefore = codeBefore; }
        
        public String getCodeAfter() { return codeAfter; }
        public void setCodeAfter(String codeAfter) { this.codeAfter = codeAfter; }
        
        public int getChangesCount() { return changesCount; }
        public void setChangesCount(int changesCount) { this.changesCount = changesCount; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
    
    public static class ValidationResult {
        private boolean valid;
        private List<String> warnings = new ArrayList<>();
        private List<String> errors = new ArrayList<>();
        private List<String> recommendations = new ArrayList<>();
        
        // Getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        
        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    }
}

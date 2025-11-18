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
 * ✨ EDIT SUGGESTION GENERATOR
 * 
 * Generates intelligent edit suggestions based on selected code and user instructions.
 * Provides multiple suggestions with confidence scores and explanations.
 * 
 * ✅ PHASE 2: Intelligent Editing - Week 1
 */
@Service
@RequiredArgsConstructor
public class EditSuggestionGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(EditSuggestionGenerator.class);
    private final ObjectMapper objectMapper;
    private final ASTAnalysisService astAnalysisService;
    private final MLPatternDetectionService mlPatternDetectionService;
    private final CodeSelectionAnalyzer codeSelectionAnalyzer;
    
    /**
     * Generate edit suggestions based on user instruction
     */
    public List<EditSuggestion> generateSuggestions(String selectedCode, String instruction) {
        logger.info("✨ Generating suggestions for: {}", instruction);
        
        try {
            List<EditSuggestion> suggestions = new ArrayList<>();
            
            // Analyze the selected code
            CodeSelectionAnalyzer.SelectionAnalysis analysis = codeSelectionAnalyzer.analyzeSelection(selectedCode);
            
            // Generate suggestions based on instruction
            if (instruction.toLowerCase().contains("extract")) {
                suggestions.addAll(generateMethodExtractionSuggestions(selectedCode, analysis));
            }
            
            if (instruction.toLowerCase().contains("rename")) {
                suggestions.addAll(generateRenamingSuggestions(selectedCode, analysis));
            }
            
            if (instruction.toLowerCase().contains("simplify")) {
                suggestions.addAll(generateSimplificationSuggestions(selectedCode, analysis));
            }
            
            if (instruction.toLowerCase().contains("optimize")) {
                suggestions.addAll(generateOptimizationSuggestions(selectedCode, analysis));
            }
            
            if (instruction.toLowerCase().contains("pattern")) {
                suggestions.addAll(generatePatternSuggestions(selectedCode, analysis));
            }
            
            if (instruction.toLowerCase().contains("fix") || instruction.toLowerCase().contains("bug")) {
                suggestions.addAll(generateBugFixSuggestions(selectedCode, analysis));
            }
            
            // If no specific instruction matched, generate general suggestions
            if (suggestions.isEmpty()) {
                suggestions.addAll(generateGeneralSuggestions(selectedCode, analysis));
            }
            
            // Sort by confidence
            suggestions.sort((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()));
            
            logger.info("✅ Generated {} suggestions", suggestions.size());
            return suggestions;
            
        } catch (Exception e) {
            logger.error("❌ Suggestion generation failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Generate method extraction suggestions
     */
    private List<EditSuggestion> generateMethodExtractionSuggestions(String code, CodeSelectionAnalyzer.SelectionAnalysis analysis) {
        logger.info("✨ Generating method extraction suggestions");
        
        List<EditSuggestion> suggestions = new ArrayList<>();
        
        if (analysis.getLineCount() > 10) {
            String methodName = generateMethodName(code);
            String extractedMethod = generateExtractedMethod(code, methodName);
            String refactoredCode = generateRefactoredCodeWithMethodCall(code, methodName);
            
            EditSuggestion suggestion = new EditSuggestion();
            suggestion.setType("Extract Method");
            suggestion.setDescription("Extract this code block into a separate method");
            suggestion.setOriginalCode(code);
            suggestion.setSuggestedCode(refactoredCode);
            suggestion.setAdditionalCode(extractedMethod);
            suggestion.setConfidence(0.95);
            suggestion.setBenefit("Improves code reusability and readability");
            suggestion.setComplexity("Medium");
            suggestion.setImpact("Affects method structure");
            
            suggestions.add(suggestion);
        }
        
        return suggestions;
    }
    
    /**
     * Generate renaming suggestions
     */
    private List<EditSuggestion> generateRenamingSuggestions(String code, CodeSelectionAnalyzer.SelectionAnalysis analysis) {
        logger.info("✨ Generating renaming suggestions");
        
        List<EditSuggestion> suggestions = new ArrayList<>();
        
        for (String variable : analysis.getVariables()) {
            if (isPoorlyNamed(variable)) {
                String newName = generateBetterName(variable, code);
                String refactoredCode = code.replaceAll("\\b" + variable + "\\b", newName);
                
                EditSuggestion suggestion = new EditSuggestion();
                suggestion.setType("Rename Variable");
                suggestion.setDescription("Rename '" + variable + "' to '" + newName + "' for clarity");
                suggestion.setOriginalCode(code);
                suggestion.setSuggestedCode(refactoredCode);
                suggestion.setConfidence(0.85);
                suggestion.setBenefit("Improves code clarity and maintainability");
                suggestion.setComplexity("Low");
                suggestion.setImpact("Affects variable usage");
                
                suggestions.add(suggestion);
            }
        }
        
        return suggestions;
    }
    
    /**
     * Generate simplification suggestions
     */
    private List<EditSuggestion> generateSimplificationSuggestions(String code, CodeSelectionAnalyzer.SelectionAnalysis analysis) {
        logger.info("✨ Generating simplification suggestions");
        
        List<EditSuggestion> suggestions = new ArrayList<>();
        
        if (analysis.getComplexity() > 10) {
            String simplified = simplifyComplexCode(code);
            
            EditSuggestion suggestion = new EditSuggestion();
            suggestion.setType("Simplify Code");
            suggestion.setDescription("Simplify complex logic for better readability");
            suggestion.setOriginalCode(code);
            suggestion.setSuggestedCode(simplified);
            suggestion.setConfidence(0.8);
            suggestion.setBenefit("Reduces complexity and improves maintainability");
            suggestion.setComplexity("Medium");
            suggestion.setImpact("Changes code logic");
            
            suggestions.add(suggestion);
        }
        
        return suggestions;
    }
    
    /**
     * Generate optimization suggestions
     */
    private List<EditSuggestion> generateOptimizationSuggestions(String code, CodeSelectionAnalyzer.SelectionAnalysis analysis) {
        logger.info("✨ Generating optimization suggestions");
        
        List<EditSuggestion> suggestions = new ArrayList<>();
        
        // Loop optimization
        if (code.contains("for") || code.contains("while")) {
            String optimized = optimizeLoop(code);
            
            EditSuggestion suggestion = new EditSuggestion();
            suggestion.setType("Optimize Loop");
            suggestion.setDescription("Optimize loop for better performance");
            suggestion.setOriginalCode(code);
            suggestion.setSuggestedCode(optimized);
            suggestion.setConfidence(0.75);
            suggestion.setBenefit("Improves performance");
            suggestion.setComplexity("Low");
            suggestion.setImpact("Performance improvement");
            
            suggestions.add(suggestion);
        }
        
        // Collection optimization
        if (code.contains("ArrayList") || code.contains("HashMap")) {
            String optimized = optimizeCollection(code);
            
            EditSuggestion suggestion = new EditSuggestion();
            suggestion.setType("Optimize Collection");
            suggestion.setDescription("Use more efficient collection");
            suggestion.setOriginalCode(code);
            suggestion.setSuggestedCode(optimized);
            suggestion.setConfidence(0.7);
            suggestion.setBenefit("Improves memory usage");
            suggestion.setComplexity("Low");
            suggestion.setImpact("Memory optimization");
            
            suggestions.add(suggestion);
        }
        
        return suggestions;
    }
    
    /**
     * Generate pattern application suggestions
     */
    private List<EditSuggestion> generatePatternSuggestions(String code, CodeSelectionAnalyzer.SelectionAnalysis analysis) {
        logger.info("✨ Generating pattern suggestions");
        
        List<EditSuggestion> suggestions = new ArrayList<>();
        
        // Strategy pattern
        if (code.contains("if") && code.contains("else if")) {
            String withPattern = applyStrategyPattern(code);
            
            EditSuggestion suggestion = new EditSuggestion();
            suggestion.setType("Apply Strategy Pattern");
            suggestion.setDescription("Replace if-else with strategy pattern");
            suggestion.setOriginalCode(code);
            suggestion.setSuggestedCode(withPattern);
            suggestion.setConfidence(0.8);
            suggestion.setBenefit("Improves extensibility and maintainability");
            suggestion.setComplexity("High");
            suggestion.setImpact("Structural change");
            
            suggestions.add(suggestion);
        }
        
        return suggestions;
    }
    
    /**
     * Generate bug fix suggestions
     */
    private List<EditSuggestion> generateBugFixSuggestions(String code, CodeSelectionAnalyzer.SelectionAnalysis analysis) {
        logger.info("✨ Generating bug fix suggestions");
        
        List<EditSuggestion> suggestions = new ArrayList<>();
        
        for (CodeSelectionAnalyzer.IssueInfo issue : analysis.getIssues()) {
            String fixed = fixIssue(code, issue);
            
            EditSuggestion suggestion = new EditSuggestion();
            suggestion.setType("Fix Bug");
            suggestion.setDescription("Fix: " + issue.getDescription());
            suggestion.setOriginalCode(code);
            suggestion.setSuggestedCode(fixed);
            suggestion.setConfidence(0.9);
            suggestion.setBenefit("Prevents runtime errors");
            suggestion.setComplexity("Low");
            suggestion.setImpact("Bug fix");
            
            suggestions.add(suggestion);
        }
        
        return suggestions;
    }
    
    /**
     * Generate general suggestions
     */
    private List<EditSuggestion> generateGeneralSuggestions(String code, CodeSelectionAnalyzer.SelectionAnalysis analysis) {
        logger.info("✨ Generating general suggestions");
        
        List<EditSuggestion> suggestions = new ArrayList<>();
        
        // Based on code smells
        for (CodeSelectionAnalyzer.CodeSmellInfo smell : analysis.getCodeSmells()) {
            EditSuggestion suggestion = new EditSuggestion();
            suggestion.setType("Address Code Smell");
            suggestion.setDescription("Address: " + smell.getDescription());
            suggestion.setOriginalCode(code);
            suggestion.setConfidence(0.7);
            suggestion.setBenefit("Improves code quality");
            suggestion.setComplexity("Medium");
            suggestion.setImpact("Code quality improvement");
            
            suggestions.add(suggestion);
        }
        
        // Based on refactoring opportunities
        for (CodeSelectionAnalyzer.RefactoringOpportunity opp : analysis.getRefactoringOpportunities()) {
            EditSuggestion suggestion = new EditSuggestion();
            suggestion.setType(opp.getType());
            suggestion.setDescription(opp.getDescription());
            suggestion.setOriginalCode(code);
            suggestion.setConfidence(0.75);
            suggestion.setBenefit(opp.getBenefit());
            suggestion.setComplexity("Medium");
            suggestion.setImpact(opp.getPriority());
            
            suggestions.add(suggestion);
        }
        
        return suggestions;
    }
    
    // Helper methods
    
    private String generateMethodName(String code) {
        if (code.contains("calculate")) return "calculateResult";
        if (code.contains("validate")) return "validateInput";
        if (code.contains("process")) return "processData";
        if (code.contains("format")) return "formatOutput";
        return "extractedMethod";
    }
    
    private String generateExtractedMethod(String code, String methodName) {
        return "private void " + methodName + "() {\n" + code + "\n}";
    }
    
    private String generateRefactoredCodeWithMethodCall(String code, String methodName) {
        return methodName + "();";
    }
    
    private boolean isPoorlyNamed(String name) {
        return name.length() <= 1 || name.equals("temp") || name.equals("data") || name.equals("value");
    }
    
    private String generateBetterName(String oldName, String context) {
        if (oldName.equals("i")) return "index";
        if (oldName.equals("x")) return "value";
        if (oldName.equals("temp")) return "temporary";
        if (oldName.equals("data")) return "processedData";
        return oldName + "Value";
    }
    
    private String simplifyComplexCode(String code) {
        String simplified = code;
        simplified = simplified.replaceAll("if\\s*\\(.*?\\)\\s*\\{\\s*return\\s+true;\\s*\\}\\s*else\\s*\\{\\s*return\\s+false;\\s*\\}", 
            "return condition;");
        return simplified;
    }
    
    private String optimizeLoop(String code) {
        return code.replaceAll("for\\s*\\(int\\s+i\\s*=\\s*0;\\s*i\\s*<\\s*list\\.size\\(\\);\\s*i\\+\\+\\)", 
            "for (Object item : list)");
    }
    
    private String optimizeCollection(String code) {
        String optimized = code;
        optimized = optimized.replace("ArrayList", "List");
        optimized = optimized.replace("HashMap", "Map");
        return optimized;
    }
    
    private String applyStrategyPattern(String code) {
        return "// Apply Strategy Pattern\n" +
               "Strategy strategy = strategyFactory.getStrategy(type);\n" +
               "strategy.execute();";
    }
    
    private String fixIssue(String code, CodeSelectionAnalyzer.IssueInfo issue) {
        if (issue.getType().contains("NPE")) {
            return "if (object != null) {\n" + code + "\n}";
        }
        if (issue.getType().contains("Resource")) {
            return "try (Resource resource = new Resource()) {\n" + code + "\n}";
        }
        return code;
    }
    
    // Inner class
    
    public static class EditSuggestion {
        private String type;
        private String description;
        private String originalCode;
        private String suggestedCode;
        private String additionalCode;
        private double confidence;
        private String benefit;
        private String complexity;
        private String impact;
        
        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getOriginalCode() { return originalCode; }
        public void setOriginalCode(String originalCode) { this.originalCode = originalCode; }
        
        public String getSuggestedCode() { return suggestedCode; }
        public void setSuggestedCode(String suggestedCode) { this.suggestedCode = suggestedCode; }
        
        public String getAdditionalCode() { return additionalCode; }
        public void setAdditionalCode(String additionalCode) { this.additionalCode = additionalCode; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        
        public String getBenefit() { return benefit; }
        public void setBenefit(String benefit) { this.benefit = benefit; }
        
        public String getComplexity() { return complexity; }
        public void setComplexity(String complexity) { this.complexity = complexity; }
        
        public String getImpact() { return impact; }
        public void setImpact(String impact) { this.impact = impact; }
    }
}

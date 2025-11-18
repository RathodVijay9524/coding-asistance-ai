package com.vijay.controller;

import com.vijay.editing.CodeSelectionAnalyzer;
import com.vijay.editing.EditSuggestionGenerator;
import com.vijay.editing.LiveCodeEditor;
import com.vijay.editing.InlineSuggestionEngine;
import com.vijay.editing.SmartCompletionEngine;
import com.vijay.editing.RefactoringAssistant;
import com.vijay.editing.CodeTransformationEngine;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 🎯 INTELLIGENT EDITOR CONTROLLER
 * 
 * REST endpoints for Phase 2 intelligent editing features.
 * Exposes code analysis, suggestion generation, and edit application.
 * 
 * ✅ PHASE 2: Intelligent Editing - Week 1
 */
@RestController
@RequestMapping("/api/editor")
@RequiredArgsConstructor
public class IntelligentEditorController {
    
    private static final Logger logger = LoggerFactory.getLogger(IntelligentEditorController.class);
    private final CodeSelectionAnalyzer codeSelectionAnalyzer;
    private final EditSuggestionGenerator editSuggestionGenerator;
    private final LiveCodeEditor liveCodeEditor;
    private final InlineSuggestionEngine inlineSuggestionEngine;
    private final SmartCompletionEngine smartCompletionEngine;
    private final RefactoringAssistant refactoringAssistant;
    private final CodeTransformationEngine codeTransformationEngine;
    
    /**
     * Analyze selected code
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeCode(@RequestBody CodeAnalysisRequest request) {
        logger.info("📊 Analyzing code selection");
        
        try {
            CodeSelectionAnalyzer.SelectionAnalysis analysis = 
                codeSelectionAnalyzer.analyzeSelection(request.getSelectedCode());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("analysis", analysis);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Analysis failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Get edit suggestions
     */
    @PostMapping("/suggest-edits")
    public ResponseEntity<?> suggestEdits(@RequestBody EditSuggestionRequest request) {
        logger.info("✨ Generating edit suggestions");
        
        try {
            var suggestions = editSuggestionGenerator.generateSuggestions(
                request.getSelectedCode(),
                request.getInstruction()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("suggestions", suggestions);
            response.put("count", suggestions.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Suggestion generation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Preview edit
     */
    @PostMapping("/preview-edit")
    public ResponseEntity<?> previewEdit(@RequestBody EditPreviewRequest request) {
        logger.info("👁️ Previewing edit");
        
        try {
            String result = liveCodeEditor.previewEdit(
                request.getOriginalCode(),
                request.getSuggestedCode(),
                request.getEditType()
            );
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "preview", result
            ));
            
        } catch (Exception e) {
            logger.error("❌ Preview failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Apply edit
     */
    @PostMapping("/apply-edit")
    public ResponseEntity<?> applyEdit(@RequestBody EditApplyRequest request) {
        logger.info("✅ Applying edit");
        
        try {
            String result = liveCodeEditor.applyEdit(
                request.getOriginalCode(),
                request.getSuggestedCode(),
                request.getEditType()
            );
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "result", result
            ));
            
        } catch (Exception e) {
            logger.error("❌ Edit application failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Validate edit
     */
    @PostMapping("/validate-edit")
    public ResponseEntity<?> validateEdit(@RequestBody EditValidationRequest request) {
        logger.info("🔍 Validating edit");
        
        try {
            LiveCodeEditor.ValidationResult validation = liveCodeEditor.validateEdit(
                request.getOriginalCode(),
                request.getSuggestedCode()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("valid", validation.isValid());
            response.put("errors", validation.getErrors());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Get inline suggestions
     */
    @PostMapping("/inline-suggestions")
    public ResponseEntity<?> getInlineSuggestions(@RequestBody InlineSuggestionsRequest request) {
        logger.info("💡 Getting inline suggestions");
        
        try {
            String result = inlineSuggestionEngine.getSuggestionsAtPosition(
                request.getFullCode(),
                request.getCursorLine(),
                request.getCursorColumn()
            );
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "suggestions", result
            ));
            
        } catch (Exception e) {
            logger.error("❌ Inline suggestions failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Get code completions
     */
    @PostMapping("/completions")
    public ResponseEntity<?> getCompletions(@RequestBody CompletionsRequest request) {
        logger.info("🎯 Getting completions");
        
        try {
            String result = smartCompletionEngine.getCompletions(
                request.getFullCode(),
                request.getCursorLine(),
                request.getCursorColumn(),
                request.getPartialInput()
            );
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "completions", result
            ));
            
        } catch (Exception e) {
            logger.error("❌ Completions failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Get context-aware completions
     */
    @PostMapping("/context-completions")
    public ResponseEntity<?> getContextCompletions(@RequestBody ContextCompletionsRequest request) {
        logger.info("🎯 Getting context-aware completions");
        
        try {
            String result = smartCompletionEngine.getContextAwareCompletions(
                request.getContext(),
                request.getPartialInput()
            );
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "completions", result
            ));
            
        } catch (Exception e) {
            logger.error("❌ Context completions failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "IntelligentEditor",
            "phase", "Phase 2 - Week 1 & 2",
            "features", List.of(
                "Code Analysis",
                "Edit Suggestions",
                "Inline Suggestions",
                "Smart Completions",
                "Edit Validation",
                "Preview & Apply"
            )
        ));
    }
    
    // Request DTOs
    
    public static class CodeAnalysisRequest {
        private String selectedCode;
        
        public String getSelectedCode() { return selectedCode; }
        public void setSelectedCode(String selectedCode) { this.selectedCode = selectedCode; }
    }
    
    public static class EditSuggestionRequest {
        private String selectedCode;
        private String instruction;
        
        public String getSelectedCode() { return selectedCode; }
        public void setSelectedCode(String selectedCode) { this.selectedCode = selectedCode; }
        
        public String getInstruction() { return instruction; }
        public void setInstruction(String instruction) { this.instruction = instruction; }
    }
    
    public static class EditPreviewRequest {
        private String originalCode;
        private String suggestedCode;
        private String editType;
        
        public String getOriginalCode() { return originalCode; }
        public void setOriginalCode(String originalCode) { this.originalCode = originalCode; }
        
        public String getSuggestedCode() { return suggestedCode; }
        public void setSuggestedCode(String suggestedCode) { this.suggestedCode = suggestedCode; }
        
        public String getEditType() { return editType; }
        public void setEditType(String editType) { this.editType = editType; }
    }
    
    public static class EditApplyRequest {
        private String originalCode;
        private String suggestedCode;
        private String editType;
        
        public String getOriginalCode() { return originalCode; }
        public void setOriginalCode(String originalCode) { this.originalCode = originalCode; }
        
        public String getSuggestedCode() { return suggestedCode; }
        public void setSuggestedCode(String suggestedCode) { this.suggestedCode = suggestedCode; }
        
        public String getEditType() { return editType; }
        public void setEditType(String editType) { this.editType = editType; }
    }
    
    public static class EditValidationRequest {
        private String originalCode;
        private String suggestedCode;
        
        public String getOriginalCode() { return originalCode; }
        public void setOriginalCode(String originalCode) { this.originalCode = originalCode; }
        
        public String getSuggestedCode() { return suggestedCode; }
        public void setSuggestedCode(String suggestedCode) { this.suggestedCode = suggestedCode; }
    }
    
    public static class InlineSuggestionsRequest {
        private String fullCode;
        private int cursorLine;
        private int cursorColumn;
        
        public String getFullCode() { return fullCode; }
        public void setFullCode(String fullCode) { this.fullCode = fullCode; }
        
        public int getCursorLine() { return cursorLine; }
        public void setCursorLine(int cursorLine) { this.cursorLine = cursorLine; }
        
        public int getCursorColumn() { return cursorColumn; }
        public void setCursorColumn(int cursorColumn) { this.cursorColumn = cursorColumn; }
    }
    
    public static class CompletionsRequest {
        private String fullCode;
        private int cursorLine;
        private int cursorColumn;
        private String partialInput;
        
        public String getFullCode() { return fullCode; }
        public void setFullCode(String fullCode) { this.fullCode = fullCode; }
        
        public int getCursorLine() { return cursorLine; }
        public void setCursorLine(int cursorLine) { this.cursorLine = cursorLine; }
        
        public int getCursorColumn() { return cursorColumn; }
        public void setCursorColumn(int cursorColumn) { this.cursorColumn = cursorColumn; }
        
        public String getPartialInput() { return partialInput; }
        public void setPartialInput(String partialInput) { this.partialInput = partialInput; }
    }
    
    public static class ContextCompletionsRequest {
        private String context;
        private String partialInput;
        
        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
        
        public String getPartialInput() { return partialInput; }
        public void setPartialInput(String partialInput) { this.partialInput = partialInput; }
    }
}

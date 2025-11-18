package com.vijay.editing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 🎯 Code Edit Controller - Phase 2 API endpoints
 * 
 * Provides REST endpoints for live code editing:
 * - POST /api/edit/inline - Apply inline edit
 * - POST /api/edit/alternatives - Get alternative edits
 * - POST /api/edit/validate - Validate edit
 * - POST /api/edit/apply - Apply edit to file
 */
@RestController
@RequestMapping("/api/edit")
public class CodeEditController {
    
    private static final Logger logger = LoggerFactory.getLogger(CodeEditController.class);
    
    private final InlineCodeEditor inlineCodeEditor;
    
    public CodeEditController(InlineCodeEditor inlineCodeEditor) {
        this.inlineCodeEditor = inlineCodeEditor;
    }
    
    /**
     * Apply inline edit to selected code
     * 
     * Example:
     * POST /api/edit/inline
     * {
     *   "selection": {
     *     "filePath": "UserService.java",
     *     "startLine": 10,
     *     "endLine": 25,
     *     "code": "public void processUser(User user) { ... }",
     *     "language": "java",
     *     "fileName": "UserService.java"
     *   },
     *   "instruction": "Extract this method into a separate private method called validateUser",
     *   "intent": "REFACTOR"
     * }
     */
    @PostMapping("/inline")
    public ResponseEntity<?> applyInlineEdit(@RequestBody CodeEditRequest request) {
        try {
            logger.info("🎯 Inline edit request: {}", request.getInstruction());
            
            CodeEditResult result = inlineCodeEditor.applyInlineEdit(request);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "original", result.getOriginalCode(),
                "suggested", result.getSuggestedEdit(),
                "confidence", result.getConfidence(),
                "explanation", result.getExplanation(),
                "editType", result.getEditType(),
                "linesChanged", result.getLinesChanged(),
                "breakingChange", result.isBreakingChange()
            ));
        } catch (Exception e) {
            logger.error("❌ Error applying inline edit: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Get alternative edit suggestions
     * 
     * Example:
     * POST /api/edit/alternatives?count=3
     */
    @PostMapping("/alternatives")
    public ResponseEntity<?> getAlternativeEdits(
            @RequestBody CodeEditRequest request,
            @RequestParam(defaultValue = "3") int count) {
        try {
            logger.info("🔄 Generating {} alternative edits", count);
            
            List<CodeEditResult> alternatives = inlineCodeEditor.getAlternativeEdits(request, count);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "count", alternatives.size(),
                "alternatives", alternatives
            ));
        } catch (Exception e) {
            logger.error("❌ Error generating alternatives: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Validate code edit before applying
     * 
     * Example:
     * POST /api/edit/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateEdit(@RequestBody CodeEditResult result) {
        try {
            logger.info("🔍 Validating code edit");
            
            boolean isValid = inlineCodeEditor.validateEdit(result);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "valid", isValid,
                "confidence", result.getConfidence(),
                "breakingChange", result.isBreakingChange()
            ));
        } catch (Exception e) {
            logger.error("❌ Error validating edit: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Apply validated edit to file
     * 
     * Example:
     * POST /api/edit/apply
     * {
     *   "filePath": "/path/to/UserService.java",
     *   "result": { ... }
     * }
     */
    @PostMapping("/apply")
    public ResponseEntity<?> applyEditToFile(
            @RequestParam String filePath,
            @RequestBody CodeEditResult result) {
        try {
            logger.info("💾 Applying edit to file: {}", filePath);
            
            inlineCodeEditor.applyEditToFile(result, filePath);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Edit applied successfully",
                "filePath", filePath,
                "linesChanged", result.getLinesChanged()
            ));
        } catch (Exception e) {
            logger.error("❌ Error applying edit to file: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "component", "InlineCodeEditor",
            "phase", "Phase 2 - Live Code Editing"
        ));
    }
}

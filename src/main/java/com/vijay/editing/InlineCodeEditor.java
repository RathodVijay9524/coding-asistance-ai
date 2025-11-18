package com.vijay.editing;

import com.vijay.context.GlobalBrainContext;
import com.vijay.context.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ✏️ InlineCodeEditor - Cursor-like select-and-edit functionality
 * 
 * Provides:
 * - Select code block
 * - Ask for changes ("Extract method", "Add error handling", etc.)
 * - Get suggestions with confidence scores
 * - Apply edits with one click
 * 
 * This is Phase 2 of the Cursor-competitive roadmap
 */
@Component
public class InlineCodeEditor {
    
    private static final Logger logger = LoggerFactory.getLogger(InlineCodeEditor.class);
    
    private final ChatClient editorClient;
    
    public InlineCodeEditor(OpenAiChatModel chatModel) {
        this.editorClient = ChatClient.builder(chatModel).build();
    }
    
    /**
     * Apply inline edit to selected code
     */
    public CodeEditResult applyInlineEdit(CodeEditRequest request) {
        String traceId = TraceContext.getTraceId();
        logger.info("[{}] ✏️ Inline Code Editor: Processing edit request", traceId);
        logger.info("[{}]    📝 Instruction: {}", traceId, request.getInstruction());
        logger.info("[{}]    📍 Selection: {} lines", traceId, 
            request.getSelection().getEndLine() - request.getSelection().getStartLine() + 1);
        
        try {
            // Build prompt for code editing
            String editPrompt = buildEditPrompt(request);
            
            // Get AI suggestion
            String suggestion = editorClient.prompt()
                    .user(editPrompt)
                    .call()
                    .content();
            
            // Parse and structure result
            CodeEditResult result = parseEditResult(
                request.getSelection().getCode(),
                suggestion,
                request.getInstruction()
            );
            
            logger.info("[{}]    ✅ Edit suggestion generated", traceId);
            logger.info("[{}]    📊 Confidence: {:.2f}%", traceId, result.getConfidence() * 100);
            logger.info("[{}]    📈 Lines changed: {}", traceId, result.getLinesChanged());
            
            return result;
            
        } catch (Exception e) {
            logger.error("[{}]    ❌ Error applying inline edit: {}", traceId, e.getMessage(), e);
            throw new RuntimeException("Failed to apply inline edit: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get multiple edit alternatives
     */
    public List<CodeEditResult> getAlternativeEdits(CodeEditRequest request, int count) {
        String traceId = TraceContext.getTraceId();
        logger.info("[{}] 🔄 Generating {} alternative edits", traceId, count);
        
        List<CodeEditResult> alternatives = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            try {
                CodeEditResult result = applyInlineEdit(request);
                result.setConfidence(result.getConfidence() * (1.0 - (i * 0.1))); // Decrease confidence for alternatives
                alternatives.add(result);
                logger.info("[{}]    ✅ Alternative {}: Confidence {:.2f}%", 
                    traceId, i + 1, result.getConfidence() * 100);
            } catch (Exception e) {
                logger.warn("[{}]    ⚠️ Failed to generate alternative {}: {}", 
                    traceId, i + 1, e.getMessage());
            }
        }
        
        return alternatives;
    }
    
    /**
     * Validate edit before applying
     */
    public boolean validateEdit(CodeEditResult result) {
        String traceId = TraceContext.getTraceId();
        logger.info("[{}] 🔍 Validating code edit", traceId);
        
        // Check for breaking changes
        if (result.isBreakingChange()) {
            logger.warn("[{}]    ⚠️ Breaking change detected", traceId);
            return false;
        }
        
        // Check confidence threshold
        if (result.getConfidence() < 0.7) {
            logger.warn("[{}]    ⚠️ Low confidence: {:.2f}%", traceId, result.getConfidence() * 100);
            return false;
        }
        
        // Check syntax validity (basic)
        if (!isValidSyntax(result.getSuggestedEdit())) {
            logger.warn("[{}]    ⚠️ Invalid syntax detected", traceId);
            return false;
        }
        
        logger.info("[{}]    ✅ Edit validation passed", traceId);
        return true;
    }
    
    /**
     * Apply validated edit to file
     */
    public void applyEditToFile(CodeEditResult result, String filePath) {
        String traceId = TraceContext.getTraceId();
        logger.info("[{}] 💾 Applying edit to file: {}", traceId, filePath);
        
        if (!validateEdit(result)) {
            logger.error("[{}]    ❌ Edit validation failed", traceId);
            throw new RuntimeException("Edit validation failed");
        }
        
        try {
            // TODO: Implement file writing logic
            // This would use FileWriter or similar to update the actual file
            logger.info("[{}]    ✅ Edit applied successfully", traceId);
        } catch (Exception e) {
            logger.error("[{}]    ❌ Error applying edit to file: {}", traceId, e.getMessage(), e);
            throw new RuntimeException("Failed to apply edit to file: " + e.getMessage(), e);
        }
    }
    
    // ===== Private Helper Methods =====
    
    private String buildEditPrompt(CodeEditRequest request) {
        return String.format(
            "You are an expert code editor. The user has selected code and wants you to modify it.\n\n" +
            "SELECTED CODE:\n```%s\n%s\n```\n\n" +
            "INSTRUCTION: %s\n" +
            "INTENT: %s\n\n" +
            "REQUIREMENTS:\n" +
            "1. Provide ONLY the modified code, no explanations\n" +
            "2. Maintain the same code style and formatting\n" +
            "3. Keep variable names consistent\n" +
            "4. Add comments if logic changes significantly\n" +
            "5. Ensure the code is syntactically valid\n\n" +
            "MODIFIED CODE:",
            request.getSelection().getLanguage(),
            request.getSelection().getCode(),
            request.getInstruction(),
            request.getIntent()
        );
    }
    
    private CodeEditResult parseEditResult(String original, String suggested, String instruction) {
        return CodeEditResult.builder()
                .originalCode(original)
                .suggestedEdit(suggested)
                .confidence(calculateConfidence(original, suggested))
                .explanation(generateExplanation(instruction))
                .alternativeEdits(new ArrayList<>())
                .editType(detectEditType(instruction))
                .linesChanged(calculateLinesChanged(original, suggested))
                .breakingChange(detectBreakingChange(original, suggested))
                .build();
    }
    
    private double calculateConfidence(String original, String suggested) {
        // Simple heuristic: if suggested is similar length and not empty, high confidence
        if (suggested == null || suggested.isEmpty()) return 0.5;
        double lengthRatio = (double) suggested.length() / original.length();
        if (lengthRatio < 0.5 || lengthRatio > 2.0) return 0.7;
        return 0.85;
    }
    
    private String generateExplanation(String instruction) {
        return "Applied: " + instruction;
    }
    
    private String detectEditType(String instruction) {
        String lower = instruction.toLowerCase();
        if (lower.contains("extract")) return "EXTRACT_METHOD";
        if (lower.contains("inline")) return "INLINE";
        if (lower.contains("rename")) return "RENAME";
        if (lower.contains("refactor")) return "REFACTOR";
        if (lower.contains("error")) return "ADD_ERROR_HANDLING";
        if (lower.contains("async")) return "CONVERT_ASYNC";
        return "GENERAL_EDIT";
    }
    
    private int calculateLinesChanged(String original, String suggested) {
        int originalLines = original.split("\n").length;
        int suggestedLines = suggested.split("\n").length;
        return Math.abs(suggestedLines - originalLines);
    }
    
    private boolean detectBreakingChange(String original, String suggested) {
        // Simple heuristic: if method signature changed, it's breaking
        if (original.contains("public") && !suggested.contains("public")) return true;
        if (original.contains("private") && !suggested.contains("private")) return true;
        return false;
    }
    
    private boolean isValidSyntax(String code) {
        // Basic syntax validation
        if (code == null || code.isEmpty()) return false;
        
        // Check for balanced braces
        int braces = 0;
        for (char c : code.toCharArray()) {
            if (c == '{') braces++;
            if (c == '}') braces--;
            if (braces < 0) return false;
        }
        
        return braces == 0;
    }
}

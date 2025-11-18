package com.vijay.editing;

import lombok.Builder;
import lombok.Data;

/**
 * 📝 CodeEditRequest - User's edit instruction
 * 
 * Represents a user's request to edit selected code
 * Example: "Extract this method" or "Add error handling"
 */
@Data
@Builder
public class CodeEditRequest {
    private CodeSelection selection;
    private String instruction;
    private String context;
    private String intent;
    private double confidence;
    
    /**
     * Get full request context for LLM
     */
    public String getFullContext() {
        return String.format(
            "SELECTED CODE:\n%s\n\n" +
            "INSTRUCTION: %s\n" +
            "INTENT: %s\n" +
            "CONTEXT: %s",
            selection.getCode(),
            instruction,
            intent,
            context
        );
    }
}

package com.vijay.editing;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * ✨ CodeEditResult - AI-generated code edit suggestions
 * 
 * Contains original code, suggested edits, and alternatives
 */
@Data
@Builder
public class CodeEditResult {
    private String originalCode;
    private String suggestedEdit;
    private double confidence;
    private String explanation;
    private List<String> alternativeEdits;
    private String editType;
    private int linesChanged;
    private boolean breakingChange;
    
    /**
     * Get formatted result for display
     */
    public String getFormattedResult() {
        return String.format(
            "ORIGINAL:\n%s\n\n" +
            "SUGGESTED:\n%s\n\n" +
            "CONFIDENCE: %.2f%%\n" +
            "EXPLANATION: %s\n" +
            "LINES CHANGED: %d\n" +
            "BREAKING CHANGE: %s",
            originalCode,
            suggestedEdit,
            confidence * 100,
            explanation,
            linesChanged,
            breakingChange ? "⚠️ YES" : "✅ NO"
        );
    }
}

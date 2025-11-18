package com.vijay.editing;

import lombok.Builder;
import lombok.Data;

/**
 * 📍 CodeSelection - User-selected code block
 * 
 * Represents a code snippet selected by the user for editing
 */
@Data
@Builder
public class CodeSelection {
    private String filePath;
    private int startLine;
    private int endLine;
    private String code;
    private String language;
    private String fileName;
    
    /**
     * Get selection context (lines before and after)
     */
    public String getContext(int contextLines) {
        return String.format(
            "File: %s (lines %d-%d)\nLanguage: %s\n\n%s",
            fileName, startLine, endLine, language, code
        );
    }
}

package com.vijay.codebase;

import lombok.Data;

/**
 * 📄 CodeFile - Individual file metadata
 */
@Data
public class CodeFile {
    private String path;
    private String name;
    private String extension;
    private long size;
    private long lineCount;
    private double complexity;
    private boolean hasTests;
    
    /**
     * Check if this file has corresponding tests
     */
    public boolean hasTests() {
        return this.hasTests;
    }
}

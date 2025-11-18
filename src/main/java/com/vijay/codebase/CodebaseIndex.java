package com.vijay.codebase;

import lombok.Data;
import java.util.List;

/**
 * 📋 CodebaseIndex - Complete file listing and metadata
 */
@Data
public class CodebaseIndex {
    private String projectRoot;
    private List<CodeFile> files;
    private int totalFiles;
    private long totalLines;
    private double averageComplexity;
}

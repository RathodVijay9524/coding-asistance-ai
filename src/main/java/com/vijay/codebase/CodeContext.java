package com.vijay.codebase;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 📊 CodeContext - Comprehensive codebase understanding
 * 
 * Contains all information about a project:
 * - File structure and organization
 * - Dependency relationships
 * - Architecture patterns
 * - Code hotspots and complexity areas
 */
@Data
@Builder
public class CodeContext {
    
    private String projectPath;
    private CodebaseIndex fileStructure;
    private DependencyGraph dependencies;
    private ArchitecturePattern architecture;
    private List<CodeHotspot> hotspots;
    private long analyzedAt;
    
    /**
     * Get summary of codebase
     */
    public String getSummary() {
        return String.format(
                "Project: %s\n" +
                "Files: %d\n" +
                "Architecture: %s\n" +
                "Hotspots: %d\n" +
                "Analyzed: %d ms ago",
                projectPath,
                fileStructure.getTotalFiles(),
                architecture.getPatternType(),
                hotspots.size(),
                System.currentTimeMillis() - analyzedAt
        );
    }
}

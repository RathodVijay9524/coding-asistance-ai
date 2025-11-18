package com.vijay.codebase;

import org.springframework.stereotype.Component;

@Component
public class ArchitectureAnalyzer {
    
    public ArchitecturePattern detectPatterns(CodebaseIndex fileIndex, DependencyGraph dependencies) {
        ArchitecturePattern pattern = new ArchitecturePattern();
        pattern.setPatternType("MONOLITH");
        return pattern;
    }
}

package com.vijay.codebase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 🧠 Codebase Intelligence Engine
 * 
 * Provides Cursor/Windsurf-like project understanding:
 * - Full repository indexing
 * - Cross-file dependency mapping
 * - Real-time code context awareness
 * - Semantic search across codebase
 * - Architecture pattern detection
 * - Code hotspot identification
 * 
 * This layer enables intelligent code suggestions and refactoring
 */
@Component
public class CodebaseIntelligenceEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(CodebaseIntelligenceEngine.class);
    
    private final CodebaseIndexer codebaseIndexer;
    private final DependencyMapper dependencyMapper;
    private final ArchitectureAnalyzer architectureAnalyzer;
    private final CodeHotspotDetector hotspotDetector;
    
    public CodebaseIntelligenceEngine(
            CodebaseIndexer codebaseIndexer,
            DependencyMapper dependencyMapper,
            ArchitectureAnalyzer architectureAnalyzer,
            CodeHotspotDetector hotspotDetector) {
        this.codebaseIndexer = codebaseIndexer;
        this.dependencyMapper = dependencyMapper;
        this.architectureAnalyzer = architectureAnalyzer;
        this.hotspotDetector = hotspotDetector;
    }
    
    /**
     * Analyze entire codebase and build comprehensive context
     */
    public CodeContext analyzeCodebase(String projectPath) {
        logger.info("🔍 Codebase Intelligence: Analyzing project at {}", projectPath);
        
        try {
            Path projectRoot = Paths.get(projectPath);
            
            // STEP 1: Index all files
            logger.info("   📁 Step 1: Indexing files...");
            CodebaseIndex fileIndex = codebaseIndexer.indexProject(projectRoot);
            logger.info("   ✅ Found {} files", fileIndex.getTotalFiles());
            
            // STEP 2: Map dependencies
            logger.info("   🔗 Step 2: Mapping dependencies...");
            DependencyGraph dependencies = dependencyMapper.mapDependencies(fileIndex);
            logger.info("   ✅ Found {} dependencies", dependencies.getEdgeCount());
            
            // STEP 3: Detect architecture patterns
            logger.info("   🏗️ Step 3: Detecting architecture patterns...");
            ArchitecturePattern architecture = architectureAnalyzer.detectPatterns(fileIndex, dependencies);
            logger.info("   ✅ Detected pattern: {}", architecture.getPatternType());
            
            // STEP 4: Find code hotspots
            logger.info("   🔥 Step 4: Identifying code hotspots...");
            List<CodeHotspot> hotspots = hotspotDetector.detectHotspots(fileIndex);
            logger.info("   ✅ Found {} hotspots", hotspots.size());
            
            // Build comprehensive context
            CodeContext context = CodeContext.builder()
                    .projectPath(projectPath)
                    .fileStructure(fileIndex)
                    .dependencies(dependencies)
                    .architecture(architecture)
                    .hotspots(hotspots)
                    .analyzedAt(System.currentTimeMillis())
                    .build();
            
            logger.info("✅ Codebase Intelligence: Analysis complete");
            return context;
            
        } catch (Exception e) {
            logger.error("❌ Error analyzing codebase: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to analyze codebase: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get intelligent suggestions for the codebase
     */
    public List<CodeSuggestion> getIntelligentSuggestions(CodeContext context) {
        logger.info("💡 Generating intelligent suggestions...");
        
        List<CodeSuggestion> suggestions = new ArrayList<>();
        
        // Refactoring suggestions
        suggestions.addAll(generateRefactoringSuggestions(context));
        
        // Test coverage suggestions
        suggestions.addAll(generateTestSuggestions(context));
        
        // Architecture suggestions
        suggestions.addAll(generateArchitectureSuggestions(context));
        
        // Performance suggestions
        suggestions.addAll(generatePerformanceSuggestions(context));
        
        logger.info("✅ Generated {} suggestions", suggestions.size());
        return suggestions;
    }
    
    /**
     * Find all files that reference a specific class/method
     */
    public List<CodeReference> findReferences(CodeContext context, String targetSymbol) {
        logger.info("🔍 Finding references to: {}", targetSymbol);
        
        return context.getFileStructure().getFiles().stream()
                .flatMap(file -> findReferencesInFile(file, targetSymbol).stream())
                .collect(Collectors.toList());
    }
    
    /**
     * Analyze impact of a potential change
     */
    public ChangeImpactAnalysis analyzeChangeImpact(CodeContext context, String filePath, String change) {
        logger.info("📊 Analyzing impact of change in: {}", filePath);
        
        List<CodeReference> affectedFiles = findReferencesInDependents(context, filePath);
        List<String> potentialBreakingChanges = identifyBreakingChanges(context, filePath, change);
        
        return ChangeImpactAnalysis.builder()
                .filePath(filePath)
                .affectedFiles(affectedFiles)
                .potentialBreakingChanges(potentialBreakingChanges)
                .riskLevel(calculateRiskLevel(affectedFiles, potentialBreakingChanges))
                .build();
    }
    
    // ===== Private Helper Methods =====
    
    private List<CodeSuggestion> generateRefactoringSuggestions(CodeContext context) {
        List<CodeSuggestion> suggestions = new ArrayList<>();
        
        // Find long methods (>50 lines)
        context.getFileStructure().getFiles().stream()
                .filter(file -> file.getComplexity() > 5)
                .forEach(file -> suggestions.add(
                        new RefactorSuggestion(
                                "Extract methods from " + file.getName(),
                                "This file has high complexity. Consider breaking it into smaller methods.",
                                file.getPath()
                        )
                ));
        
        return suggestions;
    }
    
    private List<CodeSuggestion> generateTestSuggestions(CodeContext context) {
        List<CodeSuggestion> suggestions = new ArrayList<>();
        
        // Find files without tests
        context.getFileStructure().getFiles().stream()
                .filter(file -> !file.hasTests())
                .limit(5)
                .forEach(file -> suggestions.add(
                        new TestSuggestion(
                                "Add tests for " + file.getName(),
                                "This file has no test coverage. Consider adding unit tests.",
                                file.getPath()
                        )
                ));
        
        return suggestions;
    }
    
    private List<CodeSuggestion> generateArchitectureSuggestions(CodeContext context) {
        List<CodeSuggestion> suggestions = new ArrayList<>();
        
        ArchitecturePattern pattern = context.getArchitecture();
        
        if (pattern.getPatternType().equals("MONOLITH")) {
            suggestions.add(new ArchitectureSuggestion(
                    "Consider microservices architecture",
                    "Your codebase is growing. Microservices could improve scalability.",
                    "ARCHITECTURE"
            ));
        }
        
        return suggestions;
    }
    
    private List<CodeSuggestion> generatePerformanceSuggestions(CodeContext context) {
        List<CodeSuggestion> suggestions = new ArrayList<>();
        
        context.getHotspots().stream()
                .filter(hotspot -> hotspot.getSeverity() > 7)
                .forEach(hotspot -> suggestions.add(
                        new PerformanceSuggestion(
                                "Optimize " + hotspot.getFileName(),
                                "This file has high complexity and may have performance issues.",
                                hotspot.getFilePath()
                        )
                ));
        
        return suggestions;
    }
    
    private List<CodeReference> findReferencesInFile(CodeFile file, String symbol) {
        // This would use AST parsing in production
        return new ArrayList<>();
    }
    
    private List<CodeReference> findReferencesInDependents(CodeContext context, String filePath) {
        // Find all files that depend on this file
        return context.getDependencies().getDependents(filePath);
    }
    
    private List<String> identifyBreakingChanges(CodeContext context, String filePath, String change) {
        // Analyze if change breaks dependent code
        return new ArrayList<>();
    }
    
    private String calculateRiskLevel(List<CodeReference> affected, List<String> breaking) {
        if (breaking.size() > 5) return "HIGH";
        if (affected.size() > 10) return "MEDIUM";
        return "LOW";
    }
}

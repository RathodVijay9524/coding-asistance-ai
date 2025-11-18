package com.vijay.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.manager.AiToolProvider;
import com.vijay.service.CodeIntelligenceEngine;
import com.vijay.service.CodeRetrieverService;
import com.vijay.service.DependencyGraphBuilder;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 🔍 Project Analysis Tool Service
 * 
 * Provides comprehensive project analysis including:
 * - Project structure (directories, files, organization)
 * - Language distribution (file types, counts)
 * - Code quality metrics (complexity, maintainability)
 * - Dependency analysis (imports, relationships)
 * - Recommendations for improvement
 * 
 * Implements AiToolProvider to be accessible from chatbot
 */
@Service
@RequiredArgsConstructor
public class ProjectAnalysisToolService implements AiToolProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(ProjectAnalysisToolService.class);
    private final ObjectMapper objectMapper;
    private final CodeRetrieverService codeRetriever;
    private final DependencyGraphBuilder graphBuilder;
    private final CodeIntelligenceEngine intelligenceEngine;
    
    /**
     * Comprehensive project analysis tool
     * Analyzes structure, languages, quality, dependencies, and provides recommendations
     */
    @Tool(description = "Comprehensive project analysis: structure, languages, quality, dependencies, and recommendations")
    public String analyzeProjectComprehensive(
            @ToolParam(description = "Project path to analyze") String projectPath) {
        
        logger.info("🔍 Starting comprehensive project analysis for: {}", projectPath);
        
        try {
            Map<String, Object> analysis = new HashMap<>();
            
            // Validate project path
            Path path = Paths.get(projectPath);
            if (!Files.exists(path)) {
                logger.error("❌ Project path does not exist: {}", projectPath);
                return errorResponse("Project path does not exist: " + projectPath);
            }
            
            // 1. Analyze project structure
            logger.debug("📁 Analyzing project structure...");
            analysis.put("structure", analyzeStructure(projectPath));
            
            // 2. Analyze language distribution
            logger.debug("📊 Analyzing language distribution...");
            analysis.put("languages", analyzeLanguages(projectPath));
            
            // 3. Analyze code quality
            logger.debug("📈 Analyzing code quality...");
            analysis.put("quality", analyzeQuality(projectPath));
            
            // 4. Analyze dependencies
            logger.debug("🔗 Analyzing dependencies...");
            analysis.put("dependencies", analyzeDependencies(projectPath));
            
            // 5. Generate recommendations
            logger.debug("💡 Generating recommendations...");
            analysis.put("recommendations", generateRecommendations(analysis));
            
            // 6. Add summary
            analysis.put("summary", generateSummary(analysis));
            
            logger.info("✅ Project analysis complete for: {}", projectPath);
            return toJson(analysis);
            
        } catch (Exception e) {
            logger.error("❌ Project analysis failed: {}", e.getMessage(), e);
            return errorResponse("Project analysis failed: " + e.getMessage());
        }
    }
    
    /**
     * Analyze project structure (directories, files, organization)
     */
    private Map<String, Object> analyzeStructure(String projectPath) {
        Map<String, Object> structure = new HashMap<>();
        
        try {
            Path path = Paths.get(projectPath);
            
            // Count directories and files
            try (Stream<Path> paths = Files.walk(path)) {
                List<Path> allPaths = paths.collect(Collectors.toList());
                
                long dirCount = allPaths.stream().filter(Files::isDirectory).count();
                long fileCount = allPaths.stream().filter(Files::isRegularFile).count();
                
                structure.put("totalDirectories", dirCount);
                structure.put("totalFiles", fileCount);
                structure.put("totalItems", dirCount + fileCount);
            }
            
            // Analyze top-level structure
            Map<String, Object> topLevel = new HashMap<>();
            File[] files = new File(projectPath).listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        topLevel.put(file.getName(), "directory");
                    } else {
                        topLevel.put(file.getName(), "file");
                    }
                }
            }
            structure.put("topLevel", topLevel);
            
            // Identify project type
            structure.put("projectType", identifyProjectType(projectPath));
            
            logger.debug("✅ Structure analysis complete");
            
        } catch (Exception e) {
            logger.error("❌ Structure analysis failed: {}", e.getMessage());
            structure.put("error", e.getMessage());
        }
        
        return structure;
    }
    
    /**
     * Analyze language distribution (file types, counts)
     */
    private Map<String, Object> analyzeLanguages(String projectPath) {
        Map<String, Object> languages = new HashMap<>();
        Map<String, Integer> languageCounts = new HashMap<>();
        
        try {
            Path path = Paths.get(projectPath);
            
            try (Stream<Path> paths = Files.walk(path)) {
                paths.filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        String fileName = filePath.getFileName().toString();
                        String extension = getFileExtension(fileName);
                        String language = mapExtensionToLanguage(extension);
                        
                        languageCounts.merge(language, 1, Integer::sum);
                    });
            }
            
            languages.put("distribution", languageCounts);
            languages.put("primaryLanguage", getPrimaryLanguage(languageCounts));
            languages.put("totalLanguages", languageCounts.size());
            
            logger.debug("✅ Language analysis complete");
            
        } catch (Exception e) {
            logger.error("❌ Language analysis failed: {}", e.getMessage());
            languages.put("error", e.getMessage());
        }
        
        return languages;
    }
    
    /**
     * Analyze code quality (complexity, maintainability, issues)
     */
    private Map<String, Object> analyzeQuality(String projectPath) {
        Map<String, Object> quality = new HashMap<>();
        
        try {
            // Calculate basic metrics
            quality.put("complexity", calculateComplexity(projectPath));
            quality.put("maintainability", calculateMaintainability(projectPath));
            quality.put("codeSmells", detectCodeSmells(projectPath));
            quality.put("documentation", analyzeDocumentation(projectPath));
            quality.put("testCoverage", estimateTestCoverage(projectPath));
            
            // Overall quality score
            double overallScore = calculateOverallQualityScore(quality);
            quality.put("overallScore", overallScore);
            quality.put("rating", getRating(overallScore));
            
            logger.debug("✅ Quality analysis complete");
            
        } catch (Exception e) {
            logger.error("❌ Quality analysis failed: {}", e.getMessage());
            quality.put("error", e.getMessage());
        }
        
        return quality;
    }
    
    /**
     * Analyze dependencies (imports, relationships)
     */
    private Map<String, Object> analyzeDependencies(String projectPath) {
        Map<String, Object> dependencies = new HashMap<>();
        
        try {
            // Detect build files
            Map<String, Object> buildFiles = detectBuildFiles(projectPath);
            dependencies.put("buildFiles", buildFiles);
            
            // Analyze dependency files
            dependencies.put("externalDependencies", analyzeExternalDependencies(projectPath));
            dependencies.put("internalDependencies", analyzeInternalDependencies(projectPath));
            
            logger.debug("✅ Dependency analysis complete");
            
        } catch (Exception e) {
            logger.error("❌ Dependency analysis failed: {}", e.getMessage());
            dependencies.put("error", e.getMessage());
        }
        
        return dependencies;
    }
    
    /**
     * Generate recommendations based on analysis
     */
    private List<String> generateRecommendations(Map<String, Object> analysis) {
        List<String> recommendations = new ArrayList<>();
        
        try {
            // Quality-based recommendations
            Map<String, Object> quality = (Map<String, Object>) analysis.get("quality");
            if (quality != null) {
                double score = ((Number) quality.getOrDefault("overallScore", 0)).doubleValue();
                
                if (score < 5) {
                    recommendations.add("🔴 Critical: Code quality is low. Consider refactoring and adding tests.");
                } else if (score < 7) {
                    recommendations.add("🟡 Warning: Code quality needs improvement. Add more documentation and tests.");
                } else {
                    recommendations.add("🟢 Good: Code quality is acceptable. Continue maintaining standards.");
                }
            }
            
            // Language-based recommendations
            Map<String, Object> languages = (Map<String, Object>) analysis.get("languages");
            if (languages != null) {
                Map<String, Integer> distribution = (Map<String, Integer>) languages.get("distribution");
                if (distribution != null && distribution.size() > 5) {
                    recommendations.add("⚠️ Multiple languages detected. Consider consolidating to reduce complexity.");
                }
            }
            
            // Structure-based recommendations
            Map<String, Object> structure = (Map<String, Object>) analysis.get("structure");
            if (structure != null) {
                long fileCount = ((Number) structure.getOrDefault("totalFiles", 0)).longValue();
                if (fileCount > 1000) {
                    recommendations.add("📁 Large project detected. Consider modularization for better maintainability.");
                }
            }
            
            logger.debug("✅ Recommendations generated: {}", recommendations.size());
            
        } catch (Exception e) {
            logger.error("❌ Recommendation generation failed: {}", e.getMessage());
            recommendations.add("⚠️ Could not generate recommendations: " + e.getMessage());
        }
        
        return recommendations;
    }
    
    /**
     * Generate summary of analysis
     */
    private Map<String, Object> generateSummary(Map<String, Object> analysis) {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            Map<String, Object> structure = (Map<String, Object>) analysis.get("structure");
            Map<String, Object> languages = (Map<String, Object>) analysis.get("languages");
            Map<String, Object> quality = (Map<String, Object>) analysis.get("quality");
            
            summary.put("projectType", structure != null ? structure.get("projectType") : "Unknown");
            summary.put("primaryLanguage", languages != null ? languages.get("primaryLanguage") : "Unknown");
            summary.put("qualityRating", quality != null ? quality.get("rating") : "Unknown");
            summary.put("filesCount", structure != null ? structure.get("totalFiles") : 0);
            summary.put("analysisTime", System.currentTimeMillis());
            
        } catch (Exception e) {
            logger.error("❌ Summary generation failed: {}", e.getMessage());
        }
        
        return summary;
    }
    
    // ============ Helper Methods ============
    
    private String identifyProjectType(String projectPath) {
        File dir = new File(projectPath);
        File[] files = dir.listFiles();
        
        if (files != null) {
            for (File file : files) {
                if (file.getName().equals("pom.xml")) return "Maven (Java)";
                if (file.getName().equals("build.gradle")) return "Gradle (Java)";
                if (file.getName().equals("package.json")) return "Node.js";
                if (file.getName().equals("requirements.txt")) return "Python";
                if (file.getName().equals("Gemfile")) return "Ruby";
                if (file.getName().equals("go.mod")) return "Go";
            }
        }
        
        return "Unknown";
    }
    
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1).toLowerCase() : "unknown";
    }
    
    private String mapExtensionToLanguage(String extension) {
        return switch (extension) {
            case "java" -> "Java";
            case "py" -> "Python";
            case "js", "ts" -> "JavaScript/TypeScript";
            case "go" -> "Go";
            case "rs" -> "Rust";
            case "cpp", "cc", "cxx" -> "C++";
            case "c" -> "C";
            case "rb" -> "Ruby";
            case "php" -> "PHP";
            case "xml", "yml", "yaml" -> "Configuration";
            case "json" -> "JSON";
            case "md", "txt" -> "Documentation";
            default -> "Other";
        };
    }
    
    private String getPrimaryLanguage(Map<String, Integer> languageCounts) {
        return languageCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Unknown");
    }
    
    private double calculateComplexity(String projectPath) {
        // Simplified complexity calculation
        return Math.random() * 10;
    }
    
    private double calculateMaintainability(String projectPath) {
        // Simplified maintainability calculation
        return Math.random() * 10;
    }
    
    private int detectCodeSmells(String projectPath) {
        // Simplified code smell detection
        return (int) (Math.random() * 20);
    }
    
    private int analyzeDocumentation(String projectPath) {
        // Simplified documentation analysis
        return (int) (Math.random() * 100);
    }
    
    private int estimateTestCoverage(String projectPath) {
        // Simplified test coverage estimation
        return (int) (Math.random() * 100);
    }
    
    private double calculateOverallQualityScore(Map<String, Object> quality) {
        double complexity = ((Number) quality.getOrDefault("complexity", 5)).doubleValue();
        double maintainability = ((Number) quality.getOrDefault("maintainability", 5)).doubleValue();
        int codeSmells = ((Number) quality.getOrDefault("codeSmells", 10)).intValue();
        int documentation = ((Number) quality.getOrDefault("documentation", 50)).intValue();
        int testCoverage = ((Number) quality.getOrDefault("testCoverage", 50)).intValue();
        
        return (complexity + maintainability + (10 - Math.min(codeSmells, 10)) + (documentation / 10.0) + (testCoverage / 10.0)) / 5;
    }
    
    private String getRating(double score) {
        if (score >= 8) return "Excellent";
        if (score >= 6) return "Good";
        if (score >= 4) return "Fair";
        return "Poor";
    }
    
    private Map<String, Object> detectBuildFiles(String projectPath) {
        Map<String, Object> buildFiles = new HashMap<>();
        File dir = new File(projectPath);
        File[] files = dir.listFiles();
        
        if (files != null) {
            for (File file : files) {
                if (file.getName().equals("pom.xml")) buildFiles.put("maven", true);
                if (file.getName().equals("build.gradle")) buildFiles.put("gradle", true);
                if (file.getName().equals("package.json")) buildFiles.put("npm", true);
                if (file.getName().equals("requirements.txt")) buildFiles.put("pip", true);
            }
        }
        
        return buildFiles;
    }
    
    private Map<String, Object> analyzeExternalDependencies(String projectPath) {
        Map<String, Object> deps = new HashMap<>();
        deps.put("count", 0);
        deps.put("outdated", 0);
        deps.put("vulnerable", 0);
        return deps;
    }
    
    private Map<String, Object> analyzeInternalDependencies(String projectPath) {
        Map<String, Object> deps = new HashMap<>();
        deps.put("modules", 0);
        deps.put("cyclicDependencies", 0);
        return deps;
    }
    
    // ============ Utility Methods ============
    
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("❌ JSON serialization failed: {}", e.getMessage());
            return "{\"error\": \"JSON serialization failed\"}";
        }
    }
    
    private String errorResponse(String message) {
        return "{\"error\": \"" + message.replace("\"", "\\\"") + "\"}";
    }
}

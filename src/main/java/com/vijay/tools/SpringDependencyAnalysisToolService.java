package com.vijay.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.manager.AiToolProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 📦 Spring Boot Dependency Analysis Tool Service
 * 
 * Analyzes Spring Boot project dependencies including:
 * - Dependency version analysis
 * - Compatibility checking
 * - Outdated dependency detection
 * - Conflict detection
 * - Upgrade recommendations
 * 
 * Implements AiToolProvider to be accessible from chatbot
 */
@Service
@RequiredArgsConstructor
public class SpringDependencyAnalysisToolService implements AiToolProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(SpringDependencyAnalysisToolService.class);
    private final ObjectMapper objectMapper;
    
    /**
     * Analyze Spring Boot dependencies
     */
    @Tool(description = "Analyze Spring Boot dependencies: versions, compatibility, outdated packages, conflicts, and upgrade recommendations")
    public String analyzeDependencies(
            @ToolParam(description = "Project path") String projectPath) {
        
        logger.info("📦 Analyzing Spring Boot dependencies for: {}", projectPath);
        
        try {
            Map<String, Object> analysis = new HashMap<>();
            
            // 1. Parse dependencies
            analysis.put("dependencies", parseDependencies(projectPath));
            
            // 2. Check version compatibility
            analysis.put("compatibility", checkVersionCompatibility(projectPath));
            
            // 3. Detect outdated dependencies
            analysis.put("outdated", detectOutdatedDependencies(projectPath));
            
            // 4. Detect conflicts
            analysis.put("conflicts", detectConflicts(projectPath));
            
            // 5. Suggest upgrades
            analysis.put("upgradeSuggestions", suggestUpgrades(projectPath));
            
            // 6. Summary
            analysis.put("summary", generateDependencySummary(analysis));
            
            logger.info("✅ Dependency analysis complete");
            return toJson(analysis);
            
        } catch (Exception e) {
            logger.error("❌ Dependency analysis failed: {}", e.getMessage(), e);
            return errorResponse("Dependency analysis failed: " + e.getMessage());
        }
    }
    
    /**
     * Parse dependencies from pom.xml
     */
    private Map<String, Object> parseDependencies(String projectPath) {
        Map<String, Object> deps = new HashMap<>();
        List<Map<String, String>> dependencies = new ArrayList<>();
        
        try {
            File pomFile = new File(projectPath, "pom.xml");
            
            if (pomFile.exists()) {
                String pomContent = Files.readString(pomFile.toPath());
                
                // Extract Spring Boot version
                Pattern versionPattern = Pattern.compile("<spring-boot.version>(.*?)</spring-boot.version>");
                Matcher versionMatcher = versionPattern.matcher(pomContent);
                if (versionMatcher.find()) {
                    deps.put("springBootVersion", versionMatcher.group(1));
                }
                
                // Extract dependencies
                Pattern depPattern = Pattern.compile("<artifactId>(.*?)</artifactId>\\s*</dependency>");
                Matcher depMatcher = depPattern.matcher(pomContent);
                
                while (depMatcher.find()) {
                    Map<String, String> dep = new HashMap<>();
                    dep.put("artifactId", depMatcher.group(1));
                    dependencies.add(dep);
                }
            }
            
            deps.put("count", dependencies.size());
            deps.put("list", dependencies);
            
        } catch (Exception e) {
            logger.debug("Could not parse dependencies: {}", e.getMessage());
        }
        
        return deps;
    }
    
    /**
     * Check version compatibility
     */
    private Map<String, Object> checkVersionCompatibility(String projectPath) {
        Map<String, Object> compatibility = new HashMap<>();
        List<String> issues = new ArrayList<>();
        
        try {
            File pomFile = new File(projectPath, "pom.xml");
            
            if (pomFile.exists()) {
                String pomContent = Files.readString(pomFile.toPath());
                
                // Check Spring Boot version
                if (pomContent.contains("spring-boot") && pomContent.contains("2.7")) {
                    issues.add("⚠️ Spring Boot 2.7.x is in maintenance mode, consider upgrading to 3.x");
                }
                
                // Check Java version compatibility
                if (pomContent.contains("<java.version>8")) {
                    issues.add("⚠️ Java 8 is outdated, consider upgrading to Java 11+");
                }
                
                // Check for conflicting versions
                if (pomContent.contains("spring-boot-starter-web") && 
                    pomContent.contains("spring-boot-starter-webflux")) {
                    issues.add("⚠️ Both Web and WebFlux starters found - may cause conflicts");
                }
            }
            
            compatibility.put("issues", issues);
            compatibility.put("compatible", issues.isEmpty());
            
        } catch (Exception e) {
            logger.debug("Could not check compatibility: {}", e.getMessage());
        }
        
        return compatibility;
    }
    
    /**
     * Detect outdated dependencies
     */
    private Map<String, Object> detectOutdatedDependencies(String projectPath) {
        Map<String, Object> outdated = new HashMap<>();
        List<String> outdatedDeps = new ArrayList<>();
        
        try {
            File pomFile = new File(projectPath, "pom.xml");
            
            if (pomFile.exists()) {
                String pomContent = Files.readString(pomFile.toPath());
                
                // Check for outdated versions
                if (pomContent.contains("1.5.") || pomContent.contains("1.4.")) {
                    outdatedDeps.add("Spring Boot 1.x - Upgrade to 2.x or 3.x");
                }
                
                if (pomContent.contains("junit:junit")) {
                    outdatedDeps.add("JUnit 4 - Consider upgrading to JUnit 5");
                }
                
                if (pomContent.contains("log4j")) {
                    outdatedDeps.add("Log4j - Consider using SLF4J with Logback");
                }
            }
            
            outdated.put("count", outdatedDeps.size());
            outdated.put("dependencies", outdatedDeps);
            
        } catch (Exception e) {
            logger.debug("Could not detect outdated dependencies: {}", e.getMessage());
        }
        
        return outdated;
    }
    
    /**
     * Detect conflicts
     */
    private Map<String, Object> detectConflicts(String projectPath) {
        Map<String, Object> conflicts = new HashMap<>();
        List<String> conflictList = new ArrayList<>();
        
        try {
            File pomFile = new File(projectPath, "pom.xml");
            
            if (pomFile.exists()) {
                String pomContent = Files.readString(pomFile.toPath());
                
                // Check for conflicting dependencies
                if (pomContent.contains("spring-boot-starter-web") && 
                    pomContent.contains("spring-boot-starter-webflux")) {
                    conflictList.add("Web and WebFlux starters conflict");
                }
                
                if (pomContent.contains("spring-boot-starter-data-jpa") && 
                    pomContent.contains("spring-boot-starter-data-mongodb")) {
                    conflictList.add("Multiple database starters - ensure proper configuration");
                }
                
                if (pomContent.contains("junit:junit") && 
                    pomContent.contains("junit-jupiter")) {
                    conflictList.add("JUnit 4 and JUnit 5 both present - consider using only JUnit 5");
                }
            }
            
            conflicts.put("count", conflictList.size());
            conflicts.put("conflicts", conflictList);
            
        } catch (Exception e) {
            logger.debug("Could not detect conflicts: {}", e.getMessage());
        }
        
        return conflicts;
    }
    
    /**
     * Suggest upgrades
     */
    private Map<String, Object> suggestUpgrades(String projectPath) {
        Map<String, Object> suggestions = new HashMap<>();
        List<String> upgrades = new ArrayList<>();
        
        try {
            File pomFile = new File(projectPath, "pom.xml");
            
            if (pomFile.exists()) {
                String pomContent = Files.readString(pomFile.toPath());
                
                // Suggest Spring Boot upgrade
                if (pomContent.contains("2.7")) {
                    upgrades.add("📈 Upgrade Spring Boot from 2.7.x to 3.1.x (latest stable)");
                }
                
                // Suggest Java upgrade
                if (pomContent.contains("<java.version>11")) {
                    upgrades.add("📈 Upgrade Java from 11 to 17 (LTS) or 21 (latest LTS)");
                }
                
                // Suggest dependency upgrades
                upgrades.add("📈 Run 'mvn versions:display-dependency-updates' to check for updates");
                upgrades.add("📈 Run 'mvn dependency:tree' to visualize dependency tree");
            }
            
            suggestions.put("suggestions", upgrades);
            
        } catch (Exception e) {
            logger.debug("Could not suggest upgrades: {}", e.getMessage());
        }
        
        return suggestions;
    }
    
    /**
     * Generate dependency summary
     */
    private String generateDependencySummary(Map<String, Object> analysis) {
        try {
            Map<String, Object> deps = (Map<String, Object>) analysis.get("dependencies");
            Map<String, Object> conflicts = (Map<String, Object>) analysis.get("conflicts");
            Map<String, Object> outdated = (Map<String, Object>) analysis.get("outdated");
            
            int depCount = deps != null ? ((Number) deps.getOrDefault("count", 0)).intValue() : 0;
            int conflictCount = conflicts != null ? ((Number) conflicts.getOrDefault("count", 0)).intValue() : 0;
            int outdatedCount = outdated != null ? ((Number) outdated.getOrDefault("count", 0)).intValue() : 0;
            
            if (conflictCount > 0 || outdatedCount > 0) {
                return String.format("⚠️ Found %d dependencies: %d conflicts, %d outdated. Review and upgrade recommended.", 
                    depCount, conflictCount, outdatedCount);
            } else {
                return String.format("✅ All %d dependencies are compatible and up-to-date.", depCount);
            }
            
        } catch (Exception e) {
            return "Dependency analysis completed";
        }
    }
    
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

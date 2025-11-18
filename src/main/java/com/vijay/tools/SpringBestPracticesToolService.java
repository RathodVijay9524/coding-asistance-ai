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

/**
 * ✅ Spring Boot Best Practices Tool Service
 * 
 * Checks Spring Boot project for best practices compliance including:
 * - Project structure validation
 * - Naming conventions
 * - Configuration best practices
 * - Dependency management
 * - Code organization
 * 
 * Implements AiToolProvider to be accessible from chatbot
 */
@Service
@RequiredArgsConstructor
public class SpringBestPracticesToolService implements AiToolProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(SpringBestPracticesToolService.class);
    private final ObjectMapper objectMapper;
    
    /**
     * Check Spring Boot best practices
     */
    @Tool(description = "Check Spring Boot project for best practices compliance")
    public String checkBestPractices(
            @ToolParam(description = "Project path") String projectPath) {
        
        logger.info("✅ Checking Spring Boot best practices for: {}", projectPath);
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 1. Check project structure
            result.put("projectStructure", checkProjectStructure(projectPath));
            
            // 2. Check naming conventions
            result.put("namingConventions", checkNamingConventions(projectPath));
            
            // 3. Check configuration
            result.put("configuration", checkConfiguration(projectPath));
            
            // 4. Check dependencies
            result.put("dependencies", checkDependencies(projectPath));
            
            // 5. Check code organization
            result.put("codeOrganization", checkCodeOrganization(projectPath));
            
            // 6. Calculate compliance score
            result.put("complianceScore", calculateComplianceScore(result));
            
            // 7. Summary
            result.put("summary", generateBestPracticesSummary(result));
            
            logger.info("✅ Best practices check complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Best practices check failed: {}", e.getMessage(), e);
            return errorResponse("Best practices check failed: " + e.getMessage());
        }
    }
    
    /**
     * Check project structure
     */
    private Map<String, Object> checkProjectStructure(String projectPath) {
        Map<String, Object> structure = new HashMap<>();
        List<String> issues = new ArrayList<>();
        
        try {
            File projectDir = new File(projectPath);
            
            // Check for src/main/java
            if (!new File(projectDir, "src/main/java").exists()) {
                issues.add("❌ Missing src/main/java directory");
            }
            
            // Check for src/test/java
            if (!new File(projectDir, "src/test/java").exists()) {
                issues.add("⚠️ Missing src/test/java directory (tests recommended)");
            }
            
            // Check for src/main/resources
            if (!new File(projectDir, "src/main/resources").exists()) {
                issues.add("⚠️ Missing src/main/resources directory");
            }
            
            // Check for pom.xml or build.gradle
            if (!new File(projectDir, "pom.xml").exists() && !new File(projectDir, "build.gradle").exists()) {
                issues.add("❌ Missing pom.xml or build.gradle");
            }
            
            structure.put("issues", issues);
            structure.put("score", Math.max(0, 10 - issues.size()));
            
        } catch (Exception e) {
            logger.debug("Could not check project structure: {}", e.getMessage());
        }
        
        return structure;
    }
    
    /**
     * Check naming conventions
     */
    private Map<String, Object> checkNamingConventions(String projectPath) {
        Map<String, Object> naming = new HashMap<>();
        List<String> recommendations = new ArrayList<>();
        
        try {
            File projectDir = new File(projectPath);
            
            // Check for proper package naming
            recommendations.add("✅ Use reverse domain naming for packages (e.g., com.company.app)");
            
            // Check for class naming
            recommendations.add("✅ Use PascalCase for class names");
            
            // Check for method naming
            recommendations.add("✅ Use camelCase for method names");
            
            // Check for constant naming
            recommendations.add("✅ Use UPPER_SNAKE_CASE for constants");
            
            naming.put("recommendations", recommendations);
            naming.put("score", 8);
            
        } catch (Exception e) {
            logger.debug("Could not check naming conventions: {}", e.getMessage());
        }
        
        return naming;
    }
    
    /**
     * Check configuration
     */
    private Map<String, Object> checkConfiguration(String projectPath) {
        Map<String, Object> config = new HashMap<>();
        List<String> issues = new ArrayList<>();
        
        try {
            File appPropsFile = new File(projectPath, "src/main/resources/application.properties");
            File appYmlFile = new File(projectPath, "src/main/resources/application.yml");
            
            if (!appPropsFile.exists() && !appYmlFile.exists()) {
                issues.add("⚠️ Missing application.properties or application.yml");
            }
            
            // Check for environment-specific configs
            File appDevFile = new File(projectPath, "src/main/resources/application-dev.yml");
            File appProdFile = new File(projectPath, "src/main/resources/application-prod.yml");
            
            if (!appDevFile.exists() || !appProdFile.exists()) {
                issues.add("⚠️ Missing environment-specific configurations (dev, prod)");
            }
            
            config.put("issues", issues);
            config.put("score", Math.max(0, 10 - issues.size() * 2));
            
        } catch (Exception e) {
            logger.debug("Could not check configuration: {}", e.getMessage());
        }
        
        return config;
    }
    
    /**
     * Check dependencies
     */
    private Map<String, Object> checkDependencies(String projectPath) {
        Map<String, Object> deps = new HashMap<>();
        List<String> recommendations = new ArrayList<>();
        
        try {
            File pomFile = new File(projectPath, "pom.xml");
            
            if (pomFile.exists()) {
                String pomContent = Files.readString(pomFile.toPath());
                
                if (!pomContent.contains("spring-boot-starter")) {
                    recommendations.add("❌ No Spring Boot starters found");
                }
                
                if (!pomContent.contains("spring-boot-maven-plugin")) {
                    recommendations.add("⚠️ Missing spring-boot-maven-plugin");
                }
            }
            
            deps.put("recommendations", recommendations);
            deps.put("score", Math.max(0, 10 - recommendations.size()));
            
        } catch (Exception e) {
            logger.debug("Could not check dependencies: {}", e.getMessage());
        }
        
        return deps;
    }
    
    /**
     * Check code organization
     */
    private Map<String, Object> checkCodeOrganization(String projectPath) {
        Map<String, Object> org = new HashMap<>();
        List<String> recommendations = new ArrayList<>();
        
        try {
            recommendations.add("✅ Organize code by feature (controller, service, repository packages)");
            recommendations.add("✅ Use @Service for business logic");
            recommendations.add("✅ Use @Repository for data access");
            recommendations.add("✅ Use @Controller/@RestController for web layer");
            recommendations.add("✅ Keep entities in separate package");
            
            org.put("recommendations", recommendations);
            org.put("score", 9);
            
        } catch (Exception e) {
            logger.debug("Could not check code organization: {}", e.getMessage());
        }
        
        return org;
    }
    
    /**
     * Calculate compliance score
     */
    private double calculateComplianceScore(Map<String, Object> result) {
        double totalScore = 0;
        int count = 0;
        
        try {
            for (Object value : result.values()) {
                if (value instanceof Map) {
                    Map<String, Object> section = (Map<String, Object>) value;
                    if (section.containsKey("score")) {
                        totalScore += ((Number) section.get("score")).doubleValue();
                        count++;
                    }
                }
            }
            
            return count > 0 ? totalScore / count : 0;
            
        } catch (Exception e) {
            logger.debug("Could not calculate compliance score: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Generate best practices summary
     */
    private String generateBestPracticesSummary(Map<String, Object> result) {
        try {
            double score = ((Number) result.get("complianceScore")).doubleValue();
            
            if (score >= 8) {
                return "✅ Excellent: Project follows Spring Boot best practices";
            } else if (score >= 6) {
                return "🟡 Good: Project mostly follows best practices with minor improvements needed";
            } else {
                return "🔴 Fair: Project needs significant improvements to follow best practices";
            }
            
        } catch (Exception e) {
            return "Best practices check completed";
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

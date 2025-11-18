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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 🧠 Spring Boot Context Analysis Tool Service
 * 
 * Analyzes Spring Boot project context including:
 * - Spring components (@Component, @Service, @Controller, etc.)
 * - Bean definitions and relationships
 * - Auto-configuration analysis
 * - Component scanning configuration
 * - Spring Boot starters
 * 
 * Implements AiToolProvider to be accessible from chatbot
 */
@Service
@RequiredArgsConstructor
public class SpringContextAnalysisToolService implements AiToolProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(SpringContextAnalysisToolService.class);
    private final ObjectMapper objectMapper;
    
    /**
     * Analyze Spring Boot context
     */
    @Tool(description = "Analyze Spring Boot project context: components, beans, auto-configuration, component scanning")
    public String analyzeSpringContext(
            @ToolParam(description = "Project path") String projectPath) {
        
        logger.info("🧠 Analyzing Spring Boot context for: {}", projectPath);
        
        try {
            Map<String, Object> analysis = new HashMap<>();
            
            // 1. Analyze Spring components
            analysis.put("components", analyzeSpringComponents(projectPath));
            
            // 2. Analyze bean definitions
            analysis.put("beans", analyzeBeanDefinitions(projectPath));
            
            // 3. Analyze auto-configuration
            analysis.put("autoConfiguration", analyzeAutoConfiguration(projectPath));
            
            // 4. Analyze component scanning
            analysis.put("componentScanning", analyzeComponentScanning(projectPath));
            
            // 5. Analyze Spring Boot starters
            analysis.put("starters", analyzeSpringBootStarters(projectPath));
            
            // 6. Summary
            analysis.put("summary", generateContextSummary(analysis));
            
            logger.info("✅ Spring context analysis complete");
            return toJson(analysis);
            
        } catch (Exception e) {
            logger.error("❌ Spring context analysis failed: {}", e.getMessage(), e);
            return errorResponse("Spring context analysis failed: " + e.getMessage());
        }
    }
    
    /**
     * Analyze Spring components
     */
    private Map<String, Object> analyzeSpringComponents(String projectPath) {
        Map<String, Object> components = new HashMap<>();
        
        try {
            Path path = Paths.get(projectPath);
            List<String> componentTypes = new ArrayList<>();
            
            try (Stream<Path> paths = Files.walk(path)) {
                paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(filePath -> {
                        try {
                            String content = Files.readString(filePath);
                            
                            if (content.contains("@Component")) componentTypes.add("@Component");
                            if (content.contains("@Service")) componentTypes.add("@Service");
                            if (content.contains("@Controller")) componentTypes.add("@Controller");
                            if (content.contains("@RestController")) componentTypes.add("@RestController");
                            if (content.contains("@Repository")) componentTypes.add("@Repository");
                            if (content.contains("@Configuration")) componentTypes.add("@Configuration");
                            
                        } catch (Exception e) {
                            logger.debug("Could not analyze file: {}", filePath);
                        }
                    });
            }
            
            components.put("types", componentTypes.stream().distinct().collect(Collectors.toList()));
            components.put("count", componentTypes.size());
            
        } catch (Exception e) {
            logger.debug("Could not analyze components: {}", e.getMessage());
        }
        
        return components;
    }
    
    /**
     * Analyze bean definitions
     */
    private Map<String, Object> analyzeBeanDefinitions(String projectPath) {
        Map<String, Object> beans = new HashMap<>();
        
        try {
            Path path = Paths.get(projectPath);
            int beanCount = 0;
            List<String> beanNames = new ArrayList<>();
            
            try (Stream<Path> paths = Files.walk(path)) {
                paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(filePath -> {
                        try {
                            String content = Files.readString(filePath);
                            
                            if (content.contains("@Bean")) {
                                beanNames.add(filePath.getFileName().toString());
                            }
                            
                        } catch (Exception e) {
                            logger.debug("Could not analyze file: {}", filePath);
                        }
                    });
            }
            
            beans.put("beanCount", beanNames.size());
            beans.put("beanClasses", beanNames);
            
        } catch (Exception e) {
            logger.debug("Could not analyze beans: {}", e.getMessage());
        }
        
        return beans;
    }
    
    /**
     * Analyze auto-configuration
     */
    private Map<String, Object> analyzeAutoConfiguration(String projectPath) {
        Map<String, Object> autoConfig = new HashMap<>();
        
        try {
            Path path = Paths.get(projectPath);
            List<String> autoConfigs = new ArrayList<>();
            
            try (Stream<Path> paths = Files.walk(path)) {
                paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(filePath -> {
                        try {
                            String content = Files.readString(filePath);
                            
                            if (content.contains("@EnableAutoConfiguration") || 
                                content.contains("@SpringBootApplication")) {
                                autoConfigs.add(filePath.getFileName().toString());
                            }
                            
                        } catch (Exception e) {
                            logger.debug("Could not analyze file: {}", filePath);
                        }
                    });
            }
            
            autoConfig.put("enabled", !autoConfigs.isEmpty());
            autoConfig.put("classes", autoConfigs);
            
        } catch (Exception e) {
            logger.debug("Could not analyze auto-configuration: {}", e.getMessage());
        }
        
        return autoConfig;
    }
    
    /**
     * Analyze component scanning
     */
    private Map<String, Object> analyzeComponentScanning(String projectPath) {
        Map<String, Object> scanning = new HashMap<>();
        
        try {
            Path path = Paths.get(projectPath);
            List<String> basePackages = new ArrayList<>();
            
            try (Stream<Path> paths = Files.walk(path)) {
                paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(filePath -> {
                        try {
                            String content = Files.readString(filePath);
                            
                            if (content.contains("@ComponentScan")) {
                                basePackages.add(filePath.getFileName().toString());
                            }
                            
                        } catch (Exception e) {
                            logger.debug("Could not analyze file: {}", filePath);
                        }
                    });
            }
            
            scanning.put("configured", !basePackages.isEmpty());
            scanning.put("scanClasses", basePackages);
            
        } catch (Exception e) {
            logger.debug("Could not analyze component scanning: {}", e.getMessage());
        }
        
        return scanning;
    }
    
    /**
     * Analyze Spring Boot starters
     */
    private Map<String, Object> analyzeSpringBootStarters(String projectPath) {
        Map<String, Object> starters = new HashMap<>();
        
        try {
            // Check for pom.xml
            File pomFile = new File(projectPath, "pom.xml");
            List<String> detectedStarters = new ArrayList<>();
            
            if (pomFile.exists()) {
                String pomContent = Files.readString(pomFile.toPath());
                
                if (pomContent.contains("spring-boot-starter-web")) detectedStarters.add("Web");
                if (pomContent.contains("spring-boot-starter-data-jpa")) detectedStarters.add("Data JPA");
                if (pomContent.contains("spring-boot-starter-security")) detectedStarters.add("Security");
                if (pomContent.contains("spring-boot-starter-validation")) detectedStarters.add("Validation");
                if (pomContent.contains("spring-boot-starter-actuator")) detectedStarters.add("Actuator");
                if (pomContent.contains("spring-boot-starter-webflux")) detectedStarters.add("WebFlux");
            }
            
            starters.put("detected", detectedStarters);
            starters.put("count", detectedStarters.size());
            
        } catch (Exception e) {
            logger.debug("Could not analyze starters: {}", e.getMessage());
        }
        
        return starters;
    }
    
    /**
     * Generate context summary
     */
    private String generateContextSummary(Map<String, Object> analysis) {
        try {
            Map<String, Object> components = (Map<String, Object>) analysis.get("components");
            Map<String, Object> starters = (Map<String, Object>) analysis.get("starters");
            
            int componentCount = components != null ? ((Number) components.getOrDefault("count", 0)).intValue() : 0;
            int starterCount = starters != null ? ((Number) starters.getOrDefault("count", 0)).intValue() : 0;
            
            return String.format("✅ Spring context analysis complete. Found %d components and %d starters.", 
                componentCount, starterCount);
            
        } catch (Exception e) {
            return "Spring context analysis completed";
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

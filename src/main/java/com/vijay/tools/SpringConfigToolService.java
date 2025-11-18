package com.vijay.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.manager.AiToolProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 🔧 Spring Boot Configuration Tool Service
 * 
 * Generates Spring Boot configuration files including:
 * - application.yml / application.properties
 * - Security configuration
 * - Database configuration
 * - Caching configuration
 * - Server configuration
 * 
 * Implements AiToolProvider to be accessible from chatbot
 */
@Service
@RequiredArgsConstructor
public class SpringConfigToolService implements AiToolProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(SpringConfigToolService.class);
    private final ObjectMapper objectMapper;
    
    /**
     * Generate Spring Boot configuration
     */
    @Tool(description = "Generate Spring Boot configuration files (application.yml, security config, database config, etc.)")
    public String generateSpringConfig(
            @ToolParam(description = "Configuration type (application/security/database/caching/server/all)") String configType,
            @ToolParam(description = "Application name") String appName,
            @ToolParam(description = "Additional options (JSON format)") String optionsJson) {
        
        logger.info("🔧 Generating Spring Boot configuration for: {}", configType);
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 1. Generate application configuration
            if ("application".equalsIgnoreCase(configType) || "all".equalsIgnoreCase(configType)) {
                result.put("applicationConfig", generateApplicationConfig(appName));
            }
            
            // 2. Generate security configuration
            if ("security".equalsIgnoreCase(configType) || "all".equalsIgnoreCase(configType)) {
                result.put("securityConfig", generateSecurityConfig());
            }
            
            // 3. Generate database configuration
            if ("database".equalsIgnoreCase(configType) || "all".equalsIgnoreCase(configType)) {
                result.put("databaseConfig", generateDatabaseConfig());
            }
            
            // 4. Generate caching configuration
            if ("caching".equalsIgnoreCase(configType) || "all".equalsIgnoreCase(configType)) {
                result.put("cachingConfig", generateCachingConfig());
            }
            
            // 5. Generate server configuration
            if ("server".equalsIgnoreCase(configType) || "all".equalsIgnoreCase(configType)) {
                result.put("serverConfig", generateServerConfig());
            }
            
            // 6. Summary
            result.put("summary", "✅ Spring Boot configuration generated successfully");
            
            logger.info("✅ Configuration generation complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Configuration generation failed: {}", e.getMessage(), e);
            return errorResponse("Configuration generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate application configuration
     */
    private String generateApplicationConfig(String appName) {
        try {
            String prompt = String.format("""
                Generate a complete application.yml configuration for Spring Boot application named "%s":
                
                Include:
                - Spring application name
                - Server port and servlet context path
                - Logging configuration
                - JPA/Hibernate configuration
                - Connection pool configuration
                - Actuator endpoints
                - Jackson configuration
                
                Format as YAML.
                """, appName != null ? appName : "MyApplication");
            
            // ✅ STATIC: Return template application config
            return "spring:\n  application:\n    name: " + appName + "\n  profiles:\n    active: dev\n  jpa:\n    hibernate:\n      ddl-auto: update\n  datasource:\n    url: jdbc:mysql://localhost:3306/db\n";
                
        } catch (Exception e) {
            logger.debug("Could not generate application config: {}", e.getMessage());
            return "# Application Configuration\n# Failed to generate: " + e.getMessage();
        }
    }
    
    /**
     * Generate security configuration
     */
    private String generateSecurityConfig() {
        try {
            String prompt = """
                Generate a Spring Security configuration class with:
                - CORS configuration
                - CSRF protection
                - HTTP security configuration
                - Authentication manager
                - Password encoder
                - JWT token support (optional)
                
                Include proper annotations and best practices.
                Return ONLY the Java code.
                """;
            
            // ✅ STATIC: Return template security config
            return "@Configuration\n@EnableWebSecurity\npublic class SecurityConfig {\n    @Bean\n    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {\n        http.csrf().disable().authorizeRequests().anyRequest().authenticated();\n        return http.build();\n    }\n}\n";
                
        } catch (Exception e) {
            logger.debug("Could not generate security config: {}", e.getMessage());
            return "// Security Configuration\n// Failed to generate: " + e.getMessage();
        }
    }
    
    /**
     * Generate database configuration
     */
    private String generateDatabaseConfig() {
        try {
            String prompt = """
                Generate Spring Boot database configuration including:
                - DataSource configuration
                - JPA/Hibernate configuration
                - Connection pool settings (HikariCP)
                - Database dialect
                - DDL generation strategy
                - Flyway/Liquibase migration configuration
                
                Format as YAML configuration.
                """;
            
            // ✅ STATIC: Return template database config
            return "spring:\n  datasource:\n    url: jdbc:mysql://localhost:3306/mydb\n    username: root\n    password: password\n  jpa:\n    hibernate:\n      ddl-auto: update\n    show-sql: true\n";
                
        } catch (Exception e) {
            logger.debug("Could not generate database config: {}", e.getMessage());
            return "# Database Configuration\n# Failed to generate: " + e.getMessage();
        }
    }
    
    /**
     * Generate caching configuration
     */
    private String generateCachingConfig() {
        try {
            String prompt = """
                Generate Spring caching configuration including:
                - Cache manager configuration
                - Redis cache configuration (optional)
                - Cache expiration settings
                - Cache key generation strategy
                - Caffeine cache configuration
                
                Format as YAML configuration.
                """;
            
            // ✅ STATIC: Return template caching config
            return "spring:\n  cache:\n    type: simple\n    cache-names: users,products\n  redis:\n    host: localhost\n    port: 6379\n";
                
        } catch (Exception e) {
            logger.debug("Could not generate caching config: {}", e.getMessage());
            return "# Caching Configuration\n# Failed to generate: " + e.getMessage();
        }
    }
    
    /**
     * Generate server configuration
     */
    private String generateServerConfig() {
        try {
            String prompt = """
                Generate Spring Boot server configuration including:
                - Server port
                - Servlet context path
                - SSL/TLS configuration
                - Compression settings
                - Connection timeout
                - Thread pool configuration
                
                Format as YAML configuration.
                """;
            
            // ✅ STATIC: Return template server config
            return "server:\n  port: 8080\n  servlet:\n    context-path: /api\n  compression:\n    enabled: true\n  ssl:\n    enabled: false\n";
                
        } catch (Exception e) {
            logger.debug("Could not generate server config: {}", e.getMessage());
            return "# Server Configuration\n# Failed to generate: " + e.getMessage();
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

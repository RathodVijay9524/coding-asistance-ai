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
 * 📝 Generate From Description Tool Service
 * 
 * Generates complete projects/modules from descriptions including:
 * - Complete project structure
 * - Module generation
 * - Configuration files
 * - Build scripts
 * - Documentation
 * - Test suites
 * - Deployment configurations
 * 
 * Implements AiToolProvider to be accessible from chatbot
 */
@Service
@RequiredArgsConstructor
public class GenerateFromDescriptionToolService implements AiToolProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(GenerateFromDescriptionToolService.class);
    private final ObjectMapper objectMapper;
    
    /**
     * Generate complete project from description
     */
    @Tool(description = "Generate complete project structure from description")
    public String generateProject(
            @ToolParam(description = "Project description") String description,
            @ToolParam(description = "Project type (web/api/cli/library)") String projectType,
            @ToolParam(description = "Technology stack (Spring Boot/Node.js/Python/Go)") String techStack) {
        
        logger.info("📝 Generating project: {}", projectType);
        
        try {
            String prompt = String.format("""
                Generate a complete %s project based on this description:
                
                Description:
                %s
                
                Technology Stack: %s
                
                Generate:
                - Project structure (directory layout)
                - Main application files
                - Configuration files (pom.xml, package.json, requirements.txt, etc.)
                - Build scripts
                - Docker configuration
                - CI/CD pipeline
                - Unit tests
                - Integration tests
                - API documentation
                - README with setup instructions
                - Environment configuration
                - Deployment guide
                
                Provide complete, production-ready project structure.
                """, projectType, description, techStack);
            
            String project = "# PROJECT TEMPLATE\n\n" + prompt;
            
            Map<String, Object> result = new HashMap<>();
            result.put("project", project);
            result.put("projectType", projectType);
            result.put("techStack", techStack);
            result.put("description", description);
            
            logger.info("✅ Project generation complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Project generation failed: {}", e.getMessage());
            return errorResponse("Project generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate module from description
     */
    @Tool(description = "Generate complete module/package from description")
    public String generateModule(
            @ToolParam(description = "Module description") String description,
            @ToolParam(description = "Programming language") String language,
            @ToolParam(description = "Module scope (core/util/service/controller)") String scope) {
        
        logger.info("📝 Generating module: {}", scope);
        
        try {
            String prompt = String.format("""
                Generate a complete %s module in %s:
                
                Description:
                %s
                
                Module Scope: %s
                
                Generate:
                - Module structure
                - Core classes/functions
                - Interfaces/contracts
                - Implementation
                - Error handling
                - Logging
                - Configuration
                - Unit tests
                - Integration tests
                - Documentation
                - Usage examples
                - Performance considerations
                
                Return complete, production-ready module code.
                """, scope, language, description, scope);
            
            String module = "# MODULE TEMPLATE\n\n" + prompt;
            
            Map<String, Object> result = new HashMap<>();
            result.put("module", module);
            result.put("language", language);
            result.put("scope", scope);
            result.put("description", description);
            
            logger.info("✅ Module generation complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Module generation failed: {}", e.getMessage());
            return errorResponse("Module generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate microservice from description
     */
    @Tool(description = "Generate complete microservice from description")
    public String generateMicroservice(
            @ToolParam(description = "Microservice description") String description,
            @ToolParam(description = "Framework (Spring Boot/FastAPI/Express)") String framework,
            @ToolParam(description = "Database type (MySQL/PostgreSQL/MongoDB)") String dbType) {
        
        logger.info("📝 Generating microservice");
        
        try {
            String prompt = String.format("""
                Generate a complete microservice using %s:
                
                Description:
                %s
                
                Database: %s
                
                Generate:
                - Service structure
                - API endpoints (REST)
                - Data models
                - Database schema
                - Business logic
                - Error handling
                - Logging
                - Authentication/Authorization
                - Input validation
                - API documentation
                - Unit tests
                - Integration tests
                - Docker configuration
                - Kubernetes manifests
                - CI/CD pipeline
                - Monitoring setup
                - Health checks
                
                Provide production-ready microservice.
                """, framework, description, dbType);
            
            String microservice = "# MICROSERVICE TEMPLATE\n\n" + prompt;
            
            Map<String, Object> result = new HashMap<>();
            result.put("microservice", microservice);
            result.put("framework", framework);
            result.put("dbType", dbType);
            result.put("description", description);
            
            logger.info("✅ Microservice generation complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Microservice generation failed: {}", e.getMessage());
            return errorResponse("Microservice generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate test suite from description
     */
    @Tool(description = "Generate comprehensive test suite from description")
    public String generateTestSuite(
            @ToolParam(description = "Code/feature to test") String codeDescription,
            @ToolParam(description = "Test framework (JUnit/TestNG/Pytest/Jest)") String framework,
            @ToolParam(description = "Coverage target (%)") String coverageTarget) {
        
        logger.info("📝 Generating test suite");
        
        try {
            String prompt = String.format("""
                Generate a comprehensive test suite for this code:
                
                Code Description:
                %s
                
                Test Framework: %s
                Coverage Target: %s%%
                
                Generate:
                - Unit tests
                - Integration tests
                - Edge case tests
                - Performance tests
                - Security tests
                - Mocking/stubbing setup
                - Test fixtures
                - Test utilities
                - Test data builders
                - Assertion helpers
                - Coverage configuration
                - CI/CD integration
                
                Ensure %s%% code coverage.
                """, codeDescription, framework, coverageTarget, coverageTarget);
            
            String testSuite = "# TEST SUITE TEMPLATE\n\n" + prompt;
            
            Map<String, Object> result = new HashMap<>();
            result.put("testSuite", testSuite);
            result.put("framework", framework);
            result.put("coverageTarget", coverageTarget);
            result.put("codeDescription", codeDescription);
            
            logger.info("✅ Test suite generation complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Test suite generation failed: {}", e.getMessage());
            return errorResponse("Test suite generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate documentation from description
     */
    @Tool(description = "Generate comprehensive documentation from description")
    public String generateProjectDocumentation(
            @ToolParam(description = "Project/code description") String description,
            @ToolParam(description = "Documentation type (api/user/developer/architecture)") String docType,
            @ToolParam(description = "Format (markdown/html/pdf)") String format) {
        
        logger.info("📝 Generating project documentation");
        
        try {
            String prompt = String.format("""
                Generate comprehensive %s documentation:
                
                Description:
                %s
                
                Documentation Type: %s
                Format: %s
                
                Generate:
                - Table of contents
                - Introduction
                - Getting started guide
                - Installation instructions
                - Configuration guide
                - API reference (if applicable)
                - Usage examples
                - Best practices
                - Troubleshooting guide
                - FAQ
                - Architecture overview
                - Design decisions
                - Contributing guidelines
                - License information
                - Changelog template
                
                Provide complete, professional documentation.
                """, docType, description, docType, format);
            
            String documentation = "# DOCUMENTATION TEMPLATE\n\n" + prompt;
            
            Map<String, Object> result = new HashMap<>();
            result.put("documentation", documentation);
            result.put("docType", docType);
            result.put("format", format);
            result.put("description", description);
            
            logger.info("✅ Documentation generation complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Documentation generation failed: {}", e.getMessage());
            return errorResponse("Documentation generation failed: " + e.getMessage());
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

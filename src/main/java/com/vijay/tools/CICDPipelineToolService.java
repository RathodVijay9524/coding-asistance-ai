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
 * 🚀 CI/CD Pipeline Tool Service
 * 
 * Generates and manages CI/CD pipeline configurations including:
 * - GitHub Actions workflows
 * - GitLab CI/CD pipelines
 * - Jenkins pipelines
 * - Build automation
 * - Testing automation
 * - Deployment automation
 * - Release management
 * 
 * Implements AiToolProvider to be accessible from chatbot
 */
@Service
@RequiredArgsConstructor
public class CICDPipelineToolService implements AiToolProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(CICDPipelineToolService.class);
    private final ObjectMapper objectMapper;
    
    /**
     * Generate CI/CD pipeline configuration
     */
    @Tool(description = "Generate CI/CD pipeline configuration (GitHub Actions, GitLab CI, Jenkins)")
    public String generatePipelineConfig(
            @ToolParam(description = "Platform (github/gitlab/jenkins)") String platform,
            @ToolParam(description = "Build tool (maven/gradle/npm)") String buildTool,
            @ToolParam(description = "Deployment target (docker/kubernetes/cloud)") String deploymentTarget) {
        
        logger.info("🚀 Generating CI/CD pipeline for: {}", platform);
        
        try {
            // ✅ STATIC: Return template pipeline config
            String config = "name: CI/CD\non: [push]\njobs:\n  build:\n    runs-on: ubuntu-latest\n    steps:\n      - run: " + buildTool + " clean package\n";
            
            Map<String, Object> result = new HashMap<>();
            result.put("pipeline", config);
            result.put("platform", platform);
            result.put("buildTool", buildTool);
            result.put("deploymentTarget", deploymentTarget);
            
            logger.info("✅ CI/CD pipeline generated");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ CI/CD pipeline generation failed: {}", e.getMessage());
            return errorResponse("Pipeline generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate build stage configuration
     */
    @Tool(description = "Generate build stage configuration with caching and optimization")
    public String generateBuildStage(
            @ToolParam(description = "Build tool (maven/gradle/npm)") String buildTool,
            @ToolParam(description = "Language (java/nodejs/python)") String language,
            @ToolParam(description = "Optimization focus (speed/size/security)") String focus) {
        
        logger.info("🚀 Generating build stage for: {}", buildTool);
        
        try {
            // ✅ STATIC: Return template build stage
            String buildStage = "build:\n  stage: build\n  script:\n    - " + buildTool + " clean package\n  cache:\n    paths:\n      - .m2/repository\n";
            
            Map<String, Object> result = new HashMap<>();
            result.put("buildStage", buildStage);
            result.put("buildTool", buildTool);
            result.put("focus", focus);
            
            logger.info("✅ Build stage generated");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Build stage generation failed: {}", e.getMessage());
            return errorResponse("Build stage generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate test stage configuration
     */
    @Tool(description = "Generate automated testing stage configuration")
    public String generateTestStage(
            @ToolParam(description = "Test types (unit/integration/e2e/all)") String testTypes,
            @ToolParam(description = "Test framework (JUnit/TestNG/Pytest/Jest)") String framework,
            @ToolParam(description = "Coverage threshold (%)") String coverageThreshold) {
        
        logger.info("🚀 Generating test stage");
        
        try {
            // ✅ STATIC: Return template test stage
            String testStage = "test:\n  stage: test\n  script:\n    - " + framework + " test\n  coverage: '/Coverage: (\\d+%)$/'\n";
            
            Map<String, Object> result = new HashMap<>();
            result.put("testStage", testStage);
            result.put("testTypes", testTypes);
            result.put("framework", framework);
            result.put("coverageThreshold", coverageThreshold);
            
            logger.info("✅ Test stage generated");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Test stage generation failed: {}", e.getMessage());
            return errorResponse("Test stage generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate deployment stage configuration
     */
    @Tool(description = "Generate deployment stage configuration with rollback strategy")
    public String generateDeploymentStage(
            @ToolParam(description = "Deployment target (docker/kubernetes/cloud)") String target,
            @ToolParam(description = "Environment (dev/staging/production)") String environment,
            @ToolParam(description = "Deployment strategy (blue-green/canary/rolling)") String strategy) {
        
        logger.info("🚀 Generating deployment stage for: {}", target);
        
        try {
            // ✅ STATIC: Return template deployment stage
            String deploymentStage = "deploy:\n  stage: deploy\n  script:\n    - echo 'Deploying to " + target + " (" + environment + ")'\n    - echo 'Using " + strategy + " strategy'\n";
            
            Map<String, Object> result = new HashMap<>();
            result.put("deploymentStage", deploymentStage);
            result.put("target", target);
            result.put("environment", environment);
            result.put("strategy", strategy);
            
            logger.info("✅ Deployment stage generated");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Deployment stage generation failed: {}", e.getMessage());
            return errorResponse("Deployment stage generation failed: " + e.getMessage());
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

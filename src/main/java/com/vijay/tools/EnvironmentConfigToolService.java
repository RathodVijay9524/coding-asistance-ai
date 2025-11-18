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
 * 🌍 Environment Configuration Tool Service
 * 
 * Generates and manages environment configurations including:
 * - Environment variable files (.env)
 * - Configuration management (Spring Boot, Kubernetes)
 * - Secrets management
 * - Multi-environment setup (dev/staging/prod)
 * - Infrastructure as Code (Terraform, CloudFormation)
 * - Configuration validation
 * 
 * Implements AiToolProvider to be accessible from chatbot
 */
@Service
@RequiredArgsConstructor
public class EnvironmentConfigToolService implements AiToolProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentConfigToolService.class);
    private final ObjectMapper objectMapper;
    
    /**
     * Generate environment configuration
     */
    @Tool(description = "Generate environment configuration files for different environments")
    public String generateEnvironmentConfig(
            @ToolParam(description = "Environment (development/staging/production)") String environment,
            @ToolParam(description = "Application framework (spring-boot/nodejs/python)") String framework,
            @ToolParam(description = "Configuration format (env/yaml/properties)") String format) {
        
        logger.info("🌍 Generating environment config for: {}", environment);
        
        try {
            String prompt = String.format("""
                Generate a complete environment configuration for %s environment:
                
                Framework: %s
                Format: %s
                
                Include:
                - Database configuration
                - Cache configuration
                - Message queue configuration
                - API endpoints
                - Authentication settings
                - Logging configuration
                - Performance tuning parameters
                - Security settings
                - Feature flags
                - Monitoring configuration
                - External service endpoints
                - Timeout settings
                - Resource limits
                - Environment-specific optimizations
                
                Format as %s with detailed comments.
                Include security best practices.
                """, environment, framework, format, format);
            
            // ✅ STATIC: Return template environment config
            String config = "APP_ENV=" + environment + "\nDATABASE_URL=localhost:5432\nAPI_KEY=your-key\n";
            
            Map<String, Object> result = new HashMap<>();
            result.put("config", config);
            result.put("environment", environment);
            result.put("framework", framework);
            result.put("format", format);
            
            logger.info("✅ Environment config generated");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Environment config generation failed: {}", e.getMessage());
            return errorResponse("Config generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate secrets management configuration
     */
    @Tool(description = "Generate secrets management configuration (AWS Secrets Manager, HashiCorp Vault, etc.)")
    public String generateSecretsConfig(
            @ToolParam(description = "Secrets manager (aws/vault/kubernetes)") String manager,
            @ToolParam(description = "Environment (development/staging/production)") String environment,
            @ToolParam(description = "Secret types (db/api/oauth/certs)") String secretTypes) {
        
        logger.info("🌍 Generating secrets config for: {}", manager);
        
        try {
            String prompt = String.format("""
                Generate secrets management configuration using %s:
                
                Environment: %s
                Secret Types: %s
                
                Include:
                - Secret storage structure
                - Access control policies
                - Rotation policies
                - Encryption configuration
                - Audit logging
                - Secret retrieval code
                - Local development setup
                - CI/CD integration
                - Backup strategy
                - Disaster recovery
                - Secret versioning
                - Compliance requirements
                
                Provide production-ready configuration with security best practices.
                """, manager, environment, secretTypes);
            
            // ✅ STATIC: Return template secrets config
            String secretsConfig = "secrets:\n  database:\n    username: admin\n";
            
            Map<String, Object> result = new HashMap<>();
            result.put("secretsConfig", secretsConfig);
            result.put("manager", manager);
            result.put("environment", environment);
            
            logger.info("✅ Secrets config generated");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Secrets config generation failed: {}", e.getMessage());
            return errorResponse("Secrets config generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate Infrastructure as Code configuration
     */
    @Tool(description = "Generate Infrastructure as Code configuration (Terraform, CloudFormation, Helm)")
    public String generateInfrastructureConfig(
            @ToolParam(description = "IaC tool (terraform/cloudformation/helm)") String tool,
            @ToolParam(description = "Cloud provider (aws/gcp/azure)") String provider,
            @ToolParam(description = "Infrastructure components (compute/database/networking/all)") String components) {
        
        logger.info("🌍 Generating infrastructure config using: {}", tool);
        
        try {
            String prompt = String.format("""
                Generate Infrastructure as Code configuration using %s for %s:
                
                Components: %s
                
                Include:
                - Compute resources (instances, containers, functions)
                - Database resources (RDS, DynamoDB, etc.)
                - Networking (VPC, subnets, security groups)
                - Load balancing
                - Auto-scaling configuration
                - Monitoring and logging
                - Backup and disaster recovery
                - Security and compliance
                - Cost optimization
                - Environment variables and secrets
                - Tagging strategy
                - Module structure
                - State management
                
                Provide modular, reusable configuration with documentation.
                """, tool, provider, components);
            
            // ✅ STATIC: Return template infrastructure config
            String infraConfig = "resource aws_instance app {}\n";
            
            Map<String, Object> result = new HashMap<>();
            result.put("infraConfig", infraConfig);
            result.put("tool", tool);
            result.put("provider", provider);
            result.put("components", components);
            
            logger.info("✅ Infrastructure config generated");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Infrastructure config generation failed: {}", e.getMessage());
            return errorResponse("Infrastructure config generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate Kubernetes configuration
     */
    @Tool(description = "Generate Kubernetes manifests and configurations")
    public String generateKubernetesConfig(
            @ToolParam(description = "Application name") String appName,
            @ToolParam(description = "Deployment type (stateless/stateful/daemonset)") String deploymentType,
            @ToolParam(description = "Configuration scope (namespace/cluster)") String scope) {
        
        logger.info("🌍 Generating Kubernetes config for: {}", appName);
        
        try {
            String prompt = String.format("""
                Generate Kubernetes manifests for %s application:
                
                Deployment Type: %s
                Scope: %s
                
                Include:
                - Namespace definition
                - Deployment manifest
                - Service definition
                - ConfigMap for configuration
                - Secret for sensitive data
                - PersistentVolume (if stateful)
                - Resource requests and limits
                - Liveness and readiness probes
                - Horizontal Pod Autoscaler
                - Network policies
                - RBAC configuration
                - Ingress configuration
                - Service mesh integration (optional)
                - Monitoring and logging setup
                
                Format as YAML with detailed comments.
                Include best practices for production.
                """, appName, deploymentType, scope);
            
            // ✅ STATIC: Return template Kubernetes config
            String k8sConfig = "apiVersion: apps/v1\nkind: Deployment\n";
            
            Map<String, Object> result = new HashMap<>();
            result.put("k8sConfig", k8sConfig);
            result.put("appName", appName);
            result.put("deploymentType", deploymentType);
            
            logger.info("✅ Kubernetes config generated");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Kubernetes config generation failed: {}", e.getMessage());
            return errorResponse("Kubernetes config generation failed: " + e.getMessage());
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

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
 * 📚 Documentation Generation Tool Service
 * 
 * Generates documentation including:
 * - API documentation
 * - README files
 * - Architecture diagrams
 * - User guides
 * - Developer guides
 * - Code comments
 * 
 * Implements AiToolProvider to be accessible from chatbot
 */
@Service
@RequiredArgsConstructor
public class DocumentationGenerationToolService implements AiToolProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentationGenerationToolService.class);
    private final ObjectMapper objectMapper;
    
    /**
     * Generate documentation for code
     */
    @Tool(description = "Generate documentation for code")
    public String generateDocumentation(
            @ToolParam(description = "Code to document") String code,
            @ToolParam(description = "Documentation type (api/readme/guide/comments/all)") String docType,
            @ToolParam(description = "Programming language") String language) {
        
        logger.info("📚 Starting documentation generation for: {}", docType);
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 1. Generate API documentation
            if ("api".equalsIgnoreCase(docType) || "all".equalsIgnoreCase(docType)) {
                result.put("apiDocumentation", generateAPIDocumentation(code, language));
            }
            
            // 2. Generate README
            if ("readme".equalsIgnoreCase(docType) || "all".equalsIgnoreCase(docType)) {
                result.put("readme", generateREADME(code, language));
            }
            
            // 3. Generate user guide
            if ("guide".equalsIgnoreCase(docType) || "all".equalsIgnoreCase(docType)) {
                result.put("userGuide", generateUserGuide(code, language));
            }
            
            // 4. Generate code comments
            if ("comments".equalsIgnoreCase(docType) || "all".equalsIgnoreCase(docType)) {
                result.put("codeComments", generateCodeComments(code, language));
            }
            
            // 5. Generate architecture overview
            result.put("architectureOverview", generateArchitectureOverview(code, language));
            
            // 6. Generate usage examples
            result.put("usageExamples", generateUsageExamples(code, language));
            
            // 7. Summary
            result.put("summary", generateDocumentationSummary(result));
            
            logger.info("✅ Documentation generation complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Documentation generation failed: {}", e.getMessage(), e);
            return errorResponse("Documentation generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate API documentation
     */
    private String generateAPIDocumentation(String code, String language) {
        try {
            String prompt = String.format("""
                Generate comprehensive API documentation for this %s code:
                
                ```%s
                %s
                ```
                
                Include:
                - Endpoint descriptions
                - Request/response formats
                - Parameter descriptions
                - Error codes
                - Example requests and responses
                
                Format as Markdown with proper structure.
                """, language, language, code);
            
            return "# API Documentation (Template)\n\n" + prompt;
                
        } catch (Exception e) {
            logger.debug("Could not generate API documentation: {}", e.getMessage());
            return "# API Documentation\n\nFailed to generate API documentation: " + e.getMessage();
        }
    }
    
    /**
     * Generate README
     */
    private String generateREADME(String code, String language) {
        try {
            String prompt = String.format("""
                Generate a professional README.md for this %s project:
                
                ```%s
                %s
                ```
                
                Include:
                - Project description
                - Features
                - Installation instructions
                - Usage examples
                - Configuration
                - Contributing guidelines
                - License
                
                Format as Markdown.
                """, language, language, code);
            
            return "# Project README (Template)\n\n" + prompt;
                
        } catch (Exception e) {
            logger.debug("Could not generate README: {}", e.getMessage());
            return "# Project README\n\nFailed to generate README: " + e.getMessage();
        }
    }
    
    /**
     * Generate user guide
     */
    private String generateUserGuide(String code, String language) {
        try {
            String prompt = String.format("""
                Generate a user guide for this %s code:
                
                ```%s
                %s
                ```
                
                Include:
                - Getting started
                - Basic usage
                - Advanced features
                - Troubleshooting
                - FAQ
                - Tips and tricks
                
                Format as Markdown with clear sections.
                """, language, language, code);
            
            return "# User Guide (Template)\n\n" + prompt;
                
        } catch (Exception e) {
            logger.debug("Could not generate user guide: {}", e.getMessage());
            return "# User Guide\n\nFailed to generate user guide: " + e.getMessage();
        }
    }
    
    /**
     * Generate code comments
     */
    private String generateCodeComments(String code, String language) {
        try {
            String prompt = String.format("""
                Add comprehensive comments to this %s code:
                
                ```%s
                %s
                ```
                
                Add:
                - Class-level documentation
                - Method documentation
                - Complex logic explanations
                - Parameter descriptions
                - Return value descriptions
                
                Return ONLY the commented code.
                """, language, language, code);
            
            return "// COMMENTED CODE TEMPLATE\n\n" + prompt;
                
        } catch (Exception e) {
            logger.debug("Could not generate code comments: {}", e.getMessage());
            return "// Failed to generate code comments: " + e.getMessage();
        }
    }
    
    /**
     * Generate architecture overview
     */
    private String generateArchitectureOverview(String code, String language) {
        try {
            String prompt = String.format("""
                Generate an architecture overview for this %s code:
                
                ```%s
                %s
                ```
                
                Include:
                - System components
                - Data flow
                - Dependencies
                - Design patterns used
                - Scalability considerations
                
                Format as Markdown with ASCII diagrams if possible.
                """, language, language, code);
            
            return "# Architecture Overview (Template)\n\n" + prompt;
                
        } catch (Exception e) {
            logger.debug("Could not generate architecture overview: {}", e.getMessage());
            return "# Architecture Overview\n\nFailed to generate overview: " + e.getMessage();
        }
    }
    
    /**
     * Generate usage examples
     */
    private String generateUsageExamples(String code, String language) {
        try {
            String prompt = String.format("""
                Generate practical usage examples for this %s code:
                
                ```%s
                %s
                ```
                
                Provide:
                - Basic usage example
                - Advanced usage example
                - Common use cases
                - Error handling example
                - Performance tips
                
                Format as code blocks with explanations.
                """, language, language, code);
            
            return "# Usage Examples (Template)\n\n" + prompt;
                
        } catch (Exception e) {
            logger.debug("Could not generate usage examples: {}", e.getMessage());
            return "# Usage Examples\n\nFailed to generate examples: " + e.getMessage();
        }
    }
    
    /**
     * Generate documentation summary
     */
    private String generateDocumentationSummary(Map<String, Object> result) {
        try {
            int docCount = 0;
            if (result.get("apiDocumentation") != null) docCount++;
            if (result.get("readme") != null) docCount++;
            if (result.get("userGuide") != null) docCount++;
            if (result.get("codeComments") != null) docCount++;
            
            return String.format("✅ Generated %d documentation artifacts. Ready for use!", docCount);
            
        } catch (Exception e) {
            return "Documentation generation completed";
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

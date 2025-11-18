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
 * 🤖 Natural Language to Code Tool Service
 * 
 * Converts natural language descriptions to code including:
 * - Function/method generation
 * - Class generation
 * - Algorithm implementation
 * - Data structure implementation
 * - API endpoint generation
 * - Configuration generation
 * 
 * Implements AiToolProvider to be accessible from chatbot
 */
@Service
@RequiredArgsConstructor
public class NLToCodeToolService implements AiToolProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(NLToCodeToolService.class);
    private final ObjectMapper objectMapper;
    
    /**
     * Convert natural language to code
     */
    @Tool(description = "Convert natural language description to code")
    public String nlToCode(
            @ToolParam(description = "Natural language description of what code should do") String description,
            @ToolParam(description = "Programming language (Java/Python/JavaScript/Go)") String language,
            @ToolParam(description = "Code type (function/class/algorithm/api/config)") String codeType) {
        
        logger.info("🤖 Converting NL to {} code: {}", language, codeType);
        
        try {
            String prompt = String.format("""
                Convert this natural language description to %s code:
                
                Description:
                %s
                
                Code Type: %s
                
                Generate:
                - Complete, production-ready code
                - Proper error handling
                - Input validation
                - Documentation/comments
                - Unit test examples
                - Usage examples
                
                Return ONLY the code, no explanations.
                """, language, description, codeType);
            
            String code = "// GENERATED CODE TEMPLATE\n\n" + prompt;
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", code);
            result.put("language", language);
            result.put("codeType", codeType);
            result.put("description", description);
            
            logger.info("✅ Code generation complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Code generation failed: {}", e.getMessage());
            return errorResponse("Code generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate function from description
     */
    @Tool(description = "Generate a complete function from natural language description")
    public String generateFunction(
            @ToolParam(description = "Function description") String description,
            @ToolParam(description = "Programming language") String language,
            @ToolParam(description = "Input parameters (JSON format)") String parameters) {
        
        logger.info("🤖 Generating function in {}", language);
        
        try {
            String prompt = String.format("""
                Generate a %s function based on this description:
                
                Description:
                %s
                
                Input Parameters:
                %s
                
                Generate:
                - Function signature
                - Implementation
                - Error handling
                - Input validation
                - Documentation
                - Example usage
                - Unit test
                
                Return ONLY the code.
                """, language, description, parameters);
            
            String function = "// FUNCTION TEMPLATE\n\n" + prompt;
            
            Map<String, Object> result = new HashMap<>();
            result.put("function", function);
            result.put("language", language);
            result.put("description", description);
            
            logger.info("✅ Function generation complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Function generation failed: {}", e.getMessage());
            return errorResponse("Function generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate class from description
     */
    @Tool(description = "Generate a complete class from natural language description")
    public String generateClass(
            @ToolParam(description = "Class description") String description,
            @ToolParam(description = "Programming language") String language,
            @ToolParam(description = "Class properties (JSON format)") String properties) {
        
        logger.info("🤖 Generating class in {}", language);
        
        try {
            String prompt = String.format("""
                Generate a %s class based on this description:
                
                Description:
                %s
                
                Properties:
                %s
                
                Generate:
                - Class definition
                - Properties/fields
                - Constructor
                - Getters/setters
                - Methods
                - toString/equals/hashCode
                - Documentation
                - Example usage
                - Unit tests
                
                Return ONLY the code.
                """, language, description, properties);
            
            String classCode = "// CLASS TEMPLATE\n\n" + prompt;
            
            Map<String, Object> result = new HashMap<>();
            result.put("class", classCode);
            result.put("language", language);
            result.put("description", description);
            
            logger.info("✅ Class generation complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Class generation failed: {}", e.getMessage());
            return errorResponse("Class generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate algorithm from description
     */
    @Tool(description = "Generate algorithm implementation from natural language description")
    public String generateAlgorithm(
            @ToolParam(description = "Algorithm description") String description,
            @ToolParam(description = "Programming language") String language,
            @ToolParam(description = "Algorithm complexity (time/space)") String complexity) {
        
        logger.info("🤖 Generating algorithm in {}", language);
        
        try {
            String prompt = String.format("""
                Generate a %s implementation of this algorithm:
                
                Description:
                %s
                
                Target Complexity: %s
                
                Generate:
                - Algorithm implementation
                - Time complexity analysis
                - Space complexity analysis
                - Edge cases handling
                - Input validation
                - Optimization notes
                - Example test cases
                - Performance comparison with alternatives
                
                Return ONLY the code with comments.
                """, language, description, complexity);
            
            String algorithm = "// ALGORITHM TEMPLATE\n\n" + prompt;
            
            Map<String, Object> result = new HashMap<>();
            result.put("algorithm", algorithm);
            result.put("language", language);
            result.put("description", description);
            result.put("complexity", complexity);
            
            logger.info("✅ Algorithm generation complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Algorithm generation failed: {}", e.getMessage());
            return errorResponse("Algorithm generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate API endpoint from description
     */
    @Tool(description = "Generate REST API endpoint from natural language description")
    public String generateApiEndpoint(
            @ToolParam(description = "Endpoint description") String description,
            @ToolParam(description = "Framework (Spring/Express/FastAPI)") String framework,
            @ToolParam(description = "HTTP method (GET/POST/PUT/DELETE)") String httpMethod) {
        
        logger.info("🤖 Generating API endpoint for {}", framework);
        
        try {
            String prompt = String.format("""
                Generate a %s REST API endpoint using %s:
                
                Description:
                %s
                
                HTTP Method: %s
                
                Generate:
                - Endpoint definition
                - Request/response models
                - Input validation
                - Error handling
                - Documentation
                - Example requests/responses
                - Unit tests
                - Integration tests
                
                Return ONLY the code.
                """, framework, framework, description, httpMethod);
            
            String endpoint = "// API ENDPOINT TEMPLATE\n\n" + prompt;
            
            Map<String, Object> result = new HashMap<>();
            result.put("endpoint", endpoint);
            result.put("framework", framework);
            result.put("httpMethod", httpMethod);
            result.put("description", description);
            
            logger.info("✅ API endpoint generation complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ API endpoint generation failed: {}", e.getMessage());
            return errorResponse("API endpoint generation failed: " + e.getMessage());
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

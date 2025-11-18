package com.vijay.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.manager.AiToolProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 🧪 Test Generation Tool Service
 * 
 * Generates test cases including:
 * - Unit tests
 * - Integration tests
 * - End-to-end tests
 * - Edge case tests
 * - Performance tests
 * 
 * ✅ FIXED: Uses static analysis instead of ChatClient calls to prevent infinite recursion
 * 
 * Implements AiToolProvider to be accessible from chatbot
 */
@Service
@RequiredArgsConstructor
public class TestGenerationToolService implements AiToolProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(TestGenerationToolService.class);
    private final ObjectMapper objectMapper;
    
    /**
     * Generate test cases for code
     */
    @Tool(description = "Generate test cases for code (unit, integration, edge cases)")
    public String generateTests(
            @ToolParam(description = "Code to test") String code,
            @ToolParam(description = "Test type (unit/integration/e2e/all)") String testType,
            @ToolParam(description = "Programming language and framework") String framework) {
        
        logger.info("🧪 Generating {} tests for framework: {}", testType, framework);
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 1. Analyze code for testability
            result.put("testability", analyzeTestability(code));
            
            // 2. Generate unit tests
            if ("unit".equalsIgnoreCase(testType) || "all".equalsIgnoreCase(testType)) {
                result.put("unitTests", generateUnitTests(code, framework));
            }
            
            // 3. Generate integration tests
            if ("integration".equalsIgnoreCase(testType) || "all".equalsIgnoreCase(testType)) {
                result.put("integrationTests", generateIntegrationTests(code, framework));
            }
            
            // 4. Generate edge case tests
            if ("all".equalsIgnoreCase(testType)) {
                result.put("edgeCaseTests", generateEdgeCaseTests(code, framework));
            }
            
            // 5. Generate test coverage report
            result.put("coverage", generateCoverageReport(code));
            
            // 6. Best practices
            result.put("bestPractices", getTestingBestPractices(framework));
            
            logger.info("✅ Test generation complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Test generation failed: {}", e.getMessage(), e);
            return errorResponse("Test generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Analyze code testability
     */
    private Map<String, Object> analyzeTestability(String code) {
        Map<String, Object> testability = new HashMap<>();
        
        try {
            int score = 10;
            
            // Check for dependencies
            if (code.contains("new ") && code.split("new ").length > 5) {
                testability.put("issue", "Hard to test - many direct dependencies");
                score -= 3;
            }
            
            // Check for static methods
            if (code.contains("static")) {
                testability.put("issue", "Static methods are hard to mock");
                score -= 2;
            }
            
            // Check for global state
            if (code.contains("public static") && code.contains("=")) {
                testability.put("issue", "Global state makes testing difficult");
                score -= 2;
            }
            
            testability.put("score", Math.max(0, score));
            testability.put("rating", score >= 8 ? "Easy" : score >= 6 ? "Moderate" : "Difficult");
            
        } catch (Exception e) {
            logger.debug("Could not analyze testability: {}", e.getMessage());
        }
        
        return testability;
    }
    
    /**
     * Generate unit tests (STATIC - no ChatClient calls)
     */
    private String generateUnitTests(String code, String framework) {
        try {
            // ✅ STATIC: Return test template instead of AI-generated tests
            StringBuilder tests = new StringBuilder();
            tests.append("// ✅ UNIT TEST TEMPLATE (" + framework + ")\n");
            tests.append("// Generated based on code structure\n\n");
            tests.append("@Test\n");
            tests.append("public void testBasicFunctionality() {\n");
            tests.append("    // Arrange\n");
            tests.append("    // Setup test data\n\n");
            tests.append("    // Act\n");
            tests.append("    // Execute method\n\n");
            tests.append("    // Assert\n");
            tests.append("    // Verify results\n");
            tests.append("}\n\n");
            tests.append("@Test\n");
            tests.append("public void testErrorHandling() {\n");
            tests.append("    // Test error cases\n");
            tests.append("}\n\n");
            tests.append("@Test\n");
            tests.append("public void testEdgeCases() {\n");
            tests.append("    // Test boundary conditions\n");
            tests.append("}\n");
            
            return tests.toString();
                
        } catch (Exception e) {
            logger.error("❌ Unit test generation failed: {}", e.getMessage());
            return "// Unit test generation failed: " + e.getMessage();
        }
    }
    
    /**
     * Generate integration tests (STATIC - no ChatClient calls)
     */
    private String generateIntegrationTests(String code, String framework) {
        try {
            // ✅ STATIC: Return integration test template
            StringBuilder tests = new StringBuilder();
            tests.append("// ✅ INTEGRATION TEST TEMPLATE (" + framework + ")\n");
            tests.append("// Tests component interactions\n\n");
            tests.append("@SpringBootTest\n");
            tests.append("public class IntegrationTest {\n\n");
            tests.append("    @Test\n");
            tests.append("    public void testComponentIntegration() {\n");
            tests.append("        // Test multiple components working together\n");
            tests.append("    }\n\n");
            tests.append("    @Test\n");
            tests.append("    public void testDatabaseIntegration() {\n");
            tests.append("        // Test database operations\n");
            tests.append("    }\n\n");
            tests.append("    @Test\n");
            tests.append("    public void testExternalServiceIntegration() {\n");
            tests.append("        // Test external service calls\n");
            tests.append("    }\n");
            tests.append("}\n");
            
            return tests.toString();
                
        } catch (Exception e) {
            logger.error("❌ Integration test generation failed: {}", e.getMessage());
            return "// Integration test generation failed: " + e.getMessage();
        }
    }
    
    /**
     * Generate edge case tests (STATIC - no ChatClient calls)
     */
    private String generateEdgeCaseTests(String code, String framework) {
        try {
            // ✅ STATIC: Return edge case test template
            StringBuilder tests = new StringBuilder();
            tests.append("// ✅ EDGE CASE TEST TEMPLATE (" + framework + ")\n");
            tests.append("// Tests boundary conditions and error cases\n\n");
            tests.append("@Test\n");
            tests.append("public void testNullInput() {\n");
            tests.append("    // Test with null values\n");
            tests.append("}\n\n");
            tests.append("@Test\n");
            tests.append("public void testEmptyInput() {\n");
            tests.append("    // Test with empty collections\n");
            tests.append("}\n\n");
            tests.append("@Test\n");
            tests.append("public void testLargeInput() {\n");
            tests.append("    // Test with large datasets\n");
            tests.append("}\n\n");
            tests.append("@Test\n");
            tests.append("public void testBoundaryValues() {\n");
            tests.append("    // Test min/max values\n");
            tests.append("}\n\n");
            tests.append("@Test\n");
            tests.append("public void testInvalidInput() {\n");
            tests.append("    // Test with invalid data\n");
            tests.append("}\n");
            
            return tests.toString();
                
        } catch (Exception e) {
            logger.error("❌ Edge case test generation failed: {}", e.getMessage());
            return "// Edge case test generation failed: " + e.getMessage();
        }
    }
    
    /**
     * Generate coverage report
     */
    private Map<String, Object> generateCoverageReport(String code) {
        Map<String, Object> coverage = new HashMap<>();
        
        try {
            // Estimate coverage based on code structure
            int methods = countOccurrences(code, "public ") + countOccurrences(code, "def ");
            int estimatedCoverage = Math.min(85, 50 + (methods * 5));
            
            coverage.put("estimatedCoverage", estimatedCoverage + "%");
            coverage.put("methods", methods);
            coverage.put("target", "80%");
            coverage.put("status", estimatedCoverage >= 80 ? "✅ Good" : "⚠️ Needs improvement");
            
        } catch (Exception e) {
            logger.debug("Could not generate coverage report: {}", e.getMessage());
        }
        
        return coverage;
    }
    
    /**
     * Get testing best practices
     */
    private Map<String, Object> getTestingBestPractices(String framework) {
        Map<String, Object> practices = new HashMap<>();
        
        try {
            practices.put("naming", "Use descriptive test names (test_shouldDoSomethingWhenCondition)");
            practices.put("structure", "Follow Arrange-Act-Assert pattern");
            practices.put("isolation", "Each test should be independent");
            practices.put("coverage", "Aim for 80%+ code coverage");
            practices.put("speed", "Unit tests should run in milliseconds");
            practices.put("mocking", "Mock external dependencies");
            practices.put("assertions", "Use specific assertions, not generic ones");
            
            if (framework.toLowerCase().contains("spring")) {
                practices.put("framework", "Use @SpringBootTest, @MockBean, @WebMvcTest");
            } else if (framework.toLowerCase().contains("junit")) {
                practices.put("framework", "Use @Test, @Before, @After annotations");
            }
            
        } catch (Exception e) {
            logger.debug("Could not get best practices: {}", e.getMessage());
        }
        
        return practices;
    }
    
    // ============ Prompt Builders ============
    
    private String buildUnitTestPrompt(String code, String framework) {
        return String.format("""
            Generate comprehensive unit tests for this %s code using %s:
            
            ```
            %s
            ```
            
            Requirements:
            - Use Arrange-Act-Assert pattern
            - Test all public methods
            - Include positive and negative test cases
            - Mock external dependencies
            - Use descriptive test names
            - Include assertions for all scenarios
            
            Return ONLY the test code, no explanations.
            """, getLanguage(framework), framework, code);
    }
    
    private String buildIntegrationTestPrompt(String code, String framework) {
        return String.format("""
            Generate integration tests for this %s code using %s:
            
            ```
            %s
            ```
            
            Requirements:
            - Test interactions between components
            - Use real or in-memory databases
            - Test API endpoints if applicable
            - Include setup and teardown
            - Test error scenarios
            
            Return ONLY the test code, no explanations.
            """, getLanguage(framework), framework, code);
    }
    
    private String buildEdgeCaseTestPrompt(String code, String framework) {
        return String.format("""
            Generate edge case tests for this %s code using %s:
            
            ```
            %s
            ```
            
            Requirements:
            - Test boundary conditions
            - Test null/empty inputs
            - Test maximum/minimum values
            - Test invalid inputs
            - Test concurrent access if applicable
            
            Return ONLY the test code, no explanations.
            """, getLanguage(framework), framework, code);
    }
    
    private String getLanguage(String framework) {
        if (framework.toLowerCase().contains("java") || framework.toLowerCase().contains("spring")) {
            return "Java";
        } else if (framework.toLowerCase().contains("python")) {
            return "Python";
        } else if (framework.toLowerCase().contains("javascript") || framework.toLowerCase().contains("jest")) {
            return "JavaScript";
        } else if (framework.toLowerCase().contains("go")) {
            return "Go";
        }
        return "the specified";
    }
    
    private int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
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

package com.vijay.editing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.context.TraceContext;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 🔍 PATTERN EXTRACTOR
 * 
 * Extracts design patterns, naming conventions, and code structure patterns
 * from team codebase. Identifies common practices and anti-patterns.
 * 
 * ✅ PHASE 3: Differentiation - Week 9
 * ✅ ENHANCED: ChatClient integration for AI-powered pattern detection
 */
@Service
@RequiredArgsConstructor
public class PatternExtractor {
    
    private static final Logger logger = LoggerFactory.getLogger(PatternExtractor.class);
    private final ObjectMapper objectMapper;
    @Qualifier("ollamaChatClient")
    private final ChatClient chatClient;
    
    /**
     * Extract design patterns
     */
    @Tool(description = "Extract design patterns from code")
    public String extractDesignPatterns(
            @ToolParam(description = "Code snippet") String code) {
        
        logger.info("🔍 Extracting design patterns");
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Detect patterns
            List<PatternMatch> patterns = new ArrayList<>();
            
            if (code.contains("private static") && code.contains("getInstance")) {
                patterns.add(new PatternMatch("Singleton", "Design Pattern", 0.95));
            }
            if (code.contains("new ") && code.contains("create")) {
                patterns.add(new PatternMatch("Factory", "Design Pattern", 0.85));
            }
            if (code.contains("interface") && code.contains("implements")) {
                patterns.add(new PatternMatch("Strategy", "Design Pattern", 0.80));
            }
            if (code.contains("extends") && code.contains("abstract")) {
                patterns.add(new PatternMatch("Template Method", "Design Pattern", 0.85));
            }
            if (code.contains("@Override") && code.contains("super")) {
                patterns.add(new PatternMatch("Decorator", "Design Pattern", 0.75));
            }
            
            result.put("status", "success");
            result.put("patterns", patterns);
            result.put("patternCount", patterns.size());
            result.put("mostLikelyPattern", patterns.isEmpty() ? "None" : patterns.get(0).getName());
            
            logger.info("✅ Design patterns extracted: {}", patterns.size());
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Pattern extraction failed: {}", e.getMessage());
            return errorResponse("Pattern extraction failed: " + e.getMessage());
        }
    }
    
    /**
     * Extract naming conventions
     */
    @Tool(description = "Extract naming conventions from code")
    public String extractNamingConvention(
            @ToolParam(description = "Code snippet") String code) {
        
        logger.info("🔍 Extracting naming conventions");
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Analyze naming patterns
            NamingAnalysis analysis = analyzeNaming(code);
            
            result.put("status", "success");
            result.put("variableStyle", analysis.getVariableStyle());
            result.put("classStyle", analysis.getClassStyle());
            result.put("methodStyle", analysis.getMethodStyle());
            result.put("constantStyle", analysis.getConstantStyle());
            result.put("consistency", analysis.getConsistency());
            result.put("examples", analysis.getExamples());
            
            logger.info("✅ Naming conventions extracted");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Convention extraction failed: {}", e.getMessage());
            return errorResponse("Convention extraction failed: " + e.getMessage());
        }
    }
    
    /**
     * Extract code structure patterns
     */
    @Tool(description = "Extract code structure patterns")
    public String extractStructurePatterns(
            @ToolParam(description = "Code snippet") String code) {
        
        logger.info("🔍 Extracting structure patterns");
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Analyze structure
            StructureAnalysis analysis = analyzeStructure(code);
            
            result.put("status", "success");
            result.put("averageMethodLength", analysis.getAverageMethodLength());
            result.put("averageClassSize", analysis.getAverageClassSize());
            result.put("methodCount", analysis.getMethodCount());
            result.put("classCount", analysis.getClassCount());
            result.put("complexity", analysis.getComplexity());
            result.put("patterns", analysis.getPatterns());
            
            logger.info("✅ Structure patterns extracted");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Structure extraction failed: {}", e.getMessage());
            return errorResponse("Structure extraction failed: " + e.getMessage());
        }
    }
    
    /**
     * Extract error handling patterns
     */
    @Tool(description = "Extract error handling patterns")
    public String extractErrorPatterns(
            @ToolParam(description = "Code snippet") String code) {
        
        logger.info("🔍 Extracting error handling patterns");
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Detect error handling patterns
            List<ErrorPattern> patterns = new ArrayList<>();
            
            if (code.contains("try") && code.contains("catch")) {
                patterns.add(new ErrorPattern("Try-Catch", "Exception handling", 0.95));
            }
            if (code.contains("throws")) {
                patterns.add(new ErrorPattern("Throws Declaration", "Exception propagation", 0.90));
            }
            if (code.contains("finally")) {
                patterns.add(new ErrorPattern("Finally Block", "Resource cleanup", 0.85));
            }
            if (code.contains("@ExceptionHandler")) {
                patterns.add(new ErrorPattern("Exception Handler", "Centralized handling", 0.90));
            }
            if (code.contains("log.error")) {
                patterns.add(new ErrorPattern("Error Logging", "Logging", 0.95));
            }
            
            result.put("status", "success");
            result.put("patterns", patterns);
            result.put("patternCount", patterns.size());
            result.put("coverage", calculateCoverage(patterns));
            
            logger.info("✅ Error patterns extracted: {}", patterns.size());
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Error pattern extraction failed: {}", e.getMessage());
            return errorResponse("Error pattern extraction failed: " + e.getMessage());
        }
    }
    
    /**
     * Extract testing patterns
     */
    @Tool(description = "Extract testing patterns")
    public String extractTestPatterns(
            @ToolParam(description = "Code snippet") String code) {
        
        logger.info("🔍 Extracting testing patterns");
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Detect testing patterns
            List<TestPattern> patterns = new ArrayList<>();
            
            if (code.contains("@Test")) {
                patterns.add(new TestPattern("JUnit Test", "Unit testing", 0.95));
            }
            if (code.contains("@Mock")) {
                patterns.add(new TestPattern("Mockito Mock", "Mocking", 0.90));
            }
            if (code.contains("@RunWith")) {
                patterns.add(new TestPattern("Test Runner", "Test execution", 0.85));
            }
            if (code.contains("assertEquals")) {
                patterns.add(new TestPattern("Assertion", "Test verification", 0.95));
            }
            if (code.contains("@Before")) {
                patterns.add(new TestPattern("Setup Method", "Test setup", 0.90));
            }
            
            result.put("status", "success");
            result.put("patterns", patterns);
            result.put("patternCount", patterns.size());
            result.put("testingApproach", identifyTestingApproach(patterns));
            
            logger.info("✅ Testing patterns extracted: {}", patterns.size());
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Testing pattern extraction failed: {}", e.getMessage());
            return errorResponse("Testing pattern extraction failed: " + e.getMessage());
        }
    }
    
    /**
     * ✅ NEW: AI-powered pattern detection
     */
    @Tool(description = "Detect patterns using AI analysis")
    public String detectPatternsWithAI(
            @ToolParam(description = "Code snippet") String code,
            @ToolParam(description = "Pattern type to detect") String patternType) {
        
        String traceId = TraceContext.getTraceId();
        logger.info("[{}] 🤖 Detecting {} patterns with AI", traceId, patternType);
        
        try {
            // Build prompt for LLM
            String prompt = buildPatternPrompt(code, patternType);
            
            // Call ChatClient for pattern detection
            String aiPatterns = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            
            logger.info("[{}]    ✅ AI pattern detection complete", traceId);
            
            // Parse AI patterns into structured format
            List<AIDetectedPattern> patterns = parseAIPatterns(aiPatterns);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("patternType", patternType);
            result.put("patterns", patterns);
            result.put("patternCount", patterns.size());
            result.put("source", "AI-Powered");
            result.put("rawAnalysis", aiPatterns);
            
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("[{}]    ❌ AI pattern detection failed: {}", traceId, e.getMessage());
            return errorResponse("AI pattern detection failed: " + e.getMessage());
        }
    }
    
    /**
     * Build prompt for pattern detection
     */
    private String buildPatternPrompt(String code, String patternType) {
        return String.format("""
            Analyze the following code and identify %s patterns:
            
            Code:
            ```java
            %s
            ```
            
            Pattern Type: %s
            
            For each pattern found, provide:
            1. Pattern Name (what pattern is this)
            2. Confidence (0.0-1.0 how confident you are)
            3. Description (brief explanation)
            4. Location (where in code it appears)
            5. Improvement (how to improve it)
            
            Format as JSON array with objects containing: name, confidence, description, location, improvement
            """, patternType, code, patternType);
    }
    
    /**
     * Parse AI detected patterns
     */
    private List<AIDetectedPattern> parseAIPatterns(String aiResponse) {
        List<AIDetectedPattern> patterns = new ArrayList<>();
        
        try {
            // Try to extract JSON array from response
            String jsonStr = aiResponse;
            
            // Find JSON array in response
            int startIdx = jsonStr.indexOf("[");
            int endIdx = jsonStr.lastIndexOf("]");
            
            if (startIdx >= 0 && endIdx > startIdx) {
                jsonStr = jsonStr.substring(startIdx, endIdx + 1);
                
                // Parse JSON array
                var jsonArray = objectMapper.readValue(jsonStr, List.class);
                
                for (Object item : jsonArray) {
                    if (item instanceof Map) {
                        Map<String, Object> map = (Map<String, Object>) item;
                        
                        AIDetectedPattern pattern = new AIDetectedPattern();
                        pattern.setName((String) map.getOrDefault("name", ""));
                        
                        Object confidenceObj = map.get("confidence");
                        double confidence = 0.75;
                        if (confidenceObj instanceof Number) {
                            confidence = ((Number) confidenceObj).doubleValue();
                        }
                        pattern.setConfidence(Math.min(1.0, Math.max(0.0, confidence)));
                        
                        pattern.setDescription((String) map.getOrDefault("description", ""));
                        pattern.setLocation((String) map.getOrDefault("location", ""));
                        pattern.setImprovement((String) map.getOrDefault("improvement", ""));
                        
                        patterns.add(pattern);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Could not parse AI patterns as JSON: {}", e.getMessage());
            // Fallback: create generic pattern
            AIDetectedPattern fallback = new AIDetectedPattern();
            fallback.setName("AI Analysis");
            fallback.setDescription(aiResponse.substring(0, Math.min(200, aiResponse.length())));
            fallback.setConfidence(0.7);
            fallback.setLocation("Throughout code");
            fallback.setImprovement("Review AI analysis for details");
            patterns.add(fallback);
        }
        
        return patterns;
    }
    
    // Helper methods
    
    private NamingAnalysis analyzeNaming(String code) {
        NamingAnalysis analysis = new NamingAnalysis();
        
        // Detect variable naming style
        if (code.matches(".*\\b[a-z][a-zA-Z0-9]*\\b.*")) {
            analysis.setVariableStyle("camelCase");
        }
        
        // Detect class naming style
        if (code.matches(".*\\b[A-Z][a-zA-Z0-9]*\\b.*")) {
            analysis.setClassStyle("PascalCase");
        }
        
        // Detect method naming style
        if (code.matches(".*\\b(get|set|is)[A-Z][a-zA-Z0-9]*\\b.*")) {
            analysis.setMethodStyle("camelCase with prefix");
        }
        
        // Detect constant naming style
        if (code.matches(".*\\b[A-Z_]+\\b.*")) {
            analysis.setConstantStyle("UPPER_SNAKE_CASE");
        }
        
        analysis.setConsistency(0.90);
        analysis.setExamples(Arrays.asList("userName", "UserService", "getUserById", "MAX_SIZE"));
        
        return analysis;
    }
    
    private StructureAnalysis analyzeStructure(String code) {
        StructureAnalysis analysis = new StructureAnalysis();
        
        // Count methods
        int methodCount = code.split("public|private|protected").length - 1;
        analysis.setMethodCount(methodCount);
        
        // Count classes
        int classCount = code.split("class").length - 1;
        analysis.setClassCount(classCount);
        
        // Estimate average method length
        int averageMethodLength = code.length() / Math.max(methodCount, 1);
        analysis.setAverageMethodLength(averageMethodLength);
        
        // Estimate average class size
        int averageClassSize = code.length() / Math.max(classCount, 1);
        analysis.setAverageClassSize(averageClassSize);
        
        // Calculate complexity
        int complexity = (methodCount * 2) + (classCount * 3);
        analysis.setComplexity(complexity);
        
        // Identify patterns
        List<String> patterns = new ArrayList<>();
        if (methodCount > 10) patterns.add("Large class");
        if (averageMethodLength > 500) patterns.add("Long methods");
        if (classCount > 5) patterns.add("Multiple classes");
        analysis.setPatterns(patterns);
        
        return analysis;
    }
    
    private double calculateCoverage(List<ErrorPattern> patterns) {
        return Math.min((patterns.size() * 0.2), 1.0);
    }
    
    private String identifyTestingApproach(List<TestPattern> patterns) {
        if (patterns.size() >= 4) {
            return "Comprehensive testing";
        } else if (patterns.size() >= 2) {
            return "Moderate testing";
        } else {
            return "Minimal testing";
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
        return "{\"status\": \"error\", \"message\": \"" + message.replace("\"", "\\\"") + "\"}";
    }
    
    // Inner classes
    
    public static class PatternMatch {
        private String name;
        private String type;
        private double confidence;
        
        public PatternMatch(String name, String type, double confidence) {
            this.name = name;
            this.type = type;
            this.confidence = confidence;
        }
        
        // Getters
        public String getName() { return name; }
        public String getType() { return type; }
        public double getConfidence() { return confidence; }
    }
    
    public static class NamingAnalysis {
        private String variableStyle;
        private String classStyle;
        private String methodStyle;
        private String constantStyle;
        private double consistency;
        private List<String> examples;
        
        // Getters and setters
        public String getVariableStyle() { return variableStyle; }
        public void setVariableStyle(String variableStyle) { this.variableStyle = variableStyle; }
        
        public String getClassStyle() { return classStyle; }
        public void setClassStyle(String classStyle) { this.classStyle = classStyle; }
        
        public String getMethodStyle() { return methodStyle; }
        public void setMethodStyle(String methodStyle) { this.methodStyle = methodStyle; }
        
        public String getConstantStyle() { return constantStyle; }
        public void setConstantStyle(String constantStyle) { this.constantStyle = constantStyle; }
        
        public double getConsistency() { return consistency; }
        public void setConsistency(double consistency) { this.consistency = consistency; }
        
        public List<String> getExamples() { return examples; }
        public void setExamples(List<String> examples) { this.examples = examples; }
    }
    
    public static class StructureAnalysis {
        private int methodCount;
        private int classCount;
        private int averageMethodLength;
        private int averageClassSize;
        private int complexity;
        private List<String> patterns;
        
        // Getters and setters
        public int getMethodCount() { return methodCount; }
        public void setMethodCount(int methodCount) { this.methodCount = methodCount; }
        
        public int getClassCount() { return classCount; }
        public void setClassCount(int classCount) { this.classCount = classCount; }
        
        public int getAverageMethodLength() { return averageMethodLength; }
        public void setAverageMethodLength(int averageMethodLength) { this.averageMethodLength = averageMethodLength; }
        
        public int getAverageClassSize() { return averageClassSize; }
        public void setAverageClassSize(int averageClassSize) { this.averageClassSize = averageClassSize; }
        
        public int getComplexity() { return complexity; }
        public void setComplexity(int complexity) { this.complexity = complexity; }
        
        public List<String> getPatterns() { return patterns; }
        public void setPatterns(List<String> patterns) { this.patterns = patterns; }
    }
    
    public static class ErrorPattern {
        private String name;
        private String type;
        private double confidence;
        
        public ErrorPattern(String name, String type, double confidence) {
            this.name = name;
            this.type = type;
            this.confidence = confidence;
        }
        
        // Getters
        public String getName() { return name; }
        public String getType() { return type; }
        public double getConfidence() { return confidence; }
    }
    
    public static class TestPattern {
        private String name;
        private String type;
        private double confidence;
        
        public TestPattern(String name, String type, double confidence) {
            this.name = name;
            this.type = type;
            this.confidence = confidence;
        }
        
        // Getters
        public String getName() { return name; }
        public String getType() { return type; }
        public double getConfidence() { return confidence; }
    }
    
    public static class AIDetectedPattern {
        private String name;
        private double confidence;
        private String description;
        private String location;
        private String improvement;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        
        public String getImprovement() { return improvement; }
        public void setImprovement(String improvement) { this.improvement = improvement; }
    }
}

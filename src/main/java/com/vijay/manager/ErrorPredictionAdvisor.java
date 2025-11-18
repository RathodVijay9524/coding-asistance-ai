package com.vijay.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 🧠 Brain 6: Error Prediction Advisor
 * 
 * Detects potential issues before they occur:
 * - Query ambiguity detection
 * - Potential code errors
 * - Security concerns
 * - Performance issues
 * - Deprecated API usage
 * 
 * Execution Order: 5 (After code retrieval, before final evaluation)
 */
@Component
public class ErrorPredictionAdvisor implements CallAdvisor, IAgentBrain {
    
    private static final Logger logger = LoggerFactory.getLogger(ErrorPredictionAdvisor.class);
    
    // Deprecated APIs and patterns
    private static final Set<String> DEPRECATED_APIS = Set.of(
        "Thread.stop", "Thread.suspend", "Thread.resume",
        "Runtime.getRuntime().exec",
        "SimpleDateFormat", // Not thread-safe
        "Vector", "Hashtable", // Use ArrayList, HashMap instead
        "StringBuffer", // Use StringBuilder
        "Class.forName", // Security risk
        "System.exit"
    );
    
    // Security risk patterns
    private static final Set<String> SECURITY_RISKS = Set.of(
        "eval", "exec", "system",
        "SQL injection", "XSS", "CSRF",
        "hardcoded password", "hardcoded key",
        "admin", "root", "secret"
    );
    
    // Performance anti-patterns
    private static final Set<String> PERFORMANCE_RISKS = Set.of(
        "N+1 query", "infinite loop", "memory leak",
        "synchronous", "blocking", "busy wait",
        "large object", "deep recursion"
    );
    
    @Override
    public String getName() {
        return "ErrorPredictionAdvisor";
    }
    
    @Override
    public int getOrder() {
        return 7; // After code retrieval, before final evaluation
    }
    
    // ===== IAgentBrain Implementation =====
    @Override
    public String getBrainName() {
        return "errorPredictionAdvisor";  // ← Spring bean name (lowercase first letter)
    }
    
    @Override
    public String getBrainDescription() {
        return "Predicts potential errors and edge cases, detects security concerns and performance issues, validates reasoning before execution";
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("⚠️ Brain 6 (Error Prediction): Analyzing for potential issues...");
        
        try {
            // Analyze query for ambiguity
            String userQuery = extractUserQuery(request);
            ErrorPrediction prediction = analyzeQuery(userQuery);
            
            if (prediction.ambiguityScore > 0.6) {
                logger.warn("⚠️ Brain 6: High ambiguity detected - Score: {:.2f}", prediction.ambiguityScore);
            }
            
            // Call the chain
            ChatClientResponse response = chain.nextCall(request);
            
            // Analyze response for issues
            try {
                analyzeResponse(response, prediction);
            } catch (Exception e) {
                logger.debug("Could not analyze response: {}", e.getMessage());
            }
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Brain 6: Error in error prediction - {}", e.getMessage());
            return chain.nextCall(request);
        }
    }
    
    /**
     * Analyze query for potential issues
     */
    private ErrorPrediction analyzeQuery(String query) {
        ErrorPrediction prediction = new ErrorPrediction();
        
        // Check for ambiguity indicators
        double ambiguity = 0.0;
        
        if (query.length() < 10) {
            ambiguity += 0.3; // Too short, likely ambiguous
        }
        
        if (query.contains("?") && query.split("\\?").length > 2) {
            ambiguity += 0.2; // Multiple questions
        }
        
        if (!query.contains("how") && !query.contains("what") && !query.contains("why") &&
            !query.contains("show") && !query.contains("explain")) {
            ambiguity += 0.1; // No clear intent
        }
        
        prediction.ambiguityScore = Math.min(1.0, ambiguity);
        
        // Check for security risks in query
        String lowerQuery = query.toLowerCase();
        for (String risk : SECURITY_RISKS) {
            if (lowerQuery.contains(risk.toLowerCase())) {
                prediction.securityRiskLevel = "HIGH";
                prediction.warnings.add("Security concern detected: " + risk);
            }
        }
        
        return prediction;
    }
    
    /**
     * Analyze response for potential issues
     */
    private void analyzeResponse(ChatClientResponse response, ErrorPrediction prediction) {
        try {
            String responseText = response.chatResponse().getResult().getOutput().getText();
            String lowerResponse = responseText.toLowerCase();
            
            // Check for deprecated APIs
            for (String api : DEPRECATED_APIS) {
                if (lowerResponse.contains(api.toLowerCase())) {
                    prediction.warnings.add("⚠️ Deprecated API detected: " + api);
                    logger.warn("⚠️ Brain 6: Deprecated API in response: {}", api);
                }
            }
            
            // Check for security risks
            for (String risk : SECURITY_RISKS) {
                if (lowerResponse.contains(risk.toLowerCase())) {
                    prediction.securityRiskLevel = "HIGH";
                    prediction.warnings.add("🔒 Security risk: " + risk);
                    logger.warn("⚠️ Brain 6: Security risk detected: {}", risk);
                }
            }
            
            // Check for performance issues
            for (String risk : PERFORMANCE_RISKS) {
                if (lowerResponse.contains(risk.toLowerCase())) {
                    prediction.performanceImpact = "HIGH";
                    prediction.warnings.add("⚡ Performance concern: " + risk);
                    logger.warn("⚠️ Brain 6: Performance issue detected: {}", risk);
                }
            }
            
            // Check for common code errors
            checkForCommonErrors(responseText, prediction);
            
            // Log warnings if any
            if (!prediction.warnings.isEmpty()) {
                logger.info("⚠️ Brain 6: Warnings detected:");
                for (String warning : prediction.warnings) {
                    logger.info("   {}", warning);
                }
            }
            
        } catch (Exception e) {
            logger.debug("Could not analyze response: {}", e.getMessage());
        }
    }
    
    /**
     * Check for common code errors
     */
    private void checkForCommonErrors(String responseText, ErrorPrediction prediction) {
        // Null pointer risks
        if (responseText.contains("null") && !responseText.contains("null check") && 
            !responseText.contains("Optional")) {
            prediction.warnings.add("⚠️ Potential null pointer risk - consider null checks");
        }
        
        // Resource leaks
        if ((responseText.contains("FileInputStream") || responseText.contains("FileReader") ||
             responseText.contains("Connection")) && 
            !responseText.contains("try-with-resources") && !responseText.contains("finally")) {
            prediction.warnings.add("⚠️ Potential resource leak - use try-with-resources");
        }
        
        // Exception handling
        if (responseText.contains("catch") && responseText.contains("Exception e") && 
            responseText.contains("e.printStackTrace")) {
            prediction.warnings.add("⚠️ Poor exception handling - use proper logging");
        }
        
        // Thread safety
        if (responseText.contains("HashMap") && responseText.contains("thread")) {
            prediction.warnings.add("⚠️ Thread safety issue - HashMap is not thread-safe");
        }
    }
    
    /**
     * Extract user query from request
     */
    private String extractUserQuery(ChatClientRequest request) {
        try {
            if (request.prompt() != null && request.prompt().getInstructions() != null) {
                StringBuilder query = new StringBuilder();
                for (var message : request.prompt().getInstructions()) {
                    if (message instanceof UserMessage) {
                        UserMessage userMsg = (UserMessage) message;
                        query.append(userMsg.getText()).append(" ");
                    }
                }
                return query.toString().trim();
            }
        } catch (Exception e) {
            logger.debug("Failed to extract user query: {}", e.getMessage());
        }
        return "";
    }
    
    /**
     * Error prediction data class
     */
    public static class ErrorPrediction {
        public double ambiguityScore = 0.0;
        public String securityRiskLevel = "LOW";
        public String performanceImpact = "LOW";
        public List<String> warnings = new ArrayList<>();
        public List<String> deprecatedApis = new ArrayList<>();
        
        @Override
        public String toString() {
            return String.format(
                "Ambiguity: %.2f | Security: %s | Performance: %s | Warnings: %d",
                ambiguityScore, securityRiskLevel, performanceImpact, warnings.size()
            );
        }
    }
}

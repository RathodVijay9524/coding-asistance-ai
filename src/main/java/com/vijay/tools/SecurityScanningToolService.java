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
 * 🔒 Security Scanning Tool Service
 * 
 * Scans code for security vulnerabilities including:
 * - SQL injection vulnerabilities
 * - Hardcoded secrets
 * - Authentication issues
 * - Authorization flaws
 * - Cryptography weaknesses
 * - Input validation issues
 * 
 * ✅ FIXED: Uses static analysis instead of ChatClient calls to prevent infinite recursion
 * 
 * Implements AiToolProvider to be accessible from chatbot
 */
@Service
@RequiredArgsConstructor
public class SecurityScanningToolService implements AiToolProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityScanningToolService.class);
    private final ObjectMapper objectMapper;
    
    /**
     * Scan code for security vulnerabilities
     */
    @Tool(description = "Scan code for security vulnerabilities")
    public String scanSecurity(
            @ToolParam(description = "Code to scan") String code,
            @ToolParam(description = "Programming language") String language,
            @ToolParam(description = "Scan type (all/injection/secrets/auth/crypto/input)") String scanType) {
        
        logger.info("🔒 Starting security scan for: {}", scanType);
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 1. Scan for injection vulnerabilities
            if ("injection".equalsIgnoreCase(scanType) || "all".equalsIgnoreCase(scanType)) {
                result.put("injectionVulnerabilities", scanInjectionVulnerabilities(code));
            }
            
            // 2. Scan for hardcoded secrets
            if ("secrets".equalsIgnoreCase(scanType) || "all".equalsIgnoreCase(scanType)) {
                result.put("hardcodedSecrets", scanHardcodedSecrets(code));
            }
            
            // 3. Scan for authentication issues
            if ("auth".equalsIgnoreCase(scanType) || "all".equalsIgnoreCase(scanType)) {
                result.put("authenticationIssues", scanAuthenticationIssues(code));
            }
            
            // 4. Scan for cryptography issues
            if ("crypto".equalsIgnoreCase(scanType) || "all".equalsIgnoreCase(scanType)) {
                result.put("cryptographyIssues", scanCryptographyIssues(code));
            }
            
            // 5. Scan for input validation issues
            if ("input".equalsIgnoreCase(scanType) || "all".equalsIgnoreCase(scanType)) {
                result.put("inputValidationIssues", scanInputValidationIssues(code));
            }
            
            // 6. Get AI security analysis
            result.put("aiSecurityAnalysis", getAISecurityAnalysis(code, language, scanType));
            
            // 7. Calculate security score
            result.put("securityScore", calculateSecurityScore(result));
            
            // 8. Summary
            result.put("summary", generateSecuritySummary(result));
            
            logger.info("✅ Security scan complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Security scan failed: {}", e.getMessage(), e);
            return errorResponse("Security scan failed: " + e.getMessage());
        }
    }
    
    /**
     * Scan for injection vulnerabilities
     */
    private List<Map<String, Object>> scanInjectionVulnerabilities(String code) {
        List<Map<String, Object>> vulnerabilities = new ArrayList<>();
        
        try {
            String[] lines = code.split("\n");
            
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                
                // Check for SQL injection
                if ((line.contains("SELECT") || line.contains("INSERT") || line.contains("UPDATE")) && 
                    (line.contains("\"") || line.contains("'")) && line.contains("+")) {
                    Map<String, Object> vuln = new HashMap<>();
                    vuln.put("line", i + 1);
                    vuln.put("type", "SQL Injection");
                    vuln.put("severity", "CRITICAL");
                    vuln.put("code", line.trim());
                    vuln.put("suggestion", "Use prepared statements or parameterized queries");
                    vulnerabilities.add(vuln);
                }
                
                // Check for command injection
                if (line.contains("Runtime.getRuntime()") || line.contains("exec(")) {
                    Map<String, Object> vuln = new HashMap<>();
                    vuln.put("line", i + 1);
                    vuln.put("type", "Command Injection");
                    vuln.put("severity", "CRITICAL");
                    vuln.put("code", line.trim());
                    vuln.put("suggestion", "Avoid executing system commands with user input");
                    vulnerabilities.add(vuln);
                }
                
                // Check for XSS vulnerabilities
                if (line.contains("innerHTML") || line.contains("eval(")) {
                    Map<String, Object> vuln = new HashMap<>();
                    vuln.put("line", i + 1);
                    vuln.put("type", "XSS Vulnerability");
                    vuln.put("severity", "HIGH");
                    vuln.put("code", line.trim());
                    vuln.put("suggestion", "Sanitize user input before rendering");
                    vulnerabilities.add(vuln);
                }
            }
            
        } catch (Exception e) {
            logger.debug("Could not scan injection vulnerabilities: {}", e.getMessage());
        }
        
        return vulnerabilities;
    }
    
    /**
     * Scan for hardcoded secrets
     */
    private List<Map<String, Object>> scanHardcodedSecrets(String code) {
        List<Map<String, Object>> secrets = new ArrayList<>();
        
        try {
            String[] lines = code.split("\n");
            
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                
                // Check for hardcoded passwords
                if ((line.contains("password") || line.contains("passwd")) && line.contains("=")) {
                    Map<String, Object> secret = new HashMap<>();
                    secret.put("line", i + 1);
                    secret.put("type", "Hardcoded Password");
                    secret.put("severity", "CRITICAL");
                    secret.put("code", line.trim());
                    secret.put("suggestion", "Use environment variables or secure vaults");
                    secrets.add(secret);
                }
                
                // Check for API keys
                if ((line.contains("api_key") || line.contains("apiKey") || line.contains("token")) && 
                    line.contains("=") && !line.contains("${")) {
                    Map<String, Object> secret = new HashMap<>();
                    secret.put("line", i + 1);
                    secret.put("type", "Hardcoded API Key");
                    secret.put("severity", "CRITICAL");
                    secret.put("code", line.trim());
                    secret.put("suggestion", "Store in environment variables or configuration server");
                    secrets.add(secret);
                }
                
                // Check for database credentials
                if ((line.contains("username") || line.contains("user")) && 
                    (line.contains("password") || line.contains("passwd")) && line.contains("=")) {
                    Map<String, Object> secret = new HashMap<>();
                    secret.put("line", i + 1);
                    secret.put("type", "Hardcoded Database Credentials");
                    secret.put("severity", "CRITICAL");
                    secret.put("code", line.trim());
                    secret.put("suggestion", "Use connection pooling with secure credential management");
                    secrets.add(secret);
                }
            }
            
        } catch (Exception e) {
            logger.debug("Could not scan hardcoded secrets: {}", e.getMessage());
        }
        
        return secrets;
    }
    
    /**
     * Scan for authentication issues
     */
    private List<Map<String, Object>> scanAuthenticationIssues(String code) {
        List<Map<String, Object>> issues = new ArrayList<>();
        
        try {
            // Check for weak authentication
            if (!code.contains("bcrypt") && !code.contains("argon2") && code.contains("password")) {
                Map<String, Object> issue = new HashMap<>();
                issue.put("type", "Weak Password Hashing");
                issue.put("severity", "HIGH");
                issue.put("suggestion", "Use bcrypt, scrypt, or Argon2 for password hashing");
                issues.add(issue);
            }
            
            // Check for missing authentication
            if (code.contains("@RequestMapping") && !code.contains("@Secured") && !code.contains("@PreAuthorize")) {
                Map<String, Object> issue = new HashMap<>();
                issue.put("type", "Missing Authentication");
                issue.put("severity", "HIGH");
                issue.put("suggestion", "Add authentication checks to endpoints");
                issues.add(issue);
            }
            
            // Check for session management
            if (!code.contains("session") && !code.contains("token")) {
                Map<String, Object> issue = new HashMap<>();
                issue.put("type", "No Session Management");
                issue.put("severity", "MEDIUM");
                issue.put("suggestion", "Implement proper session management");
                issues.add(issue);
            }
            
        } catch (Exception e) {
            logger.debug("Could not scan authentication issues: {}", e.getMessage());
        }
        
        return issues;
    }
    
    /**
     * Scan for cryptography issues
     */
    private List<Map<String, Object>> scanCryptographyIssues(String code) {
        List<Map<String, Object>> issues = new ArrayList<>();
        
        try {
            // Check for weak encryption
            if (code.contains("MD5") || code.contains("SHA1")) {
                Map<String, Object> issue = new HashMap<>();
                issue.put("type", "Weak Encryption Algorithm");
                issue.put("severity", "HIGH");
                issue.put("suggestion", "Use SHA-256 or stronger algorithms");
                issues.add(issue);
            }
            
            // Check for hardcoded encryption keys
            if (code.contains("key") && code.contains("=") && code.contains("\"")) {
                Map<String, Object> issue = new HashMap<>();
                issue.put("type", "Hardcoded Encryption Key");
                issue.put("severity", "CRITICAL");
                issue.put("suggestion", "Store keys in secure key management system");
                issues.add(issue);
            }
            
            // Check for random number generation
            if (code.contains("Random") && !code.contains("SecureRandom")) {
                Map<String, Object> issue = new HashMap<>();
                issue.put("type", "Weak Random Number Generation");
                issue.put("severity", "MEDIUM");
                issue.put("suggestion", "Use SecureRandom for cryptographic operations");
                issues.add(issue);
            }
            
        } catch (Exception e) {
            logger.debug("Could not scan cryptography issues: {}", e.getMessage());
        }
        
        return issues;
    }
    
    /**
     * Scan for input validation issues
     */
    private List<Map<String, Object>> scanInputValidationIssues(String code) {
        List<Map<String, Object>> issues = new ArrayList<>();
        
        try {
            // Check for missing input validation
            if (code.contains("@RequestParam") && !code.contains("@Valid") && !code.contains("@NotNull")) {
                Map<String, Object> issue = new HashMap<>();
                issue.put("type", "Missing Input Validation");
                issue.put("severity", "HIGH");
                issue.put("suggestion", "Add validation annotations (@Valid, @NotNull, etc.)");
                issues.add(issue);
            }
            
            // Check for missing bounds checking
            if (code.contains("array") && code.contains("[") && !code.contains("length")) {
                Map<String, Object> issue = new HashMap<>();
                issue.put("type", "Missing Bounds Checking");
                issue.put("severity", "MEDIUM");
                issue.put("suggestion", "Check array bounds before access");
                issues.add(issue);
            }
            
        } catch (Exception e) {
            logger.debug("Could not scan input validation issues: {}", e.getMessage());
        }
        
        return issues;
    }
    
    /**
     * Get AI security analysis (STATIC - no ChatClient calls)
     */
    private List<String> getAISecurityAnalysis(String code, String language, String scanType) {
        List<String> analysis = new ArrayList<>();
        
        try {
            // ✅ STATIC: Return predefined security analysis instead of AI-generated
            analysis.add("1. 🔴 CRITICAL: Validate all user inputs to prevent injection attacks");
            analysis.add("2. 🔴 CRITICAL: Never hardcode secrets or credentials in code");
            analysis.add("3. 🟡 WARNING: Use parameterized queries for database operations");
            analysis.add("4. 🟡 WARNING: Implement proper authentication and authorization");
            analysis.add("5. 🟡 WARNING: Use strong encryption for sensitive data");
            
        } catch (Exception e) {
            logger.debug("Could not get security analysis: {}", e.getMessage());
            analysis.add("Unable to generate analysis at this time");
        }
        
        return analysis;
    }
    
    /**
     * Calculate security score
     */
    private int calculateSecurityScore(Map<String, Object> result) {
        int score = 100;
        
        try {
            List<Map<String, Object>> injections = (List<Map<String, Object>>) result.get("injectionVulnerabilities");
            List<Map<String, Object>> secrets = (List<Map<String, Object>>) result.get("hardcodedSecrets");
            List<Map<String, Object>> authIssues = (List<Map<String, Object>>) result.get("authenticationIssues");
            
            if (injections != null) score -= injections.size() * 20;
            if (secrets != null) score -= secrets.size() * 25;
            if (authIssues != null) score -= authIssues.size() * 15;
            
        } catch (Exception e) {
            logger.debug("Could not calculate security score: {}", e.getMessage());
        }
        
        return Math.max(0, score);
    }
    
    /**
     * Generate security summary
     */
    private String generateSecuritySummary(Map<String, Object> result) {
        try {
            int score = ((Number) result.get("securityScore")).intValue();
            
            if (score >= 80) {
                return "✅ Good security posture. Minor issues to address.";
            } else if (score >= 60) {
                return "⚠️ Moderate security concerns. Review and fix issues.";
            } else {
                return "🔴 Critical security issues found. Immediate action required!";
            }
            
        } catch (Exception e) {
            return "Security scan completed";
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
